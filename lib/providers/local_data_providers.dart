import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/ledger_display_item.dart';
import '../services/system/logger_service.dart';
import 'database_providers.dart';
import 'statistics_providers.dart';
import 'ui_state_providers.dart';

/// 本地数据编辑后的缓存刷新信号。
final localDataRevisionProvider = StateProvider<int>((ref) => 0);
final ledgerListRefreshProvider = StateProvider<int>((ref) => 0);
final ledgerDataRefreshByLedgerProvider = StateProvider.family<int, int>((ref, _) => 0);
final localLedgersProvider =
    FutureProvider<List<LedgerDisplayItem>>((ref) async {
  // 监听刷新触发器（账本列表和统计信息）
  ref.watch(ledgerListRefreshProvider);
  ref.watch(statsRefreshProvider); // 监听统计刷新，确保自动记账后刷新

  try {
    final repo = ref.watch(repositoryProvider);

    // 获取账户功能开启状态
    final accountFeatureEnabled =
        await ref.watch(accountFeatureEnabledProvider.future);

    final localLedgers = await repo.getAllLedgers();

    final result = <LedgerDisplayItem>[];
    for (final ledger in localLedgers) {
      final stats = await repo.getLedgerStats(
        ledgerId: ledger.id,
        accountFeatureEnabled: accountFeatureEnabled,
      );

      result.add(LedgerDisplayItem.fromLocal(
        id: ledger.id,
        name: ledger.name,
        currency: ledger.currency,
        createdAt: ledger.createdAt,
        transactionCount: stats.transactionCount,
        balance: stats.balance,
      ));
    }

    return result;
  } catch (e, stackTrace) {
    logger.error('LocalLedgers', '获取本地账本列表失败', e, stackTrace);
    rethrow;
  }
});

final allLedgersProvider = FutureProvider<List<LedgerDisplayItem>>((ref) async {
  // 监听刷新触发器
  ref.watch(ledgerListRefreshProvider);

  try {
    final repo = ref.watch(repositoryProvider);
    final localLedgers = await repo.getAllLedgers();

    final result = <LedgerDisplayItem>[];
    for (final ledger in localLedgers) {
      final stats = await repo.getLedgerStats(
        ledgerId: ledger.id,
        accountFeatureEnabled: false,
      );

      result.add(LedgerDisplayItem.fromLocal(
        id: ledger.id,
        name: ledger.name,
        currency: ledger.currency,
        createdAt: ledger.createdAt,
        transactionCount: stats.transactionCount,
        balance: stats.balance,
      ));
    }

    return result;
  } catch (e, stackTrace) {
    logger.error('AllLedgers', '获取账本列表失败', e, stackTrace);
    rethrow;
  }
});
