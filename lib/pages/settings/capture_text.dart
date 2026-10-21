import 'package:flutter/material.dart';

class CaptureText {
  CaptureText(BuildContext context) : zh = Localizations.localeOf(context).languageCode == 'zh';
  final bool zh;
  String t(String chinese, String english) => zh ? chinese : english;
  String get title => t('交易采集', 'Transaction capture');
  String get inbox => t('待确认交易', 'Captured transactions');
  String get failed => t('操作失败，记录仍保留在本机。请刷新后重试。', 'Operation failed. Your local drafts are retained. Refresh and try again.');
  String get saveFailed => t('保存未完成，请核对账本和账户后重试。', 'Save did not finish. Check the ledger and accounts, then try again.');
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
