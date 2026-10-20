import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../ai/providers/ai_provider_config.dart';
import '../ai/providers/ai_provider_manager.dart';

/// AI 执行策略
enum AIStrategy {
  /// 本地优先
  localFirst,

  /// 云端优先
  cloudFirst,

  /// 仅本地
  localOnly,

  /// 仅云端
  cloudOnly,
}

/// AI 全局配置数据类
///
/// 只管理全局开关和策略设置，服务商配置由 AIProviderManager 管理
class AIConfigData {
  /// AI 增强是否启用
  final bool enabled;

  /// 是否使用视觉模型（上传图片）
  final bool useVision;

  /// 执行策略
  final AIStrategy strategy;

  const AIConfigData({
    this.enabled = false,
    this.useVision = false,
    this.strategy = AIStrategy.localOnly,
  });

  /// 复制并修改
  AIConfigData copyWith({
    bool? enabled,
    bool? useVision,
    AIStrategy? strategy,
  }) {
    return AIConfigData(
      enabled: enabled ?? this.enabled,
      useVision: useVision ?? this.useVision,
      strategy: strategy ?? this.strategy,
    );
  }
}

/// AI 全局配置 Notifier
///
/// 只管理全局开关和策略设置，服务商配置由 AIProviderManager 管理
class AIConfigNotifier extends StateNotifier<AIConfigData> {
  /// 加载完成标志
  final Completer<void> _loadCompleter = Completer<void>();

  AIConfigNotifier() : super(const AIConfigData()) {
    _loadFromPrefs();
  }

  /// 确保配置已加载完成
  ///
  /// 在需要读取配置前调用此方法，确保异步加载已完成
  Future<void> ensureLoaded() => _loadCompleter.future;

  Future<void> _loadFromPrefs() async {
    if (mounted) state = const AIConfigData();
    if (!_loadCompleter.isCompleted) _loadCompleter.complete();
  }

  Future<void> setStrategy(AIStrategy strategy) async {}
  Future<void> setEnabled(bool enabled) async {}
  Future<void> setUseVision(bool useVision) async {}
  Future<void> reload() => _loadFromPrefs();
}

/// AI 全局配置 Provider
final aiConfigProvider =
    StateNotifierProvider<AIConfigNotifier, AIConfigData>((ref) {
  return AIConfigNotifier();
});

/// AI 是否启用 Provider（简化访问）
final aiEnabledProvider = Provider<bool>((ref) {
  return ref.watch(aiConfigProvider).enabled;
});

// ============================================================
// 新版多服务商架构 Providers
// ============================================================

/// 能力绑定刷新 Provider
final aiCapabilityBindingRefreshProvider = StateProvider<int>((ref) => 0);

/// 能力绑定数据 Provider
final aiCapabilityBindingProvider = FutureProvider<AICapabilityBinding>((ref) async {
  ref.watch(aiCapabilityBindingRefreshProvider);
  return AIProviderManager.getCapabilityBinding();
});

/// 服务商列表刷新 Provider (供能力选择使用)
final aiProviderListForCapabilityRefreshProvider = StateProvider<int>((ref) => 0);

/// 服务商列表 Provider (供能力选择使用)
final aiProviderListForCapabilityProvider = FutureProvider<List<AIServiceProviderConfig>>((ref) async {
  ref.watch(aiProviderListForCapabilityRefreshProvider);
  return AIProviderManager.getProviders();
});
