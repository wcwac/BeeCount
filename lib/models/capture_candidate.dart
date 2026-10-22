/// Persisted native candidate. No raw accessibility tree crosses into storage.
class CaptureCandidate {
  final Map<String, dynamic> payload;
  CaptureCandidate.fromMap(Map<dynamic, dynamic> map)
      : payload = Map<String, dynamic>.unmodifiable(Map<String, dynamic>.from(map)) {
    final candidateId = payload['id'], ledger = payload['ledgerId'], package = payload['sourcePackage'];
    if (candidateId is! String || candidateId.isEmpty || candidateId.length > 100 ||
        ledger is! int || ledger <= 0 || package is! String || package.isEmpty || package.length > 255) {
      throw const FormatException('Invalid capture identity');
    }
    _integer('observedAt', isRequired: true, min: 1, max: 8640000000000000);
    _integer('transactionTime', min: 1, max: 8640000000000000);
    _integer('amountMinor', min: 1, max: 99999999999);
    _integer('discountMinor', min: 0, max: 99999999999);
    for (final key in ['appName', 'merchant', 'remark', 'paymentMethod', 'ledgerKey',
        'adapterId', 'sessionId', 'parserVersion', 'referenceHash', 'transactionIdentity', 'source']) {
      final value = payload[key];
      if (value != null && (value is! String || value.length > 512)) {
        throw FormatException('Invalid capture $key');
      }
    }
    final direction = payload['direction'], currency = payload['currency'];
    if ((direction != null && !const ['expense', 'income', 'transfer'].contains(direction)) ||
        (currency != null && (currency is! String || !_currencyPattern.hasMatch(currency)))) {
      throw const FormatException('Invalid capture direction or currency');
    }
    final issues = payload['issues'], evidence = payload['evidence'];
    if (issues != null && (issues is! List || issues.length > 32 ||
        issues.any((value) => value is! String || value.length > 80))) {
      throw const FormatException('Invalid capture issues');
    }
    if (evidence != null && (evidence is! Map || evidence.length > 32 || evidence.entries.any(
        (entry) => entry.key is! String || (entry.key as String).length > 80 ||
            entry.value is! String || (entry.value as String).length > 240))) {
      throw const FormatException('Invalid capture evidence');
    }
  }
  static final _currencyPattern = RegExp(r'^[A-Z]{3}$');
  void _integer(String key, {bool isRequired = false, required int min, required int max}) {
    final value = payload[key];
    if ((value == null && isRequired) || (value != null && (value is! int || value < min || value > max))) {
      throw FormatException('Invalid capture $key');
    }
  }
  String get id => payload['id'] as String;
  String get sourcePackage => payload['sourcePackage'] as String;
  String get appName => payload['appName'] as String? ?? sourcePackage;
  int get ledgerId => (payload['ledgerId'] as num).toInt();
  String? get ledgerKey => payload['ledgerKey'] as String?;
  int? get amountMinor => (payload['amountMinor'] as num?)?.toInt();
  String? get merchant => payload['merchant'] as String?;
  String? get remark => payload['remark'] as String?;
  String? get paymentMethod => payload['paymentMethod'] as String?;
  String? get direction => payload['direction'] as String?;
  String get currency => payload['currency'] as String? ?? 'CNY';
  String get parserVersion => payload['parserVersion'] as String? ?? 'unknown';
  List<String> get issues => (payload['issues'] as List? ?? const []).cast<String>();
  DateTime get observedAt => DateTime.fromMillisecondsSinceEpoch((payload['observedAt'] as num).toInt());
  DateTime get transactionTime => payload['transactionTime'] == null
      ? observedAt : DateTime.fromMillisecondsSinceEpoch((payload['transactionTime'] as num).toInt());
  String? get initialNote {
    final value = [merchant, remark].whereType<String>()
        .where((s) => s.isNotEmpty).toSet().join(' · ');
    return value.isEmpty ? null : value;
  }
}

class CaptureAppInfo {
  final String packageName, name, adapter;
  final bool limited;
  CaptureAppInfo.fromMap(Map<dynamic, dynamic> map)
      : packageName = map['packageName'] as String,
        name = map['name'] as String,
        adapter = map['adapter'] as String,
        limited = map['limited'] == true;
}

class CaptureStatus {
  final bool supported, permissionGranted, connected, enabled, notifications, notificationsGranted,
      notificationsConnected;
  final int? ledgerId;
  final String? ledgerKey;
  final int pendingCount, skippedObservations, overflowCursor;
  final bool importantObservationsSkipped;
  final Set<String> packages;
  final List<CaptureAppInfo> apps;
  final String? lastError;
  final CaptureDiagnostics? diagnostics;
  CaptureStatus.fromMap(Map<dynamic, dynamic> map)
      : supported = map['supported'] == true,
        permissionGranted = map['permissionGranted'] == true,
        connected = map['serviceConnected'] == true,
        enabled = (map['settings'] as Map?)?['enabled'] == true,
        notifications = (map['settings'] as Map?)?['notifications'] == true,
        notificationsGranted = map['notificationsGranted'] == true,
        notificationsConnected = map['notificationsConnected'] == true,
        ledgerId = (((map['settings'] as Map?)?['ledgerId']) as num?)?.toInt(),
        ledgerKey = (map['settings'] as Map?)?['ledgerKey'] as String?,
        pendingCount = (map['pendingCount'] as num?)?.toInt() ?? 0,
        skippedObservations = (map['skippedObservations'] as num?)?.toInt() ?? 0,
        overflowCursor = (map['overflowCursor'] as num?)?.toInt() ?? 0,
        importantObservationsSkipped = map['importantObservationsSkipped'] == true,
        packages = Set<String>.unmodifiable((((map['settings'] as Map?)?['packages']) as List? ?? const []).cast<String>()),
        apps = List<CaptureAppInfo>.unmodifiable((map['apps'] as List? ?? const [])
            .map((a) => CaptureAppInfo.fromMap(a as Map))),
        lastError = map['lastError'] as String?,
        diagnostics = map['diagnostics'] is Map
            ? CaptureDiagnostics.fromMap(map['diagnostics'] as Map) : null;
}

/// Optional, in-memory capture metadata. Never accepts page text or draft data.
class CaptureDiagnostics {
  final int? startedAt, lastEventAt, lastReadAt, lastProcessedAt;
  final int eventCount, readCount, processedCount, savedCount;
  final int? lastNodeCount, lastTextCount;
  final String? lastEventPackage, lastReadPackage, lastProcessedPackage;
  final String? lastEventSource, lastReadSource, lastProcessedSource;
  final String? lastReadResult, lastOutcome;

  CaptureDiagnostics.fromMap(Map<dynamic, dynamic> map)
      : startedAt = _timestamp(map['startedAt']),
        eventCount = _count(map['eventCount']) ?? 0,
        readCount = _count(map['readCount']) ?? 0,
        processedCount = _count(map['processedCount']) ?? 0,
        savedCount = _count(map['savedCount']) ?? 0,
        lastEventAt = _timestamp(map['lastEventAt']),
        lastEventPackage = _package(map['lastEventPackage']),
        lastEventSource = _source(map['lastEventSource']),
        lastReadAt = _timestamp(map['lastReadAt']),
        lastReadPackage = _package(map['lastReadPackage']),
        lastReadSource = _source(map['lastReadSource']),
        lastReadResult = code(map['lastReadResult']),
        lastNodeCount = _count(map['lastNodeCount']),
        lastTextCount = _count(map['lastTextCount']),
        lastProcessedAt = _timestamp(map['lastProcessedAt']),
        lastProcessedPackage = _package(map['lastProcessedPackage']),
        lastProcessedSource = _source(map['lastProcessedSource']),
        lastOutcome = code(map['lastOutcome']);

  static int? _count(Object? value) =>
      value is int && value >= 0 ? value : null;
  static int? _timestamp(Object? value) => value is int && value > 0 &&
      value <= 8640000000000000 ? value : null;
  static String? _package(Object? value) => value is String &&
      value.length <= 255 && _packagePattern.hasMatch(value) ? value : null;
  static String? _source(Object? value) =>
      value == 'accessibility' || value == 'notification' ? value as String : null;
  static String? code(Object? value) => value is String &&
      value.length <= 80 && _codePattern.hasMatch(value) ? value : null;
  static final _packagePattern = RegExp(r'^[A-Za-z_]\w*(?:\.[A-Za-z_]\w*)+$');
  static final _codePattern = RegExp(r'^[A-Z][A-Z0-9_]*$');

  /// Explicit allowlist used for copying diagnostics. Unknown native fields,
  /// including any future transaction payloads, cannot enter the clipboard.
  Map<String, Object?> toMetadata() => {
    'startedAt': startedAt,
    'eventCount': eventCount, 'readCount': readCount,
    'processedCount': processedCount, 'savedCount': savedCount,
    'lastEventAt': lastEventAt, 'lastEventPackage': lastEventPackage,
    'lastEventSource': lastEventSource,
    'lastReadAt': lastReadAt, 'lastReadPackage': lastReadPackage,
    'lastReadSource': lastReadSource, 'lastReadResult': lastReadResult,
    'lastNodeCount': lastNodeCount, 'lastTextCount': lastTextCount,
    'lastProcessedAt': lastProcessedAt, 'lastProcessedPackage': lastProcessedPackage,
    'lastProcessedSource': lastProcessedSource, 'lastOutcome': lastOutcome,
  };
}
