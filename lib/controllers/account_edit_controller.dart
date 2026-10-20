import 'package:shared_preferences/shared_preferences.dart';

import '../data/db.dart' as db;
import '../data/repositories/base_repository.dart';
import '../providers/credit_card_reminder_providers.dart';
import '../services/system/logger_service.dart';
import '../utils/account_type_utils.dart';

/// Form values, independent of text fields and widget lifecycle.
class AccountEditValues {
  const AccountEditValues({
    required this.name,
    required this.type,
    required this.currency,
    required this.enteredBalance,
    this.creditOverpayment = false,
    this.creditLimit,
    this.billingDay,
    this.paymentDueDay,
    this.bankName = '',
    this.cardLastFour = '',
    this.note = '',
    this.reminderEnabled = false,
    this.reminderDaysBefore = 3,
  });

  final String name;
  final String type;
  final String currency;
  final double enteredBalance;
  final bool creditOverpayment;
  final double? creditLimit;
  final int? billingDay;
  final int? paymentDueDay;
  final String bankName;
  final String cardLastFour;
  final String note;
  final bool reminderEnabled;
  final int reminderDaysBefore;

  double get balance {
    if (type == 'credit_card') {
      return creditOverpayment ? enteredBalance : -enteredBalance;
    }
    return type == 'loan' ? -enteredBalance.abs() : enteredBalance;
  }
}

class AccountCurrencyLocked implements Exception {
  const AccountCurrencyLocked();
}

/// Coordinates account writes. Confirmation and navigation stay with the view.
class AccountEditController {
  AccountEditController(
    this.repository, {
    Future<void> Function(int, AccountEditValues)? saveReminder,
    Future<void> Function(int)? cancelReminder,
  }) : _saveReminder = saveReminder ?? _persistReminder,
       _cancelReminder = cancelReminder ?? CreditCardReminderService.cancelReminder;

  final BaseRepository repository;
  final Future<void> Function(int, AccountEditValues) _saveReminder;
  final Future<void> Function(int) _cancelReminder;

  Future<({int accountId, bool balanceChanged})?> save({
    required int ledgerId,
    required AccountEditValues values,
    db.Account? account,
    double? loadedCurrentBalance,
    required Future<bool?> Function(double current, double target)
        confirmBalanceAdjustment,
  }) async {
    if (!values.enteredBalance.isFinite ||
        (values.type == 'credit_card' && values.enteredBalance < 0)) {
      throw ArgumentError.value(values.enteredBalance, 'enteredBalance');
    }
    final isCredit = values.type == 'credit_card';
    final isBank = isCredit || values.type == 'bank_card';
    final targetBalance = values.balance;
    var balanceChanged = false;
    var createAdjustment = false;
    late final int accountId;

    if (account != null) {
      final currencyChanged = values.currency != account.currency;
      if (currencyChanged && await repository.hasTransactions(account.id)) {
        throw const AccountCurrencyLocked();
      }
      if (!isValuationOnlyType(account.type) &&
          !isValuationOnlyType(values.type)) {
        final current = await repository.getAccountBalance(account.id);
        // A metadata-only save must preserve transactions added since loading.
        if ((targetBalance - (loadedCurrentBalance ?? current)).abs() >=
            0.0000001) {
          final choice = await confirmBalanceAdjustment(current, targetBalance);
          if (choice == null) return null;
          balanceChanged = true;
          createAdjustment = choice;
        }
      }
      final wasBank = account.type == 'bank_card' ||
          account.type == 'credit_card';
      await repository.updateAccount(
        account.id,
        name: values.name.trim(),
        type: values.type,
        currency: currencyChanged ? values.currency : null,
        creditLimit: isCredit ? values.creditLimit : null,
        billingDay: isCredit ? values.billingDay : null,
        paymentDueDay: isCredit ? values.paymentDueDay : null,
        clearCreditCardFields: account.type == 'credit_card',
        bankName: isBank ? _nonEmpty(values.bankName) : null,
        cardLastFour: isBank ? _nonEmpty(values.cardLastFour) : null,
        note: _nonEmpty(values.note),
        clearMetadataFields: wasBank && !isBank,
      );
      if (balanceChanged) {
        await repository.setAccountBalance(
          ledgerId: ledgerId,
          accountId: account.id,
          targetBalance: targetBalance,
          createBalanceAdjustment: createAdjustment,
        );
      }
      accountId = account.id;
    } else {
      accountId = await repository.createAccount(
        ledgerId: ledgerId,
        name: values.name.trim(),
        type: values.type,
        currency: values.currency,
        initialBalance: targetBalance,
        creditLimit: isCredit ? values.creditLimit : null,
        billingDay: isCredit ? values.billingDay : null,
        paymentDueDay: isCredit ? values.paymentDueDay : null,
        bankName: isBank ? _nonEmpty(values.bankName) : null,
        cardLastFour: isBank ? _nonEmpty(values.cardLastFour) : null,
        note: _nonEmpty(values.note),
      );
    }
    // The account is already committed. A notification/preferences failure must
    // not invite the user to retry creation and create a duplicate account.
    await _completeSettings(() async {
      if (isCredit) {
        await _saveReminder(accountId, values);
      } else if (account?.type == 'credit_card') {
        await _cancelReminder(accountId);
        final prefs = await SharedPreferences.getInstance();
        await prefs.remove('cc_reminder_enabled_$accountId');
        await prefs.remove('cc_reminder_days_$accountId');
      }
    });
    return (accountId: accountId, balanceChanged: balanceChanged);
  }

  Future<int> transactionCount(int accountId) =>
      repository.getTransactionCountByAccount(accountId);

  Future<int> recurringCount(int accountId) =>
      repository.getActiveRecurringCountByAccount(accountId);

  Future<void> delete(int accountId) async {
    await repository.deleteAccount(accountId);
    await _completeSettings(() async {
      await _cancelReminder(accountId);
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove('cc_reminder_enabled_$accountId');
      await prefs.remove('cc_reminder_days_$accountId');
      await _clearDefaults(prefs, accountId);
    });
  }

  /// Returns whether hiding cleared a default income/expense account.
  Future<bool> setHidden(int accountId, bool hidden) async {
    await repository.setAccountHidden(accountId, hidden);
    if (!hidden) return false;
    var cleared = false;
    await _completeSettings(() async {
      cleared = await _clearDefaults(await SharedPreferences.getInstance(), accountId);
    });
    return cleared;
  }

  static Future<void> _completeSettings(Future<void> Function() action) async {
    try {
      await action();
    } catch (error, stack) {
      logger.error('AccountEdit',
          '账户已保存，但提醒或默认账户设置更新失败', error, stack);
    }
  }

  static Future<bool> _clearDefaults(
      SharedPreferences prefs, int accountId) async {
    var cleared = false;
    for (final key in ['default_income_account_id', 'default_expense_account_id']) {
      if (prefs.getInt(key) == accountId) {
        await prefs.remove(key);
        cleared = true;
      }
    }
    return cleared;
  }

  static String? _nonEmpty(String text) =>
      text.trim().isEmpty ? null : text.trim();

  static Future<void> _persistReminder(
      int accountId, AccountEditValues values) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('cc_reminder_enabled_$accountId', values.reminderEnabled);
    await prefs.setInt('cc_reminder_days_$accountId', values.reminderDaysBefore);
    if (values.reminderEnabled && values.paymentDueDay != null) {
      await CreditCardReminderService.scheduleReminder(
        accountId: accountId,
        accountName: values.name.trim(),
        paymentDueDay: values.paymentDueDay!,
        daysBefore: values.reminderDaysBefore,
      );
    } else {
      await CreditCardReminderService.cancelReminder(accountId);
    }
  }
}
