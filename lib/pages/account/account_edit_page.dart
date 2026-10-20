import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../providers.dart';
import '../../widgets/ui/ui.dart';
import '../../widgets/biz/section_card.dart';
import '../../data/db.dart' as db;
import '../../l10n/app_localizations.dart';
import '../../controllers/account_edit_controller.dart';
import '../../utils/currencies.dart';
import '../../styles/tokens.dart';
import '../../utils/ui_scale_extensions.dart';
import '../../utils/account_type_utils.dart';

class AccountEditPage extends ConsumerStatefulWidget {
  final db.Account? account; // null表示新建
  final int ledgerId;

  const AccountEditPage({
    super.key,
    this.account,
    required this.ledgerId,
  });

  @override
  ConsumerState<AccountEditPage> createState() => _AccountEditPageState();
}

class _AccountEditPageState extends ConsumerState<AccountEditPage> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _nameController;
  late final TextEditingController _initialBalanceController;
  late final TextEditingController _creditLimitController;
  late final TextEditingController _bankNameController;
  late final TextEditingController _cardLastFourController;
  late final TextEditingController _noteController;
  late String _selectedType;
  late String _selectedCurrency;
  int? _billingDay;
  int? _paymentDueDay;
  bool _reminderEnabled = false;
  int _reminderDaysBefore = 3;
  bool _saving = false;
  bool _balanceLoaded = true;
  bool _balanceLoadFailed = false;
  bool _creditOverpayment = false;
  double? _loadedCurrentBalance;
  bool _isNameDuplicate = false;
  String? _nameErrorText;
  // 账户类型 Tab：0 = 日常账户，1 = 估值账户
  int _typeTab = 0;

  // 日常账户类型（走流水）
  static const List<String> tradableAccountTypes = [
    'cash',
    'bank_card',
    'credit_card',
    'alipay',
    'wechat',
    'other',
  ];

  // 估值账户类型（只记当前价值 / 欠款，不走流水）
  static const List<String> valuationAccountTypes = [
    'real_estate',
    'vehicle',
    'investment',
    'insurance',
    'social_fund',
    'loan',
  ];

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.account?.name ?? '');
    final hasEditableCurrentBalance = widget.account != null &&
        !isValuationOnlyType(widget.account!.type);
    _balanceLoaded = !hasEditableCurrentBalance;
    _initialBalanceController = TextEditingController(
      text: !hasEditableCurrentBalance &&
              widget.account?.initialBalance != null &&
              widget.account!.initialBalance != 0.0
          ? widget.account!.initialBalance.abs().toStringAsFixed(2)
          : '',
    );
    _creditLimitController = TextEditingController(
      text: widget.account?.creditLimit != null
          ? widget.account!.creditLimit!.toStringAsFixed(2)
          : '',
    );
    _bankNameController = TextEditingController(text: widget.account?.bankName ?? '');
    _cardLastFourController = TextEditingController(text: widget.account?.cardLastFour ?? '');
    _noteController = TextEditingController(text: widget.account?.note ?? '');
    _selectedType = widget.account?.type ?? 'cash';
    _selectedCurrency = widget.account?.currency ?? 'CNY';
    _billingDay = widget.account?.billingDay;
    _paymentDueDay = widget.account?.paymentDueDay;
    _typeTab = isValuationOnlyType(_selectedType) ? 1 : 0;
    if (hasEditableCurrentBalance) _loadEditableCurrentBalance();
    _loadReminderSettings();
  }

  Future<void> _loadEditableCurrentBalance() async {
    try {
      final balance = await ref
          .read(repositoryProvider)
          .getAccountBalance(widget.account!.id);
      if (!mounted) return;
      setState(() {
        _loadedCurrentBalance = balance;
        _creditOverpayment = balance > 0;
        _initialBalanceController.text = (_selectedType == 'credit_card'
            ? balance.abs() : balance).toStringAsFixed(2);
        _balanceLoaded = true;
      });
    } catch (error) {
      if (!mounted) return;
      setState(() => _balanceLoadFailed = true);
      showToast(context, '${AppLocalizations.of(context).commonError}: $error');
    }
  }

  Future<void> _loadReminderSettings() async {
    if (widget.account != null) {
      final prefs = await SharedPreferences.getInstance();
      final enabled = prefs.getBool('cc_reminder_enabled_${widget.account!.id}') ?? false;
      final daysBefore = prefs.getInt('cc_reminder_days_${widget.account!.id}') ?? 3;
      if (mounted) {
        setState(() {
          _reminderEnabled = enabled;
          _reminderDaysBefore = daysBefore;
        });
      }
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _initialBalanceController.dispose();
    _creditLimitController.dispose();
    _bankNameController.dispose();
    _cardLastFourController.dispose();
    _noteController.dispose();
    super.dispose();
  }

  bool get isEditing => widget.account != null;

  String? _validateBalance(String? value) {
    final text = value?.trim() ?? '';
    if (text.isEmpty && !isEditing) return null;
    final parsed = double.tryParse(text);
    if (parsed == null || !parsed.isFinite ||
        (_selectedType == 'credit_card' && parsed < 0)) {
      return AppLocalizations.of(context).creditCardAmountInvalid;
    }
    return null;
  }

  String _getInitialBalanceLabel(AppLocalizations l10n) {
    if (_selectedType == 'credit_card') {
      return _creditOverpayment ? l10n.creditCardOverpayment : l10n.creditCardOwed;
    }
    if (isValuationOnlyType(_selectedType)) {
      return isLiabilityType(_selectedType)
          ? l10n.valuationCurrentDebt
          : l10n.valuationCurrentValue;
    }
    return l10n.accountInitialBalance;
  }

  String _getInitialBalanceHint(AppLocalizations l10n) {
    if (isValuationOnlyType(_selectedType)) {
      return isLiabilityType(_selectedType)
          ? l10n.valuationDebtHint
          : l10n.valuationAccountHint;
    }
    switch (_selectedType) {
      case 'credit_card':
        return _creditOverpayment
            ? l10n.creditCardOverpaymentHint : l10n.creditCardDebtHint;
      default:
        return l10n.accountInitialBalanceHint;
    }
  }

  Future<bool?> _promptBalanceAdjustmentChoice({
    required double currentBalance,
    required double targetBalance,
    required AppLocalizations l10n,
  }) async {
    final difference = targetBalance - currentBalance;
    final currencySymbol = getCurrencySymbol(_selectedCurrency);
    String formatBalance(double value) {
      final sign = value < 0 ? '−' : '';
      return '$sign$currencySymbol${value.abs().toStringAsFixed(2)}';
    }

    return showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      backgroundColor: BeeTokens.surface(context),
      constraints: const BoxConstraints(maxWidth: 560),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
      ),
      builder: (sheetContext) {
        final differenceColor = difference > 0
            ? BeeTokens.success(sheetContext)
            : difference < 0
                ? BeeTokens.error(sheetContext)
                : BeeTokens.textSecondary(sheetContext);
        final signedDifference =
            '${difference > 0 ? '+' : difference < 0 ? '−' : ''}'
            '$currencySymbol${difference.abs().toStringAsFixed(2)}';

        return SafeArea(
          top: false,
          child: SingleChildScrollView(
            child: Padding(
              padding: EdgeInsets.fromLTRB(
                20,
                8,
                20,
                12 + MediaQuery.of(sheetContext).viewInsets.bottom,
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        width: 44,
                        height: 44,
                        decoration: BoxDecoration(
                          color: BeeTokens.surfaceSelected(sheetContext),
                          shape: BoxShape.circle,
                        ),
                        child: Icon(
                          Icons.account_balance_wallet_outlined,
                          color: Theme.of(sheetContext).colorScheme.primary,
                          size: 23,
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          l10n.accountBalanceAdjustmentTitle,
                          style: TextStyle(
                            color: BeeTokens.textPrimary(sheetContext),
                            fontSize: 20,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                      IconButton(
                        tooltip: l10n.commonCancel,
                        onPressed: () => Navigator.pop(sheetContext),
                        icon: Icon(
                          Icons.close_rounded,
                          color: BeeTokens.iconSecondary(sheetContext),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  Padding(
                    padding: const EdgeInsets.only(left: 56),
                    child: Text(
                      l10n.accountBalanceAdjustmentChooseAction,
                      style: TextStyle(
                        color: BeeTokens.textSecondary(sheetContext),
                        fontSize: 14,
                      ),
                    ),
                  ),
                  const SizedBox(height: 20),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: BeeTokens.surfaceSecondary(sheetContext),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Column(
                      children: [
                        Row(
                          children: [
                            Expanded(
                              child: _balanceSummaryValue(
                                context: sheetContext,
                                label: l10n.accountBalanceCurrent,
                                value: formatBalance(currentBalance),
                                alignEnd: false,
                              ),
                            ),
                            Padding(
                              padding: const EdgeInsets.only(top: 18),
                              child: Icon(
                                Icons.arrow_forward_rounded,
                                color: BeeTokens.iconTertiary(sheetContext),
                                size: 20,
                              ),
                            ),
                            const SizedBox(width: 10),
                            Expanded(
                              child: _balanceSummaryValue(
                                context: sheetContext,
                                label: l10n.accountBalanceAfterAdjustment,
                                value: formatBalance(targetBalance),
                                alignEnd: true,
                                valueColor: Theme.of(sheetContext)
                                    .colorScheme
                                    .primary,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),
                        Divider(
                          height: 1,
                          color: BeeTokens.borderStrong(sheetContext),
                        ),
                        const SizedBox(height: 12),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              l10n.accountBalanceDifference,
                              style: TextStyle(
                                color: BeeTokens.textSecondary(sheetContext),
                                fontSize: 13,
                              ),
                            ),
                            Text(
                              signedDifference,
                              style: TextStyle(
                                color: differenceColor,
                                fontSize: 15,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 22),
                  _BalanceAdjustmentModeSelector(
                    l10n: l10n,
                    difference: signedDifference,
                    differenceColor: differenceColor,
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _balanceSummaryValue({
    required BuildContext context,
    required String label,
    required String value,
    required bool alignEnd,
    Color? valueColor,
  }) {
    return Column(
      crossAxisAlignment:
          alignEnd ? CrossAxisAlignment.end : CrossAxisAlignment.start,
      children: [
        Text(
          label,
          textAlign: alignEnd ? TextAlign.end : TextAlign.start,
          style: TextStyle(
            color: BeeTokens.textSecondary(context),
            fontSize: 12,
          ),
        ),
        const SizedBox(height: 7),
        Text(
          value,
          textAlign: alignEnd ? TextAlign.end : TextAlign.start,
          style: TextStyle(
            color: valueColor ?? BeeTokens.textPrimary(context),
            fontSize: 17,
            fontWeight: FontWeight.w700,
          ),
        ),
      ],
    );
  }

  void _retryEditableCurrentBalance() {
    setState(() => _balanceLoadFailed = false);
    _loadEditableCurrentBalance();
  }

  /// v1.15.0: 检查账户名称是否重复
  Future<void> _checkNameDuplicate(String name) async {
    if (name.trim().isEmpty) {
      setState(() {
        _isNameDuplicate = false;
        _nameErrorText = null;
      });
      return;
    }

    final repo = ref.read(repositoryProvider);
    final allAccounts = await repo.getAllAccounts();
    final isDuplicate = allAccounts.any((account) {
      // 如果是编辑模式，排除当前账户本身
      if (isEditing && account.id == widget.account!.id) {
        return false;
      }
      return account.name == name.trim();
    });

    if (mounted) {
      setState(() {
        _isNameDuplicate = isDuplicate;
        _nameErrorText = isDuplicate
            ? AppLocalizations.of(context).accountNameDuplicate
            : null;
      });
    }
  }

  /// 切换账户类型：复用旧逻辑——离开信用卡/银行卡时清空对应字段。
  void _selectType(String type) {
    setState(() {
      final oldType = _selectedType;
      if (oldType != type && (oldType == 'credit_card' || type == 'credit_card')) {
        final parsed = double.tryParse(_initialBalanceController.text);
        if (parsed != null) {
          final signed = oldType == 'credit_card'
              ? (_creditOverpayment ? parsed : -parsed) : parsed;
          _creditOverpayment = signed > 0;
          _initialBalanceController.text =
              (type == 'credit_card' ? signed.abs() : signed).toStringAsFixed(2);
        }
      }
      _selectedType = type;
      if (oldType == 'credit_card' && type != 'credit_card') {
        _creditLimitController.clear();
        _billingDay = null;
        _paymentDueDay = null;
        _reminderEnabled = false;
      }
      final wasBankOrCredit = oldType == 'bank_card' || oldType == 'credit_card';
      final isBankOrCredit = type == 'bank_card' || type == 'credit_card';
      if (wasBankOrCredit && !isBankOrCredit) {
        _bankNameController.clear();
        _cardLastFourController.clear();
      }
    });
  }

  TextStyle _sectionTitle(BuildContext context) => TextStyle(
        fontSize: 14,
        fontWeight: FontWeight.w600,
        color: BeeTokens.textPrimary(context),
      );

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final primaryColor = ref.watch(primaryColorProvider);

    // 统一的 filled 圆角输入框装饰（委托顶层实现，便于点击式字段框复用）
    InputDecoration filledDec(
            {String? label, String? hint, String? prefix, String? errorText}) =>
        _filledDecoration(context, primaryColor,
            label: label, hint: hint, prefix: prefix, errorText: errorText);

    final typesForTab = _typeTab == 0 ? tradableAccountTypes : valuationAccountTypes;
    final isCreditCard = _selectedType == 'credit_card';
    final isBankCard = _selectedType == 'bank_card';

    return Scaffold(
      backgroundColor: BeeTokens.scaffoldBackground(context),
      body: Column(
        children: [
          PrimaryHeader(
            title: isEditing ? l10n.accountEditTitle : l10n.accountNewTitle,
            showBack: true,
          ),
          Expanded(
            child: Form(
              key: _formKey,
              child: ListView(
                padding: EdgeInsets.only(
                  left: 12.0.scaled(context, ref),
                  right: 12.0.scaled(context, ref),
                  top: 8.0.scaled(context, ref),
                  bottom: 8.0.scaled(context, ref) +
                      MediaQuery.of(context).padding.bottom,
                ),
                children: [
                  // ===== 账户类型（日常/估值 Tab + 缩小网格）=====
                  SectionCard(
                    margin: EdgeInsets.zero,
                    child: Padding(
                      padding: EdgeInsets.all(16.0.scaled(context, ref)),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          CapsuleSwitcher<int>(
                            key: const ValueKey('account-type-group'),
                            selectedValue: _typeTab,
                            height: 44.0.scaled(context, ref),
                            options: [
                              CapsuleOption(value: 0, label: l10n.accountGroupTradable),
                              CapsuleOption(value: 1, label: l10n.accountTabValuation),
                            ],
                            onChanged: (value) => setState(() => _typeTab = value),
                          ),
                          SizedBox(height: 16.0.scaled(context, ref)),
                          GridView.count(
                            crossAxisCount: 4,
                            shrinkWrap: true,
                            physics: const NeverScrollableScrollPhysics(),
                            mainAxisSpacing: 10.0.scaled(context, ref),
                            crossAxisSpacing: 10.0.scaled(context, ref),
                            childAspectRatio: 1.0,
                            children: typesForTab.map((type) {
                              final isSelected = _selectedType == type;
                              // 编辑模式禁止跨“可交易 / 估值”大类切换（语义不同）
                              final disabled = isEditing &&
                                  isValuationOnlyType(type) !=
                                      isValuationOnlyType(widget.account!.type);
                              return _AccountTypeCard(
                                type: type,
                                label: getAccountTypeLabel(context, type),
                                isSelected: isSelected,
                                primaryColor: primaryColor,
                                disabled: disabled,
                                onTap: disabled ? () {} : () => _selectType(type),
                              );
                            }).toList(),
                          ),
                        ],
                      ),
                    ),
                  ),

                  SizedBox(height: 8.0.scaled(context, ref)),

                  // ===== 基本（名称 + 币种/余额）=====
                  SectionCard(
                    margin: EdgeInsets.zero,
                    child: Padding(
                      padding: EdgeInsets.all(16.0.scaled(context, ref)),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          TextFormField(
                            key: const ValueKey('account-name'),
                            controller: _nameController,
                            decoration: filledDec(
                              label: l10n.accountNameLabel,
                              hint: l10n.accountNameHint,
                              errorText: _nameErrorText,
                            ),
                            style: const TextStyle(fontSize: 16),
                            onChanged: (value) => _checkNameDuplicate(value),
                            validator: (value) {
                              if (value == null || value.trim().isEmpty) {
                                return l10n.accountNameRequired;
                              }
                              return null;
                            },
                          ),
                          SizedBox(height: 12.0.scaled(context, ref)),
                          Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              SizedBox(
                                width: 120.0.scaled(context, ref),
                                child: InkWell(
                                  borderRadius: BorderRadius.circular(12),
                                  onTap: () async {
                                    // 同账单日：开选择器前先收键盘
                                    FocusManager.instance.primaryFocus?.unfocus();
                                    if (isEditing) {
                                      final repo = ref.read(repositoryProvider);
                                      final hasTransactions = await repo
                                          .hasTransactions(widget.account!.id);
                                      if (hasTransactions) {
                                        if (!context.mounted) return;
                                        await AppDialog.info(
                                          context,
                                          title: l10n.commonNotice,
                                          message: l10n.accountCurrencyLocked,
                                        );
                                        return;
                                      }
                                    }
                                    if (!context.mounted) return;
                                    final picked = await _showCurrencyPicker(
                                        context,
                                        initial: _selectedCurrency);
                                    if (picked != null) {
                                      setState(() => _selectedCurrency = picked);
                                    }
                                  },
                                  child: InputDecorator(
                                    decoration:
                                        filledDec(label: l10n.ledgersCurrency),
                                    child: Row(
                                      children: [
                                        Expanded(
                                          child: Text(
                                            displayCurrency(
                                                _selectedCurrency, context),
                                            maxLines: 1,
                                            overflow: TextOverflow.ellipsis,
                                            style: const TextStyle(fontSize: 16),
                                          ),
                                        ),
                                        Icon(Icons.expand_more,
                                            size: 18.0.scaled(context, ref),
                                            color:
                                                BeeTokens.iconTertiary(context)),
                                      ],
                                    ),
                                  ),
                                ),
                              ),
                              SizedBox(width: 12.0.scaled(context, ref)),
                              if (!isEditing)
                                Expanded(
                                  child: TextFormField(
                                    key: const ValueKey('account-balance-amount'),
                                    controller: _initialBalanceController,
                                    decoration: filledDec(
                                      label: _getInitialBalanceLabel(l10n),
                                      hint: _getInitialBalanceHint(l10n),
                                      prefix:
                                          '${getCurrencySymbol(_selectedCurrency)} ',
                                    ),
                                    style: const TextStyle(fontSize: 16),
                                    keyboardType:
                                        const TextInputType.numberWithOptions(
                                            decimal: true, signed: true),
                                    validator: _validateBalance,
                                  ),
                                )
                              else if (!isValuationOnlyType(widget.account!.type) &&
                                  !isValuationOnlyType(_selectedType))
                                Expanded(
                                  child: TextFormField(
                                    key: const ValueKey('account-balance-amount'),
                                    controller: _initialBalanceController,
                                    readOnly:
                                        !_balanceLoaded || _balanceLoadFailed,
                                    decoration: filledDec(
                                      label: _selectedType == 'credit_card'
                                          ? _getInitialBalanceLabel(l10n) : l10n.accountBalance,
                                      prefix:
                                          '${getCurrencySymbol(_selectedCurrency)} ',
                                    ).copyWith(
                                      suffixIcon: _balanceLoadFailed
                                          ? IconButton(
                                              tooltip: l10n.helpCenterRetry,
                                              onPressed: _retryEditableCurrentBalance,
                                              icon: const Icon(Icons.refresh),
                                            )
                                          : _balanceLoaded
                                              ? null
                                              : Padding(
                                                  padding: EdgeInsets.all(
                                                    12.0.scaled(context, ref),
                                                  ),
                                                  child:
                                                      const CircularProgressIndicator(
                                                    strokeWidth: 2,
                                                  ),
                                                ),
                                    ),
                                    style: const TextStyle(fontSize: 16),
                                    keyboardType:
                                        const TextInputType.numberWithOptions(
                                      decimal: true,
                                      signed: true,
                                    ),
                                    validator: _validateBalance,
                                  ),
                                )
                              else
                                const Spacer(),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),

                  if (isCreditCard) Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        CapsuleSwitcher<bool>(
                          key: const ValueKey('account-credit-balance-kind'),
                          selectedValue: _creditOverpayment,
                          height: 44.0.scaled(context, ref),
                          options: [
                            CapsuleOption(value: false, label: l10n.creditCardOwed),
                            CapsuleOption(value: true, label: l10n.creditCardOverpayment),
                          ],
                          onChanged: (value) =>
                              setState(() => _creditOverpayment = value),
                        ),
                        const SizedBox(height: 8),
                        Text(_getInitialBalanceHint(l10n),
                            style: TextStyle(fontSize: 12, color: BeeTokens.textSecondary(context))),
                      ],
                    ),
                  ),
                  // ===== 信用卡信息（仅 credit_card）=====
                  if (isCreditCard) ...[
                    SizedBox(height: 8.0.scaled(context, ref)),
                    SectionCard(
                      margin: EdgeInsets.zero,
                      child: Padding(
                        padding: EdgeInsets.all(16.0.scaled(context, ref)),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(l10n.creditCardSettings, style: _sectionTitle(context)),
                            SizedBox(height: 12.0.scaled(context, ref)),
                            // 信用额度（必填）
                            TextFormField(
                              key: const ValueKey('account-credit-limit'),
                              controller: _creditLimitController,
                              decoration: filledDec(
                                label: l10n.creditLimit,
                                hint: l10n.creditLimitHint,
                                prefix: '${getCurrencySymbol(_selectedCurrency)} ',
                              ),
                              style: const TextStyle(fontSize: 16),
                              keyboardType: const TextInputType.numberWithOptions(
                                  decimal: true),
                              validator: (value) {
                                final t = value?.trim() ?? '';
                                final parsed = double.tryParse(t);
                                if (t.isEmpty) return null;
                                if (parsed == null || !parsed.isFinite || parsed <= 0) {
                                  return l10n.creditLimitHint;
                                }
                                return null;
                              },
                            ),
                            SizedBox(height: 12.0.scaled(context, ref)),
                            // 账单日 / 还款日（双列，必填）
                            Row(
                              children: [
                                Expanded(
                                  child: _DayPickerTile(
                                    label: l10n.billingDay,
                                    value: _billingDay,
                                    primaryColor: primaryColor,
                                    onChanged: (day) =>
                                        setState(() => _billingDay = day),
                                  ),
                                ),
                                SizedBox(width: 12.0.scaled(context, ref)),
                                Expanded(
                                  child: _DayPickerTile(
                                    label: l10n.paymentDueDay,
                                    value: _paymentDueDay,
                                    primaryColor: primaryColor,
                                    onChanged: (day) =>
                                        setState(() => _paymentDueDay = day),
                                  ),
                                ),
                              ],
                            ),
                            SizedBox(height: 12.0.scaled(context, ref)),
                            // 开户行 / 卡号后四（双列）
                            Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Expanded(
                                  child: TextFormField(
                                    controller: _bankNameController,
                                    decoration: filledDec(
                                      label: l10n.accountBankName,
                                      hint: l10n.accountBankNameHint,
                                    ),
                                    style: const TextStyle(fontSize: 16),
                                  ),
                                ),
                                SizedBox(width: 12.0.scaled(context, ref)),
                                Expanded(
                                  child: TextFormField(
                                    controller: _cardLastFourController,
                                    decoration: filledDec(
                                      label: l10n.accountCardLastFour,
                                      hint: l10n.accountCardLastFourHint,
                                    ).copyWith(counterText: ''),
                                    style: const TextStyle(fontSize: 16),
                                    maxLength: 4,
                                    keyboardType: TextInputType.number,
                                  ),
                                ),
                              ],
                            ),
                            SizedBox(height: 4.0.scaled(context, ref)),
                            Divider(color: BeeTokens.divider(context)),
                            // 还款提醒
                            SwitchListTile(
                              dense: true,
                              contentPadding: EdgeInsets.zero,
                              title: Text(
                                l10n.creditCardReminderTitle,
                                style: TextStyle(
                                  fontSize: 14,
                                  color: BeeTokens.textPrimary(context),
                                ),
                              ),
                              subtitle: Text(
                                l10n.creditCardReminderDesc,
                                style: TextStyle(
                                  fontSize: 12,
                                  color: BeeTokens.textTertiary(context),
                                ),
                              ),
                              value: _reminderEnabled,
                              activeColor: primaryColor,
                              onChanged: (value) =>
                                  setState(() => _reminderEnabled = value),
                            ),
                            if (_reminderEnabled) ...[
                              SizedBox(height: 4.0.scaled(context, ref)),
                              CapsuleSwitcher<int>(
                                key: const ValueKey('account-credit-reminder-days'),
                                selectedValue: _reminderDaysBefore,
                                height: 44.0.scaled(context, ref),
                                options: [
                                  for (final days in [1, 3, 5, 7])
                                    CapsuleOption(value: days,
                                        label: l10n.creditCardReminderDaysBefore(days)),
                                ],
                                onChanged: (days) => setState(
                                    () => _reminderDaysBefore = days),
                              ),
                            ],
                          ],
                        ),
                      ),
                    ),
                  ],

                  // ===== 卡信息（仅 bank_card）=====
                  if (isBankCard) ...[
                    SizedBox(height: 8.0.scaled(context, ref)),
                    SectionCard(
                      margin: EdgeInsets.zero,
                      child: Padding(
                        padding: EdgeInsets.all(16.0.scaled(context, ref)),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(l10n.accountMetaInfo, style: _sectionTitle(context)),
                            SizedBox(height: 12.0.scaled(context, ref)),
                            Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Expanded(
                                  child: TextFormField(
                                    controller: _bankNameController,
                                    decoration: filledDec(
                                      label: l10n.accountBankName,
                                      hint: l10n.accountBankNameHint,
                                    ),
                                    style: const TextStyle(fontSize: 16),
                                  ),
                                ),
                                SizedBox(width: 12.0.scaled(context, ref)),
                                Expanded(
                                  child: TextFormField(
                                    controller: _cardLastFourController,
                                    decoration: filledDec(
                                      label: l10n.accountCardLastFour,
                                      hint: l10n.accountCardLastFourHint,
                                    ).copyWith(counterText: ''),
                                    style: const TextStyle(fontSize: 16),
                                    maxLength: 4,
                                    keyboardType: TextInputType.number,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],

                  // ===== 备注（所有类型）=====
                  SizedBox(height: 8.0.scaled(context, ref)),
                  SectionCard(
                    margin: EdgeInsets.zero,
                    child: Padding(
                      padding: EdgeInsets.all(16.0.scaled(context, ref)),
                      child: TextFormField(
                        controller: _noteController,
                        decoration: filledDec(
                          label: l10n.accountNote,
                          hint: l10n.accountNoteHint,
                        ),
                        style: const TextStyle(fontSize: 16),
                        maxLines: 3,
                        minLines: 1,
                      ),
                    ),
                  ),

                  SizedBox(height: 24.0.scaled(context, ref)),

                  // 保存按钮
                  SizedBox(
                    width: double.infinity,
                    height: 48.0.scaled(context, ref),
                    child: ElevatedButton(
                      onPressed: (_saving ||
                              _isNameDuplicate ||
                              !_balanceLoaded ||
                              _balanceLoadFailed)
                          ? null
                          : _save,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: primaryColor,
                        foregroundColor: Colors.white,
                        disabledBackgroundColor: Colors.grey[400],
                        elevation: 0,
                        shape: RoundedRectangleBorder(
                          borderRadius:
                              BorderRadius.circular(8.0.scaled(context, ref)),
                        ),
                      ),
                      child: _saving
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                  strokeWidth: 2, color: Colors.white),
                            )
                          : Text(
                              l10n.commonSave,
                              style: const TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                    ),
                  ),

                  // 隐藏/恢复 + 删除按钮（仅编辑时显示；账户隐藏 #240,产品设计
                  // 01 §3.2:隐藏=留数据、可恢复、仍计资产,删除=硬删除且不可逆;
                  // 二者并列,删除按钮样式保持原样不变）
                  if (isEditing) ...[
                    SizedBox(height: 12.0.scaled(context, ref)),
                    SizedBox(
                      width: double.infinity,
                      height: 48.0.scaled(context, ref),
                      child: OutlinedButton(
                        onPressed: _saving ? null : _toggleHidden,
                        style: OutlinedButton.styleFrom(
                          foregroundColor: primaryColor,
                          side: BorderSide(color: primaryColor, width: 1.5),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(
                                8.0.scaled(context, ref)),
                          ),
                        ),
                        child: Text(
                          widget.account!.hidden ? l10n.accountUnhide : l10n.accountHide,
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ),
                    SizedBox(height: 12.0.scaled(context, ref)),
                    SizedBox(
                      width: double.infinity,
                      height: 48.0.scaled(context, ref),
                      child: OutlinedButton(
                        onPressed: _saving ? null : _delete,
                        style: OutlinedButton.styleFrom(
                          foregroundColor: Colors.red,
                          side: const BorderSide(color: Colors.red, width: 1.5),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(
                                8.0.scaled(context, ref)),
                          ),
                        ),
                        child: Text(
                          l10n.commonDelete,
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  AccountEditController get _controller =>
      AccountEditController(ref.read(repositoryProvider));

  void _refreshAccountState(int accountId) {
    ref.invalidate(accountStatsProvider(accountId));
    ref.invalidate(defaultIncomeAccountIdProvider);
    ref.invalidate(defaultExpenseAccountIdProvider);
    ref.read(statsRefreshProvider.notifier).state++;
    ref.read(tagListRefreshProvider.notifier).state++;
  }

  Future<void> _save() async {
    if (_saving || !_balanceLoaded || _balanceLoadFailed) return;
    if (!_formKey.currentState!.validate()) return;
    if (_selectedType == 'credit_card' &&
        _reminderEnabled && _paymentDueDay == null) {
      showToast(context, AppLocalizations.of(context).creditCardReminderDueRequired);
      return;
    }
    setState(() => _saving = true);
    try {
      final result = await _controller.save(
        ledgerId: widget.ledgerId,
        account: widget.account,
        loadedCurrentBalance: _loadedCurrentBalance,
        values: AccountEditValues(
          name: _nameController.text,
          type: _selectedType,
          currency: _selectedCurrency,
          enteredBalance: double.tryParse(_initialBalanceController.text.trim()) ?? 0,
          creditOverpayment: _creditOverpayment,
          creditLimit: _selectedType == 'credit_card' &&
                  _creditLimitController.text.trim().isNotEmpty
              ? double.parse(_creditLimitController.text.trim()) : null,
          billingDay: _billingDay,
          paymentDueDay: _paymentDueDay,
          bankName: _bankNameController.text,
          cardLastFour: _cardLastFourController.text,
          note: _noteController.text,
          reminderEnabled: _reminderEnabled,
          reminderDaysBefore: _reminderDaysBefore,
        ),
        confirmBalanceAdjustment: (current, target) async {
          if (!mounted) return null;
          final choice = await _promptBalanceAdjustmentChoice(
            currentBalance: current,
            targetBalance: target,
            l10n: AppLocalizations.of(context),
          );
          return mounted ? choice : null;
        },
      );
      if (!mounted || result == null) return;
      _refreshAccountState(result.accountId);
      Navigator.of(context).pop(true);
    } on AccountCurrencyLocked {
      if (mounted) {
        final l10n = AppLocalizations.of(context);
        await AppDialog.info(context,
            title: l10n.commonNotice, message: l10n.accountCurrencyLocked);
      }
    } catch (e) {
      if (mounted) {
        showToast(context, '${AppLocalizations.of(context).commonError}: $e');
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Future<void> _delete() async {
    if (_saving) return;
    final l10n = AppLocalizations.of(context);
    final account = widget.account!;
    final controller = _controller;
    setState(() => _saving = true);
    try {
      final txCount = await controller.transactionCount(account.id);
      if (!mounted) return;
      final confirm = await showDialog<bool>(
        context: context,
        builder: (context) => AlertDialog(
          title: Text(txCount > 0
              ? l10n.accountDeleteWarningTitle : l10n.commonConfirm),
          content: Text(txCount > 0
              ? l10n.accountDeleteWarningMessage(txCount)
              : l10n.accountDeleteConfirm),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(false),
              child: Text(l10n.commonCancel),
            ),
            TextButton(
              onPressed: () => Navigator.of(context).pop(true),
              style: TextButton.styleFrom(foregroundColor: Colors.red),
              child: Text(l10n.commonDelete),
            ),
          ],
        ),
      );
      if (!mounted || confirm != true) return;
      await controller.delete(account.id);
      if (mounted) {
        _refreshAccountState(account.id);
        Navigator.of(context).pop(true);
      }
    } catch (e) {
      if (mounted) showToast(context, '${l10n.commonError}: $e');
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Future<void> _toggleHidden() async {
    if (_saving) return;
    final l10n = AppLocalizations.of(context);
    final account = widget.account!;
    final controller = _controller;
    setState(() => _saving = true);
    try {
      if (!account.hidden) {
        final recurringCount = await controller.recurringCount(account.id);
        if (!mounted) return;
        final confirm = await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: Text(l10n.accountHideConfirmTitle),
            content: Text(recurringCount > 0
                ? '${l10n.accountHideConfirmBody}\n${l10n.accountHideRecurringWarn(recurringCount)}'
                : l10n.accountHideConfirmBody),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: Text(l10n.commonCancel),
              ),
              TextButton(
                onPressed: () => Navigator.of(context).pop(true),
                child: Text(l10n.accountHide),
              ),
            ],
          ),
        );
        if (!mounted || confirm != true) return;
      }
      final clearedDefault = await controller.setHidden(account.id, !account.hidden);
      if (!mounted) return;
      _refreshAccountState(account.id);
      showToast(context, account.hidden
          ? l10n.accountRestoredToast
          : clearedDefault
              ? '${l10n.accountHiddenToast} · ${l10n.accountHideClearedDefault}'
              : l10n.accountHiddenToast);
      Navigator.of(context).pop(true);
    } catch (e) {
      if (mounted) showToast(context, '${l10n.commonError}: $e');
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  /// 显示币种选择器（复用账本页面的实现）
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
                      color: Colors.black12,
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
                              ? const Icon(Icons.check, color: Colors.black)
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

class _BalanceAdjustmentModeSelector extends StatefulWidget {
  final AppLocalizations l10n;
  final String difference;
  final Color differenceColor;

  const _BalanceAdjustmentModeSelector({
    required this.l10n,
    required this.difference,
    required this.differenceColor,
  });

  @override
  State<_BalanceAdjustmentModeSelector> createState() =>
      _BalanceAdjustmentModeSelectorState();
}

class _BalanceAdjustmentModeSelectorState
    extends State<_BalanceAdjustmentModeSelector> {
  bool _createTransaction = false;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(4),
          decoration: BoxDecoration(
            color: BeeTokens.surfaceCapsule(context),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Row(
            children: [
              _buildTab(
                context: context,
                createTransaction: false,
                title: widget.l10n.accountBalanceAdjustmentOnly,
              ),
              _buildTab(
                context: context,
                createTransaction: true,
                title: widget.l10n.accountBalanceAdjustmentCreate,
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),
        AnimatedSize(
          duration: const Duration(milliseconds: 200),
          curve: Curves.easeInOut,
          alignment: Alignment.topCenter,
          child: AnimatedSwitcher(
            duration: const Duration(milliseconds: 180),
            child: _buildDescription(context),
          ),
        ),
        const SizedBox(height: 18),
        SizedBox(
          width: double.infinity,
          height: 52,
          child: FilledButton.icon(
            onPressed: () => Navigator.pop(context, _createTransaction),
            icon: const Icon(Icons.check_rounded),
            label: Text(
              _createTransaction
                  ? widget.l10n.accountBalanceAdjustmentConfirmCreate
                  : widget.l10n.accountBalanceAdjustmentConfirmOnly,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildTab({
    required BuildContext context,
    required bool createTransaction,
    required String title,
  }) {
    final selected = _createTransaction == createTransaction;
    final primary = Theme.of(context).colorScheme.primary;

    return Expanded(
      child: Semantics(
        button: true,
        selected: selected,
        child: Material(
          color: Colors.transparent,
          borderRadius: BorderRadius.circular(12),
          child: InkWell(
            borderRadius: BorderRadius.circular(12),
            onTap: () => setState(
              () => _createTransaction = createTransaction,
            ),
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 160),
              height: 48,
              alignment: Alignment.center,
              padding: const EdgeInsets.symmetric(horizontal: 8),
              decoration: BoxDecoration(
                color: selected
                    ? BeeTokens.surfaceSelected(context)
                    : Colors.transparent,
                borderRadius: BorderRadius.circular(12),
                border: selected
                    ? Border.all(color: primary.withValues(alpha: 0.2))
                    : null,
              ),
              child: Text(
                title,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  color: selected
                      ? BeeTokens.textPrimary(context)
                      : BeeTokens.textSecondary(context),
                  fontSize: 13,
                  fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildDescription(BuildContext context) {
    final l10n = widget.l10n;
    final creating = _createTransaction;

    return Container(
      key: ValueKey(creating),
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: BeeTokens.surfaceSecondary(context),
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: BeeTokens.surfaceSelected(context),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(
                  creating
                      ? Icons.receipt_long_outlined
                      : Icons.account_balance_outlined,
                  color: Theme.of(context).colorScheme.primary,
                  size: 21,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  creating
                      ? l10n.accountBalanceAdjustmentCreate
                      : l10n.accountBalanceAdjustmentOnly,
                  style: TextStyle(
                    color: BeeTokens.textPrimary(context),
                    fontSize: 15,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            creating
                ? l10n.accountBalanceAdjustmentCreateMessage
                : l10n.accountBalanceAdjustmentOnlyMessage,
            style: TextStyle(
              color: BeeTokens.textSecondary(context),
              fontSize: 13,
              height: 1.4,
            ),
          ),
          if (creating) ...[
            const SizedBox(height: 12),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 9),
              decoration: BoxDecoration(
                color: BeeTokens.surface(context),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                children: [
                  Text(
                    l10n.accountBalanceDifference,
                    style: TextStyle(
                      color: BeeTokens.textSecondary(context),
                      fontSize: 12,
                    ),
                  ),
                  const Spacer(),
                  Text(
                    widget.difference,
                    style: TextStyle(
                      color: widget.differenceColor,
                      fontSize: 14,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }
}

/// 统一的 filled 圆角输入框装饰（TextField 与点击式字段框共用，保证等高同款）
InputDecoration _filledDecoration(
  BuildContext context,
  Color primary, {
  String? label,
  String? hint,
  String? prefix,
  String? errorText,
}) {
  OutlineInputBorder b(Color c, double w) => OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: w == 0 ? BorderSide.none : BorderSide(color: c, width: w),
      );
  return InputDecoration(
    labelText: label,
    hintText: hint,
    hintStyle: TextStyle(color: BeeTokens.textTertiary(context)),
    prefixText: prefix,
    errorText: errorText,
    filled: true,
    fillColor: BeeTokens.surfaceInput(context),
    isDense: true,
    floatingLabelBehavior: FloatingLabelBehavior.auto,
    contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
    border: b(Colors.transparent, 0),
    enabledBorder: b(Colors.transparent, 0),
    focusedBorder: b(primary, 1.5),
    errorBorder: b(Colors.red, 1),
    focusedErrorBorder: b(Colors.red, 1.5),
  );
}

/// 日期选择行（1-28）— filled 输入框样式，可双列并排
class _DayPickerTile extends ConsumerWidget {
  final String label;
  final int? value;
  final Color primaryColor;
  final ValueChanged<int?> onChanged;

  const _DayPickerTile({
    required this.label,
    required this.value,
    required this.primaryColor,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final hasValue = value != null;
    return InkWell(
      onTap: () => _showDayPicker(context, l10n),
      borderRadius: BorderRadius.circular(12),
      child: InputDecorator(
        decoration: _filledDecoration(context, primaryColor, label: label),
        child: Row(
          children: [
            Expanded(
              child: Text(
                hasValue ? l10n.dayOfMonth(value!) : l10n.selectDay,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  fontSize: 16,
                  color: hasValue
                      ? BeeTokens.textPrimary(context)
                      : BeeTokens.textTertiary(context),
                ),
              ),
            ),
            if (hasValue) IconButton(
              tooltip: l10n.commonRemove,
              icon: const Icon(Icons.close, size: 16),
              onPressed: () => onChanged(null),
            ),
            Icon(Icons.expand_more,
                size: 18.0.scaled(context, ref),
                color: BeeTokens.iconTertiary(context)),
          ],
        ),
      ),
    );
  }

  void _showDayPicker(BuildContext context, AppLocalizations l10n) async {
    // 先收起键盘并等其收完，避免输入框焦点残留导致选完日期后键盘又弹回来
    FocusManager.instance.primaryFocus?.unfocus();
    await Future.delayed(const Duration(milliseconds: 100));
    if (!context.mounted) return;
    await showModalBottomSheet(
      context: context,
      backgroundColor: BeeTokens.surfaceElevated(context),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (ctx) {
        return SizedBox(
          height: 320,
          child: Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  label,
                  style: Theme.of(ctx).textTheme.titleMedium,
                ),
              ),
              Expanded(
                child: GridView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 7,
                    mainAxisSpacing: 8,
                    crossAxisSpacing: 8,
                  ),
                  itemCount: 28,
                  itemBuilder: (_, index) {
                    final day = index + 1;
                    final isSelected = day == value;
                    return GestureDetector(
                      onTap: () {
                        onChanged(day);
                        Navigator.pop(ctx);
                      },
                      child: Container(
                        decoration: BoxDecoration(
                          color: isSelected
                              ? primaryColor
                              : Colors.transparent,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(
                            color: isSelected
                                ? primaryColor
                                : BeeTokens.border(ctx),
                          ),
                        ),
                        alignment: Alignment.center,
                        child: Text(
                          '$day',
                          style: TextStyle(
                            fontSize: 14,
                            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                            color: isSelected
                                ? Colors.white
                                : BeeTokens.textPrimary(ctx),
                          ),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

/// 账户类型选择卡片
class _AccountTypeCard extends ConsumerWidget {
  final String type;
  final String label;
  final bool isSelected;
  final Color primaryColor;
  final VoidCallback onTap;
  final bool disabled;

  const _AccountTypeCard({
    required this.type,
    required this.label,
    required this.isSelected,
    required this.primaryColor,
    required this.onTap,
    this.disabled = false,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // 禁用态：灰底 + 浅边 + 灰字 + 图标淡化，明确区别于可选/选中
    final Color bg = disabled
        ? BeeTokens.surfaceInput(context)
        : (isSelected
            ? primaryColor.withValues(alpha: 0.12)
            : BeeTokens.surfaceElevated(context));
    final Color borderColor = disabled
        ? BeeTokens.divider(context)
        : (isSelected ? primaryColor : BeeTokens.border(context));
    final Color fg = disabled
        ? BeeTokens.textTertiary(context)
        : (isSelected ? primaryColor : BeeTokens.textSecondary(context));
    return InkWell(
      onTap: disabled ? null : onTap,
      borderRadius: BorderRadius.circular(8.0.scaled(context, ref)),
      child: Container(
        padding: EdgeInsets.symmetric(horizontal: 2.0.scaled(context, ref)),
        decoration: BoxDecoration(
          color: bg,
          border: Border.all(
            color: borderColor,
            width: isSelected ? 2 : 1,
          ),
          borderRadius: BorderRadius.circular(8.0.scaled(context, ref)),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Opacity(
              opacity: disabled ? 0.35 : 1.0,
              child: AccountTypeIcon(
                type: type,
                size: 24.0.scaled(context, ref),
              ),
            ),
            SizedBox(height: 6.0.scaled(context, ref)),
            Text(
              label,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                fontSize: 12,
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                color: fg,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
