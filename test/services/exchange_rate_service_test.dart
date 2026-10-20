import 'package:flutter_test/flutter_test.dart';
import 'package:beecount/services/currency/exchange_rate_service.dart';

void main() {
  test('public exchange-rate fetching is unavailable', () async {
    await expectLater(
      ExchangeRateService().fetch('CNY'),
      throwsA(isA<RateFetchException>()),
    );
  });
}
