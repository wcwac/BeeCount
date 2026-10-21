import 'dart:async';

import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/models/capture_candidate.dart';
import 'package:beecount/pages/transaction/capture_inbox_page.dart';
import 'package:beecount/pages/tag/widgets/tag_selector.dart';
import 'package:beecount/providers.dart';
import 'package:beecount/services/capture/capture_commit_service.dart';
import 'package:beecount/services/capture/capture_service.dart';
import 'package:beecount/widgets/biz/amount_editor_sheet.dart';
import 'package:drift/native.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late BeeDatabase db;
  late LocalRepository repo;
  late Ledger ledger;
  late ProviderContainer container;
  final time = DateTime(2026, 10, 21, 10);

  Map<String, Object?> payload(String id, String merchant) => {
    'id': id, 'sourcePackage': 'com.tencent.mm', 'appName': 'Fixture payment',
    'ledgerId': ledger.id, 'ledgerKey': CaptureCommitService.ledgerKey(ledger),
    'amountMinor': 1250, 'direction': 'expense', 'currency': 'CNY',
    'observedAt': time.millisecondsSinceEpoch, 'merchant': merchant,
    'issues': <String>[],
  };

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    db = BeeDatabase.forTesting(NativeDatabase.memory());
    repo = LocalRepository(db);
    final id = await repo.createLedger(name: 'Original ledger');
    ledger = (await repo.getLedgerById(id))!;
    container = ProviderContainer(overrides: [
      databaseProvider.overrideWithValue(db),
      repositoryProvider.overrideWithValue(repo),
      ledgersStreamProvider.overrideWith((ref) => Stream.value([ledger])),
    ]);
  });
  tearDown(() async {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(CaptureService.channel, null);
    container.dispose();
    await db.close();
  });

  Widget app() => UncontrolledProviderScope(container: container, child: MaterialApp(
    locale: const Locale('en'),
    localizationsDelegates: AppLocalizations.localizationsDelegates,
    supportedLocales: AppLocalizations.supportedLocales,
    home: const CaptureInboxPage(),
  ));

  testWidgets('an unhandled native snapshot is displayed without mutating its fixed list', (tester) async {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'listCandidates') return [payload('unhandled', 'Fresh draft')];
        return null;
      },
    );
    await tester.pumpWidget(app());
    await tester.pumpAndSettle();
    expect(find.text('Fresh draft'), findsOneWidget);
    expect(find.text('Review and record'), findsOneWidget);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('a failed acknowledgement does not hide unrelated unhandled drafts', (tester) async {
    final handled = payload('handled', 'Already recorded');
    await CaptureCommitService(repo).commit(candidate: CaptureCandidate.fromMap(handled),
      type: 'expense', amount: 12.5, happenedAt: time);
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'listCandidates') return [handled, payload('unhandled', 'Still needs review')];
        if (call.method == 'ackCommitted') throw PlatformException(code: 'TEMPORARY_UNAVAILABLE');
        return null;
      },
    );
    await tester.pumpWidget(app());
    await tester.pumpAndSettle();
    expect(find.text('Still needs review'), findsOneWidget);
    expect(find.text('Already recorded'), findsNothing);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('review saves to its frozen ledger and carries that scope through editor sheets', (tester) async {
    final targetId = await repo.createLedger(name: 'USD capture ledger', currency: 'USD');
    final target = (await repo.getLedgerById(targetId))!;
    await repo.createCategory(name: 'Scope food', kind: 'expense');
    // Warm the homepage providers first: a child review must not inherit their
    // cached CNY value or switch the homepage's current ledger to the target.
    container.read(currentLedgerIdProvider.notifier).state = ledger.id;
    // Drift's first watched value can need an event-loop turn. Waiting for it
    // directly in testWidgets' fake clock can stall before the first pump.
    await tester.runAsync(() async {
      await container.read(currentLedgerProvider.future);
    });
    expect(container.read(currentLedgerCurrencyProvider), 'CNY');
    final draft = {...payload('scoped-draft', 'USD payment'),
      'ledgerId': targetId, 'ledgerKey': CaptureCommitService.ledgerKey(target), 'currency': 'USD'};
    var acknowledged = false;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'listCandidates') return acknowledged ? [] : [draft];
        if (call.method == 'ackCommitted') acknowledged = true;
        return null;
      },
    );
    await tester.pumpWidget(app());
    await tester.pumpAndSettle();
    await tester.tap(find.text('Review and record'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Scope food'));
    await tester.pumpAndSettle();
    // Regression: review must use quickAdd; otherwise choosing a category just
    // closes the page without ever presenting an amount or saving a transaction.
    expect(find.byType(AmountEditorSheet), findsOneWidget);
    final amountContext = tester.element(find.byType(AmountEditorSheet));
    final scoped = ProviderScope.containerOf(amountContext, listen: false);
    expect(scoped.read(currentLedgerIdProvider), targetId);
    expect((await scoped.read(currentLedgerProvider.future))!.id, targetId);
    expect(scoped.read(currentLedgerCurrencyProvider), 'USD');
    unawaited(TagSelector.show(amountContext));
    await tester.pumpAndSettle();
    final tagsContext = tester.element(find.byType(TagSelector));
    expect(ProviderScope.containerOf(tagsContext, listen: false).read(currentLedgerIdProvider), targetId);
    Navigator.of(tagsContext).pop();
    await tester.pumpAndSettle();
    await tester.tap(find.text('Finish'));
    await tester.pumpAndSettle();
    final rows = await db.select(db.transactions).get();
    expect(rows, hasLength(1));
    expect(rows.single.ledgerId, targetId);
    expect(rows.single.amount, 12.5);
    expect(rows.single.currencyCode, 'USD');
    expect(rows.single.nativeAmount, 12.5);
    expect(acknowledged, isTrue);
    expect(container.read(currentLedgerIdProvider), ledger.id);
    expect(container.read(currentLedgerCurrencyProvider), 'CNY');
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android), timeout: const Timeout(Duration(minutes: 1)));
}
