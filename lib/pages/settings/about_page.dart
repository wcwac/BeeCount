import 'package:flutter/material.dart';
import 'package:package_info_plus/package_info_plus.dart';

import '../../l10n/app_localizations.dart';
import '../../styles/tokens.dart';
import '../../widgets/ui/local_document_page.dart';
import '../../widgets/ui/primary_header.dart';
import 'log_center_page.dart';
import 'privacy_policy_page.dart';

class AboutPage extends StatefulWidget {
  const AboutPage({super.key});
  @override
  State<AboutPage> createState() => _AboutPageState();
}

class _AboutPageState extends State<AboutPage> {
  String _version = '';

  @override
  void initState() {
    super.initState();
    _loadVersion();
  }

  Future<void> _loadVersion() async {
    try {
      final info = await PackageInfo.fromPlatform();
      if (mounted) setState(() => _version = '${info.version} (${info.buildNumber})');
    } catch (_) {}
  }

  void _open(Widget page) {
    Navigator.of(context).push(MaterialPageRoute(builder: (_) => page));
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final chinese = Localizations.localeOf(context).languageCode == 'zh';
    return Scaffold(
      backgroundColor: BeeTokens.scaffoldBackground(context),
      body: Column(children: [
        PrimaryHeader(title: l10n.aboutPageTitle, showBack: true, compact: true),
        Expanded(child: ListView(padding: const EdgeInsets.all(16), children: [
          Text(l10n.appName, style: Theme.of(context).textTheme.headlineSmall),
          Text(_version),
          const SizedBox(height: 16),
          Text(chinese
              ? '离线版：仅使用本地数据，云同步、AI、在线更新与外部链接已禁用。请通过本地导出保存备份。'
              : 'Offline edition: local data only. Cloud sync, AI, online updates and external links are disabled. Keep backups using local export.'),
          const SizedBox(height: 16),
          const SelectableText('BeeCount © 2024–2026 sunxiao (TNT-Likely)\nhttps://github.com/TNT-Likely/BeeCount'),
          ListTile(
            title: Text(chinese ? '软件许可' : 'Software license'),
            onTap: () => _open(LocalDocumentPage(
              title: chinese ? '软件许可' : 'Software license',
              assetPath: chinese ? 'LICENSE' : 'LICENSE_EN',
            )),
          ),
          ListTile(
            title: Text(chinese ? '第三方声明' : 'Third-party notices'),
            onTap: () => _open(LocalDocumentPage(
              title: chinese ? '第三方声明' : 'Third-party notices',
              assetPath: 'THIRD-PARTY-NOTICES.md',
            )),
          ),
          ListTile(title: Text(l10n.aboutPrivacyPolicy),
              onTap: () => _open(const PrivacyPolicyPage())),
          ListTile(title: Text(l10n.logCenterTitle),
              onTap: () => _open(const LogCenterPage())),
        ])),
      ]),
    );
  }
}
