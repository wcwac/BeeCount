import 'dart:io';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:yaml/yaml.dart';
import 'package:drift/drift.dart' as d;
import '../../utils/app_localizations_resolver.dart';
import '../../data/db.dart';
import '../../models/finance.dart';
import '../../data/repositories/base_repository.dart';
import '../system/logger_service.dart';

// 导入 OrderingTerm
typedef OrderingTerm = d.OrderingTerm;

/// 导出选项 - 控制导出哪些内容
class ExportOptions {
  final bool ledgers;
  final bool categories;
  final bool accounts;
  final bool tags;
  final bool recurringTransactions;
  final bool budgets;
  final bool appSettings; // 本地应用偏好
  const ExportOptions({
    this.ledgers = true,
    this.categories = true,
    this.accounts = true,
    this.tags = true,
    this.recurringTransactions = true,
    this.budgets = true,
    this.appSettings = true,
  });

  /// 全选
  static const all = ExportOptions();

  /// 全不选
  static const none = ExportOptions(
    ledgers: false,
    categories: false,
    accounts: false,
    tags: false,
    recurringTransactions: false,
    budgets: false,
    appSettings: false,
  );
}

/// 应用配置模型
class AppConfig {
  final AppSettingsConfig? appSettings;
  final LedgersConfig? ledgers;
  final RecurringTransactionsConfig? recurringTransactions;
  final AccountsConfig? accounts;
  final CategoriesConfig? categories;
  final TagsConfig? tags;
  final BudgetsConfig? budgets;

  const AppConfig({
    this.appSettings,
    this.ledgers,
    this.recurringTransactions,
    this.accounts,
    this.categories,
    this.tags,
    this.budgets,
  });

  Map<String, dynamic> toYaml() {
    final map = <String, dynamic>{};











    if (appSettings != null) {
      map['app_settings'] = appSettings!.toMap();
    }

    if (ledgers != null) {
      map['ledgers'] = ledgers!.toMap();
    }

    if (recurringTransactions != null) {
      map['recurring_transactions'] = recurringTransactions!.toMap();
    }

    if (accounts != null) {
      map['accounts'] = accounts!.toMap();
    }

    if (categories != null) {
      map['categories'] = categories!.toMap();
    }

    if (tags != null) {
      map['tags'] = tags!.toMap();
    }

    if (budgets != null) {
      map['budgets'] = budgets!.toMap();
    }

    return map;
  }

  static AppConfig fromYaml(Map<dynamic, dynamic> yaml) {
    return AppConfig(
      appSettings: yaml.containsKey('app_settings')
          ? AppSettingsConfig.fromMap(
              Map<String, dynamic>.from(yaml['app_settings'] as Map))
          : null,
      ledgers: yaml.containsKey('ledgers')
          ? LedgersConfig.fromMap(
              Map<String, dynamic>.from(yaml['ledgers'] as Map))
          : null,
      recurringTransactions: yaml.containsKey('recurring_transactions')
          ? RecurringTransactionsConfig.fromMap(
              Map<String, dynamic>.from(yaml['recurring_transactions'] as Map))
          : null,
      accounts: yaml.containsKey('accounts')
          ? AccountsConfig.fromMap(
              Map<String, dynamic>.from(yaml['accounts'] as Map))
          : null,
      categories: yaml.containsKey('categories')
          ? CategoriesConfig.fromMap(
              Map<String, dynamic>.from(yaml['categories'] as Map))
          : null,
      tags: yaml.containsKey('tags')
          ? TagsConfig.fromMap(
              Map<String, dynamic>.from(yaml['tags'] as Map))
          : null,
      budgets: yaml.containsKey('budgets')
          ? BudgetsConfig.fromMap(
              Map<String, dynamic>.from(yaml['budgets'] as Map))
          : null,
    );
  }
}

class AppSettingsConfig {
  // 账户管理
  final bool? accountFeatureEnabled;
  final String? defaultIncomeAccountName; // 默认收入账户名称（用于导出/导入匹配）
  final String? defaultExpenseAccountName; // 默认支出账户名称（用于导出/导入匹配）

  // 记账提醒
  final bool? reminderEnabled;
  final int? reminderHour;
  final int? reminderMinute;

  // 语言设置
  final String? languageCode;
  final String? countryCode;

  // 个性化设置
  final int? primaryColor;
  final int? fontScaleLevel;
  final double? customFontScale;

  // 外观设置
  final String? themeMode;
  final bool? compactAmount;
  final bool? showTransactionTime;
  final String? noteDisplayMode; // 备注显示方式:'category' | 'note'
  final String? noteHistoryScope; // 历史备注范围:'allCategories' | 'currentCategory'
  final String? noteHistorySort; // 历史备注排序:'frequency' | 'recent'
  final int? noteHistoryLimit; // 历史备注展示数量
  final bool? incomeExpenseColorScheme; // 收支颜色方案：true=红色收入/绿色支出，false=红色支出/绿色收入

  // 云服务选择

  // 自动记账功能

  const AppSettingsConfig({
    this.accountFeatureEnabled,
    this.defaultIncomeAccountName,
    this.defaultExpenseAccountName,
    this.reminderEnabled,
    this.reminderHour,
    this.reminderMinute,
    this.languageCode,
    this.countryCode,
    this.primaryColor,
    this.fontScaleLevel,
    this.customFontScale,
    this.themeMode,
    this.compactAmount,
    this.showTransactionTime,
    this.noteDisplayMode,
    this.noteHistoryScope,
    this.noteHistorySort,
    this.noteHistoryLimit,
    this.incomeExpenseColorScheme,
  });

  Map<String, dynamic> toMap() {
    final map = <String, dynamic>{};

    if (accountFeatureEnabled != null) {
      map['account_feature_enabled'] = accountFeatureEnabled;
    }
    if (defaultIncomeAccountName != null) {
      map['default_income_account_name'] = defaultIncomeAccountName;
    }
    if (defaultExpenseAccountName != null) {
      map['default_expense_account_name'] = defaultExpenseAccountName;
    }
    if (reminderEnabled != null) {
      map['reminder_enabled'] = reminderEnabled;
    }
    if (reminderHour != null) {
      map['reminder_hour'] = reminderHour;
    }
    if (reminderMinute != null) {
      map['reminder_minute'] = reminderMinute;
    }
    if (languageCode != null && languageCode!.isNotEmpty) {
      map['language_code'] = languageCode;
    }
    if (countryCode != null && countryCode!.isNotEmpty) {
      map['country_code'] = countryCode;
    }
    if (primaryColor != null) {
      map['primary_color'] = primaryColor;
    }
    if (fontScaleLevel != null) {
      map['font_scale_level'] = fontScaleLevel;
    }
    if (customFontScale != null) {
      map['custom_font_scale'] = customFontScale;
    }
    if (themeMode != null && themeMode!.isNotEmpty) {
      map['theme_mode'] = themeMode;
    }
    if (compactAmount != null) {
      map['compact_amount'] = compactAmount;
    }
    if (showTransactionTime != null) {
      map['show_transaction_time'] = showTransactionTime;
    }
    if (noteDisplayMode != null && noteDisplayMode!.isNotEmpty) {
      map['note_display_mode'] = noteDisplayMode;
    }
    if (noteHistoryScope != null && noteHistoryScope!.isNotEmpty) {
      map['note_history_scope'] = noteHistoryScope;
    }
    if (noteHistorySort != null && noteHistorySort!.isNotEmpty) {
      map['note_history_sort'] = noteHistorySort;
    }
    if (noteHistoryLimit != null) {
      map['note_history_limit'] = noteHistoryLimit;
    }
    if (incomeExpenseColorScheme != null) {
      map['income_expense_color_scheme'] = incomeExpenseColorScheme;
    }

    return map;
  }

  static AppSettingsConfig fromMap(Map<String, dynamic> map) =>
      AppSettingsConfig(
        accountFeatureEnabled: map['account_feature_enabled'] as bool?,
        defaultIncomeAccountName: map['default_income_account_name'] as String?,
        defaultExpenseAccountName: map['default_expense_account_name'] as String?,
        reminderEnabled: map['reminder_enabled'] as bool?,
        reminderHour: map['reminder_hour'] as int?,
        reminderMinute: map['reminder_minute'] as int?,
        languageCode: map['language_code'] as String?,
        countryCode: map['country_code'] as String?,
        primaryColor: map['primary_color'] as int?,
        fontScaleLevel: map['font_scale_level'] as int?,
        customFontScale: map['custom_font_scale'] != null
            ? (map['custom_font_scale'] as num).toDouble()
            : null,
        themeMode: map['theme_mode'] as String?,
        compactAmount: map['compact_amount'] as bool?,
        showTransactionTime: map['show_transaction_time'] as bool?,
        noteDisplayMode: map['note_display_mode'] as String?,
        noteHistoryScope: map['note_history_scope'] as String?,
        noteHistorySort: map['note_history_sort'] as String?,
        noteHistoryLimit: map['note_history_limit'] as int?,
        incomeExpenseColorScheme: map['income_expense_color_scheme'] as bool?,
      );
}

/// 账本配置
class LedgersConfig {
  final List<LedgerItem> items;

  const LedgersConfig({required this.items});

  Map<String, dynamic> toMap() {
    return {
      'items': items.map((item) => item.toMap()).toList(),
    };
  }

  static LedgersConfig fromMap(Map<String, dynamic> map) {
    final itemsList = map['items'] as List<dynamic>? ?? [];
    return LedgersConfig(
      items: itemsList
          .map((item) =>
              LedgerItem.fromMap(Map<String, dynamic>.from(item as Map)))
          .toList(),
    );
  }
}

/// 账本项
class LedgerItem {
  final String name;
  final String currency;
  final String? type; // personal / shared
  final String? createdAt; // ISO 8601 format

  const LedgerItem({
    required this.name,
    required this.currency,
    this.type,
    this.createdAt,
  });

  Map<String, dynamic> toMap() {
    final map = <String, dynamic>{
      'name': name,
      'currency': currency,
    };
    if (type != null && type!.isNotEmpty) map['type'] = type;
    if (createdAt != null) map['created_at'] = createdAt;
    return map;
  }

  static LedgerItem fromMap(Map<String, dynamic> map) {
    return LedgerItem(
      name: map['name'] as String,
      currency: map['currency'] as String? ?? 'CNY',
      type: map['type'] as String?,
      createdAt: map['created_at'] as String?,
    );
  }

  factory LedgerItem.fromDb(Ledger ledger) {
    return LedgerItem(
      name: ledger.name,
      currency: ledger.currency,
      type: ledger.type,
      createdAt: ledger.createdAt.toIso8601String(),
    );
  }
}

/// 周期账单配置
class RecurringTransactionsConfig {
  final List<RecurringTransactionItem> items;

  const RecurringTransactionsConfig({required this.items});

  Map<String, dynamic> toMap() {
    return {
      'items': items.map((item) => item.toMap()).toList(),
    };
  }

  static RecurringTransactionsConfig fromMap(Map<String, dynamic> map) {
    final itemsList = map['items'] as List<dynamic>? ?? [];
    return RecurringTransactionsConfig(
      items: itemsList
          .map((item) =>
              RecurringTransactionItem.fromMap(Map<String, dynamic>.from(item as Map)))
          .toList(),
    );
  }
}

/// 周期账单项
class RecurringTransactionItem {
  final String ledgerName; // 账本名称（用于导出/导入匹配）
  final String type; // expense / income / transfer
  final double amount;
  final String? categoryName; // 分类名称（用于导出/导入匹配）
  final String? accountName; // 账户名称（用于导出/导入匹配）
  final String? toAccountName; // 转账目标账户名称（用于导出/导入匹配）
  final String? note;
  final String frequency; // daily / weekly / monthly / yearly
  final int interval;
  final int? dayOfMonth;
  final int? dayOfWeek;
  final int? monthOfYear;
  final String startDate; // ISO 8601 format
  final String? endDate;
  final bool enabled;
  final String? currencyCode; // v32(#444)模板币种;null = 账本本位币

  const RecurringTransactionItem({
    required this.ledgerName,
    required this.type,
    required this.amount,
    this.categoryName,
    this.accountName,
    this.toAccountName,
    this.note,
    required this.frequency,
    required this.interval,
    this.dayOfMonth,
    this.dayOfWeek,
    this.monthOfYear,
    required this.startDate,
    this.endDate,
    required this.enabled,
    this.currencyCode,
  });

  Map<String, dynamic> toMap() {
    final map = <String, dynamic>{
      'ledger_name': ledgerName,
      'type': type,
      'amount': amount,
      'frequency': frequency,
      'interval': interval,
      'start_date': startDate,
      'enabled': enabled,
    };
    if (categoryName != null) map['category_name'] = categoryName;
    if (accountName != null) map['account_name'] = accountName;
    if (toAccountName != null) map['to_account_name'] = toAccountName;
    if (note != null && note!.isNotEmpty) map['note'] = note;
    if (dayOfMonth != null) map['day_of_month'] = dayOfMonth;
    if (dayOfWeek != null) map['day_of_week'] = dayOfWeek;
    if (monthOfYear != null) map['month_of_year'] = monthOfYear;
    if (endDate != null) map['end_date'] = endDate;
    if (currencyCode != null) map['currency_code'] = currencyCode;
    return map;
  }

  static RecurringTransactionItem fromMap(Map<String, dynamic> map) {
    return RecurringTransactionItem(
      ledgerName: map['ledger_name'] as String,
      type: map['type'] as String,
      amount: (map['amount'] as num).toDouble(),
      categoryName: map['category_name'] as String?,
      accountName: map['account_name'] as String?,
      toAccountName: map['to_account_name'] as String?,
      note: map['note'] as String?,
      frequency: map['frequency'] as String,
      interval: map['interval'] as int,
      dayOfMonth: map['day_of_month'] as int?,
      dayOfWeek: map['day_of_week'] as int?,
      monthOfYear: map['month_of_year'] as int?,
      startDate: map['start_date'] as String,
      endDate: map['end_date'] as String?,
      enabled: map['enabled'] as bool,
      currencyCode: map['currency_code'] as String?,
    );
  }

  /// 从数据库实体创建，需要传入名称映射
  factory RecurringTransactionItem.fromDb(
    RecurringTransaction rt, {
    required Map<int, String> ledgerIdToName,
    required Map<int, String> categoryIdToName,
    required Map<int, String> accountIdToName,
  }) {
    return RecurringTransactionItem(
      ledgerName: ledgerIdToName[rt.ledgerId] ?? 'Unknown',
      type: rt.type,
      amount: rt.amount,
      categoryName: rt.categoryId != null ? categoryIdToName[rt.categoryId] : null,
      accountName: rt.accountId != null ? accountIdToName[rt.accountId] : null,
      toAccountName: rt.toAccountId != null ? accountIdToName[rt.toAccountId] : null,
      note: rt.note,
      frequency: rt.frequency,
      interval: rt.interval,
      dayOfMonth: rt.dayOfMonth,
      dayOfWeek: rt.dayOfWeek,
      monthOfYear: rt.monthOfYear,
      startDate: rt.startDate.toIso8601String(),
      endDate: rt.endDate?.toIso8601String(),
      enabled: rt.enabled,
      currencyCode: rt.currencyCode,
    );
  }
}

/// 账户配置
class AccountsConfig {
  final List<AccountItem> items;

  const AccountsConfig({required this.items});

  Map<String, dynamic> toMap() {
    return {
      'items': items.map((item) => item.toMap()).toList(),
    };
  }

  static AccountsConfig fromMap(Map<String, dynamic> map) {
    final itemsList = map['items'] as List<dynamic>? ?? [];
    return AccountsConfig(
      items: itemsList
          .map((item) =>
              AccountItem.fromMap(Map<String, dynamic>.from(item as Map)))
          .toList(),
    );
  }
}

/// 账户项
class AccountItem {
  final String name;
  final String type;
  final String currency;
  final double initialBalance;
  final String? createdAt; // ISO 8601 format
  final double? creditLimit; // 信用额度
  final int? billingDay; // 账单日 (1-28)
  final int? paymentDueDay; // 还款日 (1-28)
  final String? bankName; // 开户行
  final String? cardLastFour; // 卡号后四位
  final String? note; // 备注

  const AccountItem({
    required this.name,
    required this.type,
    required this.currency,
    required this.initialBalance,
    this.createdAt,
    this.creditLimit,
    this.billingDay,
    this.paymentDueDay,
    this.bankName,
    this.cardLastFour,
    this.note,
  });

  Map<String, dynamic> toMap() {
    final map = <String, dynamic>{
      'name': name,
      'type': type,
      'currency': currency,
      'initial_balance': initialBalance,
    };
    if (createdAt != null) map['created_at'] = createdAt;
    if (creditLimit != null) map['credit_limit'] = creditLimit;
    if (billingDay != null) map['billing_day'] = billingDay;
    if (paymentDueDay != null) map['payment_due_day'] = paymentDueDay;
    if (bankName != null) map['bank_name'] = bankName;
    if (cardLastFour != null) map['card_last_four'] = cardLastFour;
    if (note != null) map['note'] = note;
    return map;
  }

  static AccountItem fromMap(Map<String, dynamic> map) {
    return AccountItem(
      name: map['name'] as String,
      type: map['type'] as String,
      currency: map['currency'] as String? ?? 'CNY',
      initialBalance: (map['initial_balance'] as num?)?.toDouble() ?? 0.0,
      createdAt: map['created_at'] as String?,
      creditLimit: (map['credit_limit'] as num?)?.toDouble(),
      billingDay: map['billing_day'] as int?,
      paymentDueDay: map['payment_due_day'] as int?,
      bankName: map['bank_name'] as String?,
      cardLastFour: map['card_last_four'] as String?,
      note: map['note'] as String?,
    );
  }

  factory AccountItem.fromDb(Account account) {
    return AccountItem(
      name: account.name,
      type: account.type,
      currency: account.currency,
      initialBalance: account.initialBalance,
      createdAt: account.createdAt?.toIso8601String(),
      creditLimit: account.creditLimit,
      billingDay: account.billingDay,
      paymentDueDay: account.paymentDueDay,
      bankName: account.bankName,
      cardLastFour: account.cardLastFour,
      note: account.note,
    );
  }
}

/// 分类配置
class CategoriesConfig {
  final List<CategoryItem> items;

  const CategoriesConfig({required this.items});

  Map<String, dynamic> toMap() {
    return {
      'items': items.map((item) => item.toMap()).toList(),
    };
  }

  static CategoriesConfig fromMap(Map<String, dynamic> map) {
    final itemsList = map['items'] as List<dynamic>? ?? [];
    return CategoriesConfig(
      items: itemsList
          .map((item) =>
              CategoryItem.fromMap(Map<String, dynamic>.from(item as Map)))
          .toList(),
    );
  }
}

/// 分类项
class CategoryItem {
  final String name;
  final String kind; // expense / income
  final String? icon;
  final int sortOrder;
  final String? parentName; // 使用父分类名称而非ID
  final int level;
  final String? iconType; // material / custom / community
  final String? customIconPath; // 自定义图标相对路径
  final String? communityIconId; // 社区图标ID

  const CategoryItem({
    required this.name,
    required this.kind,
    this.icon,
    required this.sortOrder,
    this.parentName,
    required this.level,
    this.iconType,
    this.customIconPath,
    this.communityIconId,
  });

  Map<String, dynamic> toMap() {
    final map = <String, dynamic>{
      'name': name,
      'kind': kind,
      'sort_order': sortOrder,
      'level': level,
    };
    if (icon != null) map['icon'] = icon;
    if (parentName != null) map['parent_name'] = parentName;
    if (iconType != null) map['icon_type'] = iconType;
    if (customIconPath != null) map['custom_icon_path'] = customIconPath;
    if (communityIconId != null) map['community_icon_id'] = communityIconId;
    return map;
  }

  static CategoryItem fromMap(Map<String, dynamic> map) {
    return CategoryItem(
      name: map['name'] as String,
      kind: map['kind'] as String,
      icon: map['icon'] as String?,
      sortOrder: map['sort_order'] as int? ?? 0,
      parentName: map['parent_name'] as String?,
      level: map['level'] as int? ?? 1,
      iconType: map['icon_type'] as String?,
      customIconPath: map['custom_icon_path'] as String?,
      communityIconId: map['community_icon_id'] as String?,
    );
  }

  factory CategoryItem.fromDb(Category category, String? parentName) {
    return CategoryItem(
      name: category.name,
      kind: category.kind,
      icon: category.icon,
      sortOrder: category.sortOrder,
      parentName: parentName,
      level: category.level,
      iconType: category.iconType,
      customIconPath: category.customIconPath,
      communityIconId: category.communityIconId,
    );
  }
}

/// 标签配置
class TagsConfig {
  final List<TagItem> items;

  const TagsConfig({required this.items});

  Map<String, dynamic> toMap() {
    return {
      'items': items.map((item) => item.toMap()).toList(),
    };
  }

  static TagsConfig fromMap(Map<String, dynamic> map) {
    final itemsList = map['items'] as List<dynamic>? ?? [];
    return TagsConfig(
      items: itemsList
          .map((item) =>
              TagItem.fromMap(Map<String, dynamic>.from(item as Map)))
          .toList(),
    );
  }
}

/// 标签项
class TagItem {
  final String name;
  final String? color;

  const TagItem({
    required this.name,
    this.color,
  });

  Map<String, dynamic> toMap() {
    final map = <String, dynamic>{
      'name': name,
    };
    if (color != null && color!.isNotEmpty) {
      map['color'] = color;
    }
    return map;
  }

  static TagItem fromMap(Map<String, dynamic> map) {
    return TagItem(
      name: map['name'] as String,
      color: map['color']?.toString(),
    );
  }

  factory TagItem.fromDb(Tag tag) {
    return TagItem(
      name: tag.name,
      color: tag.color,
    );
  }
}

/// 预算配置
class BudgetsConfig {
  final List<BudgetItem> items;

  const BudgetsConfig({required this.items});

  Map<String, dynamic> toMap() {
    return {
      'items': items.map((item) => item.toMap()).toList(),
    };
  }

  static BudgetsConfig fromMap(Map<String, dynamic> map) {
    final itemsList = map['items'] as List<dynamic>? ?? [];
    return BudgetsConfig(
      items: itemsList
          .map((item) =>
              BudgetItem.fromMap(Map<String, dynamic>.from(item as Map)))
          .toList(),
    );
  }
}

/// 预算项
class BudgetItem {
  final String ledgerName; // 账本名称（用于导出/导入匹配）
  final String type; // total / category
  final String? categoryName; // 分类名称（用于导出/导入匹配）
  final double amount;
  final int startDay;

  const BudgetItem({
    required this.ledgerName,
    required this.type,
    this.categoryName,
    required this.amount,
    required this.startDay,
  });

  Map<String, dynamic> toMap() {
    final map = <String, dynamic>{
      'ledger_name': ledgerName,
      'type': type,
      'amount': amount,
      'start_day': startDay,
    };
    if (categoryName != null) map['category_name'] = categoryName;
    return map;
  }

  static BudgetItem fromMap(Map<String, dynamic> map) {
    return BudgetItem(
      ledgerName: map['ledger_name'] as String,
      type: map['type'] as String,
      categoryName: map['category_name'] as String?,
      amount: (map['amount'] as num).toDouble(),
      startDay: map['start_day'] as int? ?? 1,
    );
  }
}

/// 配置内容检测结果
class ConfigContentInfo {
  final bool hasLedgers;
  final bool hasCategories;
  final bool hasAccounts;
  final bool hasTags;
  final bool hasRecurringTransactions;
  final bool hasBudgets;
  final bool hasAppSettings; // 本地应用偏好

  const ConfigContentInfo({
    this.hasLedgers = false,
    this.hasCategories = false,
    this.hasAccounts = false,
    this.hasTags = false,
    this.hasRecurringTransactions = false,
    this.hasBudgets = false,
    this.hasAppSettings = false,
  });
}

/// 配置导入导出服务
class ConfigExportService {
  /// 检测 YAML 内容中包含哪些配置项
  static ConfigContentInfo detectContent(String yamlContent) {
    try {
      final doc = loadYaml(yamlContent);
      if (doc is! Map) {
        return const ConfigContentInfo();
      }

      return ConfigContentInfo(
        hasLedgers: doc.containsKey('ledgers'),
        hasCategories: doc.containsKey('categories'),
        hasAccounts: doc.containsKey('accounts'),
        hasTags: doc.containsKey('tags'),
        hasRecurringTransactions: doc.containsKey('recurring_transactions'),
        hasBudgets: doc.containsKey('budgets'),
        hasAppSettings: doc.containsKey('app_settings'),
      );
    } catch (_) {
      return const ConfigContentInfo();
    }
  }

  /// 导出配置到YAML字符串
  /// [repository] 数据仓库实例，用于导出周期账单等数据
  /// [ledgerId] 账本ID，用于过滤导出的周期账单
  /// [options] 导出选项，控制导出哪些内容
  static Future<String> exportToYaml({
    BaseRepository? repository,
    int? ledgerId,
    ExportOptions options = ExportOptions.all,
  }) async {
    final prefs = await SharedPreferences.getInstance();

    // 预先获取所有账本、分类、账户的名称映射（用于关联数据导出）
    Map<int, String> ledgerIdToName = {};
    Map<int, String> categoryIdToName = {};
    Map<int, String> accountIdToName = {};

    if (repository != null) {
      try {
        final ledgers = await repository.getAllLedgers();
        ledgerIdToName = {for (var l in ledgers) l.id: l.name};

        final categories = await repository.getAllCategories();
        categoryIdToName = {for (var c in categories) c.id: c.name};

        final accounts = await repository.getAllAccounts();
        accountIdToName = {for (var a in accounts) a.id: a.name};
      } catch (e) {
        logger.warning('ConfigExport', '获取名称映射失败: $e');
      }
    }

    // 收集需要强制导出的关联数据ID
    final Set<int> requiredLedgerIds = {};
    final Set<int> requiredCategoryIds = {};
    final Set<int> requiredAccountIds = {};

    // 读取应用设置
    AppSettingsConfig? appSettings;
    final accountFeatureEnabled = prefs.getBool('account_feature_enabled');
    final defaultIncomeAccountId = prefs.getInt('default_income_account_id');
    final defaultExpenseAccountId = prefs.getInt('default_expense_account_id');
    final reminderEnabled = prefs.getBool('reminder_enabled');
    final reminderHour = prefs.getInt('reminder_hour');
    final reminderMinute = prefs.getInt('reminder_minute');
    final languageCode = prefs.getString('selected_language');
    final countryCode = prefs.getString('selected_language_country');
    final primaryColor = prefs.getInt('primaryColor');
    final fontScaleLevel = prefs.getInt('fontScaleLevel');
    final customFontScale = prefs.getDouble('customFontScale');
    final themeMode = prefs.getString('themeMode');
    final compactAmount = prefs.getBool('compactAmount');
    final showTransactionTime = prefs.getBool('showTransactionTime');
    final noteDisplayMode = prefs.getString('noteDisplayMode');
    // 兼容尚未完成首次偏好初始化的旧安装，导出时始终写入历史备注默认配置。
    final noteHistoryScope =
        prefs.getString('noteHistoryScope') ?? 'allCategories';
    final noteHistorySort = prefs.getString('noteHistorySort') ?? 'frequency';
    final noteHistoryLimit =
        prefs.getInt('noteHistoryLimit') ?? 20;
    final incomeExpenseColorScheme = prefs.getBool('incomeExpenseColorScheme');

    // 获取默认账户名称并收集需要强制导出的账户
    String? defaultIncomeAccountName;
    String? defaultExpenseAccountName;
    if (defaultIncomeAccountId != null) {
      defaultIncomeAccountName = accountIdToName[defaultIncomeAccountId];
      if (defaultIncomeAccountName != null) {
        requiredAccountIds.add(defaultIncomeAccountId);
      }
    }
    if (defaultExpenseAccountId != null) {
      defaultExpenseAccountName = accountIdToName[defaultExpenseAccountId];
      if (defaultExpenseAccountName != null) {
        requiredAccountIds.add(defaultExpenseAccountId);
      }
    }

    // 如果有任何应用设置，就创建配置对象
    if (accountFeatureEnabled != null ||
        defaultIncomeAccountName != null ||
        defaultExpenseAccountName != null ||
        reminderEnabled != null ||
        reminderHour != null ||
        reminderMinute != null ||
        languageCode != null ||
        countryCode != null ||
        primaryColor != null ||
        fontScaleLevel != null ||
        customFontScale != null ||
        themeMode != null ||
        compactAmount != null ||
        showTransactionTime != null ||
        noteDisplayMode != null ||
        noteHistoryScope != null ||
        noteHistorySort != null ||
        noteHistoryLimit != null ||
        incomeExpenseColorScheme != null) {
      appSettings = AppSettingsConfig(
        accountFeatureEnabled: accountFeatureEnabled,
        defaultIncomeAccountName: defaultIncomeAccountName,
        defaultExpenseAccountName: defaultExpenseAccountName,
        reminderEnabled: reminderEnabled,
        reminderHour: reminderHour,
        reminderMinute: reminderMinute,
        languageCode: languageCode,
        countryCode: countryCode,
        primaryColor: primaryColor,
        fontScaleLevel: fontScaleLevel,
        customFontScale: customFontScale,
        themeMode: themeMode,
        compactAmount: compactAmount,
        showTransactionTime: showTransactionTime,
        noteDisplayMode: noteDisplayMode,
        noteHistoryScope: noteHistoryScope,
        noteHistorySort: noteHistorySort,
        noteHistoryLimit: noteHistoryLimit,
        incomeExpenseColorScheme: incomeExpenseColorScheme,
      );
    }

    // 读取周期账单配置（导出全部账本的周期记账）
    RecurringTransactionsConfig? recurringConfig;
    if (options.recurringTransactions && repository != null) {
      try {
        final recurringList = await repository.getAllRecurringTransactions();

        if (recurringList.isNotEmpty) {
          // 收集周期账单关联的账本、分类、账户ID
          for (final rt in recurringList) {
            requiredLedgerIds.add(rt.ledgerId);
            if (rt.categoryId != null) {
              requiredCategoryIds.add(rt.categoryId!);
            }
            if (rt.accountId != null) {
              requiredAccountIds.add(rt.accountId!);
            }
            if (rt.toAccountId != null) {
              requiredAccountIds.add(rt.toAccountId!);
            }
          }

          recurringConfig = RecurringTransactionsConfig(
            items: recurringList
                .map((rt) => RecurringTransactionItem.fromDb(
                      rt,
                      ledgerIdToName: ledgerIdToName,
                      categoryIdToName: categoryIdToName,
                      accountIdToName: accountIdToName,
                    ))
                .toList(),
          );
        }
      } catch (e) {
        logger.warning('ConfigExport', '读取周期账单配置失败: $e');
      }
    }

    // 读取账本配置（导出全部账本，或强制导出关联的账本）
    LedgersConfig? ledgersConfig;
    if (repository != null && (options.ledgers || requiredLedgerIds.isNotEmpty)) {
      try {
        final ledgersList = await repository.getAllLedgers();

        if (ledgersList.isNotEmpty) {
          // 如果用户选择了导出账本，则导出全部
          // 如果用户没有选择但有关联数据需要账本，则只导出关联的账本
          final itemsToExport = options.ledgers
              ? ledgersList
              : ledgersList.where((l) => requiredLedgerIds.contains(l.id)).toList();

          if (itemsToExport.isNotEmpty) {
            ledgersConfig = LedgersConfig(
              items: itemsToExport
                  .map((ledger) => LedgerItem.fromDb(ledger))
                  .toList(),
            );
          }
        }
      } catch (e) {
        logger.warning('ConfigExport', '读取账本配置失败: $e');
      }
    }

    // 读取账户配置（导出全部账户，或强制导出关联的账户）
    AccountsConfig? accountsConfig;
    if (repository != null && (options.accounts || requiredAccountIds.isNotEmpty)) {
      try {
        // Managed balances require events and links from the complete JSON
        // backup. Configuration-only export must not turn them into freehand
        // accounts and silently duplicate assets on import.
        final accountsList = (await repository.getAllAccounts())
            .where((a) => !financeAccountTypes.contains(a.type)).toList();

        if (accountsList.isNotEmpty) {
          // 如果用户选择了导出账户，则导出全部
          // 如果用户没有选择但有关联数据需要账户，则只导出关联的账户
          final itemsToExport = options.accounts
              ? accountsList
              : accountsList.where((a) => requiredAccountIds.contains(a.id)).toList();

          if (itemsToExport.isNotEmpty) {
            accountsConfig = AccountsConfig(
              items: itemsToExport
                  .map((account) => AccountItem.fromDb(account))
                  .toList(),
            );
          }
        }
      } catch (e) {
        logger.warning('ConfigExport', '读取账户配置失败: $e');
      }
    }

    // 读取分类配置（导出全部分类，或强制导出关联的分类）
    CategoriesConfig? categoriesConfig;
    if (repository != null && (options.categories || requiredCategoryIds.isNotEmpty)) {
      try {
        // 获取所有分类（收入、支出和转账）
        final expenseCategories = await repository.getTopLevelCategories('expense');
        final incomeCategories = await repository.getTopLevelCategories('income');
        final categoriesList = <Category>[];
        categoriesList.addAll(expenseCategories);
        categoriesList.addAll(incomeCategories);

        // 获取虚拟转账分类
        try {
          final transferCategory = await repository.getTransferCategory();
          categoriesList.add(transferCategory);
        } catch (e) {
          // 转账分类不存在时忽略
        }

        // 获取所有子分类
        for (final category in [...expenseCategories, ...incomeCategories]) {
          final subCategories = await repository.getSubCategories(category.id);
          categoriesList.addAll(subCategories);
        }

        if (categoriesList.isNotEmpty) {
          // 构建 ID 到分类的映射，用于查找父分类名称
          final categoryMap = <int, Category>{
            for (var cat in categoriesList) cat.id: cat
          };

          // 如果用户选择了导出分类，则导出全部
          // 如果用户没有选择但有关联数据需要分类，则只导出关联的分类及其父分类
          List<Category> itemsToExport;
          if (options.categories) {
            itemsToExport = categoriesList;
          } else {
            // 收集需要导出的分类（包括父分类）
            final idsToExport = <int>{};
            for (final id in requiredCategoryIds) {
              if (categoryMap.containsKey(id)) {
                idsToExport.add(id);
                // 如果是子分类，也要导出父分类
                final parentId = categoryMap[id]!.parentId;
                if (parentId != null) {
                  idsToExport.add(parentId);
                }
              }
            }
            itemsToExport = categoriesList.where((c) => idsToExport.contains(c.id)).toList();
          }

          if (itemsToExport.isNotEmpty) {
            categoriesConfig = CategoriesConfig(
              items: itemsToExport.map((category) {
                // 查找父分类名称
                String? parentName;
                if (category.parentId != null && categoryMap.containsKey(category.parentId)) {
                  parentName = categoryMap[category.parentId]!.name;
                }
                return CategoryItem.fromDb(category, parentName);
              }).toList(),
            );
          }
        }
      } catch (e) {
        logger.warning('ConfigExport', '读取分类配置失败: $e');
      }
    }

    // 读取标签配置（导出全部标签）
    TagsConfig? tagsConfig;
    if (options.tags && repository != null) {
      try {
        final tagsList = await repository.getAllTags();

        if (tagsList.isNotEmpty) {
          tagsConfig = TagsConfig(
            items: tagsList.map((tag) => TagItem.fromDb(tag)).toList(),
          );
        }
      } catch (e) {
        logger.warning('ConfigExport', '读取标签配置失败: $e');
      }
    }

    // 读取预算配置（导出全部预算）
    BudgetsConfig? budgetsConfig;
    if (options.budgets && repository != null) {
      try {
        final budgetsList = await repository.getAllBudgetsForExport();

        if (budgetsList.isNotEmpty) {
          // 获取账本和分类的名称映射
          final ledgers = await repository.getAllLedgers();
          final ledgerMap = {for (var l in ledgers) l.id: l.name};

          final categories = await repository.getAllCategories();
          final categoryMap = {for (var c in categories) c.id: c.name};

          budgetsConfig = BudgetsConfig(
            items: budgetsList.map((budget) => BudgetItem(
              ledgerName: ledgerMap[budget.ledgerId] ?? 'Unknown',
              type: budget.type,
              categoryName: budget.categoryId != null
                  ? categoryMap[budget.categoryId]
                  : null,
              amount: budget.amount,
              startDay: budget.startDay,
            )).toList(),
          );
        }
      } catch (e) {
        logger.warning('ConfigExport', '读取预算配置失败: $e');
      }
    }

    final config = AppConfig(
      appSettings: options.appSettings ? appSettings : null,
      ledgers: ledgersConfig,
      recurringTransactions: recurringConfig,
      accounts: accountsConfig,
      categories: categoriesConfig,
      tags: tagsConfig,
      budgets: budgetsConfig,
    );

    // 转换为YAML格式
    final yamlMap = config.toYaml();

    // 手动构建YAML字符串以保持良好格式
    final buffer = StringBuffer();
    buffer.writeln('# BeeCount 应用配置');
    buffer.writeln('# 导出时间: ${DateTime.now().toIso8601String()}');
    buffer.writeln();

    if (yamlMap.containsKey('app_settings')) {
      buffer.writeln('app_settings:');
      final settings = yamlMap['app_settings'] as Map<String, dynamic>;

      if (settings.containsKey('account_feature_enabled') ||
          settings.containsKey('default_income_account_name') ||
          settings.containsKey('default_expense_account_name')) {
        buffer.writeln('  # 账户管理');
        if (settings.containsKey('account_feature_enabled')) {
          buffer.writeln('  account_feature_enabled: ${settings['account_feature_enabled']}');
        }
        if (settings.containsKey('default_income_account_name')) {
          buffer.writeln('  default_income_account_name: "${settings['default_income_account_name']}"');
        }
        if (settings.containsKey('default_expense_account_name')) {
          buffer.writeln('  default_expense_account_name: "${settings['default_expense_account_name']}"');
        }
      }

      if (settings.containsKey('reminder_enabled') ||
          settings.containsKey('reminder_hour') ||
          settings.containsKey('reminder_minute')) {
        buffer.writeln('  # 记账提醒');
        if (settings.containsKey('reminder_enabled')) {
          buffer.writeln('  reminder_enabled: ${settings['reminder_enabled']}');
        }
        if (settings.containsKey('reminder_hour')) {
          buffer.writeln('  reminder_hour: ${settings['reminder_hour']}');
        }
        if (settings.containsKey('reminder_minute')) {
          buffer.writeln('  reminder_minute: ${settings['reminder_minute']}');
        }
      }

      if (settings.containsKey('language_code') || settings.containsKey('country_code')) {
        buffer.writeln('  # 语言设置');
        if (settings.containsKey('language_code')) {
          buffer.writeln('  language_code: "${settings['language_code']}"');
        }
        if (settings.containsKey('country_code')) {
          buffer.writeln('  country_code: "${settings['country_code']}"');
        }
      }

      if (settings.containsKey('primary_color') ||
          settings.containsKey('font_scale_level') ||
          settings.containsKey('custom_font_scale')) {
        buffer.writeln('  # 个性化设置');
        if (settings.containsKey('primary_color')) {
          buffer.writeln('  primary_color: ${settings['primary_color']}');
        }
        if (settings.containsKey('font_scale_level')) {
          buffer.writeln('  font_scale_level: ${settings['font_scale_level']}');
        }
        if (settings.containsKey('custom_font_scale')) {
          buffer.writeln('  custom_font_scale: ${settings['custom_font_scale']}');
        }
      }

      if (settings.containsKey('theme_mode') ||
          settings.containsKey('compact_amount') ||
          settings.containsKey('show_transaction_time') ||
          settings.containsKey('note_display_mode') ||
          settings.containsKey('note_history_scope') ||
          settings.containsKey('note_history_sort') ||
          settings.containsKey('note_history_limit')) {
        buffer.writeln('  # 外观设置');
        if (settings.containsKey('theme_mode')) {
          buffer.writeln('  theme_mode: "${settings['theme_mode']}"');
        }
        if (settings.containsKey('compact_amount')) {
          buffer.writeln('  compact_amount: ${settings['compact_amount']}');
        }
        if (settings.containsKey('show_transaction_time')) {
          buffer.writeln('  show_transaction_time: ${settings['show_transaction_time']}');
        }
        if (settings.containsKey('note_display_mode')) {
          buffer.writeln('  note_display_mode: "${settings['note_display_mode']}"');
        }
        if (settings.containsKey('note_history_scope')) {
          buffer.writeln('  note_history_scope: "${settings['note_history_scope']}"');
        }
        if (settings.containsKey('note_history_sort')) {
          buffer.writeln('  note_history_sort: "${settings['note_history_sort']}"');
        }
        if (settings.containsKey('note_history_limit')) {
          buffer.writeln('  note_history_limit: ${settings['note_history_limit']}');
        }
      }
    }

    // 账本
    if (yamlMap.containsKey('ledgers')) {
      buffer.writeln('# 账本');
      buffer.writeln('ledgers:');
      final ledgers = yamlMap['ledgers'] as Map<String, dynamic>;
      final items = ledgers['items'] as List;

      if (items.isNotEmpty) {
        buffer.writeln('  items:');
        for (final item in items) {
          final itemMap = item as Map<String, dynamic>;
          buffer.writeln('    - name: "${itemMap['name']}"');
          buffer.writeln('      currency: "${itemMap['currency']}"');
          if (itemMap.containsKey('type') && itemMap['type'] != null) {
            buffer.writeln('      type: "${itemMap['type']}"');
          }
          if (itemMap.containsKey('created_at') && itemMap['created_at'] != null) {
            buffer.writeln('      created_at: "${itemMap['created_at']}"');
          }
        }
      }
      buffer.writeln();
    }

    // 周期账单
    if (yamlMap.containsKey('recurring_transactions')) {
      buffer.writeln('# 周期账单');
      buffer.writeln('recurring_transactions:');
      final recurring = yamlMap['recurring_transactions'] as Map<String, dynamic>;
      final items = recurring['items'] as List;

      if (items.isNotEmpty) {
        buffer.writeln('  items:');
        for (final item in items) {
          final itemMap = item as Map<String, dynamic>;
          buffer.writeln('    - ledger_name: "${itemMap['ledger_name']}"');
          buffer.writeln('      type: "${itemMap['type']}"');
          buffer.writeln('      amount: ${itemMap['amount']}');

          if (itemMap.containsKey('category_name') && itemMap['category_name'] != null) {
            buffer.writeln('      category_name: "${itemMap['category_name']}"');
          }
          if (itemMap.containsKey('account_name') && itemMap['account_name'] != null) {
            buffer.writeln('      account_name: "${itemMap['account_name']}"');
          }
          if (itemMap.containsKey('to_account_name') && itemMap['to_account_name'] != null) {
            buffer.writeln('      to_account_name: "${itemMap['to_account_name']}"');
          }
          if (itemMap.containsKey('note') && itemMap['note'] != null) {
            buffer.writeln('      note: "${itemMap['note']}"');
          }

          buffer.writeln('      frequency: "${itemMap['frequency']}"');
          buffer.writeln('      interval: ${itemMap['interval']}');

          if (itemMap.containsKey('day_of_month')) {
            buffer.writeln('      day_of_month: ${itemMap['day_of_month']}');
          }
          if (itemMap.containsKey('day_of_week')) {
            buffer.writeln('      day_of_week: ${itemMap['day_of_week']}');
          }
          if (itemMap.containsKey('month_of_year')) {
            buffer.writeln('      month_of_year: ${itemMap['month_of_year']}');
          }

          buffer.writeln('      start_date: "${itemMap['start_date']}"');
          if (itemMap.containsKey('end_date') && itemMap['end_date'] != null) {
            buffer.writeln('      end_date: "${itemMap['end_date']}"');
          }
          buffer.writeln('      enabled: ${itemMap['enabled']}');
          if (itemMap.containsKey('currency_code') &&
              itemMap['currency_code'] != null) {
            buffer.writeln('      currency_code: "${itemMap['currency_code']}"');
          }
        }
      }
      buffer.writeln();
    }

    // 账户
    if (yamlMap.containsKey('accounts')) {
      buffer.writeln('# 账户');
      buffer.writeln('accounts:');
      final accounts = yamlMap['accounts'] as Map<String, dynamic>;
      final items = accounts['items'] as List;

      if (items.isNotEmpty) {
        buffer.writeln('  items:');
        for (final item in items) {
          final itemMap = item as Map<String, dynamic>;
          buffer.writeln('    - name: "${itemMap['name']}"');
          buffer.writeln('      type: "${itemMap['type']}"');
          buffer.writeln('      currency: "${itemMap['currency']}"');
          buffer.writeln('      initial_balance: ${itemMap['initial_balance']}');
          if (itemMap.containsKey('created_at') && itemMap['created_at'] != null) {
            buffer.writeln('      created_at: "${itemMap['created_at']}"');
          }
          if (itemMap.containsKey('credit_limit') && itemMap['credit_limit'] != null) {
            buffer.writeln('      credit_limit: ${itemMap['credit_limit']}');
          }
          if (itemMap.containsKey('billing_day') && itemMap['billing_day'] != null) {
            buffer.writeln('      billing_day: ${itemMap['billing_day']}');
          }
          if (itemMap.containsKey('payment_due_day') && itemMap['payment_due_day'] != null) {
            buffer.writeln('      payment_due_day: ${itemMap['payment_due_day']}');
          }
          if (itemMap.containsKey('bank_name') && itemMap['bank_name'] != null) {
            buffer.writeln('      bank_name: "${itemMap['bank_name']}"');
          }
          if (itemMap.containsKey('card_last_four') && itemMap['card_last_four'] != null) {
            buffer.writeln('      card_last_four: "${itemMap['card_last_four']}"');
          }
          if (itemMap.containsKey('note') && itemMap['note'] != null) {
            buffer.writeln('      note: "${itemMap['note']}"');
          }
        }
      }
      buffer.writeln();
    }

    // 分类
    if (yamlMap.containsKey('categories')) {
      buffer.writeln('# 分类');
      buffer.writeln('categories:');
      final categories = yamlMap['categories'] as Map<String, dynamic>;
      final items = categories['items'] as List;

      if (items.isNotEmpty) {
        buffer.writeln('  items:');
        for (final item in items) {
          final itemMap = item as Map<String, dynamic>;
          buffer.writeln('    - name: "${itemMap['name']}"');
          buffer.writeln('      kind: "${itemMap['kind']}"');
          if (itemMap.containsKey('icon') && itemMap['icon'] != null) {
            buffer.writeln('      icon: "${itemMap['icon']}"');
          }
          buffer.writeln('      sort_order: ${itemMap['sort_order']}');
          if (itemMap.containsKey('parent_name') && itemMap['parent_name'] != null) {
            buffer.writeln('      parent_name: "${itemMap['parent_name']}"');
          }
          buffer.writeln('      level: ${itemMap['level']}');
          // 自定义图标字段
          if (itemMap.containsKey('icon_type') && itemMap['icon_type'] != null) {
            buffer.writeln('      icon_type: "${itemMap['icon_type']}"');
          }
          if (itemMap.containsKey('custom_icon_path') && itemMap['custom_icon_path'] != null) {
            buffer.writeln('      custom_icon_path: "${itemMap['custom_icon_path']}"');
          }
          if (itemMap.containsKey('community_icon_id') && itemMap['community_icon_id'] != null) {
            buffer.writeln('      community_icon_id: "${itemMap['community_icon_id']}"');
          }
        }
      }
      buffer.writeln();
    }

    // 标签
    if (yamlMap.containsKey('tags')) {
      buffer.writeln('# 标签');
      buffer.writeln('tags:');
      final tags = yamlMap['tags'] as Map<String, dynamic>;
      final items = tags['items'] as List;

      if (items.isNotEmpty) {
        buffer.writeln('  items:');
        for (final item in items) {
          final itemMap = item as Map<String, dynamic>;
          buffer.writeln('    - name: "${itemMap['name']}"');
          if (itemMap.containsKey('color') && itemMap['color'] != null) {
            buffer.writeln('      color: "${itemMap['color']}"');
          }
        }
      }
      buffer.writeln();
    }

    // 预算
    if (yamlMap.containsKey('budgets')) {
      buffer.writeln('# 预算');
      buffer.writeln('budgets:');
      final budgets = yamlMap['budgets'] as Map<String, dynamic>;
      final items = budgets['items'] as List;

      if (items.isNotEmpty) {
        buffer.writeln('  items:');
        for (final item in items) {
          final itemMap = item as Map<String, dynamic>;
          buffer.writeln('    - ledger_name: "${itemMap['ledger_name']}"');
          buffer.writeln('      type: "${itemMap['type']}"');
          if (itemMap.containsKey('category_name') && itemMap['category_name'] != null) {
            buffer.writeln('      category_name: "${itemMap['category_name']}"');
          }
          buffer.writeln('      amount: ${itemMap['amount']}');
          buffer.writeln('      start_day: ${itemMap['start_day']}');
        }
      }
      buffer.writeln();
    }

    return buffer.toString();
  }

  /// 从YAML字符串导入配置
  /// [yamlContent] YAML内容
  /// [repository] 数据仓库实例，用于导入周期账单等数据
  /// [ledgerId] 账本ID，用于导入周期账单到指定账本
  /// [options] 导入选项，控制导入哪些内容
  static Future<void> importFromYaml(
    String yamlContent, {
    BaseRepository? repository,
    int? ledgerId,
    ExportOptions options = ExportOptions.all,
  }) async {
    final doc = loadYaml(yamlContent);

    if (doc is! Map) {
      throw FormatException(resolveAppLocalizations(null).configExportInvalidYaml);
    }

    final config = AppConfig.fromYaml(doc);
    final prefs = await SharedPreferences.getInstance();

    // 导入应用设置（除了默认账户，稍后处理）
    String? pendingDefaultIncomeAccountName;
    String? pendingDefaultExpenseAccountName;

    if (options.appSettings && config.appSettings != null) {
      final settings = config.appSettings!;

      // 账户管理
      if (settings.accountFeatureEnabled != null) {
        await prefs.setBool('account_feature_enabled', settings.accountFeatureEnabled!);
      }
      // 默认账户通过名称查找ID（需要先导入账户再处理此配置）
      pendingDefaultIncomeAccountName = settings.defaultIncomeAccountName;
      pendingDefaultExpenseAccountName = settings.defaultExpenseAccountName;

      // 记账提醒
      if (settings.reminderEnabled != null) {
        await prefs.setBool('reminder_enabled', settings.reminderEnabled!);
      }
      if (settings.reminderHour != null) {
        await prefs.setInt('reminder_hour', settings.reminderHour!);
      }
      if (settings.reminderMinute != null) {
        await prefs.setInt('reminder_minute', settings.reminderMinute!);
      }

      // 语言设置
      if (settings.languageCode != null) {
        await prefs.setString('selected_language', settings.languageCode!);
      }
      if (settings.countryCode != null) {
        await prefs.setString('selected_language_country', settings.countryCode!);
      }

      // 个性化设置
      if (settings.primaryColor != null) {
        await prefs.setInt('primaryColor', settings.primaryColor!);
      }
      if (settings.fontScaleLevel != null) {
        await prefs.setInt('fontScaleLevel', settings.fontScaleLevel!);
      }
      if (settings.customFontScale != null) {
        await prefs.setDouble('customFontScale', settings.customFontScale!);
      }

      // 外观设置
      if (settings.themeMode != null) {
        await prefs.setString('themeMode', settings.themeMode!);
      }
      if (settings.compactAmount != null) {
        await prefs.setBool('compactAmount', settings.compactAmount!);
      }
      if (settings.showTransactionTime != null) {
        await prefs.setBool(
            'showTransactionTime', settings.showTransactionTime!);
      }
      if (settings.noteDisplayMode != null) {
        await prefs.setString('noteDisplayMode', settings.noteDisplayMode!);
      }
      if (settings.noteHistoryScope != null) {
        await prefs.setString('noteHistoryScope', settings.noteHistoryScope!);
      }
      if (settings.noteHistorySort != null) {
        await prefs.setString('noteHistorySort', settings.noteHistorySort!);
      }
      if (settings.noteHistoryLimit != null &&
          settings.noteHistoryLimit! >= 1 &&
          settings.noteHistoryLimit! <= 100) {
        await prefs.setInt('noteHistoryLimit', settings.noteHistoryLimit!);
      }
      if (settings.incomeExpenseColorScheme != null) {
        await prefs.setBool(
            'incomeExpenseColorScheme', settings.incomeExpenseColorScheme!);
      }

      // 云服务

      // 自动记账

      logger.info('ConfigImport', '应用设置已导入（默认账户待处理）');
    }

    // === 按依赖顺序导入数据 ===
    // 1. 导入账本（周期账单、预算依赖账本）
    if (options.ledgers && config.ledgers != null && repository != null) {
      try {
        final items = config.ledgers!.items;

        // 获取现有账本名称集合
        final existingLedgers = await repository.getAllLedgers();
        final existingNames = existingLedgers.map((l) => l.name.toLowerCase()).toSet();

        // 过滤掉已存在的账本（按名称去重）
        final newItems = items.where((item) =>
          !existingNames.contains(item.name.toLowerCase())
        ).toList();

        if (newItems.isNotEmpty) {
          for (final item in newItems) {
            await repository.createLedger(
              name: item.name,
              currency: item.currency,
            );
          }
          logger.info('ConfigImport', '账本已导入: ${newItems.length}条 (跳过已存在: ${items.length - newItems.length}条)');
        } else {
          logger.info('ConfigImport', '账本全部已存在，跳过导入');
        }
      } catch (e) {
        logger.error('ConfigImport', '导入账本失败: $e');
      }
    }

    // 2. 导入分类（周期账单、预算依赖分类）
    if (options.categories && config.categories != null && repository != null) {
      try {
        final items = config.categories!.items;

        // 获取现有分类名称集合（用于去重）
        final existingCategories = await repository.getAllCategories();
        // 按 (name, kind) 去重,允许跨 kind 同名(收入/支出可同名)
        final existingKeys =
            existingCategories.map((c) => '${c.name.toLowerCase()}|${c.kind}').toSet();

        // 特殊处理：更新虚拟转账分类（如果存在）
        final transferItem = items.firstWhere(
          (item) => item.kind == 'transfer',
          orElse: () => CategoryItem(name: '', kind: '', sortOrder: 0, level: 1),
        );
        if (transferItem.name.isNotEmpty) {
          try {
            final existingTransfer = existingCategories.firstWhere(
              (c) => c.kind == 'transfer',
              orElse: () => throw Exception(
                  resolveAppLocalizations(null).configExportTransferCategoryMissing),
            );
            // 更新现有转账分类的图标设置
            await repository.updateCategoryIcon(
              existingTransfer.id,
              iconType: transferItem.iconType ?? 'material',
              icon: transferItem.icon,
              customIconPath: transferItem.customIconPath,
              communityIconId: transferItem.communityIconId,
            );
            logger.info('ConfigImport', '转账分类图标已更新');
          } catch (e) {
            // 转账分类不存在，将在后续插入逻辑中创建
            logger.debug('ConfigImport', '转账分类不存在，将创建: $e');
          }
        }

        // 第一步：过滤并批量插入一级分类
        final level1Items = items.where((item) => item.parentName == null).toList();
        final newLevel1Items = level1Items.where((item) =>
          !existingKeys.contains('${item.name.toLowerCase()}|${item.kind}')
        ).toList();

        if (newLevel1Items.isNotEmpty) {
          final level1Companions = newLevel1Items.map((item) => CategoriesCompanion.insert(
            name: item.name,
            kind: item.kind,
            icon: d.Value(item.icon),
            sortOrder: d.Value(item.sortOrder),
            parentId: const d.Value(null),
            level: d.Value(item.level),
            iconType: d.Value(item.iconType ?? 'material'),
            customIconPath: d.Value(item.customIconPath),
            communityIconId: d.Value(item.communityIconId),
          )).toList();

          await repository.batchInsertCategories(level1Companions);
        }

        // 第二步：查询所有分类，构建名称到ID的映射
        final allCategories = await repository.getAllCategories();
        final keyToId = <String, int>{
          for (var cat in allCategories) '${cat.name.toLowerCase()}|${cat.kind}': cat.id
        };

        // 更新现有分类集合（包含刚插入的一级分类），按 (name, kind)
        final updatedKeys = allCategories.map((c) => '${c.name.toLowerCase()}|${c.kind}').toSet();

        // 第三步：过滤并批量插入二级分类
        final level2Items = items.where((item) => item.parentName != null).toList();
        final newLevel2Items = level2Items.where((item) =>
          !updatedKeys.contains('${item.name.toLowerCase()}|${item.kind}')
        ).toList();
        final level2Companions = <CategoriesCompanion>[];

        for (final item in newLevel2Items) {
          // 父分类与子分类同 kind,按 (parentName, kind) 查父 id
          final parentId = keyToId['${item.parentName?.toLowerCase()}|${item.kind}'];
          if (parentId != null) {
            level2Companions.add(CategoriesCompanion.insert(
              name: item.name,
              kind: item.kind,
              icon: d.Value(item.icon),
              sortOrder: d.Value(item.sortOrder),
              parentId: d.Value(parentId),
              level: d.Value(item.level),
              iconType: d.Value(item.iconType ?? 'material'),
              customIconPath: d.Value(item.customIconPath),
              communityIconId: d.Value(item.communityIconId),
            ));
          } else {
            logger.warning('ConfigImport', '找不到父分类 "${item.parentName}"，跳过二级分类: ${item.name}');
          }
        }

        if (level2Companions.isNotEmpty) {
          await repository.batchInsertCategories(level2Companions);
        }

        final skippedCount = (level1Items.length - newLevel1Items.length) +
                             (level2Items.length - newLevel2Items.length);
        logger.info('ConfigImport',
          '分类已批量导入: 一级${newLevel1Items.length}条, 二级${level2Companions.length}条'
          '${skippedCount > 0 ? ' (跳过已存在: $skippedCount条)' : ''}');
      } catch (e) {
        logger.error('ConfigImport', '导入分类失败: $e');
      }
    }

    // 3. 导入账户（周期账单、默认账户依赖账户）
    if (options.accounts && config.accounts != null && repository != null) {
      try {
        final items = config.accounts!.items;

        // 获取现有账户名称集合
        final existingAccounts = await repository.getAllAccounts();
        final existingNames = existingAccounts.map((a) => a.name.toLowerCase()).toSet();

        // 过滤掉已存在的账户（按名称去重）
        final newItems = items.where((item) =>
          !existingNames.contains(item.name.toLowerCase())
        ).toList();

        if (newItems.isNotEmpty) {
          // 准备批量插入的数据
          final accountsToInsert = newItems.map((item) => AccountsCompanion.insert(
            ledgerId: 0, // 保留字段，但不再使用（v2迁移后会移除）
            name: item.name,
            type: d.Value(item.type),
            currency: d.Value(item.currency),
            initialBalance: d.Value(item.initialBalance),
            createdAt: d.Value(
                item.createdAt != null ? DateTime.parse(item.createdAt!) : null),
            updatedAt: d.Value(DateTime.now()),
            creditLimit: d.Value(item.creditLimit),
            billingDay: d.Value(item.billingDay),
            paymentDueDay: d.Value(item.paymentDueDay),
            bankName: d.Value(item.bankName),
            cardLastFour: d.Value(item.cardLastFour),
            note: d.Value(item.note),
          )).toList();

          // 使用 repository 方法进行批量插入
          await repository.batchInsertAccounts(accountsToInsert);

          logger.info('ConfigImport', '账户已导入: ${newItems.length}条 (跳过已存在: ${items.length - newItems.length}条)');
        } else {
          logger.info('ConfigImport', '账户全部已存在，跳过导入');
        }
      } catch (e) {
        logger.error('ConfigImport', '导入账户失败: $e');
      }
    }

    // 4. 导入标签
    if (options.tags && config.tags != null && repository != null) {
      try {
        final items = config.tags!.items;

        // 获取现有标签名称集合
        final existingTags = await repository.getAllTags();
        final existingNames = existingTags.map((t) => t.name.toLowerCase()).toSet();

        // 过滤掉已存在的标签（按名称去重）
        final newItems = items.where((item) =>
          !existingNames.contains(item.name.toLowerCase())
        ).toList();

        if (newItems.isNotEmpty) {
          // 准备批量插入的数据
          final tagsToInsert = newItems.map((item) => TagsCompanion.insert(
            name: item.name,
            color: d.Value(item.color),
          )).toList();

          // 使用 repository 方法进行批量插入
          await repository.batchInsertTags(tagsToInsert);

          logger.info('ConfigImport', '标签已导入: ${newItems.length}条 (跳过已存在: ${items.length - newItems.length}条)');
        } else {
          logger.info('ConfigImport', '标签全部已存在，跳过导入');
        }
      } catch (e) {
        logger.error('ConfigImport', '导入标签失败: $e');
      }
    }

    // 5. 导入周期账单（依赖账本、分类、账户）
    if (options.recurringTransactions && config.recurringTransactions != null && repository != null) {
      try {
        final items = config.recurringTransactions!.items;

        // 构建名称到ID的映射
        final ledgers = await repository.getAllLedgers();
        final ledgerNameToId = {for (var l in ledgers) l.name: l.id};

        final categories = await repository.getAllCategories();
        // 按 (name, kind) 映射,跨 kind 同名各自命中
        final catKeyToId = {for (var c in categories) '${c.name.toLowerCase()}|${c.kind}': c.id};

        final accounts = await repository.getAllAccounts();
        final accountNameToId = {for (var a in accounts) a.name: a.id};

        int importedCount = 0;
        int skippedCount = 0;

        for (final item in items) {
          // 通过名称查找账本ID
          final targetLedgerId = ledgerNameToId[item.ledgerName];
          if (targetLedgerId == null) {
            logger.warning('ConfigImport', '找不到账本: ${item.ledgerName}，跳过周期账单');
            skippedCount++;
            continue;
          }

          // 通过名称查找分类ID
          int? categoryId;
          if (item.categoryName != null) {
            // 周期账单 type(expense/income/transfer)即分类 kind
            categoryId = catKeyToId['${item.categoryName!.toLowerCase()}|${item.type}'];
            if (categoryId == null) {
              logger.warning('ConfigImport', '找不到分类: ${item.categoryName}，跳过周期账单');
              skippedCount++;
              continue;
            }
          }

          // 通过名称查找账户ID
          int? accountId;
          if (item.accountName != null) {
            accountId = accountNameToId[item.accountName];
            if (accountId == null) {
              logger.warning('ConfigImport', '找不到账户: ${item.accountName}，跳过周期账单');
              skippedCount++;
              continue;
            }
          }

          // 通过名称查找转账目标账户ID
          int? toAccountId;
          if (item.toAccountName != null) {
            toAccountId = accountNameToId[item.toAccountName];
            if (toAccountId == null) {
              logger.warning('ConfigImport', '找不到转账目标账户: ${item.toAccountName}，跳过周期账单');
              skippedCount++;
              continue;
            }
          }

          await repository.addRecurringTransaction(
            ledgerId: targetLedgerId,
            type: item.type,
            amount: item.amount,
            categoryId: categoryId,
            accountId: accountId,
            toAccountId: toAccountId,
            note: item.note,
            frequency: item.frequency,
            interval: item.interval,
            dayOfMonth: item.dayOfMonth,
            dayOfWeek: item.dayOfWeek,
            monthOfYear: item.monthOfYear,
            startDate: DateTime.parse(item.startDate),
            endDate: item.endDate != null ? DateTime.parse(item.endDate!) : null,
            enabled: item.enabled,
            currencyCode: item.currencyCode,
          );
          importedCount++;
        }

        logger.info('ConfigImport', '周期账单已导入: $importedCount条${skippedCount > 0 ? '，跳过: $skippedCount条' : ''}');
      } catch (e) {
        logger.error('ConfigImport', '导入周期账单失败: $e');
      }
    }

    // 6. 导入预算（依赖账本、分类）
    if (options.budgets && config.budgets != null && repository != null) {
      try {
        final items = config.budgets!.items;
        int importedCount = 0;
        int skippedCount = 0;

        // 构建名称到 ID 的映射
        final ledgers = await repository.getAllLedgers();
        final ledgerNameToId = {for (var l in ledgers) l.name: l.id};

        final categories = await repository.getAllCategories();
        // 分类预算只针对支出一级分类,按 (name, kind) 映射
        final catKeyToId = {for (var c in categories) '${c.name.toLowerCase()}|${c.kind}': c.id};

        for (final item in items) {
          // 通过名称查找账本 ID
          int? targetLedgerId = ledgerId; // 优先使用指定的账本
          if (targetLedgerId == null) {
            targetLedgerId = ledgerNameToId[item.ledgerName];
            if (targetLedgerId == null) {
              logger.warning('ConfigImport', '找不到账本: ${item.ledgerName}，跳过此预算');
              skippedCount++;
              continue;
            }
          }

          // 通过名称查找分类 ID（仅分类预算需要）
          int? categoryId;
          if (item.type == 'category' && item.categoryName != null) {
            // 分类预算针对支出分类
            categoryId = catKeyToId['${item.categoryName!.toLowerCase()}|expense'];
            if (categoryId == null) {
              logger.warning('ConfigImport', '找不到分类: ${item.categoryName}，跳过此预算');
              skippedCount++;
              continue;
            }
          }

          await repository.createBudget(
            ledgerId: targetLedgerId,
            type: item.type,
            categoryId: categoryId,
            amount: item.amount,
            startDay: item.startDay,
          );
          importedCount++;
        }

        logger.info('ConfigImport', '预算已导入: $importedCount条${skippedCount > 0 ? '，跳过: $skippedCount条' : ''}');
      } catch (e) {
        logger.error('ConfigImport', '导入预算失败: $e');
      }
    }

    // 7. 处理默认账户设置（所有数据导入完成后）
    if (repository != null && (pendingDefaultIncomeAccountName != null || pendingDefaultExpenseAccountName != null)) {
      try {
        final accounts = await repository.getAllAccounts();
        final accountNameToId = {for (var a in accounts) a.name: a.id};

        if (pendingDefaultIncomeAccountName != null) {
          final accountId = accountNameToId[pendingDefaultIncomeAccountName];
          if (accountId != null) {
            await prefs.setInt('default_income_account_id', accountId);
            logger.info('ConfigImport', '默认收入账户已设置: $pendingDefaultIncomeAccountName');
          } else {
            logger.warning('ConfigImport', '找不到默认收入账户: $pendingDefaultIncomeAccountName');
          }
        }

        if (pendingDefaultExpenseAccountName != null) {
          final accountId = accountNameToId[pendingDefaultExpenseAccountName];
          if (accountId != null) {
            await prefs.setInt('default_expense_account_id', accountId);
            logger.info('ConfigImport', '默认支出账户已设置: $pendingDefaultExpenseAccountName');
          } else {
            logger.warning('ConfigImport', '找不到默认支出账户: $pendingDefaultExpenseAccountName');
          }
        }
      } catch (e) {
        logger.error('ConfigImport', '处理默认账户设置失败: $e');
      }
    }
  }

  /// 导出配置到文件
  static Future<void> exportToFile(
    String filePath, {
    BaseRepository? repository,
    int? ledgerId,
    ExportOptions options = ExportOptions.all,
  }) async {
    final yamlContent = await exportToYaml(
      repository: repository,
      ledgerId: ledgerId,
      options: options,
    );
    final file = File(filePath);
    await file.writeAsString(yamlContent);
    logger.info('ConfigExport', '配置已导出到: $filePath');
  }

  /// 从文件导入配置
  static Future<void> importFromFile(
    String filePath, {
    BaseRepository? repository,
    int? ledgerId,
    ExportOptions options = ExportOptions.all,
  }) async {
    final file = File(filePath);
    if (!await file.exists()) {
      throw Exception(resolveAppLocalizations(null)
          .configExportFileMissing(filePath));
    }

    final yamlContent = await file.readAsString();
    await importFromYaml(yamlContent, repository: repository, ledgerId: ledgerId, options: options);
    logger.info('ConfigImport', '配置已从文件导入: $filePath');
  }
}
