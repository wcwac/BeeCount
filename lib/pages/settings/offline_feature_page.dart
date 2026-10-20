import 'package:flutter/material.dart';

import '../../styles/tokens.dart';
import '../../widgets/ui/primary_header.dart';

/// Safe destination for old navigation paths retained by existing installs.
class OfflineFeaturePage extends StatelessWidget {
  const OfflineFeaturePage({super.key});

  @override
  Widget build(BuildContext context) {
    final chinese = Localizations.localeOf(context).languageCode == 'zh';
    return Scaffold(
      backgroundColor: BeeTokens.scaffoldBackground(context),
      body: Column(
        children: [
          PrimaryHeader(
            title: chinese ? '离线版' : 'Offline edition',
            showBack: true,
            compact: true,
          ),
          Expanded(
            child: Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Text(
                  chinese
                      ? '此版本不连接网络。云同步、AI、图片识别和语音记账已禁用。'
                      : 'This edition does not connect to the network. Cloud sync, AI, image recognition and voice entry are disabled.',
                  textAlign: TextAlign.center,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
