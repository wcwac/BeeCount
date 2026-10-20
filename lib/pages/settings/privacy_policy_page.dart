import 'package:flutter/material.dart';

import '../../l10n/app_localizations.dart';
import '../../widgets/ui/local_document_page.dart';

/// The policy ships with the app; opening it makes no network request.
class PrivacyPolicyPage extends StatelessWidget {
  const PrivacyPolicyPage({super.key});

  @override
  Widget build(BuildContext context) {
    return LocalDocumentPage(
      title: AppLocalizations.of(context).aboutPrivacyPolicy,
      assetPath: 'PRIVACY.md',
    );
  }
}
