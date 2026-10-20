/// 用于列表展示的本地账本摘要。
class LedgerDisplayItem {
  final int id;
  final String name;
  final String currency;
  final int transactionCount;
  final double balance;
  final DateTime lastUpdated;

  const LedgerDisplayItem({
    required this.id,
    required this.name,
    required this.currency,
    required this.transactionCount,
    required this.balance,
    required this.lastUpdated,
  });

  factory LedgerDisplayItem.fromLocal({
    required int id,
    required String name,
    required String currency,
    required DateTime createdAt,
    required int transactionCount,
    required double balance,
  }) => LedgerDisplayItem(
    id: id,
    name: name,
    currency: currency,
    transactionCount: transactionCount,
    balance: balance,
    lastUpdated: createdAt,
  );
}
