import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/finance.dart';
import '../../providers/finance_providers.dart';
import '../../services/billing/post_processor.dart';
import 'finance_posting_page.dart';
import 'finance_text.dart';

class FinanceDetailPage extends ConsumerStatefulWidget {
  const FinanceDetailPage({super.key, required this.planId});
  final int planId;
  @override
  ConsumerState<FinanceDetailPage> createState() => _FinanceDetailPageState();
}

class _FinanceDetailPageState extends ConsumerState<FinanceDetailPage> {
  bool _busy = false;
  Object? _error;

  Future<void> _correct(FinancePlan plan, {required bool delete}) async {
    if (_busy) return;
    final text = FinanceText(context);
    final confirmed = await showDialog<bool>(context: context, builder: (context) => AlertDialog(
      title: Text(delete ? text.t('删除整条记录？', 'Delete this record?') : text.t('撤销最后一笔？', 'Undo the latest event?')),
      content: Text(delete
        ? text.t('此记录及其生成的资金流水将一并删除。关联的原支出将恢复。此操作不可撤销。', 'This record and its generated cash transactions will be deleted. A linked original expense will be restored. This cannot be undone.')
        : text.t('最后一笔操作及其生成的资金流水将撤销。', 'The latest event and its generated cash transactions will be reversed.')),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context, false), child: Text(text.t('取消', 'Cancel'))),
        FilledButton(onPressed: () => Navigator.pop(context, true), child: Text(text.t('确认', 'Confirm'))),
      ],
    ));
    if (!mounted || confirmed != true || _busy) return;
    setState(() { _busy = true; _error = null; });
    try {
      final repository = ref.read(financeRepositoryProvider);
      if (delete) { await repository.delete(plan.id); }
      else { await repository.undoLastEvent(plan.id); }
      if (!mounted) return;
      await PostProcessor.run(ref, ledgerId: plan.ledgerId);
      if (mounted && delete) Navigator.of(context).pop();
    } catch (error) {
      if (mounted) setState(() => _error = error);
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _post(FinancePlan plan, FinanceAction action, {RepaymentRow? period, bool full = false}) async {
    if (_busy) return;
    await Navigator.of(context).push(MaterialPageRoute(builder: (_) => FinancePostingPage(
      plan: plan, action: action, period: period, settleInFull: full,
    )));
  }

  @override
  Widget build(BuildContext context) {
    final text = FinanceText(context);
    final planState = ref.watch(financePlanProvider(widget.planId));
    return PopScope(canPop: !_busy, child: Scaffold(
      appBar: AppBar(title: Text(text.t('财务记录', 'Finance record'))),
      body: planState.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => _loadError(error, () => ref.invalidate(financePlanProvider(widget.planId))),
        data: (plan) {
          if (plan == null) return Center(child: Text(text.t('此记录已不存在。', 'This record no longer exists.')));
          final eventsState = ref.watch(financeEventsProvider(plan.id));
          return AbsorbPointer(absorbing: _busy, child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Text(plan.title, style: Theme.of(context).textTheme.headlineSmall),
              const SizedBox(height: 8),
              Text('${text.kind(plan.kind)} · ${financeDate(plan.openedAt)}'),
              if (plan.party.isNotEmpty) Text(plan.party),
              if (plan.note.isNotEmpty) Padding(padding: const EdgeInsets.only(top: 8), child: Text(plan.note)),
              if (plan.isClosed) Padding(padding: const EdgeInsets.only(top: 8), child: Text(text.t('已结清', 'Closed'))),
              const SizedBox(height: 16),
              if (plan.kind == FinanceKind.investment) ...[
                _metric(text.t('持有份额', 'Units held'), financeDecimal(plan.unitsMicros, 6)),
                _metric(text.t('剩余成本', 'Remaining cost'), financeMoney(plan.costCents, plan.currency)),
                _metric(text.t('当前市值', 'Current value'), financeMoney(plan.valueCents, plan.currency)),
                _metric(text.t('已实现收益', 'Realized profit'), financeMoney(plan.realizedProfitCents, plan.currency)),
                _metric(text.t('浮动盈亏', 'Unrealized profit / loss'), financeMoney(plan.valueCents - plan.costCents, plan.currency)),
              ] else ...[
                _metric(text.t('初始本金', 'Original principal'), financeMoney(plan.principalCents, plan.currency)),
                _metric(text.t('未结本金', 'Outstanding principal'), financeMoney(plan.outstandingCents, plan.currency)),
                if (plan.kind != FinanceKind.reimbursement)
                  _metric(plan.kind == FinanceKind.lend ? text.t('累计收取利息', 'Interest received') : text.t('累计支付利息', 'Interest paid'), financeMoney(plan.paidInterestCents, plan.currency)),
              ],
              if (_busy) const LinearProgressIndicator(),
              if (_error != null) Padding(padding: const EdgeInsets.symmetric(vertical: 12),
                child: Text(text.error(_error!), style: TextStyle(color: Theme.of(context).colorScheme.error))),
              const SizedBox(height: 16),
              if (!plan.isClosed || plan.kind == FinanceKind.investment) Wrap(spacing: 8, runSpacing: 8, children: [
                if (plan.kind == FinanceKind.investment)
                  for (final action in [FinanceAction.buy, if (!plan.isClosed) ...[FinanceAction.sell, FinanceAction.dividend, FinanceAction.valuation]])
                    OutlinedButton(onPressed: () => _post(plan, action), child: Text(text.action(plan, action)))
                else if (plan.kind.isScheduled)
                  OutlinedButton(onPressed: () => _post(plan, FinanceAction.settle, full: true),
                    child: Text(text.t('提前结清', 'Settle early')))
                else ...[
                  FilledButton(onPressed: () => _post(plan, FinanceAction.settle),
                    child: Text(text.action(plan, FinanceAction.settle))),
                  OutlinedButton(onPressed: () => _post(plan, FinanceAction.settle, full: true),
                    child: Text(text.t('全部结清', 'Settle in full'))),
                ],
              ]),
              const SizedBox(height: 16),
              eventsState.when(
                loading: () => const Center(child: CircularProgressIndicator()),
                error: (error, _) => _loadError(error, () => ref.invalidate(financeEventsProvider(plan.id))),
                data: (events) => Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                  if (plan.kind.isScheduled) _schedule(plan, events),
                  const SizedBox(height: 12),
                  Text(text.t('操作记录', 'History'), style: Theme.of(context).textTheme.titleMedium),
                  if (events.isEmpty) Text(text.t('暂无操作', 'No events')),
                  for (final event in events.reversed)
                    Card(child: ListTile(
                      title: Text('${text.event(event.action)} · ${financeDate(event.happenedAt)}'),
                      subtitle: Text([
                        if (event.periodNumber != null) text.t('第 ${event.periodNumber} 期', 'Period ${event.periodNumber}'),
                        if (event.cashCents != 0) '${text.t('资金变动', 'Cash movement')}: ${financeMoney(event.cashCents, plan.currency)}',
                        if (event.principalDeltaCents != 0) '${text.t('本金变动', 'Principal movement')}: ${financeMoney(event.principalDeltaCents, plan.currency)}',
                        if (event.interestCents != 0) '${text.t('利息', 'Interest')}: ${financeMoney(event.interestCents, plan.currency)}',
                        if (event.unitsDeltaMicros != 0) '${text.t('份额变动', 'Units movement')}: ${financeDecimal(event.unitsDeltaMicros, 6)}',
                        if (event.profitCents != 0) '${text.t('收益', 'Profit')}: ${financeMoney(event.profitCents, plan.currency)}',
                        if (event.action == 'valuation') '${text.t('估值', 'Valuation')}: ${financeMoney(event.valueAfterCents, plan.currency)}',
                        if (event.note.isNotEmpty) event.note,
                      ].join('\n')),
                    )),
                  if (events.length > 1) OutlinedButton.icon(
                    onPressed: () => _correct(plan, delete: false),
                    icon: const Icon(Icons.undo), label: Text(text.t('撤销最后一笔', 'Undo latest event'))),
                ]),
              ),
              const SizedBox(height: 24),
              TextButton.icon(onPressed: () => _correct(plan, delete: true),
                icon: const Icon(Icons.delete_outline), label: Text(text.t('删除整条记录', 'Delete record'))),
              const SizedBox(height: 24),
            ],
          ));
        },
      ),
    ));
  }
  Widget _loadError(Object error, VoidCallback retry) {
    final text = FinanceText(context);
    return Padding(padding: const EdgeInsets.all(16), child: Column(mainAxisSize: MainAxisSize.min, children: [
      Text(text.error(error)), TextButton(onPressed: retry, child: Text(text.t('重试', 'Retry'))),
    ]));
  }
  Widget _metric(String label, String value) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 4),
    child: Wrap(spacing: 12, runSpacing: 4, alignment: WrapAlignment.spaceBetween,
      children: [Text(label), Text(value, style: Theme.of(context).textTheme.titleMedium)]),
  );
  Widget _schedule(FinancePlan plan, List<FinanceEvent> events) {
    final text = FinanceText(context);
    List<RepaymentRow> rows;
    try {
      rows = repaymentSchedule(principalCents: plan.principalCents,
        annualRateBps: plan.annualRateBps, periods: plan.periods,
        firstDueAt: plan.firstDueAt, method: plan.method);
    } catch (error) { return Text(text.error(error)); }
    final paid = events.where((event) => event.action == 'settle' && event.periodNumber != null)
        .map((event) => event.periodNumber!).toSet();
    final remaining = rows.where((row) => !paid.contains(row.number));
    final next = remaining.isEmpty ? null : remaining.first.number;
    Widget rowTile(RepaymentRow row) => ListTile(
      contentPadding: EdgeInsets.zero,
      title: Text('${text.t('第 ${row.number} 期', 'Period ${row.number}')} · ${financeDate(row.dueAt)}'),
      subtitle: Text([
        '${text.t('本金', 'Principal')}: ${financeMoney(row.principalCents, plan.currency)}',
        '${text.t('利息', 'Interest')}: ${financeMoney(row.interestCents, plan.currency)}',
        '${text.t('合计', 'Total')}: ${financeMoney(row.totalCents, plan.currency)}',
        '${text.t('剩余本金', 'Remaining principal')}: ${financeMoney(row.balanceCents, plan.currency)}',
        if (paid.contains(row.number)) text.t('已还款', 'Paid'),
      ].join('\n')),
    );
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      Text(text.t('还款计划', 'Repayment schedule'), style: Theme.of(context).textTheme.titleMedium),
      Text('${text.method(plan.method)} · ${financeDecimal(plan.annualRateBps, 2)}% · ${plan.periods} ${text.t('期', 'months')}'),
      if (!plan.isClosed && next != null) ...[
        rowTile(rows.firstWhere((row) => row.number == next)),
        FilledButton.icon(
          onPressed: () => _post(plan, FinanceAction.settle,
            period: rows.firstWhere((row) => row.number == next)),
          icon: const Icon(Icons.payment),
          label: Text(text.t('记录第 $next 期还款', 'Record payment $next')),
        ),
      ],
      OutlinedButton.icon(
        icon: const Icon(Icons.table_rows_outlined),
        label: Text(text.t('查看全部还款计划', 'View full schedule')),
        onPressed: () => Navigator.of(context).push(MaterialPageRoute(
          builder: (context) => Scaffold(
            appBar: AppBar(title: Text(text.t('还款计划', 'Repayment schedule'))),
            body: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: rows.length,
              itemBuilder: (context, index) => rowTile(rows[index]),
            ),
          ),
        )),
      ),
    ]);
  }
}
