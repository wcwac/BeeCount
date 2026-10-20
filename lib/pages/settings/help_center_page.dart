import 'package:flutter/material.dart';

import '../../l10n/app_localizations.dart';
import '../../widgets/ui/local_document_page.dart';

/// Opens bundled documentation without a WebView or network requests.
class HelpCenterPage extends StatelessWidget {
  const HelpCenterPage({super.key, this.initialUrl, this.title});

  /// Used only to select a local document; this URL is never fetched.
  final String? initialUrl;
  final String? title;

  @override
  Widget build(BuildContext context) {
    final chinese = Localizations.localeOf(context).languageCode == 'zh';
    final cloudGuide =
        Uri.tryParse(initialUrl ?? '')?.path.contains('/cloud-sync/') ?? false;
    final assetPath = cloudGuide
        ? (chinese ? 'docs/cloud-setup.md' : 'docs/cloud-setup_EN.md')
        : (chinese ? 'README.md' : 'README_EN.md');
    return LocalDocumentPage(
      title: title ?? AppLocalizations.of(context).mineHelp,
      assetPath: assetPath,
    );
  }
}
