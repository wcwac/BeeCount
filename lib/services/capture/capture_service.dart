import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import '../../models/capture_candidate.dart';

class CaptureService {
  static const channel = MethodChannel('beecount/local_capture');
  static final _completions = <({String id, int? transactionId}), Future<void>>{};
  bool get supported => !kIsWeb && defaultTargetPlatform == TargetPlatform.android;

  Future<CaptureStatus> status() async {
    if (!supported) return CaptureStatus.fromMap(const {'supported': false});
    final map = await channel.invokeMapMethod<String, dynamic>('getStatus');
    if (map == null) throw const FormatException('Missing capture status');
    return CaptureStatus.fromMap(map);
  }
  Future<void> updateSettings({required bool enabled, required Set<String> packages, required int? ledgerId,
      required String? ledgerKey, required bool notifications}) =>
      channel.invokeMethod<void>('updateSettings', {'enabled': enabled, 'packages': packages.toList(),
        'ledgerId': ledgerId, 'ledgerKey': ledgerKey, 'notifications': notifications});
  Future<void> openSettings() => channel.invokeMethod<void>('openAccessibilitySettings');
  Future<void> openNotificationSettings() => channel.invokeMethod<void>('openNotificationSettings');
  Future<List<CaptureCandidate>> pending() async {
    if (!supported) return const [];
    final list = await channel.invokeListMethod<dynamic>('listCandidates', {'limit': 1000});
    return (list ?? const []).map((v) => CaptureCandidate.fromMap(v as Map)).toList(growable: false);
  }
  Future<void> acknowledge(String id, int transactionId) => _completeCandidate(id, transactionId);
  Future<void> dismiss(String id) => _completeCandidate(id, null);
  bool isDismissing(String id) => _completions.containsKey((id: id, transactionId: null));

  Future<void> _completeCandidate(String id, int? transactionId) {
    final key = (id: id, transactionId: transactionId);
    final pending = _completions[key];
    if (pending != null) return pending;
    // Keep late bridge calls bounded across refreshes and reopened pages.
    // Rejected calls leave native drafts intact; the next inbox load retries
    // their durable ledger links after an outstanding call has completed.
    if (_completions.length >= 32) {
      return Future<void>.error(PlatformException(code: 'CAPTURE_BUSY'));
    }
    final result = channel.invokeMethod<void>(
      transactionId == null ? 'dismissCandidate' : 'ackCommitted',
      {'id': id, if (transactionId != null) 'transactionId': transactionId},
    ).whenComplete(() { _completions.remove(key); });
    _completions[key] = result;
    return result;
  }
  Future<void> acknowledgeOverflow(int observedCursor) =>
      channel.invokeMethod<void>('acknowledgeOverflow', {'observedCursor': observedCursor});
}
