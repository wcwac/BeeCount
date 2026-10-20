import 'package:flutter/material.dart';

import '../../l10n/app_localizations.dart';

/// The app shell keeps tab state while using standard Material navigation.
class MainNavigationScaffold extends StatelessWidget {
  const MainNavigationScaffold({
    super.key,
    required this.pages,
    required this.selectedIndex,
    required this.onDestinationSelected,
    required this.onRecord,
  }) : assert(pages.length == 4);

  final List<Widget> pages;
  final int selectedIndex;
  final ValueChanged<int> onDestinationSelected;
  final VoidCallback onRecord;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Scaffold(
      body: IndexedStack(
        index: selectedIndex,
        children: [
          for (var i = 0; i < pages.length; i++)
            TickerMode(enabled: i == selectedIndex, child: pages[i]),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        tooltip: l10n.tabRecord,
        icon: const Icon(Icons.add),
        label: Text(l10n.tabRecord),
        onPressed: onRecord,
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: selectedIndex,
        onDestinationSelected: onDestinationSelected,
        destinations: [
          NavigationDestination(
            icon: const Icon(Icons.receipt_long_outlined),
            selectedIcon: const Icon(Icons.receipt_long),
            label: l10n.tabHome,
          ),
          NavigationDestination(
            icon: const Icon(Icons.pie_chart_outline_rounded),
            selectedIcon: const Icon(Icons.pie_chart_rounded),
            label: l10n.tabInsights,
          ),
          NavigationDestination(
            icon: const Icon(Icons.account_balance_wallet_outlined),
            selectedIcon: const Icon(Icons.account_balance_wallet),
            label: l10n.tabAssets,
          ),
          NavigationDestination(
            icon: const Icon(Icons.settings_outlined),
            selectedIcon: const Icon(Icons.settings),
            label: l10n.tabMine,
          ),
        ],
      ),
    );
  }
}
