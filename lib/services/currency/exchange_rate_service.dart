/// Result contract retained for an explicitly supplied rate source.
class RateFetchResult {
  final String rateDate;
  final String source;
  final Map<String, String> ratesBaseToQuote;
  const RateFetchResult({
    required this.rateDate,
    required this.source,
    required this.ratesBaseToQuote,
  });
}

class RateFetchException implements Exception {
  final String message;
  RateFetchException(this.message);
  @override
  String toString() => 'RateFetchException: $message';
}

/// Public rate fetching is removed. Callers may use cached/manual rates or
/// the user's configured BeeCount Cloud; failure must never contact a public
/// fallback endpoint.
class ExchangeRateService {
  Future<RateFetchResult> fetch(String base) async {
    throw RateFetchException('Public exchange-rate requests are disabled.');
  }
}
