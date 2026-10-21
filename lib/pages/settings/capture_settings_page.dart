import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/capture_candidate.dart';
import '../../providers.dart';
import '../../services/capture/capture_service.dart';
import '../../services/capture/capture_commit_service.dart';
import '../transaction/capture_inbox_page.dart';
import 'capture_text.dart';

class CaptureSettingsPage extends ConsumerStatefulWidget {
  const CaptureSettingsPage({super.key});
  @override
  ConsumerState<CaptureSettingsPage> createState() => _CaptureSettingsPageState();
}

class _CaptureSettingsPageState extends ConsumerState<CaptureSettingsPage> with WidgetsBindingObserver {
  final _service = CaptureService();
  CaptureStatus? _status;
  int _statusRevision = 0;
  bool _statusKnown = false;
  bool _busy = false;
  bool _stopPending = false;
  bool _stopConfirmed = false;
  String? _error;

  @override
  void initState() { super.initState(); WidgetsBinding.instance.addObserver(this); _load(); }
  @override
  void dispose() { WidgetsBinding.instance.removeObserver(this); super.dispose(); }
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) _load();
  }
  Future<void> _load() async {
    // A resume/read started during a write cannot confirm its final state.
    if (!mounted || _busy) return;
    final revision = ++_statusRevision;
    await _readStatus(revision);
  }

  Future<void> _readStatus(int revision, {String? operationError}) async {
    try {
      final value = await _service.status().timeout(const Duration(seconds: 5));
      if (mounted && revision == _statusRevision) {
        setState(() { _status = value; _statusKnown = true; _stopConfirmed = false; _error = operationError; });
      }
    } catch (_) {
      if (mounted && revision == _statusRevision) {
        setState(() {
          _statusKnown = false;
          final text = CaptureText(context);
          _error = _stopConfirmed ? text.stoppedSettingsUnknown : text.statusUnknown;
        });
      }
    }
  }

  Future<void> _write(Future<void> Function(int revision) action, {bool stopping = false}) async {
    if (!mounted || (stopping ? _stopPending : _busy)) return;
    final revision = ++_statusRevision;
    setState(() {
      _busy = true; _statusKnown = false; _error = null;
      // Retrying a stop cannot revoke an earlier successful stop confirmation.
      // Only a fresh status or a later ordinary edit can replace that evidence.
      if (!stopping) _stopConfirmed = false;
      if (stopping) _stopPending = true;
    });
    String? operationError;
    try {
      await action(revision);
    } catch (error) {
      if (mounted) {
        final text = CaptureText(context);
        operationError = error is StateError && error.message == 'CAPTURE_LEDGER_MISSING'
            ? text.ledgerUnavailable : text.settingsFailed;
      }
    } finally {
      // The native write can have committed before its error reply. Always
      // read back; neither success nor failure makes the old snapshot current.
      if (mounted && revision == _statusRevision) {
        await _readStatus(revision, operationError: operationError);
      }
      if (mounted && revision == _statusRevision) setState(() => _busy = false);
      if (mounted && stopping) setState(() => _stopPending = false);
    }
  }

  Future<void> _stopCapture() => _write((revision) async {
    await _service.stopCapture();
    if (mounted && revision == _statusRevision) {
      setState(() { _stopConfirmed = true; _error = CaptureText(context).stoppedSettingsUnknown; });
    }
  }, stopping: true);

  Future<void> _save({bool? enabled, Set<String>? packages, int? ledgerId, bool? notifications}) async {
    if (enabled == false) { await _stopCapture(); return; }
    final status = _status;
    if (status == null || !_statusKnown || _busy) return;
    await _write((revision) async {
      final nextEnabled = enabled ?? status.enabled;
      final targetId = ledgerId ?? status.ledgerId;
      var targetKey = status.ledgerKey;
      if (ledgerId != null || nextEnabled) {
        if (targetId == null) throw StateError('CAPTURE_LEDGER_MISSING');
        final ledger = await ref.read(repositoryProvider).getLedgerById(targetId);
        if (!mounted || revision != _statusRevision) return;
        if (ledger == null || (ledgerId == null &&
            CaptureCommitService.ledgerKey(ledger) != status.ledgerKey)) {
          throw StateError('CAPTURE_LEDGER_MISSING');
        }
        // Only explicit ledger selection may bind a replacement with the same
        // numeric ID. Other edits preserve the confirmed stable identity.
        if (ledgerId != null) targetKey = CaptureCommitService.ledgerKey(ledger);
      }
      if (!mounted || revision != _statusRevision) return;
      await _service.updateSettings(enabled: nextEnabled,
        packages: packages ?? status.packages,
        ledgerId: targetId, ledgerKey: targetKey,
        notifications: notifications ?? status.notifications);
    });
  }

  Future<void> _acknowledgeOverflow(CaptureStatus shown) async {
    await _write((_) => _service.acknowledgeOverflow(shown.overflowCursor));
  }

  @override
  Widget build(BuildContext context) {
    final text = CaptureText(context), status = _status;
    final ledgersState = ref.watch(ledgersStreamProvider);
    final ledgers = ledgersState.value ?? [];
    final selected = status?.ledgerId;
    final targetAvailable = status != null && ledgers.any((l) =>
        l.id == selected && CaptureCommitService.ledgerKey(l) == status.ledgerKey);
    final canEdit = _statusKnown && !_busy;
    return Scaffold(
      appBar: AppBar(title: Text(text.title), actions: [IconButton(onPressed: _busy ? null : _load, icon: const Icon(Icons.refresh))]),
      body: ListView(children: [
        if (_error != null) ListTile(leading: const Icon(Icons.error_outline), title: Text(_error!), onTap: _load),
        if (status == null && _error == null) const LinearProgressIndicator(),
        if (status != null && !status.supported) ListTile(title: Text(text.t('交易采集仅支持 Android。', 'Transaction capture is available on Android.'))),
        if (_service.supported && (status == null || status.supported))
          Padding(padding: const EdgeInsets.all(16), child: OutlinedButton.icon(
            key: const ValueKey('capture-stop'), onPressed: _stopPending ? null : _stopCapture,
            icon: const Icon(Icons.stop_circle_outlined), label: Text(_stopPending
              ? _stopConfirmed
                ? text.t('已停止，正在刷新设置', 'Stopped; refreshing settings')
                : text.t('正在停止采集，等待确认', 'Stopping capture; awaiting confirmation')
              : text.stopCapture))),
        if (status != null && status.supported) ...[
          Padding(padding: const EdgeInsets.all(16), child: Text(text.t(
            '仅在你启用后读取所选应用的界面以识别交易，数据保存在本机，不上传。结果先进入待确认列表，由你核对并保存。采集不会代替你付款。',
            'When enabled, reads selected app screens to detect transactions and stores drafts locally without uploading them. Review drafts before saving. Capture never initiates payments.'))),
          SwitchListTile(title: Text(text.t('启用本地采集', 'Enable local capture')),
            subtitle: Text(text.t('关闭后停止采集，已采集的待确认交易保留。', 'Turning off capture retains existing drafts.')),
            value: _stopConfirmed ? false : status.enabled, onChanged: !canEdit ? null : (v) => _save(enabled: v)),
          ListTile(
            title: Text(text.t('无障碍服务', 'Accessibility service')),
            subtitle: Text(text.connection(status.permissionGranted, status.connected)),
            trailing: const Icon(Icons.open_in_new),
            onTap: () async { try { await _service.openSettings(); } catch (_) { if (mounted) setState(() => _error = text.failed); } },
          ),
          SwitchListTile(title: Text(text.t('通知辅助（可选）', 'Notification assistance (optional)')),
            subtitle: Text(text.t('仅处理所选应用新到达的交易通知，结果仍需确认。', 'Use new transaction notifications from selected apps. Results still require review.')),
            value: status.notifications, onChanged: !canEdit ? null : (v) => _save(notifications: v)),
          if (status.notifications) ListTile(
            title: Text(text.t('通知访问权限', 'Notification access')),
            subtitle: Text('${text.connection(status.notificationsGranted, status.notificationsConnected)}\n${text.t(
              '通知访问用于读取交易通知；本应用的待处理提醒另受发送通知权限控制。',
              'Notification access reads transaction notifications. Review reminders also depend on this app’s permission to send notifications.')}'),
            trailing: const Icon(Icons.open_in_new),
            onTap: () async { try { await _service.openNotificationSettings(); } catch (_) { if (mounted) setState(() => _error = text.failed); } },
          ),
          Padding(padding: const EdgeInsets.symmetric(horizontal: 16), child: DropdownButtonFormField<int>(
            value: targetAvailable ? selected : null,
            decoration: InputDecoration(labelText: text.t('采集目标账本', 'Ledger for new captures')),
            items: [for (final ledger in ledgers) DropdownMenuItem(value: ledger.id, child: Text(ledger.name))],
            onChanged: !canEdit ? null : (id) { if (id != null) _save(ledgerId: id); },
          )),
          if (ledgersState.hasValue && selected != null && !targetAvailable)
            Padding(padding: const EdgeInsets.all(16), child: Text(text.ledgerUnavailable,
              style: TextStyle(color: Theme.of(context).colorScheme.error))),
          Padding(padding: const EdgeInsets.all(16), child: Text(text.t('更换目标只影响之后采集的交易；已有候选保留原账本。', 'Changing the ledger affects future captures; existing drafts retain their original ledger.'))),
          ListTile(leading: const Icon(Icons.inbox_outlined), title: Text('${text.t('采集记录待处理', 'Capture records to process')} (${status.pendingCount})'),
            trailing: const Icon(Icons.chevron_right), onTap: () async {
              await Navigator.of(context).push(MaterialPageRoute(builder: (_) => const CaptureInboxPage()));
              await _load();
            }),
          if (status.lastError != null && status.lastError != 'CAPTURE_BUSY' &&
              (status.lastError != 'INBOX_FULL' || status.pendingCount >= 1000 || status.skippedObservations > 0))
            ListTile(leading: const Icon(Icons.warning_amber),
            title: Text(status.lastError == 'INBOX_FULL'
              ? status.pendingCount >= 1000
                ? text.t('待处理采集记录已达上限，请先处理列表。', 'Your capture inbox is full; process pending records first.')
                : text.t('采集记录曾达上限，部分交易可能遗漏。请对照支付记录核对。', 'The capture inbox was full and may have missed transactions. Check your payment records.')
              : text.t('采集存储失败，可能有遗漏，请核对交易。', 'Capture storage failed. Check for missed transactions.'))),
          if (status.skippedObservations > 0) ListTile(leading: const Icon(Icons.warning_amber),
            title: Text(status.importantObservationsSkipped
              ? text.t('采集曾出现拥堵或存储异常，部分交易可能遗漏。请对照支付记录核对。', 'Capture had an overload or storage problem and may have missed transactions. Check your payment records.')
              : text.t('采集曾繁忙，部分页面信息未处理。请核对待确认交易是否完整。', 'Capture was busy and skipped some page updates. Check that your drafts are complete.')),
            trailing: TextButton(onPressed: !canEdit ? null : () => _acknowledgeOverflow(status),
              child: Text(text.t('已知晓', 'Dismiss warning')))),
          const Divider(),
          Padding(padding: const EdgeInsets.all(16), child: Text(text.t(
            '以下均为实验性适配，需按客户端版本验证。淘宝和抖音系部分页面可能无法读取。',
            'These are experimental adapters requiring validation per app version. Some Taobao and Douyin pages may expose no readable content.'))),
          for (final app in status.apps) CheckboxListTile(
            title: Text(app.name),
            subtitle: Text(app.limited ? text.t('实验性适配 · 部分页面受限', 'Experimental · some pages restricted') : text.t('实验性适配', 'Experimental')),
            value: status.packages.contains(app.packageName),
            onChanged: !canEdit ? null : (v) {
              final packages = {...status.packages};
              if (v == true) { packages.add(app.packageName); } else { packages.remove(app.packageName); }
              _save(packages: packages);
            }),
        ],
      ]),
    );
  }
}
