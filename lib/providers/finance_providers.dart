import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/repositories/finance_repository.dart';
import '../models/finance.dart';
import 'database_providers.dart';
import 'local_data_providers.dart';

final financeRepositoryProvider = Provider<FinanceRepository>((ref) =>
    FinanceRepository(ref.watch(databaseProvider)));

final financePlansProvider = FutureProvider.autoDispose.family<List<FinancePlan>, int>((ref, id) {
  ref.watch(localDataRevisionProvider);
  return ref.watch(financeRepositoryProvider).list(id);
});
final financePlanProvider = FutureProvider.autoDispose.family<FinancePlan?, int>((ref, id) {
  ref.watch(localDataRevisionProvider);
  return ref.watch(financeRepositoryProvider).get(id);
});
final financeEventsProvider = FutureProvider.autoDispose.family<List<FinanceEvent>, int>((ref, id) {
  ref.watch(localDataRevisionProvider);
  return ref.watch(financeRepositoryProvider).events(id);
});

final financePlanForTransactionProvider = FutureProvider.autoDispose.family<int?, int>((ref, id) {
  ref.watch(localDataRevisionProvider);
  return ref.watch(financeRepositoryProvider).planIdForTransaction(id);
});
final financePlanForAccountProvider = FutureProvider.autoDispose.family<int?, int>((ref, id) {
  ref.watch(localDataRevisionProvider);
  return ref.watch(financeRepositoryProvider).planIdForAccount(id);
});
