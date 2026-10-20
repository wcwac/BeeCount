import 'package:flutter/material.dart';
import '../settings/offline_feature_page.dart';

/// Retained for legacy navigation; this edition has no remote accounts.
class AuthPage extends StatelessWidget {
  const AuthPage({super.key});

  @override
  Widget build(BuildContext context) => const OfflineFeaturePage();
}

class LoginPage extends AuthPage {
  const LoginPage({super.key});
}
