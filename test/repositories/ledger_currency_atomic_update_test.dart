import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late BeeDatabase db;
  late LocalRepository repo;
  late int ledgerId;
  late int transactionId;

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    db = BeeDatabase.forTesting(NativeDatabase.memory());
    repo = LocalRepository(db);
    ledgerId = await repo.createLedger(name: 'Before', currency: 'CNY');
    transactionId = await repo.addTransaction(
      ledgerId: ledgerId, type: 'expense', amount: 10,
      happenedAt: DateTime.now(), currencyCode: 'USD', nativeAmount: 70,
    );
  });
  tearDown(() async => db.close());

  test('changing base currency recalculates stored transaction amounts', () async {
    await repo.updateLedger(id: ledgerId, name: 'After', currency: 'USD');
    expect((await repo.getLedgerById(ledgerId))!.currency, 'USD');
    expect((await repo.getTransactionById(transactionId))!.nativeAmount, 10);
  });

  test('failed amount conversion rolls back ledger name and currency together', () async {
    await db.customStatement('''
      CREATE TRIGGER reject_conversion BEFORE UPDATE OF native_amount ON transactions
      BEGIN SELECT RAISE(ABORT, 'conversion rejected'); END
    ''');
    await expectLater(
      repo.updateLedger(id: ledgerId, name: 'After', currency: 'USD'),
      throwsA(anything),
    );
    final ledger = (await repo.getLedgerById(ledgerId))!;
    expect(ledger.name, 'Before');
    expect(ledger.currency, 'CNY');
    expect((await repo.getTransactionById(transactionId))!.nativeAmount, 70);
  });
}
