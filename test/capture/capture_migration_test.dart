import 'dart:io';
import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  test('v34 upgrade adds capture metadata without changing ledger balances', () async {
    SharedPreferences.setMockInitialValues({});
    final directory = await Directory.systemTemp.createTemp('capture-migration-');
    final file = File('${directory.path}/ledger.sqlite');
    var db = BeeDatabase.forTesting(NativeDatabase(file));
    try {
      var repo = LocalRepository(db);
      final ledger = await repo.createLedger(name: 'Existing ledger');
      final account = await repo.createAccount(ledgerId: ledger, name: 'Cash', initialBalance: 100);
      final id = await repo.addTransaction(ledgerId: ledger, type: 'expense', amount: 12.5,
        accountId: account, happenedAt: DateTime(2026, 1, 1));
      await db.customStatement('DROP TRIGGER capture_preserve_deleted');
      await db.customStatement('DROP TABLE transaction_capture_links');
      await db.customStatement('PRAGMA user_version=34');
      await db.close();
      db = BeeDatabase.forTesting(NativeDatabase.createInBackground(file));
      repo = LocalRepository(db);
      expect((await repo.getTransactionById(id))!.amount, 12.5);
      expect(await repo.getAccountBalance(account), closeTo(87.5, .00001));
      expect(await db.customSelect('SELECT * FROM transaction_capture_links').get(), isEmpty);
      expect((await db.customSelect('PRAGMA user_version').getSingle()).read<int>('user_version'), 35);
    } finally {
      await db.close();
      await directory.delete(recursive: true);
    }
  });
}
