import 'package:drift/drift.dart' as d;
import 'package:uuid/uuid.dart';

import '../../db.dart';
import '../../../models/finance.dart';
import '../../../services/system/logger_service.dart';
import '../../../utils/account_type_utils.dart';
import '../../../utils/app_localizations_resolver.dart';
import '../account_repository.dart';
import '../exceptions.dart';

/// 本地账户Repository实现
/// 基于 Drift 数据库实现
class LocalAccountRepository implements AccountRepository {
  static const _uuid = Uuid();
  final BeeDatabase db;

  LocalAccountRepository(this.db);

  Future<bool> _isManaged(int accountId) async =>
      (await db.customSelect(
        'SELECT 1 FROM finance_plans WHERE account_id = ? LIMIT 1',
        variables: [d.Variable<int>(accountId)],
      ).get()).isNotEmpty;

  Future<void> _requireOrdinary(int accountId) async {
    if (await _isManaged(accountId)) {
      throw const FinanceFailure('managed_account');
    }
  }

  @override
  Stream<List<Account>> watchAccountsForLedger(int ledgerId) {
    return (db.select(db.accounts)
          ..where((a) => a.ledgerId.equals(ledgerId)))
        .watch();
  }

  @override
  Stream<List<Account>> watchAllAccounts() {
    return (db.select(db.accounts)
          ..orderBy([
            (a) => d.OrderingTerm(expression: a.type),
            (a) => d.OrderingTerm(expression: a.sortOrder),
          ]))
        .watch();
  }

  @override
  Future<List<Account>> getAllAccounts() async {
    return await (db.select(db.accounts)
          ..orderBy([
            (a) => d.OrderingTerm(expression: a.type),
            (a) => d.OrderingTerm(expression: a.sortOrder),
          ]))
        .get();
  }

  @override
  Future<Account?> getAccount(int accountId) async {
    return await (db.select(db.accounts)
          ..where((a) => a.id.equals(accountId)))
        .getSingleOrNull();
  }

  @override
  Future<List<Account>> getAvailableAccountsForLedger(int ledgerId) async {
    // 获取账本信息
    final ledger = await (db.select(db.ledgers)
          ..where((l) => l.id.equals(ledgerId)))
        .getSingle();

    // 通过币种过滤账户
    return await (db.select(db.accounts)
          ..where((a) => a.currency.equals(ledger.currency)))
        .get();
  }

  @override
  Future<List<Account>> getAccountsByCurrency(String currency) async {
    return await (db.select(db.accounts)
          ..where((a) => a.currency.equals(currency)))
        .get();
  }

  @override
  Future<Map<String, List<Account>>> getAccountsGroupedByCurrency() async {
    final allAccounts = await getAllAccounts();
    final Map<String, List<Account>> grouped = {};

    for (final account in allAccounts) {
      grouped.putIfAbsent(account.currency, () => []).add(account);
    }

    return grouped;
  }

  @override
  Future<int> createAccount({
    required int ledgerId,
    required String name,
    String type = 'cash',
    String currency = 'CNY',
    double initialBalance = 0.0,
    double? creditLimit,
    int? billingDay,
    int? paymentDueDay,
    String? bankName,
    String? cardLastFour,
    String? note,
    String? syncId,
  }) async {
    if (financeAccountTypes.contains(type)) {
      throw const FinanceFailure('managed_account');
    }
    // 撞同名抛 DuplicateNameException(name 全局唯一)。静默路径(import /
    // app-link 等)请改用 [upsertAccount]。
    final existingByName =
        await (db.select(db.accounts)..where((a) => a.name.equals(name))).get();
    if (existingByName.isNotEmpty) {
      throw DuplicateNameException(
        entityType: 'account',
        name: name,
        existingId: existingByName.first.id,
      );
    }
    try {
      // 计算同类型最大 sortOrder + 1
      final maxSortOrderResult = await db.customSelect(
        'SELECT COALESCE(MAX(sort_order), -1) AS max_order FROM accounts WHERE type = ?1',
        variables: [d.Variable.withString(type)],
        readsFrom: {db.accounts},
      ).getSingle();
      final nextSortOrder = (maxSortOrderResult.data['max_order'] as int) + 1;

      final companion = AccountsCompanion.insert(
        ledgerId: ledgerId,
        name: name,
        type: d.Value(type),
        currency: d.Value(currency),
        initialBalance: d.Value(initialBalance),
        createdAt: d.Value(DateTime.now()),
        sortOrder: d.Value(nextSortOrder),
        creditLimit: d.Value(creditLimit),
        billingDay: d.Value(billingDay),
        paymentDueDay: d.Value(paymentDueDay),
        bankName: d.Value(bankName),
        cardLastFour: d.Value(cardLastFour),
        note: d.Value(note),
        syncId: d.Value(syncId ?? _uuid.v4()),
      );

      final id = await db.into(db.accounts).insert(companion);
      // 单条 INFO 日志(import 批量场景下 3 条/账户 会把 logger 队列冲爆,降级
      // 到 debug;只在出错时 error)
      logger.debug('AccountCreate', '账户创建: id=$id name=$name type=$type');
      return id;
    } catch (e, stack) {
      logger.error('AccountCreate', '创建账户失败 name=$name', e, stack);
      rethrow;
    }
  }

  @override
  Future<int> upsertAccount({
    required String name,
    int ledgerId = 0,
    String type = 'cash',
    String currency = 'CNY',
    double initialBalance = 0.0,
  }) async {
    final existing = await (db.select(db.accounts)
          ..where((a) => a.name.equals(name)))
        .get();
    if (existing.isNotEmpty) return existing.first.id;
    // 复用 createAccount(此时 name 不冲突,不会抛)
    return createAccount(
      ledgerId: ledgerId,
      name: name,
      type: type,
      currency: currency,
      initialBalance: initialBalance,
    );
  }

  @override
  Future<void> updateAccount(
    int id, {
    String? name,
    String? type,
    String? currency,
    double? initialBalance,
    double? creditLimit,
    int? billingDay,
    int? paymentDueDay,
    bool clearCreditCardFields = false,
    String? bankName,
    String? cardLastFour,
    String? note,
    bool clearMetadataFields = false,
    bool? hidden,
  }) async {
    if (initialBalance != null || type != null || currency != null) {
      if (await _isManaged(id)) {
        final current = await getAccount(id);
        if (initialBalance != null ||
            (type != null && type != current?.type) ||
            (currency != null && currency != current?.currency)) {
          throw const FinanceFailure('managed_account');
        }
      } else if (type != null && financeAccountTypes.contains(type)) {
        throw const FinanceFailure('managed_account');
      }
    }

    await (db.update(db.accounts)..where((a) => a.id.equals(id))).write(
      AccountsCompanion(
        name: name != null ? d.Value(name) : const d.Value.absent(),
        type: type != null ? d.Value(type) : const d.Value.absent(),
        currency: currency != null ? d.Value(currency) : const d.Value.absent(),
        initialBalance: initialBalance != null ? d.Value(initialBalance) : const d.Value.absent(),
        creditLimit: clearCreditCardFields ? d.Value(creditLimit) : (creditLimit != null ? d.Value(creditLimit) : const d.Value.absent()),
        billingDay: clearCreditCardFields ? d.Value(billingDay) : (billingDay != null ? d.Value(billingDay) : const d.Value.absent()),
        paymentDueDay: clearCreditCardFields ? d.Value(paymentDueDay) : (paymentDueDay != null ? d.Value(paymentDueDay) : const d.Value.absent()),
        bankName: clearMetadataFields ? const d.Value(null) : (bankName != null ? d.Value(bankName) : const d.Value.absent()),
        cardLastFour: clearMetadataFields ? const d.Value(null) : (cardLastFour != null ? d.Value(cardLastFour) : const d.Value.absent()),
        note: clearMetadataFields ? const d.Value(null) : (note != null ? d.Value(note) : const d.Value.absent()),
        hidden: hidden == null ? const d.Value.absent() : d.Value(hidden),
      ),
    );
  }

  @override
  Future<void> setAccountHidden(int id, bool hidden) =>
      updateAccount(id, hidden: hidden);

  @override
  Future<List<Account>> getCreditCardAccounts() async {
    return await (db.select(db.accounts)
          ..where((a) => a.type.equals('credit_card'))
          ..orderBy([(a) => d.OrderingTerm(expression: a.sortOrder)]))
        .get();
  }

  @override
  Future<double> getCreditCardUsedAmount(int accountId) async {
    // 已用额度 = -balance（余额为负表示欠款）
    final balance = await getAccountBalance(accountId);
    return balance < 0 ? -balance : 0.0;
  }

  @override
  Future<void> deleteAccount(int id) async {
    await _requireOrdinary(id);
    await (db.delete(db.accounts)..where((a) => a.id.equals(id))).go();
  }

  @override
  Future<double> getAccountBalance(int accountId) async {
    // 获取账户初始资金
    final account = await (db.select(db.accounts)
          ..where((a) => a.id.equals(accountId)))
        .getSingleOrNull();

    if (account == null) return 0.0;

    // 估值账户直接返回 initialBalance 作为当前估值
    if (isValuationOnlyType(account.type)) {
      return account.initialBalance;
    }

    double balance = account.initialBalance;

    // 收入和支出
    final normalTxs = await (db.select(db.transactions)
          ..where((t) =>
              t.accountId.equals(accountId)))
        .get();

    for (final t in normalTxs) {
      if (t.type == 'income') {
        balance += t.amount;
      } else if (t.type == 'expense') {
        balance -= t.amount;
      } else if (t.type == 'transfer') {
        // 作为转出账户
        balance -= t.amount;
      }
    }

    // 作为转入账户的转账
    final transfersIn = await (db.select(db.transactions)
          ..where((t) =>
              t.toAccountId.equals(accountId) &
              t.type.equals('transfer')))
        .get();

    for (final t in transfersIn) {
      balance += t.amount;
    }

    return balance;
  }

  @override
  Future<double> getAccountGlobalBalance(int accountId) async {
    final account = await (db.select(db.accounts)
          ..where((a) => a.id.equals(accountId)))
        .getSingle();

    // 估值账户直接返回 initialBalance
    if (isValuationOnlyType(account.type)) {
      return account.initialBalance;
    }

    // 获取所有交易
    final transactions = await (db.select(db.transactions)
          ..where((t) =>
              (t.accountId.equals(accountId) | t.toAccountId.equals(accountId))))
        .get();

    double balance = account.initialBalance;

    for (final tx in transactions) {
      if (tx.accountId == accountId) {
        // 作为主账户
        if (tx.type == 'income') {
          balance += tx.amount;
        } else if (tx.type == 'expense') {
          balance -= tx.amount;
        } else if (tx.type == 'transfer') {
          balance -= tx.amount;
        }
      } else if (tx.toAccountId == accountId) {
        // 作为转入账户（转账）
        balance += tx.amount;
      }
    }

    return balance;
  }

  @override
  Future<double> getAccountBalanceInLedger(int accountId, int ledgerId) async {
    final transactions = await (db.select(db.transactions)
          ..where((t) =>
              (t.accountId.equals(accountId) | t.toAccountId.equals(accountId)) &
              t.ledgerId.equals(ledgerId)))
        .get();

    double balance = 0.0;

    for (final tx in transactions) {
      if (tx.accountId == accountId) {
        // 作为主账户
        if (tx.type == 'income') {
          balance += tx.amount;
        } else if (tx.type == 'expense') {
          balance -= tx.amount;
        } else if (tx.type == 'transfer') {
          balance -= tx.amount;
        }
      } else if (tx.toAccountId == accountId) {
        // 作为转入账户（转账）
        balance += tx.amount;
      }
    }

    return balance;
  }

  @override
  Future<Map<int, double>> getAllAccountBalances(int ledgerId) async {
    final accounts = await (db.select(db.accounts)
          ..where((a) => a.ledgerId.equals(ledgerId)))
        .get();

    final Map<int, double> balances = {};
    for (final account in accounts) {
      balances[account.id] = await getAccountBalance(account.id);
    }

    return balances;
  }

  @override
  Future<int> getTransactionCountByAccount(int accountId) async {
    // 统计作为主账户的交易数
    final mainCount = await db.customSelect(
      'SELECT COUNT(*) AS count FROM transactions WHERE account_id = ?1',
      variables: [d.Variable.withInt(accountId)],
      readsFrom: {db.transactions},
    ).getSingle();

    // 统计作为转入账户的交易数
    final toCount = await db.customSelect(
      'SELECT COUNT(*) AS count FROM transactions WHERE to_account_id = ?1',
      variables: [d.Variable.withInt(accountId)],
      readsFrom: {db.transactions},
    ).getSingle();

    int parseCount(dynamic v) {
      if (v is int) return v;
      if (v is BigInt) return v.toInt();
      if (v is num) return v.toInt();
      return 0;
    }

    return parseCount(mainCount.data['count']) + parseCount(toCount.data['count']);
  }

  @override
  Future<double> getAccountExpense(int accountId) async {
    double expense = 0.0;

    // 获取作为主账户的支出和转出(不计入收支的交易排除)
    final normalTxs = await (db.select(db.transactions)
          ..where((t) =>
              t.accountId.equals(accountId) &
              t.excludeFromStats.equals(false)))
        .get();

    for (final t in normalTxs) {
      if (t.type == 'expense') {
        expense += t.amount;
      } else if (t.type == 'transfer') {
        // 作为转出账户
        expense += t.amount;
      }
    }

    return expense;
  }

  @override
  Future<double> getAccountIncome(int accountId) async {
    double income = 0.0;

    // 获取作为主账户的收入(不计入收支的交易排除)
    final normalTxs = await (db.select(db.transactions)
          ..where((t) =>
              t.accountId.equals(accountId) &
              t.excludeFromStats.equals(false)))
        .get();

    for (final t in normalTxs) {
      if (t.type == 'income') {
        income += t.amount;
      }
    }

    // 作为转入账户的转账(不计入收支的交易排除)
    final transfersIn = await (db.select(db.transactions)
          ..where((t) =>
              t.toAccountId.equals(accountId) &
              t.type.equals('transfer') &
              t.excludeFromStats.equals(false)))
        .get();

    for (final t in transfersIn) {
      income += t.amount;
    }

    return income;
  }

  @override
  Future<({double balance, double expense, double income})> getAccountStats(int accountId) async {
    final balance = await getAccountBalance(accountId);
    final expense = await getAccountExpense(accountId);
    final income = await getAccountIncome(accountId);
    return (balance: balance, expense: expense, income: income);
  }

  @override
  Future<Map<int, ({double balance, double expense, double income})>> getAllAccountStats() async {
    final accounts = await db.select(db.accounts).get();

    final Map<int, ({double balance, double expense, double income})> stats = {};
    for (final account in accounts) {
      stats[account.id] = await getAccountStats(account.id);
    }

    return stats;
  }

  @override
  /// ⚠️ 多币种口径未处理:本方法跨所有账本/账户按 type 裸加 amount。当前
  /// 无 UI 消费(allAccountsTotalStatsProvider 是死代码),故不影响任何界面。
  /// 若将来接「全局总收支」卡片:这是跨账本汇总,正确口径是按各账户币种
  /// rate 折算到用户主币种(同净值卡 convertedNetWorth),**不是** nativeAmount
  /// (各账本本位币可能不同,nativeAmount 相加无意义)。届时须重写,勿直接
  /// 套账本维度的 nativeAmount 折算。
  Future<({double totalBalance, double totalExpense, double totalIncome})> getAllAccountsTotalStats() async {
    final accounts = await db.select(db.accounts).get();

    // 总余额 = 所有账户余额之和（转账不影响总余额）
    double totalBalance = 0.0;
    for (final account in accounts) {
      final balance = await getAccountBalance(account.id);
      totalBalance += balance;
    }

    // 总收入/支出：直接从交易表查询，排除转账类型
    final accountIds = accounts.map((a) => a.id).toSet();

    // 收入/支出排除不计入收支的交易(余额不受影响,见上方 totalBalance)
    final allTxs = await (db.select(db.transactions)
          ..where((t) =>
              t.accountId.isNotNull() &
              t.excludeFromStats.equals(false)))
        .get();

    double totalIncome = 0.0;
    double totalExpense = 0.0;

    for (final t in allTxs) {
      // 只统计属于已有账户的交易
      if (t.accountId != null && accountIds.contains(t.accountId)) {
        if (t.type == 'income') {
          totalIncome += t.amount;
        } else if (t.type == 'expense') {
          totalExpense += t.amount;
        }
        // 转账类型不计入总收入/支出
      }
    }

    return (totalBalance: totalBalance, totalExpense: totalExpense, totalIncome: totalIncome);
  }

  @override
  Future<Map<int, int>> getAccountUsageInLedgers(int accountId) async {
    final result = await db.customSelect(
      '''
      SELECT ledger_id, COUNT(*) as count
      FROM transactions
      WHERE account_id = ? OR to_account_id = ?
      GROUP BY ledger_id
      ''',
      variables: [d.Variable.withInt(accountId), d.Variable.withInt(accountId)],
      readsFrom: {db.transactions},
    ).get();

    final Map<int, int> usage = {};
    for (final row in result) {
      final ledgerId = row.data['ledger_id'] as int;
      final count = row.data['count'];

      int countInt = 0;
      if (count is int) {
        countInt = count;
      } else if (count is BigInt) {
        countInt = count.toInt();
      } else if (count is num) {
        countInt = count.toInt();
      }

      usage[ledgerId] = countInt;
    }

    return usage;
  }

  @override
  Future<int> migrateAccount({
    required int fromAccountId,
    required int toAccountId,
  }) async {
    return db.transaction(() async {

      await _requireOrdinary(fromAccountId);
      await _requireOrdinary(toAccountId);
      final linked = await db.customSelect(
        'SELECT 1 FROM finance_transaction_links l JOIN transactions t '
        'ON t.id=l.transaction_id WHERE t.account_id=? OR t.to_account_id=? LIMIT 1',
        variables: [d.Variable<int>(fromAccountId), d.Variable<int>(fromAccountId)],
      ).get();
      if (linked.isNotEmpty) throw const FinanceFailure('linked_transaction');

      final beforeCount = await getTransactionCountByAccount(fromAccountId);

      // 迁移作为主账户的交易
      await (db.update(db.transactions)
            ..where((t) => t.accountId.equals(fromAccountId)))
          .write(TransactionsCompanion(accountId: d.Value(toAccountId)));

      // 迁移作为转入账户的交易
      await (db.update(db.transactions)
            ..where((t) => t.toAccountId.equals(fromAccountId)))
          .write(TransactionsCompanion(toAccountId: d.Value(toAccountId)));

      return beforeCount;
    });
  }

  @override
  Future<bool> hasTransactions(int accountId) async {
    final count = await db.customSelect(
      'SELECT COUNT(*) as count FROM transactions WHERE account_id = ? OR to_account_id = ?',
      variables: [d.Variable.withInt(accountId), d.Variable.withInt(accountId)],
      readsFrom: {db.transactions},
    ).getSingle();

    final c = count.data['count'];
    if (c is int) return c > 0;
    if (c is BigInt) return c > BigInt.zero;
    if (c is num) return c > 0;
    return false;
  }

  @override
  Stream<Account?> watchAccount(int accountId) {
    return (db.select(db.accounts)..where((a) => a.id.equals(accountId)))
        .watchSingleOrNull();
  }

  @override
  Stream<List<Transaction>> watchAccountTransactions(int accountId) {
    return (db.select(db.transactions)
          ..where((t) => t.accountId.equals(accountId) | t.toAccountId.equals(accountId))
          ..orderBy([
            (t) => d.OrderingTerm(
                expression: t.happenedAt, mode: d.OrderingMode.desc)
          ]))
        .watch();
  }

  @override
  Future<void> batchInsertAccounts(List<AccountsCompanion> accounts) async {
    if (accounts.any((a) => a.type.present && financeAccountTypes.contains(a.type.value))) {
      throw const FinanceFailure('managed_account');
    }
    await db.batch((batch) {
      batch.insertAll(db.accounts, accounts);
    });
  }

  @override
  Future<List<Account>> getAccountsByIds(List<int> accountIds) async {
    if (accountIds.isEmpty) return [];
    return await (db.select(db.accounts)
          ..where((a) => a.id.isIn(accountIds)))
        .get();
  }

  @override
  Future<void> updateAccountSortOrders(
      List<({int id, int sortOrder})> updates) async {
    await db.transaction(() async {
      for (final update in updates) {
        await (db.update(db.accounts)..where((a) => a.id.equals(update.id)))
            .write(AccountsCompanion(sortOrder: d.Value(update.sortOrder)));
      }
    });
  }

  @override
  Future<List<Transaction>> getAccountTransactions(
    int accountId, {int limit = 50, int offset = 0, String? flow}) async {
    // flow 过滤按资金流向:支出视图含转出,收入视图含转入,null 为全部
    final where = switch (flow) {
      'expense' => "account_id = ?1 AND type IN ('expense', 'transfer')",
      'income' =>
        "(type = 'income' AND account_id = ?1) OR (type = 'transfer' AND to_account_id = ?1)",
      _ => 'account_id = ?1 OR to_account_id = ?1',
    };
    final results = await db.customSelect(
      '''
      SELECT * FROM transactions
      WHERE ($where)
      ORDER BY happened_at DESC
      LIMIT ?2 OFFSET ?3
      ''',
      variables: [
        d.Variable.withInt(accountId),
        d.Variable.withInt(limit),
        d.Variable.withInt(offset),
      ],
      readsFrom: {db.transactions},
    ).get();

    return results.map((row) {
      return Transaction(
        id: row.data['id'] as int,
        ledgerId: row.data['ledger_id'] as int,
        type: row.data['type'] as String,
        amount: (row.data['amount'] as num).toDouble(),
        categoryId: row.data['category_id'] as int?,
        accountId: row.data['account_id'] as int?,
        toAccountId: row.data['to_account_id'] as int?,
        happenedAt: DateTime.fromMillisecondsSinceEpoch(
            (row.data['happened_at'] as int) * 1000),
        note: row.data['note'] as String?,
        recurringId: row.data['recurring_id'] as int?,
        syncId: row.data['sync_id'] as String?,
        excludeFromStats: (row.data['exclude_from_stats'] as int? ?? 0) != 0,
        excludeFromBudget: (row.data['exclude_from_budget'] as int? ?? 0) != 0,
      );
    }).toList();
  }

  @override
  Future<List<({DateTime date, double balance})>> getAccountDailyBalances(
    int accountId, {required DateTime startDate, required DateTime endDate}) async {
    final account = await getAccount(accountId);
    if (account == null) return [];

    // Financial positions are event-sourced: later valuations must not rewrite
    // earlier net-worth history. The current account balance is only a mirror.
    if (financeAccountTypes.contains(account.type)) {
      final rows = await db.customSelect(
        'SELECT e.happened_at,e.value_after_cents FROM finance_events e '
        'JOIN finance_plans p ON p.id=e.plan_id WHERE p.account_id=? '
        'ORDER BY e.happened_at,e.id',
        variables: [d.Variable<int>(accountId)],
      ).get();
      final result = <({DateTime date, double balance})>[];
      var day = DateTime(startDate.year, startDate.month, startDate.day);
      final end = DateTime(endDate.year, endDate.month, endDate.day);
      var index = 0;
      var balance = 0.0;
      final sign = isLiabilityType(account.type) ? -1 : 1;
      while (!day.isAfter(end)) {
        final next = DateTime(day.year, day.month, day.day + 1);
        while (index < rows.length &&
            DateTime.parse(rows[index].read<String>('happened_at')).isBefore(next)) {
          balance = sign * rows[index].read<int>('value_after_cents') / 100;
          index++;
        }
        result.add((date: day, balance: balance));
        day = next;
      }
      return result;
    }

    // 估值账户：每天返回固定估值
    if (isValuationOnlyType(account.type)) {
      final result = <({DateTime date, double balance})>[];
      var currentDate = DateTime(startDate.year, startDate.month, startDate.day);
      final end = DateTime(endDate.year, endDate.month, endDate.day);
      while (!currentDate.isAfter(end)) {
        result.add((date: currentDate, balance: account.initialBalance));
        currentDate = currentDate.add(const Duration(days: 1));
      }
      return result;
    }

    // 获取 endDate **当天结束**之前的所有交易(按日期升序)。
    // endDate 语义是「含当天」:调用方(trendTodayAnchor)传当天 0 点,若用
    // <= endDate 会把当天发生的交易全部截掉 —— 趋势终点永远停在"昨晚为止",
    // 今天记的账不进趋势线。
    final endExclusive = DateTime(endDate.year, endDate.month, endDate.day)
        .add(const Duration(days: 1));
    final allTxs = await (db.select(db.transactions)
          ..where((t) => t.accountId.equals(accountId) | t.toAccountId.equals(accountId))
          ..where((t) => t.happenedAt.isSmallerThanValue(endExclusive))
          ..orderBy([(t) => d.OrderingTerm(expression: t.happenedAt)]))
        .get();

    // 计算 startDate 之前的余额
    double runningBalance = account.initialBalance;
    int txIndex = 0;

    // 先累加 startDate 之前的交易
    while (txIndex < allTxs.length && allTxs[txIndex].happenedAt.isBefore(startDate)) {
      final tx = allTxs[txIndex];
      if (tx.accountId == accountId) {
        if (tx.type == 'income') {
          runningBalance += tx.amount;
        } else if (tx.type == 'expense') {
          runningBalance -= tx.amount;
        } else if (tx.type == 'transfer') {
          runningBalance -= tx.amount;
        }
      }
      if (tx.toAccountId == accountId && tx.type == 'transfer') {
        runningBalance += tx.amount;
      }
      txIndex++;
    }

    // 按天填充
    final result = <({DateTime date, double balance})>[];
    var currentDate = DateTime(startDate.year, startDate.month, startDate.day);
    final end = DateTime(endDate.year, endDate.month, endDate.day);

    while (!currentDate.isAfter(end)) {
      final nextDate = currentDate.add(const Duration(days: 1));

      // 累加当天的交易
      while (txIndex < allTxs.length && allTxs[txIndex].happenedAt.isBefore(nextDate)) {
        final tx = allTxs[txIndex];
        if (tx.accountId == accountId) {
          if (tx.type == 'income') {
            runningBalance += tx.amount;
          } else if (tx.type == 'expense') {
            runningBalance -= tx.amount;
          } else if (tx.type == 'transfer') {
            runningBalance -= tx.amount;
          }
        }
        if (tx.toAccountId == accountId && tx.type == 'transfer') {
          runningBalance += tx.amount;
        }
        txIndex++;
      }

      result.add((date: currentDate, balance: runningBalance));
      currentDate = nextDate;
    }

    return result;
  }

  @override
  Future<List<({int? id, String name, String? icon, double total})>>
      getAccountCategoryStats(int accountId, {required String type}) async {
    final results = await db.customSelect(
      '''
      SELECT c.id, c.name, c.icon, SUM(t.amount) as total
      FROM transactions t
      LEFT JOIN categories c ON t.category_id = c.id
      WHERE t.account_id = ?1 AND t.type = ?2
      GROUP BY c.id
      ORDER BY total DESC
      ''',
      variables: [
        d.Variable.withInt(accountId),
        d.Variable.withString(type),
      ],
      readsFrom: {db.transactions, db.categories},
    ).get();

    return results.map((row) {
      return (
        id: row.data['id'] as int?,
        name: (row.data['name'] as String?) ?? resolveAppLocalizations(null).commonUncategorized,
        icon: row.data['icon'] as String?,
        total: (row.data['total'] as num).toDouble(),
      );
    }).toList();
  }

  @override
  Future<({double totalAssets, double totalLiabilities, double netWorth})> getNetWorthBreakdown() async {
    final accounts = await getAllAccounts();
    double totalAssets = 0.0;
    double totalLiabilities = 0.0;

    for (final account in accounts) {
      final balance = await getAccountBalance(account.id);
      if (isAssetBalance(account.type, balance)) {
        totalAssets += balance;
      } else {
        totalLiabilities += balance;
      }
    }

    return (
      totalAssets: totalAssets,
      totalLiabilities: totalLiabilities,
      netWorth: totalAssets + totalLiabilities,
    );
  }

  @override
  Future<Map<String, ({double totalAssets, double totalLiabilities, double netWorth})>> getNetWorthBreakdownByCurrency() async {
    final accounts = await getAllAccounts();
    final Map<String, ({double totalAssets, double totalLiabilities, double netWorth})> result = {};

    for (final account in accounts) {
      final balance = await getAccountBalance(account.id);
      final currency = account.currency.toUpperCase();
      final prev = result[currency] ?? (totalAssets: 0.0, totalLiabilities: 0.0, netWorth: 0.0);

      if (isAssetBalance(account.type, balance)) {
        result[currency] = (
          totalAssets: prev.totalAssets + balance,
          totalLiabilities: prev.totalLiabilities,
          netWorth: prev.netWorth + balance,
        );
      } else {
        result[currency] = (
          totalAssets: prev.totalAssets,
          totalLiabilities: prev.totalLiabilities + balance,
          netWorth: prev.netWorth + balance,
        );
      }
    }

    return result;
  }

  @override
  Future<List<({DateTime date, double balance})>> getNetWorthDailyBalances({
    required DateTime startDate,
    required DateTime endDate,
  }) async {
    final accounts = await getAllAccounts();
    if (accounts.isEmpty) return [];

    // 获取每个账户的每日余额
    final allBalances = <int, List<({DateTime date, double balance})>>{};
    for (final account in accounts) {
      allBalances[account.id] = await getAccountDailyBalances(
        account.id,
        startDate: startDate,
        endDate: endDate,
      );
    }

    // 按日聚合
    final result = <({DateTime date, double balance})>[];
    var currentDate = DateTime(startDate.year, startDate.month, startDate.day);
    final end = DateTime(endDate.year, endDate.month, endDate.day);
    int dayIndex = 0;

    while (!currentDate.isAfter(end)) {
      double dayTotal = 0.0;
      for (final account in accounts) {
        final balances = allBalances[account.id]!;
        if (dayIndex < balances.length) {
          dayTotal += balances[dayIndex].balance;
        }
      }
      result.add((date: currentDate, balance: dayTotal));
      currentDate = currentDate.add(const Duration(days: 1));
      dayIndex++;
    }

    return result;
  }

  @override
  Future<List<({DateTime date, double assets, double liabilities, double net})>>
      getNetWorthTrendSeries({
    required DateTime startDate,
    required DateTime endDate,
    required Map<String, double> ratesToBase,
  }) async {
    final accounts = await getAllAccounts();
    if (accounts.isEmpty) return [];

    final allBalances = <int, List<({DateTime date, double balance})>>{};
    for (final account in accounts) {
      allBalances[account.id] =
          await getAccountDailyBalances(account.id, startDate: startDate, endDate: endDate);
    }

    final result = <({DateTime date, double assets, double liabilities, double net})>[];
    var currentDate = DateTime(startDate.year, startDate.month, startDate.day);
    final end = DateTime(endDate.year, endDate.month, endDate.day);
    int dayIndex = 0;
    while (!currentDate.isAfter(end)) {
      double assets = 0.0, liabilities = 0.0;
      for (final account in accounts) {
        final balances = allBalances[account.id]!;
        if (dayIndex < balances.length) {
          // 折算到主币种:缺汇率的币种整条剔除(与净资产卡同口径,绝不按 1.0 裸加)。
          final rate = ratesToBase[account.currency.toUpperCase()];
          if (rate == null) continue;
          final bal = balances[dayIndex].balance * rate;
          if (isAssetBalance(account.type, bal)) {
            assets += bal;
          } else {
            liabilities += bal;
          }
        }
      }
      result.add((date: currentDate, assets: assets, liabilities: liabilities, net: assets + liabilities));
      currentDate = currentDate.add(const Duration(days: 1));
      dayIndex++;
    }
    return result;
  }

  @override
  Future<List<({String type, double totalBalance})>> getAssetCompositionByType() async {
    final accounts = await getAllAccounts();
    final Map<String, double> typeBalances = {};

    for (final account in accounts) {
      final balance = await getAccountBalance(account.id);
      final type = positiveAssetCompositionType(account.type, balance) ??
          (isAssetType(account.type) && balance < 0 ? '_negative_assets_' : null);
      if (type == null) continue;
      typeBalances.update(type, (v) => v + balance, ifAbsent: () => balance);
    }

    return typeBalances.entries
        .map((e) => (type: e.key, totalBalance: e.value))
        .toList();
  }

  @override
  Future<List<({String type, String currency, double totalBalance})>>
      getAssetCompositionByTypeAndCurrency() async {
    final accounts = await getAllAccounts();
    // (type, currency 大写) -> 余额累加
    final Map<({String type, String currency}), double> balances = {};

    for (final account in accounts) {
      final balance = await getAccountBalance(account.id);
      final type = positiveAssetCompositionType(account.type, balance) ??
          (isAssetType(account.type) && balance < 0 ? '_negative_assets_' : null);
      if (type == null) continue;
      final key = (type: type, currency: account.currency.toUpperCase());
      balances.update(key, (v) => v + balance, ifAbsent: () => balance);
    }

    return balances.entries
        .map((e) => (
              type: e.key.type,
              currency: e.key.currency,
              totalBalance: e.value,
            ))
        .toList();
  }

  @override
  Future<void> updateAccountValuation(int accountId, double newValue) async {
    await _requireOrdinary(accountId);
    await (db.update(db.accounts)..where((a) => a.id.equals(accountId))).write(
      AccountsCompanion(
        initialBalance: d.Value(newValue),
        updatedAt: d.Value(DateTime.now()),
      ),
    );
  }

  @override
  Future<SharedLedgerAccount?> getSharedAccountBySyncId(String syncId) {
    return (db.select(db.sharedLedgerAccounts)
          ..where((t) => t.syncId.equals(syncId)))
        .getSingleOrNull();
  }

  @override
  Future<Set<String>> getUsedCurrencies() async {
    final rows = await db
        .customSelect('SELECT DISTINCT currency FROM accounts')
        .get();
    return rows
        .map((r) => (r.read<String>('currency')).toUpperCase())
        .toSet();
  }
}
