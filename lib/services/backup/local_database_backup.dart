import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:drift/drift.dart' as d;

import '../../data/db.dart';
import '../../models/finance.dart';

/// A validated snapshot, held in memory between preview and confirmation.
/// It contains database rows only; preferences and attachment bytes are excluded.
class LocalDatabaseSnapshot {
  LocalDatabaseSnapshot._(this._tables, this.rowCount);
  final Map<String, _BackupTable> _tables;
  final int rowCount;
}

class _BackupTable {
  _BackupTable(this.columns, this.rows);
  final List<String> columns;
  final List<List<Object?>> rows;
}

class LocalDatabaseBackupService {
  LocalDatabaseBackupService(this.db);
  final BeeDatabase db;
  static const maxBytes = 100 * 1024 * 1024;
  static const _format = 'beecount-local-database';
  static const _schemaVersion = 34;
  static const _financeTables = [
    'finance_plans', 'finance_events', 'finance_transaction_links',
  ];

  String _quoted(String name) => '"${name.replaceAll('"', '""')}"';
  Never _invalid(String detail) => throw FormatException(
      '备份无效或版本不兼容 / Invalid or incompatible backup: $detail');

  Future<Map<String, List<String>>> _schema() async {
    // Accessing sqlite_master opens/migrates the database before inspection.
    final existing = await db.customSelect(
        "SELECT name FROM sqlite_master WHERE type='table'").get();
    final names = existing.map((r) => r.read<String>('name')).toSet();
    final version = await db.customSelect('PRAGMA user_version').getSingle();
    if (db.schemaVersion != _schemaVersion ||
        version.data.values.single != _schemaVersion) {
      _invalid('database schema must be $_schemaVersion');
    }
    final allowed = {
      for (final table in db.allTables) table.actualTableName,
      ..._financeTables,
    };
    final schema = <String, List<String>>{};
    for (final name in allowed) {
      if (name.startsWith('sqlite_') || name.startsWith('agent_memory_fts') ||
          !names.contains(name)) _invalid('missing table $name');
      final columns = await db.customSelect('PRAGMA table_info(${_quoted(name)})').get();
      schema[name] = columns.map((r) => r.read<String>('name')).toList();
    }
    return schema;
  }

  Future<Uint8List> exportBytes() => db.transaction(() async {
    final schema = await _schema();
    final tables = <String, Object?>{};
    for (final entry in schema.entries) {
      final rows = await db.customSelect('SELECT * FROM ${_quoted(entry.key)}').get();
      tables[entry.key] = {
        'columns': entry.value,
        'rows': [for (final row in rows)
          [for (final column in entry.value) _encodeValue(row.data[column])]],
      };
    }
    final bytes = Uint8List.fromList(utf8.encode(jsonEncode({
      'format': _format,
      'version': 1,
      'schemaVersion': _schemaVersion,
      'createdAt': DateTime.now().toUtc().toIso8601String(),
      'tables': tables,
    })));
    if (bytes.length > maxBytes) _invalid('larger than 100 MB');
    return bytes;
  });

  Object? _encodeValue(Object? value) => value is Uint8List
      ? {'blobBase64': base64Encode(value)} : value;

  Object? _decodeValue(Object? value) {
    if (value == null || value is String || value is int) return value;
    if (value is double && value.isFinite) return value;
    if (value is Map && value.length == 1 && value['blobBase64'] is String) {
      return base64Decode(value['blobBase64'] as String);
    }
    _invalid('unsupported cell value');
  }

  /// Bounded streaming read also handles a file growing after its size check.
  Future<Uint8List> readFile(File file) async {
    if (await file.length() > maxBytes) _invalid('larger than 100 MB');
    final data = BytesBuilder(copy: false);
    await for (final chunk in file.openRead()) {
      if (data.length + chunk.length > maxBytes) _invalid('larger than 100 MB');
      data.add(chunk);
    }
    return data.takeBytes();
  }

  Future<LocalDatabaseSnapshot> inspectBytes(Uint8List bytes) async {
    if (bytes.isEmpty || bytes.length > maxBytes) _invalid('file size');
    final document = jsonDecode(utf8.decode(bytes));
    if (document is! Map || document['format'] != _format ||
        document['version'] != 1 || document['schemaVersion'] != _schemaVersion ||
        document['tables'] is! Map) _invalid('format/version');
    final schema = await _schema();
    final rawTables = document['tables'] as Map;
    if (rawTables.length != schema.length ||
        !rawTables.keys.every(schema.containsKey)) _invalid('table set');
    final tables = <String, _BackupTable>{};
    var count = 0;
    for (final entry in schema.entries) {
      final raw = rawTables[entry.key];
      if (raw is! Map || raw['columns'] is! List || raw['rows'] is! List) {
        _invalid('table ${entry.key}');
      }
      final columns = raw['columns'] as List;
      if (columns.length != entry.value.length ||
          columns.toSet().length != columns.length ||
          !columns.every((column) => column is String && entry.value.contains(column))) {
        _invalid('columns of ${entry.key}');
      }
      final rows = <List<Object?>>[];
      for (final row in raw['rows'] as List) {
        if (row is! List || row.length != columns.length) _invalid('row shape');
        rows.add(List.unmodifiable(row.map(_decodeValue)));
      }
      tables[entry.key] = _BackupTable(List<String>.unmodifiable(columns.cast<String>()), List.unmodifiable(rows));
      count += rows.length;
    }
    _validateFinanceReferences(tables);
    return LocalDatabaseSnapshot._(Map.unmodifiable(tables), count);
  }

  void _validateFinanceReferences(Map<String, _BackupTable> tables) {
    List<Map<String, Object?>> rows(String name) {
      final table = tables[name]!;
      return [for (final row in table.rows)
        {for (var i = 0; i < table.columns.length; i++) table.columns[i]: row[i]}];
    }
    int integer(Map<String, Object?> row, String key) {
      final value = row[key];
      if (value is! int) _invalid('financial integer $key');
      return value;
    }
    String text(Map<String, Object?> row, String key) {
      final value = row[key];
      if (value is! String) _invalid('financial text $key');
      return value;
    }
    DateTime date(Map<String, Object?> row, String key) {
      final value = DateTime.tryParse(text(row, key));
      if (value == null) _invalid('financial date $key');
      return value;
    }
    Map<Object?, Map<String, Object?>> indexed(String name) => {
      for (final row in rows(name)) row['id']: row,
    };
    final ledgers = indexed('ledgers'), accounts = indexed('accounts');
    final plans = indexed('finance_plans'), events = indexed('finance_events');
    final transactions = indexed('transactions');
    if (plans.length != tables['finance_plans']!.rows.length ||
        events.length != tables['finance_events']!.rows.length) {
      _invalid('duplicate financial id');
    }
    final byPlan = <int, List<Map<String, Object?>>>{};
    for (final event in events.values) {
      if (integer(event, 'id') <= 0) _invalid('financial event id');
      final planId = integer(event, 'plan_id');
      if (!plans.containsKey(planId)) _invalid('financial event references');
      for (final key in ['principal_delta_cents', 'units_delta_micros',
          'profit_cents', 'interest_cents', 'cash_cents', 'value_after_cents']) {
        integer(event, key);
      }
      if (integer(event, 'interest_cents') < 0 ||
          integer(event, 'value_after_cents') < 0) _invalid('negative financial value');
      if (event['period_number'] != null) integer(event, 'period_number');
      text(event, 'note');
      date(event, 'happened_at');
      (byPlan[planId] ??= []).add(event);
    }
    final managedAccounts = <int>{};
    for (final plan in plans.values) {
      final id = integer(plan, 'id');
      final ledgerId = integer(plan, 'ledger_id');
      final accountId = integer(plan, 'account_id');
      final account = accounts[accountId];
      if (id <= 0 || !ledgers.containsKey(ledgerId) || account == null ||
          !managedAccounts.add(accountId)) _invalid('financial plan references');
      final kindName = text(plan, 'kind');
      if (!FinanceKind.values.any((kind) => kind.name == kindName) ||
          !RepaymentMethod.values.any((method) => method.name == plan['method'])) {
        _invalid('financial enum');
      }
      final kind = FinanceKind.values.byName(kindName);
      for (final key in ['title', 'party', 'note', 'currency']) { text(plan, key); }
      final opened = date(plan, 'opened_at');
      final due = date(plan, 'first_due_at');
      final principal = integer(plan, 'principal_cents');
      final rate = integer(plan, 'annual_rate_bps');
      final periods = integer(plan, 'periods');
      if (principal <= 0 || principal > 100000000000000 ||
          (kind.isScheduled && (periods < 1 || periods > 600 ||
              principal < periods || rate < 0 || rate > 10000 || due.isBefore(opened)))) {
        _invalid('financial plan amount/schedule');
      }
      if (account['type'] != kind.accountType ||
          account['currency'] != plan['currency'] || account['ledger_id'] != ledgerId) {
        _invalid('financial account type/currency/ledger');
      }
      final history = byPlan[id] ?? [];
      history.sort((a, b) => integer(a, 'id').compareTo(integer(b, 'id')));
      if (history.isEmpty || history.first['action'] != 'opening' ||
          history.where((event) => event['action'] == 'opening').length != 1 ||
          history.first['principal_delta_cents'] != principal) {
        _invalid('financial opening event');
      }
      var cost = 0, units = 0, value = 0;
      var previousDate = opened;
      for (final event in history) {
        final action = text(event, 'action');
        final allowed = kind == FinanceKind.investment
            ? {'opening', 'buy', 'sell', 'dividend', 'valuation'}
            : {'opening', 'settle'};
        if (!allowed.contains(action)) _invalid('financial event action');
        final happened = date(event, 'happened_at');
        if (happened.isBefore(previousDate)) _invalid('financial event date order');
        previousDate = happened;
        cost += integer(event, 'principal_delta_cents');
        units += integer(event, 'units_delta_micros');
        value = integer(event, 'value_after_cents');
        if (cost < 0 || cost > 100000000000000 || units < 0 ||
            units > 1000000000000000 || value > 100000000000000 ||
            (kind == FinanceKind.investment && units == 0 && (cost != 0 || value != 0)) ||
            (kind != FinanceKind.investment && (units != 0 || value != cost))) {
          _invalid('financial event balance/units');
        }
      }
      final balance = account['initial_balance'];
      final expected = (kind.isDebt ? -1 : 1) * value;
      if (balance is! num || !balance.isFinite ||
          (balance * 100 - expected).abs() > 0.05) {
        _invalid('financial account balance mirror');
      }
    }
    for (final link in rows('finance_transaction_links')) {
      final event = events[integer(link, 'event_id')];
      final transaction = transactions[integer(link, 'transaction_id')];
      if (event == null || transaction == null) _invalid('financial transaction references');
      final plan = plans[event['plan_id']]!;
      if (transaction['ledger_id'] != plan['ledger_id']) _invalid('financial transaction ledger');
      for (final key in ['owned', 'original_exclude_stats', 'original_exclude_budget']) {
        if (![0, 1].contains(integer(link, key))) _invalid('financial link flag');
      }
      if (link['owned'] == 0 && (plan['kind'] != 'reimbursement' ||
          event['action'] != 'opening' || transaction['type'] != 'expense' ||
          transaction['exclude_from_stats'] != 1 || transaction['exclude_from_budget'] != 1)) {
        _invalid('linked reimbursement must remain excluded');
      }
    }
  }

  /// Replaces rows only, preserving schema, indexes and financial guard triggers.
  /// No change is committed if any insert/constraint check fails.
  Future<int?> restore(LocalDatabaseSnapshot snapshot) async {
    final firstLedger = await db.transaction(() async {
      final schema = await _schema();
      if (schema.length != snapshot._tables.length ||
          !schema.keys.every(snapshot._tables.containsKey)) _invalid('schema changed');
      for (final entry in schema.entries) {
        final columns = snapshot._tables[entry.key]!.columns;
        if (columns.length != entry.value.length ||
            !columns.every(entry.value.contains)) _invalid('columns changed');
      }
      await db.customStatement('PRAGMA defer_foreign_keys = ON');
      // Release financial guard triggers before deleting the referenced rows.
      for (final name in _financeTables.reversed) {
        await db.customStatement('DELETE FROM ${_quoted(name)}');
      }
      final ordinary = schema.keys.where((name) => !_financeTables.contains(name)).toList();
      for (final name in ordinary.reversed) {
        await db.customStatement('DELETE FROM ${_quoted(name)}');
      }
      for (final name in [...ordinary, ..._financeTables]) {
        final table = snapshot._tables[name]!;
        final sql = 'INSERT INTO ${_quoted(name)} '
            '(${table.columns.map(_quoted).join(',')}) VALUES '
            '(${List.filled(table.columns.length, '?').join(',')})';
        for (final row in table.rows) {
          await db.customStatement(sql, row);
        }
      }
      // FTS is a derived index, not part of the portable backup.
      await db.customStatement('DELETE FROM agent_memory_fts');
      await db.customStatement('INSERT INTO agent_memory_fts(memory_id,content) '
          'SELECT id,content FROM agent_memories');
      if ((await db.customSelect('PRAGMA foreign_key_check').get()).isNotEmpty) {
        _invalid('foreign key integrity');
      }
      final ledger = await db.customSelect('SELECT id FROM ledgers ORDER BY id LIMIT 1').getSingleOrNull();
      return ledger?.read<int>('id');
    });
    db.notifyUpdates({
      for (final name in snapshot._tables.keys) d.TableUpdate(name),
    });
    return firstLedger;
  }
}
