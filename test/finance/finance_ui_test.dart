import 'dart:async';

import 'package:beecount/data/db.dart' as db;
import 'package:beecount/data/repositories/finance_repository.dart';
import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/models/finance.dart';
import 'package:beecount/pages/finance/finance_detail_page.dart';
import 'package:beecount/pages/finance/finance_page.dart';
import 'package:beecount/pages/finance/finance_posting_page.dart';
import 'package:beecount/pages/finance/finance_text.dart';
import 'package:beecount/providers/finance_providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('decimal entry preserves cents and micro-units without binary rounding',
      () {
    expect(parseFinanceDecimal('0.29', 2), 29);
    expect(parseFinanceDecimal('12.345678', 6), 12345678);
    expect(parseFinanceDecimal('1.001', 2), isNull);
    expect(parseFinanceDecimal('-1', 2), isNull);
    expect(parseFinanceDecimal('NaN', 2), isNull);
    expect(parseFinanceDecimal('1e3', 2), isNull);
    expect(financeDecimal(12345678, 6), '12.345678');
  });

  testWidgets(
      'finance filters remain scrollable at narrow width and double text scale',
      (tester) async {
    tester.view.physicalSize = const Size(320, 640);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(_app(
      home: const FinancePage(ledgerId: 1),
      repository: _RecordingRepository(),
      plans: [_plan(kind: FinanceKind.reimbursement)],
      textScale: 2,
    ));
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
    await tester.scrollUntilVisible(find.text('Include closed records'), 250);
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
    expect(find.byType(FloatingActionButton), findsOneWidget);
    await tester.scrollUntilVisible(find.text('Trip advance'), 250);
    expect(find.text('Trip advance'), findsOneWidget);
  });

  testWidgets(
      'partial reimbursement sends exact principal and prevents double submission',
      (tester) async {
    final repository = _RecordingRepository();
    final plan = _plan(kind: FinanceKind.reimbursement);
    await tester.pumpWidget(_app(
      home: FinancePostingPage(plan: plan, action: FinanceAction.settle),
      repository: repository,
    ));
    await tester.pumpAndSettle();
    await tester.enterText(
        find.byKey(const ValueKey('finance-principal')), '0');
    await tester.ensureVisible(find.text('Record'));
    await tester.tap(find.text('Record'));
    await tester.pump();
    expect(repository.postings, isEmpty);

    await tester.scrollUntilVisible(
        find.byKey(const ValueKey('finance-principal')), -200,
        scrollable: find.descendant(of: find.byType(ListView),
          matching: find.byType(Scrollable)).first);
    await tester.enterText(
        find.byKey(const ValueKey('finance-principal')), '25.50');
    await tester.ensureVisible(find.text('Record'));
    await tester.tap(find.text('Record'));
    await tester.pump();
    expect(repository.postings, hasLength(1));
    expect(repository.postings.single.principalCents, 2550);
    expect(repository.postings.single.interestCents, 0);
    expect(repository.postings.single.cashAccountId, 10);
    expect(tester.widget<FilledButton>(find.byType(FilledButton)).onPressed,
        isNull);
    expect(tester.takeException(), isNull);

    // Leave while the fake I/O is pending, then finish it: mounted guards must
    // keep the completed future from touching the disposed form.
    await tester.pumpWidget(const SizedBox());
    repository.completion.complete();
    await tester.pump();
    expect(tester.takeException(), isNull);
  });

  testWidgets('a fully sold investment can be bought again', (tester) async {
    final plan = _plan(kind: FinanceKind.investment, closed: true);
    await tester.pumpWidget(_app(
      home: FinanceDetailPage(planId: plan.id),
      repository: _RecordingRepository(),
      plan: plan,
    ));
    await tester.pumpAndSettle();
    await tester.scrollUntilVisible(find.text('Buy'), 200);
    expect(find.text('Buy'), findsOneWidget);
    expect(find.text('Sell'), findsNothing);
    await tester.tap(find.text('Buy'));
    await tester.pumpAndSettle();
    expect(find.byType(FinancePostingPage), findsOneWidget);
    expect(find.text('Total cost including fees (CNY)'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}

Widget _app(
        {required Widget home,
        required _RecordingRepository repository,
        List<FinancePlan> plans = const [],
        FinancePlan? plan,
        double textScale = 1}) =>
    ProviderScope(
      overrides: [
        financeRepositoryProvider.overrideWithValue(repository),
        financePlansProvider(1).overrideWith((ref) async => plans),
        financePlanProvider(1).overrideWith((ref) async => plan),
        financeEventsProvider(1).overrideWith((ref) async => <FinanceEvent>[]),
      ],
      child: MaterialApp(
        locale: const Locale('en'),
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        supportedLocales: AppLocalizations.supportedLocales,
        builder: (context, child) => MediaQuery(
          data: MediaQuery.of(context)
              .copyWith(textScaler: TextScaler.linear(textScale)),
          child: child!,
        ),
        home: home,
      ),
    );

FinancePlan _plan({required FinanceKind kind, bool closed = false}) =>
    FinancePlan(
      id: 1,
      ledgerId: 1,
      accountId: 20,
      kind: kind,
      title: 'Trip advance',
      party: '',
      currency: 'CNY',
      note: '',
      principalCents: 10000,
      annualRateBps: 0,
      periods: 12,
      method: RepaymentMethod.annuity,
      openedAt: DateTime(2020, 1, 1),
      firstDueAt: DateTime(2020, 2, 1),
      outstandingCents: closed ? 0 : 10000,
      unitsMicros: closed ? 0 : 1000000,
      valueCents: closed ? 0 : 10000,
      realizedProfitCents: 0,
      paidInterestCents: 0,
    );

class _RecordingRepository implements FinanceRepository {
  final postings = <FinancePosting>[];
  final completion = Completer<void>();
  @override
  Future<List<db.Account>> cashAccounts(int ledgerId,
          {String? currency}) async =>
      [
        const db.Account(
            id: 10,
            ledgerId: 1,
            name: 'Cash',
            type: 'cash',
            currency: 'CNY',
            initialBalance: 1000,
            sortOrder: 0,
            hidden: false),
      ];
  @override
  Future<void> post(int id, FinancePosting posting) {
    postings.add(posting);
    return completion.future;
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
