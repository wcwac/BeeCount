import 'dart:io';

import 'package:beecount/services/security/offline_network_policy.dart';
import 'package:beecount/services/security/offline_url_launcher.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:beecount/services/platform/app_link_service.dart';

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


  test('retired deep links cannot open automatic billing', () {
    for (final action in ['voice', 'image', 'camera', 'ai-chat', 'add',
        'auto-billing', 'quick-billing']) {
      expect(AppLinkService.parseAction(Uri.parse('beecount://$action')),
          AppLinkAction.unknown);
    }
    expect(AppLinkService.parseAction(Uri.parse('https://new')),
        AppLinkAction.unknown);
  });

  test('widget deep links retain manual navigation', () {
    expect(AppLinkService.parseAction(Uri.parse('beecount://new?type=expense')),
        AppLinkAction.newTransaction);
    expect(AppLinkService.parseAction(Uri.parse('beecount://open?page=assets')),
        AppLinkAction.open);
  });
}
