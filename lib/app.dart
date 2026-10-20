import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'pages/main/home_page.dart';
import 'pages/main/analytics_page.dart';
import 'pages/account/accounts_page.dart';
import 'pages/budget/budget_page.dart';
import 'pages/main/mine_page.dart';
import 'pages/transaction/transaction_editor_page.dart';
import 'providers.dart';
import 'l10n/app_localizations.dart';
import 'widget/widget_manager.dart';
import 'widgets/ui/ui.dart';
import 'widgets/ui/main_navigation_scaffold.dart';
import 'services/platform/app_link_service.dart';
import 'services/system/logger_service.dart';
import 'services/security/app_lock_service.dart';
import 'providers/security_providers.dart';

class BeeApp extends ConsumerStatefulWidget {
  const BeeApp({super.key});

  @override
  ConsumerState<BeeApp> createState() => _BeeAppState();
}

class _BeeAppState extends ConsumerState<BeeApp>
    with WidgetsBindingObserver {
  final _pages = const [
    HomePage(),
    AnalyticsPage(),
    AccountsPage(asTab: true),
    MinePage(),
  ];

  // 双击检测：记录最后一次点击的时间和索引
  DateTime? _lastTapTime;
  int? _lastTappedIndex;

  // 双击返回退出：记录最后一次返回键按下时间
  DateTime? _lastBackPressTime;

  // AppLink 监听订阅
  ProviderSubscription<AppLinkAction?>? _appLinkSubscription;


  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);

    // 延迟监听 AppLink，确保 context 可用
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) _setupAppLinkListener();
    });
  }

  /// 设置 AppLink 监听
  void _setupAppLinkListener() {
    logger.info('AppLink', 'BeeApp: 设置 AppLink 监听...');
    _appLinkSubscription = ref.listenManual<AppLinkAction?>(
      pendingAppLinkActionProvider,
      (previous, next) {
        logger.info('AppLink',
            'BeeApp: 监听触发 previous=$previous, next=$next, mounted=$mounted');
        if (next != null && mounted) {
          // 不在此处直接 push：冷启动 / 厂商主题变更(themeChanged)会重建页面树,
          // 此刻多半处于 inactive/hidden,push 的路由会被丢弃(deep-link「没打开」根因)。
          // 改为持久化待处理深链,等 ready + 前台 resumed 后在最终页面树上认领打开。
          _persistPendingDeepLink(
            next,
            ref.read(pendingNewTransactionTypeProvider),
            categoryId: ref.read(pendingNewTransactionCategoryIdProvider),
            page: ref.read(pendingOpenPageProvider),
          );
          ref.read(pendingAppLinkActionProvider.notifier).state = null;
          _drainPendingDeepLink(trigger: 'listener');
        }
      },
      fireImmediately: true,
    );
    logger.info('AppLink', 'BeeApp: AppLink 监听已设置');
  }

  // ——— 深链「重建可恢复」打开 ———
  // 背景:部分厂商(如 ColorOS)在浏览器→App 拉起 deep-link 时会触发主题变更
  // (onConfigurationChanged: themeChanged),导致页面树/Activity 重建;若在重建前就
  // push,路由会被丢弃,用户看到「没打开」。做法:把待打开的深链持久化(跨重建/重置
  // 存活),等 appInitState==ready 且生命周期 resumed(前台稳定)后,在最终页面树上认领
  // 打开,认领即清除并去重,确保只打开一次。
  static const String _kPendingDeepLink = 'pending_deeplink_action';
  int? _lastDrainedDeepLinkTs;
  Timer? _drainTimer;

  void _persistPendingDeepLink(
    AppLinkAction action,
    String? type, {
    int? categoryId,
    String? page,
  }) {
    SharedPreferences.getInstance().then((p) {
      p.setString(_kPendingDeepLink, jsonEncode({
        'action': action.name,
        'type': type,
        if (categoryId != null) 'categoryId': categoryId,
        if (page != null) 'page': page,
        'ts': DateTime.now().millisecondsSinceEpoch,
      }));
    }).catchError((_) {});
  }

  void _drainPendingDeepLink({String trigger = ''}) {
    if (!mounted) return;
    if (ref.read(appInitStateProvider) != AppInitState.ready) return;
    if (WidgetsBinding.instance.lifecycleState != AppLifecycleState.resumed) return;
    // 重建有时在 resumed 之后还会再发生一次:延迟一拍再认领,只在「存活过这段缓冲期」的
    // 最终页面树上打开。若本页在缓冲期内被销毁(重建),timer 随 dispose 取消,新页面会
    // 重新排程,自然落到稳定的页面树上。
    _drainTimer?.cancel();
    _drainTimer =
        Timer(const Duration(milliseconds: 700), () => _executeDrain(trigger));
  }

  Future<void> _executeDrain(String trigger) async {
    if (!mounted) return;
    // 必须就绪 + 前台稳定:冷启动/主题变更的重建窗口(inactive/hidden)里打开会被丢弃
    if (ref.read(appInitStateProvider) != AppInitState.ready) return;
    if (WidgetsBinding.instance.lifecycleState != AppLifecycleState.resumed) return;

    SharedPreferences prefs;
    try {
      prefs = await SharedPreferences.getInstance();
    } catch (_) {
      return;
    }
    if (!mounted) return;
    final raw = prefs.getString(_kPendingDeepLink);
    if (raw == null) return;

    Map<String, dynamic> data;
    try {
      data = jsonDecode(raw) as Map<String, dynamic>;
    } catch (_) {
      await prefs.remove(_kPendingDeepLink);
      return;
    }
    final ts = (data['ts'] as num?)?.toInt() ?? 0;
    final ageMs = DateTime.now().millisecondsSinceEpoch - ts;
    // 过期(>20s)或时间异常 → 丢弃,避免历史深链在普通启动时误触发
    if (ageMs < 0 || ageMs > 20000) {
      await prefs.remove(_kPendingDeepLink);
      return;
    }
    // 同一条只认领一次(listener / resumed 多次触发去重)
    if (_lastDrainedDeepLinkTs == ts) return;

    AppLinkAction? action;
    final actionName = data['action'] as String?;
    for (final a in AppLinkAction.values) {
      if (a.name == actionName) {
        action = a;
        break;
      }
    }
    if (action == null) {
      await prefs.remove(_kPendingDeepLink);
      return;
    }

    // 认领成功:打标 + 清持久化,再打开(用根 Navigator,落在最终稳定的页面树上)
    _lastDrainedDeepLinkTs = ts;
    await prefs.remove(_kPendingDeepLink);
    if (!mounted) return;
    final type = data['type'] as String?;
    final categoryId = (data['categoryId'] as num?)?.toInt();
    final page = data['page'] as String?;
    logger.info('AppLink',
        'BeeApp: drain($trigger) 打开深链 $action type=$type categoryId=$categoryId page=$page');
    _openDeepLink(action, type, categoryId: categoryId, page: page);
  }

  /// 本地深链和桌面小组件的导航入口。
  ///
  /// [categoryId] 仅 [AppLinkAction.newTransaction] 使用(小组件「快速记账」
  /// 预填分类);[page] 仅 [AppLinkAction.open] 使用(小组件「净资产/预算/
  /// 最近交易」卡片点击落地页,见 [_openPageForDeepLink])。
  void _openDeepLink(
    AppLinkAction action,
    String? type, {
    int? categoryId,
    String? page,
  }) {
    final nav = Navigator.of(context, rootNavigator: true);
    switch (action) {
      case AppLinkAction.newTransaction:
        // 小组件「快速记账」点分类格携带 categoryId 时,预填该分类(见
        // TransactionEditorPage.initialCategoryId);普通「记一笔」categoryId 为 null。
        nav.push(MaterialPageRoute(
          builder: (_) => TransactionEditorPage(
            initialKind: type ?? 'expense',
            quickAdd: true,
            initialCategoryId: categoryId,
          ),
        ));
        break;
      case AppLinkAction.open:
        _openPageForDeepLink(nav, page);
        break;
      default:
        break;
    }
  }

  /// 小组件「净资产 / 预算 / 最近交易」卡片点击 → `beecount://open?page=` 的
  /// 落地页路由。detail(最近交易)先落统计页,后续可按需换成专门的明细列表页。
  void _openPageForDeepLink(NavigatorState nav, String? page) {
    switch (page) {
      case 'assets':
        nav.push(MaterialPageRoute(builder: (_) => const AccountsPage()));
        break;
      case 'budget':
        nav.push(MaterialPageRoute(builder: (_) => const BudgetPage()));
        break;
      case 'detail':
        // 最近交易 / 仪表盘主体 → 首页明细列表:App 没有独立的明细页,首页
        // 就是完整账单流,切回主壳首页 tab 而不是 push 页面。(此前误映射到
        // 洞察/统计页,真机反馈"点小组件进了洞察页"。)
        nav.popUntil((route) => route.isFirst);
        ref.read(bottomTabIndexProvider.notifier).state = 0;
        break;
      default:
        logger.warning('AppLink', 'open 未知 page: $page');
        break;
    }
  }

  @override
  void dispose() {
    _drainTimer?.cancel();
    _appLinkSubscription?.close();
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);
    if (state == AppLifecycleState.inactive) {
      // 多任务切换时显示隐私模糊屏（仅在应用锁启用时）
      if (ref.read(appLockEnabledProvider)) {
        ref.read(showPrivacyScreenProvider.notifier).state = true;
      }
    } else if (state == AppLifecycleState.paused) {
      // 记录进入后台时间
      AppLockService.recordBackgroundTime();
    } else if (state == AppLifecycleState.resumed) {
      // 移除隐私模糊屏
      ref.read(showPrivacyScreenProvider.notifier).state = false;
      // 检查是否需要锁定
      _checkAppLockOnResume();
      // 当app从后台恢复到前台时，更新小组件数据
      _updateWidget();
      // 前台稳定后认领待处理深链(冷启动/主题变更重建后,在最终页面树上打开)
      _drainPendingDeepLink(trigger: 'resumed');
    }
  }

  @override
  void didChangePlatformBrightness() {
    super.didChangePlatformBrightness();
    // 系统明暗切换时重渲小组件:图片渲染方案不会随系统主题自动重绘(原生壳
    // 只是展示一张静态 PNG),渲染时机全靠 App 侧触发。小组件明暗跟随**系统**
    // 而非 App 内主题设置(行业惯例,桌面是系统的地盘;widget_manager 渲染
    // 批次内取 PlatformDispatcher.platformBrightness),这里监听的正是系统
    // 明暗变化——App 在前台/存活时系统切换明暗,桌面组件立刻换肤,而不是
    // 等下一次记账/前台恢复才刷新。
    _updateWidget();
  }

  Future<void> _checkAppLockOnResume() async {
    final shouldLock = await AppLockService.shouldLockOnResume();
    if (shouldLock && mounted) {
      ref.read(isAppLockedProvider.notifier).state = true;
    }
  }

  Future<void> _updateWidget() async {
    try {
      final repository = ref.read(repositoryProvider);
      final ledgerId = ref.read(currentLedgerIdProvider);
      final primaryColor = ref.read(primaryColorProvider);
      final redForIncome = ref.read(incomeExpenseColorSchemeProvider);
      final baseCurrency = ref.read(baseCurrencyProvider);

      final widgetManager = WidgetManager();
      // 前台恢复路径也走本地化封装,让小组件文案跟随 App 语言(与
      // main.dart / theme_providers 等无 context 调用点一致,靠 languageProvider
      // 还原 locale,不依赖 async 后可能失效的 BuildContext)。
      await widgetManager.updateAllWidgetsLocalized(
        repository,
        ledgerId,
        primaryColor,
        explicitLocale: ref.read(languageProvider),
        redForIncome: redForIncome,
        baseCurrency: baseCurrency,
      );
      logger.info('App', 'App恢复前台，小组件数据已更新');
    } catch (e) {
      logger.warning('App', '更新小组件失败: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    final idx = ref.watch(bottomTabIndexProvider);
    final l10n = AppLocalizations.of(context);
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (bool didPop, Object? result) {
        if (didPop) return;

        final now = DateTime.now();

        if (_lastBackPressTime == null ||
            now.difference(_lastBackPressTime!) > const Duration(seconds: 2)) {
          _lastBackPressTime = now;
          showToast(context, l10n.commonPressAgainToExit);
        } else {
          SystemNavigator.pop();
        }
      },
      child: MainNavigationScaffold(
        pages: _pages,
        selectedIndex: idx,
        onDestinationSelected: _selectTab,
        onRecord: () => Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => const TransactionEditorPage(
              initialKind: 'expense',
              quickAdd: true,
            ),
          ),
        ),
      ),
    );
  }

  void _selectTab(int index) {
    final now = DateTime.now();
    if (_lastTappedIndex == index &&
        _lastTapTime != null &&
        now.difference(_lastTapTime!) < const Duration(milliseconds: 300)) {
      if (index == 0) ref.read(homeScrollToTopProvider.notifier).state++;
      _lastTapTime = null;
      _lastTappedIndex = null;
    } else {
      _lastTapTime = now;
      _lastTappedIndex = index;
      ref.read(bottomTabIndexProvider.notifier).state = index;
    }
  }
}
