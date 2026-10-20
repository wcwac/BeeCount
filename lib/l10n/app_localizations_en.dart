import 'package:intl/intl.dart' as intl;

import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get aboutPrivacyPolicy => 'Privacy Policy';

  @override
  String get aboutChangelog => 'Changelog';

  @override
  String get appTitle => 'Bee Accounting';

  @override
  String get tabHome => 'Home';

  @override
  String get tabInsights => 'Stats';

  @override
  String get tabAssets => 'Assets';

  @override
  String get tabRecord => 'Record';

  @override
  String get tabMine => 'Mine';

  @override
  String get commonCancel => 'Cancel';

  @override
  String get commonConfirm => 'Confirm';

  @override
  String get commonSave => 'Save';

  @override
  String get commonDelete => 'Delete';

  @override
  String get commonAdd => 'Add';

  @override
  String get commonEdit => 'Edit';

  @override
  String get commonMore => 'More';

  @override
  String get commonOk => 'OK';

  @override
  String get commonKnow => 'Got it';

  @override
  String get commonNo => 'No';

  @override
  String get commonEmpty => 'No data';

  @override
  String get commonError => 'Error';

  @override
  String get commonSuccess => 'Success';

  @override
  String get commonFailed => 'Failed';

  @override
  String get commonBack => 'Back';

  @override
  String get commonNext => 'Next';

  @override
  String get fabActionCamera => 'Camera';

  @override
  String get fabActionGallery => 'Gallery';

  @override
  String get commonPrevious => 'Previous';

  @override
  String get commonFinish => 'Finish';

  @override
  String get commonClose => 'Close';

  @override
  String get commonOther => 'Other';

  @override
  String get commonYesterday => 'Yesterday';

  @override
  String get commonSearch => 'Search';

  @override
  String get commonNoteHint => 'Note...';

  @override
  String get commonSettings => 'Settings';

  @override
  String get commonGoSettings => 'Go to Settings';

  @override
  String get commonLanguage => 'Language';

  @override
  String get commonCurrent => 'Current';

  @override
  String get commonTutorial => 'Tutorial';

  @override
  String get commonConfigure => 'Configure';

  @override
  String get commonPressAgainToExit => 'Press again to exit';

  @override
  String get commonWeekdayMonday => 'Monday';

  @override
  String get commonWeekdayTuesday => 'Tuesday';

  @override
  String get commonWeekdayWednesday => 'Wednesday';

  @override
  String get commonWeekdayThursday => 'Thursday';

  @override
  String get commonWeekdayFriday => 'Friday';

  @override
  String get commonWeekdaySaturday => 'Saturday';

  @override
  String get commonWeekdaySunday => 'Sunday';

  @override
  String get homeIncome => 'Income';

  @override
  String get homeExpense => 'Expense';

  @override
  String get homeBalance => 'Balance';

  @override
  String get homeNoRecords => 'No records yet';

  @override
  String get homeSelectDate => 'Select date';

  @override
  String get homeAppTitle => 'Bee Accounting';

  @override
  String get homeSearch => 'Search';

  @override
  String homeYear(int year) {
    return '$year';
  }

  @override
  String homeMonth(String month) {
    return '${month}M';
  }

  @override
  String get homeNoRecordsSubtext => 'Tap the plus button at the bottom to add a record';

  @override
  String get homeLastMonthReportSubtitle => 'View and share last month\'s report';

  @override
  String get homeLastMonthReportView => 'View';

  @override
  String homeAnnualReportReminder(int year) {
    return 'Your $year Annual Report is ready';
  }

  @override
  String get homeAnnualReportView => 'View';

  @override
  String get widgetTodayExpense => 'Today\'s Expense';

  @override
  String get widgetConsumptionRhythmTitle => 'Spending Rhythm';

  @override
  String get widgetConsumptionRhythmRange => 'Last 30 days';

  @override
  String get widgetConsumptionRhythmStable => 'Spending is steady';

  @override
  String get widgetConsumptionRhythmIncrease => 'Faster than last week';

  @override
  String get widgetConsumptionRhythmDecrease => 'Steadier than last week';

  @override
  String get widgetConsumptionRhythmEmpty => 'No spending in the last 30 days';

  @override
  String get widgetBeeTrailTitle => 'Record Bee Trail';

  @override
  String get widgetBeeTrailStreakSuffix => 'days';

  @override
  String get widgetBeeTrailCompletion => '28-day completion';

  @override
  String get widgetBeeTrailEmpty => 'Add a record to light the first cell';

  @override
  String get widgetTodayIncome => 'Today\'s Income';

  @override
  String get widgetMonthExpense => 'Month\'s Expense';

  @override
  String get widgetMonthIncome => 'Month\'s Income';

  @override
  String get widgetMonthSuffix => '';

  @override
  String get widgetToday => 'Today';

  @override
  String get widgetQuickAddLabel => 'Add';

  @override
  String get widgetBudgetTotal => 'Total';

  @override
  String get widgetBudgetRemaining => 'Left';

  @override
  String get widgetNoBudget => 'No Budget';

  @override
  String get widgetNoTransactions => 'No Transactions';

  @override
  String get widgetRecentTransactions => 'Recent Transactions';

  @override
  String get widgetNoAccounts => 'No Accounts';

  @override
  String get searchTitle => 'Search';

  @override
  String get searchHint => 'Search notes, categories or amounts...';

  @override
  String get searchCategoryHint => 'Search category name...';

  @override
  String get searchCategoryFilter => 'Category Filter';

  @override
  String get searchMinAmount => 'Min amount';

  @override
  String get searchMaxAmount => 'Max amount';

  @override
  String get searchNoInput => 'Enter keywords to start searching';

  @override
  String get searchNoResults => 'No matching results found';

  @override
  String get searchBatchMode => 'Batch Operations';

  @override
  String searchBatchModeWithCount(Object selected, Object total) {
    return 'Batch Operations ($selected/$total)';
  }

  @override
  String get searchExitBatchMode => 'Exit Batch Mode';

  @override
  String get searchSelectAll => 'Select All';

  @override
  String get searchDeselectAll => 'Deselect All';

  @override
  String searchSelectedCount(Object count) {
    return '$count selected';
  }

  @override
  String get searchBatchSetNote => 'Set Note';

  @override
  String get searchBatchChangeCategory => 'Change Category';

  @override
  String get searchBatchDeleteConfirmTitle => 'Confirm Delete';

  @override
  String searchBatchDeleteConfirmMessage(Object count) {
    return 'Are you sure you want to delete the selected $count transactions?\nThis action cannot be undone.';
  }

  @override
  String get searchBatchSetNoteTitle => 'Batch Set Note';

  @override
  String searchBatchSetNoteMessage(Object count) {
    return 'Set the same note for the selected $count transactions';
  }

  @override
  String get searchBatchSetNoteHint => 'Enter note content (leave empty to clear notes)';

  @override
  String searchBatchDeleteSuccess(Object count) {
    return 'Successfully deleted $count transactions';
  }

  @override
  String searchBatchDeleteFailed(Object error) {
    return 'Delete failed: $error';
  }

  @override
  String searchBatchSetNoteSuccess(Object count) {
    return 'Successfully set note for $count transactions';
  }

  @override
  String searchBatchSetNoteFailed(Object error) {
    return 'Set note failed: $error';
  }

  @override
  String searchBatchChangeCategorySuccess(Object count) {
    return 'Successfully changed category for $count transactions';
  }

  @override
  String searchBatchChangeCategoryFailed(Object error) {
    return 'Change category failed: $error';
  }

  @override
  String searchResultsCount(Object count) {
    return '$count results';
  }

  @override
  String get searchSummaryIncome => 'Income';

  @override
  String get searchSummaryExpense => 'Expense';

  @override
  String get searchFilterTitle => 'Filter';

  @override
  String get searchAmountFilter => 'Amount Filter';

  @override
  String get searchDateFilter => 'Date Filter';

  @override
  String get searchStartDate => 'Start Date';

  @override
  String get searchEndDate => 'End Date';

  @override
  String get searchNotSet => 'Not Set';

  @override
  String get searchClearFilter => 'Clear Filter';

  @override
  String get searchBatchCategoryTransferError => 'Selected transactions contain transfers, cannot change category';

  @override
  String get searchBatchCategoryTypeError => 'Selected transactions have different types, please select all income or all expense';

  @override
  String get searchDateStart => 'Start';

  @override
  String get searchDateEnd => 'End';

  @override
  String get analyticsMonth => 'Month';

  @override
  String get analyticsYear => 'Year';

  @override
  String get analyticsAll => 'All';

  @override
  String get analyticsCategoryRanking => 'Category Ranking';

  @override
  String get analyticsTotalAmount => 'Total';

  @override
  String get analyticsNoDataSubtext => 'Swipe left/right to switch periods, or tap button to toggle income/expense';

  @override
  String get analyticsSwipeHint => 'Swipe left/right to change period';

  @override
  String analyticsSwitchTo(String type) {
    return 'Switch to $type';
  }

  @override
  String get analyticsTipHeader => 'Tip: Top capsule can switch Month/Year/All';

  @override
  String get analyticsSwipeToSwitch => 'Swipe to switch';

  @override
  String get analyticsAllYears => 'All Years';

  @override
  String get analyticsToday => 'Today';

  @override
  String get splashAppName => 'Bee Accounting';

  @override
  String get splashSlogan => 'Record Every Drop';

  @override
  String get splashSecurityTitle => 'Open Source Data Security';

  @override
  String get splashSecurityFeature1 => '• Local data storage, complete privacy control';

  @override
  String get splashSecurityFeature2 => '• Open source code transparency, trustworthy security';

  @override
  String get splashSecurityFeature3 => '• Optional cloud sync, consistent data across devices';

  @override
  String get splashInitializing => 'Initializing data...';

  @override
  String get ledgersTitle => 'Ledger Management';

  @override
  String get ledgersNew => 'New Ledger';

  @override
  String get ledgersClear => 'Clear Ledger';

  @override
  String ledgersClearMessage(Object name) {
    return 'Are you sure to clear all transactions in ledger \"$name\"? This action cannot be undone.\\nThe ledger will be kept, only transaction data will be deleted.';
  }

  @override
  String get ledgerDefaultName => 'Default Ledger';

  @override
  String get ledgersEdit => 'Edit Ledger';

  @override
  String get ledgersDelete => 'Delete Ledger';

  @override
  String get ledgersDeleteConfirm => 'Delete Ledger';

  @override
  String get ledgersDeleteMessage => 'Are you sure you want to delete this ledger and all its records? This action cannot be undone.\\nIf there is a backup in the cloud, it will also be deleted.';

  @override
  String get ledgersDeleted => 'Deleted';

  @override
  String get ledgersDeleteFailed => 'Delete Failed';

  @override
  String get ledgersClearTitle => 'Clear Ledger';

  @override
  String get ledgersClearSuccess => 'Ledger cleared';

  @override
  String get ledgersDeleteLocal => 'Delete Local Ledger Only';

  @override
  String get ledgersDeleteLocalTitle => 'Delete Local Ledger';

  @override
  String ledgersDeleteLocalMessage(Object name) {
    return 'Are you sure to delete local ledger \"$name\"?\\nCloud backup will be kept and you can restore it anytime.';
  }

  @override
  String get ledgersDeleteLocalSuccess => 'Local ledger deleted';

  @override
  String get ledgersName => 'Name';

  @override
  String get ledgersDefaultLedgerName => 'Default Ledger';

  @override
  String get ledgersCurrency => 'Currency';

  @override
  String get ledgersMonthStartDay => 'Month start day';

  @override
  String get ledgersMonthStartDayHint => 'Statistics and budgets use this day (1-28) as the start of each monthly period';

  @override
  String get ledgersMonthStartDayNatural => '1st (calendar month)';

  @override
  String ledgersMonthStartDayValue(int day) {
    return 'Day $day of each month';
  }

  @override
  String get ledgersSelectCurrency => 'Select Currency';

  @override
  String get ledgersSearchCurrency => 'Search: Chinese or code';

  @override
  String get ledgersCreate => 'Create';

  @override
  String get ledgersActions => 'Actions';

  @override
  String ledgersRecords(String count) {
    return 'Records: $count';
  }

  @override
  String ledgersBalance(String balance) {
    return 'Balance: $balance';
  }

  @override
  String get ledgerCardDownloadCloud => 'Download from Cloud';

  @override
  String get ledgersLocal => 'Local Ledgers';

  @override
  String get ledgersRemote => 'Cloud Ledgers';

  @override
  String get ledgersEmpty => 'No ledgers';

  @override
  String get ledgersRestoreAll => 'Restore All';

  @override
  String ledgersSwitched(String name) {
    return 'Switched to ledger \"$name\"';
  }

  @override
  String get ledgersDownloadTitle => 'Download Ledger';

  @override
  String ledgersDownloadMessage(String name) {
    return 'Confirm download ledger \"$name\" to local?';
  }

  @override
  String get ledgersDownloading => 'Downloading...';

  @override
  String ledgersDownloadSuccess(String name) {
    return 'Ledger \"$name\" downloaded successfully';
  }

  @override
  String get ledgersDownload => 'Download';

  @override
  String get ledgersDeleteRemote => 'Delete Cloud Ledger';

  @override
  String get ledgersDeleteRemoteConfirm => 'Delete Cloud Ledger';

  @override
  String ledgersDeleteRemoteMessage(String name) {
    return 'Confirm delete cloud ledger \"$name\"? This action cannot be undone.';
  }

  @override
  String get ledgersDeleting => 'Deleting...';

  @override
  String get ledgersDeleteRemoteSuccess => 'Cloud ledger deleted';

  @override
  String get ledgersCannotDeleteLastOne => 'Cannot delete the last ledger';

  @override
  String get ledgersRestoreAllTitle => 'Batch Restore';

  @override
  String ledgersRestoreAllMessage(int count) {
    return 'Confirm restore all cloud ledgers? Total $count.';
  }

  @override
  String get ledgersRestoring => 'Restoring...';

  @override
  String get ledgersRestoreComplete => 'Restore Complete';

  @override
  String ledgersRestoreResult(int success, int failed) {
    return 'Success: $success, Failed: $failed';
  }

  @override
  String get categoryTitle => 'Category Management';

  @override
  String get categoryIconGroupBasic => 'Basics';

  @override
  String get categoryIconGroupFoodDining => 'Food & Dining';

  @override
  String get categoryIconGroupTransportation => 'Transportation';

  @override
  String get categoryIconGroupShopping => 'Shopping';

  @override
  String get categoryIconGroupHomeLiving => 'Home & Living';

  @override
  String get categoryIconGroupCommunicationDevices => 'Communication & Devices';

  @override
  String get categoryIconGroupEntertainmentLeisure => 'Entertainment & Leisure';

  @override
  String get categoryIconGroupHealthMedical => 'Health & Medicine';

  @override
  String get categoryIconGroupEducationLearning => 'Education & Learning';

  @override
  String get categoryIconGroupPetsAnimals => 'Pets & Animals';

  @override
  String get categoryIconGroupClothingBeauty => 'Clothing & Beauty';

  @override
  String get categoryIconGroupOtherMiscellaneous => 'Other';

  @override
  String get categoryIconGroupWorkCareer => 'Work & Career';

  @override
  String get categoryIconGroupFinance => 'Finance';

  @override
  String get categoryIconGroupRewardsGifts => 'Rewards & Gifts';

  @override
  String get categoryIconGroupInvestmentReturns => 'Investment Returns';

  @override
  String get categoryIconGroupOtherIncome => 'Other Income';

  @override
  String get categoryGenerateDefault => 'Generate Default Categories';

  @override
  String get categoryGenerateDefaultFlat => 'Level 1 categories';

  @override
  String get categoryGenerateDefaultHierarchical => 'Level 2 categories';

  @override
  String get categoryGenerateDefaultHint => 'Existing categories are kept. Default categories with matching names are skipped.';

  @override
  String categoryGenerateDefaultSuccess(int count) {
    return 'Generated $count default categories';
  }

  @override
  String get categoryGenerateDefaultNoChanges => 'Default categories already exist';

  @override
  String get categoryGenerateDefaultFailed => 'Could not generate default categories. Try again.';

  @override
  String get categoryNew => 'New Category';

  @override
  String get categoryExpense => 'Expense';

  @override
  String get categoryIncome => 'Income';

  @override
  String get categoryEmpty => 'No categories';

  @override
  String get categoryDefault => 'Default Category';

  @override
  String get categoryReorderTip => 'Long press to drag and reorder categories';

  @override
  String categoryLoadFailed(String error) {
    return 'Load failed: $error';
  }

  @override
  String get iconPickerTitle => 'Select Icon';

  @override
  String get iconCategoryTransport => 'Transport';

  @override
  String get iconCategoryShopping => 'Shopping';

  @override
  String get iconCategoryEntertainment => 'Entertainment';

  @override
  String get iconCategoryLife => 'Life';

  @override
  String get iconCategoryHealth => 'Health';

  @override
  String get iconCategoryEducation => 'Education';

  @override
  String get iconCategoryWork => 'Work';

  @override
  String get iconCategoryFinance => 'Finance';

  @override
  String get iconCategoryReward => 'Reward';

  @override
  String get iconCategoryOther => 'Other';

  @override
  String get iconCategoryDining => 'Dining';

  @override
  String get importTitle => 'Import Bills';

  @override
  String get importBillType => 'Bill Type';

  @override
  String get importBillTypeGeneric => 'Generic CSV';

  @override
  String get importBillTypeAlipay => 'Alipay';

  @override
  String get importBillTypeWechat => 'WeChat';

  @override
  String get importChooseFile => 'Choose File';

  @override
  String get importNoFileSelected => 'No file selected';

  @override
  String get importHint => 'Tip: Please select a file to start importing (CSV/TSV/XLSX)';

  @override
  String get importReading => 'Reading file…';

  @override
  String get importPreparing => 'Preparing…';

  @override
  String importColumnNumber(Object number) {
    return 'Column $number';
  }

  @override
  String get importConfirmMapping => 'Confirm Mapping';

  @override
  String get importCategoryMapping => 'Category Mapping';

  @override
  String get importNoDataParsed => 'No data parsed. Please return to previous page to check CSV content or separator.';

  @override
  String get importFieldDate => 'Date';

  @override
  String get importFieldType => 'Type';

  @override
  String get importFieldAmount => 'Amount';

  @override
  String get importFieldCategory => 'Category';

  @override
  String get importFieldAccount => 'Account';

  @override
  String get importFieldNote => 'Note';

  @override
  String get importPreview => 'Data Preview';

  @override
  String importPreviewLimit(Object shown, Object total) {
    return 'Showing first $shown of $total records';
  }

  @override
  String get importCategoryNotSelected => 'Category not selected';

  @override
  String get importCategoryMappingDescription => 'Please select corresponding local categories for each category name:';

  @override
  String get importKeepOriginalName => 'Keep original name';

  @override
  String importProgress(Object fail, Object ok) {
    return 'Importing, success: $ok, failed: $fail';
  }

  @override
  String get importCancelImport => 'Cancel Import';

  @override
  String get importCompleteTitle => 'Import Complete';

  @override
  String get importSelectCategoryFirst => 'Please select category mapping first';

  @override
  String get importNextStep => 'Next Step';

  @override
  String get importPreviousStep => 'Previous Step';

  @override
  String get importStartImport => 'Start Import';

  @override
  String get importAutoDetect => 'Auto Detect';

  @override
  String get importInProgress => 'Import in Progress';

  @override
  String importProgressDetail(Object done, Object fail, Object ok, Object total) {
    return 'Imported $done / $total records, success $ok, failed $fail';
  }

  @override
  String get importBackgroundImport => 'Background Import';

  @override
  String get importCancelled => 'Import Cancelled';

  @override
  String importCompleted(Object cancelled, Object fail, Object ok) {
    return 'Import Completed$cancelled, success $ok, failed $fail';
  }

  @override
  String importSkippedNonTransactionTypes(Object count) {
    return 'Skipped $count non-transaction records (debts, etc.)';
  }

  @override
  String importTransactionFailed(Object error) {
    return 'Import failed, all changes have been rolled back: $error';
  }

  @override
  String importFileOpenError(String error) {
    return 'Unable to open file picker: $error';
  }

  @override
  String get mineTitle => 'Mine';

  @override
  String get mineReminder => 'Reminder Settings';

  @override
  String get mineImport => 'Import Data';

  @override
  String get mineExport => 'Export Data';

  @override
  String get mineLanguageSettings => 'Language';

  @override
  String get languageTitle => 'Language Settings';

  @override
  String get languageChinese => '中文';

  @override
  String get languageEnglish => 'English';

  @override
  String get languageSystemDefault => 'Follow System';

  @override
  String get deleteConfirmTitle => 'Delete Confirmation';

  @override
  String get deleteConfirmMessage => 'Are you sure you want to delete this record?';

  @override
  String get mineSlogan => 'Bee Accounting, Every Penny Counts';

  @override
  String get mineDisplayNameEditTitle => 'Set nickname';

  @override
  String get mineDisplayNameHint => 'Enter a nickname';

  @override
  String get mineDisplayNameSaved => 'Nickname updated';

  @override
  String get mineGreetingMorning => 'Good morning';

  @override
  String get mineGreetingNoon => 'Good noon';

  @override
  String get mineGreetingAfternoon => 'Good afternoon';

  @override
  String get mineGreetingEvening => 'Good evening';

  @override
  String get mineGreetingNight => 'Good night';

  @override
  String mineGreetingNamed(String greeting, String name) {
    return '$greeting, $name';
  }

  @override
  String get mineProfileEditTitle => 'Edit profile';

  @override
  String get mineAvatarTitle => 'Avatar Settings';

  @override
  String get mineAvatarFromGallery => 'Choose from Gallery';

  @override
  String get mineAvatarFromCamera => 'Take Photo';

  @override
  String get mineAvatarDelete => 'Delete Avatar';

  @override
  String get annualReportTitle => 'Annual Report';

  @override
  String annualReportSubtitle(int year) {
    return 'Review your $year financial journey';
  }

  @override
  String get annualReportEntrySubtitle => 'Create your personalized annual report to share';

  @override
  String annualReportNoData(int year) {
    return 'No data for $year';
  }

  @override
  String get annualReportPage1Title => 'Annual Overview';

  @override
  String annualReportPage1Subtitle(int year) {
    return 'Your $year bookkeeping journey';
  }

  @override
  String get annualReportTotalDays => 'Days Tracked';

  @override
  String get annualReportTotalRecords => 'Total Records';

  @override
  String get annualReportTotalIncome => 'Total Income';

  @override
  String get annualReportTotalExpense => 'Total Expense';

  @override
  String get annualReportNetSavings => 'Net Savings';

  @override
  String get annualReportPage2Title => 'Expense Analysis';

  @override
  String get annualReportPage2Subtitle => 'Where your money went';

  @override
  String get annualReportPage3Title => 'Monthly Trends';

  @override
  String get annualReportPage3Subtitle => '12 months of income & expense';

  @override
  String get annualReportHighestMonth => 'Highest Spending Month';

  @override
  String get annualReportLowestMonth => 'Lowest Spending Month';

  @override
  String get annualReportPage4Title => 'Special Moments';

  @override
  String get annualReportPage4Subtitle => 'Memorable transactions';

  @override
  String get annualReportLargestExpense => 'Largest Expense';

  @override
  String get annualReportLargestIncome => 'Largest Income';

  @override
  String get annualReportFirstRecord => 'First Record';

  @override
  String get annualReportPage5Title => 'Achievements';

  @override
  String get annualReportPage5Subtitle => 'Your bookkeeping badges';

  @override
  String get annualReportAchievementConsistent => 'Consistent';

  @override
  String annualReportAchievementConsistentDesc(int days) {
    return 'Tracked for over $days days straight';
  }

  @override
  String get annualReportAchievementSaver => 'Saver';

  @override
  String get annualReportAchievementSaverDesc => 'Positive net savings this year';

  @override
  String get annualReportAchievementDetail => 'Detail-Oriented';

  @override
  String annualReportAchievementDetailDesc(int count) {
    return 'Over $count records tracked';
  }

  @override
  String get annualReportShareButton => 'Generate Share Poster';

  @override
  String get annualReportGenerating => 'Generating annual report...';

  @override
  String get annualReportSaveSuccess => 'Annual report poster saved';

  @override
  String get mineShareApp => 'Share App';

  @override
  String get mineShareWithFriends => 'Share BeeCount with friends';

  @override
  String get mineCopyPromoText => 'Copy Promo Text';

  @override
  String get mineCopyPromoSubtitle => 'Copy and share with friends';

  @override
  String get mineShareGenerating => 'Generating share poster...';

  @override
  String get sharePosterAppName => 'BeeCount';

  @override
  String get sharePosterSlogan => 'Smart Accounting, Beautiful Life';

  @override
  String get sharePosterFeature1 => 'Data Security·You Control';

  @override
  String get sharePosterFeature2 => 'Open Source·Auditable';

  @override
  String get sharePosterFeature3 => 'AI Smart·Photo & Voice';

  @override
  String get sharePosterFeature4 => 'Photo Accounting·Auto Recognition';

  @override
  String get sharePosterFeature5 => 'Multi Ledger·Dark Mode';

  @override
  String get sharePosterFeature6 => 'Self-Hosted Cloud·Free Forever';

  @override
  String get sharePosterScanText => 'Scan to visit open source project';

  @override
  String get appPromoTagOpenSource => 'Open Source';

  @override
  String get appPromoTagFree => 'Free';

  @override
  String get appPromoFooterText => 'Track every penny, trace every moment';

  @override
  String userProfileJourneyYears(int years) {
    return 'Bookkeeping Pro $years Years';
  }

  @override
  String get userProfileJourneyOneYear => 'One Year of Bookkeeping';

  @override
  String get userProfileJourneyHalfYear => 'Six Months Strong';

  @override
  String get userProfileJourneyThreeMonths => 'Three Months In';

  @override
  String get userProfileJourneyOneMonth => 'One Month Milestone';

  @override
  String get userProfileJourneyOneWeek => 'First Week Done';

  @override
  String get userProfileJourneyStart => 'Starting the Journey';

  @override
  String get userProfileDailyAverage => 'Daily Average';

  @override
  String get sharePosterSave => 'Save to Gallery';

  @override
  String get sharePosterShare => 'Share';

  @override
  String get sharePosterHideIncome => 'Hide Income';

  @override
  String get sharePosterShowIncome => 'Show Income';

  @override
  String get sharePosterSaveSuccess => 'Saved to gallery';

  @override
  String get shareGuidanceCopyText => 'Track my expenses with BeeCount - open source, free & ad-free! 🐝 Download: https://github.com/TNT-Likely/BeeCount';

  @override
  String get shareGuidanceCopied => 'Text copied';

  @override
  String get sharePosterSaveFailed => 'Failed to save';

  @override
  String get sharePosterPermissionDenied => 'Gallery permission denied, please enable in settings';

  @override
  String get sharePosterGenerating => 'Generating...';

  @override
  String get sharePosterGenerateFailed => 'Failed to generate poster, please try again';

  @override
  String get sharePosterNoLedger => 'Please select a ledger first';

  @override
  String get sharePosterYearTitle => 'My Annual Bookkeeping Report';

  @override
  String get sharePosterYearSubtitle => 'Record life with data, plan future with reason';

  @override
  String get sharePosterMonthTitle => 'Monthly Bill Report';

  @override
  String get sharePosterMonthSubtitle => 'Budget Wisely, Spend Rationally';

  @override
  String get sharePosterLedgerTitle => 'Ledger Statistics Report';

  @override
  String get sharePosterRecordDays => 'Record Days';

  @override
  String get sharePosterRecordCount => 'Record Count';

  @override
  String get sharePosterTotalExpense => 'Total Expense';

  @override
  String get sharePosterTotalIncome => 'Total Income';

  @override
  String get sharePosterYearBalance => 'Annual Balance';

  @override
  String get sharePosterYearDeficit => 'Annual Deficit';

  @override
  String get sharePosterMonthBalance => 'Monthly Balance';

  @override
  String get sharePosterBalance => 'Total Balance';

  @override
  String get sharePosterAvgMonthlyExpense => 'Avg. Monthly Expense';

  @override
  String get sharePosterAvgMonthlyIncome => 'Avg. Monthly Income';

  @override
  String get sharePosterAvgDailyExpense => 'Avg. Daily Expense';

  @override
  String get sharePosterMaxExpenseMonth => 'Highest Expense Month';

  @override
  String get sharePosterTopExpense => 'TOP 3 Expenses';

  @override
  String get sharePosterCompareLastMonth => 'vs Last Month';

  @override
  String get sharePosterIncreaseRate => 'Increase';

  @override
  String get sharePosterDecreaseRate => 'Decrease';

  @override
  String get sharePosterSavedMoneyTitle => 'Congrats! You saved this month';

  @override
  String get sharePosterLedgerName => 'Ledger Name';

  @override
  String get sharePosterUnitDay => 'days';

  @override
  String get sharePosterUnitCount => '';

  @override
  String get sharePosterUnitYuan => '';

  @override
  String userProfilePosterStartDate(String date) {
    return 'Bookkeeping since $date';
  }

  @override
  String get userProfilePosterRecordDays => 'Days';

  @override
  String get userProfilePosterDaysUnit => 'days';

  @override
  String get userProfilePosterRecordCount => 'Records';

  @override
  String get userProfilePosterCountUnit => '';

  @override
  String get userProfilePosterLedgerCount => 'Ledgers';

  @override
  String get userProfilePosterLedgerUnit => '';

  @override
  String get mineDaysCount => 'Days';

  @override
  String get mineTotalRecords => 'Records';

  @override
  String get mineCurrentBalance => 'Ledger balance';

  @override
  String get mineLoggedInEmail => 'Logged in';

  @override
  String get mineLogoutSubtitle => 'Tap to logout';

  @override
  String get mineLogoutConfirmTitle => 'Logout';

  @override
  String get mineLogoutConfirmMessage => 'Are you sure you want to logout?\nYou won\'t be able to use cloud sync after logout.';

  @override
  String get mineLogoutButton => 'Logout';

  @override
  String get mineImportProgressTitle => 'Importing in background...';

  @override
  String mineImportProgressSubtitle(Object done, Object fail, Object ok, Object total) {
    return 'Progress: $done/$total, Success $ok, Failed $fail';
  }

  @override
  String get mineImportCompleteTitle => 'Import complete';

  @override
  String get mineCategoryManagement => 'Category Management';

  @override
  String get mineCategoryManagementSubtitle => 'Edit custom categories';

  @override
  String get mineCategoryMigration => 'Category Migration';

  @override
  String get mineCategoryMigrationSubtitle => 'Migrate category data to other categories';

  @override
  String get mineRecurringTransactions => 'Recurring Bills';

  @override
  String get mineRecurringTransactionsSubtitle => 'Manage recurring bills';

  @override
  String get mineReminderSettings => 'Reminder Settings';

  @override
  String get mineReminderSettingsSubtitle => 'Set daily recording reminders';

  @override
  String get minePersonalize => 'Personalization';

  @override
  String get mineDisplayScale => 'Display Scale';

  @override
  String get mineDisplayScaleSubtitle => 'Adjust text and UI element sizes';

  @override
  String get mineFeedback => 'Feedback';

  @override
  String get mineFeedbackSubtitle => 'Report issues or suggestions';

  @override
  String get mineHelp => 'Help';

  @override
  String get helpCenterOpenInBrowser => 'Open in browser';

  @override
  String get helpCenterLoadFailed => 'Failed to load. Please check your network.';

  @override
  String get helpCenterRetry => 'Retry';

  @override
  String get mineHelpSubtitle => 'View documentation and FAQ';

  @override
  String get mineSupportAuthor => 'Star the Project ⭐️';

  @override
  String mineSupportAuthorSubtitle(String count) {
    return 'Open source, $count stars';
  }

  @override
  String get categoryEditTitle => 'Edit Category';

  @override
  String get categoryNewTitle => 'New Category';

  @override
  String get categoryDetailTooltip => 'Category Details';

  @override
  String get categoryMigrationTooltip => 'Category Migration';

  @override
  String get categoryMigrationTitle => 'Category Migration';

  @override
  String get categoryMigrationDescription => 'Category Migration Instructions';

  @override
  String get categoryMigrationDescriptionContent => '• Migrate all transaction records from one category to another\n• After migration, all transaction data from the source category will be transferred to the target category\n• This operation cannot be undone, please choose carefully';

  @override
  String get categoryMigrationTypeLabel => 'Select Type';

  @override
  String get categoryMigrationFromLabel => 'From Category';

  @override
  String get categoryMigrationFromHint => 'Select category to migrate from';

  @override
  String get categoryMigrationToLabel => 'To Category';

  @override
  String get categoryMigrationToHint => 'Select target category';

  @override
  String get categoryMigrationToHintFirst => 'Please select source category first';

  @override
  String get categoryMigrationStartButton => 'Start Migration';

  @override
  String get categoryMigrationCannotTitle => 'Cannot Migrate';

  @override
  String get categoryMigrationCannotMessage => 'Selected categories cannot be migrated, please check category status.';

  @override
  String get categoryExpenseType => 'Expense Category';

  @override
  String get categoryIncomeType => 'Income Category';

  @override
  String get categoryDefaultTitle => 'Default Category';

  @override
  String get categoryNameLabel => 'Category Name';

  @override
  String get categoryNameHint => 'Enter category name';

  @override
  String get categoryNameRequired => 'Please enter category name';

  @override
  String get categoryNameTooLong => 'Category name cannot exceed 4 characters';

  @override
  String get categoryNameDuplicate => 'Category name already exists';

  @override
  String get categoryIconLabel => 'Category Icon';

  @override
  String get categoryCustomIconTitle => 'Custom Icon';

  @override
  String get categoryCustomIconTapToSelect => 'Tap to select image';

  @override
  String get categoryCustomIconTapToChange => 'Tap to change image';

  @override
  String get categoryCustomIconError => 'Error selecting image';

  @override
  String get categoryCustomIconRequired => 'Please select a custom icon image';

  @override
  String get categoryCustomIconCrop => 'Crop Icon';

  @override
  String get categoryDangerousOperations => 'Dangerous Operations';

  @override
  String get categoryDeleteTitle => 'Delete Category';

  @override
  String get categoryDeleteSubtitle => 'Cannot be recovered after deletion';

  @override
  String get categorySaveError => 'Save failed';

  @override
  String categoryUpdated(Object name) {
    return 'Category \"$name\" updated';
  }

  @override
  String categoryCreated(Object name) {
    return 'Category \"$name\" created';
  }

  @override
  String get categoryCannotDelete => 'Cannot delete';

  @override
  String categoryCannotDeleteMessage(Object count) {
    return 'This category has $count transaction records. Please handle them first.';
  }

  @override
  String get categoryShare => 'Share Categories';

  @override
  String get categoryImport => 'Import Categories';

  @override
  String get categoryClearUnused => 'Clear Unused Categories';

  @override
  String get categoryClearUnusedTitle => 'Clear Unused Categories';

  @override
  String categoryClearUnusedMessage(Object count) {
    return 'Are you sure you want to delete $count unused categories? This action cannot be undone.';
  }

  @override
  String get categoryClearUnusedListTitle => 'Categories to be deleted:';

  @override
  String get categoryClearUnusedEmpty => 'No unused categories';

  @override
  String categoryClearUnusedSuccess(Object count) {
    return 'Deleted $count categories';
  }

  @override
  String get categoryClearUnusedFailed => 'Clear failed';

  @override
  String get categoryShareScopeTitle => 'Select Scope';

  @override
  String get categoryShareScopeExpense => 'Expense categories only';

  @override
  String get categoryShareScopeIncome => 'Income categories only';

  @override
  String get categoryShareScopeAll => 'All categories';

  @override
  String categoryShareSuccess(Object path) {
    return 'Saved to $path';
  }

  @override
  String get categoryShareSubject => 'BeeCount Category Configuration';

  @override
  String get categoryShareFailed => 'Share failed';

  @override
  String get categoryImportInvalidFile => 'Please select a category package file (.zip)';

  @override
  String get categoryImportModeTitle => 'Select Import Mode';

  @override
  String get categoryImportModeMerge => 'Merge';

  @override
  String get categoryImportModeMergeDesc => 'Keep existing, add new ones';

  @override
  String get categoryImportModeOverwrite => 'Overwrite';

  @override
  String get categoryImportModeOverwriteDesc => 'Clear unused categories then import';

  @override
  String get categoryImportSuccess => 'Import successful';

  @override
  String categoryImportSuccessDetail(int imported, int skipped, int icons) {
    return 'Imported $imported categories, skipped $skipped, imported $icons icons';
  }

  @override
  String get categoryImportFailed => 'Import failed';

  @override
  String get categoryDeleteConfirmTitle => 'Delete Category';

  @override
  String categoryDeleteConfirmMessage(Object name) {
    return 'Are you sure you want to delete category \"$name\"? This action cannot be undone.';
  }

  @override
  String get categoryDeleteError => 'Delete failed';

  @override
  String categoryDeleted(Object name) {
    return 'Category \"$name\" deleted';
  }

  @override
  String get categorySubCategoryTitle => 'Subcategory';

  @override
  String get categorySubCategoryDescriptionEnabled => 'This category belongs to a parent category';

  @override
  String get categorySubCategoryDescriptionDisabled => 'This is an independent top-level category';

  @override
  String get categoryParentCategoryTitle => 'Parent Category';

  @override
  String get categoryParentCategoryHint => 'Please select parent category';

  @override
  String get categorySelectParentTitle => 'Select Parent Category';

  @override
  String categorySubCategoryCreated(Object name) {
    return 'Subcategory added: $name';
  }

  @override
  String get categoryParentRequired => 'Please select parent category';

  @override
  String get categoryParentRequiredTitle => 'Error';

  @override
  String get categoryExpenseList => 'Dining-Transport-Shopping-Entertainment-Home-Family-Communication-Utilities-Housing-Medical-Education-Pets-Sports-Digital-Travel-Alcohol & Tobacco-Baby Care-Beauty-Repair-Social-Learning-Car-Taxi-Subway-Delivery-Property-Parking-Donation-Give Gift-Tax-Beverage-Clothing-Snacks-Send Red Packet-Fruit-Game-Book-Lover-Decoration-Daily Goods-Lottery-Stock-Social Security-Express-Work';

  @override
  String get categoryIncomeList => 'Salary-Investment-Receive Red Packet-Bonus-Reimbursement-Part time-Receive Gift-Interest-Refund-Investment Income-Second hand-Social Benefit-Tax Refund-Provident Fund';

  @override
  String get categoryExpenseDining => 'Dining-Breakfast-Lunch-Dinner-Meituan Delivery-Ele.me Delivery-JD Delivery-Restaurant-Food';

  @override
  String get categoryExpenseSnacks => 'Snacks-Cookies-Chips-Candy-Chocolate-Nuts';

  @override
  String get categoryExpenseFruit => 'Fruit-Apple-Banana-Orange-Grape-Watermelon-Other Fruits';

  @override
  String get categoryExpenseBeverage => 'Beverage-Milk Tea-Coffee-Juice-Soda-Mineral Water';

  @override
  String get categoryExpensePastry => 'Pastry-Cake-Bread-Dessert-Baked Goods';

  @override
  String get categoryExpenseCooking => 'Cooking Ingredients-Vegetables-Meat-Seafood-Seasoning-Grain & Oil';

  @override
  String get categoryExpenseShopping => 'Shopping-Clothing-Shoes & Hats-Bags-Accessories-Daily Necessities';

  @override
  String get categoryExpensePets => 'Pets-Pet Food-Pet Supplies-Pet Medical-Pet Grooming';

  @override
  String get categoryExpenseTransport => 'Transport-Subway-Bus-Taxi-Ride hailing-Parking Fee-Fuel';

  @override
  String get categoryExpenseCar => 'Car-Car Maintenance-Car Repair-Car Insurance-Car Wash-Traffic Fine';

  @override
  String get categoryExpenseClothing => 'Apparel-Top-Pants-Dress-Shoes-Apparel Accessories';

  @override
  String get categoryExpenseDailyGoods => 'Daily Goods-Personal Care-Paper Products-Cleaning Supplies-Kitchen Supplies';

  @override
  String get categoryExpenseEducation => 'Education-Tuition-Training Fee-Books-Stationery-Office Supplies';

  @override
  String get categoryExpenseInvestLoss => 'Investment Loss-Stock Loss-Fund Loss-Other Investment Loss';

  @override
  String get categoryExpenseEntertainment => 'Entertainment-Movie-KTV-Amusement Park-Bar-Other Entertainment';

  @override
  String get categoryExpenseGame => 'Game-Game Top up-Game Equipment-Game Membership';

  @override
  String get categoryExpenseHealthProducts => 'Health Products-Vitamins-Health Food-Nutritional Supplements';

  @override
  String get categoryExpenseSubscription => 'Subscription-Video Membership-Music Membership-Cloud Storage-Other Subscription';

  @override
  String get categoryExpenseSports => 'Sports-Gym-Sports Equipment-Sports Course-Outdoor Activity';

  @override
  String get categoryExpenseHousing => 'Housing-Rent-Property Fee-Mortgage-Renovation';

  @override
  String get categoryExpenseHome => 'Home-Furniture-Appliances-Decorations-Bedding';

  @override
  String get categoryExpenseBeauty => 'Beauty-Skincare-Cosmetics-Beauty Salon-Nail Care';

  @override
  String get categoryIncomeSalary => 'Salary-Base Salary-Performance Bonus-Year end Bonus-Overtime Pay';

  @override
  String get categoryIncomeInvestment => 'Investment-Fund Earnings-Stock Dividend-Wealth Management-Other Wealth Management';

  @override
  String get categoryIncomeRedPacket => 'Red Packet-Holiday Red Packet-Birthday Red Packet-Return Gift';

  @override
  String get categoryIncomeBonus => 'Bonus-Annual Bonus-Quarterly Bonus-Project Bonus-Other Bonus';

  @override
  String get categoryIncomeReimbursement => 'Reimbursement-Travel Reimbursement-Meal Reimbursement-Other Reimbursement';

  @override
  String get categoryIncomePartTime => 'Part time-Part time Income-Side Income';

  @override
  String get categoryIncomeGift => 'Gift-Wedding Gift-Birthday Gift-Other Gift';

  @override
  String get categoryIncomeInterest => 'Interest-Bank Interest-Other Interest';

  @override
  String get categoryIncomeRefund => 'Refund-Shopping Refund-Service Refund-Other Refund';

  @override
  String get categoryIncomeInvestIncome => 'Investment Income-Stock Earnings-Fund Investment-Other Investment Income';

  @override
  String get categoryIncomeSecondHand => 'Second hand-Idle Items-Second hand Goods';

  @override
  String get categoryIncomeSocialBenefit => 'Social Benefit-Unemployment Insurance-Maternity Subsidy-Other Subsidy';

  @override
  String get categoryIncomeTaxRefund => 'Tax Refund-Individual Tax Refund-Other Tax Refund';

  @override
  String get categoryIncomeProvidentFund => 'Provident Fund-Provident Fund Withdrawal-Provident Fund Interest';

  @override
  String get personalizeTitle => 'Theme color';

  @override
  String get personalizeSubtitle => 'Pick or customize the app accent color';

  @override
  String get personalizeCustomColor => 'Choose custom color';

  @override
  String get personalizeCustomTitle => 'Custom';

  @override
  String personalizeHue(Object value) {
    return 'Hue ($value°)';
  }

  @override
  String personalizeSaturation(Object value) {
    return 'Saturation ($value%)';
  }

  @override
  String personalizeBrightness(Object value) {
    return 'Brightness ($value%)';
  }

  @override
  String get personalizeSelectColor => 'Select this color';

  @override
  String get appearanceThemeMode => 'Appearance';

  @override
  String get appearanceThemeModeSystem => 'Follow System';

  @override
  String get appearanceThemeModeLight => 'Light Mode';

  @override
  String get appearanceThemeModeDark => 'Dark Mode';

  @override
  String get appearanceDarkModePattern => 'Dark Mode Header Pattern';

  @override
  String get appearancePatternNone => 'None';

  @override
  String get appearancePatternIcons => 'Icon Tiling';

  @override
  String get appearancePatternParticles => 'Particles';

  @override
  String get appearancePatternHoneycomb => 'Honeycomb';

  @override
  String get appearanceAmountFormat => 'Balance Display Format';

  @override
  String get appearanceAmountFormatFull => 'Full Amount';

  @override
  String get appearanceAmountFormatFullDesc => 'Show full amount, e.g. 123,456.78';

  @override
  String get appearanceAmountFormatCompact => 'Compact';

  @override
  String get appearanceAmountFormatCompactDesc => 'Abbreviate large amounts, e.g. 12.3K (only affects account balance)';

  @override
  String get appearanceShowTransactionTime => 'Show Transaction Time';

  @override
  String get appearanceShowTransactionTimeDesc => 'Display time in transaction list, allow time selection when editing';

  @override
  String get appearanceNoteDisplay => 'Note display';

  @override
  String get appearanceNoteDisplayCategory => 'Category first';

  @override
  String get appearanceNoteDisplayCategoryDesc => 'Show category, note in parentheses';

  @override
  String get appearanceNoteDisplayNote => 'Note first';

  @override
  String get appearanceNoteDisplayNoteDesc => 'Show note when present, else category';

  @override
  String get appearanceNoteHistory => 'Note history';

  @override
  String get appearanceNoteHistoryScope => 'Show';

  @override
  String get appearanceNoteHistoryScopeAllCategories => 'All categories';

  @override
  String get appearanceNoteHistoryScopeCurrentCategory => 'Current category';

  @override
  String get appearanceNoteHistorySort => 'Sort by';

  @override
  String get appearanceNoteHistorySortFrequency => 'Usage frequency';

  @override
  String get appearanceNoteHistorySortRecent => 'Most recent';

  @override
  String get appearanceNoteHistoryLimit => 'Display limit';

  @override
  String get appearanceNoteHistoryLimitHint => 'Enter a value from 1 to 100';

  @override
  String get appearanceNoteHistoryLimitInvalid => 'Enter an integer from 1 to 100';

  @override
  String get appearanceColorScheme => 'Income/Expense Color Scheme';

  @override
  String get appearanceColorSchemeOn => 'Red for income · Green for expense';

  @override
  String get appearanceColorSchemeOff => 'Red for expense · Green for income';

  @override
  String get appearanceColorSchemeOnDesc => 'Red represents income, green represents expense';

  @override
  String get appearanceColorSchemeOffDesc => 'Red represents expense, green represents income';

  @override
  String fontSettingsCurrentScale(Object scale) {
    return 'Current scale: x$scale';
  }

  @override
  String get fontSettingsPreview => 'Live Preview';

  @override
  String get fontSettingsPreviewText => 'Spent 23.50 on lunch today, record it;\nRecorded for 45 days this month, 320 entries;\nPersistence is victory!';

  @override
  String fontSettingsCurrentLevel(Object level, Object scale) {
    return 'Current level: $level (scale x$scale)';
  }

  @override
  String get fontSettingsQuickLevel => 'Quick Levels';

  @override
  String get fontSettingsCustomAdjust => 'Custom Adjustment';

  @override
  String get fontSettingsDescription => 'Note: This setting ensures consistent display at 1.0x across all devices, with device differences auto-compensated; adjust values for personalized scaling on this consistent base.';

  @override
  String get fontSettingsExtraSmall => 'Extra Small';

  @override
  String get fontSettingsVerySmall => 'Very Small';

  @override
  String get fontSettingsSmall => 'Small';

  @override
  String get fontSettingsStandard => 'Standard';

  @override
  String get fontSettingsLarge => 'Large';

  @override
  String get fontSettingsBig => 'Big';

  @override
  String get fontSettingsVeryBig => 'Very Big';

  @override
  String get fontSettingsExtraBig => 'Extra Big';

  @override
  String get fontSettingsMoreStyles => 'More Styles';

  @override
  String get fontSettingsPageTitle => 'Page Title';

  @override
  String get fontSettingsBlockTitle => 'Block Title';

  @override
  String get fontSettingsBodyExample => 'Body Text';

  @override
  String get fontSettingsLabelExample => 'Label Text';

  @override
  String get fontSettingsStrongNumber => 'Strong Number';

  @override
  String get fontSettingsListTitle => 'List Item Title';

  @override
  String get fontSettingsListSubtitle => 'Helper Text';

  @override
  String get fontSettingsScreenInfo => 'Screen Adaptation Info';

  @override
  String get fontSettingsScreenDensity => 'Screen Density';

  @override
  String get fontSettingsScreenWidth => 'Screen Width';

  @override
  String get fontSettingsDeviceScale => 'Device Scale';

  @override
  String get fontSettingsUserScale => 'User Scale';

  @override
  String get fontSettingsFinalScale => 'Final Scale';

  @override
  String get fontSettingsBaseDevice => 'Base Device';

  @override
  String get fontSettingsRecommendedScale => 'Recommended Scale';

  @override
  String get fontSettingsYes => 'Yes';

  @override
  String get fontSettingsNo => 'No';

  @override
  String get fontSettingsScaleExample => 'This box and spacing auto-scale based on device';

  @override
  String get fontSettingsPreciseAdjust => 'Precise Adjustment';

  @override
  String get fontSettingsResetTo1x => 'Reset to 1.0x';

  @override
  String get fontSettingsAdaptBase => 'Adapt to Base';

  @override
  String get reminderTitle => 'Recording Reminder';

  @override
  String get reminderSubtitle => 'Set daily recording reminder time';

  @override
  String get reminderDailyTitle => 'Daily Recording Reminder';

  @override
  String get reminderDailySubtitle => 'When enabled, will remind you to record at specified time';

  @override
  String get reminderDailyBody => 'Don\'t forget to record today\'s income and expenses 💰';

  @override
  String get reminderTimeTitle => 'Reminder Time';

  @override
  String get commonSelectTime => 'Select Time';

  @override
  String get reminderTestNotification => 'Send Test Notification';

  @override
  String get reminderTestSent => 'Test notification sent';

  @override
  String get reminderTestTitle => 'Test Notification';

  @override
  String get reminderTestBody => 'This is a test notification, tap to see the effect';

  @override
  String get reminderCheckBattery => 'Check Battery Optimization Status';

  @override
  String get reminderBatteryStatus => 'Battery Optimization Status';

  @override
  String reminderManufacturer(Object value) {
    return 'Manufacturer: $value';
  }

  @override
  String reminderModel(Object value) {
    return 'Model: $value';
  }

  @override
  String reminderAndroidVersion(Object value) {
    return 'Android Version: $value';
  }

  @override
  String get reminderBatteryIgnored => 'Battery optimization: Ignored ✅';

  @override
  String get reminderBatteryNotIgnored => 'Battery optimization: Not ignored ⚠️';

  @override
  String get reminderBatteryAdvice => 'Recommend disabling battery optimization for proper notifications';

  @override
  String get reminderCheckChannel => 'Check Notification Channel Settings';

  @override
  String get reminderChannelStatus => 'Notification Channel Status';

  @override
  String get reminderChannelEnabled => 'Channel enabled: Yes ✅';

  @override
  String get reminderChannelDisabled => 'Channel enabled: No ❌';

  @override
  String reminderChannelImportance(Object value) {
    return 'Importance: $value';
  }

  @override
  String get reminderChannelSoundOn => 'Sound: On 🔊';

  @override
  String get reminderChannelSoundOff => 'Sound: Off 🔇';

  @override
  String get reminderChannelVibrationOn => 'Vibration: On 📳';

  @override
  String get reminderChannelVibrationOff => 'Vibration: Off';

  @override
  String get reminderChannelDndBypass => 'Do Not Disturb: Can bypass';

  @override
  String get reminderChannelDndNoBypass => 'Do Not Disturb: Cannot bypass';

  @override
  String get reminderChannelAdvice => '⚠️ Recommended settings:';

  @override
  String get reminderChannelAdviceImportance => '• Importance: Urgent or High';

  @override
  String get reminderChannelAdviceSound => '• Enable sound and vibration';

  @override
  String get reminderChannelAdviceBanner => '• Allow banner notifications';

  @override
  String get reminderChannelAdviceXiaomi => '• Xiaomi phones need individual channel setup';

  @override
  String get reminderChannelGood => '✅ Notification channel well configured';

  @override
  String get reminderOpenAppSettings => 'Open App Settings';

  @override
  String get reminderAppSettingsMessage => 'Please allow notifications and disable battery optimization in settings';

  @override
  String get reminderDescription => 'Tip: When recording reminder is enabled, the system will send notifications at the specified time daily to remind you to record income and expenses.';

  @override
  String get reminderIOSInstructions => '🍎 iOS notification settings:\n• Settings > Notifications > Bee Accounting\n• Enable \"Allow Notifications\"\n• Set notification style: Banner or Alert\n• Enable sound and vibration\n\n⚠️ Important Note:\n• iOS local notifications depend on app process\n• Do not force quit app from task manager\n• Notifications work when app is in background or foreground\n• Force quitting will disable notifications\n\n💡 Usage Tips:\n• Simply press Home button to exit app\n• iOS will manage background apps automatically\n• Keep app in background to receive reminders';

  @override
  String get reminderAndroidInstructions => 'If notifications don\'t work properly, check:\n• App is allowed to send notifications\n• Disable battery optimization/power saving for app\n• Allow app to run in background and auto-start\n• Android 12+ needs exact alarm permission\n\n📱 Xiaomi phone special settings:\n• Settings > App Management > Bee Accounting > Notification Management\n• Tap \"Recording Reminder\" channel\n• Set importance to \"Urgent\" or \"High\"\n• Enable \"Banner notifications\", \"Sound\", \"Vibration\"\n• Security Center > App Management > Permissions > Auto-start\n\n🔒 Lock background methods:\n• Find Bee Accounting in recent tasks\n• Pull down app card to show lock icon\n• Tap lock icon to prevent cleanup';

  @override
  String get categoryDetailLoadFailed => 'Load failed';

  @override
  String get categoryDetailSummaryTitle => 'Category Summary';

  @override
  String get categoryDetailTotalCount => 'Total Count';

  @override
  String get categoryDetailTotalAmount => 'Total Amount';

  @override
  String get categoryDetailAverageAmount => 'Average Amount';

  @override
  String get categoryDetailSortTitle => 'Sort';

  @override
  String get categoryDetailSortTimeDesc => 'Time ↓';

  @override
  String get categoryDetailSortTimeAsc => 'Time ↑';

  @override
  String get categoryDetailSortAmountDesc => 'Amount ↓';

  @override
  String get categoryDetailSortAmountAsc => 'Amount ↑';

  @override
  String get categoryDetailNoTransactions => 'No transactions';

  @override
  String get categoryDetailNoTransactionsSubtext => 'No transactions in this category yet';

  @override
  String get categoryDetailDeleteFailed => 'Delete failed';

  @override
  String get categoryMigrationConfirmTitle => 'Confirm Migration';

  @override
  String categoryMigrationConfirmMessage(Object count, Object fromName, Object toName) {
    return 'Migrate $count transactions from \"$fromName\" to \"$toName\"?\n\nThis operation cannot be undone!';
  }

  @override
  String get categoryMigrationConfirmOk => 'Confirm Migration';

  @override
  String get categoryMigrationCompleteTitle => 'Migration Complete';

  @override
  String categoryMigrationCompleteMessage(Object count, Object fromName, Object toName) {
    return 'Successfully migrated $count transactions from \"$fromName\" to \"$toName\".';
  }

  @override
  String get categoryMigrationFailedTitle => 'Migration Failed';

  @override
  String categoryMigrationFailedMessage(Object error) {
    return 'Migration error: $error';
  }

  @override
  String categoryMigrationTransactionLabel(int count) {
    return '$count records';
  }

  @override
  String get mineImportCompleteAllSuccess => 'All Success';

  @override
  String get importSelectCsvFile => 'Please select a file to import (CSV/TSV/XLSX supported)';

  @override
  String get exportTitle => 'Export';

  @override
  String get exportDescription => 'Supported export types:\n• Transactions (Income/Expense/Transfer)\n• Categories\n• Accounts\n\nClick the button below to select save location and export current ledger to CSV file.';

  @override
  String get exportButtonIOS => 'Export and Share';

  @override
  String get exportButtonAndroid => 'Export Data';

  @override
  String exportSavedTo(String path) {
    return 'Saved to: $path';
  }

  @override
  String get exportCsvHeaderType => 'Type';

  @override
  String get exportCsvHeaderCategory => 'Category';

  @override
  String get exportCsvHeaderSubCategory => 'Subcategory';

  @override
  String get exportCsvHeaderAmount => 'Amount';

  @override
  String get exportCsvHeaderAccount => 'Account';

  @override
  String get exportCsvHeaderFromAccount => 'From Account';

  @override
  String get exportCsvHeaderToAccount => 'To Account';

  @override
  String get exportCsvHeaderNote => 'Note';

  @override
  String get exportCsvHeaderTime => 'Time';

  @override
  String get exportCsvHeaderTags => 'Tags';

  @override
  String get exportCsvHeaderAttachments => 'Attachments';

  @override
  String get exportShareText => 'BeeCount Export File';

  @override
  String get exportSuccessTitle => 'Export Successful';

  @override
  String exportSuccessMessageIOS(String path) {
    return 'Saved and available in share history:\n$path';
  }

  @override
  String exportSuccessMessageAndroid(String path) {
    return 'Saved to:\n$path';
  }

  @override
  String get exportFailedTitle => 'Export Failed';

  @override
  String get exportTypeIncome => 'Income';

  @override
  String get exportTypeExpense => 'Expense';

  @override
  String get exportTypeTransfer => 'Transfer';

  @override
  String get personalizeThemeHoney => 'Bee Yellow';

  @override
  String get personalizeThemeOrange => 'Flame Orange';

  @override
  String get personalizeThemeGreen => 'Emerald Green';

  @override
  String get personalizeThemePurple => 'Purple Lotus';

  @override
  String get personalizeThemePink => 'Cherry Pink';

  @override
  String get personalizeThemeBlue => 'Sky Blue';

  @override
  String get personalizeThemeMint => 'Forest Moon';

  @override
  String get personalizeThemeSand => 'Sunset Dune';

  @override
  String get personalizeThemeLavender => 'Snow & Pine';

  @override
  String get personalizeThemeSky => 'Misty Wonderland';

  @override
  String get personalizeThemeWarmOrange => 'Warm Orange';

  @override
  String get personalizeThemeMintGreen => 'Mint Green';

  @override
  String get personalizeThemeRoseGold => 'Rose Gold';

  @override
  String get personalizeThemeDeepBlue => 'Deep Blue';

  @override
  String get personalizeThemeMapleRed => 'Maple Red';

  @override
  String get personalizeThemeEmerald => 'Emerald';

  @override
  String get personalizeThemeLavenderPurple => 'Lavender';

  @override
  String get personalizeThemeAmber => 'Amber';

  @override
  String get personalizeThemeRouge => 'Rouge Red';

  @override
  String get personalizeThemeIndigo => 'Indigo Blue';

  @override
  String get personalizeThemeOlive => 'Olive Green';

  @override
  String get personalizeThemeCoral => 'Coral Pink';

  @override
  String get personalizeThemeDarkGreen => 'Dark Green';

  @override
  String get personalizeThemeViolet => 'Violet';

  @override
  String get personalizeThemeSunset => 'Sunset Orange';

  @override
  String get personalizeThemePeacock => 'Peacock Blue';

  @override
  String get personalizeThemeLime => 'Lime Green';

  @override
  String get analyticsMonthlyAvg => 'Monthly Avg';

  @override
  String get analyticsDailyAvg => 'Daily Avg';

  @override
  String get analyticsOverallAvg => 'Overall Avg';

  @override
  String get analyticsTotalIncome => 'Total Income: ';

  @override
  String get analyticsTotalExpense => 'Total Expense: ';

  @override
  String get analyticsBalance => 'Balance: ';

  @override
  String analyticsAvgIncome(String avgLabel) {
    return '$avgLabel Income: ';
  }

  @override
  String analyticsAvgExpense(String avgLabel) {
    return '$avgLabel Expense: ';
  }

  @override
  String get analyticsExpense => 'Expense';

  @override
  String get analyticsIncome => 'Income';

  @override
  String analyticsTotal(String type) {
    return 'Total $type: ';
  }

  @override
  String analyticsAverage(String avgLabel) {
    return '$avgLabel: ';
  }

  @override
  String get currencyCNY => 'Chinese Yuan';

  @override
  String get currencyUSD => 'US Dollar';

  @override
  String get currencyEUR => 'Euro';

  @override
  String get currencyJPY => 'Japanese Yen';

  @override
  String get currencyHKD => 'Hong Kong Dollar';

  @override
  String get currencyTWD => 'New Taiwan Dollar';

  @override
  String get currencyGBP => 'British Pound';

  @override
  String get currencyAUD => 'Australian Dollar';

  @override
  String get currencyCAD => 'Canadian Dollar';

  @override
  String get currencyKRW => 'South Korean Won';

  @override
  String get currencySGD => 'Singapore Dollar';

  @override
  String get currencyMYR => 'Malaysian Ringgit';

  @override
  String get currencyTHB => 'Thai Baht';

  @override
  String get currencyIDR => 'Indonesian Rupiah';

  @override
  String get currencyPHP => 'Philippine Peso';

  @override
  String get currencyVND => 'Vietnamese Dong';

  @override
  String get currencyINR => 'Indian Rupee';

  @override
  String get currencyRUB => 'Russian Ruble';

  @override
  String get currencyBYN => 'Belarusian Ruble';

  @override
  String get currencyNZD => 'New Zealand Dollar';

  @override
  String get currencyCHF => 'Swiss Franc';

  @override
  String get currencySEK => 'Swedish Krona';

  @override
  String get currencyNOK => 'Norwegian Krone';

  @override
  String get currencyDKK => 'Danish Krone';

  @override
  String get currencyBRL => 'Brazilian Real';

  @override
  String get currencyMXN => 'Mexican Peso';

  @override
  String get currencyTRY => 'Turkish Lira';

  @override
  String get currencyZAR => 'South African Rand';

  @override
  String get currencyAED => 'UAE Dirham';

  @override
  String get currencySAR => 'Saudi Riyal';

  @override
  String get currencyPLN => 'Polish Zloty';

  @override
  String get currencyCZK => 'Czech Koruna';

  @override
  String get currencyHUF => 'Hungarian Forint';

  @override
  String get currencyARS => 'Argentine Peso';

  @override
  String get currencyCLP => 'Chilean Peso';

  @override
  String get currencyCOP => 'Colombian Peso';

  @override
  String get currencyPEN => 'Peruvian Sol';

  @override
  String get currencyEGP => 'Egyptian Pound';

  @override
  String get currencyNGN => 'Nigerian Naira';

  @override
  String get currencyKZT => 'Kazakhstani Tenge';

  @override
  String get currencyUAH => 'Ukrainian Hryvnia';

  @override
  String get currencyILS => 'Israeli New Shekel';

  @override
  String get currencyPKR => 'Pakistani Rupee';

  @override
  String get currencyBDT => 'Bangladeshi Taka';

  @override
  String get currencyLKR => 'Sri Lankan Rupee';

  @override
  String get currencyMMK => 'Myanmar Kyat';

  @override
  String get recurringTransactionTitle => 'Recurring Bills';

  @override
  String get recurringTransactionAdd => 'Add Recurring Bill';

  @override
  String get recurringTransactionEdit => 'Edit Recurring Bill';

  @override
  String get recurringTransactionFrequency => 'Frequency';

  @override
  String get recurringTransactionDaily => 'Daily';

  @override
  String get recurringTransactionWeekly => 'Weekly';

  @override
  String get recurringTransactionMonthly => 'Monthly';

  @override
  String get recurringTransactionYearly => 'Yearly';

  @override
  String get recurringTransactionInterval => 'Interval';

  @override
  String get recurringTransactionDayOfMonth => 'Day of Month';

  @override
  String get recurringTransactionStartDate => 'Start Date';

  @override
  String get recurringTransactionEndDate => 'End Date';

  @override
  String get recurringTransactionNoEndDate => 'Perpetual';

  @override
  String get recurringTransactionDeleteConfirm => 'Are you sure you want to delete this recurring bill?';

  @override
  String get recurringTransactionEmpty => 'No Recurring Bills';

  @override
  String get recurringTransactionEmptyHint => 'Tap the + button in the top right corner to add';

  @override
  String recurringTransactionEveryNDays(int n) {
    return 'Every $n day(s)';
  }

  @override
  String recurringTransactionEveryNWeeks(int n) {
    return 'Every $n week(s)';
  }

  @override
  String recurringTransactionEveryNMonths(int n) {
    return 'Every $n month(s)';
  }

  @override
  String recurringTransactionEveryNYears(int n) {
    return 'Every $n year(s)';
  }

  @override
  String get recurringTransactionUsageTitle => 'Usage Guide';

  @override
  String get recurringTransactionUsageContent => 'Recurring transactions are automatically scanned and generated when the app cold starts. After setting a date, the system will create corresponding bills on the first startup after that date. For example: if set to Nov 27, bills will be auto-recorded on the first launch after Nov 27.';

  @override
  String get ledgerSelectTitle => 'Select Ledger';

  @override
  String get ledgerSelect => 'Select Ledger';

  @override
  String get accountsTitle => 'Asset Management';

  @override
  String get accountsEmptyMessage => 'No accounts yet, tap the top right to add';

  @override
  String get accountAddTooltip => 'Add Account';

  @override
  String get accountAddButton => 'Add Account';

  @override
  String get accountBalance => 'Balance';

  @override
  String get accountUpdateBalance => 'Update Balance';

  @override
  String get accountBalanceAdjustmentTitle => 'Confirm balance change';

  @override
  String get accountBalanceAdjustmentChooseAction => 'Choose how to save this balance change';

  @override
  String get accountBalanceAdjustmentOnly => 'Balance only';

  @override
  String get accountBalanceAdjustmentOnlyMessage => 'No transaction will be created. Historical balance trends will use the new balance as their baseline.';

  @override
  String get accountBalanceAdjustmentCreate => 'Create transaction';

  @override
  String get accountBalanceAdjustmentCreateMessage => 'Also add a regular income or expense under the Balance category, included in income, expense, and budget statistics.';

  @override
  String get accountBalanceAdjustmentConfirmOnly => 'Confirm balance only';

  @override
  String get accountBalanceAdjustmentConfirmCreate => 'Confirm and create transaction';

  @override
  String get accountBalanceCurrent => 'Current balance';

  @override
  String get accountBalanceAfterAdjustment => 'Adjusted balance';

  @override
  String get accountBalanceDifference => 'Difference';

  @override
  String get accountBalanceUnchanged => 'The balance has not changed';

  @override
  String get accountEditTitle => 'Edit Account';

  @override
  String get accountNewTitle => 'New Account';

  @override
  String get accountNameLabel => 'Account Name';

  @override
  String get accountNameHint => 'e.g.: ICBC, Alipay, etc.';

  @override
  String get accountNameRequired => 'Please enter account name';

  @override
  String get accountNameDuplicate => 'Account name already exists, please use a different name';

  @override
  String get accountTypeLabel => 'Account Type';

  @override
  String get accountTypeCash => 'Cash';

  @override
  String get accountTypeBankCard => 'Bank Card';

  @override
  String get accountTypeCreditCard => 'Credit Card';

  @override
  String get accountTypeAlipay => 'Alipay';

  @override
  String get accountTypeWechat => 'WeChat';

  @override
  String get accountTypeOther => 'Other';

  @override
  String get accountInitialBalance => 'Initial Balance';

  @override
  String get accountInitialBalanceHint => 'Enter initial balance (optional)';

  @override
  String get accountDeleteWarningTitle => 'Confirm Delete';

  @override
  String accountDeleteWarningMessage(int count) {
    return 'This account has $count related transactions. After deletion, account information in transaction records will be cleared. Confirm deletion?';
  }

  @override
  String get accountDeleteConfirm => 'Confirm to delete this account?';

  @override
  String get accountSelectTitle => 'Select Account';

  @override
  String get accountNone => 'No Account';

  @override
  String get accountsEnableFeature => 'Enable Account Feature';

  @override
  String get accountHide => 'Hide account';

  @override
  String get accountUnhide => 'Restore account';

  @override
  String get accountRestore => 'Restore';

  @override
  String get accountHiddenTag => 'Hidden';

  @override
  String get accountHiddenSection => 'Hidden';

  @override
  String accountHiddenSectionSummary(int count, String total) {
    return '$count hidden · $total';
  }

  @override
  String get accountHideConfirmTitle => 'Hide this account?';

  @override
  String get accountHideConfirmBody => 'It won\'t be selectable for new records; history and balance are kept and you can restore it anytime.';

  @override
  String accountHideRecurringWarn(int count) {
    return '$count recurring bills use this account; they\'ll be skipped while hidden.';
  }

  @override
  String get accountHideClearedDefault => 'Cleared its default-account setting';

  @override
  String get accountHiddenToast => 'Hidden';

  @override
  String get accountRestoredToast => 'Restored';

  @override
  String get privacyOpenSourceUrlError => 'Cannot open link';

  @override
  String get welcomeTitle => 'Welcome to BeeCount';

  @override
  String get welcomeDescription => 'An accounting app that truly respects your privacy';

  @override
  String get welcomeCurrencyDescription => 'Choose your preferred currency, you can change it anytime in settings';

  @override
  String get welcomeCreateDefaultLedger => 'Create a default ledger';

  @override
  String get welcomePrivacyTitle => 'Open Source · Community Driven';

  @override
  String get welcomePrivacyFeature1 => '100% open source code, supervised by community';

  @override
  String get welcomePrivacyFeature2 => 'No privacy concerns, data stored locally';

  @override
  String get welcomeOpenSourceFeature1 => 'Active developer community, continuous improvement';

  @override
  String get welcomeViewGitHub => 'Visit GitHub Repository';

  @override
  String get widgetManagement => 'Home Screen Widget';

  @override
  String get widgetManagementDesc => 'Quick view of income and expenses on home screen';

  @override
  String get widgetPreview => 'Widget Preview';

  @override
  String get widgetPreviewDesc => 'Widget automatically displays actual data from current ledger, theme color follows app settings';

  @override
  String get widgetGalleryTitle => 'Widget Gallery';

  @override
  String get widgetGalleryDesc => 'Previews use sample data — the real widget shows your current ledger and follows your theme color.';

  @override
  String get widgetGalleryGlanceTitle => 'Overview';

  @override
  String get widgetGalleryGlanceDesc => 'Today\'s and this month\'s income and expenses at a glance';

  @override
  String get widgetGalleryNetWorthDesc => 'Total assets, liabilities and net worth trend';

  @override
  String get widgetGalleryQuickAddTitle => 'Quick Add';

  @override
  String get widgetGalleryQuickAddDesc => 'One-tap entry for your frequent categories';

  @override
  String get widgetGalleryBudgetDesc => 'Track your budget progress at a glance';

  @override
  String get widgetGalleryRecentDesc => 'See your latest transactions';

  @override
  String get widgetGalleryDashboardTitle => 'Dashboard';

  @override
  String get widgetDashboardTitle => 'This Month';

  @override
  String get widgetGalleryDashboardDesc => 'Income, trend and recent transactions in one view';

  @override
  String get widgetGalleryConsumptionRhythmDesc => 'See your spending pace across the last 30 days';

  @override
  String get widgetGalleryBeeTrailDesc => 'Build a gentle daily recording habit';

  @override
  String get widgetSizeSmall => 'Small';

  @override
  String get widgetSizeMedium => 'Medium';

  @override
  String get widgetSizeLarge => 'Large';

  @override
  String get howToAddWidget => 'How to Add Widget';

  @override
  String get iosWidgetStep1 => 'Long press on home screen blank area to enter edit mode';

  @override
  String get iosWidgetStep2 => 'Tap the \"+\" button in upper left corner';

  @override
  String get iosWidgetStep3 => 'Search and select \"BeeCount\"';

  @override
  String get iosWidgetStep4 => 'Select medium widget and add to home screen';

  @override
  String get androidWidgetStep1 => 'Long press on home screen blank area';

  @override
  String get androidWidgetStep2 => 'Select \"Widgets\"';

  @override
  String get androidWidgetStep3 => 'Find and long press \"BeeCount\" widget';

  @override
  String get androidWidgetStep4 => 'Drag to suitable position on home screen';

  @override
  String get aboutWidget => 'About Widget';

  @override
  String get widgetDescription => 'Widget automatically syncs to display today\'s and this month\'s income and expense data, refreshing every 30 minutes. Data updates immediately when app is opened.';

  @override
  String get widgetQuickEntryTitle => 'Quick Entry';

  @override
  String get widgetQuickEntryDesc => 'Tap the left side of the widget to quickly add an expense, or tap the right side to add an income. You can also use beecount://new?type=transfer via Shortcuts to quickly start a transfer.';

  @override
  String get appName => 'BeeCount';

  @override
  String get monthSuffix => '';

  @override
  String get todayExpense => 'Today\'s Expense';

  @override
  String get todayIncome => 'Today\'s Income';

  @override
  String get monthExpense => 'Month\'s Expense';

  @override
  String get monthIncome => 'Month\'s Income';

  @override
  String get featureDescription => 'Feature Description';

  @override
  String get featureDescriptionContent => 'After taking a screenshot of payment page, the system will automatically recognize amount and merchant info, and create expense record.\n\n⚡ Recognition speed: 2-3 seconds (may be longer on some devices)\n🤖 Smart category matching\n📝 Auto-fill notes\n\n⚠️ Note:\n• Different devices have different screenshot save speeds, delay may be 5-10 seconds\n• May not work on some devices, depending on system implementation\n• Recognized screenshots will be skipped automatically\n• Due to Android Scoped Storage restrictions (Android 10+), apps cannot delete system screenshots. Manual cleanup required';

  @override
  String get enabled => 'Enabled';

  @override
  String get disabled => 'Disabled';

  @override
  String get photosPermissionRequired => 'Photos permission required for screenshot monitoring';

  @override
  String get enableSuccess => 'Auto billing enabled';

  @override
  String get disableSuccess => 'Auto billing disabled';

  @override
  String get enableFailed => 'Enable failed';

  @override
  String get disableFailed => 'Disable failed';

  @override
  String get iosAutoFeatureDesc => 'Use iOS \"Shortcuts\" app to automatically identify payment information from screenshots and create transactions. Once set up, it will automatically trigger on every screenshot.';

  @override
  String get iosAutoShortcutConfigTitle => 'Configuration Steps:';

  @override
  String get iosAutoShortcutStep1 => 'Open \"Shortcuts\" app, tap \"+\" in top right to create new shortcut';

  @override
  String get iosAutoShortcutStep2 => 'Add \"Take Screenshot\" action';

  @override
  String get iosAutoShortcutStep3 => 'Search and add \"BeeCount - Auto Billing\" action';

  @override
  String get iosAutoShortcutStep4 => 'Set the screenshot parameter of \"BeeCount\" to the previous \"Screenshot\"';

  @override
  String get iosAutoShortcutStep5 => '(Optional) Go to Settings > Accessibility > Touch > Back Tap, bind this shortcut';

  @override
  String get iosAutoShortcutStep6 => 'Done! Double tap phone back during payment for quick billing';

  @override
  String get iosAutoShortcutRecommendedTip => '✅ Recommended: After binding the shortcut to \"Back Tap\", double tap phone back during payment to auto-screenshot and recognize billing, no manual screenshot needed.';

  @override
  String get iosAutoBackTapTitle => '💡 Double Tap Back to Trigger (Recommended)';

  @override
  String get iosAutoBackTapDesc => 'Settings > Accessibility > Touch > Back Tap\n• Select \"Double Tap\" or \"Triple Tap\"\n• Choose the shortcut you just created\n• After setup, double tap phone back during payment to auto-record, no screenshot needed';

  @override
  String get iosAutoTutorialTitle => 'Video Tutorial';

  @override
  String get iosAutoTutorialDesc => 'Watch detailed setup video tutorial';

  @override
  String get iosAutoImportTitle => 'Get Shortcut in One Tap';

  @override
  String get iosAutoImportDesc => 'Tap the button below to import the ready-made \"Screenshot → Auto Billing\" shortcut — no need to add the \"Take Screenshot\" action or connect parameters manually. After importing, binding it to \"Back Tap\" is recommended.';

  @override
  String get iosAutoImportButton => 'Get Shortcut';

  @override
  String get iosAutoImportFailed => 'Couldn\'t open the shortcut link. Check your connection and try again.';

  @override
  String get iosAutoManualConfigTitle => 'Manual Setup (Advanced)';

  @override
  String get iosAutoManualConfigDesc => 'If one-tap import isn\'t available, follow these steps to create the shortcut manually.';

  @override
  String get dataManagement => 'Data Management';

  @override
  String get dataManagementDesc => 'Import, export, categories and accounts';

  @override
  String get dataManagementPageTitle => 'Data Management';

  @override
  String get dataManagementPageSubtitle => 'Manage transaction data and categories';

  @override
  String get dataManagementAttachmentHint => 'When restoring data, please import the attachment package first, then import ledger data (CSV or cloud sync) to ensure attachments are correctly associated.';

  @override
  String get shareBilling => 'Share to Bill';

  @override
  String get shareBillingDesc => 'Share a payment screenshot from Alipay/WeChat to bill it';

  @override
  String get shareBillingGuide => 'When you see a payment screenshot in Alipay, WeChat, Photos, etc., tap \"Share\" and choose \"BeeCount\" to auto-recognize the amount, merchant, and time and create a transaction — no need to save the screenshot first.';

  @override
  String get shareBillingActionHint => 'Recognized automatically in the background after sharing — no need to open BeeCount';

  @override
  String get automation => 'Automation';

  @override
  String get automationDesc => 'Recurring transactions and reminders';

  @override
  String get automationPageTitle => 'Automation';

  @override
  String get automationPageSubtitle => 'Recurring transactions and reminder settings';

  @override
  String get appearanceSettings => 'Personalization';

  @override
  String get appearanceSettingsDesc => 'Theme, font, language, app lock, etc.';

  @override
  String get appearanceSettingsPageTitle => 'Personalization';

  @override
  String get appearanceSettingsPageSubtitle => 'Appearance, display, security and other app preferences';

  @override
  String get about => 'About';

  @override
  String get aboutDesc => 'Version info, help and feedback';

  @override
  String get mineRateApp => 'Rate the App';

  @override
  String get mineRateAppSubtitle => 'Rate us on the App Store';

  @override
  String get aboutPageTitle => 'About';

  @override
  String get aboutPageSubtitle => 'App information and help';

  @override
  String get aboutPageLoadingVersion => 'Loading version...';

  @override
  String get aboutWebsite => 'Official Website';

  @override
  String get aboutGitHubRepo => 'GitHub Repository';

  @override
  String get aboutXiaohongshu => 'Xiaohongshu';

  @override
  String get aboutDouyin => 'Douyin';

  @override
  String get aboutTelegram => 'Telegram';

  @override
  String get aboutSupportDevelopment => 'Support Development';

  @override
  String get aboutSupportDevelopmentSubtitle => 'Buy me a coffee';

  @override
  String get aboutDeveloperStoryTitle => 'From the Developer';

  @override
  String get aboutDeveloperStory => 'I started tracking my expenses in 2015 as an intern, and I\'ve kept the habit for over a decade. Concerned about ads, paywalls, privacy risks, and apps shutting down, I decided to build my own — first as a small tool for myself and my family.\n\nIn September 2025, BeeCount launched its first version. Honestly, I had no idea if anyone would use it. But gradually, feedback started coming in — someone said they finally found a clean budgeting app, others offered great suggestions, and some quietly left five-star reviews. Every single message reminded me this was worth continuing.\n\nBeeCount is ad-free, subscription-free, and fully open source. All your data stays on your device and is never uploaded to any third-party server. But publishing and maintaining an app isn\'t free — developer accounts, servers, and other costs are currently covered by community donations, and every system update, bug fix, and new feature is built outside of my day job.\n\nIf BeeCount has been helpful to you, a rating, a share, or a donation would help this little project go further. Thank you for your trust.';

  @override
  String get aboutRelatedProducts => 'More Products';

  @override
  String get logCenterTitle => 'Log Center';

  @override
  String get logCenterSubtitle => 'View app runtime logs';

  @override
  String get logCenterSearchHint => 'Search log content or tags...';

  @override
  String get logCenterFilterLevel => 'Log Level';

  @override
  String get logCenterFilterPlatform => 'Platform';

  @override
  String get logCenterTotal => 'Total';

  @override
  String get logCenterFiltered => 'Filtered';

  @override
  String get logCenterEmpty => 'No logs';

  @override
  String get logCenterExport => 'Export';

  @override
  String get logCenterClear => 'Clear';

  @override
  String get logCenterExportFailed => 'Export failed';

  @override
  String get logCenterClearConfirmTitle => 'Clear Logs';

  @override
  String get logCenterClearConfirmMessage => 'Are you sure you want to clear all logs? This action cannot be undone.';

  @override
  String get logCenterCleared => 'Logs cleared';

  @override
  String get logCenterCopied => 'Copied to clipboard';

  @override
  String get configImportExportTitle => 'Config Import/Export';

  @override
  String get configImportExportSubtitle => 'Backup and restore app configurations';

  @override
  String get configImportExportInfoTitle => 'Feature Description';

  @override
  String get configImportExportInfoMessage => 'This feature is used to export and import app configurations, including cloud service settings, AI settings, etc. The config file uses YAML format for easy viewing and editing.\n\n⚠️ Config files contain sensitive information (such as API keys, passwords, etc.), please keep them safe.';

  @override
  String get configExportTitle => 'Export Config';

  @override
  String get configExportSubtitle => 'Export current config to YAML file';

  @override
  String get configExportShareSubject => 'BeeCount Config File';

  @override
  String get configExportSuccess => 'Config exported successfully';

  @override
  String get configExportFailed => 'Config export failed';

  @override
  String get configImportTitle => 'Import Config';

  @override
  String get configImportSubtitle => 'Restore config from YAML file';

  @override
  String get configImportNoFilePath => 'No file selected';

  @override
  String get configImportConfirmTitle => 'Confirm Import';

  @override
  String get configImportSuccess => 'Config imported successfully';

  @override
  String get configImportFailed => 'Config import failed';

  @override
  String get configImportRestartTitle => 'Restart Required';

  @override
  String get configImportRestartMessage => 'Config has been imported. Some settings will take effect after restarting the app.';

  @override
  String get configImportExportIncludesTitle => 'Included Configurations';

  @override
  String configExportSavedTo(String path) {
    return 'Saved to: $path';
  }

  @override
  String get configExportViewContent => 'View Content';

  @override
  String get configExportCopyContent => 'Copy Content';

  @override
  String get configExportContentCopied => 'Copied to clipboard';

  @override
  String get configExportReadFileFailed => 'Failed to read file';

  @override
  String get configIncludeLedgers => 'Ledgers';

  @override
  String get configIncludeAppSettings => 'App settings (language, appearance, reminder, default account, etc.)';

  @override
  String get configIncludeRecurringTransactions => 'Recurring transactions';

  @override
  String get configIncludeAccounts => 'Accounts';

  @override
  String get configIncludeCategories => 'Categories';

  @override
  String get configIncludeTags => 'Tags';

  @override
  String get configIncludeBudgets => 'Budgets';

  @override
  String get configIncludeOtherSettings => 'Other Settings';

  @override
  String get configIncludeOtherSettingsSubtitle => 'Including cloud service, AI config, app settings, etc.';

  @override
  String get configExportSelectTitle => 'Select Export Content';

  @override
  String get configExportPreviewTitle => 'Export Preview';

  @override
  String get configExportConfirmTitle => 'Confirm Export';

  @override
  String get configImportSelectTitle => 'Select Import Content';

  @override
  String get configImportPreviewTitle => 'Import Preview';

  @override
  String get ledgersConflictTitle => 'Sync Conflict';

  @override
  String get ledgersConflictMessage => 'Local and cloud ledger data are inconsistent, please choose an action:';

  @override
  String ledgersConflictLocalInfo(int count) {
    return 'Local: $count transactions';
  }

  @override
  String ledgersConflictRemoteInfo(int count) {
    return 'Cloud: $count transactions';
  }

  @override
  String ledgersConflictRemoteUpdated(String time) {
    return 'Cloud updated: $time';
  }

  @override
  String ledgersConflictLocalFingerprint(String fp) {
    return 'Local fingerprint: $fp';
  }

  @override
  String ledgersConflictRemoteFingerprint(String fp) {
    return 'Cloud fingerprint: $fp';
  }

  @override
  String get ledgersConflictUpload => 'Upload to Cloud';

  @override
  String get ledgersConflictDownload => 'Download to Local';

  @override
  String get ledgersConflictUploading => 'Uploading...';

  @override
  String get ledgersConflictDownloading => 'Downloading...';

  @override
  String get ledgersConflictUploadSuccess => 'Upload successful';

  @override
  String ledgersConflictDownloadSuccess(int inserted) {
    return 'Download successful, merged $inserted transactions';
  }

  @override
  String get storageManagementTitle => 'Storage Management';

  @override
  String get storageManagementSubtitle => 'Clear cache to free up space';

  @override
  String get storageAPKFiles => 'Installation Packages';

  @override
  String get storageNoData => 'No Data';

  @override
  String get storageFiles => 'files';

  @override
  String get storageHint => 'Tap items to clear corresponding cache files';

  @override
  String get storageClearConfirmTitle => 'Confirm Clear';

  @override
  String storageClearAPKMessage(String size) {
    return 'Are you sure you want to clear all installation packages? Size: $size';
  }

  @override
  String get storageClearSuccess => 'Cleared successfully';

  @override
  String get accountNoTransactions => 'No transactions';

  @override
  String get accountTransactionHistory => 'Transaction History';

  @override
  String get accountTotalBalance => 'Net Assets';

  @override
  String get accountCurrencyLocked => 'This account has transactions and cannot change currency';

  @override
  String get accountDefaultIncomeTitle => 'Default Income Account';

  @override
  String get accountDefaultExpenseTitle => 'Default Expense Account';

  @override
  String get accountDefaultNone => 'Not Set';

  @override
  String get commonNotice => 'Notice';

  @override
  String get transferTitle => 'Transfer';

  @override
  String get transferIconSettings => 'Transfer Icon Settings';

  @override
  String get transferIconSettingsDesc => 'Customize the display icon for transfer records';

  @override
  String get transferFromAccount => 'From Account';

  @override
  String get transferToAccount => 'To Account';

  @override
  String get transferSelectAccount => 'Select Account';

  @override
  String get transferCreateSuccess => 'Transfer created successfully';

  @override
  String get transferUpdateSuccess => 'Transfer updated successfully';

  @override
  String get transferDifferentCurrencyError => 'Transfer only supports accounts with the same currency';

  @override
  String get transferToPrefix => 'To';

  @override
  String get transferFromPrefix => 'From';

  @override
  String get welcomeCategoryModeTitle => 'Choose Category Mode';

  @override
  String get welcomeCategoryModeDescription => 'Select the category structure that suits your needs';

  @override
  String get welcomeCategoryModeFlatTitle => 'Flat Categories';

  @override
  String get welcomeCategoryModeFlatDescription => 'Simple and fast';

  @override
  String get welcomeCategoryModeFlatFeature1 => 'Flat structure, easy to use';

  @override
  String get welcomeCategoryModeFlatFeature2 => 'Perfect for simple categorization';

  @override
  String get welcomeCategoryModeFlatFeature3 => 'Quick selection, efficient tracking';

  @override
  String get welcomeCategoryModeHierarchicalTitle => 'Hierarchical Categories';

  @override
  String get welcomeCategoryModeHierarchicalDescription => 'Detailed management';

  @override
  String get welcomeCategoryModeHierarchicalFeature1 => 'Support parent-child category levels';

  @override
  String get welcomeCategoryModeHierarchicalFeature2 => 'More detailed transaction classification';

  @override
  String get welcomeCategoryModeHierarchicalFeature3 => 'Perfect for detailed management';

  @override
  String get welcomeCategoryModeNoneTitle => 'No Categories';

  @override
  String get welcomeCategoryModeNoneDescription => 'Fully customizable, add as needed';

  @override
  String get welcomeCategoryModeNoneFeature1 => 'No preset categories';

  @override
  String get welcomeCategoryModeNoneFeature2 => 'Create categories based on your needs';

  @override
  String get welcomeCategoryModeNoneFeature3 => 'Perfect for custom classification needs';

  @override
  String get welcomeExistingUserTitle => 'Existing User?';

  @override
  String get welcomeExistingUserButton => 'Import Config';

  @override
  String get welcomeImportingConfig => 'Importing configuration...';

  @override
  String get welcomeImportSuccess => 'Configuration imported successfully';

  @override
  String welcomeImportFailed(String error) {
    return 'Import failed: $error';
  }

  @override
  String get welcomeImportNoFile => 'No file selected';

  @override
  String get welcomeImportAttachmentTitle => 'Import Attachments';

  @override
  String get welcomeImportAttachmentDesc => 'Would you like to import attachment files as well?';

  @override
  String get welcomeImportAttachmentButton => 'Select Attachment File';

  @override
  String get welcomeImportAttachmentSkip => 'Skip';

  @override
  String welcomeImportAttachmentSuccess(int imported) {
    return 'Attachments imported: $imported';
  }

  @override
  String welcomeImportAttachmentFailed(String error) {
    return 'Attachment import failed: $error';
  }

  @override
  String get welcomeImportingAttachment => 'Importing attachments...';

  @override
  String get iosVersionWarningTitle => 'Requires iOS 16.0 or later';

  @override
  String get iosVersionWarningDesc => 'Screenshot auto-billing feature uses the App Intents framework introduced in iOS 16. Your device is running an older version and does not support this feature.\n\nPlease upgrade to iOS 16 or later to use this feature.';

  @override
  String get billCardSuccess => 'Booking Successful';

  @override
  String get billCardUndone => 'Undone';

  @override
  String get billCardAmount => '💰 Amount';

  @override
  String get billCardCategory => '🏷️ Category';

  @override
  String get billCardTime => '📅 Time';

  @override
  String get billCardNote => '📝 Note';

  @override
  String get billCardAccount => '💳 Account';

  @override
  String get billCardTransferAccounts => '🔁 Transfer accounts';

  @override
  String get billCardType => '↕️ Type';

  @override
  String get billCardExpense => 'Expense';

  @override
  String get billCardIncome => 'Income';

  @override
  String get billCardTransfer => 'Transfer';

  @override
  String get billCardCurrency => '💱 Currency';

  @override
  String get billCardTags => '🏷️ Tags';

  @override
  String get billCardUndo => 'Undo';

  @override
  String get billCardEdit => 'Edit';

  @override
  String get billCardUnknownLedger => 'Unknown Ledger';

  @override
  String get tagManageTitle => 'Tags';

  @override
  String get tagManageSubtitle => 'Manage transaction tags';

  @override
  String get tagManageEmpty => 'No tags yet';

  @override
  String get tagManageEmptyHint => 'Tap + to add a tag';

  @override
  String get tagManageGenerateDefault => 'Generate Default Tags';

  @override
  String get tagManageGenerateDefaultConfirm => 'Generate default tags? Existing tags with the same name will not be overwritten.';

  @override
  String get tagManageGenerateDefaultSuccess => 'Default tags generated';

  @override
  String get tagEditTitle => 'Edit Tag';

  @override
  String get tagAddTitle => 'Add Tag';

  @override
  String get tagNameLabel => 'Tag Name';

  @override
  String get tagNameHint => 'Enter tag name';

  @override
  String get tagNameRequired => 'Tag name is required';

  @override
  String get tagNameDuplicate => 'Tag name already exists';

  @override
  String get tagColorLabel => 'Tag Color';

  @override
  String get tagCreateSuccess => 'Tag created';

  @override
  String get tagUpdateSuccess => 'Tag updated';

  @override
  String get tagDeleteConfirmTitle => 'Delete Tag';

  @override
  String tagDeleteConfirmMessage(String name) {
    return 'Delete tag \"$name\"? This will not affect associated transactions.';
  }

  @override
  String get tagDeleteSuccess => 'Tag deleted';

  @override
  String get tagSelectTitle => 'Select Tags';

  @override
  String get tagSelectHint => 'Multiple selection';

  @override
  String get tagSelectCreateNew => 'Create New Tag';

  @override
  String get tagSelectOwnerManaged => 'Shared ledger tags are managed by the owner';

  @override
  String get tagSelectRecentlyUsed => 'Recently Used';

  @override
  String get tagSelectAllTags => 'All Tags';

  @override
  String tagTransactionCount(int count) {
    return '$count transactions';
  }

  @override
  String get tagDetailTitle => 'Tag Details';

  @override
  String get tagDetailTotalCount => 'Transactions';

  @override
  String get tagDetailTotalExpense => 'Total Expense';

  @override
  String get tagDetailTotalIncome => 'Total Income';

  @override
  String get tagDetailTransactionList => 'Related Transactions';

  @override
  String get tagDetailNoTransactions => 'No related transactions';

  @override
  String get tagDetailNoTransactionsHint => 'Transactions with this tag will appear here';

  @override
  String get tagNotFound => 'Tag not found';

  @override
  String get tagDefaultMeituan => 'Meituan';

  @override
  String get tagDefaultEleme => 'Eleme';

  @override
  String get tagDefaultTaobao => 'Taobao';

  @override
  String get tagDefaultJD => 'JD.com';

  @override
  String get tagDefaultPDD => 'Pinduoduo';

  @override
  String get tagDefaultStarbucks => 'Starbucks';

  @override
  String get tagDefaultLuckin => 'Luckin Coffee';

  @override
  String get tagDefaultMcDonalds => 'McDonald\'s';

  @override
  String get tagDefaultKFC => 'KFC';

  @override
  String get tagDefaultHema => 'Hema';

  @override
  String get tagDefaultSams => 'Sam\'s Club';

  @override
  String get tagDefaultCostco => 'Costco';

  @override
  String get tagDefaultBusinessTrip => 'Business Trip';

  @override
  String get tagDefaultTravel => 'Travel';

  @override
  String get tagDefaultDining => 'Dining Out';

  @override
  String get tagDefaultOnlineShopping => 'Online Shopping';

  @override
  String get tagDefaultDaily => 'Daily';

  @override
  String get tagDefaultReimbursement => 'Reimbursable';

  @override
  String get tagDefaultRefundable => 'Refundable';

  @override
  String get tagDefaultRefunded => 'Refunded';

  @override
  String get tagDefaultVoiceBilling => 'Voice';

  @override
  String get tagDefaultImageBilling => 'Image';

  @override
  String get tagDefaultCameraBilling => 'Camera';

  @override
  String get tagDefaultAiBilling => 'AI';

  @override
  String get tagShare => 'Share Tags';

  @override
  String get tagImport => 'Import Tags';

  @override
  String get tagClearUnused => 'Clear Unused';

  @override
  String tagShareSuccess(String path) {
    return 'Saved to $path';
  }

  @override
  String get tagShareSubject => 'BeeCount Tags Configuration';

  @override
  String get tagShareFailed => 'Share failed';

  @override
  String get tagImportInvalidFile => 'Please select a YAML file';

  @override
  String get tagImportNoTags => 'No tags found in file';

  @override
  String get tagImportModeTitle => 'Select Import Mode';

  @override
  String get tagImportModeMerge => 'Merge';

  @override
  String get tagImportModeMergeDesc => 'Keep existing tags, add new ones';

  @override
  String get tagImportModeOverwrite => 'Overwrite';

  @override
  String get tagImportModeOverwriteDesc => 'Clear unused tags before import';

  @override
  String get tagImportSuccess => 'Import successful';

  @override
  String get tagImportFailed => 'Import failed';

  @override
  String get tagClearUnusedEmpty => 'No unused tags';

  @override
  String get tagClearUnusedTitle => 'Clear Unused Tags';

  @override
  String tagClearUnusedMessage(int count) {
    return 'Delete $count unused tags?';
  }

  @override
  String tagClearUnusedSuccess(int count) {
    return 'Deleted $count tags';
  }

  @override
  String get tagClearUnusedFailed => 'Clear failed';

  @override
  String get homeSwitchLedger => 'Select Ledger';

  @override
  String get homeManageLedgers => 'Manage Ledgers';

  @override
  String get budgetTitle => 'Budget';

  @override
  String get budgetShowOnHome => 'Show budget on home';

  @override
  String get budgetEmptyHint => 'No budget set yet';

  @override
  String get budgetAddTotal => 'Add Total Budget';

  @override
  String get budgetMonthlyBudget => 'Monthly Budget';

  @override
  String get budgetUsed => 'Used';

  @override
  String get budgetRemaining => 'Remaining';

  @override
  String budgetDaysRemaining(int days) {
    return '$days days remaining';
  }

  @override
  String budgetDailyAvailable(String amount) {
    return 'Daily available $amount';
  }

  @override
  String get budgetCategoryBudgets => 'Category Budgets';

  @override
  String get budgetEditTitle => 'Edit Budget';

  @override
  String get budgetAddTitle => 'Add Budget';

  @override
  String get budgetTypeTotalLabel => 'Total Budget';

  @override
  String get budgetTypeCategoryLabel => 'Category Budget';

  @override
  String get budgetAmountLabel => 'Budget Amount';

  @override
  String get budgetAmountHint => 'Enter budget amount';

  @override
  String get budgetCategoryLabel => 'Select Category';

  @override
  String get budgetCategoryHint => 'Select budget category';

  @override
  String get budgetStartDayLabel => 'Start Day';

  @override
  String get budgetPeriodLabel => 'Period';

  @override
  String get budgetSaveSuccess => 'Budget saved';

  @override
  String get budgetDeleteConfirm => 'Delete this budget?';

  @override
  String get budgetDeleteSuccess => 'Budget deleted';

  @override
  String get attachmentAdd => 'Add Image';

  @override
  String get attachmentTakePhoto => 'Take Photo';

  @override
  String get attachmentChooseFromGallery => 'Choose from Gallery';

  @override
  String get attachmentMaxReached => 'Maximum attachments reached';

  @override
  String get attachmentDeleteConfirm => 'Delete this attachment?';

  @override
  String attachmentCount(int count) {
    return '$count images';
  }

  @override
  String get commonDeleted => 'Deleted';

  @override
  String get attachmentExportTitle => 'Export Attachments';

  @override
  String get attachmentExportSubtitle => 'Export all attachments as a compressed file';

  @override
  String get attachmentImportTitle => 'Import Attachments';

  @override
  String get attachmentImportSubtitle => 'Import attachments from a compressed file';

  @override
  String get attachmentExportEmpty => 'No attachments to export';

  @override
  String attachmentExportProgress(int current, int total) {
    return 'Exporting attachments ($current/$total)';
  }

  @override
  String attachmentExportProgressDetail(int attachmentCount, int iconCount, int current, int total) {
    return 'Exporting $attachmentCount attachment(s) + $iconCount icon(s) ($current/$total)';
  }

  @override
  String get attachmentExportSuccess => 'Attachments exported successfully';

  @override
  String attachmentExportSavedTo(String path) {
    return 'Saved to: $path';
  }

  @override
  String get attachmentImportConflictStrategy => 'Conflict Strategy';

  @override
  String get attachmentImportConflictSkip => 'Skip existing attachments';

  @override
  String get attachmentImportConflictOverwrite => 'Overwrite existing attachments';

  @override
  String attachmentImportProgress(int current, int total) {
    return 'Importing attachments ($current/$total)';
  }

  @override
  String attachmentImportResult(int imported, int skipped, int overwritten, int failed) {
    return 'Imported $imported, Skipped $skipped, Overwritten $overwritten, Failed $failed';
  }

  @override
  String get attachmentImportFailed => 'Failed to import attachments';

  @override
  String attachmentArchiveInfo(int count, String date) {
    return '$count attachments, exported on $date';
  }

  @override
  String get attachmentStartImport => 'Start Import';

  @override
  String get attachmentPreview => 'Preview Attachments';

  @override
  String attachmentPreviewSubtitle(int count) {
    return '$count images in total';
  }

  @override
  String get attachmentPreviewEmpty => 'No attachments';

  @override
  String get attachmentExportPreviewTitle => 'Export Preview';

  @override
  String get attachmentImportPreviewTitle => 'Import Preview';

  @override
  String get shortcutsGuide => 'Shortcuts';

  @override
  String get shortcutsGuideDesc => 'Quick access to voice, camera billing, etc.';

  @override
  String get shortcutsIntroTitle => 'Quick Billing';

  @override
  String get shortcutsIntroDesc => 'Use shortcuts to directly open voice billing, camera billing, and other features from your home screen without opening the app first.';

  @override
  String get availableShortcuts => 'Available Shortcuts';

  @override
  String get shortcutImage => 'Image Billing';

  @override
  String get shortcutImageDesc => 'Recognize bills from gallery images';

  @override
  String get shortcutCamera => 'Camera Billing';

  @override
  String get shortcutCameraDesc => 'Take photos to recognize bills';

  @override
  String get shortcutNewExpense => 'Quick Expense';

  @override
  String get shortcutNewExpenseDesc => 'Open expense entry page directly';

  @override
  String get shortcutNewIncome => 'Quick Income';

  @override
  String get shortcutNewIncomeDesc => 'Open income entry page directly';

  @override
  String get shortcutNewTransfer => 'Quick Transfer';

  @override
  String get shortcutNewTransferDesc => 'Open transfer entry page directly';

  @override
  String get shortcutUrlCopied => 'URL copied to clipboard';

  @override
  String get howToAddShortcut => 'How to Add Shortcuts';

  @override
  String get iosShortcutStep1 => 'Open the Shortcuts app';

  @override
  String get iosShortcutStep2 => 'Tap + in the upper right to create a new shortcut';

  @override
  String get iosShortcutStep3 => 'Add \'Open URL\' action';

  @override
  String get iosShortcutStep4 => 'Paste the copied URL (e.g., beecount://voice)';

  @override
  String get iosShortcutStep5 => 'Save and add to home screen';

  @override
  String get androidShortcutStep1 => 'Download a shortcut creator app (e.g., Shortcut Maker)';

  @override
  String get androidShortcutStep2 => 'Select \'URL Shortcut\'';

  @override
  String get androidShortcutStep3 => 'Paste the copied URL (e.g., beecount://voice)';

  @override
  String get androidShortcutStep4 => 'Set icon and name, then add to home screen';

  @override
  String get shortcutsTip => 'Tips';

  @override
  String get shortcutsTipDesc => 'Shortcuts require AI features. Make sure AI is enabled and API Key is configured.';

  @override
  String get shortcutOpenShortcutsApp => 'Open Shortcuts App';

  @override
  String get shortcutAutoAdd => 'Auto Billing API';

  @override
  String get shortcutAutoAddDesc => 'Automatically create bills via URL parameters. Works great with Shortcuts and automation tools.';

  @override
  String get shortcutAutoAddExample => 'Example URL:';

  @override
  String get shortcutAutoAddParams => 'Supported parameters:';

  @override
  String get shortcutParamAmount => 'Amount (required)';

  @override
  String get shortcutParamType => 'Type: expense / income / transfer';

  @override
  String get shortcutParamCategory => 'Category name (must match existing category)';

  @override
  String get shortcutParamNote => 'Note';

  @override
  String get shortcutParamAccount => 'Account name (must match existing account)';

  @override
  String get shortcutParamTags => 'Tags (comma separated)';

  @override
  String get shortcutParamDate => 'Date (ISO format, e.g., 2024-01-15)';

  @override
  String get quickActionImage => 'Photo Billing';

  @override
  String get quickActionCamera => 'Camera Billing';

  @override
  String get quickActionAiChat => 'AI Assistant';

  @override
  String get calendarTitle => 'Calendar';

  @override
  String get calendarToday => 'Today';

  @override
  String get calendarNoTransactions => 'No transactions';

  @override
  String get calendarAddTransaction => 'Add entry on this day';

  @override
  String get calendarAddTransactionTooltip => 'Add a record on the selected day';

  @override
  String get commonUncategorized => 'Uncategorized';

  @override
  String get commonSaved => 'Saved';

  @override
  String get appLockTitle => 'App Lock';

  @override
  String get appLockDesc => 'PIN & biometric to protect privacy';

  @override
  String get appLockEnable => 'Enable App Lock';

  @override
  String get appLockEnableDesc => 'Require authentication on launch and resume';

  @override
  String get appLockSetPin => 'Set PIN';

  @override
  String get appLockChangePin => 'Change PIN';

  @override
  String get appLockVerifyPin => 'Verify PIN';

  @override
  String get appLockVerifyCurrentPin => 'Enter current PIN';

  @override
  String get appLockSetNewPin => 'Set new PIN';

  @override
  String get appLockConfirmPin => 'Confirm PIN';

  @override
  String get appLockEnterPin => 'Enter PIN';

  @override
  String get appLockPinSetSuccess => 'PIN set successfully';

  @override
  String get appLockDisabled => 'App Lock disabled';

  @override
  String get appLockBiometric => 'Biometric Unlock';

  @override
  String get appLockBiometricDesc => 'Use Face ID or fingerprint to unlock';

  @override
  String get appLockBiometricReason => 'Verify identity to unlock Bee Accounting';

  @override
  String get appLockTimeout => 'Auto-lock Timeout';

  @override
  String get appLockTimeoutImmediate => 'Immediately';

  @override
  String get appLockTimeout1Min => 'After 1 minute';

  @override
  String get appLockTimeout5Min => 'After 5 minutes';

  @override
  String get appLockTimeout15Min => 'After 15 minutes';

  @override
  String get creditCardSettings => 'Credit Card Settings';

  @override
  String get accountTabValuation => 'Valuation';

  @override
  String get creditCardDaysRequired => 'Please select billing & due dates';

  @override
  String get creditLimit => 'Credit Limit';

  @override
  String get creditLimitHint => 'Enter credit limit';

  @override
  String get billingDay => 'Billing Day';

  @override
  String get paymentDueDay => 'Payment Due Day';

  @override
  String get creditUsed => 'Used';

  @override
  String get creditAvailable => 'Est. available';

  @override
  String get creditCardOwed => 'Current debt';

  @override
  String dayOfMonth(int day) {
    return '${day}th of each month';
  }

  @override
  String get creditCardReminderTitle => 'Payment Reminder';

  @override
  String get creditCardReminderDesc => 'Remind before payment due date';

  @override
  String creditCardReminderDaysBefore(int days) {
    return '$days days before';
  }

  @override
  String get creditCardInitialBalanceHint => 'Current debt (negative number)';

  @override
  String get selectDay => 'Select Day';

  @override
  String get accountBankName => 'Bank';

  @override
  String get accountBankNameHint => 'e.g. ICBC';

  @override
  String get accountCardLastFour => 'Last 4 Digits';

  @override
  String get accountCardLastFourHint => 'e.g. 1234';

  @override
  String get accountNote => 'Note';

  @override
  String get accountNoteHint => 'Add a note';

  @override
  String get accountMetaInfo => 'Account Info';

  @override
  String get accountBalanceTrend => 'Balance Trend';

  @override
  String get accountCategoryBreakdown => 'Category Breakdown';

  @override
  String get accountCategoryExpense => 'Expense';

  @override
  String get accountCategoryIncome => 'Income';

  @override
  String get accountNoMoreData => 'No more data';

  @override
  String get totalAssets => 'Total Assets';

  @override
  String get totalLiabilities => 'Total Liabilities';

  @override
  String get assetAccounts => 'Asset Accounts';

  @override
  String get liabilityAccounts => 'Liability Accounts';

  @override
  String get assetComposition => 'Asset Composition';

  @override
  String get accountTypeInvestment => 'Investment';

  @override
  String get accountTypeLoan => 'Loan';

  @override
  String get accountTypeReceivable => 'Receivable';

  @override
  String get accountTypeRealEstate => 'Real Estate';

  @override
  String get accountTypeVehicle => 'Vehicle';

  @override
  String get accountTypeInsurance => 'Insurance';

  @override
  String get accountTypeSocialFund => 'Social Fund';

  @override
  String get valuationCurrentValue => 'Current Valuation';

  @override
  String get valuationCurrentDebt => 'Current Debt';

  @override
  String get valuationUpdateValue => 'Update Valuation';

  @override
  String get valuationUpdateDebt => 'Update Debt';

  @override
  String valuationLastUpdated(String date) {
    return 'Last updated: $date';
  }

  @override
  String get valuationAccountHint => 'Enter current valuation';

  @override
  String get valuationDebtHint => 'Enter current debt amount';

  @override
  String get accountGroupTradable => 'Daily Accounts';

  @override
  String get accountGroupValuation => 'Assets/Liabilities';

  @override
  String creditCardBillingInfo(int billingDay, int paymentDueDay) {
    return 'Bills on ${billingDay}th · Due on ${paymentDueDay}th';
  }

  @override
  String creditCardDaysUntilPayment(int days) {
    return '$days days until payment due';
  }

  @override
  String get creditCardPaymentDueToday => 'Payment due today';

  @override
  String get creditCardQuickRepay => 'Record Repayment';

  @override
  String get budgetManagement => 'Budget';

  @override
  String get budgetManagementDesc => 'Set monthly budget and control spending';

  @override
  String get budgetSetupHint => 'Set a budget to control monthly spending';

  @override
  String get budgetSetupAction => 'Set up';

  @override
  String get twofaChallengeTitle => 'Two-factor authentication';

  @override
  String get twofaMethodTotp => 'Code';

  @override
  String get twofaMethodRecovery => 'Recovery code';

  @override
  String get twofaTotpHint => 'Enter the 6-digit code from your authenticator app (Google Authenticator / 1Password / Authy / etc).';

  @override
  String get twofaRecoveryHint => 'Enter a recovery code saved when you enabled 2FA (e.g. abcd-efgh). Each code works once.';

  @override
  String get twofaTotpInputPlaceholder => '6-digit code';

  @override
  String get twofaRecoveryInputPlaceholder => 'Recovery code';

  @override
  String twofaCountdown(String time) {
    return 'Time remaining $time';
  }

  @override
  String get twofaVerifyButton => 'Verify';

  @override
  String get twofaStatusTitle => 'Two-factor authentication';

  @override
  String get twofaStatusEnabled => 'Enabled ✓';

  @override
  String get twofaStatusDisabled => 'Not enabled';

  @override
  String twofaStatusEnabledAt(String date) {
    return 'Enabled on $date';
  }

  @override
  String get twofaStatusManageHint => 'Manage from the Web app (enable / disable / regenerate recovery codes)';

  @override
  String get twofaStatusOpenWeb => 'Enable on the Web app →';

  @override
  String get commonCopied => 'Copied';

  @override
  String get commonRemove => 'Remove';

  @override
  String get maintenanceOrphanCleanupTitle => 'Data Cleanup';

  @override
  String get maintenanceOrphanCleanupSubtitle => 'Detect and clean local orphan data';

  @override
  String get maintenanceOrphanRescan => 'Rescan';

  @override
  String get maintenanceOrphanEmpty => 'Local data is clean, no orphan data found';

  @override
  String get maintenanceOrphanGroupDb => 'Database orphans';

  @override
  String get maintenanceOrphanGroupFile => 'File orphans';

  @override
  String get maintenanceOrphanGroupSync => 'Sync state orphans';

  @override
  String maintenanceOrphanSummary(int count) {
    return 'Found $count issue(s)';
  }

  @override
  String maintenanceOrphanSummarySize(String size) {
    return 'Reclaimable space ~ $size';
  }

  @override
  String get maintenanceOrphanSelectAll => 'Select all';

  @override
  String get maintenanceOrphanDeselectAll => 'Deselect all';

  @override
  String get maintenanceOrphanDeleteOne => 'Delete this';

  @override
  String maintenanceOrphanSelectedHint(int count) {
    return '$count selected';
  }

  @override
  String get maintenanceOrphanCleanSelected => 'Clean selected';

  @override
  String get maintenanceOrphanConfirmTitle => 'Confirm cleanup';

  @override
  String maintenanceOrphanConfirmDeleteOne(String title) {
    return 'Delete \"$title\"? This cannot be undone.';
  }

  @override
  String maintenanceOrphanConfirmDeleteBatch(int count) {
    return 'Delete the $count selected item(s)? This cannot be undone.';
  }

  @override
  String maintenanceOrphanCleanSuccess(int count) {
    return 'Cleaned $count item(s)';
  }

  @override
  String maintenanceOrphanCleanPartial(int ok, int fail) {
    return 'Cleaned $ok item(s), $fail failed';
  }

  @override
  String get exchangeRatePageTitle => 'Exchange Rates';

  @override
  String get exchangeRateEntrySubtitle => 'Auto-fetched rates with manual override';

  @override
  String get baseCurrencyLabel => 'Primary Currency';

  @override
  String get rateSourceAuto => 'Local cache';

  @override
  String get rateSourceManual => 'Manual';

  @override
  String rateUpdatedAt(String date) {
    return 'Updated $date';
  }

  @override
  String get rateNotFetched => 'Not set';

  @override
  String get rateTapToSet => 'Tap to set manually';

  @override
  String get rateEditTitle => 'Edit Rate';

  @override
  String rateInverseHint(String base, String rate, String quote) {
    return 'Inverse: 1 $base ≈ $rate $quote';
  }

  @override
  String get rateResetToAuto => 'Remove manual rate';

  @override
  String get rateRefreshSuccess => 'Rates updated';

  @override
  String get rateRefreshFailed => 'Fetch failed, you can set rates manually';

  @override
  String get ratesEmptyHint => 'Rates appear here once your accounts use different currencies';

  @override
  String get rateDisclaimer => 'Rates come from local cache or manual entries and are not updated automatically. Conversion is for reference and may differ from bank rates.';

  @override
  String convertedNetWorth(String currency) {
    return 'Net worth (in $currency)';
  }

  @override
  String convertedFootnote(String date) {
    return 'Converted at $date rates, tap to manage';
  }

  @override
  String convertedPartialWarning(String currencies) {
    return '$currencies not converted, tap to set rates';
  }

  @override
  String get unconvertedBadge => 'Not converted';

  @override
  String get commonDetail => 'Detail';

  @override
  String get conversionDetailTitle => 'Conversion Details';

  @override
  String get assetConversionToggle => 'Convert to primary currency';

  @override
  String rateManualApplied(int count) {
    return 'Applied $count manual rates';
  }

  @override
  String get netWorthTrendTitle => 'Net Worth Trend';

  @override
  String get netWorthTrend3M => '3M';

  @override
  String get netWorthTrend6M => '6M';

  @override
  String get netWorthTrend12M => '12M';

  @override
  String get netWorthTrendAll => 'All';

  @override
  String get netWorthTrendLineNet => 'Net Worth';

  @override
  String get netWorthTrendLineAssets => 'Total Assets';

  @override
  String get netWorthTrendLineLiabilities => 'Total Liabilities';

  @override
  String get netWorthTrendMultiCurrencyNote => 'Historical net worth is the raw sum of each currency, not converted';

  @override
  String get txFlagExcludeFromStats => 'Exclude from income/expense';

  @override
  String get txFlagExcludeFromBudget => 'Exclude from budget';

  @override
  String get txFlagMoreOptions => 'More options';

  @override
  String get txFlagDialogTitle => 'Transaction flags';

  @override
  String get txFlagExcludeFromStatsHint => 'Excluded from stats, still counts toward balance';

  @override
  String get txFlagExcludeFromBudgetHint => 'Doesn\'t count against your budget';

  @override
  String get txFlagExcludedTag => 'Excluded';

  @override
  String get txFlagBudgetExcludedTag => 'No budget';

  @override
  String get txCurrencyLabel => 'Currency';

  @override
  String get txRateLabel => 'Rate';

  @override
  String get transactionDoubleTapHint => 'Double tap';

  @override
  String get transactionSavedNextCategory => 'Saved. Choose another category.';

  @override
  String txConvertedPreview(Object amount, Object currency) {
    return '≈ $amount $currency';
  }

  @override
  String get txRateMissingHint => 'Please enter the rate for this entry before saving';

  @override
  String get txCrossCurrencyTransferBlocked => 'Cross-currency transfers are not supported yet. Record two entries or use same-currency accounts.';

  @override
  String get ledgerBaseCurrencyLabel => 'Primary currency';

  @override
  String statsConvertedFootnote(Object currency) {
    return 'Includes foreign currency, converted to $currency at entry-time rates';
  }

  @override
  String get ledgerCurrencyChangeRecalcHint => 'Changing the base currency will reconvert all history at current rates';

  @override
  String get recalcForeignTxBanner => 'Unconverted foreign-currency transactions detected in this ledger';

  @override
  String get recalcForeignTxAction => 'Reconvert at current rates';

  @override
  String recalcForeignTxDone(Object count) {
    return 'Reconverted $count foreign-currency transactions';
  }

  @override
  String get txCurrencyPickerTitle => 'Select currency';

  @override
  String recalcSyncCountHint(Object count) {
    return '$count transactions will be reconverted and synced';
  }

  @override
  String get exportCsvHeaderCurrency => 'Currency';

  @override
  String get importFieldCurrency => 'Currency';

  @override
  String get currencyMOP => 'Macau Pataca';

  @override
  String get currencyMNT => 'Mongolian Tughrik';

  @override
  String get currencyKPW => 'North Korean Won';

  @override
  String get currencyKHR => 'Cambodian Riel';

  @override
  String get currencyLAK => 'Lao Kip';

  @override
  String get currencyBND => 'Bruneian Dollar';

  @override
  String get currencyNPR => 'Nepalese Rupee';

  @override
  String get currencyBTN => 'Bhutanese Ngultrum';

  @override
  String get currencyMVR => 'Maldivian Rufiyaa';

  @override
  String get currencyAFN => 'Afghan Afghani';

  @override
  String get currencyUZS => 'Uzbekistani Som';

  @override
  String get currencyTJS => 'Tajikistani Somoni';

  @override
  String get currencyTMT => 'Turkmenistani Manat';

  @override
  String get currencyKGS => 'Kyrgyzstani Som';

  @override
  String get currencyQAR => 'Qatari Riyal';

  @override
  String get currencyKWD => 'Kuwaiti Dinar';

  @override
  String get currencyBHD => 'Bahraini Dinar';

  @override
  String get currencyOMR => 'Omani Rial';

  @override
  String get currencyJOD => 'Jordanian Dinar';

  @override
  String get currencyLBP => 'Lebanese Pound';

  @override
  String get currencyIQD => 'Iraqi Dinar';

  @override
  String get currencyIRR => 'Iranian Rial';

  @override
  String get currencyYER => 'Yemeni Rial';

  @override
  String get currencySYP => 'Syrian Pound';

  @override
  String get currencyGEL => 'Georgian Lari';

  @override
  String get currencyAMD => 'Armenian Dram';

  @override
  String get currencyAZN => 'Azerbaijan Manat';

  @override
  String get currencyRON => 'Romanian Leu';

  @override
  String get currencyBGN => 'Bulgarian Lev';

  @override
  String get currencyRSD => 'Serbian Dinar';

  @override
  String get currencyISK => 'Icelandic Krona';

  @override
  String get currencyMDL => 'Moldovan Leu';

  @override
  String get currencyALL => 'Albanian Lek';

  @override
  String get currencyMKD => 'Macedonian Denar';

  @override
  String get currencyBAM => 'Bosnian Convertible Mark';

  @override
  String get currencyGIP => 'Gibraltar Pound';

  @override
  String get currencyGTQ => 'Guatemalan Quetzal';

  @override
  String get currencyHNL => 'Honduran Lempira';

  @override
  String get currencyNIO => 'Nicaraguan Cordoba';

  @override
  String get currencyCRC => 'Costa Rican Colon';

  @override
  String get currencyPAB => 'Panamanian Balboa';

  @override
  String get currencyDOP => 'Dominican Peso';

  @override
  String get currencyCUP => 'Cuban Peso';

  @override
  String get currencyJMD => 'Jamaican Dollar';

  @override
  String get currencyTTD => 'Trinidadian Dollar';

  @override
  String get currencyBSD => 'Bahamian Dollar';

  @override
  String get currencyBBD => 'Barbadian or Bajan Dollar';

  @override
  String get currencyBZD => 'Belizean Dollar';

  @override
  String get currencyHTG => 'Haitian Gourde';

  @override
  String get currencyXCD => 'East Caribbean Dollar';

  @override
  String get currencyKYD => 'Caymanian Dollar';

  @override
  String get currencyAWG => 'Aruban or Dutch Guilder';

  @override
  String get currencyANG => 'Dutch Guilder';

  @override
  String get currencyBMD => 'Bermudian Dollar';

  @override
  String get currencyUYU => 'Uruguayan Peso';

  @override
  String get currencyPYG => 'Paraguayan Guarani';

  @override
  String get currencyBOB => 'Bolivian Bolíviano';

  @override
  String get currencyVES => 'Venezuelan Bolívar';

  @override
  String get currencyGYD => 'Guyanese Dollar';

  @override
  String get currencySRD => 'Surinamese Dollar';

  @override
  String get currencyFJD => 'Fijian Dollar';

  @override
  String get currencyPGK => 'Papua New Guinean Kina';

  @override
  String get currencySBD => 'Solomon Islander Dollar';

  @override
  String get currencyTOP => 'Tongan Pa\'anga';

  @override
  String get currencyVUV => 'Ni-Vanuatu Vatu';

  @override
  String get currencyWST => 'Samoan Tala';

  @override
  String get currencyXPF => 'CFP Franc';

  @override
  String get currencyKES => 'Kenyan Shilling';

  @override
  String get currencyGHS => 'Ghanaian Cedi';

  @override
  String get currencyMAD => 'Moroccan Dirham';

  @override
  String get currencyDZD => 'Algerian Dinar';

  @override
  String get currencyTND => 'Tunisian Dinar';

  @override
  String get currencyLYD => 'Libyan Dinar';

  @override
  String get currencyETB => 'Ethiopian Birr';

  @override
  String get currencyUGX => 'Ugandan Shilling';

  @override
  String get currencyTZS => 'Tanzanian Shilling';

  @override
  String get currencyRWF => 'Rwandan Franc';

  @override
  String get currencyXAF => 'Central African CFA Franc';

  @override
  String get currencyXOF => 'West African CFA Franc';

  @override
  String get currencyMUR => 'Mauritian Rupee';

  @override
  String get currencyBWP => 'Botswana Pula';

  @override
  String get currencyNAD => 'Namibian Dollar';

  @override
  String get currencyZMW => 'Zambian Kwacha';

  @override
  String get currencyMWK => 'Malawian Kwacha';

  @override
  String get currencyMZN => 'Mozambican Metical';

  @override
  String get currencyAOA => 'Angolan Kwanza';

  @override
  String get currencyCDF => 'Congolese Franc';

  @override
  String get currencyGMD => 'Gambian Dalasi';

  @override
  String get currencyGNF => 'Guinean Franc';

  @override
  String get currencyLRD => 'Liberian Dollar';

  @override
  String get currencySLE => 'Sierra Leonean Leone';

  @override
  String get currencySDG => 'Sudanese Pound';

  @override
  String get currencySSP => 'South Sudanese Pound';

  @override
  String get currencySOS => 'Somali Shilling';

  @override
  String get currencyDJF => 'Djiboutian Franc';

  @override
  String get currencyERN => 'Eritrean Nakfa';

  @override
  String get currencyBIF => 'Burundian Franc';

  @override
  String get currencyCVE => 'Cape Verdean Escudo';

  @override
  String get currencySTN => 'Sao Tomean Dobra';

  @override
  String get currencySCR => 'Seychellois Rupee';

  @override
  String get currencyKMF => 'Comorian Franc';

  @override
  String get currencyLSL => 'Basotho Loti';

  @override
  String get currencySZL => 'Swazi Lilangeni';

  @override
  String get currencyMGA => 'Malagasy Ariary';

  @override
  String get currencyMRU => 'Mauritanian Ouguiya';

  @override
  String get annualReportInsightsTitle => 'Annual Insights';

  @override
  String get annualReportInsightsSubtitle => 'Discover your spending habits through data';

  @override
  String get annualReportAveragePerRecord => 'Average Spending per Record';

  @override
  String get annualReportAveragePerRecordDesc => 'Average amount per bookkeeping record';

  @override
  String get annualReportDailyAverage => 'Daily Average Spending';

  @override
  String get annualReportDailyAverageDesc => 'Average amount spent each day';

  @override
  String get annualReportMonthlyAverage => 'Monthly Average Spending';

  @override
  String get annualReportMonthlyAverageDesc => 'Average amount spent each month';

  @override
  String get annualReportBusiestMonth => 'Most Active Month';

  @override
  String get annualReportBusiestMonthDesc => 'Month with the most bookkeeping activity';

  @override
  String get annualReportCategoryCount => 'Spending Categories';

  @override
  String get annualReportCategoryCountDesc => 'Number of spending categories used';

  @override
  String annualReportCategoryCountValue(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count categories',
      one: '1 category',
    );
    return '$_temp0';
  }

  @override
  String get annualReportSavingsRate => 'Savings Rate';

  @override
  String get annualReportSavingsRatePositive => 'The share of your income saved this year';

  @override
  String get annualReportSavingsRateNegative => 'Spending exceeded income this year';

  @override
  String get annualReportComparisonTitle => 'Income vs. Expense';

  @override
  String get annualReportComparisonSubtitle => 'Compare monthly income and expenses';

  @override
  String get annualReportHighestIncome => 'Highest Income';

  @override
  String get annualReportHighestExpense => 'Highest Expense';

  @override
  String get annualReportConsistency => 'Bookkeeping Streak';

  @override
  String annualReportDaysValue(int days) {
    String _temp0 = intl.Intl.pluralLogic(
      days,
      locale: localeName,
      other: '$days days',
      one: '1 day',
    );
    return '$_temp0';
  }

  @override
  String get annualReportSavingsPositive => 'Congratulations on your savings';

  @override
  String get annualReportSavingsNegative => 'Spending exceeded income this year';

  @override
  String get annualReportMonthlyExpenseTrend => 'Monthly Spending Trend';

  @override
  String get annualReportAchieved => 'Achieved';

  @override
  String get annualReportNotAchieved => 'Not Achieved';

  @override
  String get annualReportDownloadHint => 'Scan to download BeeCount and start bookkeeping';

  @override
  String get attachmentKeepOriginal => 'Keep Original Attachments';

  @override
  String get attachmentKeepOriginalDesc => 'Uses more storage and sync data';

  @override
  String get transactionCopyAction => 'Copy as new transaction';

  @override
  String get transactionCopyUnavailable => 'This transaction cannot be copied. Check the ledger and your access.';

  @override
  String creditCardReminderDueSoonTitle(String accountName) {
    return '$accountName payment due soon';
  }

  @override
  String creditCardReminderDueBody(int paymentDueDay) {
    return 'Payment due on day $paymentDueDay of each month, please repay on time';
  }

  @override
  String get reminderBackupChannelName => 'Recording reminder (backup)';

  @override
  String get reminderBackupChannelDescription => 'Backup channel for recording reminders';

  @override
  String get appLinkOpenImageBilling => 'Open image billing';

  @override
  String get appLinkOpenCameraBilling => 'Open camera billing';

  @override
  String get appLinkOpenAiAssistant => 'Open AI assistant';

  @override
  String get appLinkOpenManualBilling => 'Open manual billing';

  @override
  String get appLinkNoTargetPage => 'No target page provided';

  @override
  String appLinkOpenPage(String page) {
    return 'Open page: $page';
  }

  @override
  String appLinkUnknownAction(String action) {
    return 'Unknown action: $action';
  }

  @override
  String get appLinkNoLedger => 'Select a ledger first';

  @override
  String get appLinkMissingAmount => 'Not recorded: enter a valid amount';

  @override
  String get appLinkMissingCategory => 'Not recorded: specify a category';

  @override
  String appLinkCategoryNotFound(String name) {
    return 'Not recorded: category \"$name\" does not exist';
  }

  @override
  String get appLinkTypeIncome => 'Income';

  @override
  String get appLinkTypeExpense => 'Expense';

  @override
  String get appLinkTypeTransfer => 'Transfer';

  @override
  String appLinkRecorded(String type, String amount) {
    return 'Recorded $type $amount';
  }

  @override
  String get appLinkRecordSuccess => 'Transaction recorded';

  @override
  String appLinkParamError(String message) {
    return 'Invalid parameters: $message';
  }

  @override
  String appLinkRecordFailed(String error) {
    return 'Recording failed: $error';
  }

  @override
  String get appLinkTextDone => 'Text processing done';

  @override
  String appLinkTextFailed(String error) {
    return 'Text billing failed: $error';
  }

  @override
  String get appLinkNoText => 'No text content provided';

  @override
  String orphanBudgetTitle(String id) {
    return 'Budget #$id';
  }

  @override
  String orphanBudgetNoLedger(String type, String amount, String id) {
    return '$type · ¥$amount · ledger deleted (id=$id)';
  }

  @override
  String orphanAttachmentRowTitle(String id) {
    return 'Attachment row #$id';
  }

  @override
  String orphanAttachmentNoTx(String fileName, String id) {
    return '$fileName · transaction deleted (txId=$id)';
  }

  @override
  String orphanTagLinkTitle(String id) {
    return 'Tag link #$id';
  }

  @override
  String orphanTagLinkNoTx(String txId, String tagId) {
    return 'transaction deleted (txId=$txId, tagId=$tagId)';
  }

  @override
  String orphanTagLinkNoTag(String txId, String tagId) {
    return 'tag deleted (txId=$txId, tagId=$tagId)';
  }

  @override
  String orphanTxTitle(String id) {
    return 'Transaction #$id';
  }

  @override
  String orphanTxNoMissing(String type, String amount, String missing) {
    return '$type · ¥$amount · $missing deleted';
  }

  @override
  String orphanTxNoCategory(String type, String amount, String id) {
    return '$type · ¥$amount · category deleted (categoryId=$id)';
  }

  @override
  String orphanSubcategoryTitle(String name, String id) {
    return 'Subcategory \"$name\" #$id';
  }

  @override
  String orphanSubcategoryNoParent(String kind, String id) {
    return '$kind · parent category deleted (parentId=$id)';
  }

  @override
  String orphanBudgetNoCategory(String type, String amount, String id) {
    return '$type · ¥$amount · category deleted (categoryId=$id)';
  }

  @override
  String orphanSharedSubcategoryTitle(String name) {
    return 'Shared subcategory \"$name\"';
  }

  @override
  String orphanSharedSubcategoryNoParent(String id) {
    return 'parent category deleted (parentSyncId=$id)';
  }

  @override
  String get orphanSharedTagOverrideTitle => 'Shared tag override';

  @override
  String orphanSharedTagNoTx(String txId, String tagId) {
    return 'transaction deleted (txSyncId=$txId, tagSyncId=$tagId)';
  }

  @override
  String get orphanFileNoRef => 'Attachment file has no DB reference';

  @override
  String get orphanCustomIconNoRef => 'Category custom icon has no DB reference';

  @override
  String get orphanSharedIconNoRef => 'Shared category icon cache has no DB reference';

  @override
  String orphanSyncChangeTitle(String id) {
    return 'Sync change #$id';
  }

  @override
  String orphanSyncChangeNoEntity(String entityType, String action, String id) {
    return '$entityType · $action · entity deleted (syncId=$id)';
  }

  @override
  String get customIconFileMissing => 'Image file does not exist';

  @override
  String get customIconFileTooLarge => 'Image file is too large (max 5MB)';

  @override
  String get customIconUnsupportedFormat => 'Unsupported image format';

  @override
  String get customIconCompressFailed => 'Image compression failed';

  @override
  String customIconSaveFailed(String error) {
    return 'Failed to save icon: $error';
  }

  @override
  String get sharePosterAppText => 'BeeCount - Simple Ledger';

  @override
  String get sharePosterDefaultLedgerName => 'Default Ledger';

  @override
  String get xlsxEmptyFile => 'The Excel file is empty or unreadable';

  @override
  String get xlsxEmptySheet => 'The worksheet is empty';

  @override
  String xlsxParseFailed(String error) {
    return 'Failed to parse Excel file: $error';
  }

  @override
  String rateFetchAllSourcesFailed(String errors) {
    return 'All sources failed: $errors';
  }

  @override
  String rateFetchSourcePayloadInvalid(String source) {
    return 'Unexpected payload from exchange rate source $source';
  }

  @override
  String get attachmentArchiveMissing => 'The archive file does not exist';

  @override
  String get attachmentArchiveInvalidFormat => 'Invalid archive format: metadata.json is missing';

  @override
  String get categoryPackageCompressFailed => 'Compression failed';

  @override
  String get categoryPackageFileMissing => 'The file does not exist';

  @override
  String get categoryPackageMissingYaml => 'Invalid category package: categories.yaml is missing';

  @override
  String get categoryPackageInvalidFormat => 'Invalid configuration file format';

  @override
  String get configExportInvalidYaml => 'Invalid YAML format';

  @override
  String get configExportTransferCategoryMissing => 'The transfer category does not exist';

  @override
  String configExportFileMissing(String path) {
    return 'The file does not exist: $path';
  }

  @override
  String ledgerTargetIdExists(String id) {
    return 'Target ledger ID already exists: $id';
  }

  @override
  String get ledgerMissing => 'The ledger does not exist';

  @override
  String get accountMissing => 'The account does not exist';

  @override
  String get logExportTitle => '=== BeeCount log export ===';

  @override
  String logExportTime(String time) {
    return 'Exported at: $time';
  }

  @override
  String logExportCount(int count) {
    return 'Log entries: $count';
  }

  @override
  String get creditCardOverpayment => 'Overpayment';

  @override
  String get creditCardDebtHint => 'Enter your total outstanding debt as a positive amount; enter 0 if none.';

  @override
  String get creditCardOverpaymentHint => 'Enter the positive amount remaining on the card after overpaying.';

  @override
  String get creditCardAmountInvalid => 'Enter a valid amount; credit-card debt or overpayment must be non-negative.';

  @override
  String get creditCardReminderDueRequired => 'Set a payment due day before enabling reminders.';

  @override
  String get creditCardEstimateHint => 'Estimated from recorded transactions; check your bank for the actual available amount.';

  @override
  String get accountAllFlows => 'All transactions';

  @override
  String get accountOutflows => 'Expenses & transfers out';

  @override
  String get accountInflows => 'Income & transfers in';

  @override
  String get positiveAssetBalance => 'Positive asset balances';

  @override
  String get negativeAssetBalanceHint => 'Negative account balances are deducted from total assets.';

  @override
  String get importAccountCurrencyPreview => 'Confirm account currencies';

  @override
  String get importAccountCurrencyPreviewHint => 'Accounts use statement currencies. Conflicting currencies are split without changing existing accounts. Missing currencies follow an existing account, an unambiguous account currency, or the ledger currency.';

  @override
  String get importCurrencyInvalid => 'Invalid currency; correct this row:';

  @override
  String get transactionReceiptAction => 'Generate receipt';

  @override
  String get transactionReceiptTitle => 'Transaction receipt';

  @override
  String get transactionReceiptFields => 'Choose what appears on your receipt';

  @override
  String get transactionReceiptLedger => 'Ledger';

  @override
  String get transactionReceiptTags => 'Tags';

  @override
  String get transactionReceiptFooter => 'Little moments, thoughtfully recorded';

  @override
  String get transactionReceiptUnavailable => 'Unable to generate a receipt. Check that this transaction still exists.';

  @override
  String get transactionReceiptShareFailed => 'Unable to share. Please try again.';
  @override
  String get accountTypePrepaid => 'Prepaid card';
}
