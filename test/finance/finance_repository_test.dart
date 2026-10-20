import 'package:drift/drift.dart' show Value;
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/finance_repository.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/models/finance.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late BeeDatabase database;
  late LocalRepository local;
  late FinanceRepository finance;
  late int ledgerId;
  late int cashId;
  final opened = DateTime(2020, 1, 2);

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    database = BeeDatabase.forTesting(NativeDatabase.memory());
    local = LocalRepository(database);
    finance = FinanceRepository(database);
    ledgerId = await database.into(database.ledgers).insert(
      LedgersCompanion.insert(name: '本地财务'));
    cashId = await database.into(database.accounts).insert(
      AccountsCompanion.insert(ledgerId: ledgerId, name: '现金',
        initialBalance: const Value(1000)));
  });
  tearDown(() async => database.close());

  Future<int> create(FinanceKind kind, {int amount = 10000,
      int units = 1000000, bool cashFlow = true}) => finance.create(FinanceDraft(
    ledgerId: ledgerId, kind: kind, title: kind.name, amountCents: amount,
    openedAt: opened, cashAccountId: cashId, unitsMicros: units,
    recordCashFlow: cashFlow));

  Future<(double, double)> totals() => local.totalsInRange(ledgerId: ledgerId,
    start: DateTime(2020, 1, 1), end: DateTime(2021, 1, 1));

  Future<int> count(String table) async =>
    (await database.customSelect('SELECT COUNT(*) AS n FROM $table')
      .getSingle()).read<int>('n');

  test('lending moves principal without expense and only interest is income', () async {
    final id = await create(FinanceKind.lend);
    final plan = (await finance.get(id))!;
    expect(await local.getAccountBalance(cashId), 900);
    expect(await local.getAccountBalance(plan.accountId), 100);
    expect((await local.getNetWorthBreakdown()).netWorth, 1000);
    expect(await totals(), (0.0, 0.0));
    await finance.post(id, FinancePosting(action: FinanceAction.settle,
      happenedAt: DateTime(2020, 1, 3), cashAccountId: cashId,
      principalCents: 4000, interestCents: 500));
    expect((await finance.get(id))!.outstandingCents, 6000);
    expect(await local.getAccountBalance(cashId), 945);
    expect(await local.getAccountBalance(plan.accountId), 60);
    expect((await local.getNetWorthBreakdown()).netWorth, 1005);
    expect(await totals(), (5.0, 0.0));
    final history = await local.getAccountDailyBalances(plan.accountId,
      startDate: DateTime(2020, 1, 1), endDate: DateTime(2020, 1, 4));
    expect(history.map((v) => v.balance).toList(), [0, 100, 60, 60]);
  });

  test('debt stays in liabilities, principal is excluded from expense and budget', () async {
    final budget = await local.createBudget(ledgerId: ledgerId,
      type: 'total', amount: 1000);
    final id = await create(FinanceKind.borrow);
    final plan = (await finance.get(id))!;
    final worth = await local.getNetWorthBreakdown();
    expect(worth.totalAssets, 1100);
    expect(worth.totalLiabilities, -100);
    expect(worth.netWorth, 1000);
    await finance.post(id, FinancePosting(action: FinanceAction.settle,
      happenedAt: DateTime(2020, 1, 3), cashAccountId: cashId,
      principalCents: 4000, interestCents: 500));
    expect(await local.getAccountBalance(cashId), 1055);
    expect(await local.getAccountBalance(plan.accountId), -60);
    expect(await totals(), (0.0, 5.0));
    expect((await local.getBudgetUsage(budget, DateTime(2020, 1))).used, 5);
    final history = await local.getAccountDailyBalances(plan.accountId,
      startDate: DateTime(2020, 1, 1), endDate: DateTime(2020, 1, 3));
    expect(history.map((v) => v.balance).toList(), [0, -100, -60]);
  });

  test('reimbursement preserves original category and attachments and restores flags', () async {
    final category = await local.createCategory(name: '差旅', kind: 'expense');
    final txId = await local.addTransaction(ledgerId: ledgerId, type: 'expense',
      amount: 100, categoryId: category, accountId: cashId,
      happenedAt: opened, excludeFromStats: false, excludeFromBudget: true);
    await database.into(database.transactionAttachments).insert(
      TransactionAttachmentsCompanion.insert(transactionId: txId, fileName: 'receipt.jpg'));
    await database.into(database.transactionTags).insert(
      TransactionTagsCompanion.insert(transactionId: txId, tagId: 123));
    final id = await finance.create(FinanceDraft(ledgerId: ledgerId,
      kind: FinanceKind.reimbursement, title: '差旅报销', amountCents: 10000,
      openedAt: opened, existingExpenseId: txId));
    final linked = (await local.getTransactionById(txId))!;
    expect(linked.categoryId, category);
    expect(linked.excludeFromStats, isTrue);
    expect(await count('transaction_attachments'), 1);
    expect(await local.getAccountBalance(cashId), 900);
    // The ordinary deletion guard must run before file/tag cleanup.
    await expectLater(local.deleteTransaction(txId), throwsA(isA<FinanceFailure>()));
    await expectLater(local.deleteTransactionsBatchBySyncIds([linked.syncId!]),
      throwsA(isA<FinanceFailure>()));
    await expectLater(local.updateTransaction(id: txId, type: 'expense', amount: 200),
      throwsA(isA<FinanceFailure>()));
    expect((await local.getTransactionById(txId))!.amount, 100);
    expect(await count('transaction_attachments'), 1);
    expect(await count('transaction_tags'), 1);
    await finance.post(id, FinancePosting(action: FinanceAction.settle,
      happenedAt: DateTime(2020, 1, 3), cashAccountId: cashId, principalCents: 4000));
    expect(await local.getAccountBalance(cashId), 940);
    await finance.delete(id);
    final restored = (await local.getTransactionById(txId))!;
    expect(restored.excludeFromStats, isFalse);
    expect(restored.excludeFromBudget, isTrue);
    expect(restored.categoryId, category);
    expect(await count('transaction_attachments'), 1);
    expect(await count('transaction_tags'), 1);
    expect(await local.getAccountBalance(cashId), 900);
    expect(await totals(), (0.0, 100.0));
  });

  test('invalid settlement rolls back all financial and cash changes', () async {
    final id = await create(FinanceKind.lend);
    final eventsBefore = await count('finance_events');
    final txBefore = await count('transactions');
    await expectLater(finance.post(id, FinancePosting(action: FinanceAction.settle,
      happenedAt: DateTime(2020, 1, 3), cashAccountId: cashId, principalCents: 10001)),
      throwsA(isA<FinanceFailure>()));
    expect(await count('finance_events'), eventsBefore);
    expect(await count('transactions'), txBefore);
    expect((await finance.get(id))!.outstandingCents, 10000);
    expect(await local.getAccountBalance(cashId), 900);
  });

  test('investment weighted cost, partial sale and undo reconcile cash and value', () async {
    final id = await create(FinanceKind.investment, units: 2000000);
    await finance.post(id, FinancePosting(action: FinanceAction.buy,
      happenedAt: DateTime(2020, 1, 3), cashAccountId: cashId,
      amountCents: 20000, unitsMicros: 2000000));
    await finance.post(id, FinancePosting(action: FinanceAction.valuation,
      happenedAt: DateTime(2020, 1, 4), amountCents: 40000));
    await finance.post(id, FinancePosting(action: FinanceAction.sell,
      happenedAt: DateTime(2020, 1, 5), cashAccountId: cashId,
      amountCents: 12000, unitsMicros: 1000000));
    var plan = (await finance.get(id))!;
    expect(plan.unitsMicros, 3000000);
    expect(plan.costCents, 22500);
    expect(plan.valueCents, 30000);
    expect(plan.realizedProfitCents, 4500);
    expect(await local.getAccountBalance(cashId), 820);
    expect(await totals(), (45.0, 0.0));
    expect((await local.getLedgerStats(ledgerId: ledgerId)).balance, -180);
    await finance.undoLastEvent(id);
    plan = (await finance.get(id))!;
    expect(plan.unitsMicros, 4000000);
    expect(plan.costCents, 30000);
    expect(plan.valueCents, 40000);
    expect(plan.realizedProfitCents, 0);
    expect(await local.getAccountBalance(cashId), 700);
    expect(await totals(), (0.0, 0.0));
    final history = await local.getAccountDailyBalances(plan.accountId,
      startDate: DateTime(2020, 1, 1), endDate: DateTime(2020, 1, 5));
    expect(history.map((v) => v.balance).toList(), [0, 100, 300, 400, 400]);
  });

  test('selling final microscopic unit consumes cost and value rounding residue', () async {
    final id = await create(FinanceKind.investment, amount: 10001, units: 3);
    for (var i = 0; i < 3; i++) {
      await finance.post(id, FinancePosting(action: FinanceAction.sell,
        happenedAt: DateTime(2020, 1, 3 + i), cashAccountId: cashId,
        unitsMicros: 1, amountCents: i == 2 ? 3333 : 3334));
    }
    final plan = (await finance.get(id))!;
    expect(plan.unitsMicros, 0);
    expect(plan.costCents, 0);
    expect(plan.valueCents, 0);
    expect(plan.realizedProfitCents, 0);
    expect(await local.getAccountBalance(cashId), closeTo(1000, 0.000001));
    expect(plan.isClosed, isTrue);
  });

  test('investment dividends and realized losses affect cash and statistics once', () async {
    final id = await create(FinanceKind.investment);
    await finance.post(id, FinancePosting(action: FinanceAction.dividend,
      happenedAt: DateTime(2020, 1, 3), cashAccountId: cashId, amountCents: 500));
    await finance.post(id, FinancePosting(action: FinanceAction.sell,
      happenedAt: DateTime(2020, 1, 4), cashAccountId: cashId,
      amountCents: 8000, unitsMicros: 1000000));
    final plan = (await finance.get(id))!;
    expect(plan.isClosed, isTrue);
    expect(plan.realizedProfitCents, -1500);
    expect(await local.getAccountBalance(cashId), 985);
    expect(await local.getAccountBalance(plan.accountId), 0);
    expect(await totals(), (5.0, 20.0));
    expect((await local.getLedgerStats(ledgerId: ledgerId)).balance, -15);
    expect((await local.getNetWorthBreakdown()).netWorth, 985);
  });

  test('oversized purchase units cannot overflow the remaining-unit limit', () async {
    final id = await create(FinanceKind.investment);
    final before = await count('finance_events');
    await expectLater(finance.post(id, FinancePosting(action: FinanceAction.buy,
      happenedAt: DateTime(2020, 1, 3), cashAccountId: cashId,
      amountCents: 100, unitsMicros: 9223372036854775807)),
      throwsA(isA<FinanceFailure>()));
    expect(await count('finance_events'), before);
    expect((await finance.get(id))!.unitsMicros, 1000000);
    expect(await local.getAccountBalance(cashId), 900);
  });

  test('clearing one ledger preserves another plan using the same cash account', () async {
    await create(FinanceKind.lend);
    final otherLedger = await local.createLedger(name: '另一个账本');
    final otherId = await finance.create(FinanceDraft(ledgerId: otherLedger,
      kind: FinanceKind.borrow, title: '其他账本借款', amountCents: 20000,
      openedAt: opened, cashAccountId: cashId));
    final other = (await finance.get(otherId))!;
    expect(await local.getAccountBalance(cashId), 1100);
    await local.clearLedgerTransactions(ledgerId);
    expect(await finance.list(ledgerId), isEmpty);
    expect((await finance.get(otherId))!.outstandingCents, 20000);
    expect(await local.getAccountBalance(other.accountId), -200);
    expect(await local.getAccountBalance(cashId), 1200);
    expect(await count('finance_transaction_links'), 1);
    expect((await local.getNetWorthBreakdown()).netWorth, 1000);
  });

  test('mortgage periods cannot be skipped or recorded twice and undo frees period', () async {
    final id = await finance.create(FinanceDraft(ledgerId: ledgerId,
      kind: FinanceKind.mortgage, title: '房贷', amountCents: 120000,
      annualRateBps: 1200, periods: 12, method: RepaymentMethod.equalPrincipal,
      openedAt: opened, firstDueAt: DateTime(2020, 2, 2), cashAccountId: cashId));
    final posting = FinancePosting(action: FinanceAction.settle,
      happenedAt: DateTime(2020, 2, 2), cashAccountId: cashId, periodNumber: 1,
      principalCents: 10000, interestCents: 1200);
    await finance.post(id, posting);
    await expectLater(finance.post(id, posting), throwsA(isA<FinanceFailure>()));
    expect((await finance.events(id)).length, 2);
    expect((await finance.get(id))!.outstandingCents, 110000);
    await finance.undoLastEvent(id);
    await finance.post(id, posting);
    expect((await finance.get(id))!.outstandingCents, 110000);
    expect(await totals(), (0.0, 12.0));
  });

  test('clearing a ledger is atomic across plan, mirror, links and transactions', () async {
    final id = await create(FinanceKind.borrow);
    final managedId = (await finance.get(id))!.accountId;
    await database.customStatement('''CREATE TRIGGER test_fail_clear
      BEFORE DELETE ON transactions BEGIN SELECT RAISE(ABORT, 'simulate write failure'); END''');
    await expectLater(local.clearLedgerTransactions(ledgerId), throwsA(anything));
    expect(await finance.get(id), isNotNull);
    expect(await local.getAccount(managedId), isNotNull);
    expect(await count('finance_transaction_links'), 1);
    expect(await count('finance_events'), 1);
    expect(await local.getAccountBalance(cashId), 1100);
    await database.customStatement('DROP TRIGGER test_fail_clear');
    await local.clearLedgerTransactions(ledgerId);
    expect(await finance.list(ledgerId), isEmpty);
    expect(await local.getAccount(managedId), isNull);
    expect(await count('finance_events'), 0);
    expect(await count('finance_transaction_links'), 0);
    expect(await count('transactions'), 0);
    expect(await local.getAccount(cashId), isNotNull);
    expect(await local.getAccountBalance(cashId), 1000);
  });

  test('ordinary account edits cannot desynchronise a managed position', () async {
    final id = await create(FinanceKind.borrow);
    final managedId = (await finance.get(id))!.accountId;
    await expectLater(local.updateAccountValuation(managedId, -12),
      throwsA(isA<FinanceFailure>()));
    await expectLater(local.deleteAccount(managedId), throwsA(isA<FinanceFailure>()));
    await expectLater(local.updateAccount(managedId, type: 'cash'),
      throwsA(isA<FinanceFailure>()));
    await expectLater(local.addTransaction(ledgerId: ledgerId, type: 'income',
      accountId: managedId, amount: 12, happenedAt: opened),
      throwsA(isA<FinanceFailure>()));
    await local.updateAccount(managedId, name: '允许改显示名称');
    expect(await local.getAccountBalance(managedId), -100);
    expect((await finance.get(id))!.outstandingCents, 10000);
  });
}
