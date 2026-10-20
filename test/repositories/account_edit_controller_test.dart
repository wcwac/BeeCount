import 'package:drift/drift.dart' show Value;
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:beecount/controllers/account_edit_controller.dart';
import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late BeeDatabase database;
  late LocalRepository repository;
  late AccountEditController controller;
  late int ledgerId;
  final cancelledReminders = <int>[];

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    database = BeeDatabase.forTesting(NativeDatabase.memory());
    repository = LocalRepository(database);
    cancelledReminders.clear();
    controller = AccountEditController(
      repository,
      saveReminder: (_, __) async {},
      cancelReminder: (id) async { cancelledReminders.add(id); },
    );
    ledgerId = await database.into(database.ledgers).insert(
      LedgersCompanion.insert(name: '本地账本'),
    );
  });

  tearDown(() async => database.close());

  Future<Account> seedAccount({String type = 'cash'}) async {
    final id = await database.into(database.accounts).insert(
      AccountsCompanion.insert(
        ledgerId: ledgerId,
        name: '现金',
        type: Value(type),
        initialBalance: const Value(100),
      ),
    );
    return (await repository.getAccount(id))!;
  }

  AccountEditValues values({double balance = 100, String currency = 'CNY'}) =>
      AccountEditValues(name: '重命名', type: 'cash', currency: currency,
        enteredBalance: balance);

  test('metadata save preserves transactions posted after form loading', () async {
    final account = await seedAccount();
    await repository.addTransaction(ledgerId: ledgerId, type: 'expense',
      amount: 20, accountId: account.id, happenedAt: DateTime(2026, 10, 1));
    final result = await controller.save(
      ledgerId: ledgerId, account: account, loadedCurrentBalance: 100,
      values: values(),
      confirmBalanceAdjustment: (_, __) async => fail('No balance edit'),
    );
    expect(result!.balanceChanged, isFalse);
    expect(await repository.getAccountBalance(account.id), 80);
    expect((await repository.getAccount(account.id))!.name, '重命名');
  });

  test('cancelled balance adjustment makes no metadata or balance writes', () async {
    final account = await seedAccount();
    final result = await controller.save(
      ledgerId: ledgerId, account: account, loadedCurrentBalance: 100,
      values: values(balance: 150),
      confirmBalanceAdjustment: (_, __) async => null,
    );
    expect(result, isNull);
    expect((await repository.getAccount(account.id))!.name, '现金');
    expect(await repository.getAccountBalance(account.id), 100);
  });

  for (final createAdjustment in [false, true]) {
    test('balance choice $createAdjustment preserves accounting semantics', () async {
      final account = await seedAccount();
      await controller.save(
        ledgerId: ledgerId, account: account, loadedCurrentBalance: 100,
        values: values(balance: 150),
        confirmBalanceAdjustment: (current, target) async {
          expect(current, 100);
          expect(target, 150);
          return createAdjustment;
        },
      );
      expect(await repository.getAccountBalance(account.id), 150);
      expect(await repository.getTransactionCountByAccount(account.id),
        createAdjustment ? 1 : 0);
    });
  }

  test('currency changes are rejected for accounts with transactions', () async {
    final account = await seedAccount();
    await repository.addTransaction(ledgerId: ledgerId, type: 'expense',
      amount: 20, accountId: account.id, happenedAt: DateTime(2026, 10, 1));
    await expectLater(controller.save(
      ledgerId: ledgerId, account: account, loadedCurrentBalance: 100,
      values: values(currency: 'USD'),
      confirmBalanceAdjustment: (_, __) async => false,
    ), throwsA(isA<AccountCurrencyLocked>()));
    expect((await repository.getAccount(account.id))!.currency, 'CNY');
    expect((await repository.getAccount(account.id))!.name, '现金');
  });

  test('new liability balances retain signed storage convention', () async {
    for (final type in ['loan', 'credit_card']) {
      final result = await controller.save(
        ledgerId: ledgerId,
        values: AccountEditValues(name: type, type: type, currency: 'CNY',
          enteredBalance: 200),
        confirmBalanceAdjustment: (_, __) async => fail('New account'),
      );
      expect((await repository.getAccount(result!.accountId))!.initialBalance, -200);
    }
    expect(const AccountEditValues(name: 'card', type: 'credit_card',
      currency: 'CNY', enteredBalance: 200, creditOverpayment: true).balance, 200);
  });

  test('changing a credit card to cash cancels its old reminder', () async {
    final account = await seedAccount(type: 'credit_card');
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('cc_reminder_enabled_${account.id}', true);
    await prefs.setInt('cc_reminder_days_${account.id}', 3);
    await controller.save(
      ledgerId: ledgerId, account: account, loadedCurrentBalance: 100,
      values: values(),
      confirmBalanceAdjustment: (_, __) async => fail('No balance edit'),
    );
    expect((await repository.getAccount(account.id))!.type, 'cash');
    expect(cancelledReminders, [account.id]);
    expect(prefs.getBool('cc_reminder_enabled_${account.id}'), isNull);
    expect(prefs.getInt('cc_reminder_days_${account.id}'), isNull);
  });

  test('reminder failure does not turn a committed account save into failure', () async {
    controller = AccountEditController(repository,
      saveReminder: (_, __) async => throw StateError('notification unavailable'),
      cancelReminder: (_) async {},
    );
    final result = await controller.save(
      ledgerId: ledgerId,
      values: const AccountEditValues(name: '信用卡', type: 'credit_card',
        currency: 'CNY', enteredBalance: 200),
      confirmBalanceAdjustment: (_, __) async => fail('New account'),
    );
    expect(result, isNotNull);
    expect((await repository.getAccount(result!.accountId))!.initialBalance, -200);
    expect((await repository.getAllAccounts()).length, 1);
  });

  test('hide clears only matching defaults and restore preserves balances', () async {
    final account = await seedAccount();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt('default_income_account_id', account.id);
    await prefs.setInt('default_expense_account_id', account.id + 100);
    expect(await controller.setHidden(account.id, true), isTrue);
    expect(prefs.getInt('default_income_account_id'), isNull);
    expect(prefs.getInt('default_expense_account_id'), account.id + 100);
    expect(await repository.getAccountBalance(account.id), 100);
    expect(await controller.setHidden(account.id, false), isFalse);
    expect((await repository.getAccount(account.id))!.hidden, isFalse);
  });

  test('delete clears obsolete reminder and default account settings', () async {
    final account = await seedAccount(type: 'credit_card');
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('cc_reminder_enabled_${account.id}', true);
    await prefs.setInt('cc_reminder_days_${account.id}', 3);
    await prefs.setInt('default_income_account_id', account.id);
    await controller.delete(account.id);
    expect(await repository.getAccount(account.id), isNull);
    expect(cancelledReminders, [account.id]);
    expect(prefs.getBool('cc_reminder_enabled_${account.id}'), isNull);
    expect(prefs.getInt('cc_reminder_days_${account.id}'), isNull);
    expect(prefs.getInt('default_income_account_id'), isNull);
  });
}
