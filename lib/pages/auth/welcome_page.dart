import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../widgets/biz/bee_icon.dart';
import 'dart:io';
import '../../l10n/app_localizations.dart';
import '../../utils/file_picker_helper.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../providers/ui_state_providers.dart';
import '../../providers/language_provider.dart';
import '../../providers/database_providers.dart';
import '../../providers/theme_providers.dart';
import '../../providers/font_scale_provider.dart';
import '../../providers/currency_providers.dart';
import '../../services/system/logger_service.dart';
import '../../services/export/config_export_service.dart';
import '../../services/billing/post_processor.dart';
import '../../services/attachment_export_import_service.dart';
import '../../utils/currencies.dart';
import '../../widgets/ui/ui.dart';

/// 首次启动欢迎页面
/// 展示应用的独特价值：隐私保护、开源透明、数据自主
class WelcomePage extends ConsumerStatefulWidget {
  const WelcomePage({super.key});

  @override
  ConsumerState<WelcomePage> createState() => _WelcomePageState();
}

class _WelcomePageState extends ConsumerState<WelcomePage> {
  final PageController _pageController = PageController();
  int _currentPage = 0;
  String _selectedCurrency = 'CNY'; // 默认货币
  bool _createDefaultLedger = true; // 是否创建默认账本(币种页复选框)
  // 分类模式: 'flat' = 一级分类, 'hierarchical' = 二级分类, 'none' = 不创建分类
  String _categoryMode = 'flat'; // 默认使用一级分类
  bool _isInitializing = false; // 初始化状态
  bool _isImporting = false; // 导入状态
  bool _isExistingUserFlow = false; // 老用户流程
  bool _isImportingAttachment = false; // 附件导入状态
  bool _isNavigating = false;
  bool _isUpdatingLanguage = false;
  bool get _busy =>
      _isInitializing ||
      _isImporting ||
      _isImportingAttachment ||
      _isNavigating ||
      _isUpdatingLanguage;

  Future<void> _movePage(int page) async {
    if (_busy || !_pageController.hasClients) return;
    setState(() => _isNavigating = true);
    try {
      await _pageController.animateToPage(page,
          duration: const Duration(milliseconds: 300), curve: Curves.easeInOut);
    } finally {
      if (mounted) setState(() => _isNavigating = false);
    }
  }

  Future<void> _changeLanguage(Locale? locale) async {
    if (_busy) return;
    setState(() => _isUpdatingLanguage = true);
    try {
      await ref.read(languageProvider.notifier).setLanguage(locale);
    } catch (error, stack) {
      await _showFailure(error, stack);
    } finally {
      if (mounted) setState(() => _isUpdatingLanguage = false);
    }
  }

  Future<void> _showFailure(Object error, StackTrace stack) async {
    logger.error('welcome', '欢迎流程未完成，可重试', error, stack);
    if (!mounted) return;
    final l10n = AppLocalizations.of(context);
    await showDialog<void>(
        context: context,
        builder: (dialogContext) => AlertDialog(
              title: Text(l10n.commonError),
              content: SingleChildScrollView(child: Text(error.toString())),
              actions: [
                TextButton(
                    onPressed: () => Navigator.pop(dialogContext),
                    child: Text(l10n.commonClose))
              ],
            ));
  }

  Future<void> _persistCompletion(SharedPreferences prefs) async {
    if (!mounted) return;
    final ledgers = await ref.read(repositoryProvider).getAllLedgers();
    if (!mounted) return;
    final currentId = ref.read(currentLedgerIdProvider);
    if (ledgers.isNotEmpty &&
        !ledgers.any((ledger) => ledger.id == currentId)) {
      ref.read(currentLedgerIdProvider.notifier).state = ledgers.first.id;
    }
    ref.read(cachedTransactionsProvider.notifier).state = null;
    ref.read(cachedTransactionsWithCategoryProvider.notifier).state = null;
    await PostProcessor.run(ref,
        ledgerId: ref.read(currentLedgerIdProvider),
        tags: true,
        attachments: true);
    if (!mounted) return;
    if (!await prefs.setBool('welcome_shown', true)) {
      throw StateError('Unable to save setup completion. Please try again.');
    }
    if (mounted) ref.read(shouldShowWelcomeProvider.notifier).state = false;
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context);

    // 老用户流程：只有1页（附件导入）
    final existingUserPages = [
      _buildAttachmentImportPage(context, theme, l10n),
    ];

    // 新用户流程：语言、币种、分类与隐私说明
    final newUserPages = [
      _buildWelcomePage(context, theme, l10n), // 第1屏：语言选择
      _buildCurrencyPage(context, theme, l10n), // 第2屏：货币选择
      _buildCategoryModePage(context, theme, l10n), // 第3屏：分类模式
      _buildPrivacyAndOpenSourcePage(context, theme, l10n), // 第4屏：隐私保护+开源透明
    ];

    final pages = _isExistingUserFlow ? existingUserPages : newUserPages;
    final pageCount = pages.length;

    return PopScope(
        canPop: !_busy,
        child: Scaffold(
          backgroundColor: theme.primaryColor,
          body: SafeArea(
            child: Column(
              children: [
                // 页面指示器
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: List.generate(
                      pageCount,
                      (index) => Container(
                        margin: const EdgeInsets.symmetric(horizontal: 4),
                        width: _currentPage == index ? 24 : 8,
                        height: 8,
                        decoration: BoxDecoration(
                          color: _currentPage == index
                              ? Colors.white
                              : Colors.white.withValues(alpha: 0.5),
                          borderRadius: BorderRadius.circular(4),
                        ),
                      ),
                    ),
                  ),
                ),

                Expanded(
                  child: AbsorbPointer(
                    absorbing: _busy,
                    child: PageView(
                      controller: _pageController,
                      physics: const NeverScrollableScrollPhysics(),
                      onPageChanged: (index) {
                        if (mounted) setState(() => _currentPage = index);
                      },
                      children: pages,
                    ),
                  ),
                ),
                if (!_isExistingUserFlow)
                  Padding(
                    padding: const EdgeInsets.all(16),
                    child: OverflowBar(
                      alignment: _currentPage > 0
                          ? MainAxisAlignment.spaceBetween
                          : MainAxisAlignment.end,
                      overflowAlignment: OverflowBarAlignment.end,
                      spacing: 8,
                      overflowSpacing: 8,
                      children: [
                        if (_currentPage > 0)
                          TextButton(
                            key: const ValueKey('welcome-previous'),
                            onPressed: _busy
                                ? null
                                : () => _movePage(_currentPage - 1),
                            style: TextButton.styleFrom(
                                foregroundColor: Colors.white),
                            child: Text(l10n.commonPrevious),
                          ),
                        if (_currentPage < pageCount - 1)
                          FilledButton(
                            key: const ValueKey('welcome-next'),
                            onPressed: _busy
                                ? null
                                : () => _movePage(_currentPage + 1),
                            style: FilledButton.styleFrom(
                                backgroundColor: Colors.white,
                                foregroundColor: theme.primaryColor),
                            child: Text(l10n.commonNext),
                          )
                        else
                          FilledButton(
                            key: const ValueKey('welcome-finish'),
                            onPressed:
                                _busy ? null : () => _finishWelcome(context),
                            style: FilledButton.styleFrom(
                                backgroundColor: Colors.white,
                                foregroundColor: theme.primaryColor),
                            child: _isInitializing
                                ? const SizedBox(
                                    width: 20,
                                    height: 20,
                                    child: CircularProgressIndicator(
                                        strokeWidth: 2))
                                : Text(l10n.commonFinish),
                          ),
                      ],
                    ),
                  ),
              ],
            ),
          ),
        ));
  }

  /// 第1页：欢迎
  Widget _buildWelcomePage(
      BuildContext context, ThemeData theme, AppLocalizations l10n) {
    final languageNotifier = ref.read(languageProvider.notifier);
    final currentLocale = ref.watch(languageProvider);

    // 可选语言列表
    final availableLocales = [
      null, // 跟随系统
      const Locale('zh'),
      const Locale('zh', 'TW'),
      const Locale('en'),
      const Locale('ko'),
    ];

    return SingleChildScrollView(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // 应用图标
          Container(
            width: 120,
            height: 120,
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.2),
              shape: BoxShape.circle,
            ),
            child: Center(
              // 用主题色填充 SVG 的 currentColor(肚子/翅膀等),传 Colors.white
              // 会让整个蜜蜂变成纯白与背景圆几乎融为一体。
              child: BeeIcon(
                color: theme.colorScheme.primary,
                size: 72,
              ),
            ),
          ),
          const SizedBox(height: 32),

          // 欢迎标题
          Text(
            l10n.welcomeTitle,
            style: theme.textTheme.headlineMedium?.copyWith(
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),

          // 欢迎描述
          Text(
            l10n.welcomeDescription,
            style: theme.textTheme.bodyLarge?.copyWith(
              color: Colors.white.withValues(alpha: 0.9),
            ),
            textAlign: TextAlign.center,
          ),

          const SizedBox(height: 48),

          // 语言选择
          Text(
            l10n.commonLanguage,
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 16),

          // 语言选择列表
          Container(
            constraints: const BoxConstraints(maxHeight: 200),
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: ListView.separated(
              shrinkWrap: true,
              itemCount: availableLocales.length,
              separatorBuilder: (context, index) => Divider(
                height: 1,
                color: Colors.white.withValues(alpha: 0.2),
              ),
              itemBuilder: (context, index) {
                final locale = availableLocales[index];
                final isSelected = currentLocale == locale;
                final displayName =
                    languageNotifier.getLanguageDisplayName(context, locale);

                return InkWell(
                  onTap: () {
                    _changeLanguage(locale);
                  },
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 16, vertical: 12),
                    child: Row(
                      children: [
                        Expanded(
                          child: Text(
                            displayName,
                            style: theme.textTheme.bodyLarge?.copyWith(
                              color: Colors.white,
                              fontWeight: isSelected
                                  ? FontWeight.bold
                                  : FontWeight.normal,
                            ),
                          ),
                        ),
                        if (isSelected)
                          const Icon(
                            Icons.check,
                            color: Colors.white,
                            size: 20,
                          ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),

          // 老用户导入配置（紧凑样式）
          const SizedBox(height: 24),
          TextButton(
            onPressed: _busy ? null : () => _importConfig(context),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                if (_isImporting)
                  const SizedBox(
                    width: 14,
                    height: 14,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: Colors.white70,
                    ),
                  )
                else
                  Icon(
                    Icons.file_upload_outlined,
                    size: 16,
                    color: Colors.white.withValues(alpha: 0.7),
                  ),
                const SizedBox(width: 6),
                Flexible(
                    child: Text(
                  _isImporting
                      ? l10n.welcomeImportingConfig
                      : '${l10n.welcomeExistingUserTitle} ${l10n.welcomeExistingUserButton}',
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: Colors.white.withValues(alpha: 0.7),
                    decoration: TextDecoration.underline,
                    decorationColor: Colors.white.withValues(alpha: 0.5),
                  ),
                )),
              ],
            ),
          ),
        ],
      ),
    );
  }

  /// Currency choices share one scroll view, so the footer remains reachable
  /// even on short screens and with large accessibility text.
  Widget _buildCurrencyPage(
      BuildContext context, ThemeData theme, AppLocalizations l10n) {
    final currencies = getCurrencies(context);
    return ListView(
      padding: const EdgeInsets.all(24),
      children: [
        const Icon(Icons.attach_money, size: 64, color: Colors.white),
        const SizedBox(height: 16),
        Text(l10n.ledgersCurrency,
            textAlign: TextAlign.center,
            style:
                theme.textTheme.headlineSmall?.copyWith(color: Colors.white)),
        const SizedBox(height: 12),
        Text(l10n.welcomeCurrencyDescription,
            textAlign: TextAlign.center,
            style: theme.textTheme.bodyLarge?.copyWith(color: Colors.white)),
        const SizedBox(height: 16),
        CheckboxListTile(
          contentPadding: EdgeInsets.zero,
          value: _createDefaultLedger,
          activeColor: Colors.white,
          checkColor: theme.primaryColor,
          title: Text(l10n.welcomeCreateDefaultLedger,
              style: const TextStyle(color: Colors.white)),
          onChanged: _busy
              ? null
              : (value) =>
                  setState(() => _createDefaultLedger = value ?? false),
        ),
        const Divider(color: Colors.white54),
        for (final currency in currencies)
          RadioListTile<String>(
            contentPadding: EdgeInsets.zero,
            value: currency.code,
            groupValue: _selectedCurrency,
            activeColor: Colors.white,
            title: Text('${currency.name} (${currency.code})',
                style: const TextStyle(color: Colors.white)),
            subtitle: Text(getCurrencySymbol(currency.code),
                style: const TextStyle(color: Colors.white70)),
            onChanged: _busy
                ? null
                : (value) {
                    if (value != null)
                      setState(() => _selectedCurrency = value);
                  },
          ),
      ],
    );
  }

  /// 第4页：开源透明与社群驱动
  Widget _buildPrivacyAndOpenSourcePage(
      BuildContext context, ThemeData theme, AppLocalizations l10n) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // 开源与社群图标
          Container(
            width: 120,
            height: 120,
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.2),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.favorite_outline,
              size: 64,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 32),

          // 标题
          Text(
            l10n.welcomePrivacyTitle,
            style: theme.textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 24),

          // 特性列表
          Center(
            child: IntrinsicWidth(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // 开源代码
                  _buildFeatureItem(
                    context,
                    Icons.code_outlined,
                    Colors.white,
                    l10n.welcomePrivacyFeature1,
                  ),
                  const SizedBox(height: 12),
                  // 隐私保护
                  _buildFeatureItem(
                    context,
                    Icons.shield_outlined,
                    Colors.white,
                    l10n.welcomePrivacyFeature2,
                  ),
                  const SizedBox(height: 12),
                  // 社群驱动
                  _buildFeatureItem(
                    context,
                    Icons.groups_outlined,
                    Colors.white,
                    l10n.welcomeOpenSourceFeature1,
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          const SelectableText(
            'BeeCount · TNT-Likely\nhttps://github.com/TNT-Likely/BeeCount',
            textAlign: TextAlign.center,
            style: TextStyle(color: Colors.white),
          ),
        ],
      ),
    );
  }

  Widget _buildFeatureItem(
    BuildContext context,
    IconData icon,
    Color color,
    String text,
  ) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, color: color, size: 24),
        const SizedBox(width: 12),
        Flexible(
          child: Text(
            text,
            style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                  color: color,
                ),
          ),
        ),
      ],
    );
  }

  /// Persist completion only after the entire seed transaction succeeds.
  Future<void> _finishWelcome(BuildContext context) async {
    if (_busy || !mounted) return;
    final l10n = AppLocalizations.of(context);
    final db = ref.read(databaseProvider);
    final currency = _selectedCurrency;
    final categoryMode = _categoryMode;
    final createLedger = _createDefaultLedger;
    setState(() => _isInitializing = true);
    try {
      await db.transaction(() => db.ensureSeed(
            l10n: l10n,
            currency: currency,
            useHierarchicalCategories: categoryMode == 'hierarchical',
            skipCategories: categoryMode == 'none',
            createDefaultLedger: createLedger,
          ));
      final prefs = await SharedPreferences.getInstance();
      if (!await prefs.setString('category_mode', categoryMode) ||
          !await prefs.setString('selected_currency', currency) ||
          !await prefs.setString('baseCurrency', currency)) {
        throw StateError('Unable to save setup preferences. Please try again.');
      }
      if (!mounted) return;
      ref.read(baseCurrencyProvider.notifier).state = currency.toUpperCase();
      await _persistCompletion(prefs);
    } catch (error, stack) {
      // The failed operation is over; do not keep animating a progress spinner
      // underneath the error dialog while the user reviews and dismisses it.
      if (mounted) setState(() => _isInitializing = false);
      await _showFailure(error, stack);
    } finally {
      if (mounted) setState(() => _isInitializing = false);
    }
  }

  /// 导入配置文件（老用户）
  Future<void> _importConfig(BuildContext context) async {
    if (_busy || !mounted) return;
    final l10n = AppLocalizations.of(context);

    setState(() {
      _isImporting = true;
    });

    try {
      // 选择文件（使用 FilePickerHelper 处理部分设备不支持扩展名过滤的问题）
      final result = await FilePickerHelper.pickYamlFile();

      if (result == null) {
        if (context.mounted) {
          showToast(context, l10n.welcomeImportNoFile);
        }
        return;
      }

      final filePath = result.path;
      if (filePath == null) {
        if (context.mounted) {
          showToast(context, l10n.welcomeImportNoFile);
        }
        return;
      }

      if (!mounted) return;
      // 读取文件内容
      final file = File(filePath);
      final yamlContent = await file.readAsString();

      logger.info('welcome', '开始导入配置文件');

      if (!mounted) return;
      // 不再调 ensureSeed:删了"配置无账本时自动创建默认账本"后 db 变量没用了。
      final repo = ref.read(repositoryProvider);

      // 导入配置
      await ConfigExportService.importFromYaml(
        yamlContent,
        repository: repo,
      );

      logger.info('welcome', '配置文件导入成功');

      // 不再自动创建默认账本 — 即使配置不含账本也尊重用户意图(可能就是想从
      // 空状态开始)。没账本时进入应用,LedgersPage 空态会引导新建。
      final ledgers = await repo.getAllLedgers();
      logger.info('welcome', '配置包含 ${ledgers.length} 个账本');

      if (!mounted) return;

      // 刷新所有配置相关的 providers，使导入的配置立即生效
      ref.invalidate(primaryColorInitProvider);
      ref.invalidate(themeModeInitProvider);
      ref.invalidate(compactAmountInitProvider);
      ref.invalidate(showTransactionTimeInitProvider);
      ref.invalidate(noteDisplayModeInitProvider);
      ref.invalidate(fontScaleInitProvider);
      ref.invalidate(languageProvider);

      if (context.mounted) {
        showToast(context, l10n.welcomeImportSuccess);
        // 切换到老用户流程（附件导入页面）
        setState(() {
          _isExistingUserFlow = true;
          _currentPage = 0;
        });
        _pageController.jumpToPage(0);
      }
    } catch (e, st) {
      logger.error('welcome', '导入配置文件失败', e, st);
      if (context.mounted) {
        showToast(context, l10n.welcomeImportFailed(e.toString()));
      }
    } finally {
      if (mounted) {
        setState(() {
          _isImporting = false;
        });
      }
    }
  }

  /// 附件导入页面（老用户流程）
  Widget _buildAttachmentImportPage(
      BuildContext context, ThemeData theme, AppLocalizations l10n) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // 图标
          Container(
            width: 120,
            height: 120,
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.2),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.image_outlined,
              size: 64,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 32),

          // 标题
          Text(
            l10n.welcomeImportAttachmentTitle,
            style: theme.textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),

          // 描述
          Text(
            l10n.welcomeImportAttachmentDesc,
            style: theme.textTheme.bodyLarge?.copyWith(
              color: Colors.white.withValues(alpha: 0.9),
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 48),

          // 导入按钮
          FilledButton.icon(
            onPressed: _busy ? null : () => _importAttachments(context),
            icon: _isImportingAttachment
                ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: Colors.white,
                    ),
                  )
                : const Icon(Icons.file_upload_outlined),
            label: Text(
              _isImportingAttachment
                  ? l10n.welcomeImportingAttachment
                  : l10n.welcomeImportAttachmentButton,
            ),
            style: FilledButton.styleFrom(
              backgroundColor: Colors.white,
              foregroundColor: theme.primaryColor,
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
            ),
          ),
          const SizedBox(height: 16),

          // 跳过按钮
          TextButton(
            onPressed: _busy ? null : _finishExistingUserFlow,
            child: Text(
              l10n.welcomeImportAttachmentSkip,
              style: TextStyle(
                color: Colors.white.withValues(alpha: 0.8),
                decoration: TextDecoration.underline,
                decorationColor: Colors.white.withValues(alpha: 0.5),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _finishExistingUserFlow() async {
    if (_busy || !mounted) return;
    setState(() => _isInitializing = true);
    try {
      await _persistCompletion(await SharedPreferences.getInstance());
    } catch (error, stack) {
      await _showFailure(error, stack);
    } finally {
      if (mounted) setState(() => _isInitializing = false);
    }
  }

  /// 导入附件
  Future<void> _importAttachments(BuildContext context) async {
    if (_busy || !mounted) return;
    setState(() => _isImportingAttachment = true);
    final l10n = AppLocalizations.of(context);

    try {
      // 选择附件归档文件（使用 FilePickerHelper 处理部分设备不支持扩展名过滤的问题）
      final result = await FilePickerHelper.pickArchiveFile();

      if (result == null) {
        return;
      }

      final filePath = result.path;
      if (filePath == null) {
        return;
      }

      if (!context.mounted) return;

      // 显示加载状态
      setState(() {
        _isImportingAttachment = true;
      });

      // 执行导入
      final importService = ref.read(attachmentExportImportServiceProvider);
      final importResult = await importService.importAttachments(
        archivePath: filePath,
        conflictStrategy: AttachmentExportImportService.conflictSkip,
      );

      if (!context.mounted) return;

      if (importResult.success) {
        showToast(context,
            l10n.welcomeImportAttachmentSuccess(importResult.imported));
        // 导入成功，完成流程
        await _persistCompletion(await SharedPreferences.getInstance());
      } else {
        showToast(context,
            l10n.welcomeImportAttachmentFailed(importResult.message ?? ''));
      }
    } catch (e, st) {
      logger.error('welcome', '导入附件失败', e, st);
      if (context.mounted) {
        showToast(context, l10n.welcomeImportAttachmentFailed(e.toString()));
      }
    } finally {
      if (mounted) {
        setState(() {
          _isImportingAttachment = false;
        });
      }
    }
  }

  /// 第3页：分类模式选择
  Widget _buildCategoryModePage(
      BuildContext context, ThemeData theme, AppLocalizations l10n) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // 图标
          Container(
            width: 120,
            height: 120,
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.2),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.category_outlined,
              size: 64,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 32),

          // 标题
          Text(
            l10n.welcomeCategoryModeTitle,
            style: theme.textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),

          // 描述
          Text(
            l10n.welcomeCategoryModeDescription,
            style: theme.textTheme.bodyMedium?.copyWith(
              color: Colors.white.withValues(alpha: 0.9),
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 32),

          // 一级分类选项
          _buildCategoryModeOption(
            context,
            theme,
            l10n,
            mode: 'flat',
            title: l10n.welcomeCategoryModeFlatTitle,
            description: l10n.welcomeCategoryModeFlatDescription,
            features: [
              l10n.welcomeCategoryModeFlatFeature1,
              l10n.welcomeCategoryModeFlatFeature2,
              l10n.welcomeCategoryModeFlatFeature3,
            ],
          ),

          const SizedBox(height: 16),

          // 二级分类选项
          _buildCategoryModeOption(
            context,
            theme,
            l10n,
            mode: 'hierarchical',
            title: l10n.welcomeCategoryModeHierarchicalTitle,
            description: l10n.welcomeCategoryModeHierarchicalDescription,
            features: [
              l10n.welcomeCategoryModeHierarchicalFeature1,
              l10n.welcomeCategoryModeHierarchicalFeature2,
              l10n.welcomeCategoryModeHierarchicalFeature3,
            ],
          ),

          const SizedBox(height: 16),

          // 不创建分类选项
          _buildCategoryModeOption(
            context,
            theme,
            l10n,
            mode: 'none',
            title: l10n.welcomeCategoryModeNoneTitle,
            description: l10n.welcomeCategoryModeNoneDescription,
            features: [
              l10n.welcomeCategoryModeNoneFeature1,
              l10n.welcomeCategoryModeNoneFeature2,
              l10n.welcomeCategoryModeNoneFeature3,
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildCategoryModeOption(
    BuildContext context,
    ThemeData theme,
    AppLocalizations l10n, {
    required String mode, // 'flat', 'hierarchical', or 'none'
    required String title,
    required String description,
    required List<String> features,
  }) {
    final isSelected = _categoryMode == mode;

    return InkWell(
      onTap: () {
        setState(() {
          _categoryMode = mode;
        });
      },
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: isSelected
              ? Colors.white.withValues(alpha: 0.3)
              : Colors.white.withValues(alpha: 0.1),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color:
                isSelected ? Colors.white : Colors.white.withValues(alpha: 0.3),
            width: 2,
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  isSelected
                      ? Icons.radio_button_checked
                      : Icons.radio_button_unchecked,
                  color: Colors.white,
                  size: 24,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        title,
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        description,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: Colors.white.withValues(alpha: 0.8),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            ...features.map((feature) => Padding(
                  padding: const EdgeInsets.only(bottom: 4),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Icon(
                        Icons.check_circle_outline,
                        color: Colors.white.withValues(alpha: 0.8),
                        size: 16,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          feature,
                          style: theme.textTheme.bodySmall?.copyWith(
                            color: Colors.white.withValues(alpha: 0.8),
                          ),
                        ),
                      ),
                    ],
                  ),
                )),
          ],
        ),
      ),
    );
  }
}
