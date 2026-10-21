import 'dart:io';

import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/models/capture_candidate.dart';
import 'package:beecount/services/capture/capture_commit_service.dart';
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('background database preserves capture retries and deletion across reopen',
      () async {
    SharedPreferences.setMockInitialValues({});
    final directory =
        await Directory.systemTemp.createTemp('capture-persistence-');
    final file = File('${directory.path}/ledger.sqlite');
    var db = BeeDatabase.forTesting(NativeDatabase.createInBackground(file));
    try {
      var repository = LocalRepository(db);
      var capture = CaptureCommitService(repository);
      final ledgerId = await repository.createLedger(name: 'Persistent capture');
      final accountId = await repository.createAccount(
        ledgerId: ledgerId,
        name: 'Cash',
        initialBalance: 100,
      );
      final ledger = (await repository.getLedgerById(ledgerId))!;
      final time = DateTime(2026, 10, 22, 10);
      final candidate = CaptureCandidate.fromMap({
        'id': 'payment-after-restart',
        'sourcePackage': 'com.tencent.mm',
        'appName': 'Test app',
        'ledgerId': ledgerId,
        'ledgerKey': CaptureCommitService.ledgerKey(ledger),
        'observedAt': time.millisecondsSinceEpoch,
        'amountMinor': 1250,
        'merchant': 'Test shop',
        'direction': 'expense',
        'currency': 'CNY',
        'parserVersion': 'fixture-v1',
        'issues': <String>[],
      });
      Future<CaptureCommitResult> save() => capture.commit(
            candidate: candidate,
            type: 'expense',
            amount: 12.5,
            happenedAt: time,
            accountId: accountId,
          );

      final first = await save();
      expect(first.created, isTrue);
      await db.close();

      // Match the production background-isolate connection, including native
      // SQLite asset loading, rather than sharing a memory database instance.
      db = BeeDatabase.forTesting(NativeDatabase.createInBackground(file));
      repository = LocalRepository(db);
      capture = CaptureCommitService(repository);
      final retry = await save();
      expect(retry.created, isFalse);
      expect(retry.transactionId, first.transactionId);
      expect(await db.select(db.transactions).get(), hasLength(1));
      expect(await repository.getAccountBalance(accountId), closeTo(87.5, 1e-9));
      expect(await capture.handled([candidate.id]),
          {candidate.id: first.transactionId});

      await repository.deleteTransaction(first.transactionId);
      await db.close();

      db = BeeDatabase.forTesting(NativeDatabase.createInBackground(file));
      repository = LocalRepository(db);
      capture = CaptureCommitService(repository);
      expect(await capture.handled([candidate.id]), {candidate.id: null});
      await expectLater(save(), throwsStateError);
      expect(await db.select(db.transactions).get(), isEmpty);
      expect(await repository.getAccountBalance(accountId), closeTo(100, 1e-9));
    } finally {
      await db.close();
      await directory.delete(recursive: true);
    }
  });
}
