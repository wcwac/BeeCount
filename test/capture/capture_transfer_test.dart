import 'dart:convert';

import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/models/capture_candidate.dart';
import 'package:beecount/providers.dart';
import 'package:beecount/services/capture/capture_commit_service.dart';
import 'package:beecount/services/capture/capture_service.dart';
import 'package:beecount/widgets/biz/amount_editor_sheet.dart';
import 'package:beecount/widgets/transaction/transfer_form.dart';
import 'package:drift/native.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  final binding = TestWidgetsFlutterBinding.ensureInitialized();
  late BeeDatabase db;
  late LocalRepository repo;
  setUp(() {
    SharedPreferences.setMockInitialValues({});
    db = BeeDatabase.forTesting(NativeDatabase.memory());
    repo = LocalRepository(db);
    binding.defaultBinaryMessenger.setMockMethodCallHandler(CaptureService.channel, (_) async => null);
  });
  tearDown(() async {
    binding.defaultBinaryMessenger.setMockMethodCallHandler(CaptureService.channel, null);
    await db.close();
  });

  for (final quote in [
    (name: 'cached', cached: true, rate: '0.14', amount: 14.0),
    (name: 'manual', cached: false, rate: '0.14123', amount: 14.12),
  ]) {
    testWidgets('a CNY capture uses the confirmed ${quote.name} rate for USD account balances', (tester) async {
      final ledgerId = await repo.createLedger(name: 'USD ledger', currency: 'USD');
      final ledger = (await repo.getLedgerById(ledgerId))!;
      final from = await repo.createAccount(ledgerId: ledgerId, name: 'From USD',
        currency: 'USD', initialBalance: 100);
      final to = await repo.createAccount(ledgerId: ledgerId, name: 'To USD',
        currency: 'USD', initialBalance: 0);
      await repo.getTransferCategory();
      if (quote.cached) {
        await repo.upsertAutoRates(base: 'USD', rateDate: '2026-10-21',
          rates: {'CNY': quote.rate}, source: 'fixture', fetchedAt: DateTime.utc(2026, 10, 21));
      }
      final candidate = CaptureCandidate.fromMap({
        'id': 'foreign-transfer-${quote.name}', 'sourcePackage': 'com.eg.android.AlipayGphone',
        'ledgerId': ledgerId, 'ledgerKey': CaptureCommitService.ledgerKey(ledger),
        'observedAt': DateTime.utc(2026, 10, 21).millisecondsSinceEpoch,
        'amountMinor': 10000, 'direction': 'transfer', 'currency': 'CNY',
      });
      var completed = 0;
      await tester.pumpWidget(ProviderScope(
        overrides: [
          databaseProvider.overrideWithValue(db),
          repositoryProvider.overrideWithValue(repo),
          currentLedgerIdProvider.overrideWith((ref) => ledgerId),
        ],
        child: MaterialApp(
          locale: const Locale('en'),
          localizationsDelegates: AppLocalizations.localizationsDelegates,
          supportedLocales: AppLocalizations.supportedLocales,
          home: Scaffold(body: TransferForm(
            captureCandidate: candidate,
            initialFromAccountId: from,
            initialToAccountId: to,
            initialAmount: 100,
            initialCurrencyCode: 'CNY',
            initialDate: candidate.transactionTime,
            onTransferComplete: () { completed++; },
          )),
        ),
      ));
      await tester.pumpAndSettle();
      expect(find.byType(AmountEditorSheet), findsOneWidget);
      expect(find.text('CNY'), findsOneWidget);
      if (!quote.cached) {
        final missingRate = find.text('Please enter the rate for this entry before saving');
        expect(missingRate, findsOneWidget);
        await tester.tap(find.text('Finish'));
        await tester.pumpAndSettle();
        expect(await db.select(db.transactions).get(), isEmpty);
        await tester.pump(const Duration(seconds: 3));
        await tester.pumpAndSettle();
        await tester.tap(missingRate.first);
        await tester.pumpAndSettle();
        await tester.enterText(find.descendant(of: find.byType(AlertDialog),
          matching: find.byType(TextField)), quote.rate);
        await tester.tap(find.descendant(of: find.byType(AlertDialog), matching: find.text('Confirm')));
        await tester.pumpAndSettle();
      }
      expect(find.textContaining('≈ ${quote.amount.toStringAsFixed(2)} USD'), findsOneWidget);
      await tester.tap(find.text('Finish'));
      await tester.pumpAndSettle();
      final transactions = await db.select(db.transactions).get();
      expect(transactions, hasLength(1));
      expect(transactions.single.amount, quote.amount);
      expect(transactions.single.currencyCode, 'USD');
      expect(transactions.single.nativeAmount, quote.amount);
      expect(await repo.getAccountBalance(from), closeTo(100 - quote.amount, 0.00001));
      expect(await repo.getAccountBalance(to), closeTo(quote.amount, 0.00001));
      final link = await db.customSelect('SELECT candidate_json FROM transaction_capture_links').getSingle();
      final original = jsonDecode(link.read<String>('candidate_json')) as Map;
      expect(original['amountMinor'], 10000);
      expect(original['currency'], 'CNY');
      expect(completed, 1);
      expect(tester.takeException(), isNull);
      await tester.pumpWidget(const SizedBox());
      await tester.pump(const Duration(seconds: 3));
      await tester.pumpAndSettle();
    }, variant: TargetPlatformVariant.only(TargetPlatform.android));
  }
}
