import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import '../../models/capture_candidate.dart';

class CaptureService {
  static const channel = MethodChannel('beecount/local_capture');
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
  Future<void> acknowledge(String id, int transactionId) =>
      channel.invokeMethod<void>('ackCommitted', {'id': id, 'transactionId': transactionId});
  Future<void> dismiss(String id) => channel.invokeMethod<void>('dismissCandidate', {'id': id});
  Future<void> acknowledgeOverflow(int observedCursor) =>
      channel.invokeMethod<void>('acknowledgeOverflow', {'observedCursor': observedCursor});
}
