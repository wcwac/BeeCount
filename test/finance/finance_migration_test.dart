import 'dart:io';
import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/finance_repository.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/models/finance.dart';
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  test(
      'v33 upgrade preserves old accounts and transactions and creates finance',
      () async {
    SharedPreferences.setMockInitialValues({});
    final dir = await Directory.systemTemp.createTemp('finance-migration-');
    final file = File('${dir.path}/test.sqlite');
    var db = BeeDatabase.forTesting(NativeDatabase(file));
    try {
      var repo = LocalRepository(db);
      final ledger = await repo.createLedger(name: 'Old');
      final account = await repo.createAccount(
          ledgerId: ledger, name: 'Cash', initialBalance: 123.45);
      final tx = await repo.addTransaction(
          ledgerId: ledger,
          type: 'expense',
          amount: 23.45,
          accountId: account,
          happenedAt: DateTime(2026, 1, 1));
      // Reproduce the previously shipped schema (all older tables unchanged).
      for (final name in [
        'finance_guard_tx_update',
        'finance_guard_tx_delete',
        'finance_guard_account_delete',
        'finance_guard_account_type'
      ]) {
        await db.customStatement('DROP TRIGGER $name');
      }
      for (final name in [
        'finance_transaction_links',
        'finance_events',
        'finance_plans'
      ]) {
        await db.customStatement('DROP TABLE $name');
      }
      await db.customStatement('PRAGMA user_version=33');
      await db.close();
      db = BeeDatabase.forTesting(NativeDatabase(file));
      repo = LocalRepository(db);
      expect((await repo.getTransactionById(tx))!.amount, 23.45);
      expect(await repo.getAccountBalance(account), closeTo(100, .00001));
      final finance = FinanceRepository(db);
      expect(await finance.list(ledger), isEmpty);
      final id = await finance.create(FinanceDraft(
          ledgerId: ledger,
          kind: FinanceKind.lend,
          title: 'Post-upgrade receivable',
          amountCents: 5000,
          openedAt: DateTime(2026, 1, 2),
          cashAccountId: account));
      expect((await finance.get(id))!.outstandingCents, 5000);
      expect(await repo.getAccountBalance(account), closeTo(50, .00001));
      expect(
          (await db.customSelect('PRAGMA user_version').getSingle())
              .read<int>('user_version'),
          34);
    } finally {
      await db.close();
      await dir.delete(recursive: true);
    }
  });
}
