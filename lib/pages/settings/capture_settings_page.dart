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
  bool _busy = false;
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
    try {
      final value = await _service.status();
      if (mounted) setState(() { _status = value; _error = null; });
    } catch (_) { if (mounted) setState(() => _error = CaptureText(context).failed); }
  }
  Future<void> _save({bool? enabled, Set<String>? packages, int? ledgerId, bool? notifications}) async {
    final status = _status;
    if (status == null || _busy) return;
    setState(() => _busy = true);
    try {
      final int targetId = ledgerId ?? status.ledgerId ?? ref.read<int>(currentLedgerIdProvider);
      final ledger = await ref.read(repositoryProvider).getLedgerById(targetId);
      if (ledger == null && (enabled ?? status.enabled)) throw StateError('CAPTURE_LEDGER_MISSING');
      await _service.updateSettings(enabled: enabled ?? status.enabled,
        packages: packages ?? status.packages,
        ledgerId: targetId, ledgerKey: ledger == null ? status.ledgerKey : CaptureCommitService.ledgerKey(ledger),
        notifications: notifications ?? status.notifications);
      await _load();
    } catch (_) { if (mounted) setState(() => _error = CaptureText(context).failed); }
    finally { if (mounted) setState(() => _busy = false); }
  }

  Future<void> _acknowledgeOverflow(CaptureStatus shown) async {
    if (_busy) return;
    setState(() => _busy = true);
    try {
      await _service.acknowledgeOverflow(shown.overflowCursor);
      await _load();
    } catch (_) { if (mounted) setState(() => _error = CaptureText(context).failed); }
    finally { if (mounted) setState(() => _busy = false); }
  }

  @override
  Widget build(BuildContext context) {
    final text = CaptureText(context), status = _status;
    final ledgers = ref.watch(ledgersStreamProvider).valueOrNull ?? [];
    final selected = status?.ledgerId ?? ref.watch(currentLedgerIdProvider);
    return Scaffold(
      appBar: AppBar(title: Text(text.title), actions: [IconButton(onPressed: _busy ? null : _load, icon: const Icon(Icons.refresh))]),
      body: ListView(children: [
        if (_error != null) ListTile(leading: const Icon(Icons.error_outline), title: Text(_error!), onTap: _load),
        if (status == null && _error == null) const LinearProgressIndicator(),
        if (status != null && !status.supported) ListTile(title: Text(text.t('交易采集仅支持 Android。', 'Transaction capture is available on Android.'))),
        if (status != null && status.supported) ...[
          Padding(padding: const EdgeInsets.all(16), child: Text(text.t(
            '仅在你启用后读取所选应用的界面以识别交易，数据保存在本机，不上传。结果先进入待确认列表，由你核对并保存。采集不会代替你付款。',
            'When enabled, reads selected app screens to detect transactions and stores drafts locally without uploading them. Review drafts before saving. Capture never initiates payments.'))),
          SwitchListTile(title: Text(text.t('启用本地采集', 'Enable local capture')),
            subtitle: Text(text.t('关闭后停止采集，已采集的待确认交易保留。', 'Turning off capture retains existing drafts.')),
            value: status.enabled, onChanged: _busy ? null : (v) => _save(enabled: v)),
          ListTile(
            title: Text(text.t('无障碍服务', 'Accessibility service')),
            subtitle: Text(status.permissionGranted && status.connected
              ? text.t('已连接', 'Connected') : text.t('未连接，请在系统设置中开启', 'Disconnected; enable in system settings')),
            trailing: const Icon(Icons.open_in_new),
            onTap: () async { try { await _service.openSettings(); } catch (_) { if (mounted) setState(() => _error = text.failed); } },
          ),
          SwitchListTile(title: Text(text.t('通知辅助（可选）', 'Notification assistance (optional)')),
            subtitle: Text(text.t('仅处理所选应用新到达的交易通知，结果仍需确认。', 'Use new transaction notifications from selected apps. Results still require review.')),
            value: status.notifications, onChanged: _busy ? null : (v) => _save(notifications: v)),
          if (status.notifications) ListTile(
            title: Text(text.t('通知访问权限', 'Notification access')),
            subtitle: Text(status.notificationsGranted ? text.t('已授权', 'Granted') : text.t('未授权，请在系统设置中开启', 'Not granted; enable in system settings')),
            trailing: const Icon(Icons.open_in_new),
            onTap: () async { try { await _service.openNotificationSettings(); } catch (_) { if (mounted) setState(() => _error = text.failed); } },
          ),
          Padding(padding: const EdgeInsets.symmetric(horizontal: 16), child: DropdownButtonFormField<int>(
            value: ledgers.any((l) => l.id == selected) ? selected : null,
            decoration: InputDecoration(labelText: text.t('采集目标账本', 'Ledger for new captures')),
            items: [for (final ledger in ledgers) DropdownMenuItem(value: ledger.id, child: Text(ledger.name))],
            onChanged: _busy ? null : (id) { if (id != null) _save(ledgerId: id); },
          )),
          Padding(padding: const EdgeInsets.all(16), child: Text(text.t('更换目标只影响之后采集的交易；已有候选保留原账本。', 'Changing the ledger affects future captures; existing drafts retain their original ledger.'))),
          ListTile(leading: const Icon(Icons.inbox_outlined), title: Text('${text.inbox} (${status.pendingCount})'),
            trailing: const Icon(Icons.chevron_right), onTap: () async {
              await Navigator.of(context).push(MaterialPageRoute(builder: (_) => const CaptureInboxPage()));
              await _load();
            }),
          if (status.lastError != null && status.lastError != 'CAPTURE_BUSY') ListTile(leading: const Icon(Icons.warning_amber),
            title: Text(status.lastError == 'INBOX_FULL'
              ? text.t('待确认交易已达上限，请先处理列表。', 'Your draft inbox is full; review or dismiss pending items.')
              : text.t('采集存储失败，可能有遗漏，请核对交易。', 'Capture storage failed. Check for missed transactions.'))),
          if (status.skippedObservations > 0) ListTile(leading: const Icon(Icons.warning_amber),
            title: Text(status.importantObservationsSkipped
              ? text.t('采集曾出现拥堵或存储异常，部分交易可能遗漏。请对照支付记录核对。', 'Capture had an overload or storage problem and may have missed transactions. Check your payment records.')
              : text.t('采集曾繁忙，部分页面信息未处理。请核对待确认交易是否完整。', 'Capture was busy and skipped some page updates. Check that your drafts are complete.')),
            trailing: TextButton(onPressed: _busy ? null : () => _acknowledgeOverflow(status),
              child: Text(text.t('已知晓', 'Dismiss warning')))),
          const Divider(),
          Padding(padding: const EdgeInsets.all(16), child: Text(text.t(
            '以下均为实验性适配，需按客户端版本验证。淘宝和抖音系部分页面可能无法读取。',
            'These are experimental adapters requiring validation per app version. Some Taobao and Douyin pages may expose no readable content.'))),
          for (final app in status.apps) CheckboxListTile(
            title: Text(app.name),
            subtitle: Text(app.limited ? text.t('实验性适配 · 部分页面受限', 'Experimental · some pages restricted') : text.t('实验性适配', 'Experimental')),
            value: status.packages.contains(app.packageName),
            onChanged: _busy ? null : (v) {
              final packages = {...status.packages};
              if (v == true) { packages.add(app.packageName); } else { packages.remove(app.packageName); }
              _save(packages: packages);
            }),
        ],
      ]),
    );
  }
}
