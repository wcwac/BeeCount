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
  String connection(bool granted, bool connected) => !granted
      ? t('未授权，请在系统设置中开启', 'Not granted; enable in system settings')
      : connected ? t('已授权，服务已连接', 'Granted; service connected')
      : t('已授权，服务未连接；请刷新检查', 'Granted; service not connected. Refresh to check');
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
    'CURRENCY_UNCERTAIN' => t('页面可能包含外币，请确认币种', 'Possible foreign currency; confirm the currency'),
    'REFERENCE_CONFLICT' => t('交易标识与已有候选不一致，请核对是否重复', 'Transaction identity conflicts with an existing draft; check for duplicates'),
    'MERCHANT_UNCERTAIN' => t('商户来自页面邻近文本，请核对', 'Merchant inferred from nearby page text; verify it'),
    'FEE_PRESENT' => t('页面有手续费，请核对金额是否已含费用，是否需要另记费用', 'A fee is shown. Check whether it is included in the amount or needs a separate entry'),
    _ => t('需要核对交易信息', 'Review the transaction details'),
  };
}
