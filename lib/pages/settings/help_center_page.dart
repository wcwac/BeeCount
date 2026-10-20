import 'package:flutter/material.dart';

import '../../l10n/app_localizations.dart';
import '../../widgets/ui/local_document_page.dart';

/// Opens bundled documentation without a WebView or network requests.
class HelpCenterPage extends StatelessWidget {
  const HelpCenterPage({super.key, this.title});

  final String? title;

  @override
  Widget build(BuildContext context) {
    final chinese = Localizations.localeOf(context).languageCode == 'zh';
    final assetPath = chinese
        ? 'docs/OFFLINE_HELP.md'
        : 'docs/OFFLINE_HELP_EN.md';
    return LocalDocumentPage(
      title: title ?? AppLocalizations.of(context).mineHelp,
      assetPath: assetPath,
    );
  }
}
