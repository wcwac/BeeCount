import 'dart:async';

import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/models/capture_candidate.dart';
import 'package:beecount/pages/transaction/capture_inbox_page.dart';
import 'package:beecount/pages/transaction/transaction_editor_page.dart';
import 'package:beecount/pages/tag/widgets/tag_selector.dart';
import 'package:beecount/providers.dart';
import 'package:beecount/services/capture/capture_commit_service.dart';
import 'package:beecount/services/capture/capture_service.dart';
import 'package:beecount/widget/widget_manager.dart';
import 'package:beecount/widgets/biz/amount_editor_sheet.dart';
import 'package:drift/native.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

class _RecordingWidgetManager implements WidgetManager {
  final refreshedLedgerIds = <int>[];

  @override
  dynamic noSuchMethod(Invocation invocation) {
    if (invocation.isMethod && invocation.memberName == #updateAllWidgets) {
      refreshedLedgerIds.add(invocation.positionalArguments[1] as int);
      return Future<void>.value();
    }
    return super.noSuchMethod(invocation);
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late BeeDatabase db;
  late LocalRepository repo;
  late Ledger ledger;
  late ProviderContainer container;
  late _RecordingWidgetManager widgetManager;
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
    widgetManager = _RecordingWidgetManager();
    container = ProviderContainer(overrides: [
      databaseProvider.overrideWithValue(db),
      repositoryProvider.overrideWithValue(repo),
      ledgersStreamProvider.overrideWith((ref) => Stream.value([ledger])),
      widgetManagerProvider.overrideWithValue(widgetManager),
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

  testWidgets('a pending dismissal locks its draft while another draft remains reviewable', (tester) async {
    final dismissal = Completer<void>();
    addTearDown(() { if (!dismissal.isCompleted) dismissal.complete(); });
    var dismissed = false;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'listCandidates') {
          return [if (!dismissed) payload('dismiss-pending', 'Pending dismissal'),
            {...payload('other-draft', 'Another actionable draft'), 'amountMinor': 2500}];
        }
        if (call.method == 'dismissCandidate') {
          await dismissal.future;
          dismissed = true;
        }
        return null;
      },
    );
    await tester.pumpWidget(app());
    await tester.pumpAndSettle();
    await tester.tap(find.descendant(of: find.byKey(const ValueKey('dismiss-pending')),
      matching: find.text('Dismiss')));
    await tester.pumpAndSettle();
    await tester.tap(find.descendant(of: find.byType(AlertDialog), matching: find.text('Dismiss')));
    await tester.pumpAndSettle();

    final review = find.descendant(of: find.byKey(const ValueKey('dismiss-pending')),
      matching: find.widgetWithText(FilledButton, 'Review and record'));
    expect(tester.widget<FilledButton>(review).onPressed, isNull);
    expect(find.byType(TransactionEditorPage), findsNothing);
    final otherReview = find.descendant(of: find.byKey(const ValueKey('other-draft')),
      matching: find.widgetWithText(FilledButton, 'Review and record'));
    await tester.ensureVisible(otherReview);
    expect(tester.widget<FilledButton>(otherReview).onPressed, isNotNull);
    await tester.tap(otherReview);
    await tester.pumpAndSettle();
    expect(tester.widget<TransactionEditorPage>(find.byType(TransactionEditorPage))
      .captureCandidate!.id, 'other-draft');
    Navigator.of(tester.element(find.byType(TransactionEditorPage))).pop();
    await tester.pumpAndSettle();

    dismissal.complete();
    await tester.pumpAndSettle();
    expect(find.text('Pending dismissal'), findsNothing);
    expect(await db.select(db.transactions).get(), isEmpty);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('one late acknowledgement does not starve other handled drafts', (tester) async {
    final first = payload('late-ack', 'Already saved one');
    final second = payload('ready-ack', 'Already saved two');
    await CaptureCommitService(repo).commit(candidate: CaptureCandidate.fromMap(first),
      type: 'expense', amount: 12.5, happenedAt: time);
    await CaptureCommitService(repo).commit(candidate: CaptureCandidate.fromMap(second),
      type: 'expense', amount: 12.5, happenedAt: time);
    final reply = Completer<void>();
    addTearDown(() { if (!reply.isCompleted) reply.complete(); });
    final acknowledged = <String>{};
    final calls = <String>[];
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'listCandidates') {
          return [first, second, payload('fresh', 'Needs a decision')]
              .where((draft) => !acknowledged.contains(draft['id'])).toList();
        }
        if (call.method == 'ackCommitted') {
          final id = (call.arguments as Map)['id'] as String;
          calls.add(id);
          if (id == 'late-ack') await reply.future;
          acknowledged.add(id);
        }
        return null;
      },
    );
    await tester.pumpWidget(app());
    await tester.pumpAndSettle();
    expect(find.text('Needs a decision'), findsOneWidget);
    await tester.pump(const Duration(seconds: 3));
    await tester.pumpAndSettle();
    expect(acknowledged, contains('ready-ack'));
    expect(acknowledged, isNot(contains('late-ack')));
    await tester.tap(find.byIcon(Icons.refresh));
    await tester.pumpAndSettle();
    expect(calls.where((id) => id == 'late-ack'), hasLength(1));
    expect(tester.widget<FilledButton>(find.widgetWithText(FilledButton, 'Review and record'))
      .onPressed, isNotNull);
    reply.complete();
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('a large inbox builds rows on demand and reviews a scrolled draft', (tester) async {
    final drafts = List.generate(1000, (index) => payload('candidate-$index', 'Draft $index'));
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'listCandidates') return drafts;
        if (call.method == 'dismissCandidate') {
          final id = (call.arguments as Map)['id'];
          drafts.removeWhere((draft) => draft['id'] == id);
        }
        return null;
      },
    );
    await tester.pumpWidget(app());
    await tester.pumpAndSettle();

    // Both ListView constructors mount visible rows lazily. Requiring a builder
    // delegate also guards against allocating every off-screen Card upfront.
    final list = tester.widget<ListView>(find.byType(ListView));
    expect(list.childrenDelegate, isA<SliverChildBuilderDelegate>());
    expect(find.byType(Card).evaluate().length, lessThan(20));
    expect(find.text('Draft 0'), findsOneWidget);
    expect(find.text('Draft 40'), findsNothing);
    expect(find.text('Draft 999'), findsNothing);

    final target = find.byKey(const ValueKey('candidate-40'));
    await tester.scrollUntilVisible(target, 600, maxScrolls: 60);
    await tester.pumpAndSettle();
    expect(find.text('Draft 40'), findsOneWidget);
    await tester.tap(find.descendant(of: target, matching: find.text('Review and record')));
    await tester.pumpAndSettle();
    final editor = tester.widget<TransactionEditorPage>(find.byType(TransactionEditorPage));
    expect(editor.captureCandidate?.id, 'candidate-40');
    expect(editor.initialNote, 'Draft 40');
    Navigator.of(tester.element(find.byType(TransactionEditorPage))).pop();
    await tester.pumpAndSettle();

    // The page owns the review lock. Returning must restore the recycled row's
    // actions, and removing that row must not associate its key with a neighbor.
    await tester.ensureVisible(target);
    await tester.tap(find.descendant(of: target, matching: find.text('Dismiss')));
    await tester.pumpAndSettle();
    await tester.tap(find.descendant(of: find.byType(AlertDialog), matching: find.text('Dismiss')));
    await tester.pumpAndSettle();
    expect(find.byKey(const ValueKey('candidate-40')), findsNothing);
    expect(find.text('Draft 41'), findsOneWidget);
    expect(drafts, hasLength(999));
    expect(await db.select(db.transactions).get(), isEmpty);
    expect(tester.takeException(), isNull);

    await tester.pumpWidget(const SizedBox());
    await tester.pump(const Duration(seconds: 3));
    await tester.pumpAndSettle();
  }, variant: TargetPlatformVariant.only(TargetPlatform.android), timeout: const Timeout(Duration(minutes: 1)));

  testWidgets('review keeps its frozen ledger and closes before a delayed acknowledgement', (tester) async {
    final targetId = await repo.createLedger(name: 'USD capture ledger', currency: 'USD');
    final target = (await repo.getLedgerById(targetId))!;
    await repo.createCategory(name: 'Scope food', kind: 'expense');
    // Warm the homepage providers first: a child review must not inherit their
    // cached CNY value or switch the homepage's current ledger to the target.
    container.read(currentLedgerIdProvider.notifier).state = ledger.id;
    // Riverpod 3 pauses stream subscriptions without listeners. Keep the
    // homepage watcher alive while warming its cache before mounting the app.
    // Drift's first watched value can need an event-loop turn. Waiting for it
    // directly in testWidgets' fake clock can stall before the first pump.
    late ProviderSubscription<AsyncValue<Ledger?>> homepageLedger;
    await tester.runAsync(() async {
      homepageLedger = container.listen(currentLedgerProvider, (_, __) {});
      await container.read(currentLedgerProvider.future)
          .timeout(const Duration(seconds: 5));
    });
    addTearDown(homepageLedger.close);
    expect(container.read(currentLedgerCurrencyProvider), 'CNY');
    final draft = {...payload('scoped-draft', 'USD payment'),
      'ledgerId': targetId, 'ledgerKey': CaptureCommitService.ledgerKey(target), 'currency': 'USD'};
    final remaining = {...payload('remaining-draft', 'Another pending payment'), 'amountMinor': 2500};
    final acknowledgement = Completer<void>();
    addTearDown(() { if (!acknowledgement.isCompleted) acknowledgement.complete(); });
    var acknowledged = false;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'listCandidates') return acknowledged ? [remaining] : [draft, remaining];
        if (call.method == 'ackCommitted') {
          await acknowledgement.future;
          acknowledged = true;
        }
        return null;
      },
    );
    await tester.pumpWidget(app());
    await tester.pumpAndSettle();
    await tester.tap(find.descendant(of: find.byKey(const ValueKey('scoped-draft')),
      matching: find.text('Review and record')));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Scope food'));
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
    // Regression: review must use quickAdd; otherwise choosing a category just
    // closes the page without ever presenting an amount or saving a transaction.
    expect(find.byType(AmountEditorSheet), findsOneWidget);
    final amountContext = tester.element(find.byType(AmountEditorSheet));
    final scoped = ProviderScope.containerOf(amountContext, listen: false);
    expect(scoped.read(currentLedgerIdProvider), targetId);
    final reviewedLedger = await tester.runAsync(() => scoped.read(currentLedgerProvider.future)
        .timeout(const Duration(seconds: 5)));
    expect(reviewedLedger!.id, targetId);
    expect(scoped.read(currentLedgerCurrencyProvider), 'USD');
    unawaited(TagSelector.show(amountContext));
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
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
    expect(acknowledged, isFalse);
    expect(find.byType(AmountEditorSheet), findsNothing);
    expect(find.byType(TransactionEditorPage), findsNothing);
    expect(find.text('Another pending payment'), findsOneWidget);
    expect(tester.widget<FilledButton>(find.widgetWithText(FilledButton, 'Review and record'))
        .onPressed, isNotNull);
    // Repeated refreshes must reuse the in-flight ACK instead of blocking the
    // next draft or launching more work for the same durable link.
    await tester.tap(find.byIcon(Icons.refresh));
    await tester.pumpAndSettle();
    expect(find.text('Another pending payment'), findsOneWidget);
    acknowledgement.complete();
    await tester.pumpAndSettle();
    expect(acknowledged, isTrue);
    expect(container.read(currentLedgerIdProvider), ledger.id);
    expect(container.read(currentLedgerCurrencyProvider), 'CNY');
    expect(widgetManager.refreshedLedgerIds, [ledger.id]);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
    // AccountSelector's diagnostic logger batches persistence for two seconds.
    await tester.pump(const Duration(seconds: 3));
    await tester.pumpAndSettle();
  }, variant: TargetPlatformVariant.only(TargetPlatform.android), timeout: const Timeout(Duration(minutes: 1)));

  testWidgets('a capture commit finishing after editor exit still refreshes live caches', (tester) async {
    await repo.createCategory(name: 'Late save', kind: 'expense');
    final draft = payload('late-commit', 'Payment saved after leaving');
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
    await tester.tap(find.text('Late save'));
    await tester.pumpAndSettle();
    expect(find.byType(AmountEditorSheet), findsOneWidget);

    // Hold a real database transaction ahead of the payment commit. The user
    // can close both routes while the already-confirmed save waits for SQLite.
    final release = Completer<void>();
    final locked = Completer<void>();
    late Future<void> blocker;
    await tester.runAsync(() async {
      blocker = db.transaction(() async {
        await db.customSelect('SELECT 1').get();
        locked.complete();
        await release.future;
      });
      await locked.future.timeout(const Duration(seconds: 5));
    });
    addTearDown(() async {
      if (!release.isCompleted) release.complete();
      await blocker.timeout(const Duration(seconds: 5));
    });
    final before = container.read(statsRefreshProvider);
    await tester.tap(find.text('Finish'));
    await tester.pump();
    Navigator.of(tester.element(find.byType(AmountEditorSheet))).pop();
    await tester.pump(const Duration(milliseconds: 400));
    Navigator.of(tester.element(find.byType(TransactionEditorPage))).pop();
    await tester.pump(const Duration(milliseconds: 400));
    expect(find.byType(TransactionEditorPage), findsNothing);

    release.complete();
    await tester.runAsync(() => blocker);
    await tester.pumpAndSettle();
    final rows = await db.select(db.transactions).get();
    expect(rows, hasLength(1));
    expect(rows.single.amount, 12.5);
    expect(container.read(statsRefreshProvider), greaterThan(before));
    expect(acknowledged, isTrue);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
    await tester.pump(const Duration(seconds: 3));
    await tester.pumpAndSettle();
  }, variant: TargetPlatformVariant.only(TargetPlatform.android), timeout: const Timeout(Duration(minutes: 1)));
}
