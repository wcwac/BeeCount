/// 本地账本管理：创建、切换、预算、编辑和删除。
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../providers.dart';
import '../../providers/currency_providers.dart';
import '../../models/ledger_display_item.dart';
import '../../widgets/ui/ui.dart';
import '../../widgets/biz/biz.dart';
import '../budget/budget_page.dart';
import '../../styles/tokens.dart';
import '../../utils/currencies.dart';
import '../../services/attachment_service.dart';
import '../../services/system/logger_service.dart';
import '../../utils/format_utils.dart';
import '../../services/billing/post_processor.dart';
import '../../l10n/app_localizations.dart';
import '../../providers/budget_providers.dart';
import '../../widget/widget_manager.dart';

class LedgersPageNew extends ConsumerStatefulWidget {
  final bool autoOpenCreateDialog;
  const LedgersPageNew({super.key, this.autoOpenCreateDialog = false});

  @override
  ConsumerState<LedgersPageNew> createState() => _LedgersPageNewState();
}

class _LedgersPageNewState extends ConsumerState<LedgersPageNew> {
  @override
  void initState() {
    super.initState();
    if (widget.autoOpenCreateDialog) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _showCreateLedgerDialog(context);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final currentId = ref.watch(currentLedgerIdProvider);
    final ledgers = ref.watch(localLedgersProvider);
    final l10n = AppLocalizations.of(context);
    ref.listen<ImportProgress>(importProgressProvider, (previous, next) {
      if (previous?.running == true && next.isJustCompleted && next.ledgerId != null) {
        PostProcessor.refreshLocal(ref, ledgerId: next.ledgerId!);
      }
    });
    return Scaffold(
      body: Column(children: [
        PrimaryHeader(
          title: l10n.ledgersTitle,
          showBack: true,
          actions: [IconButton(
            tooltip: l10n.ledgersCreate,
            onPressed: () => _showCreateLedgerDialog(context),
            icon: const Icon(Icons.add),
          )],
        ),
        Expanded(child: ledgers.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (error, _) => Center(child: Text('${l10n.commonError}: $error')),
          data: (items) => items.isEmpty
              ? Center(child: Column(mainAxisSize: MainAxisSize.min, children: [
                  AppEmpty(text: l10n.ledgersEmpty),
                  OutlinedButton.icon(
                    onPressed: () => _showCreateLedgerDialog(context),
                    icon: const Icon(Icons.add),
                    label: Text(l10n.ledgersNew),
                  ),
                ]))
              : ListView.builder(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  itemCount: items.length,
                  itemBuilder: (context, index) {
                    final ledger = items[index];
                    return LedgerCard(
                      ledger: ledger,
                      selected: ledger.id == currentId,
                      onTap: () => _selectLedger(ledger),
                      onMore: () => _showLocalLedgerActions(context, ledger),
                      onLongPress: () => _showLocalLedgerActions(context, ledger),
                    );
                  },
                ),
        )),
      ]),
    );
  }

  void _selectLedger(LedgerDisplayItem ledger) {
    ref.read(currentLedgerIdProvider.notifier).state = ledger.id;
    ref.invalidate(cachedTransactionsWithCategoryProvider);
    showToast(context, AppLocalizations.of(context).ledgersSwitched(
      translateLedgerName(context, ledger.name),
    ));
  }

  Future<void> _showLocalLedgerActions(BuildContext context, LedgerDisplayItem ledger) async {
    final l10n = AppLocalizations.of(context);
    final action = await showDialog<String>(
      context: context,
      builder: (dialogContext) => SimpleDialog(
        title: Text(l10n.ledgersActions),
        children: [
          for (final option in [
            ('edit', Icons.edit_outlined, l10n.ledgersEdit),
            ('budget', Icons.pie_chart_outline, l10n.budgetManagement),
            ('clear', Icons.clear_all, l10n.ledgersClear),
            ('delete', Icons.delete_outline, l10n.ledgersDelete),
          ]) SimpleDialogOption(
            onPressed: () => Navigator.pop(dialogContext, option.$1),
            child: Row(children: [
              Icon(option.$2),
              const SizedBox(width: 12),
              Flexible(child: Text(option.$3)),
            ]),
          ),
        ],
      ),
    );
    if (!mounted) return;
    switch (action) {
      case 'edit':
        try {
          await _handleEditLedger(context, ledger);
        } catch (error) {
          if (mounted) {
            await AppDialog.error(
              context,
              title: l10n.commonFailed,
              message: '$error',
            );
          }
        }
      case 'budget':
        ref.read(currentLedgerIdProvider.notifier).state = ledger.id;
        await Navigator.of(context).push(MaterialPageRoute(builder: (_) => const BudgetPage()));
      case 'clear':
        await _handleClearLedger(context, ledger);
      case 'delete':
        await _handleDeleteLocalLedger(context, ledger);
    }
  }

  /// 编辑账本
  Future<void> _handleEditLedger(BuildContext context, LedgerDisplayItem ledger) async {
    final repo = ref.read(repositoryProvider);
    final ledgerData = await repo.getLedgerById(ledger.id);

    if (ledgerData == null || !mounted) return;

    final result = await _showLedgerEditorDialog(
      context,
      title: AppLocalizations.of(context).ledgersEdit,
      initialName: ledgerData.name,
      initialCurrency: ledgerData.currency,
      initialMonthStartDay: ledgerData.monthStartDay,
    );

    if (result == null || !mounted) return;

    // v30 本位币变更:存量交易的 nativeAmount 快照是按旧本位币算的,需按新
    // 本位币全量重算(边界 5,.docs/multi-currency-ledger 02 §八)。先确认再改。
    final currencyChanged =
        result.currency.toUpperCase() != ledgerData.currency.toUpperCase();
    if (currencyChanged) {
      final stats = await repo.getLedgerStats(ledgerId: ledger.id);
      final txCount = stats.transactionCount;
      // 用 State 的 mounted/this.context:传入的参数 context 可能随编辑入口
      // 弹层一起销毁(mounted=false → 静默 return 会把整个保存吞掉)。
      if (!mounted) return;
      final l10n = AppLocalizations.of(this.context);
      final confirmed = await showDialog<bool>(
        context: this.context,
        builder: (dctx) => AlertDialog(
          title: Text(l10n.ledgerBaseCurrencyLabel),
          content: Text(
            '${l10n.ledgerCurrencyChangeRecalcHint}\n${l10n.ledgersRecords('$txCount')}',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dctx, false),
              child: Text(AppLocalizations.of(dctx).commonCancel),
            ),
            TextButton(
              onPressed: () => Navigator.pop(dctx, true),
              child: Text(AppLocalizations.of(dctx).commonConfirm),
            ),
          ],
        ),
      );
      if (confirmed != true) return;
    }

    await repo.updateLedger(
      id: ledger.id,
      name: result.name.trim(),
      currency: result.currency,
      monthStartDay: result.monthStartDay,
    );

    // 刷新本地账本、统计和预算。
    ref.read(ledgerListRefreshProvider.notifier).state++;
    // currentLedgerProvider 已是 StreamProvider(Drift watch 自动推送),
    // 此 invalidate 仅作防御性重订阅(如流曾进入 error 态),正常路径冗余无害。
    ref.invalidate(currentLedgerProvider);
    ref.read(statsRefreshProvider.notifier).state++;
    ref.read(budgetRefreshProvider.notifier).state++;

    await PostProcessor.refreshLocal(ref, ledgerId: ledger.id);

    // 起始日影响小部件「本月」口径,立即刷新
    try {
      final repository = ref.read(repositoryProvider);
      final redForIncome = ref.read(incomeExpenseColorSchemeProvider);
      // 没有 BuildContext,靠 languageProvider 还原当前 App 语言(见
      // widget_manager.dart resolveWidgetLocalizations 文档)。
      await WidgetManager().updateAllWidgetsLocalized(
        repository,
        ledger.id,
        ref.read(primaryColorProvider),
        explicitLocale: ref.read(languageProvider),
        redForIncome: redForIncome,
        baseCurrency: ref.read(baseCurrencyProvider),
      );
    } catch (_) {}
  }

  /// 清空 / 删除账本后,精准清理该账本关联的附件物理文件(best-effort)。
  /// 清空(clearLedgerTransactions)和删账本(deleteLedger)走批量 SQL 删行,
  /// 只删 DB 行不删物理文件;调用方在删除前先收集该账本的 fileName 传入。
  /// 按引用计数删除:其他账本/交易仍引用同一 fileName 的不会被误删。
  Future<void> _cleanupLedgerAttachmentFiles(List<String> fileNames) async {
    if (fileNames.isEmpty) return;
    try {
      await ref
          .read(attachmentServiceProvider)
          .deletePhysicalFilesIfUnreferenced(fileNames);
    } catch (e) {
      logger.warning('ledger', '清理账本附件文件失败（忽略）：$e');
    }
  }

  /// 清空账本（删除所有账单，保留账本）
  Future<void> _handleClearLedger(BuildContext context, LedgerDisplayItem ledger) async {
    final l10n = AppLocalizations.of(context);
    final confirmed = await AppDialog.confirm<bool>(
      context,
      title: l10n.ledgersClearTitle,
      message: l10n.ledgersClearMessage(translateLedgerName(context, ledger.name)),
    );

    if (confirmed != true || !mounted) return;

    try {
      final repo = ref.read(repositoryProvider);

      // 删账单前先收集该账本附件 fileName(删行后就查不到了)
      final attachmentFiles =
          await repo.getAttachmentFileNamesByLedger(ledger.id);
      // 删除该账本的所有账单(批量删行不删物理文件)
      await repo.clearLedgerTransactions(ledger.id);
      // 删行后精准清理这些附件的物理文件(引用计数)
      await _cleanupLedgerAttachmentFiles(attachmentFiles);

      if (!mounted) return;

      // 清空缓存的交易数据（避免首页使用旧缓存）
      ref.read(cachedTransactionsProvider.notifier).state = null;

      // 触发同步状态刷新
      await PostProcessor.refreshLocal(ref, ledgerId: ledger.id);

      ref.read(ledgerListRefreshProvider.notifier).state++;
      ref.read(statsRefreshProvider.notifier).state++;

      showToast(context, l10n.ledgersClearSuccess);
    } catch (e) {
      if (!mounted) return;
      await AppDialog.error(
        context,
        title: l10n.commonFailed,
        message: '$e',
      );
    }
  }

  /// 删除本地账本
  Future<void> _handleDeleteLocalLedger(BuildContext context, LedgerDisplayItem ledger) async {
    final l10n = AppLocalizations.of(context);

    final repo = ref.read(repositoryProvider);
    final allLedgers = await repo.getAllLedgers();

    final confirmed = await AppDialog.confirm<bool>(
      context,
      title: l10n.ledgersDeleteConfirm,
      message: l10n.ledgersDeleteMessage,
    );

    if (confirmed != true || !mounted) return;

    try {
      final current = ref.read(currentLedgerIdProvider);
      final deletedLedgerId = ledger.id;

      // 如果删除的是当前账本,有其他账本就切一下;没有就让 currentLedger 落空
      // (首页胶囊会回到「+ 新建账本」引导,允许删完所有账本)
      if (current == deletedLedgerId) {
        final remainAfterDelete =
            allLedgers.where((l) => l.id != deletedLedgerId).toList();
        if (remainAfterDelete.isNotEmpty) {
          ref.read(currentLedgerIdProvider.notifier).state =
              remainAfterDelete.first.id;
        }
      }

      // 删除账本及其不再被引用的附件。
      final attachmentFiles =
          await repo.getAttachmentFileNamesByLedger(deletedLedgerId);
      await repo.deleteLedger(deletedLedgerId);
      await _cleanupLedgerAttachmentFiles(attachmentFiles);

      await PostProcessor.refreshLocal(ref, ledgerId: deletedLedgerId);

      if (!mounted) return;

      ref.invalidate(currentLedgerProvider);
      ref.read(ledgerListRefreshProvider.notifier).state++;
      ref.read(statsRefreshProvider.notifier).state++;

      showToast(context, AppLocalizations.of(context).ledgersDeleted);
    } catch (e) {
      if (!mounted) return;
      await AppDialog.error(
        context,
        title: AppLocalizations.of(context).ledgersDeleteFailed,
        message: '$e',
      );
    }
  }

  /// 显示创建账本对话框
  Future<void> _showCreateLedgerDialog(BuildContext context) async {
    final result = await _showLedgerEditorDialog(
      context,
      title: AppLocalizations.of(context).ledgersNew,
    );

    if (result == null || !mounted) return;

    try {
      final repo = ref.read(repositoryProvider);
      final newLedgerId = await repo.createLedger(
        name: result.name.trim(),
        currency: result.currency,
      );

      // 创建弹窗里也能选起始日:createLedger 不收该参数,创建后补写
      if (result.monthStartDay != 1) {
        await repo.updateLedger(
            id: newLedgerId, monthStartDay: result.monthStartDay);
      }

      // 空账本场景(welcome 未勾默认账本 / 老用户导入配置不含账本)进入此页
      // 创建第一个账本时,currentLedgerIdProvider 还指向默认值 1(无效),
      // 必须切到新账本 id 否则首页 header 胶囊继续显示「新建账本」、列表为
      // 空。已有账本时不动 currentLedger,保留用户当前所在账本。
      if (!mounted) return;
      final activeLedgerId = ref.read(currentLedgerIdProvider);
      final currentLedger = await repo.getLedgerById(activeLedgerId);
      if (!mounted) return;
      if (currentLedger == null &&
          ref.read(currentLedgerIdProvider) == activeLedgerId) {
        ref.read(currentLedgerIdProvider.notifier).state = newLedgerId;
        ref.invalidate(currentLedgerProvider);
      }

      ref.read(ledgerListRefreshProvider.notifier).state++;

      PostProcessor.refreshLocal(ref, ledgerId: newLedgerId);

      if (!mounted) return;
      showToast(context, '账本创建成功');
    } catch (e) {
      if (!mounted) return;
      showToast(context, '创建失败: ${e.toString()}');
    }
  }

  Future<({String name, String currency, int monthStartDay})?> _showLedgerEditorDialog(
    BuildContext context, {
    String? title,
    String? initialName,
    String? initialCurrency,
    int? initialMonthStartDay,
  }) => showDialog<({String name, String currency, int monthStartDay})>(
        context: context,
        builder: (_) => _LedgerEditorDialog(
          title: title ?? AppLocalizations.of(context).ledgersEdit,
          initialName: initialName ?? '',
          initialCurrency: initialCurrency ?? 'CNY',
          initialMonthStartDay: initialMonthStartDay ?? 1,
          pickCurrency: (ctx, current) => _showCurrencyPicker(ctx, initial: current),
          pickMonthStartDay: (ctx, current) =>
              _showMonthStartDayPicker(ctx, initial: current),
        ),
      );

  /// 28宫格月起始日选择器
  Future<int?> _showMonthStartDayPicker(BuildContext context,
      {required int initial}) {
    return showModalBottomSheet<int>(
      context: context,
      backgroundColor: BeeTokens.surfaceElevated(context),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (ctx) {
        final primary = Theme.of(ctx).colorScheme.primary;
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(AppLocalizations.of(ctx).ledgersMonthStartDay,
                    style: Theme.of(ctx).textTheme.titleMedium),
                const SizedBox(height: 4),
                Text(AppLocalizations.of(ctx).ledgersMonthStartDayHint,
                    style: Theme.of(ctx)
                        .textTheme
                        .bodySmall
                        ?.copyWith(color: BeeTokens.textTertiary(ctx))),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: List.generate(28, (index) {
                    final day = index + 1;
                    final isSelected = initial == day;
                    return InkWell(
                      onTap: () => Navigator.pop(ctx, day),
                      borderRadius: BorderRadius.circular(8),
                      child: Container(
                        width: 40,
                        height: 40,
                        alignment: Alignment.center,
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(8),
                          color: isSelected
                              ? primary.withValues(alpha: 0.12)
                              : Colors.transparent,
                          border: Border.all(
                              color:
                                  isSelected ? primary : BeeTokens.divider(ctx)),
                        ),
                        child: Text('$day',
                            style: TextStyle(
                                color: isSelected
                                    ? primary
                                    : BeeTokens.textPrimary(ctx))),
                      ),
                    );
                  }),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  /// 货币选择器
  Future<String?> _showCurrencyPicker(BuildContext context, {String? initial}) async {
    return showModalBottomSheet<String>(
      context: context,
      isScrollControlled: true,
      backgroundColor: BeeTokens.surfaceElevated(context),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (bctx) {
        String query = '';
        String? selected = initial;
        return StatefulBuilder(builder: (sctx, setState) {
          final filtered = getCurrencies(context).where((c) {
            final q = query.trim();
            if (q.isEmpty) return true;
            final uq = q.toUpperCase();
            return c.code.contains(uq) || c.name.contains(q);
          }).toList();

          return Padding(
            padding: EdgeInsets.only(
              left: 16,
              right: 16,
              top: 12,
              bottom: 16 + MediaQuery.of(bctx).viewInsets.bottom,
            ),
            child: SizedBox(
              height: 420,
              child: Column(
                children: [
                  Container(
                    width: 36,
                    height: 4,
                    margin: const EdgeInsets.only(bottom: 8),
                    decoration: BoxDecoration(
                      color: BeeTokens.textTertiary(context).withValues(alpha: 0.3),
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ),
                  Text(
                    AppLocalizations.of(bctx).ledgersSelectCurrency,
                    style: Theme.of(bctx).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 8),
                  TextField(
                    decoration: InputDecoration(
                      prefixIcon: const Icon(Icons.search),
                      hintText: AppLocalizations.of(bctx).ledgersSearchCurrency,
                    ),
                    onChanged: (v) => setState(() => query = v),
                  ),
                  const SizedBox(height: 8),
                  Expanded(
                    child: ListView.builder(
                      itemCount: filtered.length,
                      itemBuilder: (_, i) {
                        final c = filtered[i];
                        final sel = c.code == selected;
                        return ListTile(
                          title: Text('${c.name} (${c.code})'),
                          trailing: sel
                              ? Icon(Icons.check, color: BeeTokens.textPrimary(context))
                              : null,
                          onTap: () => Navigator.pop(bctx, c.code),
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),
          );
        });
      },
    );
  }

}

/// Own the text controller for the entire dialog route, including its exit animation.
class _LedgerEditorDialog extends StatefulWidget {
  const _LedgerEditorDialog({
    required this.title,
    required this.initialName,
    required this.initialCurrency,
    required this.initialMonthStartDay,
    required this.pickCurrency,
    required this.pickMonthStartDay,
  });

  final String title;
  final String initialName;
  final String initialCurrency;
  final int initialMonthStartDay;
  final Future<String?> Function(BuildContext, String) pickCurrency;
  final Future<int?> Function(BuildContext, int) pickMonthStartDay;

  @override
  State<_LedgerEditorDialog> createState() => _LedgerEditorDialogState();
}

class _LedgerEditorDialogState extends State<_LedgerEditorDialog> {
  late final TextEditingController _name =
      TextEditingController(text: widget.initialName);
  late String _currency = widget.initialCurrency;
  late int _monthStartDay = widget.initialMonthStartDay;

  @override
  void dispose() {
    _name.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return AlertDialog(
      scrollable: true,
      title: Text(widget.title),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            controller: _name,
            decoration: InputDecoration(labelText: l10n.ledgersName),
          ),
          const SizedBox(height: 12),
          ListTile(
            contentPadding: EdgeInsets.zero,
            title: Text(l10n.ledgerBaseCurrencyLabel),
            subtitle: Text(displayCurrency(_currency, context)),
            trailing: const Icon(Icons.chevron_right),
            onTap: () async {
              final picked = await widget.pickCurrency(context, _currency);
              if (mounted && picked != null) setState(() => _currency = picked);
            },
          ),
          ListTile(
            contentPadding: EdgeInsets.zero,
            title: Text(l10n.ledgersMonthStartDay),
            subtitle: Text(_monthStartDay <= 1
                ? l10n.ledgersMonthStartDayNatural
                : l10n.ledgersMonthStartDayValue(_monthStartDay)),
            trailing: const Icon(Icons.chevron_right),
            onTap: () async {
              final picked = await widget.pickMonthStartDay(context, _monthStartDay);
              if (mounted && picked != null) setState(() => _monthStartDay = picked);
            },
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: Text(l10n.commonCancel),
        ),
        ValueListenableBuilder<TextEditingValue>(
          valueListenable: _name,
          builder: (context, value, _) => FilledButton(
            onPressed: value.text.trim().isEmpty ? null : () => Navigator.of(context).pop((
              name: value.text.trim(),
              currency: _currency,
              monthStartDay: _monthStartDay,
            )),
            child: Text(l10n.commonSave),
          ),
        ),
      ],
    );
  }
}
