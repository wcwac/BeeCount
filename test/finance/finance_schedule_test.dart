import 'package:beecount/models/finance.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  List<RepaymentRow> schedule(int principal, int rate, int periods,
      {RepaymentMethod method = RepaymentMethod.annuity,
      DateTime? firstDue}) => repaymentSchedule(
        principalCents: principal, annualRateBps: rate, periods: periods,
        firstDueAt: firstDue ?? DateTime(2026, 1, 31), method: method);

  test('zero-rate payments conserve every cent and finish at zero', () {
    for (final method in RepaymentMethod.values) {
      final rows = schedule(10001, 0, 12, method: method);
      expect(rows.fold<int>(0, (n, r) => n + r.principalCents), 10001);
      expect(rows.every((r) => r.interestCents == 0), isTrue);
      expect(rows.last.balanceCents, 0);
    }
  });

  test('month-end schedule recovers original day after February', () {
    final rows = schedule(100000, 300, 4);
    expect(rows.map((r) => r.dueAt), [DateTime(2026, 1, 31),
      DateTime(2026, 2, 28), DateTime(2026, 3, 31), DateTime(2026, 4, 30)]);
    final leap = schedule(10000, 0, 2, firstDue: DateTime(2028, 1, 31));
    expect(leap.last.dueAt, DateTime(2028, 2, 29));
  });

  test('annuity uses opening principal for first monthly interest', () {
    // 120,000 at 6% APR: first interest = 600.
    final rows = schedule(12000000, 600, 12);
    expect(rows.first.interestCents, 60000);
    expect(rows.first.totalCents, closeTo(1032797, 1));
    expect(rows.last.balanceCents, 0);
    expect(rows.fold<int>(0, (sum, r) => sum + r.principalCents), 12000000);
    expect(rows.take(11).map((r) => r.totalCents).toSet().length, 1);
  });

  test('equal principal repayments have decreasing interest', () {
    final rows = schedule(12000000, 600, 12,
      method: RepaymentMethod.equalPrincipal);
    expect(rows.every((r) => r.principalCents == 1000000), isTrue);
    expect(rows.first.totalCents, 1060000);
    expect(rows.last.totalCents, 1005000);
  });

  test('small balances and long schedules never overpay principal', () {
    for (final method in RepaymentMethod.values) {
      for (final principal in [600, 601, 10001, 100000000000000]) {
        for (final rate in [0, 1, 600, 10000]) {
          final rows = schedule(principal, rate, 600, method: method);
          expect(rows.every((r) => r.principalCents > 0 &&
            r.interestCents >= 0 && r.balanceCents >= 0), isTrue);
          expect(rows.last.balanceCents, 0);
          expect(rows.fold<int>(0, (sum, r) => sum + r.principalCents), principal);
        }
      }
    }
  });

  test('invalid contract terms are rejected before any plan exists', () {
    for (final args in [(0, 0, 12), (100, -1, 12), (100, 10001, 12),
      (100, 0, 0), (100, 0, 601), (1, 0, 12),
      (100000000000001, 0, 12)]) {
      expect(() => schedule(args.$1, args.$2, args.$3),
        throwsA(isA<FinanceFailure>()));
    }
  });
}
