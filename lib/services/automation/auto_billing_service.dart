import 'dart:async';
import 'dart:io';
import 'dart:ui';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../ai/core/prompt_builder.dart';
import '../../ai/providers/ai_provider_config.dart';
import '../../utils/app_localizations_resolver.dart';
import '../../ai/providers/ai_provider_manager.dart';
import '../../l10n/app_localizations.dart';
import '../../providers.dart';
import '../../providers/ai_chat_providers.dart';
import '../ai/bookkeeping_result.dart';
import '../attachment_service.dart';
import '../billing/post_processor.dart';
import '../data/tag_seed_service.dart';
import '../system/logger_service.dart';
import 'auto_billing_config.dart';

/// 自动记账服务 - 通用核心逻辑
/// Android和iOS共用的OCR识别和自动记账逻辑
class AutoBillingService {
  static const _ledgerIdKey = 'current_ledger_id';
  static const _processedScreenshotsKey = 'processed_screenshots';

  final ProviderContainer _container;
  final FlutterLocalNotificationsPlugin _notificationsPlugin =
      FlutterLocalNotificationsPlugin();

  // 防重复处理
  final Set<String> _processedPaths = {};
  String? _lastProcessedPath;
  int _lastProcessedTime = 0;

  AutoBillingService(this._container) {
    _initNotifications();
    _loadProcessedScreenshots();
  }

  /// 解析当前账本 ID(Provider → SharedPreferences → 数据库默认)。
  Future<int?> _resolveLedgerId() async {
    try {
      final id = _container.read(currentLedgerIdProvider);
      return id;
    } catch (_) {}
    final prefs = await SharedPreferences.getInstance();
    final fromPrefs = prefs.getInt(_ledgerIdKey);
    if (fromPrefs != null) return fromPrefs;
    final repo = _container.read(repositoryProvider);
    final ledgers = await repo.getAllLedgers();
    if (ledgers.isEmpty) return null;
    final fallback = ledgers.first.id;
    await prefs.setInt(_ledgerIdKey, fallback);
    return fallback;
  }

  /// 初始化通知
  Future<void> _initNotifications() async {
    const androidSettings =
        AndroidInitializationSettings('@mipmap/ic_launcher');
    const iosSettings = DarwinInitializationSettings();

    const initSettings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );

    // 同 _showNotification:通知子系统任何异常都不允许影响记账主流程
    try {
      await _notificationsPlugin.initialize(initSettings);
    } catch (e) {
      logger.warning('AutoBilling', '通知初始化失败(仅影响进度通知,不影响记账): $e');
    }
  }

  /// 加载已处理的截图列表
  Future<void> _loadProcessedScreenshots() async {
    final prefs = await SharedPreferences.getInstance();
    final list = prefs.getStringList(_processedScreenshotsKey) ?? [];
    _processedPaths.addAll(list);

    // 只保留最近N个，避免内存占用过大
    if (_processedPaths.length > AutoBillingConfig.maxProcessedCache) {
      final toRemove =
          _processedPaths.length - AutoBillingConfig.maxProcessedCache;
      _processedPaths.removeAll(_processedPaths.take(toRemove));
      await _saveProcessedScreenshots();
      logger.debug('AutoBilling', '清理已处理缓存',
          '移除=$toRemove, 保留=${AutoBillingConfig.maxProcessedCache}');
    }
  }

  /// 保存已处理的截图列表
  Future<void> _saveProcessedScreenshots() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setStringList(
        _processedScreenshotsKey, _processedPaths.toList());
  }

  /// 标记截图已处理
  Future<void> _markAsProcessed(String path) async {
    _processedPaths.add(path);
    await _saveProcessedScreenshots();
  }

  /// 检查截图是否已处理
  bool _isProcessed(String path) {
    return _processedPaths.contains(path);
  }

  /// 核心：处理截图并自动记账
  /// [imagePath] 截图文件路径
  /// [recognitionImagePath] 可选的识别副本路径，附件仍保存 [imagePath]。
  /// [showNotification] 是否显示通知（默认true）
  /// 返回：交易记录ID，失败返回null
  Future<int?> processScreenshot(
    String imagePath, {
    String? recognitionImagePath,
    bool showNotification = true,
  }) async {
    return null;
  }

  /// 核心：直接处理文本并自动记账(快捷指令推荐方式)
  /// [text] 快捷指令传递的识别文本
  /// [showNotification] 是否显示通知（默认true）
  /// 返回：交易记录ID，失败返回null
  Future<int?> processText(
    String text, {
    bool showNotification = true,
  }) async {
    return null;
  }

  /// 通知标题统一格式
  String _successTitle(BookkeepingResult result, AppLocalizations l10n) {
    if (result.isMulti) {
      return l10n.autoBillingNotifySuccessMultiTitle(result.savedCount);
    }
    return l10n.autoBillingNotifySuccessSingleTitle(
        result.totalAbsAmount.toStringAsFixed(2));
  }

  /// 通知正文统一格式
  String _successBody(BookkeepingResult result, AppLocalizations l10n) {
    if (result.isMulti) {
      return l10n.autoBillingNotifySuccessMultiBody(
          result.totalAbsAmount.toStringAsFixed(2));
    }
    final note = result.firstBill?.note;
    return (note != null && note.isNotEmpty)
        ? l10n.autoBillingNotifySuccessSingleBodyNote(note)
        : l10n.autoBillingNotifySuccessSingleBodyDefault;
  }

  /// 显示通知。
  ///
  /// 通知失败**绝不向外抛**:通知只是进度提示,记账主流程不能因它中断。
  /// iOS 27 起对未授权通知的应用调 show() 会抛 PlatformException(Error 2003,
  /// "Source is not authorized"),而 iOS ≤26 同场景是静默不弹 —— 不隔离的话
  /// 截图还没进 AI 识别就在"开始识别"通知处整链失败(#322)。
  Future<void> _showNotification({
    required int id,
    required String title,
    required String body,
  }) async {
    final l10n = resolveAppLocalizations(null);
    final androidDetails = AndroidNotificationDetails(
      'screenshot_ocr',
      l10n.autoBillingChannelName,
      channelDescription: l10n.autoBillingChannelDescription,
      importance: Importance.high,
      priority: Priority.high,
    );

    const iosDetails = DarwinNotificationDetails();

    final details = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    try {
      await _notificationsPlugin.show(id, title, body, details);
    } catch (e) {
      logger.warning('AutoBilling',
          '通知发送失败(未授权通知时属预期,不中断记账流程): $e');
    }
  }

  /// 显示「最终结果」通知。
  ///
  /// iOS 上,**用同一 ID 重复 `show()` 只会静默更新通知中心条目,不会重新弹
  /// banner**。所以「正在识别 → 成功/失败」如果共用 ID,用户只能看到第一条
  /// banner,直到进通知中心才看到结果。
  ///
  /// 这个方法用**新 ID** 发结果通知,iOS 把它当作新通知重新弹 banner。
  /// 不 cancel 旧的「正在识别」—— 实测在 AppIntent background-launch 状态下
  /// cancel + show 紧挨着的组合 iOS 会把它当成一次「替换」处理,banner 不弹;
  /// 留着旧的反而能保证新的作为独立通知正常弹出(旧的在结果通知出现后用户可自
  /// 行清理或自然过期)。
  Future<void> _showFinalNotification({
    required int progressId,
    required int finalId,
    required String title,
    required String body,
  }) async {
    await _showNotification(id: finalId, title: title, body: body);
  }

  /// 释放资源(AI 服务无 native handle,不需要 dispose,保留方法以备后续添加)
  void dispose() {}
}
