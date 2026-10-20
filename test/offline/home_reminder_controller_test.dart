import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:beecount/controllers/home_reminder_controller.dart';

void main() {
  late DateTime now;
  late HomeReminderController reminders;
  late SharedPreferences preferences;

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    preferences = await SharedPreferences.getInstance();
    now = DateTime(2026, 12, 1);
    reminders = HomeReminderController(preferences, now: () => now);
  });

  test('monthly reminders appear only on the first seven days', () {
    expect(reminders.showMonthly, isTrue);
    now = DateTime(2026, 12, 7);
    expect(reminders.showMonthly, isTrue);
    now = DateTime(2026, 12, 8);
    expect(reminders.showMonthly, isFalse);
  });

  test('monthly dismissal survives recreation and resets next month', () async {
    await reminders.dismissMonthly();
    expect(HomeReminderController(preferences, now: () => now).showMonthly, isFalse);
    now = DateTime(2027, 1, 1);
    expect(reminders.showMonthly, isTrue);
  });

  test('annual window runs December 15 through January 31', () {
    now = DateTime(2026, 12, 14);
    expect(reminders.showAnnual, isFalse);
    now = DateTime(2026, 12, 15);
    expect(reminders.showAnnual, isTrue);
    now = DateTime(2027, 1, 31);
    expect(reminders.showAnnual, isTrue);
    expect(HomeReminderController.reportYear(now), 2026);
    now = DateTime(2027, 2, 1);
    expect(reminders.showAnnual, isFalse);
  });

  test('annual dismissal covers both sides of the year boundary', () async {
    now = DateTime(2026, 12, 20);
    await reminders.dismissAnnual();
    now = DateTime(2027, 1, 4);
    expect(reminders.showAnnual, isFalse);
    now = DateTime(2027, 12, 20);
    expect(reminders.showAnnual, isTrue);
  });
}
