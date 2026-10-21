import 'dart:async';

import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/base_repository.dart';
import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/pages/settings/capture_settings_page.dart';
import 'package:beecount/providers.dart';
import 'package:beecount/services/capture/capture_commit_service.dart';
import 'package:beecount/services/capture/capture_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  final ledger = Ledger(
    id: 1,
    name: 'Settings ledger',
    currency: 'CNY',
    type: 'personal',
    createdAt: DateTime.utc(2026, 10, 21),
    syncId: 'settings-ledger',
    myRole: 'owner',
    memberCount: 1,
    isShared: false,
    monthStartDay: 1,
  );
  Map<String, Object?> status(bool enabled) => {
    'supported': true,
    'settings': {
      'enabled': enabled,
      'notifications': false,
      'ledgerId': ledger.id,
      'ledgerKey': CaptureCommitService.ledgerKey(ledger),
      'packages': ['com.tencent.mm'],
    },
    'apps': [
      {'packageName': 'com.tencent.mm', 'name': 'Fixture payment', 'adapter': 'wechat'},
    ],
  };

  Future<void> showSettings(WidgetTester tester, _LedgerRepository repository,
      {List<Ledger>? available}) async {
    await tester.pumpWidget(ProviderScope(
      overrides: [
        repositoryProvider.overrideWithValue(repository),
        ledgersStreamProvider.overrideWith((ref) => Stream.value(available ?? [ledger])),
      ],
      child: MaterialApp(
        locale: const Locale('en'),
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        supportedLocales: AppLocalizations.supportedLocales,
        home: const CaptureSettingsPage(),
      ),
    ));
    await tester.pumpAndSettle();
  }

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(CaptureService.channel, null);
  });

  testWidgets('a late status cannot re-enable capture after it was turned off', (tester) async {
    final staleStatus = Completer<Map<String, Object?>>();
    addTearDown(() { if (!staleStatus.isCompleted) staleStatus.complete(status(true)); });
    var reads = 0;
    var enabled = true;
    var stops = 0;
    final updates = <Map<String, dynamic>>[];
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'getStatus') {
          reads++;
          if (reads == 2) return staleStatus.future;
          return status(enabled);
        }
        if (call.method == 'updateSettings') {
          final settings = Map<String, dynamic>.from(call.arguments as Map);
          updates.add(settings);
          enabled = settings['enabled'] as bool;
        }
        if (call.method == 'stopCapture') { stops++; enabled = false; }
        return null;
      },
    );
    await tester.pumpWidget(ProviderScope(
      overrides: [
        repositoryProvider.overrideWithValue(_LedgerRepository(ledger)),
        ledgersStreamProvider.overrideWith((ref) => Stream.value([ledger])),
      ],
      child: MaterialApp(
        locale: const Locale('en'),
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        supportedLocales: AppLocalizations.supportedLocales,
        home: const CaptureSettingsPage(),
      ),
    ));
    await tester.pumpAndSettle();
    await tester.tap(find.byIcon(Icons.refresh));
    await tester.pumpAndSettle();
    expect(reads, 2);

    final captureSwitch = find.widgetWithText(SwitchListTile, 'Enable local capture');
    await tester.tap(captureSwitch);
    await tester.pumpAndSettle();
    expect(stops, 1);
    expect(updates, isEmpty);
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);

    staleStatus.complete(status(true));
    await tester.pumpAndSettle();
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);

    // A later package edit uses the displayed enabled value. A stale status
    // must not turn that unrelated edit into a new grant of capture consent.
    final payment = find.widgetWithText(CheckboxListTile, 'Fixture payment');
    await tester.scrollUntilVisible(payment, 300);
    await tester.tap(payment);
    await tester.pumpAndSettle();
    expect(updates, hasLength(1));
    expect(updates.last['enabled'], isFalse);
    expect(updates.last['packages'], isEmpty);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  for (final errorAfterCommit in [false, true]) {
    testWidgets('unreadable settings after ${errorAfterCommit ? 'an error reply' : 'a successful write'} stay locked until stopped', (tester) async {
      final write = Completer<void>();
      addTearDown(() { if (!write.isCompleted) write.complete(); });
      final repository = _LedgerRepository(ledger);
      var enabled = false;
      var reads = 0, updates = 0, stops = 0;
      var failReadback = false;
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
        CaptureService.channel, (call) async {
          if (call.method == 'getStatus') {
            reads++;
            if (failReadback) throw PlatformException(code: 'CAPTURE_UNAVAILABLE');
            return status(enabled);
          }
          if (call.method == 'updateSettings') {
            updates++;
            enabled = true; // Persistence precedes the bridge reply.
            failReadback = true;
            await write.future;
            if (errorAfterCommit) throw PlatformException(code: 'INVALID_SETTINGS');
          }
          if (call.method == 'stopCapture') {
            stops++;
            enabled = false;
            failReadback = false;
          }
          return null;
        },
      );
      await showSettings(tester, repository);
      final captureSwitch = find.widgetWithText(SwitchListTile, 'Enable local capture');
      await tester.tap(captureSwitch);
      await tester.pumpAndSettle();
      expect(updates, 1);
      // Returning from system settings during the write cannot unlock the old
      // snapshot. Only a read after the final write reply can confirm state.
      tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.inactive);
      tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.resumed);
      await tester.pumpAndSettle();
      expect(reads, 1);
      expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNull);

      write.complete();
      await tester.pumpAndSettle();
      expect(reads, 2);
      expect(enabled, isTrue);
      expect(find.textContaining('Capture status is unknown.'), findsOneWidget);
      expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNull);
      expect(tester.widget<SwitchListTile>(find.widgetWithText(SwitchListTile,
        'Notification assistance (optional)')).onChanged, isNull);
      final payment = find.widgetWithText(CheckboxListTile, 'Fixture payment');
      await tester.scrollUntilVisible(payment, 300);
      expect(tester.widget<CheckboxListTile>(payment).onChanged, isNull);
      expect(tester.widget<DropdownButtonFormField<int>>(
        find.byType(DropdownButtonFormField<int>)).onChanged, isNull);
      await tester.tap(payment);
      await tester.pumpAndSettle();
      expect(updates, 1);

      // Revocation bypasses both the unreadable snapshot and the repository.
      final ledgerReads = repository.reads;
      repository.failReads = true;
      final stop = find.byKey(const ValueKey('capture-stop'));
      await tester.scrollUntilVisible(stop, -300);
      await tester.tap(stop);
      await tester.pumpAndSettle();
      expect(stops, 1);
      expect(repository.reads, ledgerReads);
      expect(enabled, isFalse);
      expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);
      expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNotNull);
      expect(find.textContaining('Capture status is unknown.'), findsNothing);
      expect(tester.takeException(), isNull);
      await tester.pumpWidget(const SizedBox());
    }, variant: TargetPlatformVariant.only(TargetPlatform.android));
  }

  testWidgets('an error after persistence reloads the real enabled value', (tester) async {
    var enabled = false;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'getStatus') return status(enabled);
        if (call.method == 'updateSettings') {
          enabled = true;
          throw PlatformException(code: 'INVALID_SETTINGS');
        }
        return null;
      },
    );
    await showSettings(tester, _LedgerRepository(ledger));
    final captureSwitch = find.widgetWithText(SwitchListTile, 'Enable local capture');
    await tester.tap(captureSwitch);
    await tester.pumpAndSettle();
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isTrue);
    expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNotNull);
    expect(find.textContaining('The current settings have been reloaded'), findsOneWidget);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('stopping invalidates an enable request waiting for its ledger', (tester) async {
    final lookup = Completer<Ledger?>();
    addTearDown(() { if (!lookup.isCompleted) lookup.complete(ledger); });
    final repository = _LedgerRepository(ledger)..readOverride = (_) => lookup.future;
    var stops = 0, updates = 0;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'getStatus') return status(false);
        if (call.method == 'stopCapture') stops++;
        if (call.method == 'updateSettings') updates++;
        return null;
      },
    );
    await showSettings(tester, repository);
    final captureSwitch = find.widgetWithText(SwitchListTile, 'Enable local capture');
    await tester.tap(captureSwitch);
    await tester.pumpAndSettle();
    expect(repository.reads, 1);
    expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNull);
    await tester.tap(find.byKey(const ValueKey('capture-stop')));
    await tester.pumpAndSettle();
    expect(stops, 1);
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);
    lookup.complete(ledger);
    await tester.pumpAndSettle();
    expect(updates, 0);
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);
    expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNotNull);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('stopping supersedes both a pending write reply and its old status readback', (tester) async {
    final writeReply = Completer<void>();
    final statusReply = Completer<Map<String, Object?>>();
    addTearDown(() {
      if (!writeReply.isCompleted) writeReply.complete();
      if (!statusReply.isCompleted) statusReply.complete(status(true));
    });
    var enabled = false, heldStatus = false;
    var updates = 0, stops = 0;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'getStatus') {
          if (updates == 2 && enabled && !heldStatus) {
            heldStatus = true;
            return statusReply.future;
          }
          return status(enabled);
        }
        if (call.method == 'updateSettings') {
          enabled = true;
          if (++updates == 1) await writeReply.future;
        }
        if (call.method == 'stopCapture') { stops++; enabled = false; }
        return null;
      },
    );
    await showSettings(tester, _LedgerRepository(ledger));
    final captureSwitch = find.widgetWithText(SwitchListTile, 'Enable local capture');
    final stop = find.byKey(const ValueKey('capture-stop'));
    await tester.tap(captureSwitch);
    await tester.pumpAndSettle();
    expect(enabled, isTrue);
    expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNull);
    await tester.tap(stop);
    await tester.pumpAndSettle();
    expect(stops, 1);
    expect(enabled, isFalse);
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);
    writeReply.complete();
    await tester.pumpAndSettle();
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);

    // A later explicit enable finishes writing, but its readback hangs. Stop
    // still bypasses that read; the late enabled snapshot cannot overwrite it.
    await tester.tap(captureSwitch);
    await tester.pumpAndSettle();
    expect(updates, 2);
    expect(heldStatus, isTrue);
    await tester.tap(stop);
    await tester.pumpAndSettle();
    expect(stops, 2);
    expect(enabled, isFalse);
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);
    statusReply.complete(status(true));
    await tester.pumpAndSettle();
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);
    expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNotNull);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('a stopped capture survives a timed out readback and a failed stop retry', (tester) async {
    final readback = Completer<Map<String, Object?>>();
    final retryStopReply = Completer<void>();
    addTearDown(() {
      if (!readback.isCompleted) readback.complete(status(true));
      if (!retryStopReply.isCompleted) retryStopReply.complete();
    });
    var reads = 0, stops = 0;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'getStatus') {
          if (++reads == 2) return readback.future;
          if (reads == 3) throw PlatformException(code: 'CAPTURE_UNAVAILABLE');
          return status(reads == 1);
        }
        if (call.method == 'stopCapture') {
          if (++stops == 2) {
            await retryStopReply.future;
            throw PlatformException(code: 'INVALID_SETTINGS');
          }
        }
        return null;
      },
    );
    await showSettings(tester, _LedgerRepository(ledger));
    final stop = find.byKey(const ValueKey('capture-stop'));
    final captureSwitch = find.widgetWithText(SwitchListTile, 'Enable local capture');
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isTrue);
    await tester.tap(stop);
    await tester.pumpAndSettle();
    expect(stops, 1);
    expect(find.text('Stopped; refreshing settings'), findsOneWidget);
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);
    expect(find.textContaining('Capture is stopped. Other settings are unknown;'), findsOneWidget);
    expect(tester.widget<OutlinedButton>(stop).onPressed, isNull);

    await tester.pump(const Duration(seconds: 5));
    await tester.pumpAndSettle();
    expect(find.textContaining('Capture is stopped. Other settings are unknown;'), findsOneWidget);
    expect(tester.widget<OutlinedButton>(stop).onPressed, isNotNull);
    expect(tester.widget<IconButton>(find.widgetWithIcon(IconButton, Icons.refresh)).onPressed, isNotNull);
    expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNull);

    readback.complete(status(true));
    await tester.pumpAndSettle();
    expect(find.textContaining('Capture is stopped. Other settings are unknown;'), findsOneWidget);
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);
    expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNull);
    await tester.tap(stop);
    await tester.pumpAndSettle();
    expect(stops, 2);
    // An outstanding retry cannot turn the old, enabled snapshot into evidence
    // that capture has restarted after the first successful stop.
    expect(find.text('Stopped; refreshing settings'), findsOneWidget);
    expect(tester.widget<OutlinedButton>(stop).onPressed, isNull);
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);
    expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNull);
    retryStopReply.complete();
    await tester.pumpAndSettle();
    expect(reads, 3);
    expect(find.textContaining('Capture is stopped. Other settings are unknown;'), findsOneWidget);
    expect(tester.widget<OutlinedButton>(stop).onPressed, isNotNull);
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);
    expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNull);

    await tester.tap(find.byIcon(Icons.refresh));
    await tester.pumpAndSettle();
    expect(reads, 4);
    expect(tester.widget<SwitchListTile>(captureSwitch).value, isFalse);
    expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNotNull);
    expect(find.textContaining('Capture is stopped. Other settings are unknown;'), findsNothing);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('capture can stop without a readable status or ledger', (tester) async {
    final repository = _LedgerRepository(null)..failReads = true;
    var stopped = false;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'getStatus') {
          if (!stopped) throw PlatformException(code: 'CAPTURE_UNAVAILABLE');
          return status(false);
        }
        if (call.method == 'stopCapture') stopped = true;
        if (call.method == 'updateSettings') fail('Stopping must not send stale settings');
        return null;
      },
    );
    await showSettings(tester, repository, available: []);
    expect(find.textContaining('Capture status is unknown.'), findsOneWidget);
    await tester.tap(find.byKey(const ValueKey('capture-stop')));
    await tester.pumpAndSettle();
    expect(stopped, isTrue);
    expect(repository.reads, 0);
    expect(tester.widget<SwitchListTile>(find.widgetWithText(SwitchListTile,
      'Enable local capture')).value, isFalse);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('only explicit selection rebinds a restored ledger with the same id', (tester) async {
    final restored = ledger.copyWith(name: 'Restored ledger',
      createdAt: ledger.createdAt.add(const Duration(seconds: 1)));
    var nativeSettings = Map<String, Object?>.from(status(true)['settings'] as Map);
    final updates = <Map<String, dynamic>>[];
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method == 'getStatus') return {...status(true), 'settings': nativeSettings};
        if (call.method == 'updateSettings') {
          final update = Map<String, dynamic>.from(call.arguments as Map);
          updates.add(update);
          nativeSettings = update;
        }
        return null;
      },
    );
    await showSettings(tester, _LedgerRepository(restored), available: [restored]);
    final dropdown = find.byType(DropdownButtonFormField<int>);
    expect(tester.widget<DropdownButtonFormField<int>>(dropdown).initialValue, isNull);
    final unavailable = find.textContaining('capture ledger is unavailable');
    await tester.scrollUntilVisible(unavailable, 300);
    expect(unavailable, findsOneWidget);
    final payment = find.widgetWithText(CheckboxListTile, 'Fixture payment');
    await tester.scrollUntilVisible(payment, 300);
    await tester.tap(payment);
    await tester.pumpAndSettle();
    expect(updates, isEmpty);
    expect(nativeSettings['ledgerKey'], CaptureCommitService.ledgerKey(ledger));

    await tester.scrollUntilVisible(dropdown, -300);
    await tester.tap(dropdown);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Restored ledger').last);
    await tester.pumpAndSettle();
    expect(updates, hasLength(1));
    expect(updates.single['ledgerId'], ledger.id);
    expect(updates.single['ledgerKey'], CaptureCommitService.ledgerKey(restored));
    expect(updates.single['enabled'], isTrue);
    expect(tester.widget<DropdownButtonFormField<int>>(dropdown).initialValue, ledger.id);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('service authorization and actual connections are shown separately', (tester) async {
    var granted = false, connected = false;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async => call.method == 'getStatus' ? {
        ...status(true),
        'permissionGranted': granted, 'serviceConnected': connected,
        'notificationsGranted': granted, 'notificationsConnected': connected,
        'settings': {...(status(true)['settings'] as Map), 'notifications': true},
      } : null,
    );
    await showSettings(tester, _LedgerRepository(ledger));
    for (final expected in [
      'Not granted; enable in system settings',
      'Granted; service not connected. Refresh to check',
      'Granted; service connected',
    ]) {
      if (expected.startsWith('Granted')) {
        granted = true;
        connected = expected == 'Granted; service connected';
        await tester.tap(find.byIcon(Icons.refresh));
        await tester.pumpAndSettle();
      }
      final accessibilityRow = find.widgetWithText(ListTile, 'Accessibility service');
      await tester.scrollUntilVisible(accessibilityRow, -250);
      final accessibility = tester.widget<ListTile>(accessibilityRow);
      expect((accessibility.subtitle as Text).data, expected);
      final notification = find.widgetWithText(ListTile, 'Notification access');
      await tester.scrollUntilVisible(notification, 250);
      expect((tester.widget<ListTile>(notification).subtitle as Text).data, startsWith(expected));
    }
    expect(find.textContaining('permission to send notifications'), findsOneWidget);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('past inbox saturation is distinct from an inbox that is currently full', (tester) async {
    var pending = 0;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async => call.method == 'getStatus' ? {
        ...status(true), 'pendingCount': pending, 'lastError': 'INBOX_FULL',
        'skippedObservations': 3, 'importantObservationsSkipped': true, 'overflowCursor': 3,
      } : null,
    );
    await showSettings(tester, _LedgerRepository(ledger));
    final history = find.text('The capture inbox was full and may have missed transactions. Check your payment records.');
    await tester.scrollUntilVisible(history, 300);
    expect(history, findsOneWidget);
    expect(find.text('Capture records to process (0)'), findsOneWidget);
    expect(find.text('Dismiss warning'), findsOneWidget);
    pending = 1000;
    await tester.tap(find.byIcon(Icons.refresh));
    await tester.pumpAndSettle();
    expect(history, findsNothing);
    expect(find.text('Your capture inbox is full; process pending records first.'), findsOneWidget);
    expect(find.text('Dismiss warning'), findsOneWidget);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));
}

class _LedgerRepository implements BaseRepository {
  final Ledger? ledger;
  bool failReads = false;
  int reads = 0;
  Future<Ledger?> Function(int id)? readOverride;
  _LedgerRepository(this.ledger);

  @override
  Future<Ledger?> getLedgerById(int id) async {
    reads++;
    if (failReads) throw StateError('Fixture ledger is unavailable');
    if (readOverride != null) return readOverride!(id);
    return id == ledger?.id ? ledger : null;
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
