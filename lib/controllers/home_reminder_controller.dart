import 'package:shared_preferences/shared_preferences.dart';

/// Calendar rules and persisted dismissals for the home report reminders.
/// The clock is injectable so month/year boundaries can be tested directly.
class HomeReminderController {
  HomeReminderController(this.preferences, {DateTime Function()? now})
      : _now = now ?? DateTime.now;

  final SharedPreferences preferences;
  final DateTime Function() _now;
  static const _monthKey = 'last_month_reminder_dismissed';
  static const _yearKey = 'annual_report_reminder_dismissed';

  static int reportYear(DateTime date) =>
      date.month == 1 ? date.year - 1 : date.year;

  bool get showMonthly {
    final date = _now();
    return date.day <= 7 &&
        preferences.getString(_monthKey) != '${date.year}-${date.month}';
  }

  bool get showAnnual {
    final date = _now();
    return ((date.month == 12 && date.day >= 15) || date.month == 1) &&
        preferences.getInt(_yearKey) != reportYear(date);
  }

  Future<void> dismissMonthly() async {
    final date = _now();
    await preferences.setString(_monthKey, '${date.year}-${date.month}');
  }

  Future<void> dismissAnnual() async {
    await preferences.setInt(_yearKey, reportYear(_now()));
  }
}
