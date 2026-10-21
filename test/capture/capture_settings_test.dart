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

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(CaptureService.channel, null);
  });

  testWidgets('a late status cannot re-enable capture after it was turned off', (tester) async {
    final staleStatus = Completer<Map<String, Object?>>();
    addTearDown(() { if (!staleStatus.isCompleted) staleStatus.complete(status(true)); });
    var reads = 0;
    var enabled = true;
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
    expect(updates.single['enabled'], isFalse);
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
    expect(updates, hasLength(2));
    expect(updates.last['enabled'], isFalse);
    expect(updates.last['packages'], isEmpty);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox());
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));
}

class _LedgerRepository implements BaseRepository {
  final Ledger ledger;
  _LedgerRepository(this.ledger);

  @override
  Future<Ledger?> getLedgerById(int id) async => id == ledger.id ? ledger : null;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
