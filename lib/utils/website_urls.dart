import 'dart:ui';

/// 官网 URL 管理
///
/// 统一管理官网链接，方便未来域名变更
class WebsiteUrls {
  WebsiteUrls._();

  /// 官网基础域名
  /// 未来换域名时只需修改这里
  static const String baseUrl = 'https://count.beejz.com';

  /// 服务商官网直达链接，不经过邀请或统计重定向。
  static const String glmWebsite = 'https://open.bigmodel.cn';

  /// 获取语言前缀
  /// 中文不需要前缀，英文需要 /en 前缀
  static String _langPrefix(Locale? locale) {
    if (locale == null) return '';
    // 简体中文和繁体中文使用默认路径
    if (locale.languageCode == 'zh') return '';
    // 其他语言使用 /en 路径
    return '/en';
  }

  /// 官网首页
  static String home([Locale? locale]) => '$baseUrl${_langPrefix(locale)}';

  /// 使用帮助/文档首页
  static String docs([Locale? locale]) =>
      '$baseUrl${_langPrefix(locale)}/docs/intro';

  /// 兼容旧入口；返回不含外观或来源参数的普通链接。
  static String docsEmbed(Locale? locale,
          {required bool dark, required String primaryHex}) =>
      '$baseUrl${_langPrefix(locale)}/docs/intro';

  /// 功能介绍
  static String docsFeature(String feature, [Locale? locale]) =>
      '$baseUrl${_langPrefix(locale)}/docs/features/$feature';

  /// 记账相关文档
  static String docsRecord(String topic, [Locale? locale]) =>
      '$baseUrl${_langPrefix(locale)}/docs/record/$topic';

  /// AI 相关文档
  static String docsAi(String topic, [Locale? locale]) =>
      '$baseUrl${_langPrefix(locale)}/docs/ai/$topic';

  /// 云同步相关文档
  static String docsCloudSync(String topic, [Locale? locale]) =>
      '$baseUrl${_langPrefix(locale)}/docs/cloud-sync/$topic';

  /// 云同步指南兼容入口，不附加设备或外观信息。
  static String docsCloudSyncEmbed(String topic, Locale? locale,
          {required bool dark, required String primaryHex}) =>
      '$baseUrl${_langPrefix(locale)}/docs/cloud-sync/$topic';

  /// FAQ
  static String faq([Locale? locale]) =>
      '$baseUrl${_langPrefix(locale)}/docs/faq';

  /// 更新日志
  static String changelog([Locale? locale]) =>
      '$baseUrl${_langPrefix(locale)}/docs/changelog';

  /// 更新日志兼容入口，不附加查询参数。
  static String changelogEmbed(Locale? locale,
          {required bool dark, required String primaryHex}) =>
      '$baseUrl${_langPrefix(locale)}/docs/changelog';

  /// 隐私政策普通链接；应用内使用打包的离线政策。
  static String privacy(Locale? locale,
          {required bool dark, required String primaryHex}) =>
      '$baseUrl${_langPrefix(locale)}/privacy';
}
