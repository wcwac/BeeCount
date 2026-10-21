import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/misc.dart' show ProviderListenable;
import '../../providers.dart';
import '../../providers/budget_providers.dart';
import '../../providers/calendar_providers.dart';
import '../attachment_service.dart';

/// 本地数据变更后统一刷新界面缓存。
class PostProcessor {
  /// Capture commits can finish after their editor's provider scope is gone.
  /// These refresh counters live in the application's root scope. Retain their
  /// controllers before awaiting the commit, and skip any that were disposed.
  static void Function() prepareLocalRefresh(WidgetRef ref, {required int ledgerId,
      bool tags = false, bool attachments = false}) {
    final counters = [
      ref.read(statsRefreshProvider.notifier),
      ref.read(budgetRefreshProvider.notifier),
      ref.read(calendarRefreshProvider.notifier),
      ref.read(ledgerDataRefreshByLedgerProvider(ledgerId).notifier),
      ref.read(localDataRevisionProvider.notifier),
      ref.read(ledgerListRefreshProvider.notifier),
      if (tags) ref.read(tagListRefreshProvider.notifier),
      if (attachments) ref.read(attachmentListRefreshProvider.notifier),
    ];
    return () {
      for (final counter in counters) {
        if (counter.mounted) counter.state++;
      }
    };
  }

  static void _refresh(T Function<T>(ProviderListenable<T>) read,
      {required int ledgerId, bool tags = false, bool attachments = false}) {
    read(statsRefreshProvider.notifier).state++;
    read(budgetRefreshProvider.notifier).state++;
    read(calendarRefreshProvider.notifier).state++;
    read(ledgerDataRefreshByLedgerProvider(ledgerId).notifier).state++;
    read(localDataRevisionProvider.notifier).state++;
    read(ledgerListRefreshProvider.notifier).state++;
    if (tags) read(tagListRefreshProvider.notifier).state++;
    if (attachments) read(attachmentListRefreshProvider.notifier).state++;
  }

  static Future<void> run(WidgetRef ref, {required int ledgerId,
      bool tags = false, bool attachments = false}) async =>
      _refresh(ref.read, ledgerId: ledgerId, tags: tags, attachments: attachments);

  static Future<void> runC(ProviderContainer c, {required int ledgerId,
      bool tags = false, bool attachments = false}) async =>
      _refresh(c.read, ledgerId: ledgerId, tags: tags, attachments: attachments);

  static Future<void> runR(Ref ref, {required int ledgerId,
      bool tags = false, bool attachments = false}) async {
    if (!ref.mounted) return;
    _refresh(ref.read, ledgerId: ledgerId, tags: tags, attachments: attachments);
  }

  static Future<void> refreshLocal(WidgetRef ref, {required int ledgerId}) async =>
      _refresh(ref.read, ledgerId: ledgerId);
  static Future<void> refreshLocalC(ProviderContainer c, {required int ledgerId}) async =>
      _refresh(c.read, ledgerId: ledgerId);
  static Future<void> refreshLocalR(Ref ref, {required int ledgerId}) async {
    if (!ref.mounted) return;
    _refresh(ref.read, ledgerId: ledgerId);
  }
}
