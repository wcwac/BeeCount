import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/db.dart' as db;
import '../../models/finance.dart';
import '../../providers.dart';
import '../../providers/finance_providers.dart';
import '../../services/billing/post_processor.dart';
import 'finance_text.dart';

class FinanceFormPage extends ConsumerStatefulWidget {
  const FinanceFormPage({super.key, required this.ledgerId, this.initialKind});
  final int ledgerId;
  final FinanceKind? initialKind;
  @override
  ConsumerState<FinanceFormPage> createState() => _FinanceFormPageState();
}

class _FinanceFormPageState extends ConsumerState<FinanceFormPage> {
  final _form = GlobalKey<FormState>();
  final _title = TextEditingController();
  final _party = TextEditingController();
  final _amount = TextEditingController();
  final _rate = TextEditingController(text: '0');
  final _periods = TextEditingController(text: '12');
  final _units = TextEditingController(text: '1');
  final _note = TextEditingController();
  late FinanceKind _kind = widget.initialKind ?? FinanceKind.reimbursement;
  RepaymentMethod _method = RepaymentMethod.annuity;
  DateTime _date = DateTime.now();
  late DateTime _firstDue = financeMonth(_date, 1);
  late bool _recordCash = _kind != FinanceKind.mortgage;
  bool _linkExpense = false;
  bool _loading = true;
  bool _saving = false;
  Object? _loadError;
  Object? _saveError;
  List<db.Account> _accounts = [];
  List<db.Transaction> _expenses = [];
  int? _accountId;
  int? _expenseId;
  String _currency = '';

  @override
  void initState() { super.initState(); _load(); }
  @override
  void dispose() {
    for (final controller in [_title, _party, _amount, _rate, _periods, _units, _note]) { controller.dispose(); }
    super.dispose();
  }
  Future<void> _load() async {
    setState(() { _loading = true; _loadError = null; });
    try {
      final repository = ref.read(repositoryProvider);
      final finance = ref.read(financeRepositoryProvider);
      final ledger = await repository.getLedgerById(widget.ledgerId);
      if (ledger == null) throw StateError('Missing ledger');
      final accounts = await finance.cashAccounts(widget.ledgerId);
      final expenses = await finance.reimbursableExpenses(widget.ledgerId);
      if (!mounted) return;
      setState(() {
        _currency = ledger.currency;
        _accounts = accounts;
        _expenses = expenses;
        _accountId = accounts.length == 1 ? accounts.single.id : null;
        _loading = false;
      });
    } catch (error) {
      if (mounted) setState(() { _loading = false; _loadError = error; });
    }
  }
  bool get _needsCash => _recordCash && !_linkExpense && _kind != FinanceKind.installment;

  Future<void> _save() async {
    if (_saving || !(_form.currentState?.validate() ?? false)) return;
    setState(() { _saving = true; _saveError = null; });
    try {
      await ref.read(financeRepositoryProvider).create(FinanceDraft(
        ledgerId: widget.ledgerId,
        kind: _kind,
        title: _title.text.trim(),
        party: _party.text.trim(),
        amountCents: parseFinanceDecimal(_amount.text, 2)!,
        openedAt: _date,
        cashAccountId: _needsCash ? _accountId : null,
        existingExpenseId: _linkExpense ? _expenseId : null,
        recordCashFlow: _linkExpense ? false : _recordCash,
        annualRateBps: _kind.isScheduled ? parseFinanceDecimal(_rate.text, 2)! : 0,
        periods: _kind.isScheduled ? int.parse(_periods.text) : 12,
        method: _method,
        firstDueAt: _kind.isScheduled ? _firstDue : null,
        unitsMicros: _kind == FinanceKind.investment ? parseFinanceDecimal(_units.text, 6)! : 1000000,
        note: _note.text.trim(),
      ));
      if (!mounted) return;
      await PostProcessor.run(ref, ledgerId: widget.ledgerId);
      if (mounted) Navigator.of(context).pop(true);
    } catch (error) {
      if (mounted) setState(() => _saveError = error);
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }
  Future<void> _pickDate(bool due) async {
    final selected = await showDatePicker(context: context,
      initialDate: due ? _firstDue : _date,
      firstDate: DateTime(1900), lastDate: due ? DateTime(2200) : DateTime.now());
    if (mounted && selected != null) setState(() { if (due) { _firstDue = selected; } else { _date = selected; } });
  }
  Future<void> _previewSchedule() async {
    final text = FinanceText(context);
    try {
      final principal = parseFinanceDecimal(_amount.text, 2);
      final rate = parseFinanceDecimal(_rate.text, 2);
      final periods = int.tryParse(_periods.text);
      if (principal == null || rate == null || periods == null) {
        throw const FinanceFailure('invalid_terms');
      }
      final rows = repaymentSchedule(principalCents: principal, annualRateBps: rate,
        periods: periods, firstDueAt: _firstDue, method: _method);
      await showDialog<void>(context: context, builder: (context) => AlertDialog(
        title: Text(text.t('还款计划预览', 'Repayment preview')),
        content: SizedBox(width: 480, height: 400,
          child: ListView.builder(itemCount: rows.length, itemBuilder: (context, index) {
            final row = rows[index];
            return ListTile(contentPadding: EdgeInsets.zero,
              title: Text('${row.number} · ${financeDate(row.dueAt)}'),
              subtitle: Text('${text.t('本金', 'Principal')}: ${financeMoney(row.principalCents, _currency)}\n'
                '${text.t('利息', 'Interest')}: ${financeMoney(row.interestCents, _currency)}\n'
                '${text.t('合计', 'Total')}: ${financeMoney(row.totalCents, _currency)}'),
            );
          })),
        actions: [TextButton(onPressed: () => Navigator.pop(context), child: Text(text.t('关闭', 'Close')))],
      ));
    } catch (error) {
      if (mounted) setState(() => _saveError = error);
    }
  }

  String? _positive(String? raw, int places, FinanceText text) {
    final number = parseFinanceDecimal(raw ?? '', places);
    return number == null || number <= 0 ? text.t('请输入大于零的有效数值', 'Enter a valid positive number') : null;
  }

  @override
  Widget build(BuildContext context) {
    final text = FinanceText(context);
    return PopScope(
      canPop: !_saving,
      child: Scaffold(
        appBar: AppBar(title: Text(text.t('新增财务记录', 'New finance record'))),
        body: _loading ? const Center(child: CircularProgressIndicator())
            : _loadError != null ? Center(child: Column(mainAxisSize: MainAxisSize.min, children: [
                Text(text.error(_loadError!)),
                TextButton(onPressed: _load, child: Text(text.t('重试', 'Retry'))),
              ]))
            : AbsorbPointer(absorbing: _saving, child: Form(
                key: _form,
                child: ListView(padding: const EdgeInsets.all(16), children: [
                  DropdownButtonFormField<FinanceKind>(
                    value: _kind, isExpanded: true,
                    decoration: InputDecoration(labelText: text.t('类型', 'Type')),
                    items: [for (final kind in FinanceKind.values)
                      DropdownMenuItem(value: kind, child: Text(text.kind(kind)))],
                    onChanged: (kind) { if (kind != null) setState(() { _kind = kind; _linkExpense = false; _expenseId = null; _recordCash = kind != FinanceKind.mortgage; }); },
                  ),
                  const SizedBox(height: 12),
                  TextFormField(controller: _title,
                    decoration: InputDecoration(labelText: text.t('名称', 'Name')),
                    validator: (value) => (value?.trim().isEmpty ?? true) ? text.t('请输入名称', 'Enter a name') : null),
                  const SizedBox(height: 12),
                  TextFormField(controller: _party,
                    decoration: InputDecoration(labelText: text.t('对方／机构（可选）', 'Person or institution (optional)'))),
                  if (_kind == FinanceKind.reimbursement) ...[
                    SwitchListTile(contentPadding: EdgeInsets.zero,
                      title: Text(text.t('关联已有支出', 'Link an existing expense')),
                      subtitle: Text(text.t('关联完整金额，保留原分类和附件，不重复扣款。', 'Link the full amount without duplicating the payment; keep categories and attachments.')),
                      value: _linkExpense,
                      onChanged: (value) => setState(() { _linkExpense = value; _expenseId = null; })),
                    if (_linkExpense)
                      DropdownButtonFormField<int>(
                        isExpanded: true, value: _expenseId,
                        decoration: InputDecoration(labelText: text.t('选择原支出', 'Original expense')),
                        items: [for (final expense in _expenses) DropdownMenuItem(
                          value: expense.id,
                          child: Text('${financeDate(expense.happenedAt)} · ${expense.amount.toStringAsFixed(2)} · ${expense.note ?? ''}', maxLines: 2, overflow: TextOverflow.ellipsis))],
                        validator: (value) => value == null ? text.t('请选择支出', 'Choose an expense') : null,
                        onChanged: (id) { if (id != null) setState(() {
                          _expenseId = id;
                          final expense = _expenses.firstWhere((item) => item.id == id);
                          _amount.text = expense.amount.toStringAsFixed(2);
                          _date = expense.happenedAt;
                        }); },
                      ),
                  ],
                  const SizedBox(height: 12),
                  TextFormField(controller: _amount, readOnly: _linkExpense,
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                    decoration: InputDecoration(labelText: '${_kind == FinanceKind.investment ? text.t('买入成本（含费用）', 'Purchase cost including fees') : text.t('本金／垫付金额', 'Principal or advance')} ($_currency)'),
                    validator: (value) => _positive(value, 2, text)),
                  if (_kind == FinanceKind.investment) ...[
                    const SizedBox(height: 12),
                    TextFormField(controller: _units,
                      keyboardType: const TextInputType.numberWithOptions(decimal: true),
                      decoration: InputDecoration(labelText: text.t('份额（最多六位小数）', 'Units (up to six decimals)')),
                      validator: (value) => _positive(value, 6, text)),
                  ],
                  ListTile(contentPadding: EdgeInsets.zero,
                    title: Text(text.t('发生日期', 'Date')), subtitle: Text(financeDate(_date)),
                    trailing: _linkExpense ? const Icon(Icons.link) : const Icon(Icons.calendar_today), onTap: _linkExpense ? null : () => _pickDate(false)),
                  if (!_linkExpense) SwitchListTile(contentPadding: EdgeInsets.zero,
                    title: Text(text.t('同时记录本次资金或消费流水', 'Record the cash flow or purchase now')),
                    subtitle: Text(text.t('关闭表示只登记已有余额，不补写历史流水。', 'Turn off to register an existing balance without creating historical transactions.')),
                    value: _recordCash, onChanged: (value) => setState(() => _recordCash = value)),
                  Padding(padding: const EdgeInsets.symmetric(vertical: 8),
                    child: Text(_kind == FinanceKind.installment
                      ? text.t('新分期消费在建立时记一次支出；后续偿还本金不会重复计为消费。只登记历史余额时请关闭上方开关。', 'A new instalment purchase counts as an expense once. Principal repayments do not count as spending again. Turn off the switch for an existing balance.')
                      : _kind == FinanceKind.mortgage && !_recordCash
                        ? text.t('登记已有房贷：本金请填写当前剩余本金，期数填写剩余期数，不重复登记过去的借款收入。', 'Existing mortgage: enter the remaining principal and months. No past cash flow is added.')
                        : _kind == FinanceKind.investment
                          ? text.t('记录资金流水会从所选账户扣除买入成本。只登记已有持仓时关闭开关，并填写剩余成本与持有份额。', 'Recording cash flow debits the purchase cost. Turn off for existing holdings and enter remaining cost and units.')
                          : text.t('记录资金流水会同步更新资金账户余额；登记已有应收或欠款请关闭开关。', 'Cash flow updates your account balance. Turn off when registering an existing receivable or debt.'))),
                  if (_needsCash) ...[
                    DropdownButtonFormField<int>(isExpanded: true, value: _accountId,
                      decoration: InputDecoration(labelText: text.t('资金账户', 'Cash account')),
                      items: [for (final account in _accounts) DropdownMenuItem(value: account.id, child: Text(account.name))],
                      validator: (value) => value == null ? text.t('请选择同币种资金账户', 'Select an account in the same currency') : null,
                      onChanged: (id) => setState(() => _accountId = id)),
                    if (_accounts.isEmpty) Padding(padding: const EdgeInsets.only(top: 8),
                      child: Text(text.t('请先在资产页创建同币种、可交易且未隐藏的账户。', 'First create a visible transaction account in this currency on Assets.'))),
                  ],
                  if (_kind.isScheduled) ...[
                    const SizedBox(height: 12),
                    TextFormField(controller: _rate,
                      keyboardType: const TextInputType.numberWithOptions(decimal: true),
                      decoration: InputDecoration(labelText: text.t('年利率（%）', 'Annual interest rate (%)')),
                      validator: (value) => parseFinanceDecimal(value ?? '', 2) == null ? text.t('请输入非负利率，最多两位小数', 'Enter a non-negative rate with up to two decimals') : null),
                    const SizedBox(height: 12),
                    TextFormField(controller: _periods, keyboardType: TextInputType.number,
                      decoration: InputDecoration(labelText: text.t('期数（月）', 'Term (months)')),
                      validator: (value) => (int.tryParse(value ?? '') ?? 0) <= 0 || (int.tryParse(value ?? '') ?? 0) > 600 ? text.t('请输入有效期数', 'Enter a valid term') : null),
                    const SizedBox(height: 12),
                    DropdownButtonFormField<RepaymentMethod>(isExpanded: true, value: _method,
                      decoration: InputDecoration(labelText: text.t('还款方式', 'Repayment method')),
                      items: [for (final method in RepaymentMethod.values) DropdownMenuItem(value: method, child: Text(text.method(method), maxLines: 2))],
                      onChanged: (value) { if (value != null) setState(() => _method = value); }),
                    ListTile(contentPadding: EdgeInsets.zero,
                      title: Text(text.t('首次还款日', 'First payment date')), subtitle: Text(financeDate(_firstDue)),
                      trailing: const Icon(Icons.calendar_today), onTap: () => _pickDate(true)),
                    Text(text.t('每月一期。建立后可查看计划表、逐期还款或提前结清。', 'Monthly instalments. After creating the plan, view the schedule, record payments or settle early.')),
                    OutlinedButton.icon(onPressed: _previewSchedule, icon: const Icon(Icons.table_rows_outlined),
                      label: Text(text.t('预览还款计划', 'Preview repayment schedule'))),
                  ],
                  const SizedBox(height: 12),
                  TextFormField(controller: _note, minLines: 1, maxLines: 4,
                    decoration: InputDecoration(labelText: text.t('备注（可选）', 'Note (optional)'))),
                  if (_saveError != null) Padding(padding: const EdgeInsets.symmetric(vertical: 12),
                    child: Text(text.error(_saveError!), style: TextStyle(color: Theme.of(context).colorScheme.error))),
                  const SizedBox(height: 24),
                  FilledButton(onPressed: _saving ? null : _save,
                    child: _saving ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
                      : Text(text.t('保存', 'Save'))),
                  const SizedBox(height: 24),
                ]),
              )),
      ),
    );
  }
}
