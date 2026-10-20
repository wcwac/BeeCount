import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/finance_repository.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/models/finance.dart';
import 'package:beecount/services/backup/local_database_backup.dart';
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late BeeDatabase db;
  late LocalRepository local;
  late FinanceRepository finance;
  late LocalDatabaseBackupService backup;
  late int ledgerId;

  setUp(() async {
    SharedPreferences.setMockInitialValues({'private_key': 'not-in-database'});
    db = BeeDatabase.forTesting(NativeDatabase.memory());
    local = LocalRepository(db);
    finance = FinanceRepository(db);
    backup = LocalDatabaseBackupService(db);
    ledgerId = await local.createLedger(name: 'Backup ledger');
  });
  tearDown(() async => db.close());

  Future<Uint8List> snapshotWithFinance() async {
    final cash = await local.createAccount(ledgerId: ledgerId, name: 'Cash',
        initialBalance: 1000);
    await finance.create(FinanceDraft(ledgerId: ledgerId, kind: FinanceKind.borrow,
        title: 'Borrowed money', amountCents: 50000, cashAccountId: cash,
        openedAt: DateTime.now()));
    return backup.exportBytes();
  }

  Map<String, dynamic> decode(Uint8List bytes) =>
      jsonDecode(utf8.decode(bytes)) as Map<String, dynamic>;
  Uint8List encode(Map<String, dynamic> data) =>
      Uint8List.fromList(utf8.encode(jsonEncode(data)));

  test('full restore roundtrips financial events, links, ids and raw timestamps', () async {
    final bytes = await snapshotWithFinance();
    final before = decode(bytes);
    expect(before['tables']['finance_plans']['rows'], isNotEmpty);
    expect(before['tables']['finance_events']['rows'], isNotEmpty);
    expect(before['tables']['finance_transaction_links']['rows'], isNotEmpty);
    expect(before['tables'].containsKey('agent_memory_fts'), isFalse);
    expect(utf8.decode(bytes), isNot(contains('not-in-database')));
    final txTable = before['tables']['transactions'];
    final dateIndex = txTable['columns'].indexOf('happened_at') as int;
    expect(txTable['rows'].first[dateIndex], isA<int>());

    final snapshot = await backup.inspectBytes(bytes);
    await local.createLedger(name: 'Will be replaced');
    await finance.create(FinanceDraft(ledgerId: ledgerId, kind: FinanceKind.lend,
        title: 'Later plan', amountCents: 1000, recordCashFlow: false,
        openedAt: DateTime.now()));
    expect(await backup.restore(snapshot), ledgerId);
    final after = decode(await backup.exportBytes());
    expect(after['tables'], before['tables']);
    expect((await finance.list(ledgerId)).single.title, 'Borrowed money');
    final plan = (await finance.list(ledgerId)).single;
    expect(await finance.events(plan.id), isNotEmpty);
    // Guard triggers are retained after restoring links.
    await expectLater(db.customStatement('DELETE FROM transactions'), throwsA(anything));
    expect(decode(await backup.exportBytes())['tables'], before['tables']);
  });

  test('rejects schema, unknown table, wrong columns and broken finance links before changing data', () async {
    final bytes = await snapshotWithFinance();
    final original = decode(bytes)['tables'];
    for (final mutate in <void Function(Map<String, dynamic>)>[
      (data) => data['schemaVersion'] = 33,
      (data) => data['tables']['sqlite_master'] = {'columns': [], 'rows': []},
      (data) => data['tables']['ledgers']['columns'][0] = 'id); DROP TABLE accounts;',
      (data) {
        final table = data['tables']['finance_transaction_links'];
        final index = table['columns'].indexOf('event_id') as int;
        table['rows'][0][index] = 99999999;
      },
    ]) {
      final data = decode(bytes);
      mutate(data);
      await expectLater(backup.inspectBytes(encode(data)), throwsFormatException);
      expect(decode(await backup.exportBytes())['tables'], original);
    }
  });

  test('rejects inconsistent financial models before restoring', () async {
    final bytes = await snapshotWithFinance();
    void setCell(Map<String, dynamic> data, String name, String column,
        Object? value, {int row = 0}) {
      final table = data['tables'][name];
      table['rows'][row][table['columns'].indexOf(column)] = value;
    }
    for (final mutate in <void Function(Map<String, dynamic>)>[
      (d) => setCell(d, 'finance_plans', 'kind', 'unknown'),
      (d) => setCell(d, 'finance_plans', 'method', 'unknown'),
      (d) => setCell(d, 'finance_plans', 'opened_at', 'invalid'),
      (d) => setCell(d, 'finance_events', 'happened_at', 'invalid'),
      (d) => setCell(d, 'finance_events', 'units_delta_micros', 1000000000000001),
      (d) => setCell(d, 'finance_events', 'principal_delta_cents', -1),
      (d) => setCell(d, 'finance_events', 'value_after_cents', 1),
      (d) => d['tables']['finance_events']['rows'] = [],
      (d) => setCell(d, 'accounts', 'initial_balance', 0, row: 1),
      (d) => setCell(d, 'accounts', 'type', 'cash', row: 1),
      (d) => setCell(d, 'accounts', 'currency', 'USD', row: 1),
      (d) => setCell(d, 'transactions', 'ledger_id', ledgerId + 1),
      (d) => setCell(d, 'finance_transaction_links', 'owned', 0),
    ]) {
      final data = decode(bytes);
      mutate(data);
      await expectLater(backup.inspectBytes(encode(data)), throwsFormatException);
      expect(decode(await backup.exportBytes())['tables'], decode(bytes)['tables']);
    }
  });

  test('investment backup rejects closed units with remaining cost', () async {
    await finance.create(FinanceDraft(ledgerId: ledgerId,
        kind: FinanceKind.investment, title: 'Investment', amountCents: 10000,
        unitsMicros: 1000000, recordCashFlow: false, openedAt: DateTime.now()));
    final bytes = await backup.exportBytes();
    await backup.inspectBytes(bytes);
    final data = decode(bytes);
    final events = data['tables']['finance_events'];
    events['rows'][0][events['columns'].indexOf('units_delta_micros')] = 0;
    await expectLater(backup.inspectBytes(encode(data)), throwsFormatException);
    expect(decode(await backup.exportBytes())['tables'], decode(bytes)['tables']);
  });

  test('insert failure after deletion rolls back all original rows', () async {
    final bytes = await snapshotWithFinance();
    final data = decode(bytes);
    final accounts = data['tables']['accounts'];
    accounts['rows'].add(List<Object?>.from(accounts['rows'].first as List));
    // Duplicate primary key is intentionally left to SQLite's real constraint.
    final invalid = await backup.inspectBytes(encode(data));
    await expectLater(backup.restore(invalid), throwsA(anything));
    expect(decode(await backup.exportBytes())['tables'], decode(bytes)['tables']);
    expect((await finance.list(ledgerId)), hasLength(1));
  });

  test('same schema accepts different physical column orders', () async {
    final bytes = await snapshotWithFinance();
    final data = decode(bytes);
    for (final table in (data['tables'] as Map).values) {
      table['columns'] = (table['columns'] as List).reversed.toList();
      table['rows'] = [for (final row in table['rows'] as List)
        (row as List).reversed.toList()];
    }
    await backup.restore(await backup.inspectBytes(encode(data)));
    expect(decode(await backup.exportBytes())['tables'], decode(bytes)['tables']);
  });

  test('Drift watchers receive the restored data after commit', () async {
    final bytes = await backup.exportBytes();
    await local.updateLedgerName(id: ledgerId, name: 'Changed');
    final watcher = StreamIterator(local.watchLedgers());
    addTearDown(watcher.cancel);
    expect(await watcher.moveNext(), isTrue);
    expect(watcher.current.single.name, 'Changed');
    final next = watcher.moveNext();
    await backup.restore(await backup.inspectBytes(bytes));
    expect(await next.timeout(const Duration(seconds: 5)), isTrue);
    expect(watcher.current.single.name, 'Backup ledger');
  });
}
