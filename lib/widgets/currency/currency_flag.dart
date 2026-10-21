import 'package:country_flags/country_flags.dart';
import 'package:flutter/material.dart';

import '../../styles/tokens.dart';
import '../../utils/currencies.dart';

/// 币种国旗(统一入口:币种选择弹窗 + 记账币种标共用)。
/// - 普通币种:CountryFlag.fromCountryCode(码前两位派生的国家)
/// - 欧元:通过 CountryFlag.fromCurrencyCode 的公开币种映射显示欧盟旗
/// - 区域货币(无国家码,如 XAF/XDR):币种符号占位圆
Widget currencyFlag(
  BuildContext context,
  String currencyCode, {
  double width = 30,
  double height = 22,
  double radius = 4,
}) {
  final country = countryCodeForCurrency(currencyCode);
  if (country == null) {
    return Container(
      width: width,
      height: height,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: BeeTokens.surfaceKeySecondary(context),
        borderRadius: BorderRadius.circular(radius),
      ),
      child: Text(
        getCurrencySymbol(currencyCode),
        maxLines: 1,
        overflow: TextOverflow.clip,
        style: TextStyle(
          fontSize: 10,
          fontWeight: FontWeight.w600,
          color: BeeTokens.textSecondary(context),
        ),
      ),
    );
  }
  final flagTheme = ImageTheme(width: width, height: height);
  final Widget flag = country == 'EU'
      ? CountryFlag.fromCurrencyCode(
          'EUR',
          theme: flagTheme,
        )
      : CountryFlag.fromCountryCode(country, theme: flagTheme);
  return ClipRRect(
    borderRadius: BorderRadius.circular(radius),
    child: SizedBox(width: width, height: height, child: flag),
  );
}
