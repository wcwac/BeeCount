import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../providers.dart';
import '../../providers/budget_providers.dart';
import '../../providers/calendar_providers.dart';
import '../attachment_service.dart';

/// 本地数据变更后统一刷新界面缓存。
class PostProcessor {
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
      bool tags = false, bool attachments = false}) async =>
      _refresh(ref.read, ledgerId: ledgerId, tags: tags, attachments: attachments);

  static Future<void> refreshLocal(WidgetRef ref, {required int ledgerId}) async =>
      _refresh(ref.read, ledgerId: ledgerId);
  static Future<void> refreshLocalC(ProviderContainer c, {required int ledgerId}) async =>
      _refresh(c.read, ledgerId: ledgerId);
  static Future<void> refreshLocalR(Ref ref, {required int ledgerId}) async =>
      _refresh(ref.read, ledgerId: ledgerId);
}
