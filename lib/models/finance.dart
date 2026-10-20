import 'dart:math' as math;

const financeAccountTypes = {
  'finance_receivable',
  'finance_debt',
  'finance_investment',
};

enum FinanceKind {
  reimbursement,
  lend,
  borrow,
  installment,
  mortgage,
  investment;

  bool get isDebt => this == borrow || isScheduled;
  bool get isScheduled => this == installment || this == mortgage;
  String get accountType => this == investment
      ? 'finance_investment'
      : isDebt
          ? 'finance_debt'
          : 'finance_receivable';
}

enum RepaymentMethod { annuity, equalPrincipal }

enum FinanceAction { settle, buy, sell, dividend, valuation }

/// Stable error codes are translated by the presentation layer.
class FinanceFailure implements Exception {
  final String code;
  const FinanceFailure(this.code);
  @override
  String toString() => 'FinanceFailure($code)';
}

class FinanceDraft {
  final int ledgerId, amountCents, annualRateBps, periods, unitsMicros;
  final FinanceKind kind;
  final String title, party, note;
  final int? cashAccountId, existingExpenseId;
  final bool recordCashFlow;
  final DateTime openedAt;
  final DateTime? firstDueAt;
  final RepaymentMethod method;
  const FinanceDraft({
    required this.ledgerId,
    required this.kind,
    required this.title,
    required this.amountCents,
    required this.openedAt,
    this.party = '',
    this.note = '',
    this.cashAccountId,
    this.existingExpenseId,
    this.recordCashFlow = true,
    this.annualRateBps = 0,
    this.periods = 12,
    this.method = RepaymentMethod.annuity,
    this.firstDueAt,
    this.unitsMicros = 1000000,
  });
}

class FinancePosting {
  final FinanceAction action;
  final DateTime happenedAt;
  final int? cashAccountId, periodNumber;
  final int principalCents, interestCents, amountCents, unitsMicros;
  final String note;
  const FinancePosting({
    required this.action,
    required this.happenedAt,
    this.cashAccountId,
    this.periodNumber,
    this.principalCents = 0,
    this.interestCents = 0,
    this.amountCents = 0,
    this.unitsMicros = 0,
    this.note = '',
  });
}

class FinancePlan {
  final int id, ledgerId, accountId, principalCents, annualRateBps, periods;
  final FinanceKind kind;
  final RepaymentMethod method;
  final String title, party, currency, note;
  final DateTime openedAt, firstDueAt;
  final int outstandingCents, unitsMicros, valueCents;
  final int realizedProfitCents, paidInterestCents;
  int get costCents => outstandingCents;
  bool get isClosed =>
      kind == FinanceKind.investment ? unitsMicros == 0 : outstandingCents == 0;
  const FinancePlan({
    required this.id,
    required this.ledgerId,
    required this.accountId,
    required this.principalCents,
    required this.annualRateBps,
    required this.periods,
    required this.kind,
    required this.method,
    required this.title,
    required this.party,
    required this.currency,
    required this.note,
    required this.openedAt,
    required this.firstDueAt,
    required this.outstandingCents,
    required this.unitsMicros,
    required this.valueCents,
    required this.realizedProfitCents,
    required this.paidInterestCents,
  });
  factory FinancePlan.fromMap(Map<String, dynamic> m) => FinancePlan(
        id: m['id'] as int,
        ledgerId: m['ledger_id'] as int,
        accountId: m['account_id'] as int,
        principalCents: m['principal_cents'] as int,
        annualRateBps: m['annual_rate_bps'] as int,
        periods: m['periods'] as int,
        kind: FinanceKind.values.byName(m['kind'] as String),
        method: RepaymentMethod.values.byName(m['method'] as String),
        title: m['title'] as String,
        party: m['party'] as String,
        currency: m['currency'] as String,
        note: m['note'] as String,
        openedAt: DateTime.parse(m['opened_at'] as String),
        firstDueAt: DateTime.parse(m['first_due_at'] as String),
        outstandingCents: m['outstanding_cents'] as int,
        unitsMicros: m['units_micros'] as int,
        valueCents: m['value_cents'] as int,
        realizedProfitCents: m['profit_cents'] as int,
        paidInterestCents: m['interest_cents'] as int,
      );
}

class FinanceEvent {
  final int id, planId, principalDeltaCents, unitsDeltaMicros, profitCents;
  final int interestCents, cashCents, valueAfterCents;
  final int? periodNumber;
  final String action, note;
  final DateTime happenedAt;
  const FinanceEvent({
    required this.id,
    required this.planId,
    required this.principalDeltaCents,
    required this.unitsDeltaMicros,
    required this.profitCents,
    required this.interestCents,
    required this.cashCents,
    required this.valueAfterCents,
    required this.periodNumber,
    required this.action,
    required this.note,
    required this.happenedAt,
  });
  factory FinanceEvent.fromMap(Map<String, dynamic> m) => FinanceEvent(
        id: m['id'] as int,
        planId: m['plan_id'] as int,
        principalDeltaCents: m['principal_delta_cents'] as int,
        unitsDeltaMicros: m['units_delta_micros'] as int,
        profitCents: m['profit_cents'] as int,
        interestCents: m['interest_cents'] as int,
        cashCents: m['cash_cents'] as int,
        valueAfterCents: m['value_after_cents'] as int,
        periodNumber: m['period_number'] as int?,
        action: m['action'] as String,
        note: m['note'] as String,
        happenedAt: DateTime.parse(m['happened_at'] as String),
      );
}

class RepaymentRow {
  final int number, principalCents, interestCents, balanceCents;
  final DateTime dueAt;
  int get totalCents => principalCents + interestCents;
  const RepaymentRow(
      {required this.number,
      required this.principalCents,
      required this.interestCents,
      required this.balanceCents,
      required this.dueAt});
}

DateTime financeDay(DateTime value) =>
    DateTime(value.year, value.month, value.day);

/// Use the original day every month: Jan 31 → Feb 28 → Mar 31.
DateTime financeMonth(DateTime origin, int offset) {
  final first = DateTime(origin.year, origin.month + offset, 1);
  final lastDay = DateTime(first.year, first.month + 1, 0).day;
  return DateTime(first.year, first.month, math.min(origin.day, lastDay));
}

/// Integer cents throughout; the last instalment consumes the rounding residue.
/// Fixed-rate monthly schedule; APR is a nominal annual rate in basis points.
List<RepaymentRow> repaymentSchedule({
  required int principalCents,
  required int annualRateBps,
  required int periods,
  required DateTime firstDueAt,
  required RepaymentMethod method,
}) {
  if (principalCents <= 0 ||
      principalCents > 100000000000000 ||
      periods < 1 ||
      periods > 600 ||
      principalCents < periods ||
      annualRateBps < 0 ||
      annualRateBps > 10000) {
    throw const FinanceFailure('invalid_terms');
  }
  final rate = annualRateBps / 120000;
  final payment = rate == 0
      ? (principalCents / periods).round()
      : (principalCents * rate / (1 - math.pow(1 + rate, -periods))).round();
  final principalPart = principalCents ~/ periods;
  var remaining = principalCents;
  final result = <RepaymentRow>[];
  for (var n = 1; n <= periods; n++) {
    final interest = (remaining * annualRateBps + 60000) ~/ 120000;
    var principal = method == RepaymentMethod.equalPrincipal
        ? principalPart
        : payment - interest;
    // Keep at least one cent for every remaining period, even for tiny loans.
    if (n == periods) {
      principal = remaining;
    } else {
      principal = principal.clamp(1, remaining - (periods - n)).toInt();
    }
    remaining -= principal;
    result.add(RepaymentRow(
        number: n,
        principalCents: principal,
        interestCents: interest,
        balanceCents: remaining,
        dueAt: financeMonth(firstDueAt, n - 1)));
  }
  return result;
}
