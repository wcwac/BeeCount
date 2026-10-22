import 'package:flutter/material.dart';

class CaptureText {
  CaptureText(BuildContext context) : zh = Localizations.localeOf(context).languageCode == 'zh';
  final bool zh;
  String t(String chinese, String english) => zh ? chinese : english;
  String get title => t('交易采集', 'Transaction capture');
  String get inbox => t('待确认交易', 'Captured transactions');
  String get failed => t('操作失败，记录仍保留在本机。请刷新后重试。', 'Operation failed. Your local drafts are retained. Refresh and try again.');
  String get saveFailed => t('保存未完成，请核对账本和账户后重试。', 'Save did not finish. Check the ledger and accounts, then try again.');
  String get statusUnknown => t('当前采集状态未确认。请刷新后再修改设置；你仍可直接停止采集。',
      'Capture status is unknown. Refresh before editing settings; you can still stop capture directly.');
  String get stoppedSettingsUnknown => t('采集已停止，其余设置尚未确认。请刷新后再修改设置。',
      'Capture is stopped. Other settings are unknown; refresh before editing settings.');
  String get stopCapture => t('停止采集', 'Stop capture');
  String get settingsFailed => t('设置请求返回错误，以下显示重新读取的当前设置。',
      'The settings request returned an error. The current settings have been reloaded below.');
  String get ledgerUnavailable => t('原采集目标账本已不存在或被备份替换，请重新选择账本。',
      'The capture ledger is unavailable or was replaced by a restore. Select a ledger again.');
  String get connectionUnknown => t('连接状态待确认，请刷新', 'Connection not confirmed; refresh status');
  String connection(bool granted, bool connected, {bool known = true}) => !known
      ? connectionUnknown : !granted
      ? t('未授权，请在系统设置中开启', 'Not granted; enable in system settings')
      : connected ? t('已授权，服务已连接', 'Granted; service connected')
      : t('已授权，服务未连接；请刷新检查', 'Granted; service not connected. Refresh to check');
  String get captureConditions => t('当前采集条件', 'Capture conditions');
  String get noAppsSelected => t('尚未选择应用，因此不会采集。', 'No apps selected; capture cannot receive transactions.');
  String get diagnosticsTitle => t('最近采集诊断', 'Recent capture diagnostics');
  String get diagnosticsUnavailable => t('当前版本未提供诊断信息，仍可查看采集条件。',
      'This version provides no diagnostics. Capture conditions are still available.');
  String get diagnosticsScope => t('以下仅含应用包名、时间、计数和结果码，不含交易正文。诊断在内存中，进程重启后重置；新增草稿次数不含已有记录的补充更新。',
      'Only app packages, timestamps, counts and result codes are shown. No transaction text is included. Diagnostics reset when the process restarts; new draft counts exclude updates to existing records.');
  String get noCaptureEvents => t('尚未收到采集事件。请打开已勾选应用的交易详情或付款结果页，再刷新诊断。',
      'No capture events received. Open a transaction or payment result page in a selected app, then refresh diagnostics.');
  String get diagnosticSnapshotUnknown => t('以下为上次读取的诊断快照，当前状态待刷新确认。',
      'The diagnostics below are from the last read. Refresh to confirm the current state.');
  String get noDiagnosticRecord => t('尚无记录', 'No record yet');
  String get copyDiagnostics => t('复制诊断信息', 'Copy diagnostics');
  String get diagnosticsCopied => t('已复制诊断元数据，不含交易正文。', 'Diagnostic metadata copied; no transaction text included.');
  String get diagnosticsCopyFailed => t('复制失败，请重试。', 'Copy failed. Try again.');
  String get processingFailed => t('采集处理失败，可能有遗漏。请刷新诊断并核对支付记录。',
      'Capture processing failed and may have missed transactions. Refresh diagnostics and check your payment records.');
  String diagnosticSource(String? source) => switch (source) {
    'accessibility' => t('无障碍界面', 'Accessibility screen'),
    'notification' => t('交易通知', 'Transaction notification'),
    _ => noDiagnosticRecord,
  };
  String diagnosticReadResult(String? result) => switch (result) {
    'READABLE' => t('已读到页面内容', 'Page content was readable'),
    'EMPTY' => t('未读到文字，当前页面可能未开放无障碍内容', 'No readable text; this page may expose no accessibility content'),
    'TRUNCATED' => t('读取已达到上限，内容可能不完整', 'Read budget reached; content may be incomplete'),
    'READ_FAILED' => t('页面读取失败，请重新打开交易页后刷新', 'Page read failed; reopen the transaction page and refresh'),
    null => noDiagnosticRecord,
    _ => t('未知读取结果（$result）', 'Unknown read result ($result)'),
  };
  String diagnosticOutcome(String? outcome) => switch (outcome) {
    'UNRECOGNIZED' => t('已处理页面信息，但规则未识别为交易', 'Page data was processed, but no transaction rule matched'),
    'WAITING_RESULT' => t('已识别结算信息，正在等待付款结果', 'Checkout detected; waiting for a payment result'),
    'PAYMENT_FAILED' => t('识别到付款失败，本次不创建草稿', 'Payment failure detected; no draft created for this result'),
    'DRAFT_SAVED' => t('草稿已写入，请查看待确认交易', 'Draft written; check captured transactions'),
    'DUPLICATE' => t('本次信息已与已有记录归并或去重', 'This observation was merged with an existing record or deduplicated'),
    'CONSENT_CHANGED' => t('采集设置已变更，本次信息未继续处理', 'Capture settings changed; this observation was not processed further'),
    'PROCESSING_FAILED' => processingFailed,
    null => noDiagnosticRecord,
    _ => t('未知处理结果（$outcome）', 'Unknown processing result ($outcome)'),
  };
  String pendingUpdates(int count) => t('有 $count 笔已保存或已处理记录仍待更新本机状态。刷新可重试，不会重复记账。',
      '${count == 1 ? '1 saved or handled record still needs' : '$count saved or handled records still need'} a local status update. Refresh to retry; no additional transactions will be created.');
  String issue(String code) => switch (code) {
    'AMOUNT_MISSING' => t('缺少金额，请补填', 'Amount missing; enter it manually'),
    'AMOUNT_CONFLICT' => t('页面存在多个金额，请核对实付金额', 'Several amounts found; check the actual payment'),
    'AMOUNT_CHANGED' => t('结算与结果页金额不同，请核对', 'Checkout and result amounts differ'),
    'AMOUNT_UNANCHORED' => t('金额来自页面数值，请核对是否为实付金额', 'Check that the detected number is the actual payment'),
    'DIRECTION_UNCERTAIN' => t('请确认是收入、支出还是转账', 'Confirm income, spending or transfer'),
    'PAYMENT_UNCONFIRMED' => t('仅识别到下单成功，请确认是否已付款', 'Order placed; payment is not confirmed'),
    'SNAPSHOT_TRUNCATED' => t('页面采集达到上限，请补充核对', 'Page collection reached its budget; check missing details'),
    'TEXT_TRUNCATED' => t('备注或付款方式过长，已截短显示，请核对。', 'The remark or payment method was too long and has been shortened for display. Please verify it.'),
    'CURRENCY_UNCERTAIN' => t('页面可能包含外币，请确认币种', 'Possible foreign currency; confirm the currency'),
    'REFERENCE_CONFLICT' => t('交易标识与已有候选不一致，请核对是否重复', 'Transaction identity conflicts with an existing draft; check for duplicates'),
    'MERCHANT_UNCERTAIN' => t('商户来自页面邻近文本，请核对', 'Merchant inferred from nearby page text; verify it'),
    'FEE_PRESENT' => t('页面有手续费，请核对金额是否已含费用，是否需要另记费用', 'A fee is shown. Check whether it is included in the amount or needs a separate entry'),
    _ => t('需要核对交易信息', 'Review the transaction details'),
  };
}
