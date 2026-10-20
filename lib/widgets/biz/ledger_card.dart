import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../models/ledger_display_item.dart';
import '../../providers/theme_providers.dart';
import '../../utils/format_utils.dart';
import '../../utils/currencies.dart';
import '../../l10n/app_localizations.dart';

/// 本地账本摘要。标准列表项保留点击、键盘和辅助功能语义。
class LedgerCard extends ConsumerWidget {
  final LedgerDisplayItem ledger;
  final VoidCallback? onTap;
  final VoidCallback? onLongPress;
  final VoidCallback? onMore;
  final bool selected;

  const LedgerCard({
    super.key,
    required this.ledger,
    this.onTap,
    this.onLongPress,
    this.onMore,
    this.selected = false,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final balance = ref.watch(compactAmountProvider)
        ? formatBalance(ledger.balance, ledger.currency,
            isChineseLocale: Localizations.localeOf(context).languageCode == 'zh')
        : formatBalanceFull(ledger.balance, ledger.currency);
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: ListTile(
        selected: selected,
        leading: Icon(selected ? Icons.check_circle_outline : Icons.book_outlined),
        title: Text(translateLedgerName(context, ledger.name)),
        subtitle: Text(
          '${getCurrencyName(ledger.currency, context)} · ${l10n.ledgersRecords('${ledger.transactionCount}')}\n${l10n.ledgersBalance(balance)}',
        ),
        isThreeLine: true,
        onTap: onTap,
        onLongPress: onLongPress,
        trailing: onMore == null ? null : IconButton(
          tooltip: l10n.ledgersActions,
          onPressed: onMore,
          icon: const Icon(Icons.more_vert),
        ),
      ),
    );
  }
}
