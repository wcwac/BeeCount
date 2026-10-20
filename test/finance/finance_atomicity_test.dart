import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/finance_repository.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/models/finance.dart';
import 'package:beecount/services/export/config_export_service.dart';
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late BeeDatabase db;
  late FinanceRepository finance;
  late LocalRepository local;
  late int ledgerId, cashId;
  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    db = BeeDatabase.forTesting(NativeDatabase.memory());
    local = LocalRepository(db);
    finance = FinanceRepository(db);
    ledgerId = await local.createLedger(name: 'Local finance');
    cashId = await local.createAccount(
        ledgerId: ledgerId, name: 'Bank', initialBalance: 1000);
  });
  tearDown(() async => db.close());

  test(
      'failure after inserting cash flow rolls back event, links and all balances',
      () async {
    final id = await finance.create(FinanceDraft(
        ledgerId: ledgerId,
        kind: FinanceKind.lend,
        title: 'Loan',
        amountCents: 10000,
        cashAccountId: cashId,
        openedAt: DateTime(2020, 1, 1)));
    final plan = (await finance.get(id))!;
    await db.customStatement('''CREATE TRIGGER reject_finance_mirror
      BEFORE UPDATE OF initial_balance ON accounts
      WHEN OLD.id=${plan.accountId}
      BEGIN SELECT RAISE(ABORT, 'mirror rejected'); END''');
    await expectLater(
        finance.post(
            id,
            FinancePosting(
                action: FinanceAction.settle,
                happenedAt: DateTime(2020, 1, 2),
                principalCents: 5000,
                interestCents: 500,
                cashAccountId: cashId)),
        throwsA(anything));
    expect(await finance.events(id), hasLength(1));
    expect((await finance.get(id))!.outstandingCents, 10000);
    expect(await local.getAccountBalance(cashId), 900);
    expect(await local.getAccountBalance(plan.accountId), 100);
    expect(await local.getTransactionsByLedger(ledgerId), hasLength(1));
    expect(
        (await db
                .customSelect(
                    'SELECT COUNT(*) AS n FROM finance_transaction_links')
                .getSingle())
            .read<int>('n'),
        1);
  });

  test(
      'old plan keeps currency when ledger currency changes and snapshots recalculate',
      () async {
    final id = await finance.create(FinanceDraft(
        ledgerId: ledgerId,
        kind: FinanceKind.lend,
        title: 'CNY receivable',
        amountCents: 10000,
        cashAccountId: cashId,
        openedAt: DateTime(2020, 1, 1)));
    await local.setOverride(base: 'USD', quote: 'CNY', rate: '0.14');
    await local.updateLedger(id: ledgerId, currency: 'USD');
    expect((await finance.get(id))!.currency, 'CNY');
    expect((await local.getTransactionsByLedger(ledgerId)).single.nativeAmount,
        closeTo(14, .000001));
    expect(await finance.cashAccounts(ledgerId), isEmpty);
    expect((await finance.cashAccounts(ledgerId, currency: 'CNY')).single.id,
        cashId);
    await finance.post(
        id,
        FinancePosting(
            action: FinanceAction.settle,
            happenedAt: DateTime(2020, 1, 2),
            principalCents: 5000,
            cashAccountId: cashId));
    final txs = await local.getTransactionsByLedger(ledgerId);
    expect(txs.firstWhere((tx) => tx.type == 'income').nativeAmount,
        closeTo(7, .000001));
  });

  test(
      'installment purchase counts once while each principal repayment is excluded',
      () async {
    final id = await finance.create(FinanceDraft(
        ledgerId: ledgerId,
        kind: FinanceKind.installment,
        title: 'Computer',
        amountCents: 120000,
        openedAt: DateTime(2020, 1, 1),
        firstDueAt: DateTime(2020, 2, 1),
        annualRateBps: 0,
        periods: 12));
    var totals = await local.totalsInRange(
        ledgerId: ledgerId, start: DateTime(2020), end: DateTime(2021));
    expect(totals, (0.0, 1200.0));
    expect((await local.getNetWorthBreakdown()).netWorth, -200);
    await finance.post(
        id,
        FinancePosting(
            action: FinanceAction.settle,
            happenedAt: DateTime(2020, 2, 1),
            periodNumber: 1,
            principalCents: 10000,
            cashAccountId: cashId));
    totals = await local.totalsInRange(
        ledgerId: ledgerId, start: DateTime(2020), end: DateTime(2021));
    expect(totals, (0.0, 1200.0));
    expect((await local.getNetWorthBreakdown()).netWorth, -200);
    expect((await finance.get(id))!.outstandingCents, 110000);
  });

  test(
      'configuration export excludes managed mirrors that require the full backup',
      () async {
    await finance.create(FinanceDraft(
        ledgerId: ledgerId,
        kind: FinanceKind.borrow,
        title: 'Private loan position',
        amountCents: 10000,
        cashAccountId: cashId,
        openedAt: DateTime(2020, 1, 1)));
    final yaml = await ConfigExportService.exportToYaml(repository: local);
    expect(yaml, contains('Bank'));
    expect(yaml, isNot(contains('Private loan position')));
    expect(yaml, isNot(contains('finance_debt')));
  });

  test(
      'wrong currency and past event date reject before changing finance state',
      () async {
    final id = await finance.create(FinanceDraft(
        ledgerId: ledgerId,
        kind: FinanceKind.borrow,
        title: 'Borrowed',
        amountCents: 10000,
        cashAccountId: cashId,
        openedAt: DateTime(2020, 1, 2)));
    final usd = await local.createAccount(
        ledgerId: ledgerId, name: 'USD', currency: 'USD');
    await expectLater(
        finance.post(
            id,
            FinancePosting(
                action: FinanceAction.settle,
                happenedAt: DateTime(2020, 1, 3),
                cashAccountId: usd,
                principalCents: 1000)),
        throwsA(isA<FinanceFailure>()
            .having((e) => e.code, 'code', 'currency_mismatch')));
    await expectLater(
        finance.post(
            id,
            FinancePosting(
                action: FinanceAction.settle,
                happenedAt: DateTime(2020, 1, 1),
                cashAccountId: cashId,
                principalCents: 1000)),
        throwsA(isA<FinanceFailure>()
            .having((e) => e.code, 'code', 'invalid_date')));
    expect(await finance.events(id), hasLength(1));
    expect(await local.getAccountBalance(cashId), 1100);
  });
}
