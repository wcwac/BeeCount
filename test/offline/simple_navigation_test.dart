import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/widgets/ui/main_navigation_scaffold.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('tabs preserve state and record has its own accessible action',
      (tester) async {
    var recordCount = 0;
    await tester.pumpWidget(_app(onRecord: () => recordCount++));
    await tester.pumpAndSettle();

    expect(find.byType(NavigationDestination), findsNWidgets(4));
    expect(find.text('Page 0: 0'), findsOneWidget);
    await tester.tap(find.text('Page 0: 0'));
    await tester.pump();
    expect(find.text('Page 0: 1'), findsOneWidget);

    await tester.tap(find.text('Stats'));
    await tester.pumpAndSettle();
    expect(find.text('Page 1: 0'), findsOneWidget);
    expect(find.text('Page 0: 1'), findsNothing);

    await tester.tap(find.text('Home'));
    await tester.pumpAndSettle();
    expect(find.text('Page 0: 1'), findsOneWidget);
    expect(find.byTooltip('Record'), findsOneWidget);
    await tester.tap(find.byType(FloatingActionButton));
    await tester.pump();
    expect(recordCount, 1);
    expect(find.byIcon(Icons.mic_rounded), findsNothing);
    expect(find.byIcon(Icons.cloud_outlined), findsNothing);
  });

  testWidgets('large text fits and hidden tabs disable their tickers',
      (tester) async {
    await tester.pumpWidget(_app(onRecord: () {}, textScale: 2));
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
    final labelContext = tester.element(find.text('Record'));
    expect(MediaQuery.textScalerOf(labelContext).scale(10), 20);

    for (var i = 0; i < 4; i++) {
      final pageContext = tester.element(find.byKey(
        ValueKey('page-$i'),
        skipOffstage: false,
      ));
      expect(TickerMode.of(pageContext), i == 0);
    }
    await tester.tap(find.text('Assets'));
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
    expect(find.text('Page 2: 0'), findsOneWidget);
  });
}

Widget _app({required VoidCallback onRecord, double textScale = 1}) {
  return MaterialApp(
    theme: ThemeData(useMaterial3: true),
    locale: const Locale('en'),
    localizationsDelegates: AppLocalizations.localizationsDelegates,
    supportedLocales: AppLocalizations.supportedLocales,
    builder: (context, child) => MediaQuery(
      data: MediaQuery.of(context).copyWith(
        textScaler: TextScaler.linear(textScale),
      ),
      child: child!,
    ),
    home: _NavigationHarness(onRecord: onRecord),
  );
}

class _NavigationHarness extends StatefulWidget {
  const _NavigationHarness({required this.onRecord});
  final VoidCallback onRecord;

  @override
  State<_NavigationHarness> createState() => _NavigationHarnessState();
}

class _NavigationHarnessState extends State<_NavigationHarness> {
  int _selected = 0;

  @override
  Widget build(BuildContext context) => MainNavigationScaffold(
        pages: [
          for (var i = 0; i < 4; i++)
            _CounterPage(key: ValueKey('page-$i'), index: i),
        ],
        selectedIndex: _selected,
        onDestinationSelected: (index) => setState(() => _selected = index),
        onRecord: widget.onRecord,
      );
}

class _CounterPage extends StatefulWidget {
  const _CounterPage({super.key, required this.index});
  final int index;

  @override
  State<_CounterPage> createState() => _CounterPageState();
}

class _CounterPageState extends State<_CounterPage> {
  int _count = 0;

  @override
  Widget build(BuildContext context) => Center(
        child: TextButton(
          onPressed: () => setState(() => _count++),
          child: Text('Page ${widget.index}: $_count'),
        ),
      );
}
