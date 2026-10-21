import 'dart:async';

import 'package:beecount/data/repositories/base_repository.dart';
import 'package:beecount/providers/attachment_settings_providers.dart';
import 'package:beecount/providers/currency_providers.dart';
import 'package:beecount/providers/database_providers.dart';
import 'package:beecount/providers/statistics_providers.dart';
import 'package:beecount/providers/ui_state_providers.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  final binding = TestWidgetsFlutterBinding.ensureInitialized();
  const preferencesChannel = MethodChannel('plugins.flutter.io/shared_preferences');

  void installPreferencesBackend(Future<Object?> Function(MethodCall) handler) {
    SharedPreferences.resetStatic();
    binding.defaultBinaryMessenger.setMockMethodCallHandler(preferencesChannel, handler);
    addTearDown(() {
      binding.defaultBinaryMessenger.setMockMethodCallHandler(preferencesChannel, null);
      SharedPreferences.resetStatic();
    });
  }

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

  test('leaving analytics during preference loading retains the cached result', () async {
    final started = Completer<void>();
    final readResult = Completer<Map<String, Object>>();
    installPreferencesBackend((call) async {
      if (call.method == 'getAll') {
        started.complete();
        return readResult.future;
      }
      throw StateError('Unexpected preferences call: ${call.method}');
    });
    addTearDown(() async {
      if (!readResult.isCompleted) readResult.complete({});
      await SharedPreferences.getInstance();
    });
    final container = ProviderContainer(retry: (retryCount, error) => null);
    addTearDown(container.dispose);
    final subscription = container.listen(
      analyticsHeaderHintDismissedProvider,
      (previous, next) {},
    );
    await started.future.timeout(const Duration(seconds: 5));
    subscription.close();
    await container.pump();
    expect(container.exists(analyticsHeaderHintDismissedProvider), isTrue);

    readResult.complete({'flutter.analytics_header_hint_dismissed': true});
    expect(await container.read(analyticsHeaderHintDismissedProvider.future), isTrue);
  });

  test('a preference write can finish after its notifier container is disposed', () async {
    final started = Completer<void>();
    final writeResult = Completer<bool>();
    installPreferencesBackend((call) async {
      if (call.method == 'getAll') return <String, Object>{};
      if (call.method == 'setBool') {
        started.complete();
        return writeResult.future;
      }
      throw StateError('Unexpected preferences call: ${call.method}');
    });
    addTearDown(() {
      if (!writeResult.isCompleted) writeResult.complete(true);
    });
    final container = ProviderContainer(retry: (retryCount, error) => null);
    addTearDown(container.dispose);
    await container.read(attachmentKeepOriginalProvider.future);
    final write = container.read(attachmentKeepOriginalProvider.notifier).setEnabled(true);
    await started.future.timeout(const Duration(seconds: 5));
    container.dispose();
    writeResult.complete(true);

    await expectLater(write, completes);
    expect(
      (await SharedPreferences.getInstance()).getBool(
        AttachmentKeepOriginalNotifier.preferenceKey,
      ),
      isTrue,
    );
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
