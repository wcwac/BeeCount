import 'package:drift/drift.dart';

/// Hand-written SQL tables are versioned with BeeDatabase. Keeping them outside
/// generated db.g.dart allows this small domain to have explicit constraints and
/// guard triggers. No lazy schema creation or separate preferences storage.
Future<void> createFinanceSchema(GeneratedDatabase db) async {
  for (final sql in financeSchemaStatements) {
    await db.customStatement(sql);
  }
}

const financeSchemaStatements = <String>[
  '''CREATE TABLE IF NOT EXISTS finance_plans (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ledger_id INTEGER NOT NULL,
    account_id INTEGER NOT NULL UNIQUE,
    kind TEXT NOT NULL CHECK(kind IN ('reimbursement','lend','borrow','installment','mortgage','investment')),
    title TEXT NOT NULL, party TEXT NOT NULL DEFAULT '',
    currency TEXT NOT NULL, note TEXT NOT NULL DEFAULT '',
    principal_cents INTEGER NOT NULL CHECK(principal_cents > 0),
    annual_rate_bps INTEGER NOT NULL DEFAULT 0,
    periods INTEGER NOT NULL DEFAULT 12,
    method TEXT NOT NULL DEFAULT 'annuity',
    opened_at TEXT NOT NULL, first_due_at TEXT NOT NULL
  )''',
  'CREATE INDEX IF NOT EXISTS idx_finance_ledger ON finance_plans(ledger_id)',
  '''CREATE TABLE IF NOT EXISTS finance_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plan_id INTEGER NOT NULL,
    action TEXT NOT NULL,
    principal_delta_cents INTEGER NOT NULL DEFAULT 0,
    units_delta_micros INTEGER NOT NULL DEFAULT 0,
    profit_cents INTEGER NOT NULL DEFAULT 0,
    interest_cents INTEGER NOT NULL DEFAULT 0 CHECK(interest_cents >= 0),
    cash_cents INTEGER NOT NULL DEFAULT 0,
    value_after_cents INTEGER NOT NULL CHECK(value_after_cents >= 0),
    happened_at TEXT NOT NULL, note TEXT NOT NULL DEFAULT '',
    period_number INTEGER,
    UNIQUE(plan_id, period_number)
  )''',
  'CREATE INDEX IF NOT EXISTS idx_finance_event_plan ON finance_events(plan_id,id)',
  '''CREATE TABLE IF NOT EXISTS finance_transaction_links (
    transaction_id INTEGER PRIMARY KEY,
    event_id INTEGER NOT NULL,
    owned INTEGER NOT NULL CHECK(owned IN (0,1)),
    original_exclude_stats INTEGER NOT NULL DEFAULT 0,
    original_exclude_budget INTEGER NOT NULL DEFAULT 0
  )''',
  'CREATE INDEX IF NOT EXISTS idx_finance_link_event ON finance_transaction_links(event_id)',
  // Linked entries must be changed as a single domain operation. Otherwise a
  // normal editor/delete/import would silently desynchronise receivables/debt.
  '''CREATE TRIGGER IF NOT EXISTS finance_guard_tx_update
    BEFORE UPDATE ON transactions WHEN EXISTS (
      SELECT 1 FROM finance_transaction_links WHERE transaction_id=OLD.id)
      AND (OLD.ledger_id IS NOT NEW.ledger_id OR
        OLD.type IS NOT NEW.type OR
        OLD.amount IS NOT NEW.amount OR
        OLD.category_id IS NOT NEW.category_id OR
        OLD.account_id IS NOT NEW.account_id OR
        OLD.to_account_id IS NOT NEW.to_account_id OR
        OLD.happened_at IS NOT NEW.happened_at OR
        OLD.note IS NOT NEW.note OR
        OLD.currency_code IS NOT NEW.currency_code OR
        OLD.exclude_from_stats IS NOT NEW.exclude_from_stats OR
        OLD.exclude_from_budget IS NOT NEW.exclude_from_budget)
    BEGIN SELECT RAISE(ABORT, 'Manage this transaction in Financial management / 请在财务管理中修改关联流水'); END''',
  '''CREATE TRIGGER IF NOT EXISTS finance_guard_tx_delete
    BEFORE DELETE ON transactions WHEN EXISTS (
      SELECT 1 FROM finance_transaction_links WHERE transaction_id=OLD.id)
    BEGIN SELECT RAISE(ABORT, 'Manage this transaction in Financial management / 请在财务管理中删除关联流水'); END''',
  '''CREATE TRIGGER IF NOT EXISTS finance_guard_account_delete
    BEFORE DELETE ON accounts WHEN EXISTS (
      SELECT 1 FROM finance_plans WHERE account_id=OLD.id)
    BEGIN SELECT RAISE(ABORT, 'Manage this account in Financial management / 请在财务管理中删除专用账户'); END''',
  '''CREATE TRIGGER IF NOT EXISTS finance_guard_account_type
    BEFORE UPDATE OF currency,type ON accounts WHEN EXISTS (
      SELECT 1 FROM finance_plans WHERE account_id=OLD.id)
      AND (NEW.currency!=OLD.currency OR NEW.type!=OLD.type)
    BEGIN SELECT RAISE(ABORT, 'Financial account currency and type are fixed / 财务专用账户的币种和类型不可修改'); END''',
];
