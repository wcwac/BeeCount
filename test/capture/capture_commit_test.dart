import 'dart:convert';
import 'dart:typed_data';
import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/models/capture_candidate.dart';
import 'package:beecount/models/prepared_attachment.dart';
import 'package:beecount/services/backup/local_database_backup.dart';
import 'package:beecount/services/capture/capture_commit_service.dart';
import 'package:beecount/utils/shared_ledger_picker_filter.dart';
import 'package:drift/drift.dart' show Value;
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late BeeDatabase db;
  late LocalRepository repo;
  late CaptureCommitService capture;
  late int ledgerId;
  late String ledgerKey;
  final time = DateTime(2026, 10, 21, 10);
  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    db = BeeDatabase.forTesting(NativeDatabase.memory());
    repo = LocalRepository(db);
    capture = CaptureCommitService(repo);
    ledgerId = await repo.createLedger(name: 'Capture ledger');
    ledgerKey = CaptureCommitService.ledgerKey((await repo.getLedgerById(ledgerId))!);
  });
  tearDown(() => db.close());

  CaptureCandidate candidate(String id, {String? packageName, String? identity}) => CaptureCandidate.fromMap({
    'id': id, 'sourcePackage': packageName ?? 'com.tencent.mm', 'appName': 'Test app',
    'ledgerId': ledgerId, 'ledgerKey': identity ?? ledgerKey,
    'observedAt': time.millisecondsSinceEpoch, 'amountMinor': 1250,
    'merchant': 'Test shop', 'direction': 'expense', 'currency': 'CNY',
    'parserVersion': 'fixture-v1', 'issues': <String>[],
  });
  Future<CaptureCommitResult> save(CaptureCandidate c) => capture.commit(
    candidate: c, type: 'expense', amount: 12.50, happenedAt: time,
  );
  Future<int> count() async => (await db.customSelect('SELECT COUNT(*) AS n FROM transactions').getSingle()).read<int>('n');

  test('retry after a lost native acknowledgement does not insert twice', () async {
    final first = await save(candidate('one'));
    final retry = await save(candidate('one'));
    expect(first.created, isTrue);
    expect(retry.created, isFalse);
    expect(first.transactionId, retry.transactionId);
    expect(await count(), 1);
    expect(await capture.handled(['one', 'unknown']), {'one': first.transactionId});
  });

  test('independent equal-amount payments are not automatically merged', () async {
    await save(candidate('one'));
    await save(candidate('two'));
    expect(await count(), 2);
  });

  test('explicit duplicate confirmation links sources without a new transaction', () async {
    final first = await save(candidate('payment'));
    final shopping = candidate('shopping', packageName: 'com.sankuai.meituan');
    expect((await capture.possibleMatches(shopping)).map((t) => t.id), contains(first.transactionId));
    await capture.linkExisting(shopping, (await repo.getTransactionById(first.transactionId))!);
    final retry = await save(shopping);
    expect(retry.transactionId, first.transactionId);
    expect(await count(), 1);
  });

  test('a changed selected record cannot receive a stale duplicate association', () async {
    final first = await save(candidate('payment'));
    final selected = (await repo.getTransactionById(first.transactionId))!;
    final shopping = candidate('shopping', packageName: 'com.sankuai.meituan');
    await repo.updateTransaction(id: selected.id, type: 'income', amount: 12.5, happenedAt: time);
    await expectLater(capture.linkExisting(shopping, selected), throwsStateError);
    expect(await capture.handled(['shopping']), isEmpty);
    expect(await count(), 1);
  });

  test('uncertain currency is not suggested or linked as a confirmed CNY duplicate', () async {
    final first = await save(candidate('payment'));
    final uncertain = CaptureCandidate.fromMap({...candidate('uncertain').payload,
      'issues': ['CURRENCY_UNCERTAIN']});
    expect(await capture.possibleMatches(uncertain), isEmpty);
    await expectLater(capture.linkExisting(uncertain,
      (await repo.getTransactionById(first.transactionId))!), throwsStateError);
    expect(await capture.handled(['uncertain']), isEmpty);
  });

  test('a metadata insert failure rolls back the financial write', () async {
    await db.customStatement("CREATE TRIGGER fail_capture BEFORE INSERT ON transaction_capture_links BEGIN SELECT RAISE(ABORT,'test'); END");
    await expectLater(save(candidate('one')), throwsA(anything));
    expect(await count(), 0);
    expect(await capture.handled(['one']), isEmpty);
  });

  test('attachment row failure rolls back payment, balance, tags and capture link together', () async {
    final account = await repo.createAccount(ledgerId: ledgerId, name: 'Cash', initialBalance: 100);
    final tag = await repo.createTag(name: 'Lunch');
    const images = [
      PreparedAttachment(fileName: 'sha_fixture_one.jpg', originalName: 'one.jpg', fileSize: 10, sortOrder: 0),
      PreparedAttachment(fileName: 'sha_fixture_two.jpg', originalName: 'two.jpg', fileSize: 20, sortOrder: 1),
    ];
    Future<CaptureCommitResult> confirm() => capture.commit(candidate: candidate('with-images'),
      type: 'expense', amount: 12.5, happenedAt: time, accountId: account,
      tagIds: [tag], attachments: images);
    await db.customStatement('''CREATE TRIGGER fail_second_attachment
      BEFORE INSERT ON transaction_attachments WHEN NEW.sort_order=1
      BEGIN SELECT RAISE(ABORT,'test attachment failure'); END''');
    await expectLater(confirm(), throwsA(anything));
    expect(await count(), 0);
    expect(await repo.getAccountBalance(account), closeTo(100, .00001));
    expect(await db.select(db.transactionTags).get(), isEmpty);
    expect(await db.select(db.transactionAttachments).get(), isEmpty);
    expect(await capture.handled(['with-images']), isEmpty);
    await db.customStatement('DROP TRIGGER fail_second_attachment');
    final first = await confirm();
    final retry = await confirm();
    expect(first.created, isTrue);
    expect(retry.created, isFalse);
    expect(retry.transactionId, first.transactionId);
    expect(await count(), 1);
    expect(await repo.getAccountBalance(account), closeTo(87.5, .00001));
    expect(await repo.getTagsForTransaction(first.transactionId), hasLength(1));
    expect(await repo.getAttachmentsByTransaction(first.transactionId), hasLength(2));
  });

  test('a removed selected tag cannot leave a partly saved payment', () async {
    final tag = await repo.createTag(name: 'Deleted before confirmation');
    await repo.deleteTag(tag);
    await expectLater(capture.commit(candidate: candidate('stale-tag'),
      type: 'expense', amount: 12.5, happenedAt: time, tagIds: [tag]), throwsStateError);
    expect(await count(), 0);
    expect(await capture.handled(['stale-tag']), isEmpty);
  });

  test('legacy mirror tags keep their identity and cannot cross the target ledger', () async {
    final ledger = (await repo.getLedgerById(ledgerId))!;
    const tagSyncId = 'legacy-tag-one';
    await db.into(db.sharedLedgerTags).insert(SharedLedgerTagsCompanion.insert(
      ledgerSyncId: ledger.syncId!, syncId: tagSyncId, name: 'Legacy tag', updatedAt: time));
    final tagId = syntheticIdForSyncId(tagSyncId);
    final saved = await capture.commit(candidate: candidate('mirror-one'), type: 'expense',
      amount: 12.5, happenedAt: time, tagIds: [tagId, tagId]);
    final overrides = await db.select(db.transactionTagOverrides).get();
    expect(overrides, hasLength(1));
    expect(overrides.single.tagSyncId, tagSyncId);
    expect((await repo.getTagsForTransaction(saved.transactionId)).map((t) => t.id), contains(tagId));
    final otherId = await repo.createLedger(name: 'Different ledger');
    final other = (await repo.getLedgerById(otherId))!;
    final otherDraft = CaptureCandidate.fromMap({...candidate('wrong-mirror').payload,
      'ledgerId': otherId, 'ledgerKey': CaptureCommitService.ledgerKey(other)});
    await expectLater(capture.commit(candidate: otherDraft, type: 'expense',
      amount: 12.5, happenedAt: time, tagIds: [tagId]), throwsStateError);
    expect(await count(), 1);
    expect(await capture.handled(['wrong-mirror']), isEmpty);
  });

  test('global accounts remain usable in a different target ledger', () async {
    final oldLedger = await repo.createLedger(name: 'Account legacy ledger');
    final account = await repo.createAccount(ledgerId: oldLedger, name: 'Cash', initialBalance: 100);
    await capture.commit(candidate: candidate('one'), type: 'expense', amount: 12.50,
      accountId: account, happenedAt: time);
    expect(await repo.getAccountBalance(account), closeTo(87.50, .00001));
  });

  test('a converted transfer debits account units and retains the captured source amount', () async {
    final usdId = await repo.createLedger(name: 'USD capture ledger', currency: 'USD');
    // Imported legacy rows can contain mixed-case ISO codes.
    await db.customStatement('UPDATE ledgers SET currency=? WHERE id=?', ['usd', usdId]);
    final usd = (await repo.getLedgerById(usdId))!;
    final from = await repo.createAccount(ledgerId: usdId, name: 'USD cash', currency: 'usd', initialBalance: 100);
    final to = await repo.createAccount(ledgerId: usdId, name: 'USD bank', currency: 'USD', initialBalance: 20);
    final draft = CaptureCandidate.fromMap({...candidate('foreign-transfer').payload,
      'ledgerId': usdId, 'ledgerKey': CaptureCommitService.ledgerKey(usd),
      'direction': 'transfer', 'amountMinor': 10000});

    // A CNY 100 capture converted to USD 14 must never debit USD 100.
    await expectLater(capture.commit(candidate: draft, type: 'transfer', amount: 100,
      happenedAt: time, accountId: from, toAccountId: to,
      currencyCode: 'CNY', nativeAmount: 14), throwsStateError);
    // Relabelling the original number also cannot bypass the snapshot check.
    await expectLater(capture.commit(candidate: draft, type: 'transfer', amount: 100,
      happenedAt: time, accountId: from, toAccountId: to,
      currencyCode: 'USD', nativeAmount: 14), throwsStateError);
    expect(await count(), 0);
    expect(await capture.handled([draft.id]), isEmpty);
    expect(await repo.getAccountBalance(from), closeTo(100, .00001));
    expect(await repo.getAccountBalance(to), closeTo(20, .00001));

    final saved = await capture.commit(candidate: draft, type: 'transfer', amount: 14,
      happenedAt: time, accountId: from, toAccountId: to,
      currencyCode: 'USD', nativeAmount: 14);
    final retry = await capture.commit(candidate: draft, type: 'transfer', amount: 14,
      happenedAt: time, accountId: from, toAccountId: to,
      currencyCode: 'USD', nativeAmount: 14);
    expect(retry.transactionId, saved.transactionId);
    expect(retry.created, isFalse);
    expect(await repo.getAccountBalance(from), closeTo(86, .00001));
    expect(await repo.getAccountBalance(to), closeTo(34, .00001));
    final transaction = (await repo.getTransactionById(saved.transactionId))!;
    expect(transaction.amount, 14);
    expect(transaction.currencyCode, 'USD');
    expect(transaction.nativeAmount, 14);
    final link = await db.customSelect('SELECT candidate_json FROM transaction_capture_links').getSingle();
    final original = jsonDecode(link.read<String>('candidate_json')) as Map<String, dynamic>;
    expect(original['amountMinor'], 10000);
    expect(original['currency'], 'CNY');
  });

  test('a changed transfer account currency rolls back before changing balances', () async {
    final from = await repo.createAccount(ledgerId: ledgerId, name: 'From', initialBalance: 100);
    final to = await repo.createAccount(ledgerId: ledgerId, name: 'To', initialBalance: 20);
    await db.customStatement('UPDATE accounts SET currency=? WHERE id=?', ['USD', to]);
    await expectLater(capture.commit(candidate: candidate('changed-transfer'),
      type: 'transfer', amount: 12.5, happenedAt: time,
      accountId: from, toAccountId: to, currencyCode: 'CNY', nativeAmount: 12.5), throwsStateError);
    expect(await count(), 0);
    expect(await capture.handled(['changed-transfer']), isEmpty);
    expect(await repo.getAccountBalance(from), closeTo(100, .00001));
    expect(await repo.getAccountBalance(to), closeTo(20, .00001));
  });

  test('same-currency global accounts keep their own units in a foreign ledger', () async {
    final usdId = await repo.createLedger(name: 'USD ledger', currency: 'USD');
    final usd = (await repo.getLedgerById(usdId))!;
    final from = await repo.createAccount(ledgerId: ledgerId, name: 'CNY from', initialBalance: 1000);
    final to = await repo.createAccount(ledgerId: ledgerId, name: 'CNY to', initialBalance: 20);
    final draft = CaptureCandidate.fromMap({...candidate('cny-accounts').payload,
      'ledgerId': usdId, 'ledgerKey': CaptureCommitService.ledgerKey(usd), 'direction': 'transfer'});
    final saved = await capture.commit(candidate: draft, type: 'transfer', amount: 100,
      happenedAt: time, accountId: from, toAccountId: to,
      currencyCode: 'CNY', nativeAmount: 14);
    expect(await repo.getAccountBalance(from), closeTo(900, .00001));
    expect(await repo.getAccountBalance(to), closeTo(120, .00001));
    final transaction = (await repo.getTransactionById(saved.transactionId))!;
    expect(transaction.amount, 100);
    expect(transaction.currencyCode, 'CNY');
    expect(transaction.nativeAmount, 14);
  });

  test('legacy mirror transfer accounts are checked in their actual currency', () async {
    final ledger = (await repo.getLedgerById(ledgerId))!;
    for (final syncId in ['mirror-from', 'mirror-to']) {
      await db.into(db.sharedLedgerAccounts).insert(SharedLedgerAccountsCompanion.insert(
        ledgerSyncId: ledger.syncId!, syncId: syncId, name: syncId,
        currency: const Value('USD'), updatedAt: time));
    }
    final draft = candidate('mirror-transfer');
    await expectLater(capture.commit(candidate: draft, type: 'transfer', amount: 12.5,
      happenedAt: time, accountSyncIdOverride: 'mirror-from', toAccountSyncIdOverride: 'mirror-to',
      currencyCode: 'CNY', nativeAmount: 12.5), throwsStateError);
    expect(await count(), 0);
    final saved = await capture.commit(candidate: draft, type: 'transfer', amount: 12.5,
      happenedAt: time, accountSyncIdOverride: 'mirror-from', toAccountSyncIdOverride: 'mirror-to',
      currencyCode: 'USD', nativeAmount: 90);
    final transaction = (await repo.getTransactionById(saved.transactionId))!;
    expect(transaction.amount, 12.5);
    expect(transaction.currencyCode, 'USD');
    expect(transaction.nativeAmount, 90);
    expect(transaction.accountSyncIdOverride, 'mirror-from');
    expect(transaction.toAccountSyncIdOverride, 'mirror-to');
  });

  test('ledger replacement identity is rejected without changing money', () async {
    await expectLater(save(candidate('stale', identity: 'replaced-ledger')), throwsStateError);
    expect(await count(), 0);
  });

  test('deleting an entry retains a tombstone against stale retry', () async {
    final first = await save(candidate('one'));
    await repo.deleteTransaction(first.transactionId);
    expect(await capture.handled(['one']), {'one': null});
    await expectLater(save(candidate('one')), throwsStateError);
    expect(await count(), 0);
  });

  test('backup roundtrip preserves source metadata and idempotency', () async {
    final first = await save(candidate('one'));
    final backup = LocalDatabaseBackupService(db);
    final bytes = await backup.exportBytes();
    final snapshot = await backup.inspectBytes(bytes);
    await backup.restore(snapshot);
    final retry = await save(candidate('one'));
    expect(retry.transactionId, first.transactionId);
    expect(await count(), 1);
    expect(jsonDecode(utf8.decode(bytes))['tables']['transaction_capture_links']['rows'], hasLength(1));
  });

  test('legacy v34 backup is accepted with an empty capture metadata table', () async {
    final backup = LocalDatabaseBackupService(db);
    final data = jsonDecode(utf8.decode(await backup.exportBytes())) as Map<String, dynamic>;
    data['schemaVersion'] = 34;
    (data['tables'] as Map).remove('transaction_capture_links');
    final snapshot = await backup.inspectBytes(Uint8List.fromList(utf8.encode(jsonEncode(data))));
    await backup.restore(snapshot);
    expect(await capture.handled(['one']), isEmpty);
  });
}
