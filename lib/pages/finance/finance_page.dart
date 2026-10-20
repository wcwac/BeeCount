import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/finance.dart';
import '../../providers.dart';
import '../../providers/finance_providers.dart';
import 'finance_detail_page.dart';
import 'finance_form_page.dart';
import 'finance_text.dart';

class FinancePage extends ConsumerStatefulWidget {
  const FinancePage({super.key, this.ledgerId});
  final int? ledgerId;
  @override
  ConsumerState<FinancePage> createState() => _FinancePageState();
}

class _FinancePageState extends ConsumerState<FinancePage> {
  FinanceKind? _filter;
  bool _includeClosed = false;

  @override
  Widget build(BuildContext context) {
    final text = FinanceText(context);
    final int ledgerId =
        widget.ledgerId ?? ref.watch<int>(currentLedgerIdProvider);
    final plans = ref.watch(financePlansProvider(ledgerId));
    return Scaffold(
      appBar: AppBar(title: Text(text.title)),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => Navigator.of(context).push(MaterialPageRoute(
          builder: (_) =>
              FinanceFormPage(ledgerId: ledgerId, initialKind: _filter),
        )),
        icon: const Icon(Icons.add),
        label: Text(text.t('新增记录', 'New record')),
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(financePlansProvider(ledgerId));
          try {
            await ref.read(financePlansProvider(ledgerId).future);
          } catch (_) {
            // The provider's error state displays a retry action below.
          }
        },
        child: CustomScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          slivers: [
            SliverToBoxAdapter(
                child: Padding(
              padding: const EdgeInsets.all(12),
              child: Wrap(spacing: 8, runSpacing: 4, children: [
                ChoiceChip(
                    label: Text(text.t('全部', 'All')),
                    selected: _filter == null,
                    onSelected: (_) => setState(() => _filter = null)),
                for (final kind in FinanceKind.values)
                  ChoiceChip(
                      label: Text(text.kind(kind)),
                      selected: _filter == kind,
                      onSelected: (_) => setState(() => _filter = kind)),
              ]),
            )),
            SliverToBoxAdapter(
                child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Text(text.t('本金往来不计入收支与预算；利息和投资收益计入统计。分期消费在建立时计一次支出。',
                  'Principal movements are excluded from income, spending and budgets. Interest and investment profits are counted. Instalment purchases count as spending when opened.')),
            )),
            SliverToBoxAdapter(
                child: SwitchListTile(
              title: Text(text.t('显示已结清记录', 'Include closed records')),
              value: _includeClosed,
              onChanged: (value) => setState(() => _includeClosed = value),
            )),
            plans.when(
              loading: () => const SliverToBoxAdapter(
                  child: Padding(
                      padding: EdgeInsets.all(24),
                      child: Center(child: CircularProgressIndicator()))),
              error: (error, _) => SliverToBoxAdapter(
                  child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(mainAxisSize: MainAxisSize.min, children: [
                  Text(text.error(error)),
                  TextButton(
                      onPressed: () =>
                          ref.invalidate(financePlansProvider(ledgerId)),
                      child: Text(text.t('重试', 'Retry'))),
                ]),
              )),
              data: (all) {
                final visible = all
                    .where((plan) =>
                        (_filter == null || plan.kind == _filter) &&
                        (_includeClosed || !plan.isClosed))
                    .toList();
                if (visible.isEmpty) {
                  return SliverToBoxAdapter(
                      child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Text(text.t('暂无记录。点击新增开始管理。',
                        'No records yet. Add one to get started.')),
                  ));
                }
                return SliverList(
                    delegate: SliverChildBuilderDelegate(
                  (context, index) {
                    final plan = visible[index];
                    return Card(
                        child: ListTile(
                      title: Text(plan.title),
                      subtitle: Text([
                        text.kind(plan.kind),
                        if (plan.party.isNotEmpty) plan.party,
                        '${text.t('当前余额', 'Current balance')}: ${financeMoney(plan.kind == FinanceKind.investment ? plan.valueCents : plan.outstandingCents, plan.currency)}',
                        if (plan.isClosed) text.t('已结清', 'Closed'),
                      ].join('\n')),
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () => Navigator.of(context).push(MaterialPageRoute(
                        builder: (_) => FinanceDetailPage(planId: plan.id),
                      )),
                    ));
                  },
                  childCount: visible.length,
                ));
              },
            ),
            const SliverToBoxAdapter(child: SizedBox(height: 100)),
          ],
        ),
      ),
    );
  }
}
