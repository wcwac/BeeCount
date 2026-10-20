import '../core/auth_service.dart';
import '../core/cloud_provider.dart';
import 'cloud_service_config.dart';

/// Offline-only build: never initialize a native or HTTP cloud backend.
Future<({CloudProvider? provider, CloudAuthService? auth})> createCloudServices(
  CloudServiceConfig config,
) async {
  if (config.type != CloudBackendType.local) {
    throw UnsupportedError('Cloud services are disabled in this offline build.');
  }
  return (provider: null, auth: null);
}

@Deprecated('Use createCloudServices instead')
Future<CloudProvider?> createCloudProvider(CloudServiceConfig config) async {
  final services = await createCloudServices(config);
  return services.provider;
}
