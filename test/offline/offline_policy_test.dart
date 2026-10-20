import 'dart:io';

import 'package:beecount/providers/ai_config_providers.dart';
import 'package:beecount/services/security/offline_network_policy.dart';
import 'package:beecount/services/security/offline_url_launcher.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:beecount/services/automation/auto_billing_service.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('HTTP client construction is rejected before connecting', () {
    HttpOverrides.runZoned(() {
      expect(() => HttpClient(), throwsA(isA<SocketException>()));
    }, createHttpClient: OfflineHttpOverrides().createHttpClient);
  });

  test('external links cannot be delegated to another app', () async {
    expect(await canLaunchUrl(Uri.parse('https://example.com')), isFalse);
    expect(await launchUrl(Uri.parse('https://example.com')), isFalse);
  });

  test('saved AI settings and setters cannot enable uploads', () async {
    SharedPreferences.setMockInitialValues({
      'ai_bill_extraction_enabled': true, 'ai_use_vision': true,
      'ai_strategy': 'cloud_only',
    });
    final settings = AIConfigNotifier();
    addTearDown(settings.dispose);
    await settings.ensureLoaded();
    await settings.setEnabled(true);
    await settings.setUseVision(true);
    await settings.setStrategy(AIStrategy.cloudOnly);
    expect(settings.state.enabled, isFalse);
    expect(settings.state.useVision, isFalse);
    expect(settings.state.strategy, AIStrategy.localOnly);
  });

  test('legacy automation rejects input without reading files or creating bills', () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final service = AutoBillingService(container);
    addTearDown(service.dispose);
    expect(await service.processScreenshot('/missing/image'), isNull);
    expect(await service.processText('买菜花了 100 元'), isNull);
  });
}
