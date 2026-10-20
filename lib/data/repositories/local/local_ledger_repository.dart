import 'package:drift/drift.dart' as d;
import 'package:uuid/uuid.dart';

import '../../db.dart';
import '../ledger_repository.dart';
import '../finance_repository.dart';
import '../../../models/finance.dart';
import '../../../utils/app_localizations_resolver.dart';

const _uuid = Uuid();

/// 本地账本Repository实现
/// 基于 Drift 数据库实现
class LocalLedgerRepository implements LedgerRepository {
  final BeeDatabase db;

  LocalLedgerRepository(this.db);

  @override
  Stream<List<Ledger>> watchLedgers() => db.select(db.ledgers).watch();

  @override
  Future<List<Ledger>> getAllLedgers() async {
    return db.select(db.ledgers).get();
  }

  @override
  Future<Ledger?> getLedgerById(int id) async {
    final query = db.select(db.ledgers)..where((l) => l.id.equals(id));
    final results = await query.get();
    return results.isEmpty ? null : results.first;
  }

  @override
  Future<int> getLedgerCount() async {
    final row = await db.customSelect('SELECT COUNT(*) AS c FROM ledgers',
        readsFrom: {db.ledgers}).getSingle();
    final v = row.data['c'];
    if (v is int) return v;
    if (v is BigInt) return v.toInt();
    return 0;
  }

  @override
  Future<int> ledgerCount() => getLedgerCount();

  @override
  Future<({int dayCount, int txCount})> getCountsForLedger({
    required int ledgerId,
  }) async {
    final txRow = await db.customSelect(
        'SELECT COUNT(*) AS c FROM transactions WHERE ledger_id = ?1',
        variables: [d.Variable.withInt(ledgerId)],
        readsFrom: {db.transactions}).getSingle();
    // 计算记账天数：今天 - 第一笔记账日期 + 1
    final dayRow = await db.customSelect("""
      SELECT CASE
        WHEN MIN(happened_at) IS NULL THEN 0
        ELSE CAST(julianday('now', 'localtime') - julianday(MIN(happened_at), 'unixepoch', 'localtime') + 1 AS INTEGER)
      END AS c
      FROM transactions WHERE ledger_id = ?1
      """,
        variables: [d.Variable.withInt(ledgerId)],
        readsFrom: {db.transactions}).getSingle();

    int parse(dynamic v) {
      if (v is int) return v;
      if (v is BigInt) return v.toInt();
      if (v is num) return v.toInt();
      return 0;
    }

    return (dayCount: parse(dayRow.data['c']), txCount: parse(txRow.data['c']));
  }

  @override
  Future<({int dayCount, int txCount})> getCountsAll() async {
    final txRow = await db.customSelect(
      'SELECT COUNT(*) AS c FROM transactions',
      readsFrom: {db.transactions},
    ).getSingle();
    // 计算记账天数：今天 - 第一笔记账日期 + 1
    final dayRow = await db.customSelect(
      """
      SELECT CASE
        WHEN MIN(happened_at) IS NULL THEN 0
        ELSE CAST(julianday('now', 'localtime') - julianday(MIN(happened_at), 'unixepoch', 'localtime') + 1 AS INTEGER)
      END AS c
      FROM transactions
      """,
      readsFrom: {db.transactions},
    ).getSingle();

    int parse(dynamic v) {
      if (v is int) return v;
      if (v is BigInt) return v.toInt();
      if (v is num) return v.toInt();
      return 0;
    }

    return (dayCount: parse(dayRow.data['c']), txCount: parse(txRow.data['c']));
  }

  @override
  Future<({double balance, int transactionCount})> getLedgerStats({
    required int ledgerId,
    bool accountFeatureEnabled = true,
    List<Transaction>? transactions,
  }) async {
    // 如果没有传入 transactions，则查询
    final rows = transactions ?? await (db.select(db.transactions)
          ..where((t) => t.ledgerId.equals(ledgerId)))
        .get();

    // Sale receipts already contain principal and profit. Their separate
    // non-cash P/L entry belongs in income/expense charts, not cash-flow balance.
    final nonCashSaleRows = await db.customSelect(
      "SELECT t.id FROM transactions t "
      "JOIN finance_transaction_links l ON l.transaction_id=t.id "
      "JOIN finance_events e ON e.id=l.event_id "
      "WHERE t.ledger_id=? AND e.action='sell' AND l.owned=1 "
      "AND t.account_id IS NULL AND t.to_account_id IS NULL",
      variables: [d.Variable<int>(ledgerId)], readsFrom: {db.transactions},
    ).get();
    final nonCashSaleIds = nonCashSaleRows.map((r) => r.read<int>('id')).toSet();

    // 交易数
    final transactionCount = rows.length;

    // v1.15.0: 账户独立后，账本余额仅计算交易收支，不再叠加账户初始余额
    double balance = 0.0;

    // 账本余额 = 跨账户收支汇总 → 账本维度,读折算值 nativeAmount(?? amount
    // 兜底,单币种账本 native==amount 结果不变)。原先裸加 t.amount 在多币种
    // 账本下把不同币种原值直接相加(CNY+JPY),且改主币种后不随折算更新。
    for (final t in rows) {
      if (nonCashSaleIds.contains(t.id)) continue;
      final v = t.nativeAmount ?? t.amount;
      if (t.type == 'income') {
        balance += v;
      } else if (t.type == 'expense') {
        balance -= v;
      }
      // transfer 不影响总余额
    }

    return (balance: balance, transactionCount: transactionCount);
  }

  @override
  Future<int> createLedger({
    required String name,
    String currency = 'CNY',
  }) async {
    // syncId 是跨设备稳定外键。新建账本必须现场写入 UUID，否则 push 侧
    // 的 `ledger.syncId ?? ledger.id.toString()` 会 fallback 到本地 int id，
    // 第二台设备本地 int id 不同 → server 会 auto-create 一个 external_id
    // 为本地 int id 字符串的 duplicate ledger。
    return db.into(db.ledgers).insert(
          LedgersCompanion.insert(
            name: name,
            currency: d.Value(currency),
            syncId: d.Value(_uuid.v4()),
          ),
        );
  }

  @override
  Future<void> updateLedgerName({required int id, required String name}) async {
    await (db.update(db.ledgers)..where((tbl) => tbl.id.equals(id))).write(
      LedgersCompanion(name: d.Value(name)),
    );
  }

  @override
  Future<void> updateLedger({
    required int id,
    String? name,
    String? currency,
    int? monthStartDay,
  }) async {
    final comp = LedgersCompanion(
      name: name != null ? d.Value(name) : const d.Value.absent(),
      currency: currency != null ? d.Value(currency) : const d.Value.absent(),
      monthStartDay: monthStartDay != null
          ? d.Value(monthStartDay.clamp(1, 28))
          : const d.Value.absent(),
    );
    await (db.update(db.ledgers)..where((tbl) => tbl.id.equals(id)))
        .write(comp);
  }

  @override
  Stream<Ledger?> watchLedger(int id) {
    return (db.select(db.ledgers)..where((l) => l.id.equals(id)))
        .watchSingleOrNull();
  }

  @override
  Future<void> deleteLedger(int id) async {
    await db.transaction(() async {
      await clearLedgerTransactions(id);
      await (db.delete(db.ledgers)..where((tbl) => tbl.id.equals(id))).go();
    });
  }

  @override
  Future<int> getMaxLedgerId() async {
    final row = await db.customSelect(
        'SELECT IFNULL(MAX(id), 0) AS m FROM ledgers',
        readsFrom: {db.ledgers}).getSingle();
    final v = row.data['m'];
    if (v is int) return v;
    if (v is BigInt) return v.toInt();
    if (v is num) return v.toInt();
    return 0;
  }

  @override
  Future<int> getNextFreeLedgerId() async {
    final maxId = await getMaxLedgerId();
    return maxId + 1;
  }

  @override
  Future<void> reassignLedgerId({
    required int fromId,
    required int toId,
  }) async {
    if (fromId == toId) return;
    final existsTo = await (db.select(db.ledgers)
          ..where((l) => l.id.equals(toId)))
        .getSingleOrNull();
    if (existsTo != null) {
      throw StateError(resolveAppLocalizations(null)
          .ledgerTargetIdExists(toId.toString()));
    }
    await db.transaction(() async {
      if ((await FinanceRepository(db).list(fromId)).isNotEmpty) {
        throw const FinanceFailure('linked_transaction');
      }

      // 先迁移子表中的外键引用
      await db.customUpdate(
        'UPDATE accounts SET ledger_id = ?1 WHERE ledger_id = ?2',
        variables: [d.Variable<int>(toId), d.Variable<int>(fromId)],
        updates: {db.accounts},
      );
      await db.customUpdate(
        'UPDATE transactions SET ledger_id = ?1 WHERE ledger_id = ?2',
        variables: [d.Variable<int>(toId), d.Variable<int>(fromId)],
        updates: {db.transactions},
      );
      // 再更新主表ID（SQLite 允许更新 INTEGER PRIMARY KEY 的值）
      await db.customUpdate(
        'UPDATE ledgers SET id = ?1 WHERE id = ?2',
        variables: [d.Variable<int>(toId), d.Variable<int>(fromId)],
        updates: {db.ledgers},
      );
    });
  }

  @override
  Future<int> clearLedgerTransactions(int ledgerId) async {
    return db.transaction(() async {
      // Remove financial ownership before deleting the same ledger's entries.
      // An error in either half rolls back plans, events, mirrors and entries.
      await FinanceRepository(db).clearLedger(ledgerId);
      final txIds = await (db.select(db.transactions)
            ..where((t) => t.ledgerId.equals(ledgerId)))
          .map((t) => t.id)
          .get();
      if (txIds.isNotEmpty) {
        await (db.delete(db.transactionTags)
              ..where((tt) => tt.transactionId.isIn(txIds))).go();
        await (db.delete(db.transactionAttachments)
              ..where((a) => a.transactionId.isIn(txIds))).go();
      }
      return (db.delete(db.transactions)
            ..where((t) => t.ledgerId.equals(ledgerId))).go();
    });
  }

  @override
  Future<double> getTotalInitialBalance(int ledgerId) async {
    final accounts = await (db.select(db.accounts)
          ..where((a) => a.ledgerId.equals(ledgerId)))
        .get();

    double total = 0.0;
    for (final account in accounts) {
      total += account.initialBalance;
    }
    return total;
  }
}
