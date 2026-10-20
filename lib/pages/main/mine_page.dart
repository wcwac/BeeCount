import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../widgets/biz/biz.dart';
import '../../styles/tokens.dart';
import '../settings/help_center_page.dart';
import '../../l10n/app_localizations.dart';
import '../settings/data_management_page.dart';
import '../settings/appearance_settings_page.dart';
import '../settings/automation_page.dart';
import '../settings/about_page.dart';
import '../../utils/ui_scale_extensions.dart';

class MinePage extends ConsumerWidget {
  const MinePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {

    return Scaffold(
      backgroundColor: BeeTokens.scaffoldBackground(context), // ⭐ 使用 Token
      appBar: AppBar(title: Text(AppLocalizations.of(context).mineTitle)),
      body: Column(
        children: [
          Expanded(
            child: ListView(
              padding: EdgeInsets.zero,
              physics: const AlwaysScrollableScrollPhysics(),
              children: [
                BeeTokens.cardDivider(context, indent: 0),
                SizedBox(height: 8.0.scaled(context, ref)),
                // 功能管理
                SizedBox(height: 8.0.scaled(context, ref)),
                SectionCard(
                  margin: EdgeInsets.fromLTRB(12.0.scaled(context, ref), 0,
                      12.0.scaled(context, ref), 0),
                  child: Column(
                    children: [
                      // 数据管理
                      AppListTile(
                        leading: Icons.storage_outlined,
                        title: AppLocalizations.of(context).dataManagement,
                        subtitle:
                            AppLocalizations.of(context).dataManagementDesc,
                        trailing: Icon(Icons.chevron_right,
                            color: BeeTokens.iconTertiary(context),
                            size: 20), // ⭐ 使用 Token
                        onTap: () async {
                          await Navigator.of(context).push(
                            MaterialPageRoute(
                                builder: (_) => const DataManagementPage()),
                          );
                        },
                      ),
                      BeeTokens.cardDivider(context),
                      // 预算管理 已挪到「账本管理 → 长按某账本 → 预算管理」
                      // (每个账本独立预算,放在账本菜单内语义更匹配)。
                      // 自动化功能
                      AppListTile(
                        leading: Icons.schedule_outlined,
                        title: AppLocalizations.of(context).automation,
                        subtitle: AppLocalizations.of(context).automationDesc,
                        trailing: Icon(Icons.chevron_right,
                            color: BeeTokens.iconTertiary(context),
                            size: 20), // ⭐ 使用 Token
                        onTap: () async {
                          await Navigator.of(context).push(
                            MaterialPageRoute(
                                builder: (_) => const AutomationPage()),
                          );
                        },
                      ),
                      BeeTokens.cardDivider(context),
                      // 外观设置
                      AppListTile(
                        leading: Icons.palette_outlined,
                        title: AppLocalizations.of(context).appearanceSettings,
                        subtitle:
                            AppLocalizations.of(context).appearanceSettingsDesc,
                        trailing: Icon(Icons.chevron_right,
                            color: BeeTokens.iconTertiary(context),
                            size: 20), // ⭐ 使用 Token
                        onTap: () async {
                          await Navigator.of(context).push(
                            MaterialPageRoute(
                                builder: (_) => const AppearanceSettingsPage()),
                          );
                        },
                      ),
                    ],
                  ),
                ),
                // 帮助与信息
                SizedBox(height: 8.0.scaled(context, ref)),
                SectionCard(
                  margin: EdgeInsets.fromLTRB(12.0.scaled(context, ref), 0,
                      12.0.scaled(context, ref), 0),
                  child: Column(
                    children: [
                      AppListTile(
                        leading: Icons.info_outline,
                        title: AppLocalizations.of(context).about,
                        subtitle: AppLocalizations.of(context).aboutDesc,
                        trailing: Icon(Icons.chevron_right,
                            color: BeeTokens.iconTertiary(context), size: 20),
                        onTap: () async {
                          await Navigator.of(context).push(
                            MaterialPageRoute(
                                builder: (_) => const AboutPage()),
                          );
                        },
                      ),
                      BeeTokens.cardDivider(context),
                      // 帮助文档随应用打包，打开页面不联网。
                      AppListTile(
                        leading: Icons.help_outline,
                        title: AppLocalizations.of(context).mineHelp,
                        subtitle: AppLocalizations.of(context).mineHelpSubtitle,
                        onTap: () async {
                          await Navigator.of(context).push(MaterialPageRoute(
                              builder: (_) => const HelpCenterPage()));
                        },
                      ),
                    ],
                  ),
                ),
                // 为记账按钮留出滚动空间
                const SizedBox(height: 88),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
