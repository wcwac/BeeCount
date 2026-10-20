import 'dart:io';

/// A fail-closed transport boundary for this offline-only build. Install before
/// plugin or provider initialization, including background isolate entry points.
class OfflineHttpOverrides extends HttpOverrides {
  @override
  HttpClient createHttpClient(SecurityContext? context) {
    throw const SocketException('Network access is disabled in this offline build.');
  }
}

void installOfflineNetworkPolicy() {
  HttpOverrides.global = OfflineHttpOverrides();
}
