import 'dart:async';

import 'package:beecount/data/db.dart' as db;
import 'package:beecount/data/repositories/base_repository.dart';
import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/pages/auth/welcome_page.dart';
import 'package:beecount/providers/database_providers.dart';
import 'package:beecount/providers/ui_state_providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

const _next = ValueKey('welcome-next');
const _previous = ValueKey('welcome-previous');
const _finish = ValueKey('welcome-finish');

void main() {
  setUp(() => SharedPreferences.setMockInitialValues({}));

  testWidgets(
      'four pages finish, previous works and repeated navigation is ignored',
      (tester) async {
    final database = _SeedDatabase();
    await tester.pumpWidget(_app(database));
    await tester.pumpAndSettle();
    final container =
        ProviderScope.containerOf(tester.element(find.byType(WelcomePage)));
    container.read(cachedTransactionsProvider.notifier).state = [];
    container.read(cachedTransactionsWithCategoryProvider.notifier).state = [];

    await tester.tap(find.byKey(_next));
    await tester.tap(find.byKey(_next));
    await tester.pump();
    expect(tester.widget<FilledButton>(find.byKey(_next)).onPressed, isNull);
    await tester.pumpAndSettle();
    expect(find.byType(CheckboxListTile),
        findsOneWidget); // currency, not category
    await _tap(tester, _previous);
    expect(find.byKey(_previous), findsNothing);
    await _tap(tester, _next);
    await _tap(tester, _next);
    await _tap(tester, _next);
    expect(find.byKey(_next), findsNothing);
    expect(find.text('Finish'), findsOneWidget);
    await _tap(tester, _previous);
    expect(find.byKey(_finish), findsNothing);
    await _tap(tester, _next);
    await _tap(tester, _finish);

    expect(find.text('setup-complete'), findsOneWidget);
    expect(database.seedCalls, 1);
    expect(database.transactionCalls, 1);
    expect(database.seededLedgers, hasLength(1));
    expect((await SharedPreferences.getInstance()).getBool('welcome_shown'),
        isTrue);
    expect(container.read(currentLedgerIdProvider), 42);
    expect(container.read(cachedTransactionsProvider), isNull);
    expect(container.read(cachedTransactionsWithCategoryProvider), isNull);
    expect(tester.takeException(), isNull);
  });

  testWidgets(
      'failed seed rolls back and retries without marking setup complete',
      (tester) async {
    final database = _SeedDatabase(failOnce: true);
    await tester.pumpWidget(_app(database));
    await tester.pumpAndSettle();
    for (var i = 0; i < 3; i++) {
      await _tap(tester, _next);
    }
    await _tap(tester, _finish);
    expect(find.textContaining('seed-failed-once'), findsOneWidget);
    expect(database.seededLedgers, isEmpty);
    expect((await SharedPreferences.getInstance()).getBool('welcome_shown'),
        isNot(true));
    expect(find.text('setup-complete'), findsNothing);

    await tester.tap(find.text('Close'));
    await tester.pumpAndSettle();
    await _tap(tester, _finish);
    expect(database.seedCalls, 2);
    expect(database.transactionCalls, 2);
    expect(database.seededLedgers, hasLength(1));
    expect(find.text('setup-complete'), findsOneWidget);
    expect((await SharedPreferences.getInstance()).getBool('welcome_shown'),
        isTrue);
    // Error logging is throttled for two seconds; advance the test clock so
    // its real persistence timer finishes before this widget test is disposed.
    await tester.pump(const Duration(seconds: 3));
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
  });

  testWidgets(
      'seed in progress disables previous and finish and cannot duplicate',
      (tester) async {
    final database = _SeedDatabase(gate: Completer<void>());
    await tester.pumpWidget(_app(database));
    await tester.pumpAndSettle();
    for (var i = 0; i < 3; i++) {
      await _tap(tester, _next);
    }
    await tester.tap(find.byKey(_finish));
    await tester.tap(find.byKey(_finish));
    await tester.pump();
    expect(database.seedCalls, 1);
    expect(tester.widget<FilledButton>(find.byKey(_finish)).onPressed, isNull);
    expect(tester.widget<TextButton>(find.byKey(_previous)).onPressed, isNull);
    expect((await SharedPreferences.getInstance()).getBool('welcome_shown'),
        isNot(true));
    database.gate!.complete();
    await tester.pumpAndSettle();
    expect(find.text('setup-complete'), findsOneWidget);
  });

  testWidgets(
      'small screens and double text keep every page scrollable and footer tappable',
      (tester) async {
    tester.view.physicalSize = const Size(320, 640);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    final database = _SeedDatabase();
    await tester.pumpWidget(_app(database, textScale: 2));
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
    await _tap(tester, _next);
    expect(tester.takeException(), isNull);
    // Honour the opt-out; finishing may leave the database without any ledger.
    await tester.scrollUntilVisible(find.byType(CheckboxListTile), 160,
        scrollable: find
            .descendant(
                of: find.byType(ListView).hitTestable(),
                matching: find.byType(Scrollable))
            .first);
    await tester.tap(find.byType(CheckboxListTile));
    await tester.pump();
    await _tap(tester, _next);
    expect(tester.takeException(), isNull);
    await _tap(tester, _next);
    expect(tester.takeException(), isNull);
    await _tap(tester, _finish);
    expect(find.text('setup-complete'), findsOneWidget);
    expect(database.seededLedgers, isEmpty);
    expect(tester.takeException(), isNull);
  });
}

Future<void> _tap(WidgetTester tester, Key key) async {
  await tester.tap(find.byKey(key));
  await tester.pumpAndSettle();
}

Widget _app(_SeedDatabase database, {double textScale = 1}) => ProviderScope(
      overrides: [
        databaseProvider.overrideWithValue(database),
        repositoryProvider.overrideWithValue(_SeedRepository(database)),
        shouldShowWelcomeProvider.overrideWith((ref) => true),
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
        home: Consumer(
            builder: (context, ref, _) => ref.watch(shouldShowWelcomeProvider)
                ? const WelcomePage()
                : const Scaffold(body: Text('setup-complete'))),
      ),
    );

/// Deterministic seed/transaction fake: exercises UI ordering and rollback,
/// without invoking real disk I/O or the app's splash initialization.
class _SeedDatabase implements db.BeeDatabase {
  _SeedDatabase({this.failOnce = false, this.gate});
  bool failOnce;
  final Completer<void>? gate;
  final seededLedgers = <db.Ledger>[];
  int seedCalls = 0;
  int transactionCalls = 0;
  bool _insideTransaction = false;

  @override
  Future<T> transaction<T>(Future<T> Function() action,
      {bool requireNew = false}) async {
    transactionCalls++;
    final before = List<db.Ledger>.of(seededLedgers);
    _insideTransaction = true;
    try {
      return await action();
    } catch (_) {
      seededLedgers
        ..clear()
        ..addAll(before);
      rethrow;
    } finally {
      _insideTransaction = false;
    }
  }

  @override
  Future<void> ensureSeed(
      {AppLocalizations? l10n,
      String currency = 'CNY',
      bool useHierarchicalCategories = false,
      bool skipCategories = false,
      bool createDefaultLedger = true}) async {
    expect(_insideTransaction, isTrue);
    seedCalls++;
    if (createDefaultLedger) {
      seededLedgers.add(db.Ledger(
          id: 42,
          name: 'Local',
          currency: currency,
          type: 'local',
          createdAt: DateTime(2020),
          myRole: 'owner',
          memberCount: 1,
          isShared: false,
          monthStartDay: 1));
    }
    if (gate != null) await gate!.future;
    if (failOnce) {
      failOnce = false;
      throw StateError('seed-failed-once');
    }
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _SeedRepository implements BaseRepository {
  _SeedRepository(this.database);
  final _SeedDatabase database;
  @override
  Future<List<db.Ledger>> getAllLedgers() async =>
      List.of(database.seededLedgers);
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
