import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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
  DateTime? _statusReadAt;
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
        setState(() { _status = value; _statusKnown = true; _stopConfirmed = false;
          _statusReadAt = DateTime.now(); _error = operationError; });
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

  Future<void> _copyDiagnostics(CaptureStatus status, bool? targetAvailable) async {
    final text = CaptureText(context);
    // Keep an explicit allowlist. Ledger names/keys, candidate payloads and
    // unknown bridge fields must never be included in a diagnostic report.
    final report = <String, Object?>{
      'statusKnown': _statusKnown,
      'statusReadAt': _statusReadAt?.millisecondsSinceEpoch,
      'supported': status.supported,
      'enabled': _stopConfirmed ? false : _statusKnown ? status.enabled : null,
      'stopConfirmed': _stopConfirmed,
      'targetLedgerSelected': _statusKnown ? status.ledgerId != null : null,
      'targetLedgerAvailable': _statusKnown ? targetAvailable : null,
      'selectedPackages': _statusKnown ? (status.packages.toList()..sort()) : null,
      'accessibilityGranted': _statusKnown ? status.permissionGranted : null,
      'accessibilityConnected': _statusKnown ? status.connected : null,
      'notificationAssistance': _statusKnown ? status.notifications : null,
      'notificationAccessGranted': _statusKnown ? status.notificationsGranted : null,
      'notificationListenerConnected': _statusKnown ? status.notificationsConnected : null,
      'pendingCount': _statusKnown ? status.pendingCount : null,
      'skippedObservations': _statusKnown ? status.skippedObservations : null,
      'lastError': _statusKnown ? CaptureDiagnostics.code(status.lastError) : null,
      'diagnostics': status.diagnostics?.toMetadata(),
    };
    try {
      await Clipboard.setData(ClipboardData(text: const JsonEncoder.withIndent('  ').convert(report)));
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text.diagnosticsCopied)));
    } catch (_) {
      if (mounted) setState(() => _error = text.diagnosticsCopyFailed);
    }
  }

  Widget _captureConditions(CaptureStatus status, {required bool ledgerKnown,
      required bool targetAvailable}) {
    final text = CaptureText(context);
    final enabled = !_stopConfirmed && status.enabled;
    final screenReady = status.permissionGranted && status.connected;
    final notificationsReady = status.notifications && status.notificationsGranted && status.notificationsConnected;
    final String summary;
    if (!_statusKnown) {
      summary = _stopConfirmed
          ? text.t('采集已停止，其余条件待确认。', 'Capture is stopped; other conditions are unconfirmed.')
          : text.connectionUnknown;
    } else if (!enabled) {
      summary = text.t('采集开关已关闭；已有草稿仍可处理。', 'Capture is turned off. Existing drafts remain available.');
    } else if (!ledgerKnown) {
      summary = text.t('目标账本状态待确认。', 'The target ledger is not confirmed.');
    } else if (!targetAvailable) {
      summary = text.t('请先选择有效的采集目标账本。', 'Select an available target ledger before capturing.');
    } else if (status.packages.isEmpty) {
      summary = text.noAppsSelected;
    } else if (screenReady) {
      summary = text.t('界面采集条件已就绪；请在所选应用打开交易页面。',
          'Screen capture is ready; open a transaction page in a selected app.');
    } else if (notificationsReady) {
      summary = text.t('仅通知采集已就绪；界面采集尚未连接。',
          'Only notification capture is ready; screen capture is not connected.');
    } else {
      summary = text.t('尚无可用采集来源，请检查授权和服务连接。',
          'No capture source is ready. Check permissions and connections.');
    }
    final unconfirmed = text.t('待确认', 'Unconfirmed');
    return Card(margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6), child: ExpansionTile(
      key: const ValueKey('capture-conditions'),
      title: Text(text.captureConditions), subtitle: Text(summary),
      children: [
        ListTile(title: Text(text.t('采集开关', 'Capture switch')),
          subtitle: Text(_stopConfirmed ? text.t('已关闭', 'Off') : !_statusKnown ? unconfirmed
              : enabled ? text.t('已开启', 'On') : text.t('已关闭', 'Off'))),
        ListTile(title: Text(text.t('目标账本', 'Target ledger')),
          subtitle: Text(!_statusKnown || !ledgerKnown ? unconfirmed : targetAvailable
              ? text.t('已选择且可用', 'Selected and available') : text.t('尚未选择或已不可用', 'Not selected or unavailable'))),
        ListTile(title: Text(text.t('所选应用', 'Selected apps')),
          subtitle: Text(!_statusKnown ? unconfirmed : status.packages.isEmpty
              ? text.noAppsSelected : text.t('已勾选 ${status.packages.length} 个应用', '${status.packages.length} apps selected'))),
        ListTile(title: Text(text.t('界面采集来源', 'Screen capture source')),
          subtitle: Text(text.connection(status.permissionGranted, status.connected, known: _statusKnown))),
        ListTile(title: Text(text.t('通知采集来源', 'Notification capture source')),
          subtitle: Text(!_statusKnown ? unconfirmed : !status.notifications ? text.t('未启用', 'Not enabled')
              : text.connection(status.notificationsGranted, status.notificationsConnected))),
      ],
    ));
  }

  Widget _captureDiagnostics(CaptureStatus status, bool? targetAvailable) {
    final text = CaptureText(context), diagnostics = status.diagnostics;
    String time(int? value) => value == null ? text.noDiagnosticRecord
        : DateTime.fromMillisecondsSinceEpoch(value).toLocal().toString();
    String observation(int? at, String? packageName, String? source) =>
        '${time(at)}\n${packageName ?? text.noDiagnosticRecord} · ${text.diagnosticSource(source)}';
    final waiting = text.t('已收到采集事件，等待后续读取或处理。',
        'Capture events received; waiting for a read or processing result.');
    final String summary;
    if (!_statusKnown) {
      summary = text.diagnosticSnapshotUnknown;
    } else if (diagnostics == null) {
      summary = text.diagnosticsUnavailable;
    } else {
      final eventAt = diagnostics.lastEventAt ?? 0;
      final readAt = diagnostics.lastReadAt ?? 0;
      final processedAt = diagnostics.lastProcessedAt ?? 0;
      // Compare stages: an older successful draft must not hide a newer
      // unreadable page or an event that has not reached the reader yet.
      if (eventAt > readAt && eventAt > processedAt) {
        summary = waiting;
      } else if (readAt > processedAt && readAt >= eventAt) {
        summary = diagnostics.lastReadResult != null
            ? text.diagnosticReadResult(diagnostics.lastReadResult) : waiting;
      } else if (diagnostics.lastOutcome != null) {
        summary = text.diagnosticOutcome(diagnostics.lastOutcome);
      } else if (diagnostics.lastReadResult != null) {
        summary = text.diagnosticReadResult(diagnostics.lastReadResult);
      } else if (diagnostics.eventCount == 0 && diagnostics.readCount == 0 &&
          diagnostics.processedCount == 0 && eventAt == 0 && readAt == 0 && processedAt == 0) {
        summary = text.noCaptureEvents;
      } else {
        summary = waiting;
      }
    }
    return Card(margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6), child: ExpansionTile(
      key: const ValueKey('capture-diagnostics'),
      title: Text(text.diagnosticsTitle), subtitle: Text(summary),
      children: [
        Padding(padding: const EdgeInsets.all(16), child: Text(text.diagnosticsScope)),
        ListTile(title: Text(text.t('状态读取时间', 'Status read at')),
          subtitle: Text(_statusReadAt?.toLocal().toString() ?? text.noDiagnosticRecord)),
        if (diagnostics != null) ...[
          ListTile(title: Text(text.t('本次诊断开始', 'Diagnostics started at')),
            subtitle: Text(time(diagnostics.startedAt))),
          ListTile(title: Text(text.t('本次诊断计数', 'Diagnostic counts')),
            subtitle: Text(text.t(
              '事件 ${diagnostics.eventCount} · 读取 ${diagnostics.readCount} · 处理 ${diagnostics.processedCount} · 新增草稿 ${diagnostics.savedCount}',
              'Events ${diagnostics.eventCount} · Reads ${diagnostics.readCount} · Processed ${diagnostics.processedCount} · New drafts ${diagnostics.savedCount}'))),
          ListTile(title: Text(text.t('最近采集事件', 'Last capture event')),
            subtitle: Text(observation(diagnostics.lastEventAt, diagnostics.lastEventPackage, diagnostics.lastEventSource))),
          ListTile(title: Text(text.t('最近页面或通知读取', 'Last screen or notification read')),
            subtitle: Text(observation(diagnostics.lastReadAt, diagnostics.lastReadPackage, diagnostics.lastReadSource))),
          ListTile(title: Text(text.t('读取结果', 'Read result')),
            subtitle: Text(text.diagnosticReadResult(diagnostics.lastReadResult))),
          ListTile(title: Text(text.t('节点与文字计数', 'Node and text counts')),
            subtitle: Text(text.t('节点 ${diagnostics.lastNodeCount ?? '—'} · 文字 ${diagnostics.lastTextCount ?? '—'}',
              'Nodes ${diagnostics.lastNodeCount ?? '—'} · Text ${diagnostics.lastTextCount ?? '—'}'))),
          ListTile(title: Text(text.t('最近规则处理', 'Last rule processing')),
            subtitle: Text(observation(diagnostics.lastProcessedAt, diagnostics.lastProcessedPackage, diagnostics.lastProcessedSource))),
          ListTile(title: Text(text.t('处理结果', 'Processing result')),
            subtitle: Text(text.diagnosticOutcome(diagnostics.lastOutcome))),
        ],
        Padding(padding: const EdgeInsets.all(16), child: OutlinedButton.icon(
          key: const ValueKey('capture-copy-diagnostics'), onPressed: () => _copyDiagnostics(status, targetAvailable),
          icon: const Icon(Icons.copy_outlined), label: Text(text.copyDiagnostics))),
      ],
    ));
  }

  @override
  Widget build(BuildContext context) {
    final text = CaptureText(context), status = _status;
    final ledgersState = ref.watch(ledgersStreamProvider);
    final currentLedgers = ledgersState.unwrapPrevious();
    final ledgers = currentLedgers.value ?? [];
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
          _captureConditions(status, ledgerKnown: currentLedgers.hasValue, targetAvailable: targetAvailable),
          ListTile(
            title: Text(text.t('无障碍服务', 'Accessibility service')),
            subtitle: Text(text.connection(status.permissionGranted, status.connected, known: _statusKnown)),
            trailing: const Icon(Icons.open_in_new),
            onTap: () async { try { await _service.openSettings(); } catch (_) { if (mounted) setState(() => _error = text.failed); } },
          ),
          SwitchListTile(title: Text(text.t('通知辅助（可选）', 'Notification assistance (optional)')),
            subtitle: Text(text.t('仅处理所选应用新到达的交易通知，结果仍需确认。', 'Use new transaction notifications from selected apps. Results still require review.')),
            value: status.notifications, onChanged: !canEdit ? null : (v) => _save(notifications: v)),
          if (status.notifications) ListTile(
            title: Text(text.t('通知访问权限', 'Notification access')),
            subtitle: Text('${text.connection(status.notificationsGranted, status.notificationsConnected, known: _statusKnown)}\n${text.t(
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
              : status.lastError == 'CAPTURE_PROCESSING_FAILED' ? text.processingFailed
              : text.t('采集存储失败，可能有遗漏，请核对交易。', 'Capture storage failed. Check for missed transactions.'))),
          if (status.skippedObservations > 0) ListTile(leading: const Icon(Icons.warning_amber),
            title: Text(status.importantObservationsSkipped
              ? text.t('采集曾出现拥堵或存储异常，部分交易可能遗漏。请对照支付记录核对。', 'Capture had an overload or storage problem and may have missed transactions. Check your payment records.')
              : text.t('采集曾繁忙，部分页面信息未处理。请核对待确认交易是否完整。', 'Capture was busy and skipped some page updates. Check that your drafts are complete.')),
            trailing: TextButton(onPressed: !canEdit ? null : () => _acknowledgeOverflow(status),
              child: Text(text.t('已知晓', 'Dismiss warning')))),
          _captureDiagnostics(status, currentLedgers.hasValue ? targetAvailable : null),
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
