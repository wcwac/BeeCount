import 'dart:convert';
import 'package:drift/drift.dart' as d;
import '../../data/db.dart';
import '../../data/repositories/local/local_repository.dart';
import '../../models/capture_candidate.dart';
import '../../models/prepared_attachment.dart';
import '../../utils/shared_ledger_picker_filter.dart';

class CaptureCommitResult {
  final int transactionId;
  final bool created;
  const CaptureCommitResult(this.transactionId, {required this.created});
}

class CaptureCommitService {
  final LocalRepository repository;
  CaptureCommitService(this.repository);

  static String ledgerKey(Ledger ledger) => '${ledger.syncId ?? 'local'}:${ledger.createdAt.toUtc().millisecondsSinceEpoch}';

  Future<List<Transaction>> possibleMatches(CaptureCandidate candidate) async {
    if (candidate.amountMinor == null || candidate.issues.contains('CURRENCY_UNCERTAIN')) return const [];
    final db = repository.db;
    final time = candidate.transactionTime;
    final query = db.select(db.transactions)
      ..where((t) => t.ledgerId.equals(candidate.ledgerId) &
        t.amount.equals(candidate.amountMinor! / 100) &
        t.happenedAt.isBetweenValues(time.subtract(const Duration(minutes: 2)), time.add(const Duration(minutes: 2))))
      ..limit(5);
    if (candidate.direction != null) query.where((t) => t.type.equals(candidate.direction!));
    query.where((t) => t.currencyCode.equals(candidate.currency));
    return query.get();
  }

  /// Explicit user confirmation links different sources without moving money.
  Future<void> linkExisting(CaptureCandidate candidate, Transaction selected) => repository.db.transaction(() async {
    final transactionId = selected.id;
    final handledRows = await handled([candidate.id]);
    if (handledRows.containsKey(candidate.id)) {
      if (handledRows[candidate.id] != transactionId) throw StateError('CAPTURE_ALREADY_HANDLED');
      return;
    }
    final tx = await repository.getTransactionById(transactionId);
    final ledger = await repository.getLedgerById(candidate.ledgerId);
    if (tx == null || tx.ledgerId != candidate.ledgerId ||
        ledger == null || candidate.ledgerKey != ledgerKey(ledger) ||
        candidate.amountMinor == null || (tx.amount * 100).round() != candidate.amountMinor ||
        candidate.issues.contains('CURRENCY_UNCERTAIN') || tx.currencyCode != candidate.currency ||
        (candidate.direction != null && tx.type != candidate.direction) ||
        tx.syncId != selected.syncId || tx.amount != selected.amount || tx.type != selected.type ||
        tx.currencyCode != selected.currencyCode || tx.happenedAt != selected.happenedAt ||
        tx.happenedAt.difference(candidate.transactionTime).inMilliseconds.abs() > 120000) {
      throw StateError('CAPTURE_MATCH_CHANGED');
    }
    await _link(candidate, transactionId);
  });

  Future<void> _link(CaptureCandidate candidate, int transactionId) => repository.db.customStatement(
    'INSERT INTO transaction_capture_links '
    '(candidate_id,transaction_id,ledger_id,source_package,candidate_json,created_at) VALUES (?,?,?,?,?,?)',
    [candidate.id, transactionId, candidate.ledgerId, candidate.sourcePackage,
      jsonEncode(candidate.payload), DateTime.now().millisecondsSinceEpoch],
  );

  Future<Map<String, int?>> handled(List<String> ids) async {
    final result = <String, int?>{};
    for (var start = 0; start < ids.length; start += 200) {
      final batch = ids.skip(start).take(200).toList();
      final rows = await repository.db.customSelect(
        'SELECT candidate_id,transaction_id FROM transaction_capture_links WHERE candidate_id IN (${List.filled(batch.length, '?').join(',')})',
        variables: batch.map((id) => d.Variable(id)).toList(),
      ).get();
      for (final row in rows) {
        result[row.read<String>('candidate_id')] = row.readNullable<int>('transaction_id');
      }
    }
    return result;
  }

  Future<CaptureCommitResult> commit({
    required CaptureCandidate candidate,
    required String type,
    required double amount,
    required DateTime happenedAt,
    int? categoryId,
    int? accountId,
    int? toAccountId,
    String? categorySyncIdOverride,
    String? accountSyncIdOverride,
    String? toAccountSyncIdOverride,
    String? note,
    bool excludeFromStats = false,
    bool excludeFromBudget = false,
    String? currencyCode,
    double? nativeAmount,
    List<int> tagIds = const [],
    List<PreparedAttachment> attachments = const [],
  }) async {
    if (!amount.isFinite || amount <= 0 || amount > 999999999.99 ||
        ((amount * 100).round() / 100 - amount).abs() > 0.0000001 ||
        !const ['expense', 'income', 'transfer'].contains(type)) {
      throw const FormatException('Invalid confirmed capture amount or direction');
    }
    if (type == 'transfer' && ((accountId == null && accountSyncIdOverride == null) ||
        (toAccountId == null && toAccountSyncIdOverride == null) ||
        (accountId != null && accountId == toAccountId) ||
        (accountSyncIdOverride != null && accountSyncIdOverride == toAccountSyncIdOverride))) {
      throw const FormatException('Choose two different transfer accounts');
    }
    final db = repository.db;
    return db.transaction(() async {
      final existing = await db.customSelect(
        'SELECT transaction_id FROM transaction_capture_links WHERE candidate_id=?',
        variables: [d.Variable(candidate.id)],
      ).getSingleOrNull();
      if (existing != null) {
        final id = existing.readNullable<int>('transaction_id');
        if (id == null) throw StateError('CAPTURE_ALREADY_DELETED');
        return CaptureCommitResult(id, created: false);
      }
      final ledger = await repository.getLedgerById(candidate.ledgerId);
      if (ledger == null || candidate.ledgerKey != ledgerKey(ledger)) throw StateError('CAPTURE_LEDGER_MISSING');
      // Categories and accounts are user-global in this fork. Their legacy
      // accounts.ledger_id is not an ownership filter. Only the target ledger
      // belongs to the candidate; stale/hidden picker selections are rejected.
      for (final entry in [('categories', categoryId), ('accounts', accountId), ('accounts', toAccountId)]) {
        if (entry.$2 == null) continue;
        final row = await db.customSelect(
          'SELECT ${entry.$1 == 'categories' ? 'id,kind' : 'id'} FROM ${entry.$1} WHERE id=?${entry.$1 == 'accounts' ? ' AND hidden=0' : ''}',
          variables: [d.Variable(entry.$2!)],
        ).getSingleOrNull();
        if (row == null) throw StateError('CAPTURE_SELECTION_CHANGED');
        if (entry.$1 == 'categories' && type != 'transfer' && row.read<String>('kind') != type) {
          throw StateError('CAPTURE_SELECTION_CHANGED');
        }
      }
      // Preserve existing offline-backup mirror selections instead of silently
      // dropping the editor's synthetic category/account choice.
      for (final entry in [('shared_ledger_categories', categorySyncIdOverride),
          ('shared_ledger_accounts', accountSyncIdOverride), ('shared_ledger_accounts', toAccountSyncIdOverride)]) {
        if (entry.$2 == null) continue;
        if (ledger.syncId == null) throw StateError('CAPTURE_SELECTION_CHANGED');
        final row = await db.customSelect('SELECT sync_id FROM ${entry.$1} WHERE sync_id=? AND ledger_sync_id=?',
          variables: [d.Variable(entry.$2!), d.Variable(ledger.syncId!)]).getSingleOrNull();
        if (row == null) throw StateError('CAPTURE_SELECTION_CHANGED');
      }
      final id = await repository.addTransaction(
        ledgerId: candidate.ledgerId, type: type, amount: amount, categoryId: categoryId,
        accountId: accountId, toAccountId: toAccountId, happenedAt: happenedAt, note: note,
        categorySyncIdOverride: categorySyncIdOverride, accountSyncIdOverride: accountSyncIdOverride,
        toAccountSyncIdOverride: toAccountSyncIdOverride,
        excludeFromStats: excludeFromStats, excludeFromBudget: excludeFromBudget,
        currencyCode: currencyCode ?? candidate.currency, nativeAmount: nativeAmount,
      );
      await _writeTags(id, ledger, tagIds);
      for (final attachment in attachments) {
        await repository.createAttachment(
          transactionId: id, fileName: attachment.fileName,
          originalName: attachment.originalName, fileSize: attachment.fileSize,
          width: attachment.width, height: attachment.height, sortOrder: attachment.sortOrder,
        );
      }
      await _link(candidate, id);
      return CaptureCommitResult(id, created: true);
    });
  }

  /// Selected metadata commits with the financial row and its capture identity.
  /// A stale/deleted tag or failed attachment insert must roll back the payment.
  Future<void> _writeTags(int transactionId, Ledger ledger, List<int> tagIds) async {
    final db = repository.db;
    final selected = tagIds.toSet();
    final local = selected.where((id) => id >= 0).toList();
    for (final tagId in local) {
      final tag = await (db.select(db.tags)..where((t) => t.id.equals(tagId))).getSingleOrNull();
      if (tag == null) throw StateError('CAPTURE_SELECTION_CHANGED');
    }
    await repository.updateTransactionTags(transactionId: transactionId, tagIds: local);
    final synthetic = selected.where((id) => id < 0);
    if (synthetic.isEmpty) return;
    if (ledger.syncId == null) throw StateError('CAPTURE_SELECTION_CHANGED');
    final mirrors = await (db.select(db.sharedLedgerTags)
      ..where((t) => t.ledgerSyncId.equals(ledger.syncId!))).get();
    final transaction = await repository.getTransactionById(transactionId);
    if (transaction?.syncId == null) throw StateError('CAPTURE_SELECTION_CHANGED');
    for (final tagId in synthetic) {
      final matches = mirrors.where((t) => syntheticIdForSyncId(t.syncId) == tagId).toList();
      if (matches.length != 1) throw StateError('CAPTURE_SELECTION_CHANGED');
      await db.into(db.transactionTagOverrides).insert(TransactionTagOverridesCompanion.insert(
        transactionSyncId: transaction!.syncId!, tagSyncId: matches.single.syncId,
        createdAt: DateTime.now().toUtc(),
      ));
    }
  }
}
