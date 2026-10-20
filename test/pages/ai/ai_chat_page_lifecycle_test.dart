import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/pages/ai/ai_chat_page.dart';
import 'package:beecount/providers/ai_chat_providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  testWidgets('old chat routes cannot construct an AI service', (tester) async {
    SharedPreferences.setMockInitialValues({});
    var constructed = false;
    await tester.pumpWidget(ProviderScope(
      overrides: [aiChatServiceProvider.overrideWith((ref) {
        constructed = true;
        throw StateError('AI must not start');
      })],
      child: MaterialApp(
        locale: const Locale('zh'),
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        supportedLocales: AppLocalizations.supportedLocales,
        home: const AIChatPage(),
      ),
    ));
    await tester.pump();
    expect(find.textContaining('云同步、AI'), findsOneWidget);
    expect(constructed, isFalse);
  });
}
