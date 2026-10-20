import 'dart:io';
import 'dart:typed_data';

import 'package:beecount/ai/providers/ai_provider_config.dart';
import 'package:beecount/ai/providers/ai_provider_factory.dart';
import 'package:beecount/providers/ai_config_providers.dart';
import 'package:beecount/services/security/offline_network_policy.dart';
import 'package:beecount/services/security/offline_url_launcher.dart';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

class CountingAdapter implements HttpClientAdapter {
  int calls = 0;
  @override
  Future<ResponseBody> fetch(RequestOptions options,
      Stream<Uint8List>? requestStream, Future<void>? cancelFuture) async {
    calls++;
    throw StateError('No transport may be reached');
  }
  @override
  void close({bool force = false}) {}
}

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

  test('AI requests and probes never reach an injected transport', () async {
    final adapter = CountingAdapter();
    final client = Dio()..httpClientAdapter = adapter;
    addTearDown(client.close);
    final config = AIServiceProviderConfig(
      id: 'old', name: 'Old provider', apiKey: 'old-key',
      baseUrl: 'https://example.com', textModel: 'model', createdAt: DateTime(2024),
    );
    final result = await AIProviderFactory.validateTextCapability(config, client: client);
    expect(result.$1, isFalse);
    final capabilities = await AIProviderFactory.probeAgentCapabilities(config, client: client);
    expect(capabilities.canRunNativeToolAgent, isFalse);
    await expectLater(
      AIProviderFactory.chatWithToolsStreamForConfig(
        config: config, messages: const [], tools: const [], client: client,
      ).toList(),
      throwsA(isA<AIException>()),
    );
    // Missing paths prove these APIs reject before reading any private file.
    await expectLater(AIProviderFactory.vision(File('/missing/image'), 'read'),
        throwsA(isA<AIException>()));
    await expectLater(AIProviderFactory.speechToText(File('/missing/audio')),
        throwsA(isA<AIException>()));
    expect(adapter.calls, 0);
  });
}
