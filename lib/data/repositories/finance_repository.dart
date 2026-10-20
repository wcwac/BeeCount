import 'package:drift/drift.dart' as d;
import 'package:uuid/uuid.dart';

import '../db.dart';
import '../../models/finance.dart';
import '../../utils/account_type_utils.dart';
import 'local/local_repository.dart';

/// All money-changing operations are single SQLite transactions. Events are the
/// source of truth; the dedicated valuation account mirrors the last event so
/// existing assets, currency conversion and net-worth screens remain usable.
class FinanceRepository {
  final BeeDatabase db;
  FinanceRepository(this.db);
  static const _maxMoney = 100000000000000;
  static const _maxUnits = 1000000000000000;
  static const _uuid = Uuid();

  static const _planQuery = '''SELECT p.*,
    COALESCE((SELECT SUM(principal_delta_cents) FROM finance_events e WHERE e.plan_id=p.id),0) AS outstanding_cents,
    COALESCE((SELECT SUM(units_delta_micros) FROM finance_events e WHERE e.plan_id=p.id),0) AS units_micros,
    COALESCE((SELECT value_after_cents FROM finance_events e WHERE e.plan_id=p.id ORDER BY id DESC LIMIT 1),0) AS value_cents,
    COALESCE((SELECT SUM(profit_cents) FROM finance_events e WHERE e.plan_id=p.id),0) AS profit_cents,
    COALESCE((SELECT SUM(interest_cents) FROM finance_events e WHERE e.plan_id=p.id),0) AS interest_cents
    FROM finance_plans p''';

  Future<List<FinancePlan>> list(int ledgerId) async {
    final rows = await db.customSelect('$_planQuery WHERE p.ledger_id=? ORDER BY p.id DESC',
      variables: [d.Variable(ledgerId)]).get();
    return rows.map((r) => FinancePlan.fromMap(r.data)).toList();
  }

  Future<FinancePlan?> get(int id) async {
    final row = await db.customSelect('$_planQuery WHERE p.id=?',
      variables: [d.Variable(id)]).getSingleOrNull();
    return row == null ? null : FinancePlan.fromMap(row.data);
  }

  Future<List<FinanceEvent>> events(int id) async {
    final rows = await db.customSelect(
      'SELECT * FROM finance_events WHERE plan_id=? ORDER BY id',
      variables: [d.Variable(id)]).get();
    return rows.map((r) => FinanceEvent.fromMap(r.data)).toList();
  }

  Future<int?> planIdForTransaction(int id) async {
    final row = await db.customSelect(
      'SELECT e.plan_id FROM finance_transaction_links l JOIN finance_events e '
      'ON e.id=l.event_id WHERE l.transaction_id=?',
      variables: [d.Variable(id)]).getSingleOrNull();
    return row?.read<int>('plan_id');
  }

  Future<int?> planIdForAccount(int id) async {
    final row = await db.customSelect('SELECT id FROM finance_plans WHERE account_id=?',
      variables: [d.Variable(id)]).getSingleOrNull();
    return row?.read<int>('id');
  }

  Future<bool> isManagedTransaction(int id) async =>
      await db.customSelect('SELECT 1 FROM finance_transaction_links WHERE transaction_id=?',
        variables: [d.Variable(id)]).getSingleOrNull() != null;

  Future<bool> isManagedAccount(int id) async =>
      await db.customSelect('SELECT 1 FROM finance_plans WHERE account_id=?',
        variables: [d.Variable(id)]).getSingleOrNull() != null;

  Future<List<Account>> cashAccounts(int ledgerId, {String? currency}) async {
    final ledger = await _ledger(ledgerId);
    final accounts = await (db.select(db.accounts)..where((a) =>
      a.currency.equals(currency ?? ledger.currency) & a.hidden.equals(false))).get();
    return accounts.where((a) => isTradableType(a.type) &&
      !financeAccountTypes.contains(a.type)).toList();
  }

  /// One query instead of an eligibility lookup for every historical expense.
  Future<List<Transaction>> reimbursableExpenses(int ledgerId) async {
    final ledger = await _ledger(ledgerId);
    final rows = await db.customSelect('''SELECT t.* FROM transactions t
      LEFT JOIN accounts a ON a.id=t.account_id
      WHERE t.ledger_id=? AND t.type='expense' AND t.amount>0
      AND COALESCE(t.currency_code,a.currency,?)=?
      AND NOT EXISTS(SELECT 1 FROM finance_transaction_links l WHERE l.transaction_id=t.id)
      ORDER BY t.happened_at DESC,t.id DESC''', variables: [
      d.Variable(ledgerId), d.Variable(ledger.currency), d.Variable(ledger.currency),
    ]).get();
    return rows.map((row) => db.transactions.map(row.data)).toList();
  }

  Future<Ledger> _ledger(int id) async {
    final ledger = await (db.select(db.ledgers)..where((l) => l.id.equals(id)))
        .getSingleOrNull();
    if (ledger == null) throw const FinanceFailure('missing_plan');
    return ledger;
  }

  Future<Account> _cash(int? id, String currency) async {
    if (id == null) throw const FinanceFailure('missing_account');
    final a = await (db.select(db.accounts)..where((a) => a.id.equals(id)))
        .getSingleOrNull();
    if (a == null || a.hidden || !isTradableType(a.type) ||
        financeAccountTypes.contains(a.type)) {
      throw const FinanceFailure('invalid_account');
    }
    if (a.currency != currency) throw const FinanceFailure('currency_mismatch');
    return a;
  }

  void _positive(int amount) {
    if (amount <= 0 || amount > _maxMoney) {
      throw const FinanceFailure('invalid_amount');
    }
  }

  void _date(DateTime date, {DateTime? previous}) {
    if (financeDay(date).isAfter(financeDay(DateTime.now())) ||
        (previous != null && financeDay(date).isBefore(financeDay(previous)))) {
      throw const FinanceFailure('invalid_date');
    }
  }

  Future<int> create(FinanceDraft draft) => db.transaction(() async {
    _positive(draft.amountCents);
    _date(draft.openedAt);
    if (draft.title.trim().isEmpty || draft.title.length > 200 ||
        draft.party.length > 200 || draft.note.length > 4000) {
      throw const FinanceFailure('invalid_terms');
    }
    final ledger = await _ledger(draft.ledgerId);
    final opened = financeDay(draft.openedAt);
    final firstDue = financeDay(draft.firstDueAt ?? financeMonth(opened, 1));
    if (draft.kind.isScheduled) {
      if (firstDue.isBefore(opened)) throw const FinanceFailure('invalid_date');
      repaymentSchedule(principalCents: draft.amountCents,
        annualRateBps: draft.annualRateBps, periods: draft.periods,
        firstDueAt: firstDue, method: draft.method);
    }
    if (draft.kind == FinanceKind.investment &&
        (draft.unitsMicros <= 0 || draft.unitsMicros > _maxUnits)) {
      throw const FinanceFailure('invalid_units');
    }
    Transaction? existing;
    Account? cash;
    if (draft.existingExpenseId != null) {
      if (draft.kind != FinanceKind.reimbursement) {
        throw const FinanceFailure('invalid_action');
      }
      existing = await (db.select(db.transactions)
        ..where((t) => t.id.equals(draft.existingExpenseId!))).getSingleOrNull();
      if (existing == null || existing.type != 'expense' ||
          existing.ledgerId != draft.ledgerId ||
          (existing.amount * 100).round() != draft.amountCents) {
        throw const FinanceFailure('invalid_amount');
      }
      if ((existing.currencyCode ?? ledger.currency) != ledger.currency) {
        throw const FinanceFailure('currency_mismatch');
      }
      if (financeDay(existing.happenedAt) != opened) {
        throw const FinanceFailure('invalid_date');
      }
      if (await isManagedTransaction(existing.id)) {
        throw const FinanceFailure('linked_transaction');
      }
    } else if (draft.recordCashFlow && draft.kind != FinanceKind.installment) {
      cash = await _cash(draft.cashAccountId, ledger.currency);
    }
    // Use a readable account name; stable identity lives in syncId, not the UI.
    final names = (await db.customSelect('SELECT name FROM accounts').get())
        .map((row) => row.read<String>('name')).toSet();
    var accountName = draft.title.trim();
    var suffix = 2;
    while (names.contains(accountName)) {
      accountName = '${draft.title.trim()} ($suffix)';
      suffix++;
    }
    final managedId = await db.into(db.accounts).insert(AccountsCompanion.insert(
      ledgerId: draft.ledgerId,
      name: accountName,
      type: d.Value(draft.kind.accountType), currency: d.Value(ledger.currency),
      initialBalance: d.Value((draft.kind.isDebt ? -1 : 1) * draft.amountCents / 100),
      syncId: d.Value(_uuid.v4()), createdAt: d.Value(opened)));
    final planId = await db.customInsert('''INSERT INTO finance_plans
      (ledger_id,account_id,kind,title,party,currency,note,principal_cents,
       annual_rate_bps,periods,method,opened_at,first_due_at)
      VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)''', variables: [
      d.Variable(draft.ledgerId), d.Variable(managedId), d.Variable(draft.kind.name),
      d.Variable(draft.title.trim()), d.Variable(draft.party.trim()),
      d.Variable(ledger.currency), d.Variable(draft.note.trim()),
      d.Variable(draft.amountCents), d.Variable(draft.annualRateBps),
      d.Variable(draft.periods), d.Variable(draft.method.name),
      d.Variable(_day(opened)), d.Variable(_day(firstDue)),
    ]);
    final eventId = await _event(planId, action: 'opening',
      principal: draft.amountCents,
      units: draft.kind == FinanceKind.investment ? draft.unitsMicros : 0,
      value: draft.amountCents, date: opened, note: draft.note,
      cash: existing != null || (draft.recordCashFlow &&
          draft.kind != FinanceKind.installment)
        ? (draft.kind.isDebt ? 1 : -1) * draft.amountCents : 0);
    if (existing != null) {
      await (db.update(db.transactions)..where((t) => t.id.equals(existing!.id)))
        .write(const TransactionsCompanion(
          excludeFromStats: d.Value(true), excludeFromBudget: d.Value(true)));
      await _link(eventId, existing.id, owned: false,
        originalStats: existing.excludeFromStats,
        originalBudget: existing.excludeFromBudget);
    } else if (draft.recordCashFlow) {
      if (draft.kind == FinanceKind.installment) {
        await _tx(eventId, ledgerId: draft.ledgerId, currency: ledger.currency,
          amount: draft.amountCents, type: 'expense', cashId: null,
          excluded: false, category: '分期消费', title: draft.title, date: opened);
      } else {
        await _tx(eventId, ledgerId: draft.ledgerId, currency: ledger.currency,
          amount: draft.amountCents, type: draft.kind.isDebt ? 'income' : 'expense',
          cashId: cash!.id, excluded: true, title: draft.title, date: opened);
      }
    }
    return planId;
  });

  Future<void> post(int id, FinancePosting posting) => db.transaction(() async {
    final plan = await get(id);
    if (plan == null) throw const FinanceFailure('missing_plan');
    final history = await events(id);
    _date(posting.happenedAt, previous: history.last.happenedAt);
    if (posting.note.length > 4000) throw const FinanceFailure('invalid_terms');
    if (posting.action == FinanceAction.valuation) {
      if (plan.kind != FinanceKind.investment || plan.isClosed ||
          posting.amountCents < 0 || posting.amountCents > _maxMoney) {
        throw const FinanceFailure('invalid_action');
      }
      await _event(id, action: 'valuation', value: posting.amountCents,
        date: posting.happenedAt, note: posting.note);
      await _mirror(plan, posting.amountCents);
      return;
    }
    if (plan.isClosed && !(plan.kind == FinanceKind.investment &&
        posting.action == FinanceAction.buy)) {
      throw const FinanceFailure('closed');
    }
    final cash = await _cash(posting.cashAccountId, plan.currency);
    switch (posting.action) {
      case FinanceAction.settle:
        if (plan.kind == FinanceKind.investment) {
          throw const FinanceFailure('invalid_action');
        }
        _positive(posting.principalCents);
        if (posting.principalCents > plan.outstandingCents) {
          throw const FinanceFailure('overpayment');
        }
        if (posting.interestCents < 0 || posting.interestCents > _maxMoney ||
            (plan.kind == FinanceKind.reimbursement && posting.interestCents != 0)) {
          throw const FinanceFailure('invalid_amount');
        }
        if (plan.kind.isScheduled) {
          if (posting.periodNumber == null) {
            if (posting.principalCents != plan.outstandingCents) {
              throw const FinanceFailure('invalid_period');
            }
          } else {
            final paid = history.where((e) => e.periodNumber != null)
              .map((e) => e.periodNumber!).toSet();
            final nextNumber = paid.length + 1;
            if (posting.periodNumber != nextNumber || nextNumber > plan.periods) {
              throw const FinanceFailure('invalid_period');
            }
            final row = repaymentSchedule(principalCents: plan.principalCents,
              annualRateBps: plan.annualRateBps, periods: plan.periods,
              firstDueAt: plan.firstDueAt, method: plan.method)[nextNumber - 1];
            if (posting.principalCents != row.principalCents ||
                posting.interestCents != row.interestCents) {
              throw const FinanceFailure('invalid_amount');
            }
          }
        } else if (posting.periodNumber != null) {
          throw const FinanceFailure('invalid_period');
        }
        final value = plan.outstandingCents - posting.principalCents;
        final eventId = await _event(id, action: 'settle',
          principal: -posting.principalCents, interest: posting.interestCents,
          value: value, cash: (plan.kind.isDebt ? -1 : 1) *
            (posting.principalCents + posting.interestCents),
          date: posting.happenedAt, note: posting.note,
          period: posting.periodNumber);
        await _tx(eventId, ledgerId: plan.ledgerId, currency: plan.currency,
          amount: posting.principalCents, type: plan.kind.isDebt ? 'expense' : 'income',
          cashId: cash.id, excluded: true, title: plan.title, date: posting.happenedAt);
        if (posting.interestCents > 0) {
          await _tx(eventId, ledgerId: plan.ledgerId, currency: plan.currency,
            amount: posting.interestCents,
            type: plan.kind.isDebt ? 'expense' : 'income', cashId: cash.id,
            excluded: false, category: '借款利息', title: plan.title,
            date: posting.happenedAt);
        }
        await _mirror(plan, value);
        break;
      case FinanceAction.buy:
        _investment(plan);
        _positive(posting.amountCents);
        if (posting.unitsMicros <= 0 || posting.unitsMicros > _maxUnits ||
            posting.unitsMicros > _maxUnits - plan.unitsMicros ||
            plan.costCents + posting.amountCents > _maxMoney ||
            plan.valueCents + posting.amountCents > _maxMoney) {
          throw const FinanceFailure('invalid_units');
        }
        final value = plan.valueCents + posting.amountCents;
        final eventId = await _event(id, action: 'buy',
          principal: posting.amountCents, units: posting.unitsMicros,
          cash: -posting.amountCents, value: value, date: posting.happenedAt,
          note: posting.note);
        await _tx(eventId, ledgerId: plan.ledgerId, currency: plan.currency,
          amount: posting.amountCents, type: 'expense', cashId: cash.id,
          excluded: true, title: plan.title, date: posting.happenedAt);
        await _mirror(plan, value);
        break;
      case FinanceAction.sell:
        _investment(plan);
        if (posting.amountCents < 0 || posting.amountCents > _maxMoney) {
          throw const FinanceFailure('invalid_amount');
        }
        if (posting.unitsMicros <= 0 || posting.unitsMicros > plan.unitsMicros) {
          throw const FinanceFailure('invalid_units');
        }
        // BigInt avoids overflow for high unit precision and large cost bases.
        final cost = _proportion(plan.costCents, posting.unitsMicros, plan.unitsMicros);
        final valueRemoved = _proportion(plan.valueCents, posting.unitsMicros, plan.unitsMicros);
        final value = plan.valueCents - valueRemoved;
        final profit = posting.amountCents - cost;
        final eventId = await _event(id, action: 'sell', principal: -cost,
          units: -posting.unitsMicros, cash: posting.amountCents,
          profit: profit, value: value, date: posting.happenedAt, note: posting.note);
        // The cash receipt affects the cash account; a separate non-cash profit
        // entry affects statistics without crediting cash twice.
        if (posting.amountCents > 0) {
          await _tx(eventId, ledgerId: plan.ledgerId, currency: plan.currency,
            amount: posting.amountCents, type: 'income', cashId: cash.id,
            excluded: true, title: plan.title, date: posting.happenedAt);
        }
        if (profit != 0) {
          await _tx(eventId, ledgerId: plan.ledgerId, currency: plan.currency,
            amount: profit.abs(), type: profit > 0 ? 'income' : 'expense',
            cashId: null, excluded: false,
            category: profit > 0 ? '理财收益' : '理财亏损', title: plan.title,
            date: posting.happenedAt);
        }
        await _mirror(plan, value);
        break;
      case FinanceAction.dividend:
        _investment(plan);
        _positive(posting.amountCents);
        final eventId = await _event(id, action: 'dividend',
          profit: posting.amountCents, cash: posting.amountCents,
          value: plan.valueCents, date: posting.happenedAt, note: posting.note);
        await _tx(eventId, ledgerId: plan.ledgerId, currency: plan.currency,
          amount: posting.amountCents, type: 'income', cashId: cash.id,
          excluded: false, category: '理财分红', title: plan.title,
          date: posting.happenedAt);
        break;
      case FinanceAction.valuation:
        break; // handled before account validation
    }
  });

  void _investment(FinancePlan plan) {
    if (plan.kind != FinanceKind.investment) {
      throw const FinanceFailure('invalid_action');
    }
  }

  int _proportion(int value, int part, int whole) =>
    ((BigInt.from(value) * BigInt.from(part) + BigInt.from(whole ~/ 2)) ~/
      BigInt.from(whole)).toInt();

  Future<int> _event(int planId, {required String action, int principal = 0,
      int units = 0, int profit = 0, int interest = 0, int cash = 0,
      required int value, required DateTime date, String note = '', int? period}) =>
    db.customInsert('''INSERT INTO finance_events
      (plan_id,action,principal_delta_cents,units_delta_micros,profit_cents,
       interest_cents,cash_cents,value_after_cents,happened_at,note,period_number)
      VALUES (?,?,?,?,?,?,?,?,?,?,?)''', variables: [
      d.Variable(planId), d.Variable(action), d.Variable(principal),
      d.Variable(units), d.Variable(profit), d.Variable(interest), d.Variable(cash),
      d.Variable(value), d.Variable(_day(date)), d.Variable(note.trim()),
      d.Variable<int>(period),
    ]);

  Future<void> _link(int eventId, int txId, {bool owned = true,
      bool originalStats = false, bool originalBudget = false}) =>
    db.customStatement('''INSERT INTO finance_transaction_links
      (transaction_id,event_id,owned,original_exclude_stats,original_exclude_budget)
      VALUES (?,?,?,?,?)''', [txId, eventId, owned ? 1 : 0,
        originalStats ? 1 : 0, originalBudget ? 1 : 0]);

  Future<void> _tx(int eventId, {required int ledgerId, required String currency,
      required int amount, required String type, required int? cashId,
      required bool excluded, String? category,
      required String title, required DateTime date}) async {
    final repo = LocalRepository(db);
    int? categoryId;
    if (category != null) {
      final row = await (db.select(db.categories)..where((c) =>
          c.name.equals(category) & c.kind.equals(type))).get();
      categoryId = row.isEmpty ? await db.into(db.categories).insert(
        CategoriesCompanion.insert(name: category, kind: type,
          syncId: d.Value(_uuid.v4()))) : row.first.id;
    }
    final id = await repo.addTransaction(ledgerId: ledgerId, type: type,
      amount: amount / 100, categoryId: categoryId, accountId: cashId,
      happenedAt: financeDay(date), note: title, currencyCode: currency,
      excludeFromStats: excluded, excludeFromBudget: excluded);
    await _link(eventId, id);
  }

  Future<void> _mirror(FinancePlan plan, int value) async {
    final changed = await (db.update(db.accounts)..where((a) => a.id.equals(plan.accountId)))
      .write(AccountsCompanion(initialBalance: d.Value(
        (plan.kind.isDebt ? -1 : 1) * value / 100), updatedAt: d.Value(DateTime.now())));
    if (changed != 1) throw const FinanceFailure('managed_account');
  }

  Future<void> undoLastEvent(int id) => db.transaction(() async {
    final plan = await get(id);
    if (plan == null) throw const FinanceFailure('missing_plan');
    final history = await events(id);
    if (history.length < 2) throw const FinanceFailure('cannot_undo_opening');
    await _removeEvents([history.last.id]);
    await _mirror(plan, history[history.length - 2].valueAfterCents);
  });

  Future<void> delete(int id) => db.transaction(() async {
    final plan = await get(id);
    if (plan == null) throw const FinanceFailure('missing_plan');
    await _removeEvents((await events(id)).map((e) => e.id).toList());
    await db.customStatement('DELETE FROM finance_plans WHERE id=?', [id]);
    await (db.delete(db.accounts)..where((a) => a.id.equals(plan.accountId))).go();
  });

  Future<void> _removeEvents(List<int> ids, {bool keepOwnedTransactions = false}) async {
    for (final id in ids) {
      final links = await db.customSelect(
        'SELECT * FROM finance_transaction_links WHERE event_id=?',
        variables: [d.Variable(id)]).get();
      await db.customStatement('DELETE FROM finance_transaction_links WHERE event_id=?', [id]);
      for (final link in links) {
        final txId = link.read<int>('transaction_id');
        if (link.read<int>('owned') == 1) {
          if (!keepOwnedTransactions) {
            // Keep physical files until orphan clean-up: unlinking a file cannot
            // be rolled back if a later SQL write in this operation fails.
            await (db.delete(db.transactionTags)
              ..where((t) => t.transactionId.equals(txId))).go();
            await (db.delete(db.transactionAttachments)
              ..where((a) => a.transactionId.equals(txId))).go();
            await (db.delete(db.transactions)
              ..where((t) => t.id.equals(txId))).go();
          }
        } else {
          await (db.update(db.transactions)..where((t) => t.id.equals(txId)))
            .write(TransactionsCompanion(
              excludeFromStats: d.Value(link.read<int>('original_exclude_stats') == 1),
              excludeFromBudget: d.Value(link.read<int>('original_exclude_budget') == 1)));
        }
      }
      await db.customStatement('DELETE FROM finance_events WHERE id=?', [id]);
    }
  }

  /// Called by ledger deletion/clear inside the caller's outer transaction.
  /// The ordinary clear then removes all cash transactions in that same unit.
  Future<void> clearLedger(int ledgerId) => db.transaction(() async {
    final plans = await list(ledgerId);
    for (final plan in plans) {
      await _removeEvents((await events(plan.id)).map((e) => e.id).toList(),
        keepOwnedTransactions: true);
      await db.customStatement('DELETE FROM finance_plans WHERE id=?', [plan.id]);
      await (db.delete(db.accounts)..where((a) => a.id.equals(plan.accountId))).go();
    }
  });

  static String _day(DateTime date) => financeDay(date).toIso8601String().substring(0, 10);
}
