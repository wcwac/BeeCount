import 'package:flutter_cloud_sync/flutter_cloud_sync.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  setUp(() => SharedPreferences.setMockInitialValues({}));

  test('direct authentication cannot send even to an injected HTTP client', () async {
    var requests = 0;
    final client = MockClient((request) async {
      requests++;
      return http.Response('{}', 500);
    });
    final auth = BeeCountCloudAuthService(
      baseUrl: 'https://cloud.example', apiPrefix: '/api', httpClient: client,
    );
    addTearDown(auth.dispose);
    await expectLater(
      auth.signInWithEmail(email: 'user@example.com', password: 'test-password'),
      throwsUnsupportedError,
    );
    await expectLater(
      auth.signUpWithEmail(email: 'user@example.com', password: 'test-password'),
      throwsUnsupportedError,
    );
    expect(requests, 0);
  });

  test('old cloud choices cannot be restored or activated', () async {
    SharedPreferences.setMockInitialValues({'cloud_active_type': 'icloud'});
    final store = CloudServiceStore();
    expect((await store.loadActive()).type, CloudBackendType.local);
    for (final type in CloudBackendType.values) {
      if (type == CloudBackendType.local) continue;
      expect(await store.activate(type), isFalse);
      final config = CloudServiceConfig(type: type, name: 'old cloud');
      await expectLater(store.saveAndActivate(config), throwsUnsupportedError);
      await expectLater(createCloudServices(config), throwsUnsupportedError);
    }
    final local = await createCloudServices(CloudServiceConfig.localStorage());
    expect(local.provider, isNull);
    expect(local.auth, isNull);
  });
}
