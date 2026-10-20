import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../l10n/app_localizations.dart';
import '../../providers/language_provider.dart';
import '../../utils/app_localizations_resolver.dart';

enum AppLinkAction { newTransaction, open, unknown }

/// 桌面小组件携带的导航参数。所有交易由用户在编辑页确认保存。
class AddTransactionParams {
  final double amount;
  final String type;
  final int? categoryId;
  final String? page;
  const AddTransactionParams({required this.amount, this.type = 'expense',
      this.categoryId, this.page});
}

class AppLinkResult {
  final bool success;
  final String? message;
  const AppLinkResult({required this.success, this.message});
  factory AppLinkResult.success({String? message}) =>
      AppLinkResult(success: true, message: message);
  factory AppLinkResult.failure(String message) =>
      AppLinkResult(success: false, message: message);
}

class AppLinkService {
  final ProviderContainer _container;
  AppLinkService(this._container);
  AppLocalizations get _l10n =>
      resolveAppLocalizations(_container.read(languageProvider));
  void Function(AppLinkAction action, {AddTransactionParams? params})? onNavigate;
  void Function(String message)? onShowToast;

  static AppLinkAction parseAction(Uri uri) {
    if (uri.scheme != 'beecount') return AppLinkAction.unknown;
    return switch (uri.host.toLowerCase()) {
      'new' => AppLinkAction.newTransaction,
      'open' => AppLinkAction.open,
      _ => AppLinkAction.unknown,
    };
  }

  Future<AppLinkResult> handleUrl(Uri uri) async {
    final action = parseAction(uri);
    switch (action) {
      case AppLinkAction.newTransaction:
        final requestedType = uri.queryParameters['type'];
        final type = const ['expense', 'income', 'transfer'].contains(requestedType)
            ? requestedType! : 'expense';
        final categoryId = int.tryParse(uri.queryParameters['category'] ?? '');
        onNavigate?.call(action, params: AddTransactionParams(
          amount: 0, type: type,
          categoryId: categoryId != null && categoryId > 0 ? categoryId : null,
        ));
        return AppLinkResult.success(message: _l10n.appLinkOpenManualBilling);
      case AppLinkAction.open:
        final page = uri.queryParameters['page'];
        if (!const ['assets', 'budget', 'detail'].contains(page)) {
          return AppLinkResult.failure(_l10n.appLinkNoTargetPage);
        }
        onNavigate?.call(action, params: AddTransactionParams(amount: 0, page: page));
        return AppLinkResult.success(message: _l10n.appLinkOpenPage(page!));
      case AppLinkAction.unknown:
        return AppLinkResult.failure(_l10n.appLinkUnknownAction(uri.host));
    }
  }

  void dispose() { onNavigate = null; onShowToast = null; }
}

class AppLinkBuilder {
  static const String scheme = 'beecount';
  /// 新建支出记账链接
  static String newExpense() => '$scheme://new?type=expense';

  /// 新建收入记账链接
  static String newIncome() => '$scheme://new?type=income';

  /// 新建转账记账链接
  static String newTransfer() => '$scheme://new?type=transfer';

  /// 新建支出记账并预填分类链接（小组件「快速记账」点分类格用）
  static String newExpenseWithCategory(int categoryId) =>
      '$scheme://new?type=expense&category=$categoryId';

  /// 打开净资产（资产页）链接（小组件「净资产」卡片点击用）
  static String openAssets() => '$scheme://open?page=assets';

  /// 打开预算页链接（小组件「预算进度」卡片点击用）
  static String openBudget() => '$scheme://open?page=budget';

  /// 打开明细页链接（小组件「最近交易」卡片点击用）
  static String openDetail() => '$scheme://open?page=detail';

}
