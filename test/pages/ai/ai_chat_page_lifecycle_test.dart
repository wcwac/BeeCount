import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/pages/ai/ai_chat_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  testWidgets('old chat routes show the offline notice', (tester) async {
    SharedPreferences.setMockInitialValues({});
    await tester.pumpWidget(ProviderScope(
      child: MaterialApp(
        locale: const Locale('zh'),
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        supportedLocales: AppLocalizations.supportedLocales,
        home: const AIChatPage(),
      ),
    ));
    await tester.pump();
    expect(find.textContaining('云同步、AI'), findsOneWidget);
  });
}
