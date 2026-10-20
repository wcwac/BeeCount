import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/db.dart' as db;
import '../../models/finance.dart';
import '../../providers/finance_providers.dart';
import '../../services/billing/post_processor.dart';
import 'finance_text.dart';

class FinancePostingPage extends ConsumerStatefulWidget {
  const FinancePostingPage({super.key, required this.plan, required this.action,
    this.period, this.settleInFull = false});
  final FinancePlan plan;
  final FinanceAction action;
  final RepaymentRow? period;
  final bool settleInFull;
  @override
  ConsumerState<FinancePostingPage> createState() => _FinancePostingPageState();
}

class _FinancePostingPageState extends ConsumerState<FinancePostingPage> {
  final _form = GlobalKey<FormState>();
  late final TextEditingController _principal = TextEditingController(
    text: financeDecimal(widget.period?.principalCents ?? widget.plan.outstandingCents, 2));
  late final TextEditingController _interest = TextEditingController(
    text: financeDecimal(widget.period?.interestCents ?? 0, 2));
  final _amount = TextEditingController();
  final _units = TextEditingController();
  final _note = TextEditingController();
  DateTime _date = DateTime.now();
  List<db.Account> _accounts = [];
  int? _accountId;
  bool _loading = true;
  bool _saving = false;
  Object? _loadError;
  Object? _saveError;
  bool get _settle => widget.action == FinanceAction.settle;
  bool get _valuation => widget.action == FinanceAction.valuation;
  bool get _hasUnits => widget.action == FinanceAction.buy || widget.action == FinanceAction.sell;

  @override
  void initState() { super.initState(); _load(); }
  @override
  void dispose() {
    for (final controller in [_principal, _interest, _amount, _units, _note]) { controller.dispose(); }
    super.dispose();
  }
  Future<void> _load() async {
    setState(() { _loading = true; _loadError = null; });
    try {
      final accounts = _valuation ? <db.Account>[]
          : await ref.read(financeRepositoryProvider).cashAccounts(widget.plan.ledgerId, currency: widget.plan.currency);
      if (!mounted) return;
      setState(() {
        _accounts = accounts;
        _accountId = accounts.length == 1 ? accounts.single.id : null;
        _loading = false;
      });
    } catch (error) {
      if (mounted) setState(() { _loading = false; _loadError = error; });
    }
  }
  String? _number(String? raw, int places, FinanceText text, {bool zeroAllowed = false}) {
    final number = parseFinanceDecimal(raw ?? '', places);
    return number == null || (zeroAllowed ? number < 0 : number <= 0)
      ? text.t('请输入有效数值', 'Enter a valid number') : null;
  }
  Future<void> _save() async {
    if (_saving || !(_form.currentState?.validate() ?? false)) return;
    setState(() { _saving = true; _saveError = null; });
    try {
      await ref.read(financeRepositoryProvider).post(widget.plan.id, FinancePosting(
        action: widget.action,
        happenedAt: _date,
        cashAccountId: _valuation ? null : _accountId,
        periodNumber: widget.period?.number,
        principalCents: _settle ? parseFinanceDecimal(_principal.text, 2)! : 0,
        interestCents: _settle && widget.plan.kind != FinanceKind.reimbursement ? parseFinanceDecimal(_interest.text, 2)! : 0,
        amountCents: _settle ? 0 : parseFinanceDecimal(_amount.text, 2)!,
        unitsMicros: _hasUnits ? parseFinanceDecimal(_units.text, 6)! : 0,
        note: _note.text.trim(),
      ));
      if (!mounted) return;
      await PostProcessor.run(ref, ledgerId: widget.plan.ledgerId);
      if (mounted) Navigator.of(context).pop(true);
    } catch (error) {
      if (mounted) setState(() => _saveError = error);
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }
  @override
  Widget build(BuildContext context) {
    final text = FinanceText(context);
    final title = widget.period != null ? text.t('第 ${widget.period!.number} 期还款', 'Payment ${widget.period!.number}')
      : widget.settleInFull ? (widget.plan.kind.isScheduled ? text.t('提前结清', 'Settle early') : text.t('全部结清', 'Settle in full')) : text.action(widget.plan, widget.action);
    return PopScope(canPop: !_saving, child: Scaffold(
      appBar: AppBar(title: Text(title)),
      body: _loading ? const Center(child: CircularProgressIndicator())
        : _loadError != null ? Center(child: Column(mainAxisSize: MainAxisSize.min, children: [
            Text(text.error(_loadError!)),
            TextButton(onPressed: _load, child: Text(text.t('重试', 'Retry'))),
          ]))
        : AbsorbPointer(absorbing: _saving, child: Form(key: _form,
          child: ListView(padding: const EdgeInsets.all(16), children: [
            Text(widget.plan.title, style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 12),
            if (_settle) ...[
              Text('${text.t('未结本金', 'Outstanding principal')}: ${financeMoney(widget.plan.outstandingCents, widget.plan.currency)}'),
              const SizedBox(height: 12),
              TextFormField(key: const ValueKey('finance-principal'), controller: _principal,
                readOnly: widget.period != null || widget.settleInFull,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: InputDecoration(labelText: text.t('本次本金', 'Principal this time')),
                validator: (value) => _number(value, 2, text)),
              if (widget.plan.kind != FinanceKind.reimbursement) ...[
                const SizedBox(height: 12),
                TextFormField(controller: _interest, readOnly: widget.period != null,
                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  decoration: InputDecoration(labelText: text.t('本次利息', 'Interest this time')),
                  validator: (value) => _number(value, 2, text, zeroAllowed: true)),
              ],
              if (widget.settleInFull) Padding(padding: const EdgeInsets.only(top: 12),
                child: Text(text.t('结清全部剩余本金；利息请填写实际结清金额，不自动计入未来利息。', 'Settle all outstanding principal. Enter the actual interest due; future scheduled interest is not added.'))),
              if (widget.period != null) Padding(padding: const EdgeInsets.only(top: 12),
                child: Text('${text.t('本期合计', 'Scheduled payment')}: ${financeMoney(widget.period!.totalCents, widget.plan.currency)}')),
            ] else ...[
              TextFormField(controller: _amount,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: InputDecoration(labelText: '${_valuation ? text.t('当前总市值', 'Current total market value') : widget.action == FinanceAction.buy ? text.t('总支出（含手续费）', 'Total cost including fees') : widget.action == FinanceAction.sell ? text.t('净到账（扣除手续费）', 'Net proceeds after fees') : text.t('到账金额', 'Cash received')} (${widget.plan.currency})'),
                validator: (value) => _number(value, 2, text, zeroAllowed: _valuation || widget.action == FinanceAction.sell)),
              if (_hasUnits) ...[
                const SizedBox(height: 12),
                TextFormField(controller: _units,
                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  decoration: InputDecoration(labelText: text.t('本次份额', 'Units this time')),
                  validator: (value) => _number(value, 6, text)),
                if (widget.action == FinanceAction.sell) Text('${text.t('持有份额', 'Units held')}: ${financeDecimal(widget.plan.unitsMicros, 6)}'),
              ],
            ],
            if (!_valuation) ...[
              const SizedBox(height: 12),
              DropdownButtonFormField<int>(value: _accountId, isExpanded: true,
                decoration: InputDecoration(labelText: text.t('资金账户', 'Cash account')),
                items: [for (final account in _accounts) DropdownMenuItem(value: account.id, child: Text(account.name))],
                onChanged: (id) => setState(() => _accountId = id),
                validator: (value) => value == null ? text.t('请选择资金账户', 'Select a cash account') : null),
              if (_accounts.isEmpty) Padding(padding: const EdgeInsets.only(top: 8),
                child: Text(text.t('没有可用的同币种资金账户，请先在资产页创建。', 'No eligible account in this currency. Create one on Assets first.'))),
            ],
            ListTile(contentPadding: EdgeInsets.zero,
              title: Text(text.t('发生日期', 'Date')), subtitle: Text(financeDate(_date)),
              trailing: const Icon(Icons.calendar_today),
              onTap: () async {
                final selected = await showDatePicker(context: context, initialDate: _date,
                  firstDate: DateTime(1900), lastDate: DateTime.now());
                if (mounted && selected != null) setState(() => _date = selected);
              }),
            TextFormField(controller: _note, minLines: 1, maxLines: 4,
              decoration: InputDecoration(labelText: text.t('备注（可选）', 'Note (optional)'))),
            if (_saveError != null) Padding(padding: const EdgeInsets.symmetric(vertical: 12),
              child: Text(text.error(_saveError!), style: TextStyle(color: Theme.of(context).colorScheme.error))),
            const SizedBox(height: 24),
            FilledButton(key: const ValueKey('finance-submit'), onPressed: _saving ? null : _save,
              child: _saving ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
                : Text(text.t('确认记录', 'Record'))),
          ]),
        )),
    ));
  }
}
