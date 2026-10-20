import 'package:flutter/material.dart';
import '../../models/finance.dart';

/// Copy is kept together so finance pages do not hard-code a language.
class FinanceText {
  FinanceText(BuildContext context)
      : chinese = Localizations.localeOf(context).languageCode == 'zh';
  final bool chinese;
  String t(String zh, String en) => chinese ? zh : en;
  String get title => t('财务管理', 'Finance');
  String get subtitle =>
      t('报销、借贷、分期房贷与理财', 'Reimbursements, loans, instalments and investments');
  String kind(FinanceKind kind) => switch (kind) {
        FinanceKind.reimbursement => t('报销', 'Reimbursement'),
        FinanceKind.lend => t('借出', 'Lent money'),
        FinanceKind.borrow => t('借入', 'Borrowed money'),
        FinanceKind.installment => t('分期', 'Instalment'),
        FinanceKind.mortgage => t('房贷', 'Mortgage'),
        FinanceKind.investment => t('理财', 'Investment'),
      };
  String method(RepaymentMethod method) => method == RepaymentMethod.annuity
      ? t('等额本息（每期总额相同）', 'Equal payments')
      : t('等额本金（每月固定本金）', 'Equal principal each month');
  String action(FinancePlan plan, FinanceAction action) => switch (action) {
        FinanceAction.settle => plan.kind == FinanceKind.reimbursement
            ? t('登记回款', 'Record reimbursement')
            : plan.kind == FinanceKind.lend
                ? t('收回本金与利息', 'Receive principal and interest')
                : t('偿还本金与利息', 'Pay principal and interest'),
        FinanceAction.buy => t('买入', 'Buy'),
        FinanceAction.sell => t('卖出', 'Sell'),
        FinanceAction.dividend => t('现金分红', 'Cash dividend'),
        FinanceAction.valuation => t('更新总市值', 'Update market value'),
      };
  String event(String action) => switch (action) {
        'opening' => t('建立记录', 'Opened'),
        'settle' => t('本金结算', 'Principal settlement'),
        'buy' => t('买入', 'Bought'),
        'sell' => t('卖出', 'Sold'),
        'dividend' => t('现金分红', 'Cash dividend'),
        'valuation' => t('估值', 'Valuation'),
        _ => action,
      };
  String error(Object error) {
    if (error is! FinanceFailure)
      return t('操作失败，请重试。', 'Operation failed. Please try again.');
    return switch (error.code) {
      'invalid_amount' =>
        t('请输入有效金额，最多两位小数。', 'Enter a valid amount with at most two decimals.'),
      'invalid_units' =>
        t('请输入有效份额，最多六位小数。', 'Enter valid units with at most six decimals.'),
      'invalid_terms' => t('请检查利率、期数与还款方式。',
          'Check the interest rate, term and repayment method.'),
      'invalid_date' => t('发生日期不能晚于今天，也不能早于建立日期或上一笔记录。',
          'The event date cannot be in the future or precede opening or the previous event.'),
      'missing_account' => t('请选择资金账户。', 'Select a cash account.'),
      'invalid_account' => t('该账户不可用于此次操作，请重新选择。',
          'This account cannot be used. Choose another one.'),
      'currency_mismatch' =>
        t('资金账户和记录的币种必须一致。', 'Account and plan currencies must match.'),
      'missing_plan' => t('此记录已不存在。', 'This record no longer exists.'),
      'closed' => t('此记录已结清。', 'This record is closed.'),
      'overpayment' => t('本金或卖出份额不能超过当前剩余量。',
          'Principal or units cannot exceed the outstanding amount.'),
      'invalid_period' => t('请按顺序还款，并使用计划中的本金和利息。',
          'Pay instalments in order using the scheduled amounts.'),
      'invalid_action' =>
        t('此记录不支持该操作。', 'This action is unavailable for this record.'),
      'linked_transaction' => t('该支出已被关联，或不符合关联条件。',
          'This expense is already linked or is not eligible.'),
      'managed_account' =>
        t('财务专用账户请在财务管理中操作。', 'Manage this account through Finance.'),
      'cannot_undo_opening' => t('建立记录不能撤销；可删除整个记录。',
          'The opening event cannot be undone; delete the record instead.'),
      'duplicate' => t('该操作已经记录，请刷新查看。',
          'This operation is already recorded. Refresh to view it.'),
      _ => t('操作未完成，请检查输入后重试。',
          'The operation was not completed. Check the inputs and retry.'),
    };
  }
}

String financeMoney(int cents, String currency) {
  final absolute = cents.abs();
  return '$currency ${cents < 0 ? '-' : ''}${absolute ~/ 100}.${(absolute % 100).toString().padLeft(2, '0')}';
}

String financeDecimal(int value, int places) {
  final digits = value.abs().toString().padLeft(places + 1, '0');
  final integer = digits.substring(0, digits.length - places);
  final fraction =
      digits.substring(digits.length - places).replaceFirst(RegExp(r'0+$'), '');
  return '${value < 0 ? '-' : ''}$integer${fraction.isEmpty ? '' : '.$fraction'}';
}

int? parseFinanceDecimal(String raw, int places) {
  final value = raw.trim();
  if (!RegExp('^[0-9]+(?:\\.[0-9]{0,$places})?\$').hasMatch(value)) return null;
  final parts = value.split('.');
  final digits =
      '${parts.first}${(parts.length == 1 ? '' : parts[1]).padRight(places, '0')}';
  return int.tryParse(digits);
}

String financeDate(DateTime date) =>
    '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
