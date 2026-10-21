import 'package:beecount/utils/notification_android.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:timezone/data/latest.dart' as tz;

void main() {
  // Flutter's generated Dart plugin registrant does not run in unit tests.
  // Version 18+ requires registering the real platform implementation explicitly.
  AndroidFlutterLocalNotificationsPlugin.registerWith();
  TestWidgetsFlutterBinding.ensureInitialized();

  test('daily reminder and backups auto-cancel on tap without disabling badges',
      () async {
    debugDefaultTargetPlatformOverride = TargetPlatform.android;
    addTearDown(() => debugDefaultTargetPlatformOverride = null);
    tz.initializeTimeZones();
    final pluginCalls = <MethodCall>[];
    final nativeCalls = <MethodCall>[];
    final scheduled = <Map<dynamic, dynamic>>[];
    const pluginChannel =
        MethodChannel('dexterous.com/flutter/local_notifications');
    const nativeChannel = MethodChannel('notification_channel');
    final messenger =
        TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;
    messenger.setMockMethodCallHandler(pluginChannel, (call) async {
      pluginCalls.add(call);
      if (call.method == 'zonedSchedule') {
        scheduled.add(call.arguments as Map<dynamic, dynamic>);
        return null;
      }
      if (const {
        'initialize',
        'requestNotificationsPermission',
        'requestExactAlarmsPermission',
        'canScheduleExactNotifications',
      }.contains(call.method)) {
        return true;
      }
      throw PlatformException(code: 'unexpected-method', message: call.method);
    });
    messenger.setMockMethodCallHandler(nativeChannel, (call) async {
      nativeCalls.add(call);
      return true;
    });
    addTearDown(() {
      messenger.setMockMethodCallHandler(pluginChannel, null);
      messenger.setMockMethodCallHandler(nativeChannel, null);
    });

    final plugin = FlutterLocalNotificationsPlugin();
    expect(
      plugin.resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin>(),
      isA<AndroidFlutterLocalNotificationsPlugin>(),
    );
    await AndroidNotificationUtil(plugin).scheduleDailyReminder(
      id: 1001,
      title: 'QA reminder',
      body: 'Synthetic notification',
      hour: 21,
      minute: 0,
    );

    expect(pluginCalls.first, isMethodCall('initialize', arguments: {
      'defaultIcon': '@mipmap/ic_launcher',
    }));
    expect(pluginCalls.where((call) => call.method == 'initialize'), hasLength(1));
    expect(scheduled.map((request) => request['id']),
        orderedEquals(List.generate(8, (index) => 1001 + index)));
    for (final request in scheduled) {
      final details = request['platformSpecifics'] as Map<dynamic, dynamic>;
      expect(details['autoCancel'], isTrue);
      expect(details['channelShowBadge'], isTrue);
      expect(request['scheduledDateTime'], isNotEmpty);
    }
    expect(nativeCalls, hasLength(1));
    expect(nativeCalls.single.method, 'scheduleNotification');
    expect((nativeCalls.single.arguments as Map)['notificationId'], 1101);
  });
}
