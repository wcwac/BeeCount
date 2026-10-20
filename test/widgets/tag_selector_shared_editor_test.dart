import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:beecount/data/db.dart';
import 'package:beecount/l10n/app_localizations.dart';
import 'package:beecount/pages/tag/widgets/tag_selector.dart';
import 'package:beecount/providers/database_providers.dart';
import 'package:beecount/providers/tag_providers.dart';

void main() {
  Ledger ledger({required String role, bool isShared = true}) => Ledger(
        id: 1,
        name: 'Shared',
        currency: 'CNY',
        type: 'personal',
        createdAt: DateTime(2026, 1, 1),
        syncId: 'shared-ledger-1',
        myRole: role,
        memberCount: 2,
        isShared: isShared,
        monthStartDay: 1,
      );

  Tag tag({required int id, required String name}) => Tag(
        id: id,
        name: name,
        sortOrder: 0,
        createdAt: DateTime(2026, 1, 1),
        syncId: 'tag-$id',
      );

  Widget host({
    required String role,
    bool isShared = true,
    List<Tag> tags = const [],
    List<int> selectedTagIds = const [],
  }) {
    return ProviderScope(
      overrides: [
        currentLedgerProvider.overrideWith(
          (ref) => Stream<Ledger?>.value(
            ledger(role: role, isShared: isShared),
          ),
        ),
        tagsForCurrentLedgerProvider.overrideWith(
          (ref) async => tags,
        ),
        recentTagsForCurrentLedgerProvider.overrideWith(
          (ref) async => const <Tag>[],
        ),
      ],
      child: MaterialApp(
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        supportedLocales: AppLocalizations.supportedLocales,
        locale: const Locale('zh'),
        home: Scaffold(
          body: TagSelector(selectedTagIds: selectedTagIds),
        ),
      ),
    );
  }

  Widget routeHost({
    required List<Tag> tags,
    required List<int> selectedTagIds,
    required void Function(List<int>?) onResult,
  }) {
    return ProviderScope(
      overrides: [
        currentLedgerProvider.overrideWith(
          (ref) => Stream<Ledger?>.value(ledger(role: 'editor')),
        ),
        tagsForCurrentLedgerProvider.overrideWith((ref) async => tags),
        recentTagsForCurrentLedgerProvider.overrideWith(
          (ref) async => const <Tag>[],
        ),
      ],
      child: MaterialApp(
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        supportedLocales: AppLocalizations.supportedLocales,
        locale: const Locale('zh'),
        home: Builder(
          builder: (context) => Scaffold(
            body: ElevatedButton(
              onPressed: () async {
                onResult(await TagSelector.show(
                  context,
                  selectedTagIds: selectedTagIds,
                ));
              },
              child: const Text('打开标签选择器'),
            ),
          ),
        ),
      ),
    );
  }

  testWidgets('历史共享账本在离线版可以创建本地标签', (tester) async {
    await tester.pumpWidget(host(
      role: 'editor',
      tags: [tag(id: -101, name: 'Owner Tag')],
    ));
    await tester.pumpAndSettle();

    expect(find.text('Owner Tag'), findsWidgets);
    expect(find.text('新建标签'), findsOneWidget);
    expect(find.text('共享账本标签由所有者管理'), findsNothing);
  });

  testWidgets('历史共享账本搜索无结果仍可创建本地标签', (tester) async {
    await tester.pumpWidget(host(
      role: 'editor',
      tags: [tag(id: -101, name: 'Owner Tag')],
    ));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField), '不存在');
    await tester.pumpAndSettle();

    expect(find.text('Owner Tag'), findsNothing);
    expect(find.text('新建标签'), findsOneWidget);
    expect(find.text('共享账本标签由所有者管理'), findsNothing);
  });

  testWidgets('共享账本 Owner 仍可从标签选择器新建标签', (tester) async {
    await tester.pumpWidget(host(role: 'owner'));
    await tester.pumpAndSettle();

    expect(find.text('新建标签'), findsOneWidget);
    expect(find.text('共享账本标签由所有者管理'), findsNothing);
  });

  testWidgets('个人账本仍可从标签选择器新建标签', (tester) async {
    await tester.pumpWidget(host(role: 'owner', isShared: false));
    await tester.pumpAndSettle();

    expect(find.text('新建标签'), findsOneWidget);
    expect(find.text('共享账本标签由所有者管理'), findsNothing);
  });

  testWidgets('Editor 确认时会移除不可见的历史个人标签 ID', (tester) async {
    final ownerTag = tag(id: -101, name: 'Owner Tag');
    List<int>? result;
    await tester.pumpWidget(routeHost(
      tags: [ownerTag],
      selectedTagIds: [42, ownerTag.id],
      onResult: (value) => result = value,
    ));
    await tester.tap(find.text('打开标签选择器'));
    await tester.pumpAndSettle();

    await tester.tap(find.text('确定'));
    await tester.pumpAndSettle();

    expect(result, containsAll([42, ownerTag.id]));

  });

  testWidgets('共享资源暂时为空时 Editor 确认仍保留已有 Owner synthetic ID', (tester) async {
    List<int>? result;
    await tester.pumpWidget(routeHost(
      tags: const [],
      selectedTagIds: const [-202, 42],
      onResult: (value) => result = value,
    ));
    await tester.tap(find.text('打开标签选择器'));
    await tester.pumpAndSettle();

    await tester.tap(find.text('确定'));
    await tester.pumpAndSettle();

    expect(result, containsAll([-202, 42]));

  });

  test('账本 provider reload 时不沿用上一个 Owner 的创建权限', () async {
    final controller = StreamController<Ledger?>.broadcast();
    final container = ProviderContainer(
      overrides: [
        currentLedgerProvider.overrideWith((ref) => controller.stream),
      ],
    );
    addTearDown(() async {
      container.dispose();
      await controller.close();
    });
    final subscription = container.listen<bool>(
      canCreateTagForCurrentLedgerProvider,
      (_, __) {},
      fireImmediately: true,
    );
    addTearDown(subscription.close);

    controller.add(ledger(role: 'owner'));
    await pumpEventQueue();
    expect(container.read(canCreateTagForCurrentLedgerProvider), isTrue);

    container.invalidate(currentLedgerProvider);
    await pumpEventQueue();
    expect(container.read(canCreateTagForCurrentLedgerProvider), isFalse,
        reason: 'reload/loading 期间必须 fail closed，不能复用 previous Owner');
  });
}
