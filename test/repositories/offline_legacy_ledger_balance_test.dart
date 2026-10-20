import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:drift/drift.dart' as d;
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('legacy editor ledger local transactions and balance adjustments agree', () async {
    SharedPreferences.setMockInitialValues({});
    final db = BeeDatabase.forTesting(NativeDatabase.memory());
    addTearDown(db.close);
    final repo = LocalRepository(db);
    final ledgerId = await repo.createLedger(name: 'Legacy local copy');
    await (db.update(db.ledgers)..where((l) => l.id.equals(ledgerId))).write(
      const LedgersCompanion(isShared: d.Value(true), myRole: d.Value('editor')),
    );
    final accountId = await repo.createAccount(
        ledgerId: ledgerId, name: 'Cash', initialBalance: 1000);
    final targetId = await repo.createAccount(ledgerId: ledgerId, name: 'Bank');
    final today = DateTime.now();
    await repo.addTransaction(ledgerId: ledgerId, type: 'expense', amount: 125,
        accountId: accountId, happenedAt: today);
    await repo.addTransaction(ledgerId: ledgerId, type: 'transfer', amount: 75,
        accountId: accountId, toAccountId: targetId, happenedAt: today);

    expect(await repo.getAccountBalance(accountId), 800);
    expect(await repo.getAccountGlobalBalance(accountId), 800);
    expect(await repo.getAccountBalance(targetId), 75);
    expect(await repo.getAccountExpense(accountId), 200);
    expect(await repo.getAccountIncome(targetId), 75);
    expect(await repo.getAccountTransactions(accountId), hasLength(2));

    final result = await repo.setAccountBalance(
        ledgerId: ledgerId, accountId: accountId, targetBalance: 900,
        createBalanceAdjustment: true, happenedAt: today);
    expect(result.newBalance, 900);
    expect(await repo.getAccountBalance(accountId), result.newBalance);
    expect(await repo.getAccountGlobalBalance(accountId), result.newBalance);
    expect((await repo.getNetWorthBreakdown()).netWorth, 975);
    final trend = await repo.getAccountDailyBalances(accountId,
        startDate: DateTime(today.year, today.month, today.day), endDate: today);
    expect(trend.single.balance, 900);
    expect(await db.select(db.localChanges).get(), isEmpty);
  });
}
