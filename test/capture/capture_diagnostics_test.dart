import 'dart:convert';

import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/base_repository.dart';
import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/models/capture_candidate.dart';
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
    id: 1, name: 'PRIVATE_LEDGER_NAME', currency: 'CNY', type: 'personal',
    createdAt: DateTime.utc(2026, 10, 21), syncId: 'PRIVATE_LEDGER_KEY',
    myRole: 'owner', memberCount: 1, isShared: false, monthStartDay: 1,
  );
  Map<String, Object?> status({Map<String, Object?>? diagnostics}) => {
    'supported': true, 'permissionGranted': true, 'serviceConnected': true,
    'settings': {
      'enabled': true, 'notifications': false, 'ledgerId': ledger.id,
      'ledgerKey': CaptureCommitService.ledgerKey(ledger),
      'packages': ['com.tencent.mm'],
    },
    'apps': [{'packageName': 'com.tencent.mm', 'name': 'Fixture payment', 'adapter': 'wechat'}],
    if (diagnostics != null) 'diagnostics': diagnostics,
  };
  Map<String, Object?> diagnostics({String read = 'EMPTY', String outcome = 'UNRECOGNIZED', int saved = 0}) => {
    'startedAt': 1792598400000, 'eventCount': 3, 'readCount': 2,
    'processedCount': 1, 'savedCount': saved,
    'lastEventAt': 1792598401000, 'lastEventPackage': 'com.tencent.mm',
    'lastEventSource': 'accessibility',
    'lastReadAt': 1792598402000, 'lastReadPackage': 'com.tencent.mm',
    'lastReadSource': 'accessibility', 'lastReadResult': read,
    'lastNodeCount': 3, 'lastTextCount': read == 'EMPTY' ? 0 : 2,
    'lastProcessedAt': 1792598403000, 'lastProcessedPackage': 'com.tencent.mm',
    'lastProcessedSource': 'accessibility', 'lastOutcome': outcome,
  };

  Future<void> showSettings(WidgetTester tester) async {
    await tester.pumpWidget(ProviderScope(
      overrides: [
        repositoryProvider.overrideWithValue(_DiagnosticRepository(ledger)),
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
  }
  Future<void> openDiagnostics(WidgetTester tester) async {
    final panel = find.byKey(const ValueKey('capture-diagnostics'));
    await tester.scrollUntilVisible(panel, 250);
    await tester.tap(panel);
    await tester.pumpAndSettle();
  }
  Future<String?> subtitle(WidgetTester tester, String title) async {
    final row = find.widgetWithText(ListTile, title);
    await tester.scrollUntilVisible(row, 250);
    return (tester.widget<ListTile>(row).subtitle as Text).data;
  }
  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(CaptureService.channel, null);
  });

  test('old native status and malformed optional diagnostics remain usable', () {
    expect(CaptureStatus.fromMap(status()).diagnostics, isNull);
    expect(CaptureStatus.fromMap({...status(), 'diagnostics': 'unsupported'}).enabled, isTrue);
    final value = CaptureStatus.fromMap(status(diagnostics: {
      'startedAt': 0, 'lastEventAt': -1, 'lastReadAt': 8640000000000001,
      'eventCount': -1, 'readCount': 2.5, 'processedCount': '3',
      'lastNodeCount': -2, 'lastTextCount': 1,
      'lastReadResult': 'FUTURE_READ_RESULT', 'lastOutcome': 'FUTURE_OUTCOME',
      'lastEventPackage': 'PRIVATE TRANSACTION TEXT', 'lastReadSource': 'page body',
    }));
    expect(value.enabled, isTrue);
    final parsed = value.diagnostics!;
    expect(parsed.startedAt, isNull);
    expect(parsed.lastEventAt, isNull);
    expect(parsed.lastReadAt, isNull);
    expect(parsed.eventCount, 0);
    expect(parsed.readCount, 0);
    expect(parsed.processedCount, 0);
    expect(parsed.lastNodeCount, isNull);
    expect(parsed.lastTextCount, 1);
    expect(parsed.lastEventPackage, isNull);
    expect(parsed.lastReadSource, isNull);
    expect(parsed.lastReadResult, 'FUTURE_READ_RESULT');
    expect(parsed.lastOutcome, 'FUTURE_OUTCOME');
  });

  test('diagnostic metadata excludes unrecognized native fields and payloads', () {
    final parsed = CaptureDiagnostics.fromMap({
      ...diagnostics(), 'merchant': 'PRIVATE_MERCHANT', 'amountMinor': 123456,
      'payload': {'remark': 'PRIVATE_REMARK'}, 'ledgerKey': 'PRIVATE_KEY',
    });
    final metadata = parsed.toMetadata();
    expect(metadata['lastEventPackage'], 'com.tencent.mm');
    expect(metadata['savedCount'], 0);
    expect(metadata, isNot(contains('merchant')));
    expect(metadata, isNot(contains('amountMinor')));
    expect(metadata, isNot(contains('payload')));
    expect(metadata, isNot(contains('ledgerKey')));
    expect(jsonEncode(metadata), isNot(contains('PRIVATE_')));
  });

  testWidgets('legacy status explains that diagnostics are unavailable', (tester) async {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async => call.method == 'getStatus' ? status() : null,
    );
    await showSettings(tester);
    final panel = find.byKey(const ValueKey('capture-diagnostics'));
    await tester.scrollUntilVisible(panel, 250);
    expect((tester.widget<ExpansionTile>(panel).subtitle as Text).data,
        'This version provides no diagnostics. Capture conditions are still available.');
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox.shrink());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('an enabled switch is insufficient without apps and a connected capture source', (tester) async {
    var current = status();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async => call.method == 'getStatus' ? current : null,
    );
    await showSettings(tester);
    final panel = find.byKey(const ValueKey('capture-conditions'));
    final fixtures = <(Map<String, Object?>, String)>[
      ({'settings': {...(status()['settings'] as Map), 'packages': <String>[]}},
        'No apps selected; capture cannot receive transactions.'),
      ({'serviceConnected': false},
        'No capture source is ready. Check permissions and connections.'),
      ({'serviceConnected': false, 'notificationsGranted': true, 'notificationsConnected': true,
        'settings': {...(status()['settings'] as Map), 'notifications': true}},
        'Only notification capture is ready; screen capture is not connected.'),
      (<String, Object?>{}, 'Screen capture is ready; open a transaction page in a selected app.'),
    ];
    for (final fixture in fixtures) {
      current = {...status(), ...fixture.$1};
      await tester.tap(find.byIcon(Icons.refresh));
      await tester.pumpAndSettle();
      await tester.scrollUntilVisible(panel, 200);
      expect((tester.widget<ExpansionTile>(panel).subtitle as Text).data, fixture.$2);
    }
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox.shrink());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('a failed refresh replaces stale connected claims with unconfirmed status', (tester) async {
    var failRead = false;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async {
        if (call.method != 'getStatus') return null;
        if (failRead) throw PlatformException(code: 'CAPTURE_UNAVAILABLE');
        return status();
      },
    );
    await showSettings(tester);
    expect(await subtitle(tester, 'Accessibility service'), 'Granted; service connected');
    failRead = true;
    await tester.tap(find.byIcon(Icons.refresh));
    await tester.pumpAndSettle();
    expect(await subtitle(tester, 'Accessibility service'), 'Connection not confirmed; refresh status');
    final captureSwitch = find.widgetWithText(SwitchListTile, 'Enable local capture');
    await tester.scrollUntilVisible(captureSwitch, -200);
    expect(tester.widget<SwitchListTile>(captureSwitch).onChanged, isNull);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox.shrink());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('empty reads, unmatched rules, waiting results and saved drafts have distinct diagnostics', (tester) async {
    var current = status(diagnostics: diagnostics());
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async => call.method == 'getStatus' ? current : null,
    );
    await showSettings(tester);
    await openDiagnostics(tester);
    expect(await subtitle(tester, 'Read result'),
        'No readable text; this page may expose no accessibility content');
    expect(await subtitle(tester, 'Processing result'),
        'Page data was processed, but no transaction rule matched');

    current = status(diagnostics: diagnostics(read: 'READABLE', outcome: 'WAITING_RESULT'));
    await tester.tap(find.byIcon(Icons.refresh));
    await tester.pumpAndSettle();
    expect(await subtitle(tester, 'Processing result'), 'Checkout detected; waiting for a payment result');

    current = {...status(diagnostics: diagnostics(read: 'READABLE', outcome: 'DRAFT_SAVED', saved: 1)),
      'pendingCount': 1};
    await tester.tap(find.byIcon(Icons.refresh));
    await tester.pumpAndSettle();
    expect(await subtitle(tester, 'Processing result'), 'Draft written; check captured transactions');
    final counts = find.widgetWithText(ListTile, 'Diagnostic counts');
    await tester.scrollUntilVisible(counts, -250);
    expect((tester.widget<ListTile>(counts).subtitle as Text).data,
        'Events 3 · Reads 2 · Processed 1 · New drafts 1');
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox.shrink());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('new empty or failed reads take precedence over an older saved draft', (tester) async {
    final previous = diagnostics(read: 'READABLE', outcome: 'DRAFT_SAVED', saved: 1);
    var current = status(diagnostics: previous);
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async => call.method == 'getStatus' ? current : null,
    );
    await showSettings(tester);
    final panel = find.byKey(const ValueKey('capture-diagnostics'));
    await tester.scrollUntilVisible(panel, 250);
    expect((tester.widget<ExpansionTile>(panel).subtitle as Text).data,
        'Draft written; check captured transactions');
    final stages = <(Map<String, Object?>, String)>[
      ({'lastEventAt': 1792598404000, 'lastReadAt': 1792598404000,
        'lastReadResult': 'EMPTY', 'lastTextCount': 0},
        'No readable text; this page may expose no accessibility content'),
      ({'lastEventAt': 1792598405000, 'lastReadAt': 1792598405000,
        'lastReadResult': 'READ_FAILED'},
        'Page read failed; reopen the transaction page and refresh'),
      ({'lastEventAt': 1792598406000},
        'Capture events received; waiting for a read or processing result.'),
      ({'lastEventAt': 1792598406000, 'lastProcessedAt': 1792598406000,
        'lastOutcome': 'UNRECOGNIZED'},
        'Page data was processed, but no transaction rule matched'),
    ];
    var latest = previous;
    for (final stage in stages) {
      latest = {...latest, ...stage.$1};
      current = status(diagnostics: latest);
      await tester.tap(find.byIcon(Icons.refresh));
      await tester.pumpAndSettle();
      await tester.scrollUntilVisible(panel, 200);
      expect((tester.widget<ExpansionTile>(panel).subtitle as Text).data, stage.$2);
    }
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox.shrink());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('processing failures are not reported as storage failures', (tester) async {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async => call.method == 'getStatus'
          ? {...status(), 'lastError': 'CAPTURE_PROCESSING_FAILED'} : null,
    );
    await showSettings(tester);
    final warning = find.text('Capture processing failed and may have missed transactions. Refresh diagnostics and check your payment records.');
    await tester.scrollUntilVisible(warning, 250);
    expect(warning, findsOneWidget);
    expect(find.text('Capture storage failed. Check for missed transactions.'), findsNothing);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox.shrink());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('copying diagnostics includes metadata and excludes ledger and transaction contents', (tester) async {
    String? copied;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      CaptureService.channel, (call) async => call.method == 'getStatus' ? {
        ...status(diagnostics: {...diagnostics(), 'payload': {'remark': 'PRIVATE_REMARK'}}),
        'merchant': 'PRIVATE_MERCHANT', 'amountMinor': 123456,
      } : null,
    );
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      SystemChannels.platform, (call) async {
        if (call.method == 'Clipboard.setData') copied = (call.arguments as Map)['text'] as String?;
        return null;
      },
    );
    addTearDown(() => TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(SystemChannels.platform, null));
    await showSettings(tester);
    await openDiagnostics(tester);
    final copy = find.byKey(const ValueKey('capture-copy-diagnostics'));
    await tester.scrollUntilVisible(copy, 250);
    await tester.tap(copy);
    await tester.pumpAndSettle();
    expect(copied, isNotNull);
    final report = jsonDecode(copied!) as Map;
    expect(report['statusKnown'], isTrue);
    expect(report['targetLedgerAvailable'], isTrue);
    expect(report['selectedPackages'], ['com.tencent.mm']);
    expect((report['diagnostics'] as Map)['lastOutcome'], 'UNRECOGNIZED');
    expect(copied, isNot(contains('PRIVATE_')));
    expect(report, isNot(contains('ledgerId')));
    expect(report, isNot(contains('ledgerKey')));
    expect(report, isNot(contains('merchant')));
    expect(report, isNot(contains('amountMinor')));
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox.shrink());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));
}

class _DiagnosticRepository implements BaseRepository {
  final Ledger ledger;
  _DiagnosticRepository(this.ledger);
  @override
  Future<Ledger?> getLedgerById(int id) async => id == ledger.id ? ledger : null;
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
