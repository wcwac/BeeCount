import 'package:beecount/models/capture_candidate.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  Map<String, Object?> valid() => {
    'id': 'draft-one', 'sourcePackage': 'com.tencent.mm', 'ledgerId': 1,
    'observedAt': 1792598400000, 'amountMinor': 1250,
    'direction': 'expense', 'currency': 'CNY', 'issues': <String>[],
  };

  test('incomplete monetary fields are retained for manual correction', () {
    final data = {...valid(), 'amountMinor': null, 'direction': null};
    final candidate = CaptureCandidate.fromMap(data);
    expect(candidate.amountMinor, isNull);
    expect(candidate.direction, isNull);
    expect(candidate.transactionTime, candidate.observedAt);
  });

  test('malformed metadata is rejected before an inbox or backup can accept it', () {
    final invalid = <Map<String, Object?>>[
      {'ledgerId': 1.5}, {'observedAt': null}, {'observedAt': -1},
      {'observedAt': 8640000000000001}, {'amountMinor': 12.5}, {'amountMinor': 0},
      {'amountMinor': 100000000000}, {'transactionTime': '2026-10-21'},
      {'direction': 'purchase'}, {'currency': '人民币'}, {'merchant': 100},
      {'issues': [100]}, {'evidence': {'amount': 100}},
    ];
    for (final fields in invalid) {
      expect(() => CaptureCandidate.fromMap({...valid(), ...fields}), throwsFormatException,
          reason: fields.keys.join(','));
    }
  });
}
