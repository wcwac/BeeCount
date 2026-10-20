import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/providers.dart';
import 'package:beecount/providers/budget_providers.dart';
import 'package:beecount/providers/calendar_providers.dart';
import 'package:beecount/services/billing/post_processor.dart';
import 'package:drift/native.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('local transaction edits refresh budget, calendar and ledger totals', () async {
    SharedPreferences.setMockInitialValues({});
    final db = BeeDatabase.forTesting(NativeDatabase.memory());
    final repo = LocalRepository(db);
    final ledgerId = await repo.createLedger(name: 'Local');
    await repo.createBudget(ledgerId: ledgerId, type: 'total', amount: 1000);
    final container = ProviderContainer(overrides: [
      databaseProvider.overrideWithValue(db),
      repositoryProvider.overrideWithValue(repo),
      currentLedgerIdProvider.overrideWith((ref) => ledgerId),
    ]);
    addTearDown(() async {
      container.dispose();
      await db.close();
    });
    final now = DateTime.now();
    final month = DateTime(now.year, now.month, 1);
    final datesProvider = transactionDatesByMonthProvider(
        (ledgerId: ledgerId, month: month));
    final subscription = container.listen(datesProvider, (_, __) {});
    addTearDown(subscription.close);

    expect((await container.read(budgetOverviewProvider.future))!.totalBudget!.used, 0);
    expect(await container.read(datesProvider.future), isEmpty);
    expect((await container.read(localLedgersProvider.future)).single.transactionCount, 0);

    await repo.addTransaction(ledgerId: ledgerId, type: 'expense', amount: 125,
        happenedAt: now);
    final dateKey = now.toIso8601String().substring(0, 10);
    // Verify the database query separately from Riverpod invalidation: Drift
    // stores timestamps as epoch seconds, so SQLite must decode that format.
    expect(await repo.getTransactionDatesByMonth(ledgerId: ledgerId, month: month),
        [dateKey]);
    await PostProcessor.runC(container, ledgerId: ledgerId);

    expect((await container.read(budgetOverviewProvider.future))!.totalBudget!.used, 125);
    expect(await container.read(datesProvider.future), [dateKey]);
    expect((await container.read(localLedgersProvider.future)).single.transactionCount, 1);
    expect(await db.select(db.localChanges).get(), isEmpty);
  });
}
