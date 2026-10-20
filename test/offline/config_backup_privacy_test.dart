import 'package:beecount/services/export/config_export_service.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('local backup does not export retired credentials or enabled flags', () async {
    SharedPreferences.setMockInitialValues({
      'cloud_supabase_cfg': '{"password":"do-not-export"}',
      'cloud_beecount_cloud_cfg': '{"password":"do-not-export"}',
      'ai_providers': '[{"apiKey":"do-not-export"}]',
      'ai_bill_extraction_enabled': true,
      'cloud_active_type': 'beecountCloud',
      'auto_sync': true,
      'auto_screenshot_billing_enabled': true,
      'headerSkin': 'anniversary',
      'themeMode': 'dark',
      'noteDisplayMode': 'note',
    });
    final yaml = await ConfigExportService.exportToYaml();
    expect(yaml, contains('theme_mode: "dark"'));
    expect(yaml, contains('note_display_mode: "note"'));
    for (final retired in ['do-not-export', 'supabase:', 'beecount_cloud:',
        'ai:', 'auto_sync:', 'auto_screenshot_enabled:', 'header_skin:']) {
      expect(yaml, isNot(contains(retired)));
    }
  });

  test('old backup imports local settings and ignores retired online sections', () async {
    SharedPreferences.setMockInitialValues({});
    await ConfigExportService.importFromYaml('''
supabase:
  url: https://example.invalid
  anon_key: secret
ai:
  api_key: secret
app_settings:
  theme_mode: dark
  note_display_mode: note
  cloud_service_type: beecountCloud
  auto_sync: true
  auto_screenshot_enabled: true
''');
    final prefs = await SharedPreferences.getInstance();
    expect(prefs.getString('themeMode'), 'dark');
    expect(prefs.getString('noteDisplayMode'), 'note');
    expect(prefs.containsKey('cloud_supabase_cfg'), isFalse);
    expect(prefs.containsKey('cloud_active_type'), isFalse);
    expect(prefs.containsKey('auto_sync'), isFalse);
    expect(prefs.containsKey('auto_screenshot_billing_enabled'), isFalse);
    expect(ConfigExportService.detectContent('supabase: {}\nai: {}').hasAppSettings,
        isFalse);
  });
}
