/// 交易级多币种 provider 层:
///   ① currentLedgerCurrencyProvider:账本本位币别名(大写/兜底 CNY)
///   ② effectiveRatesForLedgerProvider:以账本本位币为 base 合成有效汇率
///      (与 effectiveRatesProvider 的差异仅在 base 来源)
import 'package:drift/native.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/providers/currency_providers.dart';
import 'package:beecount/providers/database_providers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late BeeDatabase db;
  late LocalRepository repo;

  setUp(() {
    db = BeeDatabase.forTesting(NativeDatabase.memory());
    repo = LocalRepository(db);
  });

  tearDown(() async => db.close());

  Ledger ledgerWith(String currency) => Ledger(
        id: 1,
        name: 'L',
        currency: currency,
        type: 'personal',
        createdAt: DateTime(2026, 1, 1),
        myRole: 'owner',
        memberCount: 1,
        isShared: false,
        monthStartDay: 1,
      );

  test('currentLedgerCurrencyProvider:账本币种大写;无账本兜底 CNY', () async {
    final container = ProviderContainer(overrides: [
      currentLedgerProvider
          .overrideWith((ref) => Stream<Ledger?>.value(ledgerWith('usd'))),
    ]);
    addTearDown(container.dispose);
    final ledgerSubscription = container.listen(currentLedgerProvider, (_, __) {});
    addTearDown(ledgerSubscription.close);
    await container.read(currentLedgerProvider.future).timeout(const Duration(seconds: 5));
    expect(container.read(currentLedgerCurrencyProvider), 'USD');

    final empty = ProviderContainer(overrides: [
      currentLedgerProvider.overrideWith((ref) => Stream<Ledger?>.value(null)),
    ]);
    addTearDown(empty.dispose);
    final emptySubscription = empty.listen(currentLedgerProvider, (_, __) {});
    addTearDown(emptySubscription.close);
    await empty.read(currentLedgerProvider.future).timeout(const Duration(seconds: 5));
    expect(empty.read(currentLedgerCurrencyProvider), 'CNY');
  });

  test('effectiveRatesForLedger 以账本本位币为 base(≠ 用户主币种)', () async {
    // 种两组汇率:base=CNY(主币种)与 base=USD(账本本位币)
    await repo.upsertAutoRates(
      base: 'CNY',
      rateDate: '2026-07-10',
      rates: {'USD': '7.2'},
      source: 'test',
      fetchedAt: DateTime.utc(2026, 7, 10),
    );
    await repo.upsertAutoRates(
      base: 'USD',
      rateDate: '2026-07-10',
      rates: {'CNY': '0.14', 'JPY': '0.0068'},
      source: 'test',
      fetchedAt: DateTime.utc(2026, 7, 10),
    );

    final container = ProviderContainer(overrides: [
      repositoryProvider.overrideWithValue(repo),
      currentLedgerProvider
          .overrideWith((ref) => Stream<Ledger?>.value(ledgerWith('USD'))),
    ]);
    addTearDown(container.dispose);
    final ledgerSubscription = container.listen(currentLedgerProvider, (_, __) {});
    addTearDown(ledgerSubscription.close);
    await container.read(currentLedgerProvider.future).timeout(const Duration(seconds: 5));
    // 主币种是 CNY,但账本本位币 USD → 应取 base=USD 的组
    container.read(baseCurrencyProvider.notifier).state = 'CNY';

    final rates =
        await container.read(effectiveRatesForLedgerProvider.future);
    expect(rates.keys, containsAll(['CNY', 'JPY']));
    expect(rates['CNY']!.rate, '0.14');
    expect(rates.containsKey('USD'), isFalse); // base 自身不在 quote 里
  });

  test('a review scope keeps its ledger currency and month boundary independent', () async {
    final homeLedger = await repo.createLedger(name: 'Home', currency: 'USD');
    final reviewLedger = await repo.createLedger(name: 'Review', currency: 'JPY');
    await repo.updateLedger(id: homeLedger, monthStartDay: 5);
    await repo.updateLedger(id: reviewLedger, monthStartDay: 20);
    final home = ProviderContainer(overrides: [
      repositoryProvider.overrideWithValue(repo),
      currentLedgerIdProvider.overrideWith((ref) => homeLedger),
    ]);
    addTearDown(home.dispose);
    final review = ProviderContainer(parent: home, overrides: [
      currentLedgerIdProvider.overrideWith((ref) => reviewLedger),
    ]);
    addTearDown(review.dispose);
    // Riverpod 3 pauses unobserved streams. Keep the same active watchers that
    // the homepage and a review editor have while their database values load.
    final homeSubscription = home.listen(currentLedgerProvider, (_, __) {});
    final reviewSubscription = review.listen(currentLedgerProvider, (_, __) {});
    addTearDown(homeSubscription.close);
    addTearDown(reviewSubscription.close);
    await home.read(currentLedgerProvider.future).timeout(const Duration(seconds: 5));
    await review.read(currentLedgerProvider.future).timeout(const Duration(seconds: 5));

    expect(home.read(currentLedgerCurrencyProvider), 'USD');
    expect(home.read(currentMonthStartDayProvider), 5);
    expect(review.read(currentLedgerCurrencyProvider), 'JPY');
    expect(review.read(currentMonthStartDayProvider), 20);
    expect(home.read(currentLedgerIdProvider), homeLedger);
  });
}
