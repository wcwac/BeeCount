import 'package:flutter/material.dart';
import '../settings/offline_feature_page.dart';

/// Compatibility route for an online feature removed from this build.
class AISettingsPage extends StatelessWidget {
  const AISettingsPage({super.key});

  @override
  Widget build(BuildContext context) => const OfflineFeaturePage();
}
