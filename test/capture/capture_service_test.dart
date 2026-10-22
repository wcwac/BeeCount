import 'dart:async';

import 'package:beecount/services/capture/capture_service.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  final binding = TestWidgetsFlutterBinding.ensureInitialized();
  tearDown(() => binding.defaultBinaryMessenger
      .setMockMethodCallHandler(CaptureService.channel, null));

  testWidgets('an unresponsive inbox read times out and a fresh read can recover', (tester) async {
    final delayedReply = Completer<List<Object?>>();
    addTearDown(() {
      if (!delayedReply.isCompleted) delayedReply.complete([]);
    });
    var calls = 0;
    binding.defaultBinaryMessenger.setMockMethodCallHandler(CaptureService.channel, (call) async {
      expect(call.method, 'listCandidates');
      calls++;
      return calls == 1 ? delayedReply.future : <Object?>[];
    });
    final failedRead = expectLater(CaptureService().pending(), throwsA(isA<TimeoutException>()));
    await tester.pump();
    await tester.pump(const Duration(seconds: 5));
    await failedRead;
    expect(await CaptureService().pending(), isEmpty);
    expect(calls, 2);
    delayedReply.complete([]);
    await tester.pump();
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  testWidgets('a missing inbox response is reported instead of showing an empty inbox', (tester) async {
    binding.defaultBinaryMessenger.setMockMethodCallHandler(CaptureService.channel, (call) async => null);
    await expectLater(CaptureService().pending(), throwsFormatException);
  }, variant: TargetPlatformVariant.only(TargetPlatform.android));

  test('repeated acknowledgements share one bridge call and clear after success', () async {
    final reply = Completer<void>();
    final started = Completer<void>();
    var calls = 0;
    final pending = <Future<void>>[];
    addTearDown(() async {
      if (!reply.isCompleted) reply.complete();
      await Future.wait(pending).timeout(const Duration(seconds: 5));
    });
    binding.defaultBinaryMessenger.setMockMethodCallHandler(CaptureService.channel, (call) async {
      calls++;
      if (!started.isCompleted) started.complete();
      await reply.future;
      return null;
    });
    pending.add(CaptureService().acknowledge('shared', 1));
    pending.add(CaptureService().acknowledge('shared', 1));
    await started.future.timeout(const Duration(seconds: 5));
    expect(calls, 1);
    reply.complete();
    await Future.wait(pending).timeout(const Duration(seconds: 5));

    await CaptureService().acknowledge('shared', 1).timeout(const Duration(seconds: 5));
    expect(calls, 2);
  });

  test('a failed acknowledgement remains retryable', () async {
    var calls = 0;
    binding.defaultBinaryMessenger.setMockMethodCallHandler(CaptureService.channel, (call) async {
      if (++calls == 1) throw PlatformException(code: 'TEMPORARILY_UNAVAILABLE');
      return null;
    });
    await expectLater(CaptureService().acknowledge('retry', 2), throwsA(isA<PlatformException>()));
    await CaptureService().acknowledge('retry', 2).timeout(const Duration(seconds: 5));
    expect(calls, 2);
  });

  test('outstanding bridge calls are bounded and rejected work can retry', () async {
    final reply = Completer<void>();
    final started = Completer<void>();
    var calls = 0;
    final pending = <Future<void>>[];
    addTearDown(() async {
      if (!reply.isCompleted) reply.complete();
      await Future.wait(pending).timeout(const Duration(seconds: 5));
    });
    binding.defaultBinaryMessenger.setMockMethodCallHandler(CaptureService.channel, (call) async {
      if (++calls == 32) started.complete();
      await reply.future;
      return null;
    });
    for (var i = 0; i < 32; i++) {
      pending.add(CaptureService().acknowledge('pending-$i', i + 1));
    }
    await started.future.timeout(const Duration(seconds: 5));
    await expectLater(CaptureService().acknowledge('retry-after-capacity', 33),
      throwsA(isA<PlatformException>().having((e) => e.code, 'code', 'CAPTURE_BUSY')));
    expect(calls, 32);
    reply.complete();
    await Future.wait(pending).timeout(const Duration(seconds: 5));

    await CaptureService().acknowledge('retry-after-capacity', 33)
        .timeout(const Duration(seconds: 5));
    expect(calls, 33);
  });
}
