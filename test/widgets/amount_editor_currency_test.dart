/// v30 记账录入的多币种交互(01 §四):
///   - 单币种态(无账户,未选币种):只有轻量币种标(=本位币),无汇率行
///   - 编辑外币交易:汇率行出现,初值=隐含汇率(nativeAmount/amount),
///     折算预览按隐含汇率(改备注不漂移)
import 'dart:async';

import 'package:drift/native.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:beecount/data/db.dart';
import 'package:beecount/data/repositories/local/local_repository.dart';
import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/providers/database_providers.dart';
import 'package:beecount/widgets/biz/amount_editor_sheet.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  SharedPreferences.setMockInitialValues({});

  late BeeDatabase db;
  late LocalRepository repo;

  setUp(() {
    db = BeeDatabase.forTesting(NativeDatabase.memory());
    repo = LocalRepository(db);
  });

  tearDown(() async => db.close());

  Ledger cnyLedger() => Ledger(
        id: 1,
        name: 'L',
        currency: 'CNY',
        type: 'personal',
        createdAt: DateTime(2026, 1, 1),
        myRole: 'owner',
        memberCount: 1,
        isShared: false,
        monthStartDay: 1,
      );

  Widget host({
    String? initialCurrencyCode,
    double? initialAmount,
    double? initialNativeAmount,
    Stream<Ledger?>? ledgerStream,
    Locale locale = const Locale('zh'),
    FutureOr<void> Function(AmountEditorResult)? onSubmit,
    String? submitErrorMessage,
  }) {
    return ProviderScope(
      overrides: [
        repositoryProvider.overrideWithValue(repo),
        currentLedgerProvider
            .overrideWith((ref) => ledgerStream ?? Stream<Ledger?>.value(cnyLedger())),
      ],
      child: MaterialApp(
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        supportedLocales: AppLocalizations.supportedLocales,
        locale: locale,
        home: Scaffold(
          body: AmountEditorSheet(
            categoryName: '餐饮',
            initialDate: DateTime(2026, 7, 12),
            initialAmount: initialAmount,
            initialCurrencyCode: initialCurrencyCode,
            initialNativeAmount: initialNativeAmount,
            ledgerId: 1,
            onSubmit: onSubmit ?? (_) {},
            submitErrorMessage: submitErrorMessage,
          ),
        ),
      ),
    );
  }

  testWidgets('单币种态:无账户显示本位币小字标,无汇率行(零打扰)', (tester) async {
    await db.customStatement(
        "INSERT INTO ledgers (id, name, currency) VALUES (1, 'L', 'CNY')");
    await tester.pumpWidget(host());
    await tester.pumpAndSettle();

    expect(find.text('CNY'), findsOneWidget); // 轻量币种标
    expect(find.textContaining('1 CNY ='), findsNothing); // 无汇率行
    expect(find.textContaining('≈'), findsNothing); // 无折算预览
  });

  testWidgets('编辑外币交易:汇率行按隐含汇率回显 + 折算预览', (tester) async {
    await db.customStatement(
        "INSERT INTO ledgers (id, name, currency) VALUES (1, 'L', 'CNY')");
    // amount=12, native=86.4 → 隐含汇率 7.2
    await tester.pumpWidget(host(
      initialCurrencyCode: 'USD',
      initialAmount: 12,
      initialNativeAmount: 86.4,
    ));
    await tester.pumpAndSettle();

    expect(find.text('USD'), findsOneWidget); // 币种标=该笔币种
    // 反馈9:不展示汇率行,只展示折算预览(按隐含汇率 86.4/12=7.2 计算,
    // 而非当前有效汇率 —— 只改备注折算基准不漂移)
    expect(find.textContaining('1 USD ='), findsNothing);
    expect(find.textContaining('≈ 86.40 CNY'), findsOneWidget);
  });

  testWidgets('英文缺汇率提示在手机宽度内完整换行，不发生横向溢出', (tester) async {
    tester.view.devicePixelRatio = 1;
    tester.view.physicalSize = const Size(360, 640);
    addTearDown(tester.view.resetDevicePixelRatio);
    addTearDown(tester.view.resetPhysicalSize);
    await db.customStatement(
        "INSERT INTO ledgers (id, name, currency) VALUES (1, 'L', 'CNY')");
    await tester.pumpWidget(host(
      locale: const Locale('en'),
      initialCurrencyCode: 'USD',
      initialAmount: 12.50,
    ));
    await tester.pumpAndSettle();

    expect(find.text('Please enter the rate for this entry before saving'), findsOneWidget);
    expect(tester.takeException(), isNull);
    await tester.pumpWidget(const SizedBox.shrink());
  });

  testWidgets('账本首值未加载时不保存，加载 USD 账本后按真实本位币保存一次', (tester) async {
    await db.customStatement(
        "INSERT INTO ledgers (id, name, currency) VALUES (1, 'L', 'USD')");
    final ledgerStream = StreamController<Ledger?>.broadcast(sync: true);
    final submitted = <AmountEditorResult>[];
    try {
      await tester.pumpWidget(host(
        ledgerStream: ledgerStream.stream,
        initialCurrencyCode: 'USD',
        initialAmount: 12.50,
        // A valid implicit rate ensures the old CNY fallback would submit an
        // incorrect native amount, instead of merely stopping for a missing rate.
        initialNativeAmount: 87.50,
        onSubmit: submitted.add,
      ));
      await tester.pumpAndSettle();
      final finish = find.text('完成');
      expect(finish, findsOneWidget);
      await tester.tap(finish);
      await tester.pumpAndSettle();
      expect(submitted, isEmpty);
      expect(tester.takeException(), isNull);

      ledgerStream.add(cnyLedger().copyWith(currency: 'USD'));
      await tester.pumpAndSettle();
      await tester.tap(finish);
      await tester.pumpAndSettle();
      expect(submitted, hasLength(1));
      expect(submitted.single.amount, 12.50);
      expect(submitted.single.currencyCode, 'USD');
      expect(submitted.single.nativeAmount, submitted.single.amount);
      expect(tester.takeException(), isNull);
    } finally {
      await tester.pumpWidget(const SizedBox.shrink());
      await tester.pump();
      // Complete stream cancellation outside the widget test's fake clock.
      await tester.runAsync(() => ledgerStream.close().timeout(const Duration(seconds: 5)));
    }
  }, timeout: const Timeout(Duration(minutes: 1)));

  testWidgets('异步保存失败后恢复输入，并且等待期间不重复提交', (tester) async {
    await db.customStatement(
        "INSERT INTO ledgers (id, name, currency) VALUES (1, 'L', 'CNY')");
    final firstSave = Completer<void>();
    var calls = 0;
    await tester.pumpWidget(host(
      initialAmount: 12.50,
      submitErrorMessage: '保存未完成，请重试',
      onSubmit: (_) async {
        calls++;
        if (calls == 1) await firstSave.future;
      },
    ));
    await tester.pumpAndSettle();
    final finish = find.text('完成');
    expect(finish, findsOneWidget);
    await tester.tap(finish);
    // A second tap before the first frame is also guarded by _isSubmitting.
    await tester.tap(finish);
    await tester.pump();
    expect(calls, 1);
    expect(find.byType(CircularProgressIndicator), findsOneWidget);
    firstSave.completeError(StateError('CAPTURE_LEDGER_CHANGED'));
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
    expect(find.text('保存未完成，请重试'), findsOneWidget);
    expect(find.byType(CircularProgressIndicator), findsNothing);
    await tester.tap(finish);
    await tester.pumpAndSettle();
    expect(calls, 2);
    await tester.pump(const Duration(seconds: 3)); // Let the toast release its overlay.
    await tester.pumpWidget(const SizedBox.shrink());
  });
}
