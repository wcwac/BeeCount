/// Compatibility values only; no platform URL-launcher plugin is imported.
enum LaunchMode { platformDefault, externalApplication, externalNonBrowserApplication }

Future<bool> canLaunchUrl(Uri uri) async => false;
Future<bool> launchUrl(Uri uri, {
  LaunchMode mode = LaunchMode.platformDefault,
}) async => false;
