import 'dart:async';

import 'package:beecount/data/repositories/base_repository.dart';
import 'package:beecount/providers/currency_providers.dart';
import 'package:beecount/providers/database_providers.dart';
import 'package:beecount/providers/statistics_providers.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('invalidated statistics cannot overwrite the last successful cache', () async {
    final repo = _DeferredRepository();
    final container = ProviderContainer(
      retry: (retryCount, error) => null,
      overrides: [repositoryProvider.overrideWithValue(repo)],
    );
    addTearDown(container.dispose);
    container.listen(countsAllProvider, (previous, next) {});
    expect(repo.counts, hasLength(1));

    container.invalidate(countsAllProvider);
    final latest = container.read(countsAllProvider.future);
    expect(repo.counts, hasLength(2));

    repo.counts.first.complete((dayCount: 1, txCount: 1));
    await Future<void>.delayed(Duration.zero);
    expect(container.read(lastCountsAllProvider), isNull);

    repo.counts.last.complete((dayCount: 2, txCount: 3));
    expect(await latest, (dayCount: 2, txCount: 3));
    expect(container.read(lastCountsAllProvider), (dayCount: 2, txCount: 3));
  });

  test('base currency changes invalidate an in-flight currency lookup', () async {
    final repo = _DeferredRepository();
    final container = ProviderContainer(
      retry: (retryCount, error) => null,
      overrides: [repositoryProvider.overrideWithValue(repo)],
    );
    addTearDown(container.dispose);
    container.listen(usedCurrenciesProvider, (previous, next) {});
    expect(repo.currencies, hasLength(1));

    container.read(baseCurrencyProvider.notifier).state = 'USD';
    final latest = container.read(usedCurrenciesProvider.future);
    expect(repo.currencies, hasLength(2));

    repo.currencies.first.complete({'CNY'});
    repo.currencies.last.complete({'CNY'});
    expect(await latest, {'CNY', 'USD'});
  });
}

class _DeferredRepository implements BaseRepository {
  final counts = <Completer<({int dayCount, int txCount})>>[];
  final currencies = <Completer<Set<String>>>[];

  @override
  Future<({int dayCount, int txCount})> getCountsAll() {
    final result = Completer<({int dayCount, int txCount})>();
    counts.add(result);
    return result.future;
  }

  @override
  Future<Set<String>> getUsedCurrencies() {
    final result = Completer<Set<String>>();
    currencies.add(result);
    return result.future;
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
