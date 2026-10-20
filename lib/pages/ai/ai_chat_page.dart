import 'package:flutter/material.dart';
import '../settings/offline_feature_page.dart';

/// Compatibility route for an online feature removed from this build.
class AIChatPage extends StatelessWidget {
  const AIChatPage({super.key});

  @override
  Widget build(BuildContext context) => const OfflineFeaturePage();
}
