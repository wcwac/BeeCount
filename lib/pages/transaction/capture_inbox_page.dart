import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/db.dart' show Transaction;
import '../../data/repositories/local/local_repository.dart';
import '../../models/capture_candidate.dart';
import '../../providers.dart';
import '../../services/capture/capture_commit_service.dart';
import '../../services/capture/capture_service.dart';
import '../settings/capture_text.dart';
import 'transaction_editor_page.dart';

class CaptureInboxPage extends ConsumerStatefulWidget {
  const CaptureInboxPage({super.key});
  @override
  ConsumerState<CaptureInboxPage> createState() => _CaptureInboxPageState();
}

class _CaptureInboxPageState extends ConsumerState<CaptureInboxPage> with WidgetsBindingObserver {
  final _service = CaptureService();
  List<CaptureCandidate>? _items;
  bool _busy = false;
  bool _reloadRequested = false;
  bool _opening = false;
  String? _error;
  @override
  void initState() { super.initState(); WidgetsBinding.instance.addObserver(this); _load(); }
  @override
  void dispose() { WidgetsBinding.instance.removeObserver(this); super.dispose(); }
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) { if (state == AppLifecycleState.resumed) _load(); }

  Future<void> _load() async {
    if (!mounted) return;
    if (_busy) { _reloadRequested = true; return; }
    setState(() => _busy = true);
    try {
      do {
        _reloadRequested = false;
        await _loadSnapshot();
      } while (mounted && _reloadRequested);
    } finally { if (mounted) setState(() => _busy = false); }
  }

  Future<void> _loadSnapshot() async {
    try {
      var pending = await _service.pending();
      final repo = ref.read(repositoryProvider);
      if (repo is LocalRepository && pending.isNotEmpty) {
        // Recover a crash after the ledger commit but before the native ACK.
        final handled = await CaptureCommitService(repo).handled(pending.map((c) => c.id).toList());
        for (final entry in handled.entries) {
          try {
            if (entry.value == null) { await _service.dismiss(entry.key); }
            else { await _service.acknowledge(entry.key, entry.value!); }
          } catch (_) {
            // The ledger's durable link already proves this item was handled.
            // Retry its native ACK on the next refresh, without hiding other drafts.
          }
        }
        // The bridge deliberately returns a fixed-size snapshot. Filtering must
        // produce a new list instead of trying to remove entries from that list.
        pending = pending.where((c) => !handled.containsKey(c.id)).toList(growable: false);
      }
      if (mounted) setState(() { _items = pending; _error = null; });
    } catch (_) { if (mounted) setState(() => _error = CaptureText(context).failed); }
  }

  Future<void> _review(CaptureCandidate candidate) async {
    if (_busy || _opening) return;
    setState(() => _opening = true);
    try { await _openReview(candidate); }
    catch (_) { if (mounted) setState(() => _error = CaptureText(context).failed); }
    finally { if (mounted) setState(() => _opening = false); }
  }

  Future<void> _openReview(CaptureCandidate candidate) async {
    final text = CaptureText(context);
    final repo = ref.read(repositoryProvider);
    final ledger = await repo.getLedgerById(candidate.ledgerId);
    if (!mounted) return;
    if (ledger == null || candidate.ledgerKey != CaptureCommitService.ledgerKey(ledger)) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text.t(
        '原目标账本已不存在或已被备份替换。请手动记账，或忽略此候选。',
        'The original ledger is unavailable or was replaced by a restore. Record manually or dismiss this draft.'))));
      return;
    }
    if (repo is LocalRepository) {
      final commit = CaptureCommitService(repo);
      final matches = await commit.possibleMatches(candidate);
      if (!mounted) return;
      if (matches.isNotEmpty) {
        final choice = await showDialog<Object>(context: context, builder: (context) => SimpleDialog(
          title: Text(text.t('可能已记过这笔交易', 'This payment may already be recorded')),
          children: [
            Padding(padding: const EdgeInsets.all(16), child: Text(text.t(
              '发现近期同金额账单。仅在确定是同一笔时关联；关联不会再创建流水。',
              'Recent entries have the same amount. Link only if it is the same payment; linking creates no additional transaction.'))),
            for (final tx in matches) SimpleDialogOption(
              onPressed: () => Navigator.pop(context, tx),
              child: Text('${_kindLabel(text, tx.type)} · ${tx.currencyCode ?? candidate.currency} ${tx.amount.toStringAsFixed(2)} · ${tx.happenedAt}\n${tx.note ?? text.t('无备注', 'No note')}'),
            ),
            SimpleDialogOption(onPressed: () => Navigator.pop(context, 'new'), child: Text(text.t('不是同一笔，继续新增', 'Different payment; create a new entry'))),
            SimpleDialogOption(onPressed: () => Navigator.pop(context), child: Text(text.t('取消', 'Cancel'))),
          ],
        ));
        if (choice is Transaction) {
          await commit.linkExisting(candidate, choice);
          // If this ACK fails, the durable link is reconciled by the next load.
          try { await _service.acknowledge(candidate.id, choice.id); } catch (_) {}
          if (mounted) await _load();
          return;
        }
        if (choice != 'new' || !mounted) return;
      }
    }
    var kind = candidate.direction;
    if (kind == null) {
      kind = await showDialog<String>(context: context, builder: (context) => SimpleDialog(
        title: Text(text.t('请选择这笔交易的类型', 'Choose the transaction type')),
        children: [
          for (final entry in [('expense', text.t('支出', 'Expense')), ('income', text.t('收入', 'Income')), ('transfer', text.t('账户间转账', 'Transfer between accounts'))])
            SimpleDialogOption(onPressed: () => Navigator.pop(context, entry.$1), child: Text(entry.$2)),
        ],
      ));
      if (kind == null || !mounted) return;
    }
    final initialKind = kind;
    await Navigator.of(context).push(MaterialPageRoute(builder: (_) => ProviderScope(
      overrides: [currentLedgerIdProvider.overrideWith((ref) => candidate.ledgerId)],
      child: TransactionEditorPage(
        quickAdd: true,
        initialKind: initialKind,
        initialAmount: candidate.amountMinor == null ? null : candidate.amountMinor! / 100,
        initialNote: candidate.initialNote,
        initialDate: candidate.transactionTime,
        initialCurrencyCode: candidate.issues.contains('CURRENCY_UNCERTAIN') ? null : candidate.currency,
        captureCandidate: candidate,
      ),
    )));
    if (mounted) {
      // Refresh widgets from the homepage scope, after leaving the review's
      // frozen target ledger. The active homepage ledger must stay unchanged.
      updateAppWidget(ref, context);
      await _load();
    }
  }

  String _kindLabel(CaptureText text, String kind) => switch (kind) {
    'expense' => text.t('支出', 'Expense'),
    'income' => text.t('收入', 'Income'),
    'transfer' => text.t('转账', 'Transfer'),
    _ => text.t('账单', 'Transaction'),
  };

  Future<void> _dismiss(CaptureCandidate candidate) async {
    final text = CaptureText(context);
    final ok = await showDialog<bool>(context: context, builder: (context) => AlertDialog(
      title: Text(text.t('忽略这笔候选？', 'Dismiss this draft?')),
      content: Text(text.t('不会创建或删除账单。', 'No ledger transaction will be created or deleted.')),
      actions: [TextButton(onPressed: () => Navigator.pop(context, false), child: Text(text.t('取消', 'Cancel'))),
        TextButton(onPressed: () => Navigator.pop(context, true), child: Text(text.t('忽略', 'Dismiss')))],
    ));
    if (ok != true || !mounted) return;
    try { await _service.dismiss(candidate.id); await _load(); }
    catch (_) { if (mounted) setState(() => _error = text.failed); }
  }

  @override
  Widget build(BuildContext context) {
    final text = CaptureText(context);
    final ledgers = ref.watch(ledgersStreamProvider).valueOrNull ?? [];
    return Scaffold(
      appBar: AppBar(title: Text(text.inbox), actions: [IconButton(onPressed: _busy ? null : _load, icon: const Icon(Icons.refresh))]),
      body: RefreshIndicator(onRefresh: _load, child: ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          Padding(padding: const EdgeInsets.all(16), child: Text(text.t(
            '请核对是否确实付款、实付金额、收支方向和账户。电商与支付应用可能分别产生同一笔交易的候选。',
            'Check payment completion, actual amount, direction and account. A shopping app and a payment app may describe the same transaction.'))),
          if (_busy) const LinearProgressIndicator(),
          if (_error != null) ListTile(title: Text(_error!), onTap: _load, leading: const Icon(Icons.error_outline)),
          if (_items?.isEmpty == true) Padding(padding: const EdgeInsets.all(32), child: Text(text.t('暂无待确认交易', 'No transactions to review'))),
          for (final candidate in _items ?? <CaptureCandidate>[]) Card(
            margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            child: Padding(padding: const EdgeInsets.all(12), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('${candidate.appName} · ${candidate.amountMinor == null ? text.t('待补金额', 'Amount needed') : '${candidate.currency} ${(candidate.amountMinor! / 100).toStringAsFixed(2)}'}', style: Theme.of(context).textTheme.titleMedium),
              if (candidate.merchant != null) Text(candidate.merchant!),
              if (candidate.remark != null && candidate.remark != candidate.merchant) Text(candidate.remark!),
              Text('${candidate.transactionTime.toLocal()}'),
              Text('${text.t('目标账本', 'Ledger')}: ${ledgers.where((l) => l.id == candidate.ledgerId && CaptureCommitService.ledgerKey(l) == candidate.ledgerKey).map((l) => l.name).firstOrNull ?? text.t('账本不可用', 'Unavailable ledger')}'),
              if (candidate.paymentMethod != null) Text('${text.t('支付方式', 'Payment method')}: ${candidate.paymentMethod}'),
              for (final issue in candidate.issues) Text(text.issue(issue), style: TextStyle(color: Theme.of(context).colorScheme.error)),
              Row(mainAxisAlignment: MainAxisAlignment.end, children: [
                TextButton(onPressed: _busy || _opening ? null : () => _dismiss(candidate), child: Text(text.t('忽略', 'Dismiss'))),
                FilledButton(onPressed: _busy || _opening ? null : () => _review(candidate), child: Text(text.t('核对并记账', 'Review and record'))),
              ]),
            ])),
          ),
        ],
      )),
    );
  }
}
