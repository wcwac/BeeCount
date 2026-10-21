import 'package:drift/drift.dart';

/// Auxiliary metadata uses the same Drift transaction as the financial write.
Future<void> createCaptureSchema(GeneratedDatabase db) async {
  for (final sql in captureSchemaStatements) {
    await db.customStatement(sql);
  }
}

const captureSchemaStatements = <String>[
  '''CREATE TABLE IF NOT EXISTS transaction_capture_links (
    candidate_id TEXT PRIMARY KEY NOT NULL,
    transaction_id INTEGER,
    ledger_id INTEGER NOT NULL,
    source_package TEXT NOT NULL,
    candidate_json TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    FOREIGN KEY(transaction_id) REFERENCES transactions(id) ON DELETE SET NULL
  )''',
  'CREATE INDEX IF NOT EXISTS idx_capture_transaction ON transaction_capture_links(transaction_id)',
  '''CREATE TRIGGER IF NOT EXISTS capture_preserve_deleted
    AFTER DELETE ON transactions BEGIN
      UPDATE transaction_capture_links SET transaction_id=NULL WHERE transaction_id=OLD.id;
    END''',
];
