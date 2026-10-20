import 'package:drift/native.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/providers/currency_providers.dart';
import 'package:beecount/providers/database_providers.dart';
import 'package:beecount/services/currency/rate_math.dart';

void main() {
  late BeeDatabase database;
  late LocalRepository repository;
  late ProviderContainer container;

  setUp(() {
    database = BeeDatabase.forTesting(NativeDatabase.memory());
    repository = LocalRepository(database);
    container = ProviderContainer(overrides: [
      repositoryProvider.overrideWithValue(repository),
    ]);
  });

  tearDown(() async {
    container.dispose();
    await database.close();
  });

  test('manual rates override local cache and removing override restores cache', () async {
    await repository.upsertAutoRates(base: 'CNY', rateDate: '2026-10-01',
      rates: {'USD': '7.1'}, source: 'existing local cache',
      fetchedAt: DateTime(2026, 10, 1));
    await repository.setOverride(base: 'CNY', quote: 'USD', rate: '7.2');
    final manual = await container.read(effectiveRatesProvider.future);
    expect(manual['USD']!.rate, '7.2');
    expect(manual['USD']!.manual, isTrue);
    expect(await container.read(currencyPickerRatesProvider('CNY').future),
      {'USD': 7.2});

    await repository.removeOverride(base: 'CNY', quote: 'USD');
    container.read(rateRefreshTickProvider.notifier).state++;
    final cached = await container.read(effectiveRatesProvider.future);
    expect(cached['USD']!.rate, '7.1');
    expect(cached['USD']!.manual, isFalse);
    expect(cached['USD']!.rateDate, '2026-10-01');
  });

  test('missing local rates remain missing and never convert at a default rate', () async {
    final rates = await container.read(effectiveRatesProvider.future);
    expect(rates, isEmpty);
    expect(await container.read(currencyPickerRatesProvider('CNY').future), isEmpty);
    expect(computeNativeAmount(amount: 10, accountCurrency: 'USD',
      ledgerBase: 'CNY', rates: rates), isNull);
    expect(computeNativeAmount(amount: 10, accountCurrency: 'CNY',
      ledgerBase: 'CNY', rates: rates), 10);
  });

  test('changing base reads only rates stored for the selected base', () async {
    await repository.setOverride(base: 'CNY', quote: 'USD', rate: '7.2');
    expect((await container.read(effectiveRatesProvider.future))['USD'], isNotNull);
    container.read(baseCurrencyProvider.notifier).state = 'EUR';
    expect(await container.read(effectiveRatesProvider.future), isEmpty);
  });
}
