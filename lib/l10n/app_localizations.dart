import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_ko.dart';
import 'app_localizations_zh.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale) : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static const LocalizationsDelegate<AppLocalizations> delegate = _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates = <LocalizationsDelegate<dynamic>>[
    delegate,
    GlobalMaterialLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
  ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('ko'),
    Locale('zh'),
    Locale('zh', 'TW')
  ];














































  /// No description provided for @aboutPrivacyPolicy.
  ///
  /// In en, this message translates to:
  /// **'Privacy Policy'**
  String get aboutPrivacyPolicy;

  /// No description provided for @aboutChangelog.
  ///
  /// In en, this message translates to:
  /// **'Changelog'**
  String get aboutChangelog;

  /// The application title
  ///
  /// In en, this message translates to:
  /// **'Bee Accounting'**
  String get appTitle;

  /// No description provided for @tabHome.
  ///
  /// In en, this message translates to:
  /// **'Home'**
  String get tabHome;

  /// No description provided for @tabInsights.
  ///
  /// In en, this message translates to:
  /// **'Stats'**
  String get tabInsights;

  /// No description provided for @tabAssets.
  ///
  /// In en, this message translates to:
  /// **'Assets'**
  String get tabAssets;

  /// No description provided for @tabRecord.
  ///
  /// In en, this message translates to:
  /// **'Record'**
  String get tabRecord;

  /// No description provided for @tabMine.
  ///
  /// In en, this message translates to:
  /// **'Mine'**
  String get tabMine;

  /// No description provided for @commonCancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get commonCancel;

  /// No description provided for @commonConfirm.
  ///
  /// In en, this message translates to:
  /// **'Confirm'**
  String get commonConfirm;

  /// No description provided for @commonSave.
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get commonSave;

  /// No description provided for @commonDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get commonDelete;

  /// No description provided for @commonAdd.
  ///
  /// In en, this message translates to:
  /// **'Add'**
  String get commonAdd;

  /// No description provided for @commonEdit.
  ///
  /// In en, this message translates to:
  /// **'Edit'**
  String get commonEdit;

  /// No description provided for @commonMore.
  ///
  /// In en, this message translates to:
  /// **'More'**
  String get commonMore;

  /// No description provided for @commonOk.
  ///
  /// In en, this message translates to:
  /// **'OK'**
  String get commonOk;

  /// No description provided for @commonKnow.
  ///
  /// In en, this message translates to:
  /// **'Got it'**
  String get commonKnow;

  /// No description provided for @commonNo.
  ///
  /// In en, this message translates to:
  /// **'No'**
  String get commonNo;

  /// No description provided for @commonEmpty.
  ///
  /// In en, this message translates to:
  /// **'No data'**
  String get commonEmpty;

  /// No description provided for @commonError.
  ///
  /// In en, this message translates to:
  /// **'Error'**
  String get commonError;

  /// No description provided for @commonSuccess.
  ///
  /// In en, this message translates to:
  /// **'Success'**
  String get commonSuccess;

  /// No description provided for @commonFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed'**
  String get commonFailed;

  /// No description provided for @commonBack.
  ///
  /// In en, this message translates to:
  /// **'Back'**
  String get commonBack;

  /// No description provided for @commonNext.
  ///
  /// In en, this message translates to:
  /// **'Next'**
  String get commonNext;

  /// No description provided for @fabActionCamera.
  ///
  /// In en, this message translates to:
  /// **'Camera'**
  String get fabActionCamera;

  /// No description provided for @fabActionGallery.
  ///
  /// In en, this message translates to:
  /// **'Gallery'**
  String get fabActionGallery;

































  /// No description provided for @commonPrevious.
  ///
  /// In en, this message translates to:
  /// **'Previous'**
  String get commonPrevious;

  /// No description provided for @commonFinish.
  ///
  /// In en, this message translates to:
  /// **'Finish'**
  String get commonFinish;

  /// No description provided for @commonClose.
  ///
  /// In en, this message translates to:
  /// **'Close'**
  String get commonClose;

  /// No description provided for @commonOther.
  ///
  /// In en, this message translates to:
  /// **'Other'**
  String get commonOther;

  /// No description provided for @commonYesterday.
  ///
  /// In en, this message translates to:
  /// **'Yesterday'**
  String get commonYesterday;

  /// No description provided for @commonSearch.
  ///
  /// In en, this message translates to:
  /// **'Search'**
  String get commonSearch;

  /// No description provided for @commonNoteHint.
  ///
  /// In en, this message translates to:
  /// **'Note...'**
  String get commonNoteHint;

  /// No description provided for @commonSettings.
  ///
  /// In en, this message translates to:
  /// **'Settings'**
  String get commonSettings;

  /// No description provided for @commonGoSettings.
  ///
  /// In en, this message translates to:
  /// **'Go to Settings'**
  String get commonGoSettings;

  /// No description provided for @commonLanguage.
  ///
  /// In en, this message translates to:
  /// **'Language'**
  String get commonLanguage;

  /// No description provided for @commonCurrent.
  ///
  /// In en, this message translates to:
  /// **'Current'**
  String get commonCurrent;

  /// No description provided for @commonTutorial.
  ///
  /// In en, this message translates to:
  /// **'Tutorial'**
  String get commonTutorial;

  /// No description provided for @commonConfigure.
  ///
  /// In en, this message translates to:
  /// **'Configure'**
  String get commonConfigure;

  /// No description provided for @commonPressAgainToExit.
  ///
  /// In en, this message translates to:
  /// **'Press again to exit'**
  String get commonPressAgainToExit;

  /// No description provided for @commonWeekdayMonday.
  ///
  /// In en, this message translates to:
  /// **'Monday'**
  String get commonWeekdayMonday;

  /// No description provided for @commonWeekdayTuesday.
  ///
  /// In en, this message translates to:
  /// **'Tuesday'**
  String get commonWeekdayTuesday;

  /// No description provided for @commonWeekdayWednesday.
  ///
  /// In en, this message translates to:
  /// **'Wednesday'**
  String get commonWeekdayWednesday;

  /// No description provided for @commonWeekdayThursday.
  ///
  /// In en, this message translates to:
  /// **'Thursday'**
  String get commonWeekdayThursday;

  /// No description provided for @commonWeekdayFriday.
  ///
  /// In en, this message translates to:
  /// **'Friday'**
  String get commonWeekdayFriday;

  /// No description provided for @commonWeekdaySaturday.
  ///
  /// In en, this message translates to:
  /// **'Saturday'**
  String get commonWeekdaySaturday;

  /// No description provided for @commonWeekdaySunday.
  ///
  /// In en, this message translates to:
  /// **'Sunday'**
  String get commonWeekdaySunday;

  /// No description provided for @homeIncome.
  ///
  /// In en, this message translates to:
  /// **'Income'**
  String get homeIncome;

  /// No description provided for @homeExpense.
  ///
  /// In en, this message translates to:
  /// **'Expense'**
  String get homeExpense;

  /// No description provided for @homeBalance.
  ///
  /// In en, this message translates to:
  /// **'Balance'**
  String get homeBalance;

  /// No description provided for @homeNoRecords.
  ///
  /// In en, this message translates to:
  /// **'No records yet'**
  String get homeNoRecords;

  /// No description provided for @homeSelectDate.
  ///
  /// In en, this message translates to:
  /// **'Select date'**
  String get homeSelectDate;

  /// No description provided for @homeAppTitle.
  ///
  /// In en, this message translates to:
  /// **'Bee Accounting'**
  String get homeAppTitle;

  /// No description provided for @homeSearch.
  ///
  /// In en, this message translates to:
  /// **'Search'**
  String get homeSearch;

  /// No description provided for @homeYear.
  ///
  /// In en, this message translates to:
  /// **'{year}'**
  String homeYear(int year);

  /// No description provided for @homeMonth.
  ///
  /// In en, this message translates to:
  /// **'{month}M'**
  String homeMonth(String month);

  /// No description provided for @homeNoRecordsSubtext.
  ///
  /// In en, this message translates to:
  /// **'Tap the plus button at the bottom to add a record'**
  String get homeNoRecordsSubtext;

  /// No description provided for @homeLastMonthReportSubtitle.
  ///
  /// In en, this message translates to:
  /// **'View and share last month\'s report'**
  String get homeLastMonthReportSubtitle;

  /// No description provided for @homeLastMonthReportView.
  ///
  /// In en, this message translates to:
  /// **'View'**
  String get homeLastMonthReportView;

  /// No description provided for @homeAnnualReportReminder.
  ///
  /// In en, this message translates to:
  /// **'Your {year} Annual Report is ready'**
  String homeAnnualReportReminder(int year);

  /// No description provided for @homeAnnualReportView.
  ///
  /// In en, this message translates to:
  /// **'View'**
  String get homeAnnualReportView;

  /// No description provided for @widgetTodayExpense.
  ///
  /// In en, this message translates to:
  /// **'Today\'s Expense'**
  String get widgetTodayExpense;

  /// No description provided for @widgetConsumptionRhythmTitle.
  ///
  /// In en, this message translates to:
  /// **'Spending Rhythm'**
  String get widgetConsumptionRhythmTitle;

  /// No description provided for @widgetConsumptionRhythmRange.
  ///
  /// In en, this message translates to:
  /// **'Last 30 days'**
  String get widgetConsumptionRhythmRange;

  /// No description provided for @widgetConsumptionRhythmStable.
  ///
  /// In en, this message translates to:
  /// **'Spending is steady'**
  String get widgetConsumptionRhythmStable;

  /// No description provided for @widgetConsumptionRhythmIncrease.
  ///
  /// In en, this message translates to:
  /// **'Faster than last week'**
  String get widgetConsumptionRhythmIncrease;

  /// No description provided for @widgetConsumptionRhythmDecrease.
  ///
  /// In en, this message translates to:
  /// **'Steadier than last week'**
  String get widgetConsumptionRhythmDecrease;

  /// No description provided for @widgetConsumptionRhythmEmpty.
  ///
  /// In en, this message translates to:
  /// **'No spending in the last 30 days'**
  String get widgetConsumptionRhythmEmpty;

  /// No description provided for @widgetBeeTrailTitle.
  ///
  /// In en, this message translates to:
  /// **'Record Bee Trail'**
  String get widgetBeeTrailTitle;

  /// No description provided for @widgetBeeTrailStreakSuffix.
  ///
  /// In en, this message translates to:
  /// **'days'**
  String get widgetBeeTrailStreakSuffix;

  /// No description provided for @widgetBeeTrailCompletion.
  ///
  /// In en, this message translates to:
  /// **'28-day completion'**
  String get widgetBeeTrailCompletion;

  /// No description provided for @widgetBeeTrailEmpty.
  ///
  /// In en, this message translates to:
  /// **'Add a record to light the first cell'**
  String get widgetBeeTrailEmpty;

  /// No description provided for @widgetTodayIncome.
  ///
  /// In en, this message translates to:
  /// **'Today\'s Income'**
  String get widgetTodayIncome;

  /// No description provided for @widgetMonthExpense.
  ///
  /// In en, this message translates to:
  /// **'Month\'s Expense'**
  String get widgetMonthExpense;

  /// No description provided for @widgetMonthIncome.
  ///
  /// In en, this message translates to:
  /// **'Month\'s Income'**
  String get widgetMonthIncome;

  /// No description provided for @widgetMonthSuffix.
  ///
  /// In en, this message translates to:
  /// **''**
  String get widgetMonthSuffix;

  /// No description provided for @widgetToday.
  ///
  /// In en, this message translates to:
  /// **'Today'**
  String get widgetToday;

  /// No description provided for @widgetQuickAddLabel.
  ///
  /// In en, this message translates to:
  /// **'Add'**
  String get widgetQuickAddLabel;

  /// No description provided for @widgetBudgetTotal.
  ///
  /// In en, this message translates to:
  /// **'Total'**
  String get widgetBudgetTotal;

  /// No description provided for @widgetBudgetRemaining.
  ///
  /// In en, this message translates to:
  /// **'Left'**
  String get widgetBudgetRemaining;

  /// No description provided for @widgetNoBudget.
  ///
  /// In en, this message translates to:
  /// **'No Budget'**
  String get widgetNoBudget;

  /// No description provided for @widgetNoTransactions.
  ///
  /// In en, this message translates to:
  /// **'No Transactions'**
  String get widgetNoTransactions;

  /// No description provided for @widgetRecentTransactions.
  ///
  /// In en, this message translates to:
  /// **'Recent Transactions'**
  String get widgetRecentTransactions;

  /// No description provided for @widgetNoAccounts.
  ///
  /// In en, this message translates to:
  /// **'No Accounts'**
  String get widgetNoAccounts;

  /// No description provided for @searchTitle.
  ///
  /// In en, this message translates to:
  /// **'Search'**
  String get searchTitle;

  /// No description provided for @searchHint.
  ///
  /// In en, this message translates to:
  /// **'Search notes, categories or amounts...'**
  String get searchHint;

  /// No description provided for @searchCategoryHint.
  ///
  /// In en, this message translates to:
  /// **'Search category name...'**
  String get searchCategoryHint;

  /// No description provided for @searchCategoryFilter.
  ///
  /// In en, this message translates to:
  /// **'Category Filter'**
  String get searchCategoryFilter;

  /// No description provided for @searchMinAmount.
  ///
  /// In en, this message translates to:
  /// **'Min amount'**
  String get searchMinAmount;

  /// No description provided for @searchMaxAmount.
  ///
  /// In en, this message translates to:
  /// **'Max amount'**
  String get searchMaxAmount;

  /// No description provided for @searchNoInput.
  ///
  /// In en, this message translates to:
  /// **'Enter keywords to start searching'**
  String get searchNoInput;

  /// No description provided for @searchNoResults.
  ///
  /// In en, this message translates to:
  /// **'No matching results found'**
  String get searchNoResults;

  /// No description provided for @searchBatchMode.
  ///
  /// In en, this message translates to:
  /// **'Batch Operations'**
  String get searchBatchMode;

  /// No description provided for @searchBatchModeWithCount.
  ///
  /// In en, this message translates to:
  /// **'Batch Operations ({selected}/{total})'**
  String searchBatchModeWithCount(Object selected, Object total);

  /// No description provided for @searchExitBatchMode.
  ///
  /// In en, this message translates to:
  /// **'Exit Batch Mode'**
  String get searchExitBatchMode;

  /// No description provided for @searchSelectAll.
  ///
  /// In en, this message translates to:
  /// **'Select All'**
  String get searchSelectAll;

  /// No description provided for @searchDeselectAll.
  ///
  /// In en, this message translates to:
  /// **'Deselect All'**
  String get searchDeselectAll;

  /// No description provided for @searchSelectedCount.
  ///
  /// In en, this message translates to:
  /// **'{count} selected'**
  String searchSelectedCount(Object count);

  /// No description provided for @searchBatchSetNote.
  ///
  /// In en, this message translates to:
  /// **'Set Note'**
  String get searchBatchSetNote;

  /// No description provided for @searchBatchChangeCategory.
  ///
  /// In en, this message translates to:
  /// **'Change Category'**
  String get searchBatchChangeCategory;

  /// No description provided for @searchBatchDeleteConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Confirm Delete'**
  String get searchBatchDeleteConfirmTitle;

  /// No description provided for @searchBatchDeleteConfirmMessage.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete the selected {count} transactions?\nThis action cannot be undone.'**
  String searchBatchDeleteConfirmMessage(Object count);

  /// No description provided for @searchBatchSetNoteTitle.
  ///
  /// In en, this message translates to:
  /// **'Batch Set Note'**
  String get searchBatchSetNoteTitle;

  /// No description provided for @searchBatchSetNoteMessage.
  ///
  /// In en, this message translates to:
  /// **'Set the same note for the selected {count} transactions'**
  String searchBatchSetNoteMessage(Object count);

  /// No description provided for @searchBatchSetNoteHint.
  ///
  /// In en, this message translates to:
  /// **'Enter note content (leave empty to clear notes)'**
  String get searchBatchSetNoteHint;

  /// No description provided for @searchBatchDeleteSuccess.
  ///
  /// In en, this message translates to:
  /// **'Successfully deleted {count} transactions'**
  String searchBatchDeleteSuccess(Object count);

  /// No description provided for @searchBatchDeleteFailed.
  ///
  /// In en, this message translates to:
  /// **'Delete failed: {error}'**
  String searchBatchDeleteFailed(Object error);

  /// No description provided for @searchBatchSetNoteSuccess.
  ///
  /// In en, this message translates to:
  /// **'Successfully set note for {count} transactions'**
  String searchBatchSetNoteSuccess(Object count);

  /// No description provided for @searchBatchSetNoteFailed.
  ///
  /// In en, this message translates to:
  /// **'Set note failed: {error}'**
  String searchBatchSetNoteFailed(Object error);

  /// No description provided for @searchBatchChangeCategorySuccess.
  ///
  /// In en, this message translates to:
  /// **'Successfully changed category for {count} transactions'**
  String searchBatchChangeCategorySuccess(Object count);

  /// No description provided for @searchBatchChangeCategoryFailed.
  ///
  /// In en, this message translates to:
  /// **'Change category failed: {error}'**
  String searchBatchChangeCategoryFailed(Object error);

  /// No description provided for @searchResultsCount.
  ///
  /// In en, this message translates to:
  /// **'{count} results'**
  String searchResultsCount(Object count);

  /// No description provided for @searchSummaryIncome.
  ///
  /// In en, this message translates to:
  /// **'Income'**
  String get searchSummaryIncome;

  /// No description provided for @searchSummaryExpense.
  ///
  /// In en, this message translates to:
  /// **'Expense'**
  String get searchSummaryExpense;

  /// No description provided for @searchFilterTitle.
  ///
  /// In en, this message translates to:
  /// **'Filter'**
  String get searchFilterTitle;

  /// No description provided for @searchAmountFilter.
  ///
  /// In en, this message translates to:
  /// **'Amount Filter'**
  String get searchAmountFilter;

  /// No description provided for @searchDateFilter.
  ///
  /// In en, this message translates to:
  /// **'Date Filter'**
  String get searchDateFilter;

  /// No description provided for @searchStartDate.
  ///
  /// In en, this message translates to:
  /// **'Start Date'**
  String get searchStartDate;

  /// No description provided for @searchEndDate.
  ///
  /// In en, this message translates to:
  /// **'End Date'**
  String get searchEndDate;

  /// No description provided for @searchNotSet.
  ///
  /// In en, this message translates to:
  /// **'Not Set'**
  String get searchNotSet;

  /// No description provided for @searchClearFilter.
  ///
  /// In en, this message translates to:
  /// **'Clear Filter'**
  String get searchClearFilter;

  /// No description provided for @searchBatchCategoryTransferError.
  ///
  /// In en, this message translates to:
  /// **'Selected transactions contain transfers, cannot change category'**
  String get searchBatchCategoryTransferError;

  /// No description provided for @searchBatchCategoryTypeError.
  ///
  /// In en, this message translates to:
  /// **'Selected transactions have different types, please select all income or all expense'**
  String get searchBatchCategoryTypeError;

  /// No description provided for @searchDateStart.
  ///
  /// In en, this message translates to:
  /// **'Start'**
  String get searchDateStart;

  /// No description provided for @searchDateEnd.
  ///
  /// In en, this message translates to:
  /// **'End'**
  String get searchDateEnd;

  /// No description provided for @analyticsMonth.
  ///
  /// In en, this message translates to:
  /// **'Month'**
  String get analyticsMonth;

  /// No description provided for @analyticsYear.
  ///
  /// In en, this message translates to:
  /// **'Year'**
  String get analyticsYear;

  /// No description provided for @analyticsAll.
  ///
  /// In en, this message translates to:
  /// **'All'**
  String get analyticsAll;

  /// No description provided for @analyticsCategoryRanking.
  ///
  /// In en, this message translates to:
  /// **'Category Ranking'**
  String get analyticsCategoryRanking;

  /// No description provided for @analyticsTotalAmount.
  ///
  /// In en, this message translates to:
  /// **'Total'**
  String get analyticsTotalAmount;

  /// No description provided for @analyticsNoDataSubtext.
  ///
  /// In en, this message translates to:
  /// **'Swipe left/right to switch periods, or tap button to toggle income/expense'**
  String get analyticsNoDataSubtext;

  /// No description provided for @analyticsSwipeHint.
  ///
  /// In en, this message translates to:
  /// **'Swipe left/right to change period'**
  String get analyticsSwipeHint;

  /// No description provided for @analyticsSwitchTo.
  ///
  /// In en, this message translates to:
  /// **'Switch to {type}'**
  String analyticsSwitchTo(String type);

  /// No description provided for @analyticsTipHeader.
  ///
  /// In en, this message translates to:
  /// **'Tip: Top capsule can switch Month/Year/All'**
  String get analyticsTipHeader;

  /// No description provided for @analyticsSwipeToSwitch.
  ///
  /// In en, this message translates to:
  /// **'Swipe to switch'**
  String get analyticsSwipeToSwitch;

  /// No description provided for @analyticsAllYears.
  ///
  /// In en, this message translates to:
  /// **'All Years'**
  String get analyticsAllYears;

  /// No description provided for @analyticsToday.
  ///
  /// In en, this message translates to:
  /// **'Today'**
  String get analyticsToday;

  /// No description provided for @splashAppName.
  ///
  /// In en, this message translates to:
  /// **'Bee Accounting'**
  String get splashAppName;

  /// No description provided for @splashSlogan.
  ///
  /// In en, this message translates to:
  /// **'Record Every Drop'**
  String get splashSlogan;

  /// No description provided for @splashSecurityTitle.
  ///
  /// In en, this message translates to:
  /// **'Open Source Data Security'**
  String get splashSecurityTitle;

  /// No description provided for @splashSecurityFeature1.
  ///
  /// In en, this message translates to:
  /// **'• Local data storage, complete privacy control'**
  String get splashSecurityFeature1;

  /// No description provided for @splashSecurityFeature2.
  ///
  /// In en, this message translates to:
  /// **'• Open source code transparency, trustworthy security'**
  String get splashSecurityFeature2;

  /// No description provided for @splashSecurityFeature3.
  ///
  /// In en, this message translates to:
  /// **'• Optional cloud sync, consistent data across devices'**
  String get splashSecurityFeature3;

  /// No description provided for @splashInitializing.
  ///
  /// In en, this message translates to:
  /// **'Initializing data...'**
  String get splashInitializing;

  /// No description provided for @ledgersTitle.
  ///
  /// In en, this message translates to:
  /// **'Ledger Management'**
  String get ledgersTitle;

  /// No description provided for @ledgersNew.
  ///
  /// In en, this message translates to:
  /// **'New Ledger'**
  String get ledgersNew;

  /// No description provided for @ledgersClear.
  ///
  /// In en, this message translates to:
  /// **'Clear Ledger'**
  String get ledgersClear;

  /// No description provided for @ledgersClearMessage.
  ///
  /// In en, this message translates to:
  /// **'Are you sure to clear all transactions in ledger \"{name}\"? This action cannot be undone.\\nThe ledger will be kept, only transaction data will be deleted.'**
  String ledgersClearMessage(Object name);

  /// No description provided for @ledgerDefaultName.
  ///
  /// In en, this message translates to:
  /// **'Default Ledger'**
  String get ledgerDefaultName;

  /// No description provided for @ledgersEdit.
  ///
  /// In en, this message translates to:
  /// **'Edit Ledger'**
  String get ledgersEdit;

  /// No description provided for @ledgersDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete Ledger'**
  String get ledgersDelete;

  /// No description provided for @ledgersDeleteConfirm.
  ///
  /// In en, this message translates to:
  /// **'Delete Ledger'**
  String get ledgersDeleteConfirm;

  /// No description provided for @ledgersDeleteMessage.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this ledger and all its records? This action cannot be undone.\\nIf there is a backup in the cloud, it will also be deleted.'**
  String get ledgersDeleteMessage;

  /// No description provided for @ledgersDeleted.
  ///
  /// In en, this message translates to:
  /// **'Deleted'**
  String get ledgersDeleted;

  /// No description provided for @ledgersDeleteFailed.
  ///
  /// In en, this message translates to:
  /// **'Delete Failed'**
  String get ledgersDeleteFailed;

  /// No description provided for @ledgersClearTitle.
  ///
  /// In en, this message translates to:
  /// **'Clear Ledger'**
  String get ledgersClearTitle;

  /// No description provided for @ledgersClearSuccess.
  ///
  /// In en, this message translates to:
  /// **'Ledger cleared'**
  String get ledgersClearSuccess;

  /// No description provided for @ledgersDeleteLocal.
  ///
  /// In en, this message translates to:
  /// **'Delete Local Ledger Only'**
  String get ledgersDeleteLocal;

  /// No description provided for @ledgersDeleteLocalTitle.
  ///
  /// In en, this message translates to:
  /// **'Delete Local Ledger'**
  String get ledgersDeleteLocalTitle;

  /// No description provided for @ledgersDeleteLocalMessage.
  ///
  /// In en, this message translates to:
  /// **'Are you sure to delete local ledger \"{name}\"?\\nCloud backup will be kept and you can restore it anytime.'**
  String ledgersDeleteLocalMessage(Object name);

  /// No description provided for @ledgersDeleteLocalSuccess.
  ///
  /// In en, this message translates to:
  /// **'Local ledger deleted'**
  String get ledgersDeleteLocalSuccess;

  /// No description provided for @ledgersName.
  ///
  /// In en, this message translates to:
  /// **'Name'**
  String get ledgersName;

  /// No description provided for @ledgersDefaultLedgerName.
  ///
  /// In en, this message translates to:
  /// **'Default Ledger'**
  String get ledgersDefaultLedgerName;

  /// No description provided for @ledgersCurrency.
  ///
  /// In en, this message translates to:
  /// **'Currency'**
  String get ledgersCurrency;

  /// No description provided for @ledgersMonthStartDay.
  ///
  /// In en, this message translates to:
  /// **'Month start day'**
  String get ledgersMonthStartDay;

  /// No description provided for @ledgersMonthStartDayHint.
  ///
  /// In en, this message translates to:
  /// **'Statistics and budgets use this day (1-28) as the start of each monthly period'**
  String get ledgersMonthStartDayHint;

  /// No description provided for @ledgersMonthStartDayNatural.
  ///
  /// In en, this message translates to:
  /// **'1st (calendar month)'**
  String get ledgersMonthStartDayNatural;

  /// No description provided for @ledgersMonthStartDayValue.
  ///
  /// In en, this message translates to:
  /// **'Day {day} of each month'**
  String ledgersMonthStartDayValue(int day);

  /// No description provided for @ledgersSelectCurrency.
  ///
  /// In en, this message translates to:
  /// **'Select Currency'**
  String get ledgersSelectCurrency;

  /// No description provided for @ledgersSearchCurrency.
  ///
  /// In en, this message translates to:
  /// **'Search: Chinese or code'**
  String get ledgersSearchCurrency;

  /// No description provided for @ledgersCreate.
  ///
  /// In en, this message translates to:
  /// **'Create'**
  String get ledgersCreate;

  /// No description provided for @ledgersActions.
  ///
  /// In en, this message translates to:
  /// **'Actions'**
  String get ledgersActions;

  /// No description provided for @ledgersRecords.
  ///
  /// In en, this message translates to:
  /// **'Records: {count}'**
  String ledgersRecords(String count);

  /// No description provided for @ledgersBalance.
  ///
  /// In en, this message translates to:
  /// **'Balance: {balance}'**
  String ledgersBalance(String balance);

  /// No description provided for @ledgerCardDownloadCloud.
  ///
  /// In en, this message translates to:
  /// **'Download from Cloud'**
  String get ledgerCardDownloadCloud;

  /// No description provided for @ledgersLocal.
  ///
  /// In en, this message translates to:
  /// **'Local Ledgers'**
  String get ledgersLocal;

  /// No description provided for @ledgersRemote.
  ///
  /// In en, this message translates to:
  /// **'Cloud Ledgers'**
  String get ledgersRemote;

  /// No description provided for @ledgersEmpty.
  ///
  /// In en, this message translates to:
  /// **'No ledgers'**
  String get ledgersEmpty;

  /// No description provided for @ledgersRestoreAll.
  ///
  /// In en, this message translates to:
  /// **'Restore All'**
  String get ledgersRestoreAll;

  /// No description provided for @ledgersSwitched.
  ///
  /// In en, this message translates to:
  /// **'Switched to ledger \"{name}\"'**
  String ledgersSwitched(String name);

  /// No description provided for @ledgersDownloadTitle.
  ///
  /// In en, this message translates to:
  /// **'Download Ledger'**
  String get ledgersDownloadTitle;

  /// No description provided for @ledgersDownloadMessage.
  ///
  /// In en, this message translates to:
  /// **'Confirm download ledger \"{name}\" to local?'**
  String ledgersDownloadMessage(String name);

  /// No description provided for @ledgersDownloading.
  ///
  /// In en, this message translates to:
  /// **'Downloading...'**
  String get ledgersDownloading;

  /// No description provided for @ledgersDownloadSuccess.
  ///
  /// In en, this message translates to:
  /// **'Ledger \"{name}\" downloaded successfully'**
  String ledgersDownloadSuccess(String name);

  /// No description provided for @ledgersDownload.
  ///
  /// In en, this message translates to:
  /// **'Download'**
  String get ledgersDownload;

  /// No description provided for @ledgersDeleteRemote.
  ///
  /// In en, this message translates to:
  /// **'Delete Cloud Ledger'**
  String get ledgersDeleteRemote;

  /// No description provided for @ledgersDeleteRemoteConfirm.
  ///
  /// In en, this message translates to:
  /// **'Delete Cloud Ledger'**
  String get ledgersDeleteRemoteConfirm;

  /// No description provided for @ledgersDeleteRemoteMessage.
  ///
  /// In en, this message translates to:
  /// **'Confirm delete cloud ledger \"{name}\"? This action cannot be undone.'**
  String ledgersDeleteRemoteMessage(String name);

  /// No description provided for @ledgersDeleting.
  ///
  /// In en, this message translates to:
  /// **'Deleting...'**
  String get ledgersDeleting;

  /// No description provided for @ledgersDeleteRemoteSuccess.
  ///
  /// In en, this message translates to:
  /// **'Cloud ledger deleted'**
  String get ledgersDeleteRemoteSuccess;

  /// No description provided for @ledgersCannotDeleteLastOne.
  ///
  /// In en, this message translates to:
  /// **'Cannot delete the last ledger'**
  String get ledgersCannotDeleteLastOne;

  /// No description provided for @ledgersRestoreAllTitle.
  ///
  /// In en, this message translates to:
  /// **'Batch Restore'**
  String get ledgersRestoreAllTitle;

  /// No description provided for @ledgersRestoreAllMessage.
  ///
  /// In en, this message translates to:
  /// **'Confirm restore all cloud ledgers? Total {count}.'**
  String ledgersRestoreAllMessage(int count);

  /// No description provided for @ledgersRestoring.
  ///
  /// In en, this message translates to:
  /// **'Restoring...'**
  String get ledgersRestoring;

  /// No description provided for @ledgersRestoreComplete.
  ///
  /// In en, this message translates to:
  /// **'Restore Complete'**
  String get ledgersRestoreComplete;

  /// No description provided for @ledgersRestoreResult.
  ///
  /// In en, this message translates to:
  /// **'Success: {success}, Failed: {failed}'**
  String ledgersRestoreResult(int success, int failed);

  /// No description provided for @categoryTitle.
  ///
  /// In en, this message translates to:
  /// **'Category Management'**
  String get categoryTitle;

  /// No description provided for @categoryIconGroupBasic.
  ///
  /// In en, this message translates to:
  /// **'Basics'**
  String get categoryIconGroupBasic;

  /// No description provided for @categoryIconGroupFoodDining.
  ///
  /// In en, this message translates to:
  /// **'Food & Dining'**
  String get categoryIconGroupFoodDining;

  /// No description provided for @categoryIconGroupTransportation.
  ///
  /// In en, this message translates to:
  /// **'Transportation'**
  String get categoryIconGroupTransportation;

  /// No description provided for @categoryIconGroupShopping.
  ///
  /// In en, this message translates to:
  /// **'Shopping'**
  String get categoryIconGroupShopping;

  /// No description provided for @categoryIconGroupHomeLiving.
  ///
  /// In en, this message translates to:
  /// **'Home & Living'**
  String get categoryIconGroupHomeLiving;

  /// No description provided for @categoryIconGroupCommunicationDevices.
  ///
  /// In en, this message translates to:
  /// **'Communication & Devices'**
  String get categoryIconGroupCommunicationDevices;

  /// No description provided for @categoryIconGroupEntertainmentLeisure.
  ///
  /// In en, this message translates to:
  /// **'Entertainment & Leisure'**
  String get categoryIconGroupEntertainmentLeisure;

  /// No description provided for @categoryIconGroupHealthMedical.
  ///
  /// In en, this message translates to:
  /// **'Health & Medicine'**
  String get categoryIconGroupHealthMedical;

  /// No description provided for @categoryIconGroupEducationLearning.
  ///
  /// In en, this message translates to:
  /// **'Education & Learning'**
  String get categoryIconGroupEducationLearning;

  /// No description provided for @categoryIconGroupPetsAnimals.
  ///
  /// In en, this message translates to:
  /// **'Pets & Animals'**
  String get categoryIconGroupPetsAnimals;

  /// No description provided for @categoryIconGroupClothingBeauty.
  ///
  /// In en, this message translates to:
  /// **'Clothing & Beauty'**
  String get categoryIconGroupClothingBeauty;

  /// No description provided for @categoryIconGroupOtherMiscellaneous.
  ///
  /// In en, this message translates to:
  /// **'Other'**
  String get categoryIconGroupOtherMiscellaneous;

  /// No description provided for @categoryIconGroupWorkCareer.
  ///
  /// In en, this message translates to:
  /// **'Work & Career'**
  String get categoryIconGroupWorkCareer;

  /// No description provided for @categoryIconGroupFinance.
  ///
  /// In en, this message translates to:
  /// **'Finance'**
  String get categoryIconGroupFinance;

  /// No description provided for @categoryIconGroupRewardsGifts.
  ///
  /// In en, this message translates to:
  /// **'Rewards & Gifts'**
  String get categoryIconGroupRewardsGifts;

  /// No description provided for @categoryIconGroupInvestmentReturns.
  ///
  /// In en, this message translates to:
  /// **'Investment Returns'**
  String get categoryIconGroupInvestmentReturns;

  /// No description provided for @categoryIconGroupOtherIncome.
  ///
  /// In en, this message translates to:
  /// **'Other Income'**
  String get categoryIconGroupOtherIncome;

  /// No description provided for @categoryGenerateDefault.
  ///
  /// In en, this message translates to:
  /// **'Generate Default Categories'**
  String get categoryGenerateDefault;

  /// No description provided for @categoryGenerateDefaultFlat.
  ///
  /// In en, this message translates to:
  /// **'Level 1 categories'**
  String get categoryGenerateDefaultFlat;

  /// No description provided for @categoryGenerateDefaultHierarchical.
  ///
  /// In en, this message translates to:
  /// **'Level 2 categories'**
  String get categoryGenerateDefaultHierarchical;

  /// No description provided for @categoryGenerateDefaultHint.
  ///
  /// In en, this message translates to:
  /// **'Existing categories are kept. Default categories with matching names are skipped.'**
  String get categoryGenerateDefaultHint;

  /// No description provided for @categoryGenerateDefaultSuccess.
  ///
  /// In en, this message translates to:
  /// **'Generated {count} default categories'**
  String categoryGenerateDefaultSuccess(int count);

  /// No description provided for @categoryGenerateDefaultNoChanges.
  ///
  /// In en, this message translates to:
  /// **'Default categories already exist'**
  String get categoryGenerateDefaultNoChanges;

  /// No description provided for @categoryGenerateDefaultFailed.
  ///
  /// In en, this message translates to:
  /// **'Could not generate default categories. Try again.'**
  String get categoryGenerateDefaultFailed;

  /// No description provided for @categoryNew.
  ///
  /// In en, this message translates to:
  /// **'New Category'**
  String get categoryNew;

  /// No description provided for @categoryExpense.
  ///
  /// In en, this message translates to:
  /// **'Expense'**
  String get categoryExpense;

  /// No description provided for @categoryIncome.
  ///
  /// In en, this message translates to:
  /// **'Income'**
  String get categoryIncome;

  /// No description provided for @categoryEmpty.
  ///
  /// In en, this message translates to:
  /// **'No categories'**
  String get categoryEmpty;

  /// No description provided for @categoryDefault.
  ///
  /// In en, this message translates to:
  /// **'Default Category'**
  String get categoryDefault;

  /// No description provided for @categoryReorderTip.
  ///
  /// In en, this message translates to:
  /// **'Long press to drag and reorder categories'**
  String get categoryReorderTip;

  /// No description provided for @categoryLoadFailed.
  ///
  /// In en, this message translates to:
  /// **'Load failed: {error}'**
  String categoryLoadFailed(String error);

  /// No description provided for @iconPickerTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Icon'**
  String get iconPickerTitle;

  /// No description provided for @iconCategoryTransport.
  ///
  /// In en, this message translates to:
  /// **'Transport'**
  String get iconCategoryTransport;

  /// No description provided for @iconCategoryShopping.
  ///
  /// In en, this message translates to:
  /// **'Shopping'**
  String get iconCategoryShopping;

  /// No description provided for @iconCategoryEntertainment.
  ///
  /// In en, this message translates to:
  /// **'Entertainment'**
  String get iconCategoryEntertainment;

  /// No description provided for @iconCategoryLife.
  ///
  /// In en, this message translates to:
  /// **'Life'**
  String get iconCategoryLife;

  /// No description provided for @iconCategoryHealth.
  ///
  /// In en, this message translates to:
  /// **'Health'**
  String get iconCategoryHealth;

  /// No description provided for @iconCategoryEducation.
  ///
  /// In en, this message translates to:
  /// **'Education'**
  String get iconCategoryEducation;

  /// No description provided for @iconCategoryWork.
  ///
  /// In en, this message translates to:
  /// **'Work'**
  String get iconCategoryWork;

  /// No description provided for @iconCategoryFinance.
  ///
  /// In en, this message translates to:
  /// **'Finance'**
  String get iconCategoryFinance;

  /// No description provided for @iconCategoryReward.
  ///
  /// In en, this message translates to:
  /// **'Reward'**
  String get iconCategoryReward;

  /// No description provided for @iconCategoryOther.
  ///
  /// In en, this message translates to:
  /// **'Other'**
  String get iconCategoryOther;

  /// No description provided for @iconCategoryDining.
  ///
  /// In en, this message translates to:
  /// **'Dining'**
  String get iconCategoryDining;

  /// No description provided for @importTitle.
  ///
  /// In en, this message translates to:
  /// **'Import Bills'**
  String get importTitle;

  /// No description provided for @importBillType.
  ///
  /// In en, this message translates to:
  /// **'Bill Type'**
  String get importBillType;

  /// No description provided for @importBillTypeGeneric.
  ///
  /// In en, this message translates to:
  /// **'Generic CSV'**
  String get importBillTypeGeneric;

  /// No description provided for @importBillTypeAlipay.
  ///
  /// In en, this message translates to:
  /// **'Alipay'**
  String get importBillTypeAlipay;

  /// No description provided for @importBillTypeWechat.
  ///
  /// In en, this message translates to:
  /// **'WeChat'**
  String get importBillTypeWechat;

  /// No description provided for @importChooseFile.
  ///
  /// In en, this message translates to:
  /// **'Choose File'**
  String get importChooseFile;

  /// No description provided for @importNoFileSelected.
  ///
  /// In en, this message translates to:
  /// **'No file selected'**
  String get importNoFileSelected;

  /// No description provided for @importHint.
  ///
  /// In en, this message translates to:
  /// **'Tip: Please select a file to start importing (CSV/TSV/XLSX)'**
  String get importHint;

  /// No description provided for @importReading.
  ///
  /// In en, this message translates to:
  /// **'Reading file…'**
  String get importReading;

  /// No description provided for @importPreparing.
  ///
  /// In en, this message translates to:
  /// **'Preparing…'**
  String get importPreparing;

  /// No description provided for @importColumnNumber.
  ///
  /// In en, this message translates to:
  /// **'Column {number}'**
  String importColumnNumber(Object number);

  /// No description provided for @importConfirmMapping.
  ///
  /// In en, this message translates to:
  /// **'Confirm Mapping'**
  String get importConfirmMapping;

  /// No description provided for @importCategoryMapping.
  ///
  /// In en, this message translates to:
  /// **'Category Mapping'**
  String get importCategoryMapping;

  /// No description provided for @importNoDataParsed.
  ///
  /// In en, this message translates to:
  /// **'No data parsed. Please return to previous page to check CSV content or separator.'**
  String get importNoDataParsed;

  /// No description provided for @importFieldDate.
  ///
  /// In en, this message translates to:
  /// **'Date'**
  String get importFieldDate;

  /// No description provided for @importFieldType.
  ///
  /// In en, this message translates to:
  /// **'Type'**
  String get importFieldType;

  /// No description provided for @importFieldAmount.
  ///
  /// In en, this message translates to:
  /// **'Amount'**
  String get importFieldAmount;

  /// No description provided for @importFieldCategory.
  ///
  /// In en, this message translates to:
  /// **'Category'**
  String get importFieldCategory;

  /// No description provided for @importFieldAccount.
  ///
  /// In en, this message translates to:
  /// **'Account'**
  String get importFieldAccount;

  /// No description provided for @importFieldNote.
  ///
  /// In en, this message translates to:
  /// **'Note'**
  String get importFieldNote;

  /// No description provided for @importPreview.
  ///
  /// In en, this message translates to:
  /// **'Data Preview'**
  String get importPreview;

  /// No description provided for @importPreviewLimit.
  ///
  /// In en, this message translates to:
  /// **'Showing first {shown} of {total} records'**
  String importPreviewLimit(Object shown, Object total);

  /// No description provided for @importCategoryNotSelected.
  ///
  /// In en, this message translates to:
  /// **'Category not selected'**
  String get importCategoryNotSelected;

  /// No description provided for @importCategoryMappingDescription.
  ///
  /// In en, this message translates to:
  /// **'Please select corresponding local categories for each category name:'**
  String get importCategoryMappingDescription;

  /// No description provided for @importKeepOriginalName.
  ///
  /// In en, this message translates to:
  /// **'Keep original name'**
  String get importKeepOriginalName;

  /// No description provided for @importProgress.
  ///
  /// In en, this message translates to:
  /// **'Importing, success: {ok}, failed: {fail}'**
  String importProgress(Object fail, Object ok);

  /// No description provided for @importCancelImport.
  ///
  /// In en, this message translates to:
  /// **'Cancel Import'**
  String get importCancelImport;

  /// No description provided for @importCompleteTitle.
  ///
  /// In en, this message translates to:
  /// **'Import Complete'**
  String get importCompleteTitle;

  /// No description provided for @importSelectCategoryFirst.
  ///
  /// In en, this message translates to:
  /// **'Please select category mapping first'**
  String get importSelectCategoryFirst;

  /// No description provided for @importNextStep.
  ///
  /// In en, this message translates to:
  /// **'Next Step'**
  String get importNextStep;

  /// No description provided for @importPreviousStep.
  ///
  /// In en, this message translates to:
  /// **'Previous Step'**
  String get importPreviousStep;

  /// No description provided for @importStartImport.
  ///
  /// In en, this message translates to:
  /// **'Start Import'**
  String get importStartImport;

  /// No description provided for @importAutoDetect.
  ///
  /// In en, this message translates to:
  /// **'Auto Detect'**
  String get importAutoDetect;

  /// No description provided for @importInProgress.
  ///
  /// In en, this message translates to:
  /// **'Import in Progress'**
  String get importInProgress;

  /// No description provided for @importProgressDetail.
  ///
  /// In en, this message translates to:
  /// **'Imported {done} / {total} records, success {ok}, failed {fail}'**
  String importProgressDetail(Object done, Object fail, Object ok, Object total);

  /// No description provided for @importBackgroundImport.
  ///
  /// In en, this message translates to:
  /// **'Background Import'**
  String get importBackgroundImport;

  /// No description provided for @importCancelled.
  ///
  /// In en, this message translates to:
  /// **'Import Cancelled'**
  String get importCancelled;

  /// No description provided for @importCompleted.
  ///
  /// In en, this message translates to:
  /// **'Import Completed{cancelled}, success {ok}, failed {fail}'**
  String importCompleted(Object cancelled, Object fail, Object ok);

  /// No description provided for @importSkippedNonTransactionTypes.
  ///
  /// In en, this message translates to:
  /// **'Skipped {count} non-transaction records (debts, etc.)'**
  String importSkippedNonTransactionTypes(Object count);

  /// No description provided for @importTransactionFailed.
  ///
  /// In en, this message translates to:
  /// **'Import failed, all changes have been rolled back: {error}'**
  String importTransactionFailed(Object error);

  /// No description provided for @importFileOpenError.
  ///
  /// In en, this message translates to:
  /// **'Unable to open file picker: {error}'**
  String importFileOpenError(String error);

  /// No description provided for @mineTitle.
  ///
  /// In en, this message translates to:
  /// **'Mine'**
  String get mineTitle;

  /// No description provided for @mineReminder.
  ///
  /// In en, this message translates to:
  /// **'Reminder Settings'**
  String get mineReminder;

  /// No description provided for @mineImport.
  ///
  /// In en, this message translates to:
  /// **'Import Data'**
  String get mineImport;

  /// No description provided for @mineExport.
  ///
  /// In en, this message translates to:
  /// **'Export Data'**
  String get mineExport;



  /// No description provided for @mineLanguageSettings.
  ///
  /// In en, this message translates to:
  /// **'Language'**
  String get mineLanguageSettings;

  /// No description provided for @languageTitle.
  ///
  /// In en, this message translates to:
  /// **'Language Settings'**
  String get languageTitle;

  /// No description provided for @languageChinese.
  ///
  /// In en, this message translates to:
  /// **'中文'**
  String get languageChinese;

  /// No description provided for @languageEnglish.
  ///
  /// In en, this message translates to:
  /// **'English'**
  String get languageEnglish;

  /// No description provided for @languageSystemDefault.
  ///
  /// In en, this message translates to:
  /// **'Follow System'**
  String get languageSystemDefault;

  /// No description provided for @deleteConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Delete Confirmation'**
  String get deleteConfirmTitle;

  /// No description provided for @deleteConfirmMessage.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this record?'**
  String get deleteConfirmMessage;

  /// No description provided for @mineSlogan.
  ///
  /// In en, this message translates to:
  /// **'Bee Accounting, Every Penny Counts'**
  String get mineSlogan;

  /// No description provided for @mineDisplayNameEditTitle.
  ///
  /// In en, this message translates to:
  /// **'Set nickname'**
  String get mineDisplayNameEditTitle;

  /// No description provided for @mineDisplayNameHint.
  ///
  /// In en, this message translates to:
  /// **'Enter a nickname'**
  String get mineDisplayNameHint;

  /// No description provided for @mineDisplayNameSaved.
  ///
  /// In en, this message translates to:
  /// **'Nickname updated'**
  String get mineDisplayNameSaved;

  /// No description provided for @mineGreetingMorning.
  ///
  /// In en, this message translates to:
  /// **'Good morning'**
  String get mineGreetingMorning;

  /// No description provided for @mineGreetingNoon.
  ///
  /// In en, this message translates to:
  /// **'Good noon'**
  String get mineGreetingNoon;

  /// No description provided for @mineGreetingAfternoon.
  ///
  /// In en, this message translates to:
  /// **'Good afternoon'**
  String get mineGreetingAfternoon;

  /// No description provided for @mineGreetingEvening.
  ///
  /// In en, this message translates to:
  /// **'Good evening'**
  String get mineGreetingEvening;

  /// No description provided for @mineGreetingNight.
  ///
  /// In en, this message translates to:
  /// **'Good night'**
  String get mineGreetingNight;

  /// No description provided for @mineGreetingNamed.
  ///
  /// In en, this message translates to:
  /// **'{greeting}, {name}'**
  String mineGreetingNamed(String greeting, String name);

  /// No description provided for @mineProfileEditTitle.
  ///
  /// In en, this message translates to:
  /// **'Edit profile'**
  String get mineProfileEditTitle;







































  /// No description provided for @mineAvatarTitle.
  ///
  /// In en, this message translates to:
  /// **'Avatar Settings'**
  String get mineAvatarTitle;

  /// No description provided for @mineAvatarFromGallery.
  ///
  /// In en, this message translates to:
  /// **'Choose from Gallery'**
  String get mineAvatarFromGallery;

  /// No description provided for @mineAvatarFromCamera.
  ///
  /// In en, this message translates to:
  /// **'Take Photo'**
  String get mineAvatarFromCamera;

  /// No description provided for @mineAvatarDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete Avatar'**
  String get mineAvatarDelete;

  /// No description provided for @annualReportTitle.
  ///
  /// In en, this message translates to:
  /// **'Annual Report'**
  String get annualReportTitle;

  /// No description provided for @annualReportSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Review your {year} financial journey'**
  String annualReportSubtitle(int year);

  /// No description provided for @annualReportEntrySubtitle.
  ///
  /// In en, this message translates to:
  /// **'Create your personalized annual report to share'**
  String get annualReportEntrySubtitle;

  /// No description provided for @annualReportNoData.
  ///
  /// In en, this message translates to:
  /// **'No data for {year}'**
  String annualReportNoData(int year);

  /// No description provided for @annualReportPage1Title.
  ///
  /// In en, this message translates to:
  /// **'Annual Overview'**
  String get annualReportPage1Title;

  /// No description provided for @annualReportPage1Subtitle.
  ///
  /// In en, this message translates to:
  /// **'Your {year} bookkeeping journey'**
  String annualReportPage1Subtitle(int year);

  /// No description provided for @annualReportTotalDays.
  ///
  /// In en, this message translates to:
  /// **'Days Tracked'**
  String get annualReportTotalDays;

  /// No description provided for @annualReportTotalRecords.
  ///
  /// In en, this message translates to:
  /// **'Total Records'**
  String get annualReportTotalRecords;

  /// No description provided for @annualReportTotalIncome.
  ///
  /// In en, this message translates to:
  /// **'Total Income'**
  String get annualReportTotalIncome;

  /// No description provided for @annualReportTotalExpense.
  ///
  /// In en, this message translates to:
  /// **'Total Expense'**
  String get annualReportTotalExpense;

  /// No description provided for @annualReportNetSavings.
  ///
  /// In en, this message translates to:
  /// **'Net Savings'**
  String get annualReportNetSavings;

  /// No description provided for @annualReportPage2Title.
  ///
  /// In en, this message translates to:
  /// **'Expense Analysis'**
  String get annualReportPage2Title;

  /// No description provided for @annualReportPage2Subtitle.
  ///
  /// In en, this message translates to:
  /// **'Where your money went'**
  String get annualReportPage2Subtitle;

  /// No description provided for @annualReportPage3Title.
  ///
  /// In en, this message translates to:
  /// **'Monthly Trends'**
  String get annualReportPage3Title;

  /// No description provided for @annualReportPage3Subtitle.
  ///
  /// In en, this message translates to:
  /// **'12 months of income & expense'**
  String get annualReportPage3Subtitle;

  /// No description provided for @annualReportHighestMonth.
  ///
  /// In en, this message translates to:
  /// **'Highest Spending Month'**
  String get annualReportHighestMonth;

  /// No description provided for @annualReportLowestMonth.
  ///
  /// In en, this message translates to:
  /// **'Lowest Spending Month'**
  String get annualReportLowestMonth;

  /// No description provided for @annualReportPage4Title.
  ///
  /// In en, this message translates to:
  /// **'Special Moments'**
  String get annualReportPage4Title;

  /// No description provided for @annualReportPage4Subtitle.
  ///
  /// In en, this message translates to:
  /// **'Memorable transactions'**
  String get annualReportPage4Subtitle;

  /// No description provided for @annualReportLargestExpense.
  ///
  /// In en, this message translates to:
  /// **'Largest Expense'**
  String get annualReportLargestExpense;

  /// No description provided for @annualReportLargestIncome.
  ///
  /// In en, this message translates to:
  /// **'Largest Income'**
  String get annualReportLargestIncome;

  /// No description provided for @annualReportFirstRecord.
  ///
  /// In en, this message translates to:
  /// **'First Record'**
  String get annualReportFirstRecord;

  /// No description provided for @annualReportPage5Title.
  ///
  /// In en, this message translates to:
  /// **'Achievements'**
  String get annualReportPage5Title;

  /// No description provided for @annualReportPage5Subtitle.
  ///
  /// In en, this message translates to:
  /// **'Your bookkeeping badges'**
  String get annualReportPage5Subtitle;

  /// No description provided for @annualReportAchievementConsistent.
  ///
  /// In en, this message translates to:
  /// **'Consistent'**
  String get annualReportAchievementConsistent;

  /// No description provided for @annualReportAchievementConsistentDesc.
  ///
  /// In en, this message translates to:
  /// **'Tracked for over {days} days straight'**
  String annualReportAchievementConsistentDesc(int days);

  /// No description provided for @annualReportAchievementSaver.
  ///
  /// In en, this message translates to:
  /// **'Saver'**
  String get annualReportAchievementSaver;

  /// No description provided for @annualReportAchievementSaverDesc.
  ///
  /// In en, this message translates to:
  /// **'Positive net savings this year'**
  String get annualReportAchievementSaverDesc;

  /// No description provided for @annualReportAchievementDetail.
  ///
  /// In en, this message translates to:
  /// **'Detail-Oriented'**
  String get annualReportAchievementDetail;

  /// No description provided for @annualReportAchievementDetailDesc.
  ///
  /// In en, this message translates to:
  /// **'Over {count} records tracked'**
  String annualReportAchievementDetailDesc(int count);

  /// No description provided for @annualReportShareButton.
  ///
  /// In en, this message translates to:
  /// **'Generate Share Poster'**
  String get annualReportShareButton;

  /// No description provided for @annualReportGenerating.
  ///
  /// In en, this message translates to:
  /// **'Generating annual report...'**
  String get annualReportGenerating;

  /// No description provided for @annualReportSaveSuccess.
  ///
  /// In en, this message translates to:
  /// **'Annual report poster saved'**
  String get annualReportSaveSuccess;

  /// No description provided for @mineShareApp.
  ///
  /// In en, this message translates to:
  /// **'Share App'**
  String get mineShareApp;

  /// No description provided for @mineShareWithFriends.
  ///
  /// In en, this message translates to:
  /// **'Share BeeCount with friends'**
  String get mineShareWithFriends;

  /// No description provided for @mineCopyPromoText.
  ///
  /// In en, this message translates to:
  /// **'Copy Promo Text'**
  String get mineCopyPromoText;

  /// No description provided for @mineCopyPromoSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Copy and share with friends'**
  String get mineCopyPromoSubtitle;

  /// No description provided for @mineShareGenerating.
  ///
  /// In en, this message translates to:
  /// **'Generating share poster...'**
  String get mineShareGenerating;

  /// No description provided for @sharePosterAppName.
  ///
  /// In en, this message translates to:
  /// **'BeeCount'**
  String get sharePosterAppName;

  /// No description provided for @sharePosterSlogan.
  ///
  /// In en, this message translates to:
  /// **'Smart Accounting, Beautiful Life'**
  String get sharePosterSlogan;

  /// No description provided for @sharePosterFeature1.
  ///
  /// In en, this message translates to:
  /// **'Data Security·You Control'**
  String get sharePosterFeature1;

  /// No description provided for @sharePosterFeature2.
  ///
  /// In en, this message translates to:
  /// **'Open Source·Auditable'**
  String get sharePosterFeature2;

  /// No description provided for @sharePosterFeature3.
  ///
  /// In en, this message translates to:
  /// **'AI Smart·Photo & Voice'**
  String get sharePosterFeature3;

  /// No description provided for @sharePosterFeature4.
  ///
  /// In en, this message translates to:
  /// **'Photo Accounting·Auto Recognition'**
  String get sharePosterFeature4;

  /// No description provided for @sharePosterFeature5.
  ///
  /// In en, this message translates to:
  /// **'Multi Ledger·Dark Mode'**
  String get sharePosterFeature5;

  /// No description provided for @sharePosterFeature6.
  ///
  /// In en, this message translates to:
  /// **'Self-Hosted Cloud·Free Forever'**
  String get sharePosterFeature6;

  /// No description provided for @sharePosterScanText.
  ///
  /// In en, this message translates to:
  /// **'Scan to visit open source project'**
  String get sharePosterScanText;

  /// No description provided for @appPromoTagOpenSource.
  ///
  /// In en, this message translates to:
  /// **'Open Source'**
  String get appPromoTagOpenSource;

  /// No description provided for @appPromoTagFree.
  ///
  /// In en, this message translates to:
  /// **'Free'**
  String get appPromoTagFree;

  /// No description provided for @appPromoFooterText.
  ///
  /// In en, this message translates to:
  /// **'Track every penny, trace every moment'**
  String get appPromoFooterText;

  /// No description provided for @userProfileJourneyYears.
  ///
  /// In en, this message translates to:
  /// **'Bookkeeping Pro {years} Years'**
  String userProfileJourneyYears(int years);

  /// No description provided for @userProfileJourneyOneYear.
  ///
  /// In en, this message translates to:
  /// **'One Year of Bookkeeping'**
  String get userProfileJourneyOneYear;

  /// No description provided for @userProfileJourneyHalfYear.
  ///
  /// In en, this message translates to:
  /// **'Six Months Strong'**
  String get userProfileJourneyHalfYear;

  /// No description provided for @userProfileJourneyThreeMonths.
  ///
  /// In en, this message translates to:
  /// **'Three Months In'**
  String get userProfileJourneyThreeMonths;

  /// No description provided for @userProfileJourneyOneMonth.
  ///
  /// In en, this message translates to:
  /// **'One Month Milestone'**
  String get userProfileJourneyOneMonth;

  /// No description provided for @userProfileJourneyOneWeek.
  ///
  /// In en, this message translates to:
  /// **'First Week Done'**
  String get userProfileJourneyOneWeek;

  /// No description provided for @userProfileJourneyStart.
  ///
  /// In en, this message translates to:
  /// **'Starting the Journey'**
  String get userProfileJourneyStart;

  /// No description provided for @userProfileDailyAverage.
  ///
  /// In en, this message translates to:
  /// **'Daily Average'**
  String get userProfileDailyAverage;

  /// No description provided for @sharePosterSave.
  ///
  /// In en, this message translates to:
  /// **'Save to Gallery'**
  String get sharePosterSave;

  /// No description provided for @sharePosterShare.
  ///
  /// In en, this message translates to:
  /// **'Share'**
  String get sharePosterShare;

  /// No description provided for @sharePosterHideIncome.
  ///
  /// In en, this message translates to:
  /// **'Hide Income'**
  String get sharePosterHideIncome;

  /// No description provided for @sharePosterShowIncome.
  ///
  /// In en, this message translates to:
  /// **'Show Income'**
  String get sharePosterShowIncome;

  /// No description provided for @sharePosterSaveSuccess.
  ///
  /// In en, this message translates to:
  /// **'Saved to gallery'**
  String get sharePosterSaveSuccess;

  /// No description provided for @shareGuidanceCopyText.
  ///
  /// In en, this message translates to:
  /// **'Track my expenses with BeeCount - open source, free & ad-free! 🐝 Download: https://github.com/TNT-Likely/BeeCount'**
  String get shareGuidanceCopyText;

  /// No description provided for @shareGuidanceCopied.
  ///
  /// In en, this message translates to:
  /// **'Text copied'**
  String get shareGuidanceCopied;

  /// No description provided for @sharePosterSaveFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save'**
  String get sharePosterSaveFailed;

  /// No description provided for @sharePosterPermissionDenied.
  ///
  /// In en, this message translates to:
  /// **'Gallery permission denied, please enable in settings'**
  String get sharePosterPermissionDenied;

  /// No description provided for @sharePosterGenerating.
  ///
  /// In en, this message translates to:
  /// **'Generating...'**
  String get sharePosterGenerating;

  /// No description provided for @sharePosterGenerateFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to generate poster, please try again'**
  String get sharePosterGenerateFailed;

  /// No description provided for @sharePosterNoLedger.
  ///
  /// In en, this message translates to:
  /// **'Please select a ledger first'**
  String get sharePosterNoLedger;

  /// No description provided for @sharePosterYearTitle.
  ///
  /// In en, this message translates to:
  /// **'My Annual Bookkeeping Report'**
  String get sharePosterYearTitle;

  /// No description provided for @sharePosterYearSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Record life with data, plan future with reason'**
  String get sharePosterYearSubtitle;

  /// No description provided for @sharePosterMonthTitle.
  ///
  /// In en, this message translates to:
  /// **'Monthly Bill Report'**
  String get sharePosterMonthTitle;

  /// No description provided for @sharePosterMonthSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Budget Wisely, Spend Rationally'**
  String get sharePosterMonthSubtitle;

  /// No description provided for @sharePosterLedgerTitle.
  ///
  /// In en, this message translates to:
  /// **'Ledger Statistics Report'**
  String get sharePosterLedgerTitle;

  /// No description provided for @sharePosterRecordDays.
  ///
  /// In en, this message translates to:
  /// **'Record Days'**
  String get sharePosterRecordDays;

  /// No description provided for @sharePosterRecordCount.
  ///
  /// In en, this message translates to:
  /// **'Record Count'**
  String get sharePosterRecordCount;

  /// No description provided for @sharePosterTotalExpense.
  ///
  /// In en, this message translates to:
  /// **'Total Expense'**
  String get sharePosterTotalExpense;

  /// No description provided for @sharePosterTotalIncome.
  ///
  /// In en, this message translates to:
  /// **'Total Income'**
  String get sharePosterTotalIncome;

  /// No description provided for @sharePosterYearBalance.
  ///
  /// In en, this message translates to:
  /// **'Annual Balance'**
  String get sharePosterYearBalance;

  /// No description provided for @sharePosterYearDeficit.
  ///
  /// In en, this message translates to:
  /// **'Annual Deficit'**
  String get sharePosterYearDeficit;

  /// No description provided for @sharePosterMonthBalance.
  ///
  /// In en, this message translates to:
  /// **'Monthly Balance'**
  String get sharePosterMonthBalance;

  /// No description provided for @sharePosterBalance.
  ///
  /// In en, this message translates to:
  /// **'Total Balance'**
  String get sharePosterBalance;

  /// No description provided for @sharePosterAvgMonthlyExpense.
  ///
  /// In en, this message translates to:
  /// **'Avg. Monthly Expense'**
  String get sharePosterAvgMonthlyExpense;

  /// No description provided for @sharePosterAvgMonthlyIncome.
  ///
  /// In en, this message translates to:
  /// **'Avg. Monthly Income'**
  String get sharePosterAvgMonthlyIncome;

  /// No description provided for @sharePosterAvgDailyExpense.
  ///
  /// In en, this message translates to:
  /// **'Avg. Daily Expense'**
  String get sharePosterAvgDailyExpense;

  /// No description provided for @sharePosterMaxExpenseMonth.
  ///
  /// In en, this message translates to:
  /// **'Highest Expense Month'**
  String get sharePosterMaxExpenseMonth;

  /// No description provided for @sharePosterTopExpense.
  ///
  /// In en, this message translates to:
  /// **'TOP 3 Expenses'**
  String get sharePosterTopExpense;

  /// No description provided for @sharePosterCompareLastMonth.
  ///
  /// In en, this message translates to:
  /// **'vs Last Month'**
  String get sharePosterCompareLastMonth;

  /// No description provided for @sharePosterIncreaseRate.
  ///
  /// In en, this message translates to:
  /// **'Increase'**
  String get sharePosterIncreaseRate;

  /// No description provided for @sharePosterDecreaseRate.
  ///
  /// In en, this message translates to:
  /// **'Decrease'**
  String get sharePosterDecreaseRate;

  /// No description provided for @sharePosterSavedMoneyTitle.
  ///
  /// In en, this message translates to:
  /// **'Congrats! You saved this month'**
  String get sharePosterSavedMoneyTitle;

  /// No description provided for @sharePosterLedgerName.
  ///
  /// In en, this message translates to:
  /// **'Ledger Name'**
  String get sharePosterLedgerName;

  /// No description provided for @sharePosterUnitDay.
  ///
  /// In en, this message translates to:
  /// **'days'**
  String get sharePosterUnitDay;

  /// No description provided for @sharePosterUnitCount.
  ///
  /// In en, this message translates to:
  /// **''**
  String get sharePosterUnitCount;

  /// No description provided for @sharePosterUnitYuan.
  ///
  /// In en, this message translates to:
  /// **''**
  String get sharePosterUnitYuan;

  /// No description provided for @userProfilePosterStartDate.
  ///
  /// In en, this message translates to:
  /// **'Bookkeeping since {date}'**
  String userProfilePosterStartDate(String date);

  /// No description provided for @userProfilePosterRecordDays.
  ///
  /// In en, this message translates to:
  /// **'Days'**
  String get userProfilePosterRecordDays;

  /// No description provided for @userProfilePosterDaysUnit.
  ///
  /// In en, this message translates to:
  /// **'days'**
  String get userProfilePosterDaysUnit;

  /// No description provided for @userProfilePosterRecordCount.
  ///
  /// In en, this message translates to:
  /// **'Records'**
  String get userProfilePosterRecordCount;

  /// No description provided for @userProfilePosterCountUnit.
  ///
  /// In en, this message translates to:
  /// **''**
  String get userProfilePosterCountUnit;

  /// No description provided for @userProfilePosterLedgerCount.
  ///
  /// In en, this message translates to:
  /// **'Ledgers'**
  String get userProfilePosterLedgerCount;

  /// No description provided for @userProfilePosterLedgerUnit.
  ///
  /// In en, this message translates to:
  /// **''**
  String get userProfilePosterLedgerUnit;

  /// No description provided for @mineDaysCount.
  ///
  /// In en, this message translates to:
  /// **'Days'**
  String get mineDaysCount;

  /// No description provided for @mineTotalRecords.
  ///
  /// In en, this message translates to:
  /// **'Records'**
  String get mineTotalRecords;

  /// No description provided for @mineCurrentBalance.
  ///
  /// In en, this message translates to:
  /// **'Ledger balance'**
  String get mineCurrentBalance;










































  /// No description provided for @mineLoggedInEmail.
  ///
  /// In en, this message translates to:
  /// **'Logged in'**
  String get mineLoggedInEmail;

  /// No description provided for @mineLogoutSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Tap to logout'**
  String get mineLogoutSubtitle;

  /// No description provided for @mineLogoutConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Logout'**
  String get mineLogoutConfirmTitle;

  /// No description provided for @mineLogoutConfirmMessage.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to logout?\nYou won\'t be able to use cloud sync after logout.'**
  String get mineLogoutConfirmMessage;

  /// No description provided for @mineLogoutButton.
  ///
  /// In en, this message translates to:
  /// **'Logout'**
  String get mineLogoutButton;




  /// No description provided for @mineImportProgressTitle.
  ///
  /// In en, this message translates to:
  /// **'Importing in background...'**
  String get mineImportProgressTitle;

  /// No description provided for @mineImportProgressSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Progress: {done}/{total}, Success {ok}, Failed {fail}'**
  String mineImportProgressSubtitle(Object done, Object fail, Object ok, Object total);

  /// No description provided for @mineImportCompleteTitle.
  ///
  /// In en, this message translates to:
  /// **'Import complete'**
  String get mineImportCompleteTitle;

  /// No description provided for @mineCategoryManagement.
  ///
  /// In en, this message translates to:
  /// **'Category Management'**
  String get mineCategoryManagement;

  /// No description provided for @mineCategoryManagementSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Edit custom categories'**
  String get mineCategoryManagementSubtitle;

  /// No description provided for @mineCategoryMigration.
  ///
  /// In en, this message translates to:
  /// **'Category Migration'**
  String get mineCategoryMigration;

  /// No description provided for @mineCategoryMigrationSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Migrate category data to other categories'**
  String get mineCategoryMigrationSubtitle;

  /// No description provided for @mineRecurringTransactions.
  ///
  /// In en, this message translates to:
  /// **'Recurring Bills'**
  String get mineRecurringTransactions;

  /// No description provided for @mineRecurringTransactionsSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Manage recurring bills'**
  String get mineRecurringTransactionsSubtitle;

  /// No description provided for @mineReminderSettings.
  ///
  /// In en, this message translates to:
  /// **'Reminder Settings'**
  String get mineReminderSettings;

  /// No description provided for @mineReminderSettingsSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Set daily recording reminders'**
  String get mineReminderSettingsSubtitle;

  /// No description provided for @minePersonalize.
  ///
  /// In en, this message translates to:
  /// **'Personalization'**
  String get minePersonalize;

  /// No description provided for @mineDisplayScale.
  ///
  /// In en, this message translates to:
  /// **'Display Scale'**
  String get mineDisplayScale;

  /// No description provided for @mineDisplayScaleSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Adjust text and UI element sizes'**
  String get mineDisplayScaleSubtitle;




  /// No description provided for @mineFeedback.
  ///
  /// In en, this message translates to:
  /// **'Feedback'**
  String get mineFeedback;

  /// No description provided for @mineFeedbackSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Report issues or suggestions'**
  String get mineFeedbackSubtitle;

  /// No description provided for @mineHelp.
  ///
  /// In en, this message translates to:
  /// **'Help'**
  String get mineHelp;

  /// No description provided for @helpCenterOpenInBrowser.
  ///
  /// In en, this message translates to:
  /// **'Open in browser'**
  String get helpCenterOpenInBrowser;

  /// No description provided for @helpCenterLoadFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to load. Please check your network.'**
  String get helpCenterLoadFailed;

  /// No description provided for @helpCenterRetry.
  ///
  /// In en, this message translates to:
  /// **'Retry'**
  String get helpCenterRetry;

  /// No description provided for @mineHelpSubtitle.
  ///
  /// In en, this message translates to:
  /// **'View documentation and FAQ'**
  String get mineHelpSubtitle;

  /// No description provided for @mineSupportAuthor.
  ///
  /// In en, this message translates to:
  /// **'Star the Project ⭐️'**
  String get mineSupportAuthor;

  /// No description provided for @mineSupportAuthorSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Open source, {count} stars'**
  String mineSupportAuthorSubtitle(String count);




  /// No description provided for @categoryEditTitle.
  ///
  /// In en, this message translates to:
  /// **'Edit Category'**
  String get categoryEditTitle;

  /// No description provided for @categoryNewTitle.
  ///
  /// In en, this message translates to:
  /// **'New Category'**
  String get categoryNewTitle;

  /// No description provided for @categoryDetailTooltip.
  ///
  /// In en, this message translates to:
  /// **'Category Details'**
  String get categoryDetailTooltip;

  /// No description provided for @categoryMigrationTooltip.
  ///
  /// In en, this message translates to:
  /// **'Category Migration'**
  String get categoryMigrationTooltip;

  /// No description provided for @categoryMigrationTitle.
  ///
  /// In en, this message translates to:
  /// **'Category Migration'**
  String get categoryMigrationTitle;

  /// No description provided for @categoryMigrationDescription.
  ///
  /// In en, this message translates to:
  /// **'Category Migration Instructions'**
  String get categoryMigrationDescription;

  /// No description provided for @categoryMigrationDescriptionContent.
  ///
  /// In en, this message translates to:
  /// **'• Migrate all transaction records from one category to another\n• After migration, all transaction data from the source category will be transferred to the target category\n• This operation cannot be undone, please choose carefully'**
  String get categoryMigrationDescriptionContent;

  /// No description provided for @categoryMigrationTypeLabel.
  ///
  /// In en, this message translates to:
  /// **'Select Type'**
  String get categoryMigrationTypeLabel;

  /// No description provided for @categoryMigrationFromLabel.
  ///
  /// In en, this message translates to:
  /// **'From Category'**
  String get categoryMigrationFromLabel;

  /// No description provided for @categoryMigrationFromHint.
  ///
  /// In en, this message translates to:
  /// **'Select category to migrate from'**
  String get categoryMigrationFromHint;

  /// No description provided for @categoryMigrationToLabel.
  ///
  /// In en, this message translates to:
  /// **'To Category'**
  String get categoryMigrationToLabel;

  /// No description provided for @categoryMigrationToHint.
  ///
  /// In en, this message translates to:
  /// **'Select target category'**
  String get categoryMigrationToHint;

  /// No description provided for @categoryMigrationToHintFirst.
  ///
  /// In en, this message translates to:
  /// **'Please select source category first'**
  String get categoryMigrationToHintFirst;

  /// No description provided for @categoryMigrationStartButton.
  ///
  /// In en, this message translates to:
  /// **'Start Migration'**
  String get categoryMigrationStartButton;

  /// No description provided for @categoryMigrationCannotTitle.
  ///
  /// In en, this message translates to:
  /// **'Cannot Migrate'**
  String get categoryMigrationCannotTitle;

  /// No description provided for @categoryMigrationCannotMessage.
  ///
  /// In en, this message translates to:
  /// **'Selected categories cannot be migrated, please check category status.'**
  String get categoryMigrationCannotMessage;

  /// No description provided for @categoryExpenseType.
  ///
  /// In en, this message translates to:
  /// **'Expense Category'**
  String get categoryExpenseType;

  /// No description provided for @categoryIncomeType.
  ///
  /// In en, this message translates to:
  /// **'Income Category'**
  String get categoryIncomeType;

  /// No description provided for @categoryDefaultTitle.
  ///
  /// In en, this message translates to:
  /// **'Default Category'**
  String get categoryDefaultTitle;

  /// No description provided for @categoryNameLabel.
  ///
  /// In en, this message translates to:
  /// **'Category Name'**
  String get categoryNameLabel;

  /// No description provided for @categoryNameHint.
  ///
  /// In en, this message translates to:
  /// **'Enter category name'**
  String get categoryNameHint;

  /// No description provided for @categoryNameRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter category name'**
  String get categoryNameRequired;

  /// No description provided for @categoryNameTooLong.
  ///
  /// In en, this message translates to:
  /// **'Category name cannot exceed 4 characters'**
  String get categoryNameTooLong;

  /// No description provided for @categoryNameDuplicate.
  ///
  /// In en, this message translates to:
  /// **'Category name already exists'**
  String get categoryNameDuplicate;

  /// No description provided for @categoryIconLabel.
  ///
  /// In en, this message translates to:
  /// **'Category Icon'**
  String get categoryIconLabel;

  /// No description provided for @categoryCustomIconTitle.
  ///
  /// In en, this message translates to:
  /// **'Custom Icon'**
  String get categoryCustomIconTitle;

  /// No description provided for @categoryCustomIconTapToSelect.
  ///
  /// In en, this message translates to:
  /// **'Tap to select image'**
  String get categoryCustomIconTapToSelect;

  /// No description provided for @categoryCustomIconTapToChange.
  ///
  /// In en, this message translates to:
  /// **'Tap to change image'**
  String get categoryCustomIconTapToChange;

  /// No description provided for @categoryCustomIconError.
  ///
  /// In en, this message translates to:
  /// **'Error selecting image'**
  String get categoryCustomIconError;

  /// No description provided for @categoryCustomIconRequired.
  ///
  /// In en, this message translates to:
  /// **'Please select a custom icon image'**
  String get categoryCustomIconRequired;

  /// No description provided for @categoryCustomIconCrop.
  ///
  /// In en, this message translates to:
  /// **'Crop Icon'**
  String get categoryCustomIconCrop;

  /// No description provided for @categoryDangerousOperations.
  ///
  /// In en, this message translates to:
  /// **'Dangerous Operations'**
  String get categoryDangerousOperations;

  /// No description provided for @categoryDeleteTitle.
  ///
  /// In en, this message translates to:
  /// **'Delete Category'**
  String get categoryDeleteTitle;

  /// No description provided for @categoryDeleteSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Cannot be recovered after deletion'**
  String get categoryDeleteSubtitle;

  /// No description provided for @categorySaveError.
  ///
  /// In en, this message translates to:
  /// **'Save failed'**
  String get categorySaveError;

  /// No description provided for @categoryUpdated.
  ///
  /// In en, this message translates to:
  /// **'Category \"{name}\" updated'**
  String categoryUpdated(Object name);

  /// No description provided for @categoryCreated.
  ///
  /// In en, this message translates to:
  /// **'Category \"{name}\" created'**
  String categoryCreated(Object name);

  /// No description provided for @categoryCannotDelete.
  ///
  /// In en, this message translates to:
  /// **'Cannot delete'**
  String get categoryCannotDelete;

  /// No description provided for @categoryCannotDeleteMessage.
  ///
  /// In en, this message translates to:
  /// **'This category has {count} transaction records. Please handle them first.'**
  String categoryCannotDeleteMessage(Object count);

  /// No description provided for @categoryShare.
  ///
  /// In en, this message translates to:
  /// **'Share Categories'**
  String get categoryShare;

  /// No description provided for @categoryImport.
  ///
  /// In en, this message translates to:
  /// **'Import Categories'**
  String get categoryImport;

  /// No description provided for @categoryClearUnused.
  ///
  /// In en, this message translates to:
  /// **'Clear Unused Categories'**
  String get categoryClearUnused;

  /// No description provided for @categoryClearUnusedTitle.
  ///
  /// In en, this message translates to:
  /// **'Clear Unused Categories'**
  String get categoryClearUnusedTitle;

  /// No description provided for @categoryClearUnusedMessage.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete {count} unused categories? This action cannot be undone.'**
  String categoryClearUnusedMessage(Object count);

  /// No description provided for @categoryClearUnusedListTitle.
  ///
  /// In en, this message translates to:
  /// **'Categories to be deleted:'**
  String get categoryClearUnusedListTitle;

  /// No description provided for @categoryClearUnusedEmpty.
  ///
  /// In en, this message translates to:
  /// **'No unused categories'**
  String get categoryClearUnusedEmpty;

  /// No description provided for @categoryClearUnusedSuccess.
  ///
  /// In en, this message translates to:
  /// **'Deleted {count} categories'**
  String categoryClearUnusedSuccess(Object count);

  /// No description provided for @categoryClearUnusedFailed.
  ///
  /// In en, this message translates to:
  /// **'Clear failed'**
  String get categoryClearUnusedFailed;

  /// No description provided for @categoryShareScopeTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Scope'**
  String get categoryShareScopeTitle;

  /// No description provided for @categoryShareScopeExpense.
  ///
  /// In en, this message translates to:
  /// **'Expense categories only'**
  String get categoryShareScopeExpense;

  /// No description provided for @categoryShareScopeIncome.
  ///
  /// In en, this message translates to:
  /// **'Income categories only'**
  String get categoryShareScopeIncome;

  /// No description provided for @categoryShareScopeAll.
  ///
  /// In en, this message translates to:
  /// **'All categories'**
  String get categoryShareScopeAll;

  /// No description provided for @categoryShareSuccess.
  ///
  /// In en, this message translates to:
  /// **'Saved to {path}'**
  String categoryShareSuccess(Object path);

  /// No description provided for @categoryShareSubject.
  ///
  /// In en, this message translates to:
  /// **'BeeCount Category Configuration'**
  String get categoryShareSubject;

  /// No description provided for @categoryShareFailed.
  ///
  /// In en, this message translates to:
  /// **'Share failed'**
  String get categoryShareFailed;

  /// No description provided for @categoryImportInvalidFile.
  ///
  /// In en, this message translates to:
  /// **'Please select a category package file (.zip)'**
  String get categoryImportInvalidFile;

  /// No description provided for @categoryImportModeTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Import Mode'**
  String get categoryImportModeTitle;

  /// No description provided for @categoryImportModeMerge.
  ///
  /// In en, this message translates to:
  /// **'Merge'**
  String get categoryImportModeMerge;

  /// No description provided for @categoryImportModeMergeDesc.
  ///
  /// In en, this message translates to:
  /// **'Keep existing, add new ones'**
  String get categoryImportModeMergeDesc;

  /// No description provided for @categoryImportModeOverwrite.
  ///
  /// In en, this message translates to:
  /// **'Overwrite'**
  String get categoryImportModeOverwrite;

  /// No description provided for @categoryImportModeOverwriteDesc.
  ///
  /// In en, this message translates to:
  /// **'Clear unused categories then import'**
  String get categoryImportModeOverwriteDesc;

  /// No description provided for @categoryImportSuccess.
  ///
  /// In en, this message translates to:
  /// **'Import successful'**
  String get categoryImportSuccess;

  /// No description provided for @categoryImportSuccessDetail.
  ///
  /// In en, this message translates to:
  /// **'Imported {imported} categories, skipped {skipped}, imported {icons} icons'**
  String categoryImportSuccessDetail(int imported, int skipped, int icons);

  /// No description provided for @categoryImportFailed.
  ///
  /// In en, this message translates to:
  /// **'Import failed'**
  String get categoryImportFailed;

  /// No description provided for @categoryDeleteConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Delete Category'**
  String get categoryDeleteConfirmTitle;

  /// No description provided for @categoryDeleteConfirmMessage.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete category \"{name}\"? This action cannot be undone.'**
  String categoryDeleteConfirmMessage(Object name);

  /// No description provided for @categoryDeleteError.
  ///
  /// In en, this message translates to:
  /// **'Delete failed'**
  String get categoryDeleteError;

  /// No description provided for @categoryDeleted.
  ///
  /// In en, this message translates to:
  /// **'Category \"{name}\" deleted'**
  String categoryDeleted(Object name);

  /// No description provided for @categorySubCategoryTitle.
  ///
  /// In en, this message translates to:
  /// **'Subcategory'**
  String get categorySubCategoryTitle;

  /// No description provided for @categorySubCategoryDescriptionEnabled.
  ///
  /// In en, this message translates to:
  /// **'This category belongs to a parent category'**
  String get categorySubCategoryDescriptionEnabled;

  /// No description provided for @categorySubCategoryDescriptionDisabled.
  ///
  /// In en, this message translates to:
  /// **'This is an independent top-level category'**
  String get categorySubCategoryDescriptionDisabled;

  /// No description provided for @categoryParentCategoryTitle.
  ///
  /// In en, this message translates to:
  /// **'Parent Category'**
  String get categoryParentCategoryTitle;

  /// No description provided for @categoryParentCategoryHint.
  ///
  /// In en, this message translates to:
  /// **'Please select parent category'**
  String get categoryParentCategoryHint;

  /// No description provided for @categorySelectParentTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Parent Category'**
  String get categorySelectParentTitle;

  /// No description provided for @categorySubCategoryCreated.
  ///
  /// In en, this message translates to:
  /// **'Subcategory added: {name}'**
  String categorySubCategoryCreated(Object name);

  /// No description provided for @categoryParentRequired.
  ///
  /// In en, this message translates to:
  /// **'Please select parent category'**
  String get categoryParentRequired;

  /// No description provided for @categoryParentRequiredTitle.
  ///
  /// In en, this message translates to:
  /// **'Error'**
  String get categoryParentRequiredTitle;

  /// No description provided for @categoryExpenseList.
  ///
  /// In en, this message translates to:
  /// **'Dining-Transport-Shopping-Entertainment-Home-Family-Communication-Utilities-Housing-Medical-Education-Pets-Sports-Digital-Travel-Alcohol & Tobacco-Baby Care-Beauty-Repair-Social-Learning-Car-Taxi-Subway-Delivery-Property-Parking-Donation-Give Gift-Tax-Beverage-Clothing-Snacks-Send Red Packet-Fruit-Game-Book-Lover-Decoration-Daily Goods-Lottery-Stock-Social Security-Express-Work'**
  String get categoryExpenseList;

  /// No description provided for @categoryIncomeList.
  ///
  /// In en, this message translates to:
  /// **'Salary-Investment-Receive Red Packet-Bonus-Reimbursement-Part time-Receive Gift-Interest-Refund-Investment Income-Second hand-Social Benefit-Tax Refund-Provident Fund'**
  String get categoryIncomeList;

  /// No description provided for @categoryExpenseDining.
  ///
  /// In en, this message translates to:
  /// **'Dining-Breakfast-Lunch-Dinner-Meituan Delivery-Ele.me Delivery-JD Delivery-Restaurant-Food'**
  String get categoryExpenseDining;

  /// No description provided for @categoryExpenseSnacks.
  ///
  /// In en, this message translates to:
  /// **'Snacks-Cookies-Chips-Candy-Chocolate-Nuts'**
  String get categoryExpenseSnacks;

  /// No description provided for @categoryExpenseFruit.
  ///
  /// In en, this message translates to:
  /// **'Fruit-Apple-Banana-Orange-Grape-Watermelon-Other Fruits'**
  String get categoryExpenseFruit;

  /// No description provided for @categoryExpenseBeverage.
  ///
  /// In en, this message translates to:
  /// **'Beverage-Milk Tea-Coffee-Juice-Soda-Mineral Water'**
  String get categoryExpenseBeverage;

  /// No description provided for @categoryExpensePastry.
  ///
  /// In en, this message translates to:
  /// **'Pastry-Cake-Bread-Dessert-Baked Goods'**
  String get categoryExpensePastry;

  /// No description provided for @categoryExpenseCooking.
  ///
  /// In en, this message translates to:
  /// **'Cooking Ingredients-Vegetables-Meat-Seafood-Seasoning-Grain & Oil'**
  String get categoryExpenseCooking;

  /// No description provided for @categoryExpenseShopping.
  ///
  /// In en, this message translates to:
  /// **'Shopping-Clothing-Shoes & Hats-Bags-Accessories-Daily Necessities'**
  String get categoryExpenseShopping;

  /// No description provided for @categoryExpensePets.
  ///
  /// In en, this message translates to:
  /// **'Pets-Pet Food-Pet Supplies-Pet Medical-Pet Grooming'**
  String get categoryExpensePets;

  /// No description provided for @categoryExpenseTransport.
  ///
  /// In en, this message translates to:
  /// **'Transport-Subway-Bus-Taxi-Ride hailing-Parking Fee-Fuel'**
  String get categoryExpenseTransport;

  /// No description provided for @categoryExpenseCar.
  ///
  /// In en, this message translates to:
  /// **'Car-Car Maintenance-Car Repair-Car Insurance-Car Wash-Traffic Fine'**
  String get categoryExpenseCar;

  /// No description provided for @categoryExpenseClothing.
  ///
  /// In en, this message translates to:
  /// **'Apparel-Top-Pants-Dress-Shoes-Apparel Accessories'**
  String get categoryExpenseClothing;

  /// No description provided for @categoryExpenseDailyGoods.
  ///
  /// In en, this message translates to:
  /// **'Daily Goods-Personal Care-Paper Products-Cleaning Supplies-Kitchen Supplies'**
  String get categoryExpenseDailyGoods;

  /// No description provided for @categoryExpenseEducation.
  ///
  /// In en, this message translates to:
  /// **'Education-Tuition-Training Fee-Books-Stationery-Office Supplies'**
  String get categoryExpenseEducation;

  /// No description provided for @categoryExpenseInvestLoss.
  ///
  /// In en, this message translates to:
  /// **'Investment Loss-Stock Loss-Fund Loss-Other Investment Loss'**
  String get categoryExpenseInvestLoss;

  /// No description provided for @categoryExpenseEntertainment.
  ///
  /// In en, this message translates to:
  /// **'Entertainment-Movie-KTV-Amusement Park-Bar-Other Entertainment'**
  String get categoryExpenseEntertainment;

  /// No description provided for @categoryExpenseGame.
  ///
  /// In en, this message translates to:
  /// **'Game-Game Top up-Game Equipment-Game Membership'**
  String get categoryExpenseGame;

  /// No description provided for @categoryExpenseHealthProducts.
  ///
  /// In en, this message translates to:
  /// **'Health Products-Vitamins-Health Food-Nutritional Supplements'**
  String get categoryExpenseHealthProducts;

  /// No description provided for @categoryExpenseSubscription.
  ///
  /// In en, this message translates to:
  /// **'Subscription-Video Membership-Music Membership-Cloud Storage-Other Subscription'**
  String get categoryExpenseSubscription;

  /// No description provided for @categoryExpenseSports.
  ///
  /// In en, this message translates to:
  /// **'Sports-Gym-Sports Equipment-Sports Course-Outdoor Activity'**
  String get categoryExpenseSports;

  /// No description provided for @categoryExpenseHousing.
  ///
  /// In en, this message translates to:
  /// **'Housing-Rent-Property Fee-Mortgage-Renovation'**
  String get categoryExpenseHousing;

  /// No description provided for @categoryExpenseHome.
  ///
  /// In en, this message translates to:
  /// **'Home-Furniture-Appliances-Decorations-Bedding'**
  String get categoryExpenseHome;

  /// No description provided for @categoryExpenseBeauty.
  ///
  /// In en, this message translates to:
  /// **'Beauty-Skincare-Cosmetics-Beauty Salon-Nail Care'**
  String get categoryExpenseBeauty;

  /// No description provided for @categoryIncomeSalary.
  ///
  /// In en, this message translates to:
  /// **'Salary-Base Salary-Performance Bonus-Year end Bonus-Overtime Pay'**
  String get categoryIncomeSalary;

  /// No description provided for @categoryIncomeInvestment.
  ///
  /// In en, this message translates to:
  /// **'Investment-Fund Earnings-Stock Dividend-Wealth Management-Other Wealth Management'**
  String get categoryIncomeInvestment;

  /// No description provided for @categoryIncomeRedPacket.
  ///
  /// In en, this message translates to:
  /// **'Red Packet-Holiday Red Packet-Birthday Red Packet-Return Gift'**
  String get categoryIncomeRedPacket;

  /// No description provided for @categoryIncomeBonus.
  ///
  /// In en, this message translates to:
  /// **'Bonus-Annual Bonus-Quarterly Bonus-Project Bonus-Other Bonus'**
  String get categoryIncomeBonus;

  /// No description provided for @categoryIncomeReimbursement.
  ///
  /// In en, this message translates to:
  /// **'Reimbursement-Travel Reimbursement-Meal Reimbursement-Other Reimbursement'**
  String get categoryIncomeReimbursement;

  /// No description provided for @categoryIncomePartTime.
  ///
  /// In en, this message translates to:
  /// **'Part time-Part time Income-Side Income'**
  String get categoryIncomePartTime;

  /// No description provided for @categoryIncomeGift.
  ///
  /// In en, this message translates to:
  /// **'Gift-Wedding Gift-Birthday Gift-Other Gift'**
  String get categoryIncomeGift;

  /// No description provided for @categoryIncomeInterest.
  ///
  /// In en, this message translates to:
  /// **'Interest-Bank Interest-Other Interest'**
  String get categoryIncomeInterest;

  /// No description provided for @categoryIncomeRefund.
  ///
  /// In en, this message translates to:
  /// **'Refund-Shopping Refund-Service Refund-Other Refund'**
  String get categoryIncomeRefund;

  /// No description provided for @categoryIncomeInvestIncome.
  ///
  /// In en, this message translates to:
  /// **'Investment Income-Stock Earnings-Fund Investment-Other Investment Income'**
  String get categoryIncomeInvestIncome;

  /// No description provided for @categoryIncomeSecondHand.
  ///
  /// In en, this message translates to:
  /// **'Second hand-Idle Items-Second hand Goods'**
  String get categoryIncomeSecondHand;

  /// No description provided for @categoryIncomeSocialBenefit.
  ///
  /// In en, this message translates to:
  /// **'Social Benefit-Unemployment Insurance-Maternity Subsidy-Other Subsidy'**
  String get categoryIncomeSocialBenefit;

  /// No description provided for @categoryIncomeTaxRefund.
  ///
  /// In en, this message translates to:
  /// **'Tax Refund-Individual Tax Refund-Other Tax Refund'**
  String get categoryIncomeTaxRefund;

  /// No description provided for @categoryIncomeProvidentFund.
  ///
  /// In en, this message translates to:
  /// **'Provident Fund-Provident Fund Withdrawal-Provident Fund Interest'**
  String get categoryIncomeProvidentFund;

  /// No description provided for @personalizeTitle.
  ///
  /// In en, this message translates to:
  /// **'Theme color'**
  String get personalizeTitle;

  /// No description provided for @personalizeSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Pick or customize the app accent color'**
  String get personalizeSubtitle;

  /// No description provided for @personalizeCustomColor.
  ///
  /// In en, this message translates to:
  /// **'Choose custom color'**
  String get personalizeCustomColor;

  /// No description provided for @personalizeCustomTitle.
  ///
  /// In en, this message translates to:
  /// **'Custom'**
  String get personalizeCustomTitle;

  /// No description provided for @personalizeHue.
  ///
  /// In en, this message translates to:
  /// **'Hue ({value}°)'**
  String personalizeHue(Object value);

  /// No description provided for @personalizeSaturation.
  ///
  /// In en, this message translates to:
  /// **'Saturation ({value}%)'**
  String personalizeSaturation(Object value);

  /// No description provided for @personalizeBrightness.
  ///
  /// In en, this message translates to:
  /// **'Brightness ({value}%)'**
  String personalizeBrightness(Object value);

  /// No description provided for @personalizeSelectColor.
  ///
  /// In en, this message translates to:
  /// **'Select this color'**
  String get personalizeSelectColor;

  /// No description provided for @appearanceThemeMode.
  ///
  /// In en, this message translates to:
  /// **'Appearance'**
  String get appearanceThemeMode;

  /// No description provided for @appearanceThemeModeSystem.
  ///
  /// In en, this message translates to:
  /// **'Follow System'**
  String get appearanceThemeModeSystem;

  /// No description provided for @appearanceThemeModeLight.
  ///
  /// In en, this message translates to:
  /// **'Light Mode'**
  String get appearanceThemeModeLight;

  /// No description provided for @appearanceThemeModeDark.
  ///
  /// In en, this message translates to:
  /// **'Dark Mode'**
  String get appearanceThemeModeDark;

  /// No description provided for @appearanceDarkModePattern.
  ///
  /// In en, this message translates to:
  /// **'Dark Mode Header Pattern'**
  String get appearanceDarkModePattern;

  /// No description provided for @appearancePatternNone.
  ///
  /// In en, this message translates to:
  /// **'None'**
  String get appearancePatternNone;

  /// No description provided for @appearancePatternIcons.
  ///
  /// In en, this message translates to:
  /// **'Icon Tiling'**
  String get appearancePatternIcons;

  /// No description provided for @appearancePatternParticles.
  ///
  /// In en, this message translates to:
  /// **'Particles'**
  String get appearancePatternParticles;

  /// No description provided for @appearancePatternHoneycomb.
  ///
  /// In en, this message translates to:
  /// **'Honeycomb'**
  String get appearancePatternHoneycomb;

  /// No description provided for @appearanceAmountFormat.
  ///
  /// In en, this message translates to:
  /// **'Balance Display Format'**
  String get appearanceAmountFormat;

  /// No description provided for @appearanceAmountFormatFull.
  ///
  /// In en, this message translates to:
  /// **'Full Amount'**
  String get appearanceAmountFormatFull;

  /// No description provided for @appearanceAmountFormatFullDesc.
  ///
  /// In en, this message translates to:
  /// **'Show full amount, e.g. 123,456.78'**
  String get appearanceAmountFormatFullDesc;

  /// No description provided for @appearanceAmountFormatCompact.
  ///
  /// In en, this message translates to:
  /// **'Compact'**
  String get appearanceAmountFormatCompact;

  /// No description provided for @appearanceAmountFormatCompactDesc.
  ///
  /// In en, this message translates to:
  /// **'Abbreviate large amounts, e.g. 12.3K (only affects account balance)'**
  String get appearanceAmountFormatCompactDesc;



  /// No description provided for @appearanceShowTransactionTime.
  ///
  /// In en, this message translates to:
  /// **'Show Transaction Time'**
  String get appearanceShowTransactionTime;

  /// No description provided for @appearanceShowTransactionTimeDesc.
  ///
  /// In en, this message translates to:
  /// **'Display time in transaction list, allow time selection when editing'**
  String get appearanceShowTransactionTimeDesc;

  /// No description provided for @appearanceNoteDisplay.
  ///
  /// In en, this message translates to:
  /// **'Note display'**
  String get appearanceNoteDisplay;

  /// No description provided for @appearanceNoteDisplayCategory.
  ///
  /// In en, this message translates to:
  /// **'Category first'**
  String get appearanceNoteDisplayCategory;

  /// No description provided for @appearanceNoteDisplayCategoryDesc.
  ///
  /// In en, this message translates to:
  /// **'Show category, note in parentheses'**
  String get appearanceNoteDisplayCategoryDesc;

  /// No description provided for @appearanceNoteDisplayNote.
  ///
  /// In en, this message translates to:
  /// **'Note first'**
  String get appearanceNoteDisplayNote;

  /// No description provided for @appearanceNoteDisplayNoteDesc.
  ///
  /// In en, this message translates to:
  /// **'Show note when present, else category'**
  String get appearanceNoteDisplayNoteDesc;

  /// No description provided for @appearanceNoteHistory.
  ///
  /// In en, this message translates to:
  /// **'Note history'**
  String get appearanceNoteHistory;

  /// No description provided for @appearanceNoteHistoryScope.
  ///
  /// In en, this message translates to:
  /// **'Show'**
  String get appearanceNoteHistoryScope;

  /// No description provided for @appearanceNoteHistoryScopeAllCategories.
  ///
  /// In en, this message translates to:
  /// **'All categories'**
  String get appearanceNoteHistoryScopeAllCategories;

  /// No description provided for @appearanceNoteHistoryScopeCurrentCategory.
  ///
  /// In en, this message translates to:
  /// **'Current category'**
  String get appearanceNoteHistoryScopeCurrentCategory;

  /// No description provided for @appearanceNoteHistorySort.
  ///
  /// In en, this message translates to:
  /// **'Sort by'**
  String get appearanceNoteHistorySort;

  /// No description provided for @appearanceNoteHistorySortFrequency.
  ///
  /// In en, this message translates to:
  /// **'Usage frequency'**
  String get appearanceNoteHistorySortFrequency;

  /// No description provided for @appearanceNoteHistorySortRecent.
  ///
  /// In en, this message translates to:
  /// **'Most recent'**
  String get appearanceNoteHistorySortRecent;

  /// No description provided for @appearanceNoteHistoryLimit.
  ///
  /// In en, this message translates to:
  /// **'Display limit'**
  String get appearanceNoteHistoryLimit;

  /// No description provided for @appearanceNoteHistoryLimitHint.
  ///
  /// In en, this message translates to:
  /// **'Enter a value from 1 to 100'**
  String get appearanceNoteHistoryLimitHint;

  /// No description provided for @appearanceNoteHistoryLimitInvalid.
  ///
  /// In en, this message translates to:
  /// **'Enter an integer from 1 to 100'**
  String get appearanceNoteHistoryLimitInvalid;

  /// No description provided for @appearanceColorScheme.
  ///
  /// In en, this message translates to:
  /// **'Income/Expense Color Scheme'**
  String get appearanceColorScheme;

  /// No description provided for @appearanceColorSchemeOn.
  ///
  /// In en, this message translates to:
  /// **'Red for income · Green for expense'**
  String get appearanceColorSchemeOn;

  /// No description provided for @appearanceColorSchemeOff.
  ///
  /// In en, this message translates to:
  /// **'Red for expense · Green for income'**
  String get appearanceColorSchemeOff;

  /// No description provided for @appearanceColorSchemeOnDesc.
  ///
  /// In en, this message translates to:
  /// **'Red represents income, green represents expense'**
  String get appearanceColorSchemeOnDesc;

  /// No description provided for @appearanceColorSchemeOffDesc.
  ///
  /// In en, this message translates to:
  /// **'Red represents expense, green represents income'**
  String get appearanceColorSchemeOffDesc;

  /// No description provided for @fontSettingsCurrentScale.
  ///
  /// In en, this message translates to:
  /// **'Current scale: x{scale}'**
  String fontSettingsCurrentScale(Object scale);

  /// No description provided for @fontSettingsPreview.
  ///
  /// In en, this message translates to:
  /// **'Live Preview'**
  String get fontSettingsPreview;

  /// No description provided for @fontSettingsPreviewText.
  ///
  /// In en, this message translates to:
  /// **'Spent 23.50 on lunch today, record it;\nRecorded for 45 days this month, 320 entries;\nPersistence is victory!'**
  String get fontSettingsPreviewText;

  /// No description provided for @fontSettingsCurrentLevel.
  ///
  /// In en, this message translates to:
  /// **'Current level: {level} (scale x{scale})'**
  String fontSettingsCurrentLevel(Object level, Object scale);

  /// No description provided for @fontSettingsQuickLevel.
  ///
  /// In en, this message translates to:
  /// **'Quick Levels'**
  String get fontSettingsQuickLevel;

  /// No description provided for @fontSettingsCustomAdjust.
  ///
  /// In en, this message translates to:
  /// **'Custom Adjustment'**
  String get fontSettingsCustomAdjust;

  /// No description provided for @fontSettingsDescription.
  ///
  /// In en, this message translates to:
  /// **'Note: This setting ensures consistent display at 1.0x across all devices, with device differences auto-compensated; adjust values for personalized scaling on this consistent base.'**
  String get fontSettingsDescription;

  /// No description provided for @fontSettingsExtraSmall.
  ///
  /// In en, this message translates to:
  /// **'Extra Small'**
  String get fontSettingsExtraSmall;

  /// No description provided for @fontSettingsVerySmall.
  ///
  /// In en, this message translates to:
  /// **'Very Small'**
  String get fontSettingsVerySmall;

  /// No description provided for @fontSettingsSmall.
  ///
  /// In en, this message translates to:
  /// **'Small'**
  String get fontSettingsSmall;

  /// No description provided for @fontSettingsStandard.
  ///
  /// In en, this message translates to:
  /// **'Standard'**
  String get fontSettingsStandard;

  /// No description provided for @fontSettingsLarge.
  ///
  /// In en, this message translates to:
  /// **'Large'**
  String get fontSettingsLarge;

  /// No description provided for @fontSettingsBig.
  ///
  /// In en, this message translates to:
  /// **'Big'**
  String get fontSettingsBig;

  /// No description provided for @fontSettingsVeryBig.
  ///
  /// In en, this message translates to:
  /// **'Very Big'**
  String get fontSettingsVeryBig;

  /// No description provided for @fontSettingsExtraBig.
  ///
  /// In en, this message translates to:
  /// **'Extra Big'**
  String get fontSettingsExtraBig;

  /// No description provided for @fontSettingsMoreStyles.
  ///
  /// In en, this message translates to:
  /// **'More Styles'**
  String get fontSettingsMoreStyles;

  /// No description provided for @fontSettingsPageTitle.
  ///
  /// In en, this message translates to:
  /// **'Page Title'**
  String get fontSettingsPageTitle;

  /// No description provided for @fontSettingsBlockTitle.
  ///
  /// In en, this message translates to:
  /// **'Block Title'**
  String get fontSettingsBlockTitle;

  /// No description provided for @fontSettingsBodyExample.
  ///
  /// In en, this message translates to:
  /// **'Body Text'**
  String get fontSettingsBodyExample;

  /// No description provided for @fontSettingsLabelExample.
  ///
  /// In en, this message translates to:
  /// **'Label Text'**
  String get fontSettingsLabelExample;

  /// No description provided for @fontSettingsStrongNumber.
  ///
  /// In en, this message translates to:
  /// **'Strong Number'**
  String get fontSettingsStrongNumber;

  /// No description provided for @fontSettingsListTitle.
  ///
  /// In en, this message translates to:
  /// **'List Item Title'**
  String get fontSettingsListTitle;

  /// No description provided for @fontSettingsListSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Helper Text'**
  String get fontSettingsListSubtitle;

  /// No description provided for @fontSettingsScreenInfo.
  ///
  /// In en, this message translates to:
  /// **'Screen Adaptation Info'**
  String get fontSettingsScreenInfo;

  /// No description provided for @fontSettingsScreenDensity.
  ///
  /// In en, this message translates to:
  /// **'Screen Density'**
  String get fontSettingsScreenDensity;

  /// No description provided for @fontSettingsScreenWidth.
  ///
  /// In en, this message translates to:
  /// **'Screen Width'**
  String get fontSettingsScreenWidth;

  /// No description provided for @fontSettingsDeviceScale.
  ///
  /// In en, this message translates to:
  /// **'Device Scale'**
  String get fontSettingsDeviceScale;

  /// No description provided for @fontSettingsUserScale.
  ///
  /// In en, this message translates to:
  /// **'User Scale'**
  String get fontSettingsUserScale;

  /// No description provided for @fontSettingsFinalScale.
  ///
  /// In en, this message translates to:
  /// **'Final Scale'**
  String get fontSettingsFinalScale;

  /// No description provided for @fontSettingsBaseDevice.
  ///
  /// In en, this message translates to:
  /// **'Base Device'**
  String get fontSettingsBaseDevice;

  /// No description provided for @fontSettingsRecommendedScale.
  ///
  /// In en, this message translates to:
  /// **'Recommended Scale'**
  String get fontSettingsRecommendedScale;

  /// No description provided for @fontSettingsYes.
  ///
  /// In en, this message translates to:
  /// **'Yes'**
  String get fontSettingsYes;

  /// No description provided for @fontSettingsNo.
  ///
  /// In en, this message translates to:
  /// **'No'**
  String get fontSettingsNo;

  /// No description provided for @fontSettingsScaleExample.
  ///
  /// In en, this message translates to:
  /// **'This box and spacing auto-scale based on device'**
  String get fontSettingsScaleExample;

  /// No description provided for @fontSettingsPreciseAdjust.
  ///
  /// In en, this message translates to:
  /// **'Precise Adjustment'**
  String get fontSettingsPreciseAdjust;

  /// No description provided for @fontSettingsResetTo1x.
  ///
  /// In en, this message translates to:
  /// **'Reset to 1.0x'**
  String get fontSettingsResetTo1x;

  /// No description provided for @fontSettingsAdaptBase.
  ///
  /// In en, this message translates to:
  /// **'Adapt to Base'**
  String get fontSettingsAdaptBase;

  /// No description provided for @reminderTitle.
  ///
  /// In en, this message translates to:
  /// **'Recording Reminder'**
  String get reminderTitle;

  /// No description provided for @reminderSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Set daily recording reminder time'**
  String get reminderSubtitle;

  /// No description provided for @reminderDailyTitle.
  ///
  /// In en, this message translates to:
  /// **'Daily Recording Reminder'**
  String get reminderDailyTitle;

  /// No description provided for @reminderDailySubtitle.
  ///
  /// In en, this message translates to:
  /// **'When enabled, will remind you to record at specified time'**
  String get reminderDailySubtitle;

  /// No description provided for @reminderDailyBody.
  ///
  /// In en, this message translates to:
  /// **'Don\'t forget to record today\'s income and expenses 💰'**
  String get reminderDailyBody;

  /// No description provided for @reminderTimeTitle.
  ///
  /// In en, this message translates to:
  /// **'Reminder Time'**
  String get reminderTimeTitle;

  /// No description provided for @commonSelectTime.
  ///
  /// In en, this message translates to:
  /// **'Select Time'**
  String get commonSelectTime;

  /// No description provided for @reminderTestNotification.
  ///
  /// In en, this message translates to:
  /// **'Send Test Notification'**
  String get reminderTestNotification;

  /// No description provided for @reminderTestSent.
  ///
  /// In en, this message translates to:
  /// **'Test notification sent'**
  String get reminderTestSent;

  /// No description provided for @reminderTestTitle.
  ///
  /// In en, this message translates to:
  /// **'Test Notification'**
  String get reminderTestTitle;

  /// No description provided for @reminderTestBody.
  ///
  /// In en, this message translates to:
  /// **'This is a test notification, tap to see the effect'**
  String get reminderTestBody;

  /// No description provided for @reminderCheckBattery.
  ///
  /// In en, this message translates to:
  /// **'Check Battery Optimization Status'**
  String get reminderCheckBattery;

  /// No description provided for @reminderBatteryStatus.
  ///
  /// In en, this message translates to:
  /// **'Battery Optimization Status'**
  String get reminderBatteryStatus;

  /// No description provided for @reminderManufacturer.
  ///
  /// In en, this message translates to:
  /// **'Manufacturer: {value}'**
  String reminderManufacturer(Object value);

  /// No description provided for @reminderModel.
  ///
  /// In en, this message translates to:
  /// **'Model: {value}'**
  String reminderModel(Object value);

  /// No description provided for @reminderAndroidVersion.
  ///
  /// In en, this message translates to:
  /// **'Android Version: {value}'**
  String reminderAndroidVersion(Object value);

  /// No description provided for @reminderBatteryIgnored.
  ///
  /// In en, this message translates to:
  /// **'Battery optimization: Ignored ✅'**
  String get reminderBatteryIgnored;

  /// No description provided for @reminderBatteryNotIgnored.
  ///
  /// In en, this message translates to:
  /// **'Battery optimization: Not ignored ⚠️'**
  String get reminderBatteryNotIgnored;

  /// No description provided for @reminderBatteryAdvice.
  ///
  /// In en, this message translates to:
  /// **'Recommend disabling battery optimization for proper notifications'**
  String get reminderBatteryAdvice;

  /// No description provided for @reminderCheckChannel.
  ///
  /// In en, this message translates to:
  /// **'Check Notification Channel Settings'**
  String get reminderCheckChannel;

  /// No description provided for @reminderChannelStatus.
  ///
  /// In en, this message translates to:
  /// **'Notification Channel Status'**
  String get reminderChannelStatus;

  /// No description provided for @reminderChannelEnabled.
  ///
  /// In en, this message translates to:
  /// **'Channel enabled: Yes ✅'**
  String get reminderChannelEnabled;

  /// No description provided for @reminderChannelDisabled.
  ///
  /// In en, this message translates to:
  /// **'Channel enabled: No ❌'**
  String get reminderChannelDisabled;

  /// No description provided for @reminderChannelImportance.
  ///
  /// In en, this message translates to:
  /// **'Importance: {value}'**
  String reminderChannelImportance(Object value);

  /// No description provided for @reminderChannelSoundOn.
  ///
  /// In en, this message translates to:
  /// **'Sound: On 🔊'**
  String get reminderChannelSoundOn;

  /// No description provided for @reminderChannelSoundOff.
  ///
  /// In en, this message translates to:
  /// **'Sound: Off 🔇'**
  String get reminderChannelSoundOff;

  /// No description provided for @reminderChannelVibrationOn.
  ///
  /// In en, this message translates to:
  /// **'Vibration: On 📳'**
  String get reminderChannelVibrationOn;

  /// No description provided for @reminderChannelVibrationOff.
  ///
  /// In en, this message translates to:
  /// **'Vibration: Off'**
  String get reminderChannelVibrationOff;

  /// No description provided for @reminderChannelDndBypass.
  ///
  /// In en, this message translates to:
  /// **'Do Not Disturb: Can bypass'**
  String get reminderChannelDndBypass;

  /// No description provided for @reminderChannelDndNoBypass.
  ///
  /// In en, this message translates to:
  /// **'Do Not Disturb: Cannot bypass'**
  String get reminderChannelDndNoBypass;

  /// No description provided for @reminderChannelAdvice.
  ///
  /// In en, this message translates to:
  /// **'⚠️ Recommended settings:'**
  String get reminderChannelAdvice;

  /// No description provided for @reminderChannelAdviceImportance.
  ///
  /// In en, this message translates to:
  /// **'• Importance: Urgent or High'**
  String get reminderChannelAdviceImportance;

  /// No description provided for @reminderChannelAdviceSound.
  ///
  /// In en, this message translates to:
  /// **'• Enable sound and vibration'**
  String get reminderChannelAdviceSound;

  /// No description provided for @reminderChannelAdviceBanner.
  ///
  /// In en, this message translates to:
  /// **'• Allow banner notifications'**
  String get reminderChannelAdviceBanner;

  /// No description provided for @reminderChannelAdviceXiaomi.
  ///
  /// In en, this message translates to:
  /// **'• Xiaomi phones need individual channel setup'**
  String get reminderChannelAdviceXiaomi;

  /// No description provided for @reminderChannelGood.
  ///
  /// In en, this message translates to:
  /// **'✅ Notification channel well configured'**
  String get reminderChannelGood;

  /// No description provided for @reminderOpenAppSettings.
  ///
  /// In en, this message translates to:
  /// **'Open App Settings'**
  String get reminderOpenAppSettings;

  /// No description provided for @reminderAppSettingsMessage.
  ///
  /// In en, this message translates to:
  /// **'Please allow notifications and disable battery optimization in settings'**
  String get reminderAppSettingsMessage;

  /// No description provided for @reminderDescription.
  ///
  /// In en, this message translates to:
  /// **'Tip: When recording reminder is enabled, the system will send notifications at the specified time daily to remind you to record income and expenses.'**
  String get reminderDescription;

  /// No description provided for @reminderIOSInstructions.
  ///
  /// In en, this message translates to:
  /// **'🍎 iOS notification settings:\n• Settings > Notifications > Bee Accounting\n• Enable \"Allow Notifications\"\n• Set notification style: Banner or Alert\n• Enable sound and vibration\n\n⚠️ Important Note:\n• iOS local notifications depend on app process\n• Do not force quit app from task manager\n• Notifications work when app is in background or foreground\n• Force quitting will disable notifications\n\n💡 Usage Tips:\n• Simply press Home button to exit app\n• iOS will manage background apps automatically\n• Keep app in background to receive reminders'**
  String get reminderIOSInstructions;

  /// No description provided for @reminderAndroidInstructions.
  ///
  /// In en, this message translates to:
  /// **'If notifications don\'t work properly, check:\n• App is allowed to send notifications\n• Disable battery optimization/power saving for app\n• Allow app to run in background and auto-start\n• Android 12+ needs exact alarm permission\n\n📱 Xiaomi phone special settings:\n• Settings > App Management > Bee Accounting > Notification Management\n• Tap \"Recording Reminder\" channel\n• Set importance to \"Urgent\" or \"High\"\n• Enable \"Banner notifications\", \"Sound\", \"Vibration\"\n• Security Center > App Management > Permissions > Auto-start\n\n🔒 Lock background methods:\n• Find Bee Accounting in recent tasks\n• Pull down app card to show lock icon\n• Tap lock icon to prevent cleanup'**
  String get reminderAndroidInstructions;

  /// No description provided for @categoryDetailLoadFailed.
  ///
  /// In en, this message translates to:
  /// **'Load failed'**
  String get categoryDetailLoadFailed;

  /// No description provided for @categoryDetailSummaryTitle.
  ///
  /// In en, this message translates to:
  /// **'Category Summary'**
  String get categoryDetailSummaryTitle;

  /// No description provided for @categoryDetailTotalCount.
  ///
  /// In en, this message translates to:
  /// **'Total Count'**
  String get categoryDetailTotalCount;

  /// No description provided for @categoryDetailTotalAmount.
  ///
  /// In en, this message translates to:
  /// **'Total Amount'**
  String get categoryDetailTotalAmount;

  /// No description provided for @categoryDetailAverageAmount.
  ///
  /// In en, this message translates to:
  /// **'Average Amount'**
  String get categoryDetailAverageAmount;

  /// No description provided for @categoryDetailSortTitle.
  ///
  /// In en, this message translates to:
  /// **'Sort'**
  String get categoryDetailSortTitle;

  /// No description provided for @categoryDetailSortTimeDesc.
  ///
  /// In en, this message translates to:
  /// **'Time ↓'**
  String get categoryDetailSortTimeDesc;

  /// No description provided for @categoryDetailSortTimeAsc.
  ///
  /// In en, this message translates to:
  /// **'Time ↑'**
  String get categoryDetailSortTimeAsc;

  /// No description provided for @categoryDetailSortAmountDesc.
  ///
  /// In en, this message translates to:
  /// **'Amount ↓'**
  String get categoryDetailSortAmountDesc;

  /// No description provided for @categoryDetailSortAmountAsc.
  ///
  /// In en, this message translates to:
  /// **'Amount ↑'**
  String get categoryDetailSortAmountAsc;

  /// No description provided for @categoryDetailNoTransactions.
  ///
  /// In en, this message translates to:
  /// **'No transactions'**
  String get categoryDetailNoTransactions;

  /// No description provided for @categoryDetailNoTransactionsSubtext.
  ///
  /// In en, this message translates to:
  /// **'No transactions in this category yet'**
  String get categoryDetailNoTransactionsSubtext;

  /// No description provided for @categoryDetailDeleteFailed.
  ///
  /// In en, this message translates to:
  /// **'Delete failed'**
  String get categoryDetailDeleteFailed;

  /// No description provided for @categoryMigrationConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Confirm Migration'**
  String get categoryMigrationConfirmTitle;

  /// No description provided for @categoryMigrationConfirmMessage.
  ///
  /// In en, this message translates to:
  /// **'Migrate {count} transactions from \"{fromName}\" to \"{toName}\"?\n\nThis operation cannot be undone!'**
  String categoryMigrationConfirmMessage(Object count, Object fromName, Object toName);

  /// No description provided for @categoryMigrationConfirmOk.
  ///
  /// In en, this message translates to:
  /// **'Confirm Migration'**
  String get categoryMigrationConfirmOk;

  /// No description provided for @categoryMigrationCompleteTitle.
  ///
  /// In en, this message translates to:
  /// **'Migration Complete'**
  String get categoryMigrationCompleteTitle;

  /// No description provided for @categoryMigrationCompleteMessage.
  ///
  /// In en, this message translates to:
  /// **'Successfully migrated {count} transactions from \"{fromName}\" to \"{toName}\".'**
  String categoryMigrationCompleteMessage(Object count, Object fromName, Object toName);

  /// No description provided for @categoryMigrationFailedTitle.
  ///
  /// In en, this message translates to:
  /// **'Migration Failed'**
  String get categoryMigrationFailedTitle;

  /// No description provided for @categoryMigrationFailedMessage.
  ///
  /// In en, this message translates to:
  /// **'Migration error: {error}'**
  String categoryMigrationFailedMessage(Object error);

  /// No description provided for @categoryMigrationTransactionLabel.
  ///
  /// In en, this message translates to:
  /// **'{count} records'**
  String categoryMigrationTransactionLabel(int count);

  /// No description provided for @mineImportCompleteAllSuccess.
  ///
  /// In en, this message translates to:
  /// **'All Success'**
  String get mineImportCompleteAllSuccess;




















































  /// No description provided for @importSelectCsvFile.
  ///
  /// In en, this message translates to:
  /// **'Please select a file to import (CSV/TSV/XLSX supported)'**
  String get importSelectCsvFile;

  /// No description provided for @exportTitle.
  ///
  /// In en, this message translates to:
  /// **'Export'**
  String get exportTitle;

  /// No description provided for @exportDescription.
  ///
  /// In en, this message translates to:
  /// **'Supported export types:\n• Transactions (Income/Expense/Transfer)\n• Categories\n• Accounts\n\nClick the button below to select save location and export current ledger to CSV file.'**
  String get exportDescription;

  /// No description provided for @exportButtonIOS.
  ///
  /// In en, this message translates to:
  /// **'Export and Share'**
  String get exportButtonIOS;

  /// No description provided for @exportButtonAndroid.
  ///
  /// In en, this message translates to:
  /// **'Export Data'**
  String get exportButtonAndroid;

  /// No description provided for @exportSavedTo.
  ///
  /// In en, this message translates to:
  /// **'Saved to: {path}'**
  String exportSavedTo(String path);

  /// No description provided for @exportCsvHeaderType.
  ///
  /// In en, this message translates to:
  /// **'Type'**
  String get exportCsvHeaderType;

  /// No description provided for @exportCsvHeaderCategory.
  ///
  /// In en, this message translates to:
  /// **'Category'**
  String get exportCsvHeaderCategory;

  /// No description provided for @exportCsvHeaderSubCategory.
  ///
  /// In en, this message translates to:
  /// **'Subcategory'**
  String get exportCsvHeaderSubCategory;

  /// No description provided for @exportCsvHeaderAmount.
  ///
  /// In en, this message translates to:
  /// **'Amount'**
  String get exportCsvHeaderAmount;

  /// No description provided for @exportCsvHeaderAccount.
  ///
  /// In en, this message translates to:
  /// **'Account'**
  String get exportCsvHeaderAccount;

  /// No description provided for @exportCsvHeaderFromAccount.
  ///
  /// In en, this message translates to:
  /// **'From Account'**
  String get exportCsvHeaderFromAccount;

  /// No description provided for @exportCsvHeaderToAccount.
  ///
  /// In en, this message translates to:
  /// **'To Account'**
  String get exportCsvHeaderToAccount;

  /// No description provided for @exportCsvHeaderNote.
  ///
  /// In en, this message translates to:
  /// **'Note'**
  String get exportCsvHeaderNote;

  /// No description provided for @exportCsvHeaderTime.
  ///
  /// In en, this message translates to:
  /// **'Time'**
  String get exportCsvHeaderTime;

  /// No description provided for @exportCsvHeaderTags.
  ///
  /// In en, this message translates to:
  /// **'Tags'**
  String get exportCsvHeaderTags;

  /// No description provided for @exportCsvHeaderAttachments.
  ///
  /// In en, this message translates to:
  /// **'Attachments'**
  String get exportCsvHeaderAttachments;

  /// No description provided for @exportShareText.
  ///
  /// In en, this message translates to:
  /// **'BeeCount Export File'**
  String get exportShareText;

  /// No description provided for @exportSuccessTitle.
  ///
  /// In en, this message translates to:
  /// **'Export Successful'**
  String get exportSuccessTitle;

  /// No description provided for @exportSuccessMessageIOS.
  ///
  /// In en, this message translates to:
  /// **'Saved and available in share history:\n{path}'**
  String exportSuccessMessageIOS(String path);

  /// No description provided for @exportSuccessMessageAndroid.
  ///
  /// In en, this message translates to:
  /// **'Saved to:\n{path}'**
  String exportSuccessMessageAndroid(String path);

  /// No description provided for @exportFailedTitle.
  ///
  /// In en, this message translates to:
  /// **'Export Failed'**
  String get exportFailedTitle;

  /// No description provided for @exportTypeIncome.
  ///
  /// In en, this message translates to:
  /// **'Income'**
  String get exportTypeIncome;

  /// No description provided for @exportTypeExpense.
  ///
  /// In en, this message translates to:
  /// **'Expense'**
  String get exportTypeExpense;

  /// No description provided for @exportTypeTransfer.
  ///
  /// In en, this message translates to:
  /// **'Transfer'**
  String get exportTypeTransfer;

  /// No description provided for @personalizeThemeHoney.
  ///
  /// In en, this message translates to:
  /// **'Bee Yellow'**
  String get personalizeThemeHoney;

  /// No description provided for @personalizeThemeOrange.
  ///
  /// In en, this message translates to:
  /// **'Flame Orange'**
  String get personalizeThemeOrange;

  /// No description provided for @personalizeThemeGreen.
  ///
  /// In en, this message translates to:
  /// **'Emerald Green'**
  String get personalizeThemeGreen;

  /// No description provided for @personalizeThemePurple.
  ///
  /// In en, this message translates to:
  /// **'Purple Lotus'**
  String get personalizeThemePurple;

  /// No description provided for @personalizeThemePink.
  ///
  /// In en, this message translates to:
  /// **'Cherry Pink'**
  String get personalizeThemePink;

  /// No description provided for @personalizeThemeBlue.
  ///
  /// In en, this message translates to:
  /// **'Sky Blue'**
  String get personalizeThemeBlue;

  /// No description provided for @personalizeThemeMint.
  ///
  /// In en, this message translates to:
  /// **'Forest Moon'**
  String get personalizeThemeMint;

  /// No description provided for @personalizeThemeSand.
  ///
  /// In en, this message translates to:
  /// **'Sunset Dune'**
  String get personalizeThemeSand;

  /// No description provided for @personalizeThemeLavender.
  ///
  /// In en, this message translates to:
  /// **'Snow & Pine'**
  String get personalizeThemeLavender;

  /// No description provided for @personalizeThemeSky.
  ///
  /// In en, this message translates to:
  /// **'Misty Wonderland'**
  String get personalizeThemeSky;

  /// No description provided for @personalizeThemeWarmOrange.
  ///
  /// In en, this message translates to:
  /// **'Warm Orange'**
  String get personalizeThemeWarmOrange;

  /// No description provided for @personalizeThemeMintGreen.
  ///
  /// In en, this message translates to:
  /// **'Mint Green'**
  String get personalizeThemeMintGreen;

  /// No description provided for @personalizeThemeRoseGold.
  ///
  /// In en, this message translates to:
  /// **'Rose Gold'**
  String get personalizeThemeRoseGold;

  /// No description provided for @personalizeThemeDeepBlue.
  ///
  /// In en, this message translates to:
  /// **'Deep Blue'**
  String get personalizeThemeDeepBlue;

  /// No description provided for @personalizeThemeMapleRed.
  ///
  /// In en, this message translates to:
  /// **'Maple Red'**
  String get personalizeThemeMapleRed;

  /// No description provided for @personalizeThemeEmerald.
  ///
  /// In en, this message translates to:
  /// **'Emerald'**
  String get personalizeThemeEmerald;

  /// No description provided for @personalizeThemeLavenderPurple.
  ///
  /// In en, this message translates to:
  /// **'Lavender'**
  String get personalizeThemeLavenderPurple;

  /// No description provided for @personalizeThemeAmber.
  ///
  /// In en, this message translates to:
  /// **'Amber'**
  String get personalizeThemeAmber;

  /// No description provided for @personalizeThemeRouge.
  ///
  /// In en, this message translates to:
  /// **'Rouge Red'**
  String get personalizeThemeRouge;

  /// No description provided for @personalizeThemeIndigo.
  ///
  /// In en, this message translates to:
  /// **'Indigo Blue'**
  String get personalizeThemeIndigo;

  /// No description provided for @personalizeThemeOlive.
  ///
  /// In en, this message translates to:
  /// **'Olive Green'**
  String get personalizeThemeOlive;

  /// No description provided for @personalizeThemeCoral.
  ///
  /// In en, this message translates to:
  /// **'Coral Pink'**
  String get personalizeThemeCoral;

  /// No description provided for @personalizeThemeDarkGreen.
  ///
  /// In en, this message translates to:
  /// **'Dark Green'**
  String get personalizeThemeDarkGreen;

  /// No description provided for @personalizeThemeViolet.
  ///
  /// In en, this message translates to:
  /// **'Violet'**
  String get personalizeThemeViolet;

  /// No description provided for @personalizeThemeSunset.
  ///
  /// In en, this message translates to:
  /// **'Sunset Orange'**
  String get personalizeThemeSunset;

  /// No description provided for @personalizeThemePeacock.
  ///
  /// In en, this message translates to:
  /// **'Peacock Blue'**
  String get personalizeThemePeacock;

  /// No description provided for @personalizeThemeLime.
  ///
  /// In en, this message translates to:
  /// **'Lime Green'**
  String get personalizeThemeLime;

  /// No description provided for @analyticsMonthlyAvg.
  ///
  /// In en, this message translates to:
  /// **'Monthly Avg'**
  String get analyticsMonthlyAvg;

  /// No description provided for @analyticsDailyAvg.
  ///
  /// In en, this message translates to:
  /// **'Daily Avg'**
  String get analyticsDailyAvg;

  /// No description provided for @analyticsOverallAvg.
  ///
  /// In en, this message translates to:
  /// **'Overall Avg'**
  String get analyticsOverallAvg;

  /// No description provided for @analyticsTotalIncome.
  ///
  /// In en, this message translates to:
  /// **'Total Income: '**
  String get analyticsTotalIncome;

  /// No description provided for @analyticsTotalExpense.
  ///
  /// In en, this message translates to:
  /// **'Total Expense: '**
  String get analyticsTotalExpense;

  /// No description provided for @analyticsBalance.
  ///
  /// In en, this message translates to:
  /// **'Balance: '**
  String get analyticsBalance;

  /// No description provided for @analyticsAvgIncome.
  ///
  /// In en, this message translates to:
  /// **'{avgLabel} Income: '**
  String analyticsAvgIncome(String avgLabel);

  /// No description provided for @analyticsAvgExpense.
  ///
  /// In en, this message translates to:
  /// **'{avgLabel} Expense: '**
  String analyticsAvgExpense(String avgLabel);

  /// No description provided for @analyticsExpense.
  ///
  /// In en, this message translates to:
  /// **'Expense'**
  String get analyticsExpense;

  /// No description provided for @analyticsIncome.
  ///
  /// In en, this message translates to:
  /// **'Income'**
  String get analyticsIncome;

  /// No description provided for @analyticsTotal.
  ///
  /// In en, this message translates to:
  /// **'Total {type}: '**
  String analyticsTotal(String type);

  /// No description provided for @analyticsAverage.
  ///
  /// In en, this message translates to:
  /// **'{avgLabel}: '**
  String analyticsAverage(String avgLabel);














































































  /// No description provided for @currencyCNY.
  ///
  /// In en, this message translates to:
  /// **'Chinese Yuan'**
  String get currencyCNY;

  /// No description provided for @currencyUSD.
  ///
  /// In en, this message translates to:
  /// **'US Dollar'**
  String get currencyUSD;

  /// No description provided for @currencyEUR.
  ///
  /// In en, this message translates to:
  /// **'Euro'**
  String get currencyEUR;

  /// No description provided for @currencyJPY.
  ///
  /// In en, this message translates to:
  /// **'Japanese Yen'**
  String get currencyJPY;

  /// No description provided for @currencyHKD.
  ///
  /// In en, this message translates to:
  /// **'Hong Kong Dollar'**
  String get currencyHKD;

  /// No description provided for @currencyTWD.
  ///
  /// In en, this message translates to:
  /// **'New Taiwan Dollar'**
  String get currencyTWD;

  /// No description provided for @currencyGBP.
  ///
  /// In en, this message translates to:
  /// **'British Pound'**
  String get currencyGBP;

  /// No description provided for @currencyAUD.
  ///
  /// In en, this message translates to:
  /// **'Australian Dollar'**
  String get currencyAUD;

  /// No description provided for @currencyCAD.
  ///
  /// In en, this message translates to:
  /// **'Canadian Dollar'**
  String get currencyCAD;

  /// No description provided for @currencyKRW.
  ///
  /// In en, this message translates to:
  /// **'South Korean Won'**
  String get currencyKRW;

  /// No description provided for @currencySGD.
  ///
  /// In en, this message translates to:
  /// **'Singapore Dollar'**
  String get currencySGD;

  /// No description provided for @currencyMYR.
  ///
  /// In en, this message translates to:
  /// **'Malaysian Ringgit'**
  String get currencyMYR;

  /// No description provided for @currencyTHB.
  ///
  /// In en, this message translates to:
  /// **'Thai Baht'**
  String get currencyTHB;

  /// No description provided for @currencyIDR.
  ///
  /// In en, this message translates to:
  /// **'Indonesian Rupiah'**
  String get currencyIDR;

  /// No description provided for @currencyPHP.
  ///
  /// In en, this message translates to:
  /// **'Philippine Peso'**
  String get currencyPHP;

  /// No description provided for @currencyVND.
  ///
  /// In en, this message translates to:
  /// **'Vietnamese Dong'**
  String get currencyVND;

  /// No description provided for @currencyINR.
  ///
  /// In en, this message translates to:
  /// **'Indian Rupee'**
  String get currencyINR;

  /// No description provided for @currencyRUB.
  ///
  /// In en, this message translates to:
  /// **'Russian Ruble'**
  String get currencyRUB;

  /// No description provided for @currencyBYN.
  ///
  /// In en, this message translates to:
  /// **'Belarusian Ruble'**
  String get currencyBYN;

  /// No description provided for @currencyNZD.
  ///
  /// In en, this message translates to:
  /// **'New Zealand Dollar'**
  String get currencyNZD;

  /// No description provided for @currencyCHF.
  ///
  /// In en, this message translates to:
  /// **'Swiss Franc'**
  String get currencyCHF;

  /// No description provided for @currencySEK.
  ///
  /// In en, this message translates to:
  /// **'Swedish Krona'**
  String get currencySEK;

  /// No description provided for @currencyNOK.
  ///
  /// In en, this message translates to:
  /// **'Norwegian Krone'**
  String get currencyNOK;

  /// No description provided for @currencyDKK.
  ///
  /// In en, this message translates to:
  /// **'Danish Krone'**
  String get currencyDKK;

  /// No description provided for @currencyBRL.
  ///
  /// In en, this message translates to:
  /// **'Brazilian Real'**
  String get currencyBRL;

  /// No description provided for @currencyMXN.
  ///
  /// In en, this message translates to:
  /// **'Mexican Peso'**
  String get currencyMXN;

  /// No description provided for @currencyTRY.
  ///
  /// In en, this message translates to:
  /// **'Turkish Lira'**
  String get currencyTRY;

  /// No description provided for @currencyZAR.
  ///
  /// In en, this message translates to:
  /// **'South African Rand'**
  String get currencyZAR;

  /// No description provided for @currencyAED.
  ///
  /// In en, this message translates to:
  /// **'UAE Dirham'**
  String get currencyAED;

  /// No description provided for @currencySAR.
  ///
  /// In en, this message translates to:
  /// **'Saudi Riyal'**
  String get currencySAR;

  /// No description provided for @currencyPLN.
  ///
  /// In en, this message translates to:
  /// **'Polish Zloty'**
  String get currencyPLN;

  /// No description provided for @currencyCZK.
  ///
  /// In en, this message translates to:
  /// **'Czech Koruna'**
  String get currencyCZK;

  /// No description provided for @currencyHUF.
  ///
  /// In en, this message translates to:
  /// **'Hungarian Forint'**
  String get currencyHUF;

  /// No description provided for @currencyARS.
  ///
  /// In en, this message translates to:
  /// **'Argentine Peso'**
  String get currencyARS;

  /// No description provided for @currencyCLP.
  ///
  /// In en, this message translates to:
  /// **'Chilean Peso'**
  String get currencyCLP;

  /// No description provided for @currencyCOP.
  ///
  /// In en, this message translates to:
  /// **'Colombian Peso'**
  String get currencyCOP;

  /// No description provided for @currencyPEN.
  ///
  /// In en, this message translates to:
  /// **'Peruvian Sol'**
  String get currencyPEN;

  /// No description provided for @currencyEGP.
  ///
  /// In en, this message translates to:
  /// **'Egyptian Pound'**
  String get currencyEGP;

  /// No description provided for @currencyNGN.
  ///
  /// In en, this message translates to:
  /// **'Nigerian Naira'**
  String get currencyNGN;

  /// No description provided for @currencyKZT.
  ///
  /// In en, this message translates to:
  /// **'Kazakhstani Tenge'**
  String get currencyKZT;

  /// No description provided for @currencyUAH.
  ///
  /// In en, this message translates to:
  /// **'Ukrainian Hryvnia'**
  String get currencyUAH;

  /// No description provided for @currencyILS.
  ///
  /// In en, this message translates to:
  /// **'Israeli New Shekel'**
  String get currencyILS;

  /// No description provided for @currencyPKR.
  ///
  /// In en, this message translates to:
  /// **'Pakistani Rupee'**
  String get currencyPKR;

  /// No description provided for @currencyBDT.
  ///
  /// In en, this message translates to:
  /// **'Bangladeshi Taka'**
  String get currencyBDT;

  /// No description provided for @currencyLKR.
  ///
  /// In en, this message translates to:
  /// **'Sri Lankan Rupee'**
  String get currencyLKR;

  /// No description provided for @currencyMMK.
  ///
  /// In en, this message translates to:
  /// **'Myanmar Kyat'**
  String get currencyMMK;



  /// No description provided for @recurringTransactionTitle.
  ///
  /// In en, this message translates to:
  /// **'Recurring Bills'**
  String get recurringTransactionTitle;

  /// No description provided for @recurringTransactionAdd.
  ///
  /// In en, this message translates to:
  /// **'Add Recurring Bill'**
  String get recurringTransactionAdd;

  /// No description provided for @recurringTransactionEdit.
  ///
  /// In en, this message translates to:
  /// **'Edit Recurring Bill'**
  String get recurringTransactionEdit;

  /// No description provided for @recurringTransactionFrequency.
  ///
  /// In en, this message translates to:
  /// **'Frequency'**
  String get recurringTransactionFrequency;

  /// No description provided for @recurringTransactionDaily.
  ///
  /// In en, this message translates to:
  /// **'Daily'**
  String get recurringTransactionDaily;

  /// No description provided for @recurringTransactionWeekly.
  ///
  /// In en, this message translates to:
  /// **'Weekly'**
  String get recurringTransactionWeekly;

  /// No description provided for @recurringTransactionMonthly.
  ///
  /// In en, this message translates to:
  /// **'Monthly'**
  String get recurringTransactionMonthly;

  /// No description provided for @recurringTransactionYearly.
  ///
  /// In en, this message translates to:
  /// **'Yearly'**
  String get recurringTransactionYearly;

  /// No description provided for @recurringTransactionInterval.
  ///
  /// In en, this message translates to:
  /// **'Interval'**
  String get recurringTransactionInterval;

  /// No description provided for @recurringTransactionDayOfMonth.
  ///
  /// In en, this message translates to:
  /// **'Day of Month'**
  String get recurringTransactionDayOfMonth;

  /// No description provided for @recurringTransactionStartDate.
  ///
  /// In en, this message translates to:
  /// **'Start Date'**
  String get recurringTransactionStartDate;

  /// No description provided for @recurringTransactionEndDate.
  ///
  /// In en, this message translates to:
  /// **'End Date'**
  String get recurringTransactionEndDate;

  /// No description provided for @recurringTransactionNoEndDate.
  ///
  /// In en, this message translates to:
  /// **'Perpetual'**
  String get recurringTransactionNoEndDate;

  /// No description provided for @recurringTransactionDeleteConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this recurring bill?'**
  String get recurringTransactionDeleteConfirm;

  /// No description provided for @recurringTransactionEmpty.
  ///
  /// In en, this message translates to:
  /// **'No Recurring Bills'**
  String get recurringTransactionEmpty;

  /// No description provided for @recurringTransactionEmptyHint.
  ///
  /// In en, this message translates to:
  /// **'Tap the + button in the top right corner to add'**
  String get recurringTransactionEmptyHint;

  /// No description provided for @recurringTransactionEveryNDays.
  ///
  /// In en, this message translates to:
  /// **'Every {n} day(s)'**
  String recurringTransactionEveryNDays(int n);

  /// No description provided for @recurringTransactionEveryNWeeks.
  ///
  /// In en, this message translates to:
  /// **'Every {n} week(s)'**
  String recurringTransactionEveryNWeeks(int n);

  /// No description provided for @recurringTransactionEveryNMonths.
  ///
  /// In en, this message translates to:
  /// **'Every {n} month(s)'**
  String recurringTransactionEveryNMonths(int n);

  /// No description provided for @recurringTransactionEveryNYears.
  ///
  /// In en, this message translates to:
  /// **'Every {n} year(s)'**
  String recurringTransactionEveryNYears(int n);

  /// No description provided for @recurringTransactionUsageTitle.
  ///
  /// In en, this message translates to:
  /// **'Usage Guide'**
  String get recurringTransactionUsageTitle;

  /// No description provided for @recurringTransactionUsageContent.
  ///
  /// In en, this message translates to:
  /// **'Recurring transactions are automatically scanned and generated when the app cold starts. After setting a date, the system will create corresponding bills on the first startup after that date. For example: if set to Nov 27, bills will be auto-recorded on the first launch after Nov 27.'**
  String get recurringTransactionUsageContent;

  /// No description provided for @ledgerSelectTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Ledger'**
  String get ledgerSelectTitle;

  /// No description provided for @ledgerSelect.
  ///
  /// In en, this message translates to:
  /// **'Select Ledger'**
  String get ledgerSelect;

















































































































































  /// No description provided for @accountsTitle.
  ///
  /// In en, this message translates to:
  /// **'Asset Management'**
  String get accountsTitle;

  /// No description provided for @accountsEmptyMessage.
  ///
  /// In en, this message translates to:
  /// **'No accounts yet, tap the top right to add'**
  String get accountsEmptyMessage;

  /// No description provided for @accountAddTooltip.
  ///
  /// In en, this message translates to:
  /// **'Add Account'**
  String get accountAddTooltip;

  /// No description provided for @accountAddButton.
  ///
  /// In en, this message translates to:
  /// **'Add Account'**
  String get accountAddButton;

  /// No description provided for @accountBalance.
  ///
  /// In en, this message translates to:
  /// **'Balance'**
  String get accountBalance;

  /// No description provided for @accountUpdateBalance.
  ///
  /// In en, this message translates to:
  /// **'Update Balance'**
  String get accountUpdateBalance;

  /// No description provided for @accountBalanceAdjustmentTitle.
  ///
  /// In en, this message translates to:
  /// **'Confirm balance change'**
  String get accountBalanceAdjustmentTitle;

  /// No description provided for @accountBalanceAdjustmentChooseAction.
  ///
  /// In en, this message translates to:
  /// **'Choose how to save this balance change'**
  String get accountBalanceAdjustmentChooseAction;

  /// No description provided for @accountBalanceAdjustmentOnly.
  ///
  /// In en, this message translates to:
  /// **'Balance only'**
  String get accountBalanceAdjustmentOnly;

  /// No description provided for @accountBalanceAdjustmentOnlyMessage.
  ///
  /// In en, this message translates to:
  /// **'No transaction will be created. Historical balance trends will use the new balance as their baseline.'**
  String get accountBalanceAdjustmentOnlyMessage;

  /// No description provided for @accountBalanceAdjustmentCreate.
  ///
  /// In en, this message translates to:
  /// **'Create transaction'**
  String get accountBalanceAdjustmentCreate;

  /// No description provided for @accountBalanceAdjustmentCreateMessage.
  ///
  /// In en, this message translates to:
  /// **'Also add a regular income or expense under the Balance category, included in income, expense, and budget statistics.'**
  String get accountBalanceAdjustmentCreateMessage;

  /// No description provided for @accountBalanceAdjustmentConfirmOnly.
  ///
  /// In en, this message translates to:
  /// **'Confirm balance only'**
  String get accountBalanceAdjustmentConfirmOnly;

  /// No description provided for @accountBalanceAdjustmentConfirmCreate.
  ///
  /// In en, this message translates to:
  /// **'Confirm and create transaction'**
  String get accountBalanceAdjustmentConfirmCreate;

  /// No description provided for @accountBalanceCurrent.
  ///
  /// In en, this message translates to:
  /// **'Current balance'**
  String get accountBalanceCurrent;

  /// No description provided for @accountBalanceAfterAdjustment.
  ///
  /// In en, this message translates to:
  /// **'Adjusted balance'**
  String get accountBalanceAfterAdjustment;

  /// No description provided for @accountBalanceDifference.
  ///
  /// In en, this message translates to:
  /// **'Difference'**
  String get accountBalanceDifference;

  /// No description provided for @accountBalanceUnchanged.
  ///
  /// In en, this message translates to:
  /// **'The balance has not changed'**
  String get accountBalanceUnchanged;

  /// No description provided for @accountEditTitle.
  ///
  /// In en, this message translates to:
  /// **'Edit Account'**
  String get accountEditTitle;

  /// No description provided for @accountNewTitle.
  ///
  /// In en, this message translates to:
  /// **'New Account'**
  String get accountNewTitle;

  /// No description provided for @accountNameLabel.
  ///
  /// In en, this message translates to:
  /// **'Account Name'**
  String get accountNameLabel;

  /// No description provided for @accountNameHint.
  ///
  /// In en, this message translates to:
  /// **'e.g.: ICBC, Alipay, etc.'**
  String get accountNameHint;

  /// No description provided for @accountNameRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter account name'**
  String get accountNameRequired;

  /// No description provided for @accountNameDuplicate.
  ///
  /// In en, this message translates to:
  /// **'Account name already exists, please use a different name'**
  String get accountNameDuplicate;

  /// No description provided for @accountTypeLabel.
  ///
  /// In en, this message translates to:
  /// **'Account Type'**
  String get accountTypeLabel;

  /// No description provided for @accountTypeCash.
  ///
  /// In en, this message translates to:
  /// **'Cash'**
  String get accountTypeCash;

  /// No description provided for @accountTypeBankCard.
  ///
  /// In en, this message translates to:
  /// **'Bank Card'**
  String get accountTypeBankCard;

  /// No description provided for @accountTypeCreditCard.
  ///
  /// In en, this message translates to:
  /// **'Credit Card'**
  String get accountTypeCreditCard;

  /// No description provided for @accountTypeAlipay.
  ///
  /// In en, this message translates to:
  /// **'Alipay'**
  String get accountTypeAlipay;

  /// No description provided for @accountTypeWechat.
  ///
  /// In en, this message translates to:
  /// **'WeChat'**
  String get accountTypeWechat;

  /// No description provided for @accountTypeOther.
  ///
  /// In en, this message translates to:
  /// **'Other'**
  String get accountTypeOther;

  /// No description provided for @accountInitialBalance.
  ///
  /// In en, this message translates to:
  /// **'Initial Balance'**
  String get accountInitialBalance;

  /// No description provided for @accountInitialBalanceHint.
  ///
  /// In en, this message translates to:
  /// **'Enter initial balance (optional)'**
  String get accountInitialBalanceHint;

  /// No description provided for @accountDeleteWarningTitle.
  ///
  /// In en, this message translates to:
  /// **'Confirm Delete'**
  String get accountDeleteWarningTitle;

  /// No description provided for @accountDeleteWarningMessage.
  ///
  /// In en, this message translates to:
  /// **'This account has {count} related transactions. After deletion, account information in transaction records will be cleared. Confirm deletion?'**
  String accountDeleteWarningMessage(int count);

  /// No description provided for @accountDeleteConfirm.
  ///
  /// In en, this message translates to:
  /// **'Confirm to delete this account?'**
  String get accountDeleteConfirm;

  /// No description provided for @accountSelectTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Account'**
  String get accountSelectTitle;

  /// No description provided for @accountNone.
  ///
  /// In en, this message translates to:
  /// **'No Account'**
  String get accountNone;

  /// No description provided for @accountsEnableFeature.
  ///
  /// In en, this message translates to:
  /// **'Enable Account Feature'**
  String get accountsEnableFeature;

  /// No description provided for @accountHide.
  ///
  /// In en, this message translates to:
  /// **'Hide account'**
  String get accountHide;

  /// No description provided for @accountUnhide.
  ///
  /// In en, this message translates to:
  /// **'Restore account'**
  String get accountUnhide;

  /// No description provided for @accountRestore.
  ///
  /// In en, this message translates to:
  /// **'Restore'**
  String get accountRestore;

  /// No description provided for @accountHiddenTag.
  ///
  /// In en, this message translates to:
  /// **'Hidden'**
  String get accountHiddenTag;

  /// No description provided for @accountHiddenSection.
  ///
  /// In en, this message translates to:
  /// **'Hidden'**
  String get accountHiddenSection;

  /// No description provided for @accountHiddenSectionSummary.
  ///
  /// In en, this message translates to:
  /// **'{count} hidden · {total}'**
  String accountHiddenSectionSummary(int count, String total);

  /// No description provided for @accountHideConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Hide this account?'**
  String get accountHideConfirmTitle;

  /// No description provided for @accountHideConfirmBody.
  ///
  /// In en, this message translates to:
  /// **'It won\'t be selectable for new records; history and balance are kept and you can restore it anytime.'**
  String get accountHideConfirmBody;

  /// No description provided for @accountHideRecurringWarn.
  ///
  /// In en, this message translates to:
  /// **'{count} recurring bills use this account; they\'ll be skipped while hidden.'**
  String accountHideRecurringWarn(int count);

  /// No description provided for @accountHideClearedDefault.
  ///
  /// In en, this message translates to:
  /// **'Cleared its default-account setting'**
  String get accountHideClearedDefault;

  /// No description provided for @accountHiddenToast.
  ///
  /// In en, this message translates to:
  /// **'Hidden'**
  String get accountHiddenToast;

  /// No description provided for @accountRestoredToast.
  ///
  /// In en, this message translates to:
  /// **'Restored'**
  String get accountRestoredToast;

  /// No description provided for @privacyOpenSourceUrlError.
  ///
  /// In en, this message translates to:
  /// **'Cannot open link'**
  String get privacyOpenSourceUrlError;



  /// No description provided for @welcomeTitle.
  ///
  /// In en, this message translates to:
  /// **'Welcome to BeeCount'**
  String get welcomeTitle;

  /// No description provided for @welcomeDescription.
  ///
  /// In en, this message translates to:
  /// **'An accounting app that truly respects your privacy'**
  String get welcomeDescription;

  /// No description provided for @welcomeCurrencyDescription.
  ///
  /// In en, this message translates to:
  /// **'Choose your preferred currency, you can change it anytime in settings'**
  String get welcomeCurrencyDescription;

  /// No description provided for @welcomeCreateDefaultLedger.
  ///
  /// In en, this message translates to:
  /// **'Create a default ledger'**
  String get welcomeCreateDefaultLedger;

  /// No description provided for @welcomePrivacyTitle.
  ///
  /// In en, this message translates to:
  /// **'Open Source · Community Driven'**
  String get welcomePrivacyTitle;

  /// No description provided for @welcomePrivacyFeature1.
  ///
  /// In en, this message translates to:
  /// **'100% open source code, supervised by community'**
  String get welcomePrivacyFeature1;

  /// No description provided for @welcomePrivacyFeature2.
  ///
  /// In en, this message translates to:
  /// **'No privacy concerns, data stored locally'**
  String get welcomePrivacyFeature2;

  /// No description provided for @welcomeOpenSourceFeature1.
  ///
  /// In en, this message translates to:
  /// **'Active developer community, continuous improvement'**
  String get welcomeOpenSourceFeature1;

  /// No description provided for @welcomeViewGitHub.
  ///
  /// In en, this message translates to:
  /// **'Visit GitHub Repository'**
  String get welcomeViewGitHub;






  /// No description provided for @widgetManagement.
  ///
  /// In en, this message translates to:
  /// **'Home Screen Widget'**
  String get widgetManagement;

  /// No description provided for @widgetManagementDesc.
  ///
  /// In en, this message translates to:
  /// **'Quick view of income and expenses on home screen'**
  String get widgetManagementDesc;

  /// No description provided for @widgetPreview.
  ///
  /// In en, this message translates to:
  /// **'Widget Preview'**
  String get widgetPreview;

  /// No description provided for @widgetPreviewDesc.
  ///
  /// In en, this message translates to:
  /// **'Widget automatically displays actual data from current ledger, theme color follows app settings'**
  String get widgetPreviewDesc;

  /// No description provided for @widgetGalleryTitle.
  ///
  /// In en, this message translates to:
  /// **'Widget Gallery'**
  String get widgetGalleryTitle;

  /// No description provided for @widgetGalleryDesc.
  ///
  /// In en, this message translates to:
  /// **'Previews use sample data — the real widget shows your current ledger and follows your theme color.'**
  String get widgetGalleryDesc;

  /// No description provided for @widgetGalleryGlanceTitle.
  ///
  /// In en, this message translates to:
  /// **'Overview'**
  String get widgetGalleryGlanceTitle;

  /// No description provided for @widgetGalleryGlanceDesc.
  ///
  /// In en, this message translates to:
  /// **'Today\'s and this month\'s income and expenses at a glance'**
  String get widgetGalleryGlanceDesc;

  /// No description provided for @widgetGalleryNetWorthDesc.
  ///
  /// In en, this message translates to:
  /// **'Total assets, liabilities and net worth trend'**
  String get widgetGalleryNetWorthDesc;

  /// No description provided for @widgetGalleryQuickAddTitle.
  ///
  /// In en, this message translates to:
  /// **'Quick Add'**
  String get widgetGalleryQuickAddTitle;

  /// No description provided for @widgetGalleryQuickAddDesc.
  ///
  /// In en, this message translates to:
  /// **'One-tap entry for your frequent categories'**
  String get widgetGalleryQuickAddDesc;

  /// No description provided for @widgetGalleryBudgetDesc.
  ///
  /// In en, this message translates to:
  /// **'Track your budget progress at a glance'**
  String get widgetGalleryBudgetDesc;

  /// No description provided for @widgetGalleryRecentDesc.
  ///
  /// In en, this message translates to:
  /// **'See your latest transactions'**
  String get widgetGalleryRecentDesc;

  /// No description provided for @widgetGalleryDashboardTitle.
  ///
  /// In en, this message translates to:
  /// **'Dashboard'**
  String get widgetGalleryDashboardTitle;

  /// No description provided for @widgetDashboardTitle.
  ///
  /// In en, this message translates to:
  /// **'This Month'**
  String get widgetDashboardTitle;

  /// No description provided for @widgetGalleryDashboardDesc.
  ///
  /// In en, this message translates to:
  /// **'Income, trend and recent transactions in one view'**
  String get widgetGalleryDashboardDesc;

  /// No description provided for @widgetGalleryConsumptionRhythmDesc.
  ///
  /// In en, this message translates to:
  /// **'See your spending pace across the last 30 days'**
  String get widgetGalleryConsumptionRhythmDesc;

  /// No description provided for @widgetGalleryBeeTrailDesc.
  ///
  /// In en, this message translates to:
  /// **'Build a gentle daily recording habit'**
  String get widgetGalleryBeeTrailDesc;

  /// No description provided for @widgetSizeSmall.
  ///
  /// In en, this message translates to:
  /// **'Small'**
  String get widgetSizeSmall;

  /// No description provided for @widgetSizeMedium.
  ///
  /// In en, this message translates to:
  /// **'Medium'**
  String get widgetSizeMedium;

  /// No description provided for @widgetSizeLarge.
  ///
  /// In en, this message translates to:
  /// **'Large'**
  String get widgetSizeLarge;

  /// No description provided for @howToAddWidget.
  ///
  /// In en, this message translates to:
  /// **'How to Add Widget'**
  String get howToAddWidget;

  /// No description provided for @iosWidgetStep1.
  ///
  /// In en, this message translates to:
  /// **'Long press on home screen blank area to enter edit mode'**
  String get iosWidgetStep1;

  /// No description provided for @iosWidgetStep2.
  ///
  /// In en, this message translates to:
  /// **'Tap the \"+\" button in upper left corner'**
  String get iosWidgetStep2;

  /// No description provided for @iosWidgetStep3.
  ///
  /// In en, this message translates to:
  /// **'Search and select \"BeeCount\"'**
  String get iosWidgetStep3;

  /// No description provided for @iosWidgetStep4.
  ///
  /// In en, this message translates to:
  /// **'Select medium widget and add to home screen'**
  String get iosWidgetStep4;

  /// No description provided for @androidWidgetStep1.
  ///
  /// In en, this message translates to:
  /// **'Long press on home screen blank area'**
  String get androidWidgetStep1;

  /// No description provided for @androidWidgetStep2.
  ///
  /// In en, this message translates to:
  /// **'Select \"Widgets\"'**
  String get androidWidgetStep2;

  /// No description provided for @androidWidgetStep3.
  ///
  /// In en, this message translates to:
  /// **'Find and long press \"BeeCount\" widget'**
  String get androidWidgetStep3;

  /// No description provided for @androidWidgetStep4.
  ///
  /// In en, this message translates to:
  /// **'Drag to suitable position on home screen'**
  String get androidWidgetStep4;

  /// No description provided for @aboutWidget.
  ///
  /// In en, this message translates to:
  /// **'About Widget'**
  String get aboutWidget;

  /// No description provided for @widgetDescription.
  ///
  /// In en, this message translates to:
  /// **'Widget automatically syncs to display today\'s and this month\'s income and expense data, refreshing every 30 minutes. Data updates immediately when app is opened.'**
  String get widgetDescription;

  /// No description provided for @widgetQuickEntryTitle.
  ///
  /// In en, this message translates to:
  /// **'Quick Entry'**
  String get widgetQuickEntryTitle;

  /// No description provided for @widgetQuickEntryDesc.
  ///
  /// In en, this message translates to:
  /// **'Tap the left side of the widget to quickly add an expense, or tap the right side to add an income. You can also use beecount://new?type=transfer via Shortcuts to quickly start a transfer.'**
  String get widgetQuickEntryDesc;

  /// No description provided for @appName.
  ///
  /// In en, this message translates to:
  /// **'BeeCount'**
  String get appName;

  /// No description provided for @monthSuffix.
  ///
  /// In en, this message translates to:
  /// **''**
  String get monthSuffix;

  /// No description provided for @todayExpense.
  ///
  /// In en, this message translates to:
  /// **'Today\'s Expense'**
  String get todayExpense;

  /// No description provided for @todayIncome.
  ///
  /// In en, this message translates to:
  /// **'Today\'s Income'**
  String get todayIncome;

  /// No description provided for @monthExpense.
  ///
  /// In en, this message translates to:
  /// **'Month\'s Expense'**
  String get monthExpense;

  /// No description provided for @monthIncome.
  ///
  /// In en, this message translates to:
  /// **'Month\'s Income'**
  String get monthIncome;




  /// No description provided for @featureDescription.
  ///
  /// In en, this message translates to:
  /// **'Feature Description'**
  String get featureDescription;

  /// No description provided for @featureDescriptionContent.
  ///
  /// In en, this message translates to:
  /// **'After taking a screenshot of payment page, the system will automatically recognize amount and merchant info, and create expense record.\n\n⚡ Recognition speed: 2-3 seconds (may be longer on some devices)\n🤖 Smart category matching\n📝 Auto-fill notes\n\n⚠️ Note:\n• Different devices have different screenshot save speeds, delay may be 5-10 seconds\n• May not work on some devices, depending on system implementation\n• Recognized screenshots will be skipped automatically\n• Due to Android Scoped Storage restrictions (Android 10+), apps cannot delete system screenshots. Manual cleanup required'**
  String get featureDescriptionContent;


  /// No description provided for @enabled.
  ///
  /// In en, this message translates to:
  /// **'Enabled'**
  String get enabled;

  /// No description provided for @disabled.
  ///
  /// In en, this message translates to:
  /// **'Disabled'**
  String get disabled;

  /// No description provided for @photosPermissionRequired.
  ///
  /// In en, this message translates to:
  /// **'Photos permission required for screenshot monitoring'**
  String get photosPermissionRequired;

  /// No description provided for @enableSuccess.
  ///
  /// In en, this message translates to:
  /// **'Auto billing enabled'**
  String get enableSuccess;

  /// No description provided for @disableSuccess.
  ///
  /// In en, this message translates to:
  /// **'Auto billing disabled'**
  String get disableSuccess;






  /// No description provided for @enableFailed.
  ///
  /// In en, this message translates to:
  /// **'Enable failed'**
  String get enableFailed;

  /// No description provided for @disableFailed.
  ///
  /// In en, this message translates to:
  /// **'Disable failed'**
  String get disableFailed;

  /// No description provided for @iosAutoFeatureDesc.
  ///
  /// In en, this message translates to:
  /// **'Use iOS \"Shortcuts\" app to automatically identify payment information from screenshots and create transactions. Once set up, it will automatically trigger on every screenshot.'**
  String get iosAutoFeatureDesc;

  /// No description provided for @iosAutoShortcutConfigTitle.
  ///
  /// In en, this message translates to:
  /// **'Configuration Steps:'**
  String get iosAutoShortcutConfigTitle;

  /// No description provided for @iosAutoShortcutStep1.
  ///
  /// In en, this message translates to:
  /// **'Open \"Shortcuts\" app, tap \"+\" in top right to create new shortcut'**
  String get iosAutoShortcutStep1;

  /// No description provided for @iosAutoShortcutStep2.
  ///
  /// In en, this message translates to:
  /// **'Add \"Take Screenshot\" action'**
  String get iosAutoShortcutStep2;

  /// No description provided for @iosAutoShortcutStep3.
  ///
  /// In en, this message translates to:
  /// **'Search and add \"BeeCount - Auto Billing\" action'**
  String get iosAutoShortcutStep3;

  /// No description provided for @iosAutoShortcutStep4.
  ///
  /// In en, this message translates to:
  /// **'Set the screenshot parameter of \"BeeCount\" to the previous \"Screenshot\"'**
  String get iosAutoShortcutStep4;

  /// No description provided for @iosAutoShortcutStep5.
  ///
  /// In en, this message translates to:
  /// **'(Optional) Go to Settings > Accessibility > Touch > Back Tap, bind this shortcut'**
  String get iosAutoShortcutStep5;

  /// No description provided for @iosAutoShortcutStep6.
  ///
  /// In en, this message translates to:
  /// **'Done! Double tap phone back during payment for quick billing'**
  String get iosAutoShortcutStep6;

  /// No description provided for @iosAutoShortcutRecommendedTip.
  ///
  /// In en, this message translates to:
  /// **'✅ Recommended: After binding the shortcut to \"Back Tap\", double tap phone back during payment to auto-screenshot and recognize billing, no manual screenshot needed.'**
  String get iosAutoShortcutRecommendedTip;

  /// No description provided for @iosAutoBackTapTitle.
  ///
  /// In en, this message translates to:
  /// **'💡 Double Tap Back to Trigger (Recommended)'**
  String get iosAutoBackTapTitle;

  /// No description provided for @iosAutoBackTapDesc.
  ///
  /// In en, this message translates to:
  /// **'Settings > Accessibility > Touch > Back Tap\n• Select \"Double Tap\" or \"Triple Tap\"\n• Choose the shortcut you just created\n• After setup, double tap phone back during payment to auto-record, no screenshot needed'**
  String get iosAutoBackTapDesc;

  /// No description provided for @iosAutoTutorialTitle.
  ///
  /// In en, this message translates to:
  /// **'Video Tutorial'**
  String get iosAutoTutorialTitle;

  /// No description provided for @iosAutoTutorialDesc.
  ///
  /// In en, this message translates to:
  /// **'Watch detailed setup video tutorial'**
  String get iosAutoTutorialDesc;

  /// No description provided for @iosAutoImportTitle.
  ///
  /// In en, this message translates to:
  /// **'Get Shortcut in One Tap'**
  String get iosAutoImportTitle;

  /// No description provided for @iosAutoImportDesc.
  ///
  /// In en, this message translates to:
  /// **'Tap the button below to import the ready-made \"Screenshot → Auto Billing\" shortcut — no need to add the \"Take Screenshot\" action or connect parameters manually. After importing, binding it to \"Back Tap\" is recommended.'**
  String get iosAutoImportDesc;

  /// No description provided for @iosAutoImportButton.
  ///
  /// In en, this message translates to:
  /// **'Get Shortcut'**
  String get iosAutoImportButton;

  /// No description provided for @iosAutoImportFailed.
  ///
  /// In en, this message translates to:
  /// **'Couldn\'t open the shortcut link. Check your connection and try again.'**
  String get iosAutoImportFailed;

  /// No description provided for @iosAutoManualConfigTitle.
  ///
  /// In en, this message translates to:
  /// **'Manual Setup (Advanced)'**
  String get iosAutoManualConfigTitle;

  /// No description provided for @iosAutoManualConfigDesc.
  ///
  /// In en, this message translates to:
  /// **'If one-tap import isn\'t available, follow these steps to create the shortcut manually.'**
  String get iosAutoManualConfigDesc;





























































































  /// No description provided for @dataManagement.
  ///
  /// In en, this message translates to:
  /// **'Data Management'**
  String get dataManagement;

  /// No description provided for @dataManagementDesc.
  ///
  /// In en, this message translates to:
  /// **'Import, export, categories and accounts'**
  String get dataManagementDesc;

  /// No description provided for @dataManagementPageTitle.
  ///
  /// In en, this message translates to:
  /// **'Data Management'**
  String get dataManagementPageTitle;

  /// No description provided for @dataManagementPageSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Manage transaction data and categories'**
  String get dataManagementPageSubtitle;

  /// No description provided for @dataManagementAttachmentHint.
  ///
  /// In en, this message translates to:
  /// **'When restoring data, please import the attachment package first, then import ledger data (CSV or cloud sync) to ensure attachments are correctly associated.'**
  String get dataManagementAttachmentHint;























  /// No description provided for @shareBilling.
  ///
  /// In en, this message translates to:
  /// **'Share to Bill'**
  String get shareBilling;

  /// No description provided for @shareBillingDesc.
  ///
  /// In en, this message translates to:
  /// **'Share a payment screenshot from Alipay/WeChat to bill it'**
  String get shareBillingDesc;

  /// No description provided for @shareBillingGuide.
  ///
  /// In en, this message translates to:
  /// **'When you see a payment screenshot in Alipay, WeChat, Photos, etc., tap \"Share\" and choose \"BeeCount\" to auto-recognize the amount, merchant, and time and create a transaction — no need to save the screenshot first.'**
  String get shareBillingGuide;

  /// No description provided for @shareBillingActionHint.
  ///
  /// In en, this message translates to:
  /// **'Recognized automatically in the background after sharing — no need to open BeeCount'**
  String get shareBillingActionHint;

  /// No description provided for @automation.
  ///
  /// In en, this message translates to:
  /// **'Automation'**
  String get automation;

  /// No description provided for @automationDesc.
  ///
  /// In en, this message translates to:
  /// **'Recurring transactions and reminders'**
  String get automationDesc;

  /// No description provided for @automationPageTitle.
  ///
  /// In en, this message translates to:
  /// **'Automation'**
  String get automationPageTitle;

  /// No description provided for @automationPageSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Recurring transactions and reminder settings'**
  String get automationPageSubtitle;

  /// No description provided for @appearanceSettings.
  ///
  /// In en, this message translates to:
  /// **'Personalization'**
  String get appearanceSettings;

  /// No description provided for @appearanceSettingsDesc.
  ///
  /// In en, this message translates to:
  /// **'Theme, font, language, app lock, etc.'**
  String get appearanceSettingsDesc;

  /// No description provided for @appearanceSettingsPageTitle.
  ///
  /// In en, this message translates to:
  /// **'Personalization'**
  String get appearanceSettingsPageTitle;

  /// No description provided for @appearanceSettingsPageSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Appearance, display, security and other app preferences'**
  String get appearanceSettingsPageSubtitle;

  /// No description provided for @about.
  ///
  /// In en, this message translates to:
  /// **'About'**
  String get about;

  /// No description provided for @aboutDesc.
  ///
  /// In en, this message translates to:
  /// **'Version info, help and feedback'**
  String get aboutDesc;

  /// No description provided for @mineRateApp.
  ///
  /// In en, this message translates to:
  /// **'Rate the App'**
  String get mineRateApp;

  /// No description provided for @mineRateAppSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Rate us on the App Store'**
  String get mineRateAppSubtitle;

  /// No description provided for @aboutPageTitle.
  ///
  /// In en, this message translates to:
  /// **'About'**
  String get aboutPageTitle;

  /// No description provided for @aboutPageSubtitle.
  ///
  /// In en, this message translates to:
  /// **'App information and help'**
  String get aboutPageSubtitle;

  /// No description provided for @aboutPageLoadingVersion.
  ///
  /// In en, this message translates to:
  /// **'Loading version...'**
  String get aboutPageLoadingVersion;

  /// No description provided for @aboutWebsite.
  ///
  /// In en, this message translates to:
  /// **'Official Website'**
  String get aboutWebsite;

  /// No description provided for @aboutGitHubRepo.
  ///
  /// In en, this message translates to:
  /// **'GitHub Repository'**
  String get aboutGitHubRepo;

  /// No description provided for @aboutXiaohongshu.
  ///
  /// In en, this message translates to:
  /// **'Xiaohongshu'**
  String get aboutXiaohongshu;

  /// No description provided for @aboutDouyin.
  ///
  /// In en, this message translates to:
  /// **'Douyin'**
  String get aboutDouyin;

  /// No description provided for @aboutTelegram.
  ///
  /// In en, this message translates to:
  /// **'Telegram'**
  String get aboutTelegram;

  /// No description provided for @aboutSupportDevelopment.
  ///
  /// In en, this message translates to:
  /// **'Support Development'**
  String get aboutSupportDevelopment;

  /// No description provided for @aboutSupportDevelopmentSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Buy me a coffee'**
  String get aboutSupportDevelopmentSubtitle;

  /// No description provided for @aboutDeveloperStoryTitle.
  ///
  /// In en, this message translates to:
  /// **'From the Developer'**
  String get aboutDeveloperStoryTitle;

  /// No description provided for @aboutDeveloperStory.
  ///
  /// In en, this message translates to:
  /// **'I started tracking my expenses in 2015 as an intern, and I\'ve kept the habit for over a decade. Concerned about ads, paywalls, privacy risks, and apps shutting down, I decided to build my own — first as a small tool for myself and my family.\n\nIn September 2025, BeeCount launched its first version. Honestly, I had no idea if anyone would use it. But gradually, feedback started coming in — someone said they finally found a clean budgeting app, others offered great suggestions, and some quietly left five-star reviews. Every single message reminded me this was worth continuing.\n\nBeeCount is ad-free, subscription-free, and fully open source. All your data stays on your device and is never uploaded to any third-party server. But publishing and maintaining an app isn\'t free — developer accounts, servers, and other costs are currently covered by community donations, and every system update, bug fix, and new feature is built outside of my day job.\n\nIf BeeCount has been helpful to you, a rating, a share, or a donation would help this little project go further. Thank you for your trust.'**
  String get aboutDeveloperStory;

  /// No description provided for @aboutRelatedProducts.
  ///
  /// In en, this message translates to:
  /// **'More Products'**
  String get aboutRelatedProducts;



















  /// No description provided for @logCenterTitle.
  ///
  /// In en, this message translates to:
  /// **'Log Center'**
  String get logCenterTitle;

  /// No description provided for @logCenterSubtitle.
  ///
  /// In en, this message translates to:
  /// **'View app runtime logs'**
  String get logCenterSubtitle;

  /// No description provided for @logCenterSearchHint.
  ///
  /// In en, this message translates to:
  /// **'Search log content or tags...'**
  String get logCenterSearchHint;

  /// No description provided for @logCenterFilterLevel.
  ///
  /// In en, this message translates to:
  /// **'Log Level'**
  String get logCenterFilterLevel;

  /// No description provided for @logCenterFilterPlatform.
  ///
  /// In en, this message translates to:
  /// **'Platform'**
  String get logCenterFilterPlatform;

  /// No description provided for @logCenterTotal.
  ///
  /// In en, this message translates to:
  /// **'Total'**
  String get logCenterTotal;

  /// No description provided for @logCenterFiltered.
  ///
  /// In en, this message translates to:
  /// **'Filtered'**
  String get logCenterFiltered;

  /// No description provided for @logCenterEmpty.
  ///
  /// In en, this message translates to:
  /// **'No logs'**
  String get logCenterEmpty;

  /// No description provided for @logCenterExport.
  ///
  /// In en, this message translates to:
  /// **'Export'**
  String get logCenterExport;

  /// No description provided for @logCenterClear.
  ///
  /// In en, this message translates to:
  /// **'Clear'**
  String get logCenterClear;

  /// No description provided for @logCenterExportFailed.
  ///
  /// In en, this message translates to:
  /// **'Export failed'**
  String get logCenterExportFailed;

  /// No description provided for @logCenterClearConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Clear Logs'**
  String get logCenterClearConfirmTitle;

  /// No description provided for @logCenterClearConfirmMessage.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to clear all logs? This action cannot be undone.'**
  String get logCenterClearConfirmMessage;

  /// No description provided for @logCenterCleared.
  ///
  /// In en, this message translates to:
  /// **'Logs cleared'**
  String get logCenterCleared;

  /// No description provided for @logCenterCopied.
  ///
  /// In en, this message translates to:
  /// **'Copied to clipboard'**
  String get logCenterCopied;

  /// No description provided for @configImportExportTitle.
  ///
  /// In en, this message translates to:
  /// **'Config Import/Export'**
  String get configImportExportTitle;

  /// No description provided for @configImportExportSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Backup and restore app configurations'**
  String get configImportExportSubtitle;

  /// No description provided for @configImportExportInfoTitle.
  ///
  /// In en, this message translates to:
  /// **'Feature Description'**
  String get configImportExportInfoTitle;

  /// No description provided for @configImportExportInfoMessage.
  ///
  /// In en, this message translates to:
  /// **'This feature is used to export and import app configurations, including cloud service settings, AI settings, etc. The config file uses YAML format for easy viewing and editing.\n\n⚠️ Config files contain sensitive information (such as API keys, passwords, etc.), please keep them safe.'**
  String get configImportExportInfoMessage;

  /// No description provided for @configExportTitle.
  ///
  /// In en, this message translates to:
  /// **'Export Config'**
  String get configExportTitle;

  /// No description provided for @configExportSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Export current config to YAML file'**
  String get configExportSubtitle;

  /// No description provided for @configExportShareSubject.
  ///
  /// In en, this message translates to:
  /// **'BeeCount Config File'**
  String get configExportShareSubject;

  /// No description provided for @configExportSuccess.
  ///
  /// In en, this message translates to:
  /// **'Config exported successfully'**
  String get configExportSuccess;

  /// No description provided for @configExportFailed.
  ///
  /// In en, this message translates to:
  /// **'Config export failed'**
  String get configExportFailed;

  /// No description provided for @configImportTitle.
  ///
  /// In en, this message translates to:
  /// **'Import Config'**
  String get configImportTitle;

  /// No description provided for @configImportSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Restore config from YAML file'**
  String get configImportSubtitle;

  /// No description provided for @configImportNoFilePath.
  ///
  /// In en, this message translates to:
  /// **'No file selected'**
  String get configImportNoFilePath;

  /// No description provided for @configImportConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Confirm Import'**
  String get configImportConfirmTitle;

  /// No description provided for @configImportSuccess.
  ///
  /// In en, this message translates to:
  /// **'Config imported successfully'**
  String get configImportSuccess;

  /// No description provided for @configImportFailed.
  ///
  /// In en, this message translates to:
  /// **'Config import failed'**
  String get configImportFailed;

  /// No description provided for @configImportRestartTitle.
  ///
  /// In en, this message translates to:
  /// **'Restart Required'**
  String get configImportRestartTitle;

  /// No description provided for @configImportRestartMessage.
  ///
  /// In en, this message translates to:
  /// **'Config has been imported. Some settings will take effect after restarting the app.'**
  String get configImportRestartMessage;

  /// No description provided for @configImportExportIncludesTitle.
  ///
  /// In en, this message translates to:
  /// **'Included Configurations'**
  String get configImportExportIncludesTitle;

  /// No description provided for @configExportSavedTo.
  ///
  /// In en, this message translates to:
  /// **'Saved to: {path}'**
  String configExportSavedTo(String path);

  /// No description provided for @configExportViewContent.
  ///
  /// In en, this message translates to:
  /// **'View Content'**
  String get configExportViewContent;

  /// No description provided for @configExportCopyContent.
  ///
  /// In en, this message translates to:
  /// **'Copy Content'**
  String get configExportCopyContent;

  /// No description provided for @configExportContentCopied.
  ///
  /// In en, this message translates to:
  /// **'Copied to clipboard'**
  String get configExportContentCopied;

  /// No description provided for @configExportReadFileFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to read file'**
  String get configExportReadFileFailed;

  /// No description provided for @configIncludeLedgers.
  ///
  /// In en, this message translates to:
  /// **'Ledgers'**
  String get configIncludeLedgers;






  /// No description provided for @configIncludeAppSettings.
  ///
  /// In en, this message translates to:
  /// **'App settings (language, appearance, reminder, default account, etc.)'**
  String get configIncludeAppSettings;

  /// No description provided for @configIncludeRecurringTransactions.
  ///
  /// In en, this message translates to:
  /// **'Recurring transactions'**
  String get configIncludeRecurringTransactions;

  /// No description provided for @configIncludeAccounts.
  ///
  /// In en, this message translates to:
  /// **'Accounts'**
  String get configIncludeAccounts;

  /// No description provided for @configIncludeCategories.
  ///
  /// In en, this message translates to:
  /// **'Categories'**
  String get configIncludeCategories;

  /// No description provided for @configIncludeTags.
  ///
  /// In en, this message translates to:
  /// **'Tags'**
  String get configIncludeTags;

  /// No description provided for @configIncludeBudgets.
  ///
  /// In en, this message translates to:
  /// **'Budgets'**
  String get configIncludeBudgets;

  /// No description provided for @configIncludeOtherSettings.
  ///
  /// In en, this message translates to:
  /// **'Other Settings'**
  String get configIncludeOtherSettings;

  /// No description provided for @configIncludeOtherSettingsSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Including cloud service, AI config, app settings, etc.'**
  String get configIncludeOtherSettingsSubtitle;

  /// No description provided for @configExportSelectTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Export Content'**
  String get configExportSelectTitle;

  /// No description provided for @configExportPreviewTitle.
  ///
  /// In en, this message translates to:
  /// **'Export Preview'**
  String get configExportPreviewTitle;

  /// No description provided for @configExportConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Confirm Export'**
  String get configExportConfirmTitle;

  /// No description provided for @configImportSelectTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Import Content'**
  String get configImportSelectTitle;

  /// No description provided for @configImportPreviewTitle.
  ///
  /// In en, this message translates to:
  /// **'Import Preview'**
  String get configImportPreviewTitle;

  /// No description provided for @ledgersConflictTitle.
  ///
  /// In en, this message translates to:
  /// **'Sync Conflict'**
  String get ledgersConflictTitle;

  /// No description provided for @ledgersConflictMessage.
  ///
  /// In en, this message translates to:
  /// **'Local and cloud ledger data are inconsistent, please choose an action:'**
  String get ledgersConflictMessage;

  /// No description provided for @ledgersConflictLocalInfo.
  ///
  /// In en, this message translates to:
  /// **'Local: {count} transactions'**
  String ledgersConflictLocalInfo(int count);

  /// No description provided for @ledgersConflictRemoteInfo.
  ///
  /// In en, this message translates to:
  /// **'Cloud: {count} transactions'**
  String ledgersConflictRemoteInfo(int count);

  /// No description provided for @ledgersConflictRemoteUpdated.
  ///
  /// In en, this message translates to:
  /// **'Cloud updated: {time}'**
  String ledgersConflictRemoteUpdated(String time);

  /// No description provided for @ledgersConflictLocalFingerprint.
  ///
  /// In en, this message translates to:
  /// **'Local fingerprint: {fp}'**
  String ledgersConflictLocalFingerprint(String fp);

  /// No description provided for @ledgersConflictRemoteFingerprint.
  ///
  /// In en, this message translates to:
  /// **'Cloud fingerprint: {fp}'**
  String ledgersConflictRemoteFingerprint(String fp);

  /// No description provided for @ledgersConflictUpload.
  ///
  /// In en, this message translates to:
  /// **'Upload to Cloud'**
  String get ledgersConflictUpload;

  /// No description provided for @ledgersConflictDownload.
  ///
  /// In en, this message translates to:
  /// **'Download to Local'**
  String get ledgersConflictDownload;

  /// No description provided for @ledgersConflictUploading.
  ///
  /// In en, this message translates to:
  /// **'Uploading...'**
  String get ledgersConflictUploading;

  /// No description provided for @ledgersConflictDownloading.
  ///
  /// In en, this message translates to:
  /// **'Downloading...'**
  String get ledgersConflictDownloading;

  /// No description provided for @ledgersConflictUploadSuccess.
  ///
  /// In en, this message translates to:
  /// **'Upload successful'**
  String get ledgersConflictUploadSuccess;

  /// No description provided for @ledgersConflictDownloadSuccess.
  ///
  /// In en, this message translates to:
  /// **'Download successful, merged {inserted} transactions'**
  String ledgersConflictDownloadSuccess(int inserted);

  /// No description provided for @storageManagementTitle.
  ///
  /// In en, this message translates to:
  /// **'Storage Management'**
  String get storageManagementTitle;

  /// No description provided for @storageManagementSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Clear cache to free up space'**
  String get storageManagementSubtitle;


  /// No description provided for @storageAPKFiles.
  ///
  /// In en, this message translates to:
  /// **'Installation Packages'**
  String get storageAPKFiles;

  /// No description provided for @storageNoData.
  ///
  /// In en, this message translates to:
  /// **'No Data'**
  String get storageNoData;

  /// No description provided for @storageFiles.
  ///
  /// In en, this message translates to:
  /// **'files'**
  String get storageFiles;

  /// No description provided for @storageHint.
  ///
  /// In en, this message translates to:
  /// **'Tap items to clear corresponding cache files'**
  String get storageHint;

  /// No description provided for @storageClearConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Confirm Clear'**
  String get storageClearConfirmTitle;


  /// No description provided for @storageClearAPKMessage.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to clear all installation packages? Size: {size}'**
  String storageClearAPKMessage(String size);

  /// No description provided for @storageClearSuccess.
  ///
  /// In en, this message translates to:
  /// **'Cleared successfully'**
  String get storageClearSuccess;

  /// No description provided for @accountNoTransactions.
  ///
  /// In en, this message translates to:
  /// **'No transactions'**
  String get accountNoTransactions;

  /// No description provided for @accountTransactionHistory.
  ///
  /// In en, this message translates to:
  /// **'Transaction History'**
  String get accountTransactionHistory;

  /// No description provided for @accountTotalBalance.
  ///
  /// In en, this message translates to:
  /// **'Net Assets'**
  String get accountTotalBalance;

  /// No description provided for @accountCurrencyLocked.
  ///
  /// In en, this message translates to:
  /// **'This account has transactions and cannot change currency'**
  String get accountCurrencyLocked;

  /// No description provided for @accountDefaultIncomeTitle.
  ///
  /// In en, this message translates to:
  /// **'Default Income Account'**
  String get accountDefaultIncomeTitle;

  /// No description provided for @accountDefaultExpenseTitle.
  ///
  /// In en, this message translates to:
  /// **'Default Expense Account'**
  String get accountDefaultExpenseTitle;

  /// No description provided for @accountDefaultNone.
  ///
  /// In en, this message translates to:
  /// **'Not Set'**
  String get accountDefaultNone;

  /// No description provided for @commonNotice.
  ///
  /// In en, this message translates to:
  /// **'Notice'**
  String get commonNotice;

  /// No description provided for @transferTitle.
  ///
  /// In en, this message translates to:
  /// **'Transfer'**
  String get transferTitle;

  /// No description provided for @transferIconSettings.
  ///
  /// In en, this message translates to:
  /// **'Transfer Icon Settings'**
  String get transferIconSettings;

  /// No description provided for @transferIconSettingsDesc.
  ///
  /// In en, this message translates to:
  /// **'Customize the display icon for transfer records'**
  String get transferIconSettingsDesc;

  /// No description provided for @transferFromAccount.
  ///
  /// In en, this message translates to:
  /// **'From Account'**
  String get transferFromAccount;

  /// No description provided for @transferToAccount.
  ///
  /// In en, this message translates to:
  /// **'To Account'**
  String get transferToAccount;

  /// No description provided for @transferSelectAccount.
  ///
  /// In en, this message translates to:
  /// **'Select Account'**
  String get transferSelectAccount;

  /// No description provided for @transferCreateSuccess.
  ///
  /// In en, this message translates to:
  /// **'Transfer created successfully'**
  String get transferCreateSuccess;

  /// No description provided for @transferUpdateSuccess.
  ///
  /// In en, this message translates to:
  /// **'Transfer updated successfully'**
  String get transferUpdateSuccess;

  /// No description provided for @transferDifferentCurrencyError.
  ///
  /// In en, this message translates to:
  /// **'Transfer only supports accounts with the same currency'**
  String get transferDifferentCurrencyError;

  /// No description provided for @transferToPrefix.
  ///
  /// In en, this message translates to:
  /// **'To'**
  String get transferToPrefix;

  /// No description provided for @transferFromPrefix.
  ///
  /// In en, this message translates to:
  /// **'From'**
  String get transferFromPrefix;

  /// No description provided for @welcomeCategoryModeTitle.
  ///
  /// In en, this message translates to:
  /// **'Choose Category Mode'**
  String get welcomeCategoryModeTitle;

  /// No description provided for @welcomeCategoryModeDescription.
  ///
  /// In en, this message translates to:
  /// **'Select the category structure that suits your needs'**
  String get welcomeCategoryModeDescription;

  /// No description provided for @welcomeCategoryModeFlatTitle.
  ///
  /// In en, this message translates to:
  /// **'Flat Categories'**
  String get welcomeCategoryModeFlatTitle;

  /// No description provided for @welcomeCategoryModeFlatDescription.
  ///
  /// In en, this message translates to:
  /// **'Simple and fast'**
  String get welcomeCategoryModeFlatDescription;

  /// No description provided for @welcomeCategoryModeFlatFeature1.
  ///
  /// In en, this message translates to:
  /// **'Flat structure, easy to use'**
  String get welcomeCategoryModeFlatFeature1;

  /// No description provided for @welcomeCategoryModeFlatFeature2.
  ///
  /// In en, this message translates to:
  /// **'Perfect for simple categorization'**
  String get welcomeCategoryModeFlatFeature2;

  /// No description provided for @welcomeCategoryModeFlatFeature3.
  ///
  /// In en, this message translates to:
  /// **'Quick selection, efficient tracking'**
  String get welcomeCategoryModeFlatFeature3;

  /// No description provided for @welcomeCategoryModeHierarchicalTitle.
  ///
  /// In en, this message translates to:
  /// **'Hierarchical Categories'**
  String get welcomeCategoryModeHierarchicalTitle;

  /// No description provided for @welcomeCategoryModeHierarchicalDescription.
  ///
  /// In en, this message translates to:
  /// **'Detailed management'**
  String get welcomeCategoryModeHierarchicalDescription;

  /// No description provided for @welcomeCategoryModeHierarchicalFeature1.
  ///
  /// In en, this message translates to:
  /// **'Support parent-child category levels'**
  String get welcomeCategoryModeHierarchicalFeature1;

  /// No description provided for @welcomeCategoryModeHierarchicalFeature2.
  ///
  /// In en, this message translates to:
  /// **'More detailed transaction classification'**
  String get welcomeCategoryModeHierarchicalFeature2;

  /// No description provided for @welcomeCategoryModeHierarchicalFeature3.
  ///
  /// In en, this message translates to:
  /// **'Perfect for detailed management'**
  String get welcomeCategoryModeHierarchicalFeature3;

  /// No description provided for @welcomeCategoryModeNoneTitle.
  ///
  /// In en, this message translates to:
  /// **'No Categories'**
  String get welcomeCategoryModeNoneTitle;

  /// No description provided for @welcomeCategoryModeNoneDescription.
  ///
  /// In en, this message translates to:
  /// **'Fully customizable, add as needed'**
  String get welcomeCategoryModeNoneDescription;

  /// No description provided for @welcomeCategoryModeNoneFeature1.
  ///
  /// In en, this message translates to:
  /// **'No preset categories'**
  String get welcomeCategoryModeNoneFeature1;

  /// No description provided for @welcomeCategoryModeNoneFeature2.
  ///
  /// In en, this message translates to:
  /// **'Create categories based on your needs'**
  String get welcomeCategoryModeNoneFeature2;

  /// No description provided for @welcomeCategoryModeNoneFeature3.
  ///
  /// In en, this message translates to:
  /// **'Perfect for custom classification needs'**
  String get welcomeCategoryModeNoneFeature3;

  /// No description provided for @welcomeExistingUserTitle.
  ///
  /// In en, this message translates to:
  /// **'Existing User?'**
  String get welcomeExistingUserTitle;

  /// No description provided for @welcomeExistingUserButton.
  ///
  /// In en, this message translates to:
  /// **'Import Config'**
  String get welcomeExistingUserButton;

  /// No description provided for @welcomeImportingConfig.
  ///
  /// In en, this message translates to:
  /// **'Importing configuration...'**
  String get welcomeImportingConfig;

  /// No description provided for @welcomeImportSuccess.
  ///
  /// In en, this message translates to:
  /// **'Configuration imported successfully'**
  String get welcomeImportSuccess;

  /// No description provided for @welcomeImportFailed.
  ///
  /// In en, this message translates to:
  /// **'Import failed: {error}'**
  String welcomeImportFailed(String error);

  /// No description provided for @welcomeImportNoFile.
  ///
  /// In en, this message translates to:
  /// **'No file selected'**
  String get welcomeImportNoFile;

  /// No description provided for @welcomeImportAttachmentTitle.
  ///
  /// In en, this message translates to:
  /// **'Import Attachments'**
  String get welcomeImportAttachmentTitle;

  /// No description provided for @welcomeImportAttachmentDesc.
  ///
  /// In en, this message translates to:
  /// **'Would you like to import attachment files as well?'**
  String get welcomeImportAttachmentDesc;

  /// No description provided for @welcomeImportAttachmentButton.
  ///
  /// In en, this message translates to:
  /// **'Select Attachment File'**
  String get welcomeImportAttachmentButton;

  /// No description provided for @welcomeImportAttachmentSkip.
  ///
  /// In en, this message translates to:
  /// **'Skip'**
  String get welcomeImportAttachmentSkip;

  /// No description provided for @welcomeImportAttachmentSuccess.
  ///
  /// In en, this message translates to:
  /// **'Attachments imported: {imported}'**
  String welcomeImportAttachmentSuccess(int imported);

  /// No description provided for @welcomeImportAttachmentFailed.
  ///
  /// In en, this message translates to:
  /// **'Attachment import failed: {error}'**
  String welcomeImportAttachmentFailed(String error);

  /// No description provided for @welcomeImportingAttachment.
  ///
  /// In en, this message translates to:
  /// **'Importing attachments...'**
  String get welcomeImportingAttachment;

  /// No description provided for @iosVersionWarningTitle.
  ///
  /// In en, this message translates to:
  /// **'Requires iOS 16.0 or later'**
  String get iosVersionWarningTitle;

  /// No description provided for @iosVersionWarningDesc.
  ///
  /// In en, this message translates to:
  /// **'Screenshot auto-billing feature uses the App Intents framework introduced in iOS 16. Your device is running an older version and does not support this feature.\n\nPlease upgrade to iOS 16 or later to use this feature.'**
  String get iosVersionWarningDesc;































































































  /// No description provided for @billCardSuccess.
  ///
  /// In en, this message translates to:
  /// **'Booking Successful'**
  String get billCardSuccess;

  /// No description provided for @billCardUndone.
  ///
  /// In en, this message translates to:
  /// **'Undone'**
  String get billCardUndone;

  /// No description provided for @billCardAmount.
  ///
  /// In en, this message translates to:
  /// **'💰 Amount'**
  String get billCardAmount;

  /// No description provided for @billCardCategory.
  ///
  /// In en, this message translates to:
  /// **'🏷️ Category'**
  String get billCardCategory;

  /// No description provided for @billCardTime.
  ///
  /// In en, this message translates to:
  /// **'📅 Time'**
  String get billCardTime;

  /// No description provided for @billCardNote.
  ///
  /// In en, this message translates to:
  /// **'📝 Note'**
  String get billCardNote;

  /// No description provided for @billCardAccount.
  ///
  /// In en, this message translates to:
  /// **'💳 Account'**
  String get billCardAccount;

  /// No description provided for @billCardTransferAccounts.
  ///
  /// In en, this message translates to:
  /// **'🔁 Transfer accounts'**
  String get billCardTransferAccounts;

  /// No description provided for @billCardType.
  ///
  /// In en, this message translates to:
  /// **'↕️ Type'**
  String get billCardType;

  /// No description provided for @billCardExpense.
  ///
  /// In en, this message translates to:
  /// **'Expense'**
  String get billCardExpense;

  /// No description provided for @billCardIncome.
  ///
  /// In en, this message translates to:
  /// **'Income'**
  String get billCardIncome;

  /// No description provided for @billCardTransfer.
  ///
  /// In en, this message translates to:
  /// **'Transfer'**
  String get billCardTransfer;

  /// No description provided for @billCardCurrency.
  ///
  /// In en, this message translates to:
  /// **'💱 Currency'**
  String get billCardCurrency;

  /// No description provided for @billCardTags.
  ///
  /// In en, this message translates to:
  /// **'🏷️ Tags'**
  String get billCardTags;

  /// No description provided for @billCardUndo.
  ///
  /// In en, this message translates to:
  /// **'Undo'**
  String get billCardUndo;

  /// No description provided for @billCardEdit.
  ///
  /// In en, this message translates to:
  /// **'Edit'**
  String get billCardEdit;




























  /// No description provided for @billCardUnknownLedger.
  ///
  /// In en, this message translates to:
  /// **'Unknown Ledger'**
  String get billCardUnknownLedger;



































  /// No description provided for @tagManageTitle.
  ///
  /// In en, this message translates to:
  /// **'Tags'**
  String get tagManageTitle;

  /// No description provided for @tagManageSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Manage transaction tags'**
  String get tagManageSubtitle;

  /// No description provided for @tagManageEmpty.
  ///
  /// In en, this message translates to:
  /// **'No tags yet'**
  String get tagManageEmpty;

  /// No description provided for @tagManageEmptyHint.
  ///
  /// In en, this message translates to:
  /// **'Tap + to add a tag'**
  String get tagManageEmptyHint;

  /// No description provided for @tagManageGenerateDefault.
  ///
  /// In en, this message translates to:
  /// **'Generate Default Tags'**
  String get tagManageGenerateDefault;

  /// No description provided for @tagManageGenerateDefaultConfirm.
  ///
  /// In en, this message translates to:
  /// **'Generate default tags? Existing tags with the same name will not be overwritten.'**
  String get tagManageGenerateDefaultConfirm;

  /// No description provided for @tagManageGenerateDefaultSuccess.
  ///
  /// In en, this message translates to:
  /// **'Default tags generated'**
  String get tagManageGenerateDefaultSuccess;

  /// No description provided for @tagEditTitle.
  ///
  /// In en, this message translates to:
  /// **'Edit Tag'**
  String get tagEditTitle;

  /// No description provided for @tagAddTitle.
  ///
  /// In en, this message translates to:
  /// **'Add Tag'**
  String get tagAddTitle;

  /// No description provided for @tagNameLabel.
  ///
  /// In en, this message translates to:
  /// **'Tag Name'**
  String get tagNameLabel;

  /// No description provided for @tagNameHint.
  ///
  /// In en, this message translates to:
  /// **'Enter tag name'**
  String get tagNameHint;

  /// No description provided for @tagNameRequired.
  ///
  /// In en, this message translates to:
  /// **'Tag name is required'**
  String get tagNameRequired;

  /// No description provided for @tagNameDuplicate.
  ///
  /// In en, this message translates to:
  /// **'Tag name already exists'**
  String get tagNameDuplicate;

  /// No description provided for @tagColorLabel.
  ///
  /// In en, this message translates to:
  /// **'Tag Color'**
  String get tagColorLabel;

  /// No description provided for @tagCreateSuccess.
  ///
  /// In en, this message translates to:
  /// **'Tag created'**
  String get tagCreateSuccess;

  /// No description provided for @tagUpdateSuccess.
  ///
  /// In en, this message translates to:
  /// **'Tag updated'**
  String get tagUpdateSuccess;

  /// No description provided for @tagDeleteConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Delete Tag'**
  String get tagDeleteConfirmTitle;

  /// No description provided for @tagDeleteConfirmMessage.
  ///
  /// In en, this message translates to:
  /// **'Delete tag \"{name}\"? This will not affect associated transactions.'**
  String tagDeleteConfirmMessage(String name);

  /// No description provided for @tagDeleteSuccess.
  ///
  /// In en, this message translates to:
  /// **'Tag deleted'**
  String get tagDeleteSuccess;

  /// No description provided for @tagSelectTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Tags'**
  String get tagSelectTitle;

  /// No description provided for @tagSelectHint.
  ///
  /// In en, this message translates to:
  /// **'Multiple selection'**
  String get tagSelectHint;

  /// No description provided for @tagSelectCreateNew.
  ///
  /// In en, this message translates to:
  /// **'Create New Tag'**
  String get tagSelectCreateNew;

  /// No description provided for @tagSelectOwnerManaged.
  ///
  /// In en, this message translates to:
  /// **'Shared ledger tags are managed by the owner'**
  String get tagSelectOwnerManaged;

  /// No description provided for @tagSelectRecentlyUsed.
  ///
  /// In en, this message translates to:
  /// **'Recently Used'**
  String get tagSelectRecentlyUsed;

  /// No description provided for @tagSelectAllTags.
  ///
  /// In en, this message translates to:
  /// **'All Tags'**
  String get tagSelectAllTags;

  /// No description provided for @tagTransactionCount.
  ///
  /// In en, this message translates to:
  /// **'{count} transactions'**
  String tagTransactionCount(int count);

  /// No description provided for @tagDetailTitle.
  ///
  /// In en, this message translates to:
  /// **'Tag Details'**
  String get tagDetailTitle;

  /// No description provided for @tagDetailTotalCount.
  ///
  /// In en, this message translates to:
  /// **'Transactions'**
  String get tagDetailTotalCount;

  /// No description provided for @tagDetailTotalExpense.
  ///
  /// In en, this message translates to:
  /// **'Total Expense'**
  String get tagDetailTotalExpense;

  /// No description provided for @tagDetailTotalIncome.
  ///
  /// In en, this message translates to:
  /// **'Total Income'**
  String get tagDetailTotalIncome;

  /// No description provided for @tagDetailTransactionList.
  ///
  /// In en, this message translates to:
  /// **'Related Transactions'**
  String get tagDetailTransactionList;

  /// No description provided for @tagDetailNoTransactions.
  ///
  /// In en, this message translates to:
  /// **'No related transactions'**
  String get tagDetailNoTransactions;

  /// No description provided for @tagDetailNoTransactionsHint.
  ///
  /// In en, this message translates to:
  /// **'Transactions with this tag will appear here'**
  String get tagDetailNoTransactionsHint;

  /// No description provided for @tagNotFound.
  ///
  /// In en, this message translates to:
  /// **'Tag not found'**
  String get tagNotFound;

  /// No description provided for @tagDefaultMeituan.
  ///
  /// In en, this message translates to:
  /// **'Meituan'**
  String get tagDefaultMeituan;

  /// No description provided for @tagDefaultEleme.
  ///
  /// In en, this message translates to:
  /// **'Eleme'**
  String get tagDefaultEleme;

  /// No description provided for @tagDefaultTaobao.
  ///
  /// In en, this message translates to:
  /// **'Taobao'**
  String get tagDefaultTaobao;

  /// No description provided for @tagDefaultJD.
  ///
  /// In en, this message translates to:
  /// **'JD.com'**
  String get tagDefaultJD;

  /// No description provided for @tagDefaultPDD.
  ///
  /// In en, this message translates to:
  /// **'Pinduoduo'**
  String get tagDefaultPDD;

  /// No description provided for @tagDefaultStarbucks.
  ///
  /// In en, this message translates to:
  /// **'Starbucks'**
  String get tagDefaultStarbucks;

  /// No description provided for @tagDefaultLuckin.
  ///
  /// In en, this message translates to:
  /// **'Luckin Coffee'**
  String get tagDefaultLuckin;

  /// No description provided for @tagDefaultMcDonalds.
  ///
  /// In en, this message translates to:
  /// **'McDonald\'s'**
  String get tagDefaultMcDonalds;

  /// No description provided for @tagDefaultKFC.
  ///
  /// In en, this message translates to:
  /// **'KFC'**
  String get tagDefaultKFC;

  /// No description provided for @tagDefaultHema.
  ///
  /// In en, this message translates to:
  /// **'Hema'**
  String get tagDefaultHema;

  /// No description provided for @tagDefaultSams.
  ///
  /// In en, this message translates to:
  /// **'Sam\'s Club'**
  String get tagDefaultSams;

  /// No description provided for @tagDefaultCostco.
  ///
  /// In en, this message translates to:
  /// **'Costco'**
  String get tagDefaultCostco;

  /// No description provided for @tagDefaultBusinessTrip.
  ///
  /// In en, this message translates to:
  /// **'Business Trip'**
  String get tagDefaultBusinessTrip;

  /// No description provided for @tagDefaultTravel.
  ///
  /// In en, this message translates to:
  /// **'Travel'**
  String get tagDefaultTravel;

  /// No description provided for @tagDefaultDining.
  ///
  /// In en, this message translates to:
  /// **'Dining Out'**
  String get tagDefaultDining;

  /// No description provided for @tagDefaultOnlineShopping.
  ///
  /// In en, this message translates to:
  /// **'Online Shopping'**
  String get tagDefaultOnlineShopping;

  /// No description provided for @tagDefaultDaily.
  ///
  /// In en, this message translates to:
  /// **'Daily'**
  String get tagDefaultDaily;

  /// No description provided for @tagDefaultReimbursement.
  ///
  /// In en, this message translates to:
  /// **'Reimbursable'**
  String get tagDefaultReimbursement;

  /// No description provided for @tagDefaultRefundable.
  ///
  /// In en, this message translates to:
  /// **'Refundable'**
  String get tagDefaultRefundable;

  /// No description provided for @tagDefaultRefunded.
  ///
  /// In en, this message translates to:
  /// **'Refunded'**
  String get tagDefaultRefunded;

  /// No description provided for @tagDefaultVoiceBilling.
  ///
  /// In en, this message translates to:
  /// **'Voice'**
  String get tagDefaultVoiceBilling;

  /// No description provided for @tagDefaultImageBilling.
  ///
  /// In en, this message translates to:
  /// **'Image'**
  String get tagDefaultImageBilling;

  /// No description provided for @tagDefaultCameraBilling.
  ///
  /// In en, this message translates to:
  /// **'Camera'**
  String get tagDefaultCameraBilling;

  /// No description provided for @tagDefaultAiBilling.
  ///
  /// In en, this message translates to:
  /// **'AI'**
  String get tagDefaultAiBilling;

  /// No description provided for @tagShare.
  ///
  /// In en, this message translates to:
  /// **'Share Tags'**
  String get tagShare;

  /// No description provided for @tagImport.
  ///
  /// In en, this message translates to:
  /// **'Import Tags'**
  String get tagImport;

  /// No description provided for @tagClearUnused.
  ///
  /// In en, this message translates to:
  /// **'Clear Unused'**
  String get tagClearUnused;

  /// No description provided for @tagShareSuccess.
  ///
  /// In en, this message translates to:
  /// **'Saved to {path}'**
  String tagShareSuccess(String path);

  /// No description provided for @tagShareSubject.
  ///
  /// In en, this message translates to:
  /// **'BeeCount Tags Configuration'**
  String get tagShareSubject;

  /// No description provided for @tagShareFailed.
  ///
  /// In en, this message translates to:
  /// **'Share failed'**
  String get tagShareFailed;

  /// No description provided for @tagImportInvalidFile.
  ///
  /// In en, this message translates to:
  /// **'Please select a YAML file'**
  String get tagImportInvalidFile;

  /// No description provided for @tagImportNoTags.
  ///
  /// In en, this message translates to:
  /// **'No tags found in file'**
  String get tagImportNoTags;

  /// No description provided for @tagImportModeTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Import Mode'**
  String get tagImportModeTitle;

  /// No description provided for @tagImportModeMerge.
  ///
  /// In en, this message translates to:
  /// **'Merge'**
  String get tagImportModeMerge;

  /// No description provided for @tagImportModeMergeDesc.
  ///
  /// In en, this message translates to:
  /// **'Keep existing tags, add new ones'**
  String get tagImportModeMergeDesc;

  /// No description provided for @tagImportModeOverwrite.
  ///
  /// In en, this message translates to:
  /// **'Overwrite'**
  String get tagImportModeOverwrite;

  /// No description provided for @tagImportModeOverwriteDesc.
  ///
  /// In en, this message translates to:
  /// **'Clear unused tags before import'**
  String get tagImportModeOverwriteDesc;

  /// No description provided for @tagImportSuccess.
  ///
  /// In en, this message translates to:
  /// **'Import successful'**
  String get tagImportSuccess;

  /// No description provided for @tagImportFailed.
  ///
  /// In en, this message translates to:
  /// **'Import failed'**
  String get tagImportFailed;

  /// No description provided for @tagClearUnusedEmpty.
  ///
  /// In en, this message translates to:
  /// **'No unused tags'**
  String get tagClearUnusedEmpty;

  /// No description provided for @tagClearUnusedTitle.
  ///
  /// In en, this message translates to:
  /// **'Clear Unused Tags'**
  String get tagClearUnusedTitle;

  /// No description provided for @tagClearUnusedMessage.
  ///
  /// In en, this message translates to:
  /// **'Delete {count} unused tags?'**
  String tagClearUnusedMessage(int count);

  /// No description provided for @tagClearUnusedSuccess.
  ///
  /// In en, this message translates to:
  /// **'Deleted {count} tags'**
  String tagClearUnusedSuccess(int count);

  /// No description provided for @tagClearUnusedFailed.
  ///
  /// In en, this message translates to:
  /// **'Clear failed'**
  String get tagClearUnusedFailed;

  /// No description provided for @homeSwitchLedger.
  ///
  /// In en, this message translates to:
  /// **'Select Ledger'**
  String get homeSwitchLedger;

  /// No description provided for @homeManageLedgers.
  ///
  /// In en, this message translates to:
  /// **'Manage Ledgers'**
  String get homeManageLedgers;

  /// No description provided for @budgetTitle.
  ///
  /// In en, this message translates to:
  /// **'Budget'**
  String get budgetTitle;

  /// No description provided for @budgetShowOnHome.
  ///
  /// In en, this message translates to:
  /// **'Show budget on home'**
  String get budgetShowOnHome;

  /// No description provided for @budgetEmptyHint.
  ///
  /// In en, this message translates to:
  /// **'No budget set yet'**
  String get budgetEmptyHint;

  /// No description provided for @budgetAddTotal.
  ///
  /// In en, this message translates to:
  /// **'Add Total Budget'**
  String get budgetAddTotal;

  /// No description provided for @budgetMonthlyBudget.
  ///
  /// In en, this message translates to:
  /// **'Monthly Budget'**
  String get budgetMonthlyBudget;

  /// No description provided for @budgetUsed.
  ///
  /// In en, this message translates to:
  /// **'Used'**
  String get budgetUsed;

  /// No description provided for @budgetRemaining.
  ///
  /// In en, this message translates to:
  /// **'Remaining'**
  String get budgetRemaining;

  /// No description provided for @budgetDaysRemaining.
  ///
  /// In en, this message translates to:
  /// **'{days} days remaining'**
  String budgetDaysRemaining(int days);

  /// No description provided for @budgetDailyAvailable.
  ///
  /// In en, this message translates to:
  /// **'Daily available {amount}'**
  String budgetDailyAvailable(String amount);

  /// No description provided for @budgetCategoryBudgets.
  ///
  /// In en, this message translates to:
  /// **'Category Budgets'**
  String get budgetCategoryBudgets;

  /// No description provided for @budgetEditTitle.
  ///
  /// In en, this message translates to:
  /// **'Edit Budget'**
  String get budgetEditTitle;

  /// No description provided for @budgetAddTitle.
  ///
  /// In en, this message translates to:
  /// **'Add Budget'**
  String get budgetAddTitle;

  /// No description provided for @budgetTypeTotalLabel.
  ///
  /// In en, this message translates to:
  /// **'Total Budget'**
  String get budgetTypeTotalLabel;

  /// No description provided for @budgetTypeCategoryLabel.
  ///
  /// In en, this message translates to:
  /// **'Category Budget'**
  String get budgetTypeCategoryLabel;

  /// No description provided for @budgetAmountLabel.
  ///
  /// In en, this message translates to:
  /// **'Budget Amount'**
  String get budgetAmountLabel;

  /// No description provided for @budgetAmountHint.
  ///
  /// In en, this message translates to:
  /// **'Enter budget amount'**
  String get budgetAmountHint;

  /// No description provided for @budgetCategoryLabel.
  ///
  /// In en, this message translates to:
  /// **'Select Category'**
  String get budgetCategoryLabel;

  /// No description provided for @budgetCategoryHint.
  ///
  /// In en, this message translates to:
  /// **'Select budget category'**
  String get budgetCategoryHint;

  /// No description provided for @budgetStartDayLabel.
  ///
  /// In en, this message translates to:
  /// **'Start Day'**
  String get budgetStartDayLabel;

  /// No description provided for @budgetPeriodLabel.
  ///
  /// In en, this message translates to:
  /// **'Period'**
  String get budgetPeriodLabel;

  /// No description provided for @budgetSaveSuccess.
  ///
  /// In en, this message translates to:
  /// **'Budget saved'**
  String get budgetSaveSuccess;

  /// No description provided for @budgetDeleteConfirm.
  ///
  /// In en, this message translates to:
  /// **'Delete this budget?'**
  String get budgetDeleteConfirm;

  /// No description provided for @budgetDeleteSuccess.
  ///
  /// In en, this message translates to:
  /// **'Budget deleted'**
  String get budgetDeleteSuccess;

  /// No description provided for @attachmentAdd.
  ///
  /// In en, this message translates to:
  /// **'Add Image'**
  String get attachmentAdd;

  /// No description provided for @attachmentTakePhoto.
  ///
  /// In en, this message translates to:
  /// **'Take Photo'**
  String get attachmentTakePhoto;

  /// No description provided for @attachmentChooseFromGallery.
  ///
  /// In en, this message translates to:
  /// **'Choose from Gallery'**
  String get attachmentChooseFromGallery;

  /// No description provided for @attachmentMaxReached.
  ///
  /// In en, this message translates to:
  /// **'Maximum attachments reached'**
  String get attachmentMaxReached;

  /// No description provided for @attachmentDeleteConfirm.
  ///
  /// In en, this message translates to:
  /// **'Delete this attachment?'**
  String get attachmentDeleteConfirm;

  /// No description provided for @attachmentCount.
  ///
  /// In en, this message translates to:
  /// **'{count} images'**
  String attachmentCount(int count);

  /// No description provided for @commonDeleted.
  ///
  /// In en, this message translates to:
  /// **'Deleted'**
  String get commonDeleted;

  /// No description provided for @attachmentExportTitle.
  ///
  /// In en, this message translates to:
  /// **'Export Attachments'**
  String get attachmentExportTitle;

  /// No description provided for @attachmentExportSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Export all attachments as a compressed file'**
  String get attachmentExportSubtitle;

  /// No description provided for @attachmentImportTitle.
  ///
  /// In en, this message translates to:
  /// **'Import Attachments'**
  String get attachmentImportTitle;

  /// No description provided for @attachmentImportSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Import attachments from a compressed file'**
  String get attachmentImportSubtitle;

  /// No description provided for @attachmentExportEmpty.
  ///
  /// In en, this message translates to:
  /// **'No attachments to export'**
  String get attachmentExportEmpty;

  /// No description provided for @attachmentExportProgress.
  ///
  /// In en, this message translates to:
  /// **'Exporting attachments ({current}/{total})'**
  String attachmentExportProgress(int current, int total);

  /// No description provided for @attachmentExportProgressDetail.
  ///
  /// In en, this message translates to:
  /// **'Exporting {attachmentCount} attachment(s) + {iconCount} icon(s) ({current}/{total})'**
  String attachmentExportProgressDetail(int attachmentCount, int iconCount, int current, int total);

  /// No description provided for @attachmentExportSuccess.
  ///
  /// In en, this message translates to:
  /// **'Attachments exported successfully'**
  String get attachmentExportSuccess;

  /// No description provided for @attachmentExportSavedTo.
  ///
  /// In en, this message translates to:
  /// **'Saved to: {path}'**
  String attachmentExportSavedTo(String path);

  /// No description provided for @attachmentImportConflictStrategy.
  ///
  /// In en, this message translates to:
  /// **'Conflict Strategy'**
  String get attachmentImportConflictStrategy;

  /// No description provided for @attachmentImportConflictSkip.
  ///
  /// In en, this message translates to:
  /// **'Skip existing attachments'**
  String get attachmentImportConflictSkip;

  /// No description provided for @attachmentImportConflictOverwrite.
  ///
  /// In en, this message translates to:
  /// **'Overwrite existing attachments'**
  String get attachmentImportConflictOverwrite;

  /// No description provided for @attachmentImportProgress.
  ///
  /// In en, this message translates to:
  /// **'Importing attachments ({current}/{total})'**
  String attachmentImportProgress(int current, int total);

  /// No description provided for @attachmentImportResult.
  ///
  /// In en, this message translates to:
  /// **'Imported {imported}, Skipped {skipped}, Overwritten {overwritten}, Failed {failed}'**
  String attachmentImportResult(int imported, int skipped, int overwritten, int failed);

  /// No description provided for @attachmentImportFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to import attachments'**
  String get attachmentImportFailed;

  /// No description provided for @attachmentArchiveInfo.
  ///
  /// In en, this message translates to:
  /// **'{count} attachments, exported on {date}'**
  String attachmentArchiveInfo(int count, String date);

  /// No description provided for @attachmentStartImport.
  ///
  /// In en, this message translates to:
  /// **'Start Import'**
  String get attachmentStartImport;

  /// No description provided for @attachmentPreview.
  ///
  /// In en, this message translates to:
  /// **'Preview Attachments'**
  String get attachmentPreview;

  /// No description provided for @attachmentPreviewSubtitle.
  ///
  /// In en, this message translates to:
  /// **'{count} images in total'**
  String attachmentPreviewSubtitle(int count);

  /// No description provided for @attachmentPreviewEmpty.
  ///
  /// In en, this message translates to:
  /// **'No attachments'**
  String get attachmentPreviewEmpty;

  /// No description provided for @attachmentExportPreviewTitle.
  ///
  /// In en, this message translates to:
  /// **'Export Preview'**
  String get attachmentExportPreviewTitle;

  /// No description provided for @attachmentImportPreviewTitle.
  ///
  /// In en, this message translates to:
  /// **'Import Preview'**
  String get attachmentImportPreviewTitle;

  /// No description provided for @shortcutsGuide.
  ///
  /// In en, this message translates to:
  /// **'Shortcuts'**
  String get shortcutsGuide;

  /// No description provided for @shortcutsGuideDesc.
  ///
  /// In en, this message translates to:
  /// **'Quick access to voice, camera billing, etc.'**
  String get shortcutsGuideDesc;

  /// No description provided for @shortcutsIntroTitle.
  ///
  /// In en, this message translates to:
  /// **'Quick Billing'**
  String get shortcutsIntroTitle;

  /// No description provided for @shortcutsIntroDesc.
  ///
  /// In en, this message translates to:
  /// **'Use shortcuts to directly open voice billing, camera billing, and other features from your home screen without opening the app first.'**
  String get shortcutsIntroDesc;

  /// No description provided for @availableShortcuts.
  ///
  /// In en, this message translates to:
  /// **'Available Shortcuts'**
  String get availableShortcuts;



  /// No description provided for @shortcutImage.
  ///
  /// In en, this message translates to:
  /// **'Image Billing'**
  String get shortcutImage;

  /// No description provided for @shortcutImageDesc.
  ///
  /// In en, this message translates to:
  /// **'Recognize bills from gallery images'**
  String get shortcutImageDesc;

  /// No description provided for @shortcutCamera.
  ///
  /// In en, this message translates to:
  /// **'Camera Billing'**
  String get shortcutCamera;

  /// No description provided for @shortcutCameraDesc.
  ///
  /// In en, this message translates to:
  /// **'Take photos to recognize bills'**
  String get shortcutCameraDesc;

  /// No description provided for @shortcutNewExpense.
  ///
  /// In en, this message translates to:
  /// **'Quick Expense'**
  String get shortcutNewExpense;

  /// No description provided for @shortcutNewExpenseDesc.
  ///
  /// In en, this message translates to:
  /// **'Open expense entry page directly'**
  String get shortcutNewExpenseDesc;

  /// No description provided for @shortcutNewIncome.
  ///
  /// In en, this message translates to:
  /// **'Quick Income'**
  String get shortcutNewIncome;

  /// No description provided for @shortcutNewIncomeDesc.
  ///
  /// In en, this message translates to:
  /// **'Open income entry page directly'**
  String get shortcutNewIncomeDesc;

  /// No description provided for @shortcutNewTransfer.
  ///
  /// In en, this message translates to:
  /// **'Quick Transfer'**
  String get shortcutNewTransfer;

  /// No description provided for @shortcutNewTransferDesc.
  ///
  /// In en, this message translates to:
  /// **'Open transfer entry page directly'**
  String get shortcutNewTransferDesc;

  /// No description provided for @shortcutUrlCopied.
  ///
  /// In en, this message translates to:
  /// **'URL copied to clipboard'**
  String get shortcutUrlCopied;

  /// No description provided for @howToAddShortcut.
  ///
  /// In en, this message translates to:
  /// **'How to Add Shortcuts'**
  String get howToAddShortcut;

  /// No description provided for @iosShortcutStep1.
  ///
  /// In en, this message translates to:
  /// **'Open the Shortcuts app'**
  String get iosShortcutStep1;

  /// No description provided for @iosShortcutStep2.
  ///
  /// In en, this message translates to:
  /// **'Tap + in the upper right to create a new shortcut'**
  String get iosShortcutStep2;

  /// No description provided for @iosShortcutStep3.
  ///
  /// In en, this message translates to:
  /// **'Add \'Open URL\' action'**
  String get iosShortcutStep3;

  /// No description provided for @iosShortcutStep4.
  ///
  /// In en, this message translates to:
  /// **'Paste the copied URL (e.g., beecount://voice)'**
  String get iosShortcutStep4;

  /// No description provided for @iosShortcutStep5.
  ///
  /// In en, this message translates to:
  /// **'Save and add to home screen'**
  String get iosShortcutStep5;

  /// No description provided for @androidShortcutStep1.
  ///
  /// In en, this message translates to:
  /// **'Download a shortcut creator app (e.g., Shortcut Maker)'**
  String get androidShortcutStep1;

  /// No description provided for @androidShortcutStep2.
  ///
  /// In en, this message translates to:
  /// **'Select \'URL Shortcut\''**
  String get androidShortcutStep2;

  /// No description provided for @androidShortcutStep3.
  ///
  /// In en, this message translates to:
  /// **'Paste the copied URL (e.g., beecount://voice)'**
  String get androidShortcutStep3;

  /// No description provided for @androidShortcutStep4.
  ///
  /// In en, this message translates to:
  /// **'Set icon and name, then add to home screen'**
  String get androidShortcutStep4;

  /// No description provided for @shortcutsTip.
  ///
  /// In en, this message translates to:
  /// **'Tips'**
  String get shortcutsTip;

  /// No description provided for @shortcutsTipDesc.
  ///
  /// In en, this message translates to:
  /// **'Shortcuts require AI features. Make sure AI is enabled and API Key is configured.'**
  String get shortcutsTipDesc;

  /// No description provided for @shortcutOpenShortcutsApp.
  ///
  /// In en, this message translates to:
  /// **'Open Shortcuts App'**
  String get shortcutOpenShortcutsApp;

  /// No description provided for @shortcutAutoAdd.
  ///
  /// In en, this message translates to:
  /// **'Auto Billing API'**
  String get shortcutAutoAdd;

  /// No description provided for @shortcutAutoAddDesc.
  ///
  /// In en, this message translates to:
  /// **'Automatically create bills via URL parameters. Works great with Shortcuts and automation tools.'**
  String get shortcutAutoAddDesc;

  /// No description provided for @shortcutAutoAddExample.
  ///
  /// In en, this message translates to:
  /// **'Example URL:'**
  String get shortcutAutoAddExample;

  /// No description provided for @shortcutAutoAddParams.
  ///
  /// In en, this message translates to:
  /// **'Supported parameters:'**
  String get shortcutAutoAddParams;

  /// No description provided for @shortcutParamAmount.
  ///
  /// In en, this message translates to:
  /// **'Amount (required)'**
  String get shortcutParamAmount;

  /// No description provided for @shortcutParamType.
  ///
  /// In en, this message translates to:
  /// **'Type: expense / income / transfer'**
  String get shortcutParamType;

  /// No description provided for @shortcutParamCategory.
  ///
  /// In en, this message translates to:
  /// **'Category name (must match existing category)'**
  String get shortcutParamCategory;

  /// No description provided for @shortcutParamNote.
  ///
  /// In en, this message translates to:
  /// **'Note'**
  String get shortcutParamNote;

  /// No description provided for @shortcutParamAccount.
  ///
  /// In en, this message translates to:
  /// **'Account name (must match existing account)'**
  String get shortcutParamAccount;

  /// No description provided for @shortcutParamTags.
  ///
  /// In en, this message translates to:
  /// **'Tags (comma separated)'**
  String get shortcutParamTags;

  /// No description provided for @shortcutParamDate.
  ///
  /// In en, this message translates to:
  /// **'Date (ISO format, e.g., 2024-01-15)'**
  String get shortcutParamDate;

  /// No description provided for @quickActionImage.
  ///
  /// In en, this message translates to:
  /// **'Photo Billing'**
  String get quickActionImage;

  /// No description provided for @quickActionCamera.
  ///
  /// In en, this message translates to:
  /// **'Camera Billing'**
  String get quickActionCamera;


  /// No description provided for @quickActionAiChat.
  ///
  /// In en, this message translates to:
  /// **'AI Assistant'**
  String get quickActionAiChat;

  /// No description provided for @calendarTitle.
  ///
  /// In en, this message translates to:
  /// **'Calendar'**
  String get calendarTitle;

  /// No description provided for @calendarToday.
  ///
  /// In en, this message translates to:
  /// **'Today'**
  String get calendarToday;

  /// No description provided for @calendarNoTransactions.
  ///
  /// In en, this message translates to:
  /// **'No transactions'**
  String get calendarNoTransactions;

  /// No description provided for @calendarAddTransaction.
  ///
  /// In en, this message translates to:
  /// **'Add entry on this day'**
  String get calendarAddTransaction;

  /// No description provided for @calendarAddTransactionTooltip.
  ///
  /// In en, this message translates to:
  /// **'Add a record on the selected day'**
  String get calendarAddTransactionTooltip;

  /// No description provided for @commonUncategorized.
  ///
  /// In en, this message translates to:
  /// **'Uncategorized'**
  String get commonUncategorized;

  /// No description provided for @commonSaved.
  ///
  /// In en, this message translates to:
  /// **'Saved'**
  String get commonSaved;










































































  /// No description provided for @appLockTitle.
  ///
  /// In en, this message translates to:
  /// **'App Lock'**
  String get appLockTitle;

  /// No description provided for @appLockDesc.
  ///
  /// In en, this message translates to:
  /// **'PIN & biometric to protect privacy'**
  String get appLockDesc;

  /// No description provided for @appLockEnable.
  ///
  /// In en, this message translates to:
  /// **'Enable App Lock'**
  String get appLockEnable;

  /// No description provided for @appLockEnableDesc.
  ///
  /// In en, this message translates to:
  /// **'Require authentication on launch and resume'**
  String get appLockEnableDesc;

  /// No description provided for @appLockSetPin.
  ///
  /// In en, this message translates to:
  /// **'Set PIN'**
  String get appLockSetPin;

  /// No description provided for @appLockChangePin.
  ///
  /// In en, this message translates to:
  /// **'Change PIN'**
  String get appLockChangePin;

  /// No description provided for @appLockVerifyPin.
  ///
  /// In en, this message translates to:
  /// **'Verify PIN'**
  String get appLockVerifyPin;

  /// No description provided for @appLockVerifyCurrentPin.
  ///
  /// In en, this message translates to:
  /// **'Enter current PIN'**
  String get appLockVerifyCurrentPin;

  /// No description provided for @appLockSetNewPin.
  ///
  /// In en, this message translates to:
  /// **'Set new PIN'**
  String get appLockSetNewPin;

  /// No description provided for @appLockConfirmPin.
  ///
  /// In en, this message translates to:
  /// **'Confirm PIN'**
  String get appLockConfirmPin;

  /// No description provided for @appLockEnterPin.
  ///
  /// In en, this message translates to:
  /// **'Enter PIN'**
  String get appLockEnterPin;

  /// No description provided for @appLockPinSetSuccess.
  ///
  /// In en, this message translates to:
  /// **'PIN set successfully'**
  String get appLockPinSetSuccess;

  /// No description provided for @appLockDisabled.
  ///
  /// In en, this message translates to:
  /// **'App Lock disabled'**
  String get appLockDisabled;

  /// No description provided for @appLockBiometric.
  ///
  /// In en, this message translates to:
  /// **'Biometric Unlock'**
  String get appLockBiometric;

  /// No description provided for @appLockBiometricDesc.
  ///
  /// In en, this message translates to:
  /// **'Use Face ID or fingerprint to unlock'**
  String get appLockBiometricDesc;

  /// No description provided for @appLockBiometricReason.
  ///
  /// In en, this message translates to:
  /// **'Verify identity to unlock Bee Accounting'**
  String get appLockBiometricReason;

  /// No description provided for @appLockTimeout.
  ///
  /// In en, this message translates to:
  /// **'Auto-lock Timeout'**
  String get appLockTimeout;

  /// No description provided for @appLockTimeoutImmediate.
  ///
  /// In en, this message translates to:
  /// **'Immediately'**
  String get appLockTimeoutImmediate;

  /// No description provided for @appLockTimeout1Min.
  ///
  /// In en, this message translates to:
  /// **'After 1 minute'**
  String get appLockTimeout1Min;

  /// No description provided for @appLockTimeout5Min.
  ///
  /// In en, this message translates to:
  /// **'After 5 minutes'**
  String get appLockTimeout5Min;

  /// No description provided for @appLockTimeout15Min.
  ///
  /// In en, this message translates to:
  /// **'After 15 minutes'**
  String get appLockTimeout15Min;

  /// No description provided for @creditCardSettings.
  ///
  /// In en, this message translates to:
  /// **'Credit Card Settings'**
  String get creditCardSettings;

  /// No description provided for @accountTabValuation.
  ///
  /// In en, this message translates to:
  /// **'Valuation'**
  String get accountTabValuation;

  /// No description provided for @creditCardDaysRequired.
  ///
  /// In en, this message translates to:
  /// **'Please select billing & due dates'**
  String get creditCardDaysRequired;

  /// No description provided for @creditLimit.
  ///
  /// In en, this message translates to:
  /// **'Credit Limit'**
  String get creditLimit;

  /// No description provided for @creditLimitHint.
  ///
  /// In en, this message translates to:
  /// **'Enter credit limit'**
  String get creditLimitHint;

  /// No description provided for @billingDay.
  ///
  /// In en, this message translates to:
  /// **'Billing Day'**
  String get billingDay;

  /// No description provided for @paymentDueDay.
  ///
  /// In en, this message translates to:
  /// **'Payment Due Day'**
  String get paymentDueDay;

  /// No description provided for @creditUsed.
  ///
  /// In en, this message translates to:
  /// **'Used'**
  String get creditUsed;

  /// No description provided for @creditAvailable.
  ///
  /// In en, this message translates to:
  /// **'Est. available'**
  String get creditAvailable;

  /// No description provided for @creditCardOwed.
  ///
  /// In en, this message translates to:
  /// **'Current debt'**
  String get creditCardOwed;

  /// No description provided for @dayOfMonth.
  ///
  /// In en, this message translates to:
  /// **'{day}th of each month'**
  String dayOfMonth(int day);

  /// No description provided for @creditCardReminderTitle.
  ///
  /// In en, this message translates to:
  /// **'Payment Reminder'**
  String get creditCardReminderTitle;

  /// No description provided for @creditCardReminderDesc.
  ///
  /// In en, this message translates to:
  /// **'Remind before payment due date'**
  String get creditCardReminderDesc;

  /// No description provided for @creditCardReminderDaysBefore.
  ///
  /// In en, this message translates to:
  /// **'{days} days before'**
  String creditCardReminderDaysBefore(int days);

  /// No description provided for @creditCardInitialBalanceHint.
  ///
  /// In en, this message translates to:
  /// **'Current debt (negative number)'**
  String get creditCardInitialBalanceHint;

  /// No description provided for @selectDay.
  ///
  /// In en, this message translates to:
  /// **'Select Day'**
  String get selectDay;

  /// No description provided for @accountBankName.
  ///
  /// In en, this message translates to:
  /// **'Bank'**
  String get accountBankName;

  /// No description provided for @accountBankNameHint.
  ///
  /// In en, this message translates to:
  /// **'e.g. ICBC'**
  String get accountBankNameHint;

  /// No description provided for @accountCardLastFour.
  ///
  /// In en, this message translates to:
  /// **'Last 4 Digits'**
  String get accountCardLastFour;

  /// No description provided for @accountCardLastFourHint.
  ///
  /// In en, this message translates to:
  /// **'e.g. 1234'**
  String get accountCardLastFourHint;

  /// No description provided for @accountNote.
  ///
  /// In en, this message translates to:
  /// **'Note'**
  String get accountNote;

  /// No description provided for @accountNoteHint.
  ///
  /// In en, this message translates to:
  /// **'Add a note'**
  String get accountNoteHint;

  /// No description provided for @accountMetaInfo.
  ///
  /// In en, this message translates to:
  /// **'Account Info'**
  String get accountMetaInfo;

  /// No description provided for @accountBalanceTrend.
  ///
  /// In en, this message translates to:
  /// **'Balance Trend'**
  String get accountBalanceTrend;

  /// No description provided for @accountCategoryBreakdown.
  ///
  /// In en, this message translates to:
  /// **'Category Breakdown'**
  String get accountCategoryBreakdown;

  /// No description provided for @accountCategoryExpense.
  ///
  /// In en, this message translates to:
  /// **'Expense'**
  String get accountCategoryExpense;

  /// No description provided for @accountCategoryIncome.
  ///
  /// In en, this message translates to:
  /// **'Income'**
  String get accountCategoryIncome;

  /// No description provided for @accountNoMoreData.
  ///
  /// In en, this message translates to:
  /// **'No more data'**
  String get accountNoMoreData;

  /// No description provided for @totalAssets.
  ///
  /// In en, this message translates to:
  /// **'Total Assets'**
  String get totalAssets;

  /// No description provided for @totalLiabilities.
  ///
  /// In en, this message translates to:
  /// **'Total Liabilities'**
  String get totalLiabilities;

  /// No description provided for @assetAccounts.
  ///
  /// In en, this message translates to:
  /// **'Asset Accounts'**
  String get assetAccounts;

  /// No description provided for @liabilityAccounts.
  ///
  /// In en, this message translates to:
  /// **'Liability Accounts'**
  String get liabilityAccounts;

  /// No description provided for @assetComposition.
  ///
  /// In en, this message translates to:
  /// **'Asset Composition'**
  String get assetComposition;

  /// No description provided for @accountTypeInvestment.
  ///
  /// In en, this message translates to:
  /// **'Investment'**
  String get accountTypeInvestment;

  /// No description provided for @accountTypeLoan.
  ///
  /// In en, this message translates to:
  /// **'Loan'**
  String get accountTypeLoan;

  /// No description provided for @accountTypeReceivable.
  ///
  /// In en, this message translates to:
  /// **'Receivable'**
  String get accountTypeReceivable;

  /// No description provided for @accountTypeRealEstate.
  ///
  /// In en, this message translates to:
  /// **'Real Estate'**
  String get accountTypeRealEstate;

  /// No description provided for @accountTypeVehicle.
  ///
  /// In en, this message translates to:
  /// **'Vehicle'**
  String get accountTypeVehicle;

  /// No description provided for @accountTypeInsurance.
  ///
  /// In en, this message translates to:
  /// **'Insurance'**
  String get accountTypeInsurance;

  /// No description provided for @accountTypeSocialFund.
  ///
  /// In en, this message translates to:
  /// **'Social Fund'**
  String get accountTypeSocialFund;

  /// No description provided for @valuationCurrentValue.
  ///
  /// In en, this message translates to:
  /// **'Current Valuation'**
  String get valuationCurrentValue;

  /// No description provided for @valuationCurrentDebt.
  ///
  /// In en, this message translates to:
  /// **'Current Debt'**
  String get valuationCurrentDebt;

  /// No description provided for @valuationUpdateValue.
  ///
  /// In en, this message translates to:
  /// **'Update Valuation'**
  String get valuationUpdateValue;

  /// No description provided for @valuationUpdateDebt.
  ///
  /// In en, this message translates to:
  /// **'Update Debt'**
  String get valuationUpdateDebt;

  /// No description provided for @valuationLastUpdated.
  ///
  /// In en, this message translates to:
  /// **'Last updated: {date}'**
  String valuationLastUpdated(String date);

  /// No description provided for @valuationAccountHint.
  ///
  /// In en, this message translates to:
  /// **'Enter current valuation'**
  String get valuationAccountHint;

  /// No description provided for @valuationDebtHint.
  ///
  /// In en, this message translates to:
  /// **'Enter current debt amount'**
  String get valuationDebtHint;

  /// No description provided for @accountGroupTradable.
  ///
  /// In en, this message translates to:
  /// **'Daily Accounts'**
  String get accountGroupTradable;

  /// No description provided for @accountGroupValuation.
  ///
  /// In en, this message translates to:
  /// **'Assets/Liabilities'**
  String get accountGroupValuation;

  /// No description provided for @creditCardBillingInfo.
  ///
  /// In en, this message translates to:
  /// **'Bills on {billingDay}th · Due on {paymentDueDay}th'**
  String creditCardBillingInfo(int billingDay, int paymentDueDay);

  /// No description provided for @creditCardDaysUntilPayment.
  ///
  /// In en, this message translates to:
  /// **'{days} days until payment due'**
  String creditCardDaysUntilPayment(int days);

  /// No description provided for @creditCardPaymentDueToday.
  ///
  /// In en, this message translates to:
  /// **'Payment due today'**
  String get creditCardPaymentDueToday;

  /// No description provided for @creditCardQuickRepay.
  ///
  /// In en, this message translates to:
  /// **'Record Repayment'**
  String get creditCardQuickRepay;

  /// No description provided for @budgetManagement.
  ///
  /// In en, this message translates to:
  /// **'Budget'**
  String get budgetManagement;

  /// No description provided for @budgetManagementDesc.
  ///
  /// In en, this message translates to:
  /// **'Set monthly budget and control spending'**
  String get budgetManagementDesc;

  /// No description provided for @budgetSetupHint.
  ///
  /// In en, this message translates to:
  /// **'Set a budget to control monthly spending'**
  String get budgetSetupHint;

  /// No description provided for @budgetSetupAction.
  ///
  /// In en, this message translates to:
  /// **'Set up'**
  String get budgetSetupAction;



























  /// No description provided for @twofaChallengeTitle.
  ///
  /// In en, this message translates to:
  /// **'Two-factor authentication'**
  String get twofaChallengeTitle;

  /// No description provided for @twofaMethodTotp.
  ///
  /// In en, this message translates to:
  /// **'Code'**
  String get twofaMethodTotp;

  /// No description provided for @twofaMethodRecovery.
  ///
  /// In en, this message translates to:
  /// **'Recovery code'**
  String get twofaMethodRecovery;

  /// No description provided for @twofaTotpHint.
  ///
  /// In en, this message translates to:
  /// **'Enter the 6-digit code from your authenticator app (Google Authenticator / 1Password / Authy / etc).'**
  String get twofaTotpHint;

  /// No description provided for @twofaRecoveryHint.
  ///
  /// In en, this message translates to:
  /// **'Enter a recovery code saved when you enabled 2FA (e.g. abcd-efgh). Each code works once.'**
  String get twofaRecoveryHint;

  /// No description provided for @twofaTotpInputPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'6-digit code'**
  String get twofaTotpInputPlaceholder;

  /// No description provided for @twofaRecoveryInputPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Recovery code'**
  String get twofaRecoveryInputPlaceholder;

  /// No description provided for @twofaCountdown.
  ///
  /// In en, this message translates to:
  /// **'Time remaining {time}'**
  String twofaCountdown(String time);

  /// No description provided for @twofaVerifyButton.
  ///
  /// In en, this message translates to:
  /// **'Verify'**
  String get twofaVerifyButton;

  /// No description provided for @twofaStatusTitle.
  ///
  /// In en, this message translates to:
  /// **'Two-factor authentication'**
  String get twofaStatusTitle;

  /// No description provided for @twofaStatusEnabled.
  ///
  /// In en, this message translates to:
  /// **'Enabled ✓'**
  String get twofaStatusEnabled;

  /// No description provided for @twofaStatusDisabled.
  ///
  /// In en, this message translates to:
  /// **'Not enabled'**
  String get twofaStatusDisabled;

  /// No description provided for @twofaStatusEnabledAt.
  ///
  /// In en, this message translates to:
  /// **'Enabled on {date}'**
  String twofaStatusEnabledAt(String date);

  /// No description provided for @twofaStatusManageHint.
  ///
  /// In en, this message translates to:
  /// **'Manage from the Web app (enable / disable / regenerate recovery codes)'**
  String get twofaStatusManageHint;

  /// No description provided for @twofaStatusOpenWeb.
  ///
  /// In en, this message translates to:
  /// **'Enable on the Web app →'**
  String get twofaStatusOpenWeb;




  /// No description provided for @commonCopied.
  ///
  /// In en, this message translates to:
  /// **'Copied'**
  String get commonCopied;

  /// No description provided for @commonRemove.
  ///
  /// In en, this message translates to:
  /// **'Remove'**
  String get commonRemove;


























































  /// No description provided for @maintenanceOrphanCleanupTitle.
  ///
  /// In en, this message translates to:
  /// **'Data Cleanup'**
  String get maintenanceOrphanCleanupTitle;

  /// No description provided for @maintenanceOrphanCleanupSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Detect and clean local orphan data'**
  String get maintenanceOrphanCleanupSubtitle;

  /// No description provided for @maintenanceOrphanRescan.
  ///
  /// In en, this message translates to:
  /// **'Rescan'**
  String get maintenanceOrphanRescan;

  /// No description provided for @maintenanceOrphanEmpty.
  ///
  /// In en, this message translates to:
  /// **'Local data is clean, no orphan data found'**
  String get maintenanceOrphanEmpty;

  /// No description provided for @maintenanceOrphanGroupDb.
  ///
  /// In en, this message translates to:
  /// **'Database orphans'**
  String get maintenanceOrphanGroupDb;

  /// No description provided for @maintenanceOrphanGroupFile.
  ///
  /// In en, this message translates to:
  /// **'File orphans'**
  String get maintenanceOrphanGroupFile;

  /// No description provided for @maintenanceOrphanGroupSync.
  ///
  /// In en, this message translates to:
  /// **'Sync state orphans'**
  String get maintenanceOrphanGroupSync;

  /// No description provided for @maintenanceOrphanSummary.
  ///
  /// In en, this message translates to:
  /// **'Found {count} issue(s)'**
  String maintenanceOrphanSummary(int count);

  /// No description provided for @maintenanceOrphanSummarySize.
  ///
  /// In en, this message translates to:
  /// **'Reclaimable space ~ {size}'**
  String maintenanceOrphanSummarySize(String size);

  /// No description provided for @maintenanceOrphanSelectAll.
  ///
  /// In en, this message translates to:
  /// **'Select all'**
  String get maintenanceOrphanSelectAll;

  /// No description provided for @maintenanceOrphanDeselectAll.
  ///
  /// In en, this message translates to:
  /// **'Deselect all'**
  String get maintenanceOrphanDeselectAll;

  /// No description provided for @maintenanceOrphanDeleteOne.
  ///
  /// In en, this message translates to:
  /// **'Delete this'**
  String get maintenanceOrphanDeleteOne;

  /// No description provided for @maintenanceOrphanSelectedHint.
  ///
  /// In en, this message translates to:
  /// **'{count} selected'**
  String maintenanceOrphanSelectedHint(int count);

  /// No description provided for @maintenanceOrphanCleanSelected.
  ///
  /// In en, this message translates to:
  /// **'Clean selected'**
  String get maintenanceOrphanCleanSelected;

  /// No description provided for @maintenanceOrphanConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Confirm cleanup'**
  String get maintenanceOrphanConfirmTitle;

  /// No description provided for @maintenanceOrphanConfirmDeleteOne.
  ///
  /// In en, this message translates to:
  /// **'Delete \"{title}\"? This cannot be undone.'**
  String maintenanceOrphanConfirmDeleteOne(String title);

  /// No description provided for @maintenanceOrphanConfirmDeleteBatch.
  ///
  /// In en, this message translates to:
  /// **'Delete the {count} selected item(s)? This cannot be undone.'**
  String maintenanceOrphanConfirmDeleteBatch(int count);

  /// No description provided for @maintenanceOrphanCleanSuccess.
  ///
  /// In en, this message translates to:
  /// **'Cleaned {count} item(s)'**
  String maintenanceOrphanCleanSuccess(int count);

  /// No description provided for @maintenanceOrphanCleanPartial.
  ///
  /// In en, this message translates to:
  /// **'Cleaned {ok} item(s), {fail} failed'**
  String maintenanceOrphanCleanPartial(int ok, int fail);



  /// No description provided for @exchangeRatePageTitle.
  ///
  /// In en, this message translates to:
  /// **'Exchange Rates'**
  String get exchangeRatePageTitle;

  /// No description provided for @exchangeRateEntrySubtitle.
  ///
  /// In en, this message translates to:
  /// **'Auto-fetched rates with manual override'**
  String get exchangeRateEntrySubtitle;

  /// No description provided for @baseCurrencyLabel.
  ///
  /// In en, this message translates to:
  /// **'Primary Currency'**
  String get baseCurrencyLabel;

  /// No description provided for @rateSourceAuto.
  ///
  /// In en, this message translates to:
  /// **'Auto'**
  String get rateSourceAuto;

  /// No description provided for @rateSourceManual.
  ///
  /// In en, this message translates to:
  /// **'Manual'**
  String get rateSourceManual;

  /// No description provided for @rateUpdatedAt.
  ///
  /// In en, this message translates to:
  /// **'Updated {date}'**
  String rateUpdatedAt(String date);

  /// No description provided for @rateNotFetched.
  ///
  /// In en, this message translates to:
  /// **'Not fetched'**
  String get rateNotFetched;

  /// No description provided for @rateTapToSet.
  ///
  /// In en, this message translates to:
  /// **'Tap to set manually'**
  String get rateTapToSet;

  /// No description provided for @rateEditTitle.
  ///
  /// In en, this message translates to:
  /// **'Edit Rate'**
  String get rateEditTitle;

  /// No description provided for @rateInverseHint.
  ///
  /// In en, this message translates to:
  /// **'Inverse: 1 {base} ≈ {rate} {quote}'**
  String rateInverseHint(String base, String rate, String quote);

  /// No description provided for @rateResetToAuto.
  ///
  /// In en, this message translates to:
  /// **'Reset to auto'**
  String get rateResetToAuto;

  /// No description provided for @rateRefreshSuccess.
  ///
  /// In en, this message translates to:
  /// **'Rates updated'**
  String get rateRefreshSuccess;

  /// No description provided for @rateRefreshFailed.
  ///
  /// In en, this message translates to:
  /// **'Fetch failed, you can set rates manually'**
  String get rateRefreshFailed;

  /// No description provided for @ratesEmptyHint.
  ///
  /// In en, this message translates to:
  /// **'Rates appear here once your accounts use different currencies'**
  String get ratesEmptyHint;

  /// No description provided for @rateDisclaimer.
  ///
  /// In en, this message translates to:
  /// **'Source: open exchange-rate data, updated daily. Conversion is for reference only and may differ from bank rates.'**
  String get rateDisclaimer;

  /// No description provided for @convertedNetWorth.
  ///
  /// In en, this message translates to:
  /// **'Net worth (in {currency})'**
  String convertedNetWorth(String currency);

  /// No description provided for @convertedFootnote.
  ///
  /// In en, this message translates to:
  /// **'Converted at {date} rates, tap to manage'**
  String convertedFootnote(String date);

  /// No description provided for @convertedPartialWarning.
  ///
  /// In en, this message translates to:
  /// **'{currencies} not converted, tap to set rates'**
  String convertedPartialWarning(String currencies);

  /// No description provided for @unconvertedBadge.
  ///
  /// In en, this message translates to:
  /// **'Not converted'**
  String get unconvertedBadge;

  /// No description provided for @commonDetail.
  ///
  /// In en, this message translates to:
  /// **'Detail'**
  String get commonDetail;

  /// No description provided for @conversionDetailTitle.
  ///
  /// In en, this message translates to:
  /// **'Conversion Details'**
  String get conversionDetailTitle;

  /// No description provided for @assetConversionToggle.
  ///
  /// In en, this message translates to:
  /// **'Convert to primary currency'**
  String get assetConversionToggle;

  /// No description provided for @rateManualApplied.
  ///
  /// In en, this message translates to:
  /// **'Applied {count} manual rates'**
  String rateManualApplied(int count);

  /// No description provided for @netWorthTrendTitle.
  ///
  /// In en, this message translates to:
  /// **'Net Worth Trend'**
  String get netWorthTrendTitle;

  /// No description provided for @netWorthTrend3M.
  ///
  /// In en, this message translates to:
  /// **'3M'**
  String get netWorthTrend3M;

  /// No description provided for @netWorthTrend6M.
  ///
  /// In en, this message translates to:
  /// **'6M'**
  String get netWorthTrend6M;

  /// No description provided for @netWorthTrend12M.
  ///
  /// In en, this message translates to:
  /// **'12M'**
  String get netWorthTrend12M;

  /// No description provided for @netWorthTrendAll.
  ///
  /// In en, this message translates to:
  /// **'All'**
  String get netWorthTrendAll;

  /// No description provided for @netWorthTrendLineNet.
  ///
  /// In en, this message translates to:
  /// **'Net Worth'**
  String get netWorthTrendLineNet;

  /// No description provided for @netWorthTrendLineAssets.
  ///
  /// In en, this message translates to:
  /// **'Total Assets'**
  String get netWorthTrendLineAssets;

  /// No description provided for @netWorthTrendLineLiabilities.
  ///
  /// In en, this message translates to:
  /// **'Total Liabilities'**
  String get netWorthTrendLineLiabilities;

  /// No description provided for @netWorthTrendMultiCurrencyNote.
  ///
  /// In en, this message translates to:
  /// **'Historical net worth is the raw sum of each currency, not converted'**
  String get netWorthTrendMultiCurrencyNote;

  /// No description provided for @txFlagExcludeFromStats.
  ///
  /// In en, this message translates to:
  /// **'Exclude from income/expense'**
  String get txFlagExcludeFromStats;

  /// No description provided for @txFlagExcludeFromBudget.
  ///
  /// In en, this message translates to:
  /// **'Exclude from budget'**
  String get txFlagExcludeFromBudget;

  /// No description provided for @txFlagMoreOptions.
  ///
  /// In en, this message translates to:
  /// **'More options'**
  String get txFlagMoreOptions;

  /// No description provided for @txFlagDialogTitle.
  ///
  /// In en, this message translates to:
  /// **'Transaction flags'**
  String get txFlagDialogTitle;

  /// No description provided for @txFlagExcludeFromStatsHint.
  ///
  /// In en, this message translates to:
  /// **'Excluded from stats, still counts toward balance'**
  String get txFlagExcludeFromStatsHint;

  /// No description provided for @txFlagExcludeFromBudgetHint.
  ///
  /// In en, this message translates to:
  /// **'Doesn\'t count against your budget'**
  String get txFlagExcludeFromBudgetHint;

  /// No description provided for @txFlagExcludedTag.
  ///
  /// In en, this message translates to:
  /// **'Excluded'**
  String get txFlagExcludedTag;

  /// No description provided for @txFlagBudgetExcludedTag.
  ///
  /// In en, this message translates to:
  /// **'No budget'**
  String get txFlagBudgetExcludedTag;

  /// No description provided for @txCurrencyLabel.
  ///
  /// In en, this message translates to:
  /// **'Currency'**
  String get txCurrencyLabel;

  /// No description provided for @txRateLabel.
  ///
  /// In en, this message translates to:
  /// **'Rate'**
  String get txRateLabel;

  /// No description provided for @transactionDoubleTapHint.
  ///
  /// In en, this message translates to:
  /// **'Double tap'**
  String get transactionDoubleTapHint;

  /// No description provided for @transactionSavedNextCategory.
  ///
  /// In en, this message translates to:
  /// **'Saved. Choose another category.'**
  String get transactionSavedNextCategory;

  /// No description provided for @txConvertedPreview.
  ///
  /// In en, this message translates to:
  /// **'≈ {amount} {currency}'**
  String txConvertedPreview(Object amount, Object currency);

  /// No description provided for @txRateMissingHint.
  ///
  /// In en, this message translates to:
  /// **'Please enter the rate for this entry before saving'**
  String get txRateMissingHint;

  /// No description provided for @txCrossCurrencyTransferBlocked.
  ///
  /// In en, this message translates to:
  /// **'Cross-currency transfers are not supported yet. Record two entries or use same-currency accounts.'**
  String get txCrossCurrencyTransferBlocked;

  /// No description provided for @ledgerBaseCurrencyLabel.
  ///
  /// In en, this message translates to:
  /// **'Primary currency'**
  String get ledgerBaseCurrencyLabel;

  /// No description provided for @statsConvertedFootnote.
  ///
  /// In en, this message translates to:
  /// **'Includes foreign currency, converted to {currency} at entry-time rates'**
  String statsConvertedFootnote(Object currency);

  /// No description provided for @ledgerCurrencyChangeRecalcHint.
  ///
  /// In en, this message translates to:
  /// **'Changing the base currency will reconvert all history at current rates'**
  String get ledgerCurrencyChangeRecalcHint;

  /// No description provided for @recalcForeignTxBanner.
  ///
  /// In en, this message translates to:
  /// **'Unconverted foreign-currency transactions detected in this ledger'**
  String get recalcForeignTxBanner;

  /// No description provided for @recalcForeignTxAction.
  ///
  /// In en, this message translates to:
  /// **'Reconvert at current rates'**
  String get recalcForeignTxAction;

  /// No description provided for @recalcForeignTxDone.
  ///
  /// In en, this message translates to:
  /// **'Reconverted {count} foreign-currency transactions'**
  String recalcForeignTxDone(Object count);

  /// No description provided for @txCurrencyPickerTitle.
  ///
  /// In en, this message translates to:
  /// **'Select currency'**
  String get txCurrencyPickerTitle;

  /// No description provided for @recalcSyncCountHint.
  ///
  /// In en, this message translates to:
  /// **'{count} transactions will be reconverted and synced'**
  String recalcSyncCountHint(Object count);

  /// No description provided for @exportCsvHeaderCurrency.
  ///
  /// In en, this message translates to:
  /// **'Currency'**
  String get exportCsvHeaderCurrency;

  /// No description provided for @importFieldCurrency.
  ///
  /// In en, this message translates to:
  /// **'Currency'**
  String get importFieldCurrency;

  /// No description provided for @currencyMOP.
  ///
  /// In en, this message translates to:
  /// **'Macau Pataca'**
  String get currencyMOP;

  /// No description provided for @currencyMNT.
  ///
  /// In en, this message translates to:
  /// **'Mongolian Tughrik'**
  String get currencyMNT;

  /// No description provided for @currencyKPW.
  ///
  /// In en, this message translates to:
  /// **'North Korean Won'**
  String get currencyKPW;

  /// No description provided for @currencyKHR.
  ///
  /// In en, this message translates to:
  /// **'Cambodian Riel'**
  String get currencyKHR;

  /// No description provided for @currencyLAK.
  ///
  /// In en, this message translates to:
  /// **'Lao Kip'**
  String get currencyLAK;

  /// No description provided for @currencyBND.
  ///
  /// In en, this message translates to:
  /// **'Bruneian Dollar'**
  String get currencyBND;

  /// No description provided for @currencyNPR.
  ///
  /// In en, this message translates to:
  /// **'Nepalese Rupee'**
  String get currencyNPR;

  /// No description provided for @currencyBTN.
  ///
  /// In en, this message translates to:
  /// **'Bhutanese Ngultrum'**
  String get currencyBTN;

  /// No description provided for @currencyMVR.
  ///
  /// In en, this message translates to:
  /// **'Maldivian Rufiyaa'**
  String get currencyMVR;

  /// No description provided for @currencyAFN.
  ///
  /// In en, this message translates to:
  /// **'Afghan Afghani'**
  String get currencyAFN;

  /// No description provided for @currencyUZS.
  ///
  /// In en, this message translates to:
  /// **'Uzbekistani Som'**
  String get currencyUZS;

  /// No description provided for @currencyTJS.
  ///
  /// In en, this message translates to:
  /// **'Tajikistani Somoni'**
  String get currencyTJS;

  /// No description provided for @currencyTMT.
  ///
  /// In en, this message translates to:
  /// **'Turkmenistani Manat'**
  String get currencyTMT;

  /// No description provided for @currencyKGS.
  ///
  /// In en, this message translates to:
  /// **'Kyrgyzstani Som'**
  String get currencyKGS;

  /// No description provided for @currencyQAR.
  ///
  /// In en, this message translates to:
  /// **'Qatari Riyal'**
  String get currencyQAR;

  /// No description provided for @currencyKWD.
  ///
  /// In en, this message translates to:
  /// **'Kuwaiti Dinar'**
  String get currencyKWD;

  /// No description provided for @currencyBHD.
  ///
  /// In en, this message translates to:
  /// **'Bahraini Dinar'**
  String get currencyBHD;

  /// No description provided for @currencyOMR.
  ///
  /// In en, this message translates to:
  /// **'Omani Rial'**
  String get currencyOMR;

  /// No description provided for @currencyJOD.
  ///
  /// In en, this message translates to:
  /// **'Jordanian Dinar'**
  String get currencyJOD;

  /// No description provided for @currencyLBP.
  ///
  /// In en, this message translates to:
  /// **'Lebanese Pound'**
  String get currencyLBP;

  /// No description provided for @currencyIQD.
  ///
  /// In en, this message translates to:
  /// **'Iraqi Dinar'**
  String get currencyIQD;

  /// No description provided for @currencyIRR.
  ///
  /// In en, this message translates to:
  /// **'Iranian Rial'**
  String get currencyIRR;

  /// No description provided for @currencyYER.
  ///
  /// In en, this message translates to:
  /// **'Yemeni Rial'**
  String get currencyYER;

  /// No description provided for @currencySYP.
  ///
  /// In en, this message translates to:
  /// **'Syrian Pound'**
  String get currencySYP;

  /// No description provided for @currencyGEL.
  ///
  /// In en, this message translates to:
  /// **'Georgian Lari'**
  String get currencyGEL;

  /// No description provided for @currencyAMD.
  ///
  /// In en, this message translates to:
  /// **'Armenian Dram'**
  String get currencyAMD;

  /// No description provided for @currencyAZN.
  ///
  /// In en, this message translates to:
  /// **'Azerbaijan Manat'**
  String get currencyAZN;

  /// No description provided for @currencyRON.
  ///
  /// In en, this message translates to:
  /// **'Romanian Leu'**
  String get currencyRON;

  /// No description provided for @currencyBGN.
  ///
  /// In en, this message translates to:
  /// **'Bulgarian Lev'**
  String get currencyBGN;

  /// No description provided for @currencyRSD.
  ///
  /// In en, this message translates to:
  /// **'Serbian Dinar'**
  String get currencyRSD;

  /// No description provided for @currencyISK.
  ///
  /// In en, this message translates to:
  /// **'Icelandic Krona'**
  String get currencyISK;

  /// No description provided for @currencyMDL.
  ///
  /// In en, this message translates to:
  /// **'Moldovan Leu'**
  String get currencyMDL;

  /// No description provided for @currencyALL.
  ///
  /// In en, this message translates to:
  /// **'Albanian Lek'**
  String get currencyALL;

  /// No description provided for @currencyMKD.
  ///
  /// In en, this message translates to:
  /// **'Macedonian Denar'**
  String get currencyMKD;

  /// No description provided for @currencyBAM.
  ///
  /// In en, this message translates to:
  /// **'Bosnian Convertible Mark'**
  String get currencyBAM;

  /// No description provided for @currencyGIP.
  ///
  /// In en, this message translates to:
  /// **'Gibraltar Pound'**
  String get currencyGIP;

  /// No description provided for @currencyGTQ.
  ///
  /// In en, this message translates to:
  /// **'Guatemalan Quetzal'**
  String get currencyGTQ;

  /// No description provided for @currencyHNL.
  ///
  /// In en, this message translates to:
  /// **'Honduran Lempira'**
  String get currencyHNL;

  /// No description provided for @currencyNIO.
  ///
  /// In en, this message translates to:
  /// **'Nicaraguan Cordoba'**
  String get currencyNIO;

  /// No description provided for @currencyCRC.
  ///
  /// In en, this message translates to:
  /// **'Costa Rican Colon'**
  String get currencyCRC;

  /// No description provided for @currencyPAB.
  ///
  /// In en, this message translates to:
  /// **'Panamanian Balboa'**
  String get currencyPAB;

  /// No description provided for @currencyDOP.
  ///
  /// In en, this message translates to:
  /// **'Dominican Peso'**
  String get currencyDOP;

  /// No description provided for @currencyCUP.
  ///
  /// In en, this message translates to:
  /// **'Cuban Peso'**
  String get currencyCUP;

  /// No description provided for @currencyJMD.
  ///
  /// In en, this message translates to:
  /// **'Jamaican Dollar'**
  String get currencyJMD;

  /// No description provided for @currencyTTD.
  ///
  /// In en, this message translates to:
  /// **'Trinidadian Dollar'**
  String get currencyTTD;

  /// No description provided for @currencyBSD.
  ///
  /// In en, this message translates to:
  /// **'Bahamian Dollar'**
  String get currencyBSD;

  /// No description provided for @currencyBBD.
  ///
  /// In en, this message translates to:
  /// **'Barbadian or Bajan Dollar'**
  String get currencyBBD;

  /// No description provided for @currencyBZD.
  ///
  /// In en, this message translates to:
  /// **'Belizean Dollar'**
  String get currencyBZD;

  /// No description provided for @currencyHTG.
  ///
  /// In en, this message translates to:
  /// **'Haitian Gourde'**
  String get currencyHTG;

  /// No description provided for @currencyXCD.
  ///
  /// In en, this message translates to:
  /// **'East Caribbean Dollar'**
  String get currencyXCD;

  /// No description provided for @currencyKYD.
  ///
  /// In en, this message translates to:
  /// **'Caymanian Dollar'**
  String get currencyKYD;

  /// No description provided for @currencyAWG.
  ///
  /// In en, this message translates to:
  /// **'Aruban or Dutch Guilder'**
  String get currencyAWG;

  /// No description provided for @currencyANG.
  ///
  /// In en, this message translates to:
  /// **'Dutch Guilder'**
  String get currencyANG;

  /// No description provided for @currencyBMD.
  ///
  /// In en, this message translates to:
  /// **'Bermudian Dollar'**
  String get currencyBMD;

  /// No description provided for @currencyUYU.
  ///
  /// In en, this message translates to:
  /// **'Uruguayan Peso'**
  String get currencyUYU;

  /// No description provided for @currencyPYG.
  ///
  /// In en, this message translates to:
  /// **'Paraguayan Guarani'**
  String get currencyPYG;

  /// No description provided for @currencyBOB.
  ///
  /// In en, this message translates to:
  /// **'Bolivian Bolíviano'**
  String get currencyBOB;

  /// No description provided for @currencyVES.
  ///
  /// In en, this message translates to:
  /// **'Venezuelan Bolívar'**
  String get currencyVES;

  /// No description provided for @currencyGYD.
  ///
  /// In en, this message translates to:
  /// **'Guyanese Dollar'**
  String get currencyGYD;

  /// No description provided for @currencySRD.
  ///
  /// In en, this message translates to:
  /// **'Surinamese Dollar'**
  String get currencySRD;

  /// No description provided for @currencyFJD.
  ///
  /// In en, this message translates to:
  /// **'Fijian Dollar'**
  String get currencyFJD;

  /// No description provided for @currencyPGK.
  ///
  /// In en, this message translates to:
  /// **'Papua New Guinean Kina'**
  String get currencyPGK;

  /// No description provided for @currencySBD.
  ///
  /// In en, this message translates to:
  /// **'Solomon Islander Dollar'**
  String get currencySBD;

  /// No description provided for @currencyTOP.
  ///
  /// In en, this message translates to:
  /// **'Tongan Pa\'anga'**
  String get currencyTOP;

  /// No description provided for @currencyVUV.
  ///
  /// In en, this message translates to:
  /// **'Ni-Vanuatu Vatu'**
  String get currencyVUV;

  /// No description provided for @currencyWST.
  ///
  /// In en, this message translates to:
  /// **'Samoan Tala'**
  String get currencyWST;

  /// No description provided for @currencyXPF.
  ///
  /// In en, this message translates to:
  /// **'CFP Franc'**
  String get currencyXPF;

  /// No description provided for @currencyKES.
  ///
  /// In en, this message translates to:
  /// **'Kenyan Shilling'**
  String get currencyKES;

  /// No description provided for @currencyGHS.
  ///
  /// In en, this message translates to:
  /// **'Ghanaian Cedi'**
  String get currencyGHS;

  /// No description provided for @currencyMAD.
  ///
  /// In en, this message translates to:
  /// **'Moroccan Dirham'**
  String get currencyMAD;

  /// No description provided for @currencyDZD.
  ///
  /// In en, this message translates to:
  /// **'Algerian Dinar'**
  String get currencyDZD;

  /// No description provided for @currencyTND.
  ///
  /// In en, this message translates to:
  /// **'Tunisian Dinar'**
  String get currencyTND;

  /// No description provided for @currencyLYD.
  ///
  /// In en, this message translates to:
  /// **'Libyan Dinar'**
  String get currencyLYD;

  /// No description provided for @currencyETB.
  ///
  /// In en, this message translates to:
  /// **'Ethiopian Birr'**
  String get currencyETB;

  /// No description provided for @currencyUGX.
  ///
  /// In en, this message translates to:
  /// **'Ugandan Shilling'**
  String get currencyUGX;

  /// No description provided for @currencyTZS.
  ///
  /// In en, this message translates to:
  /// **'Tanzanian Shilling'**
  String get currencyTZS;

  /// No description provided for @currencyRWF.
  ///
  /// In en, this message translates to:
  /// **'Rwandan Franc'**
  String get currencyRWF;

  /// No description provided for @currencyXAF.
  ///
  /// In en, this message translates to:
  /// **'Central African CFA Franc'**
  String get currencyXAF;

  /// No description provided for @currencyXOF.
  ///
  /// In en, this message translates to:
  /// **'West African CFA Franc'**
  String get currencyXOF;

  /// No description provided for @currencyMUR.
  ///
  /// In en, this message translates to:
  /// **'Mauritian Rupee'**
  String get currencyMUR;

  /// No description provided for @currencyBWP.
  ///
  /// In en, this message translates to:
  /// **'Botswana Pula'**
  String get currencyBWP;

  /// No description provided for @currencyNAD.
  ///
  /// In en, this message translates to:
  /// **'Namibian Dollar'**
  String get currencyNAD;

  /// No description provided for @currencyZMW.
  ///
  /// In en, this message translates to:
  /// **'Zambian Kwacha'**
  String get currencyZMW;

  /// No description provided for @currencyMWK.
  ///
  /// In en, this message translates to:
  /// **'Malawian Kwacha'**
  String get currencyMWK;

  /// No description provided for @currencyMZN.
  ///
  /// In en, this message translates to:
  /// **'Mozambican Metical'**
  String get currencyMZN;

  /// No description provided for @currencyAOA.
  ///
  /// In en, this message translates to:
  /// **'Angolan Kwanza'**
  String get currencyAOA;

  /// No description provided for @currencyCDF.
  ///
  /// In en, this message translates to:
  /// **'Congolese Franc'**
  String get currencyCDF;

  /// No description provided for @currencyGMD.
  ///
  /// In en, this message translates to:
  /// **'Gambian Dalasi'**
  String get currencyGMD;

  /// No description provided for @currencyGNF.
  ///
  /// In en, this message translates to:
  /// **'Guinean Franc'**
  String get currencyGNF;

  /// No description provided for @currencyLRD.
  ///
  /// In en, this message translates to:
  /// **'Liberian Dollar'**
  String get currencyLRD;

  /// No description provided for @currencySLE.
  ///
  /// In en, this message translates to:
  /// **'Sierra Leonean Leone'**
  String get currencySLE;

  /// No description provided for @currencySDG.
  ///
  /// In en, this message translates to:
  /// **'Sudanese Pound'**
  String get currencySDG;

  /// No description provided for @currencySSP.
  ///
  /// In en, this message translates to:
  /// **'South Sudanese Pound'**
  String get currencySSP;

  /// No description provided for @currencySOS.
  ///
  /// In en, this message translates to:
  /// **'Somali Shilling'**
  String get currencySOS;

  /// No description provided for @currencyDJF.
  ///
  /// In en, this message translates to:
  /// **'Djiboutian Franc'**
  String get currencyDJF;

  /// No description provided for @currencyERN.
  ///
  /// In en, this message translates to:
  /// **'Eritrean Nakfa'**
  String get currencyERN;

  /// No description provided for @currencyBIF.
  ///
  /// In en, this message translates to:
  /// **'Burundian Franc'**
  String get currencyBIF;

  /// No description provided for @currencyCVE.
  ///
  /// In en, this message translates to:
  /// **'Cape Verdean Escudo'**
  String get currencyCVE;

  /// No description provided for @currencySTN.
  ///
  /// In en, this message translates to:
  /// **'Sao Tomean Dobra'**
  String get currencySTN;

  /// No description provided for @currencySCR.
  ///
  /// In en, this message translates to:
  /// **'Seychellois Rupee'**
  String get currencySCR;

  /// No description provided for @currencyKMF.
  ///
  /// In en, this message translates to:
  /// **'Comorian Franc'**
  String get currencyKMF;

  /// No description provided for @currencyLSL.
  ///
  /// In en, this message translates to:
  /// **'Basotho Loti'**
  String get currencyLSL;

  /// No description provided for @currencySZL.
  ///
  /// In en, this message translates to:
  /// **'Swazi Lilangeni'**
  String get currencySZL;

  /// No description provided for @currencyMGA.
  ///
  /// In en, this message translates to:
  /// **'Malagasy Ariary'**
  String get currencyMGA;

  /// No description provided for @currencyMRU.
  ///
  /// In en, this message translates to:
  /// **'Mauritanian Ouguiya'**
  String get currencyMRU;







































  /// No description provided for @annualReportInsightsTitle.
  ///
  /// In en, this message translates to:
  /// **'Annual Insights'**
  String get annualReportInsightsTitle;

  /// No description provided for @annualReportInsightsSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Discover your spending habits through data'**
  String get annualReportInsightsSubtitle;

  /// No description provided for @annualReportAveragePerRecord.
  ///
  /// In en, this message translates to:
  /// **'Average Spending per Record'**
  String get annualReportAveragePerRecord;

  /// No description provided for @annualReportAveragePerRecordDesc.
  ///
  /// In en, this message translates to:
  /// **'Average amount per bookkeeping record'**
  String get annualReportAveragePerRecordDesc;

  /// No description provided for @annualReportDailyAverage.
  ///
  /// In en, this message translates to:
  /// **'Daily Average Spending'**
  String get annualReportDailyAverage;

  /// No description provided for @annualReportDailyAverageDesc.
  ///
  /// In en, this message translates to:
  /// **'Average amount spent each day'**
  String get annualReportDailyAverageDesc;

  /// No description provided for @annualReportMonthlyAverage.
  ///
  /// In en, this message translates to:
  /// **'Monthly Average Spending'**
  String get annualReportMonthlyAverage;

  /// No description provided for @annualReportMonthlyAverageDesc.
  ///
  /// In en, this message translates to:
  /// **'Average amount spent each month'**
  String get annualReportMonthlyAverageDesc;

  /// No description provided for @annualReportBusiestMonth.
  ///
  /// In en, this message translates to:
  /// **'Most Active Month'**
  String get annualReportBusiestMonth;

  /// No description provided for @annualReportBusiestMonthDesc.
  ///
  /// In en, this message translates to:
  /// **'Month with the most bookkeeping activity'**
  String get annualReportBusiestMonthDesc;

  /// No description provided for @annualReportCategoryCount.
  ///
  /// In en, this message translates to:
  /// **'Spending Categories'**
  String get annualReportCategoryCount;

  /// No description provided for @annualReportCategoryCountDesc.
  ///
  /// In en, this message translates to:
  /// **'Number of spending categories used'**
  String get annualReportCategoryCountDesc;

  /// No description provided for @annualReportCategoryCountValue.
  ///
  /// In en, this message translates to:
  /// **'{count, plural, one{1 category} other{{count} categories}}'**
  String annualReportCategoryCountValue(int count);

  /// No description provided for @annualReportSavingsRate.
  ///
  /// In en, this message translates to:
  /// **'Savings Rate'**
  String get annualReportSavingsRate;

  /// No description provided for @annualReportSavingsRatePositive.
  ///
  /// In en, this message translates to:
  /// **'The share of your income saved this year'**
  String get annualReportSavingsRatePositive;

  /// No description provided for @annualReportSavingsRateNegative.
  ///
  /// In en, this message translates to:
  /// **'Spending exceeded income this year'**
  String get annualReportSavingsRateNegative;

  /// No description provided for @annualReportComparisonTitle.
  ///
  /// In en, this message translates to:
  /// **'Income vs. Expense'**
  String get annualReportComparisonTitle;

  /// No description provided for @annualReportComparisonSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Compare monthly income and expenses'**
  String get annualReportComparisonSubtitle;

  /// No description provided for @annualReportHighestIncome.
  ///
  /// In en, this message translates to:
  /// **'Highest Income'**
  String get annualReportHighestIncome;

  /// No description provided for @annualReportHighestExpense.
  ///
  /// In en, this message translates to:
  /// **'Highest Expense'**
  String get annualReportHighestExpense;

  /// No description provided for @annualReportConsistency.
  ///
  /// In en, this message translates to:
  /// **'Bookkeeping Streak'**
  String get annualReportConsistency;

  /// No description provided for @annualReportDaysValue.
  ///
  /// In en, this message translates to:
  /// **'{days, plural, one{1 day} other{{days} days}}'**
  String annualReportDaysValue(int days);

  /// No description provided for @annualReportSavingsPositive.
  ///
  /// In en, this message translates to:
  /// **'Congratulations on your savings'**
  String get annualReportSavingsPositive;

  /// No description provided for @annualReportSavingsNegative.
  ///
  /// In en, this message translates to:
  /// **'Spending exceeded income this year'**
  String get annualReportSavingsNegative;

  /// No description provided for @annualReportMonthlyExpenseTrend.
  ///
  /// In en, this message translates to:
  /// **'Monthly Spending Trend'**
  String get annualReportMonthlyExpenseTrend;

  /// No description provided for @annualReportAchieved.
  ///
  /// In en, this message translates to:
  /// **'Achieved'**
  String get annualReportAchieved;

  /// No description provided for @annualReportNotAchieved.
  ///
  /// In en, this message translates to:
  /// **'Not Achieved'**
  String get annualReportNotAchieved;

  /// No description provided for @annualReportDownloadHint.
  ///
  /// In en, this message translates to:
  /// **'Scan to download BeeCount and start bookkeeping'**
  String get annualReportDownloadHint;

  /// No description provided for @attachmentKeepOriginal.
  ///
  /// In en, this message translates to:
  /// **'Keep Original Attachments'**
  String get attachmentKeepOriginal;

  /// No description provided for @attachmentKeepOriginalDesc.
  ///
  /// In en, this message translates to:
  /// **'Uses more storage and sync data'**
  String get attachmentKeepOriginalDesc;

  /// No description provided for @transactionCopyAction.
  ///
  /// In en, this message translates to:
  /// **'Copy as new transaction'**
  String get transactionCopyAction;

  /// No description provided for @transactionCopyUnavailable.
  ///
  /// In en, this message translates to:
  /// **'This transaction cannot be copied. Check the ledger and your access.'**
  String get transactionCopyUnavailable;





  /// No description provided for @creditCardReminderDueSoonTitle.
  ///
  /// In en, this message translates to:
  /// **'{accountName} payment due soon'**
  String creditCardReminderDueSoonTitle(String accountName);

  /// No description provided for @creditCardReminderDueBody.
  ///
  /// In en, this message translates to:
  /// **'Payment due on day {paymentDueDay} of each month, please repay on time'**
  String creditCardReminderDueBody(int paymentDueDay);

  /// No description provided for @reminderBackupChannelName.
  ///
  /// In en, this message translates to:
  /// **'Recording reminder (backup)'**
  String get reminderBackupChannelName;

  /// No description provided for @reminderBackupChannelDescription.
  ///
  /// In en, this message translates to:
  /// **'Backup channel for recording reminders'**
  String get reminderBackupChannelDescription;




  /// No description provided for @appLinkOpenImageBilling.
  ///
  /// In en, this message translates to:
  /// **'Open image billing'**
  String get appLinkOpenImageBilling;

  /// No description provided for @appLinkOpenCameraBilling.
  ///
  /// In en, this message translates to:
  /// **'Open camera billing'**
  String get appLinkOpenCameraBilling;

  /// No description provided for @appLinkOpenAiAssistant.
  ///
  /// In en, this message translates to:
  /// **'Open AI assistant'**
  String get appLinkOpenAiAssistant;

  /// No description provided for @appLinkOpenManualBilling.
  ///
  /// In en, this message translates to:
  /// **'Open manual billing'**
  String get appLinkOpenManualBilling;

  /// No description provided for @appLinkNoTargetPage.
  ///
  /// In en, this message translates to:
  /// **'No target page provided'**
  String get appLinkNoTargetPage;

  /// No description provided for @appLinkOpenPage.
  ///
  /// In en, this message translates to:
  /// **'Open page: {page}'**
  String appLinkOpenPage(String page);

  /// No description provided for @appLinkUnknownAction.
  ///
  /// In en, this message translates to:
  /// **'Unknown action: {action}'**
  String appLinkUnknownAction(String action);

  /// No description provided for @appLinkNoLedger.
  ///
  /// In en, this message translates to:
  /// **'Select a ledger first'**
  String get appLinkNoLedger;

  /// No description provided for @appLinkMissingAmount.
  ///
  /// In en, this message translates to:
  /// **'Not recorded: enter a valid amount'**
  String get appLinkMissingAmount;

  /// No description provided for @appLinkMissingCategory.
  ///
  /// In en, this message translates to:
  /// **'Not recorded: specify a category'**
  String get appLinkMissingCategory;

  /// No description provided for @appLinkCategoryNotFound.
  ///
  /// In en, this message translates to:
  /// **'Not recorded: category \"{name}\" does not exist'**
  String appLinkCategoryNotFound(String name);

  /// No description provided for @appLinkTypeIncome.
  ///
  /// In en, this message translates to:
  /// **'Income'**
  String get appLinkTypeIncome;

  /// No description provided for @appLinkTypeExpense.
  ///
  /// In en, this message translates to:
  /// **'Expense'**
  String get appLinkTypeExpense;

  /// No description provided for @appLinkTypeTransfer.
  ///
  /// In en, this message translates to:
  /// **'Transfer'**
  String get appLinkTypeTransfer;

  /// No description provided for @appLinkRecorded.
  ///
  /// In en, this message translates to:
  /// **'Recorded {type} {amount}'**
  String appLinkRecorded(String type, String amount);

  /// No description provided for @appLinkRecordSuccess.
  ///
  /// In en, this message translates to:
  /// **'Transaction recorded'**
  String get appLinkRecordSuccess;

  /// No description provided for @appLinkParamError.
  ///
  /// In en, this message translates to:
  /// **'Invalid parameters: {message}'**
  String appLinkParamError(String message);

  /// No description provided for @appLinkRecordFailed.
  ///
  /// In en, this message translates to:
  /// **'Recording failed: {error}'**
  String appLinkRecordFailed(String error);

  /// No description provided for @appLinkTextDone.
  ///
  /// In en, this message translates to:
  /// **'Text processing done'**
  String get appLinkTextDone;

  /// No description provided for @appLinkTextFailed.
  ///
  /// In en, this message translates to:
  /// **'Text billing failed: {error}'**
  String appLinkTextFailed(String error);

  /// No description provided for @appLinkNoText.
  ///
  /// In en, this message translates to:
  /// **'No text content provided'**
  String get appLinkNoText;

  /// No description provided for @orphanBudgetTitle.
  ///
  /// In en, this message translates to:
  /// **'Budget #{id}'**
  String orphanBudgetTitle(String id);

  /// No description provided for @orphanBudgetNoLedger.
  ///
  /// In en, this message translates to:
  /// **'{type} · ¥{amount} · ledger deleted (id={id})'**
  String orphanBudgetNoLedger(String type, String amount, String id);

  /// No description provided for @orphanAttachmentRowTitle.
  ///
  /// In en, this message translates to:
  /// **'Attachment row #{id}'**
  String orphanAttachmentRowTitle(String id);

  /// No description provided for @orphanAttachmentNoTx.
  ///
  /// In en, this message translates to:
  /// **'{fileName} · transaction deleted (txId={id})'**
  String orphanAttachmentNoTx(String fileName, String id);

  /// No description provided for @orphanTagLinkTitle.
  ///
  /// In en, this message translates to:
  /// **'Tag link #{id}'**
  String orphanTagLinkTitle(String id);

  /// No description provided for @orphanTagLinkNoTx.
  ///
  /// In en, this message translates to:
  /// **'transaction deleted (txId={txId}, tagId={tagId})'**
  String orphanTagLinkNoTx(String txId, String tagId);

  /// No description provided for @orphanTagLinkNoTag.
  ///
  /// In en, this message translates to:
  /// **'tag deleted (txId={txId}, tagId={tagId})'**
  String orphanTagLinkNoTag(String txId, String tagId);

  /// No description provided for @orphanTxTitle.
  ///
  /// In en, this message translates to:
  /// **'Transaction #{id}'**
  String orphanTxTitle(String id);

  /// No description provided for @orphanTxNoMissing.
  ///
  /// In en, this message translates to:
  /// **'{type} · ¥{amount} · {missing} deleted'**
  String orphanTxNoMissing(String type, String amount, String missing);

  /// No description provided for @orphanTxNoCategory.
  ///
  /// In en, this message translates to:
  /// **'{type} · ¥{amount} · category deleted (categoryId={id})'**
  String orphanTxNoCategory(String type, String amount, String id);

  /// No description provided for @orphanSubcategoryTitle.
  ///
  /// In en, this message translates to:
  /// **'Subcategory \"{name}\" #{id}'**
  String orphanSubcategoryTitle(String name, String id);

  /// No description provided for @orphanSubcategoryNoParent.
  ///
  /// In en, this message translates to:
  /// **'{kind} · parent category deleted (parentId={id})'**
  String orphanSubcategoryNoParent(String kind, String id);

  /// No description provided for @orphanBudgetNoCategory.
  ///
  /// In en, this message translates to:
  /// **'{type} · ¥{amount} · category deleted (categoryId={id})'**
  String orphanBudgetNoCategory(String type, String amount, String id);

  /// No description provided for @orphanSharedSubcategoryTitle.
  ///
  /// In en, this message translates to:
  /// **'Shared subcategory \"{name}\"'**
  String orphanSharedSubcategoryTitle(String name);

  /// No description provided for @orphanSharedSubcategoryNoParent.
  ///
  /// In en, this message translates to:
  /// **'parent category deleted (parentSyncId={id})'**
  String orphanSharedSubcategoryNoParent(String id);

  /// No description provided for @orphanSharedTagOverrideTitle.
  ///
  /// In en, this message translates to:
  /// **'Shared tag override'**
  String get orphanSharedTagOverrideTitle;

  /// No description provided for @orphanSharedTagNoTx.
  ///
  /// In en, this message translates to:
  /// **'transaction deleted (txSyncId={txId}, tagSyncId={tagId})'**
  String orphanSharedTagNoTx(String txId, String tagId);

  /// No description provided for @orphanFileNoRef.
  ///
  /// In en, this message translates to:
  /// **'Attachment file has no DB reference'**
  String get orphanFileNoRef;

  /// No description provided for @orphanCustomIconNoRef.
  ///
  /// In en, this message translates to:
  /// **'Category custom icon has no DB reference'**
  String get orphanCustomIconNoRef;

  /// No description provided for @orphanSharedIconNoRef.
  ///
  /// In en, this message translates to:
  /// **'Shared category icon cache has no DB reference'**
  String get orphanSharedIconNoRef;

  /// No description provided for @orphanSyncChangeTitle.
  ///
  /// In en, this message translates to:
  /// **'Sync change #{id}'**
  String orphanSyncChangeTitle(String id);

  /// No description provided for @orphanSyncChangeNoEntity.
  ///
  /// In en, this message translates to:
  /// **'{entityType} · {action} · entity deleted (syncId={id})'**
  String orphanSyncChangeNoEntity(String entityType, String action, String id);

  /// No description provided for @customIconFileMissing.
  ///
  /// In en, this message translates to:
  /// **'Image file does not exist'**
  String get customIconFileMissing;

  /// No description provided for @customIconFileTooLarge.
  ///
  /// In en, this message translates to:
  /// **'Image file is too large (max 5MB)'**
  String get customIconFileTooLarge;

  /// No description provided for @customIconUnsupportedFormat.
  ///
  /// In en, this message translates to:
  /// **'Unsupported image format'**
  String get customIconUnsupportedFormat;

  /// No description provided for @customIconCompressFailed.
  ///
  /// In en, this message translates to:
  /// **'Image compression failed'**
  String get customIconCompressFailed;

  /// No description provided for @customIconSaveFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save icon: {error}'**
  String customIconSaveFailed(String error);

  /// No description provided for @sharePosterAppText.
  ///
  /// In en, this message translates to:
  /// **'BeeCount - Simple Ledger'**
  String get sharePosterAppText;

  /// No description provided for @sharePosterDefaultLedgerName.
  ///
  /// In en, this message translates to:
  /// **'Default Ledger'**
  String get sharePosterDefaultLedgerName;









  /// No description provided for @xlsxEmptyFile.
  ///
  /// In en, this message translates to:
  /// **'The Excel file is empty or unreadable'**
  String get xlsxEmptyFile;

  /// No description provided for @xlsxEmptySheet.
  ///
  /// In en, this message translates to:
  /// **'The worksheet is empty'**
  String get xlsxEmptySheet;

  /// No description provided for @xlsxParseFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to parse Excel file: {error}'**
  String xlsxParseFailed(String error);

  /// No description provided for @rateFetchAllSourcesFailed.
  ///
  /// In en, this message translates to:
  /// **'All sources failed: {errors}'**
  String rateFetchAllSourcesFailed(String errors);

  /// No description provided for @rateFetchSourcePayloadInvalid.
  ///
  /// In en, this message translates to:
  /// **'Unexpected payload from exchange rate source {source}'**
  String rateFetchSourcePayloadInvalid(String source);







  /// No description provided for @attachmentArchiveMissing.
  ///
  /// In en, this message translates to:
  /// **'The archive file does not exist'**
  String get attachmentArchiveMissing;

  /// No description provided for @attachmentArchiveInvalidFormat.
  ///
  /// In en, this message translates to:
  /// **'Invalid archive format: metadata.json is missing'**
  String get attachmentArchiveInvalidFormat;

  /// No description provided for @categoryPackageCompressFailed.
  ///
  /// In en, this message translates to:
  /// **'Compression failed'**
  String get categoryPackageCompressFailed;

  /// No description provided for @categoryPackageFileMissing.
  ///
  /// In en, this message translates to:
  /// **'The file does not exist'**
  String get categoryPackageFileMissing;

  /// No description provided for @categoryPackageMissingYaml.
  ///
  /// In en, this message translates to:
  /// **'Invalid category package: categories.yaml is missing'**
  String get categoryPackageMissingYaml;

  /// No description provided for @categoryPackageInvalidFormat.
  ///
  /// In en, this message translates to:
  /// **'Invalid configuration file format'**
  String get categoryPackageInvalidFormat;

  /// No description provided for @configExportInvalidYaml.
  ///
  /// In en, this message translates to:
  /// **'Invalid YAML format'**
  String get configExportInvalidYaml;

  /// No description provided for @configExportTransferCategoryMissing.
  ///
  /// In en, this message translates to:
  /// **'The transfer category does not exist'**
  String get configExportTransferCategoryMissing;

  /// No description provided for @configExportFileMissing.
  ///
  /// In en, this message translates to:
  /// **'The file does not exist: {path}'**
  String configExportFileMissing(String path);

  /// No description provided for @ledgerTargetIdExists.
  ///
  /// In en, this message translates to:
  /// **'Target ledger ID already exists: {id}'**
  String ledgerTargetIdExists(String id);

  /// No description provided for @ledgerMissing.
  ///
  /// In en, this message translates to:
  /// **'The ledger does not exist'**
  String get ledgerMissing;


  /// No description provided for @accountMissing.
  ///
  /// In en, this message translates to:
  /// **'The account does not exist'**
  String get accountMissing;



  /// No description provided for @logExportTitle.
  ///
  /// In en, this message translates to:
  /// **'=== BeeCount log export ==='**
  String get logExportTitle;

  /// No description provided for @logExportTime.
  ///
  /// In en, this message translates to:
  /// **'Exported at: {time}'**
  String logExportTime(String time);

  /// No description provided for @logExportCount.
  ///
  /// In en, this message translates to:
  /// **'Log entries: {count}'**
  String logExportCount(int count);

  /// No description provided for @creditCardOverpayment.
  ///
  /// In en, this message translates to:
  /// **'Overpayment'**
  String get creditCardOverpayment;

  /// No description provided for @creditCardDebtHint.
  ///
  /// In en, this message translates to:
  /// **'Enter your total outstanding debt as a positive amount; enter 0 if none.'**
  String get creditCardDebtHint;

  /// No description provided for @creditCardOverpaymentHint.
  ///
  /// In en, this message translates to:
  /// **'Enter the positive amount remaining on the card after overpaying.'**
  String get creditCardOverpaymentHint;

  /// No description provided for @creditCardAmountInvalid.
  ///
  /// In en, this message translates to:
  /// **'Enter a valid amount; credit-card debt or overpayment must be non-negative.'**
  String get creditCardAmountInvalid;

  /// No description provided for @creditCardReminderDueRequired.
  ///
  /// In en, this message translates to:
  /// **'Set a payment due day before enabling reminders.'**
  String get creditCardReminderDueRequired;

  /// No description provided for @creditCardEstimateHint.
  ///
  /// In en, this message translates to:
  /// **'Estimated from recorded transactions; check your bank for the actual available amount.'**
  String get creditCardEstimateHint;

  /// No description provided for @accountAllFlows.
  ///
  /// In en, this message translates to:
  /// **'All transactions'**
  String get accountAllFlows;

  /// No description provided for @accountOutflows.
  ///
  /// In en, this message translates to:
  /// **'Expenses & transfers out'**
  String get accountOutflows;

  /// No description provided for @accountInflows.
  ///
  /// In en, this message translates to:
  /// **'Income & transfers in'**
  String get accountInflows;

  /// No description provided for @positiveAssetBalance.
  ///
  /// In en, this message translates to:
  /// **'Positive asset balances'**
  String get positiveAssetBalance;

  /// No description provided for @negativeAssetBalanceHint.
  ///
  /// In en, this message translates to:
  /// **'Negative account balances are deducted from total assets.'**
  String get negativeAssetBalanceHint;

  /// No description provided for @importAccountCurrencyPreview.
  ///
  /// In en, this message translates to:
  /// **'Confirm account currencies'**
  String get importAccountCurrencyPreview;

  /// No description provided for @importAccountCurrencyPreviewHint.
  ///
  /// In en, this message translates to:
  /// **'Accounts use statement currencies. Conflicting currencies are split without changing existing accounts. Missing currencies follow an existing account, an unambiguous account currency, or the ledger currency.'**
  String get importAccountCurrencyPreviewHint;

  /// No description provided for @importCurrencyInvalid.
  ///
  /// In en, this message translates to:
  /// **'Invalid currency; correct this row:'**
  String get importCurrencyInvalid;

  /// No description provided for @transactionReceiptAction.
  ///
  /// In en, this message translates to:
  /// **'Generate receipt'**
  String get transactionReceiptAction;

  /// No description provided for @transactionReceiptTitle.
  ///
  /// In en, this message translates to:
  /// **'Transaction receipt'**
  String get transactionReceiptTitle;

  /// No description provided for @transactionReceiptFields.
  ///
  /// In en, this message translates to:
  /// **'Choose what appears on your receipt'**
  String get transactionReceiptFields;

  /// No description provided for @transactionReceiptLedger.
  ///
  /// In en, this message translates to:
  /// **'Ledger'**
  String get transactionReceiptLedger;

  /// No description provided for @transactionReceiptTags.
  ///
  /// In en, this message translates to:
  /// **'Tags'**
  String get transactionReceiptTags;

  /// No description provided for @transactionReceiptFooter.
  ///
  /// In en, this message translates to:
  /// **'Little moments, thoughtfully recorded'**
  String get transactionReceiptFooter;

  /// No description provided for @transactionReceiptUnavailable.
  ///
  /// In en, this message translates to:
  /// **'Unable to generate a receipt. Check that this transaction still exists.'**
  String get transactionReceiptUnavailable;

  /// No description provided for @transactionReceiptShareFailed.
  ///
  /// In en, this message translates to:
  /// **'Unable to share. Please try again.'**
  String get transactionReceiptShareFailed;
}

class _AppLocalizationsDelegate extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) => <String>['en', 'ko', 'zh'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {

  // Lookup logic when language+country codes are specified.
  switch (locale.languageCode) {
    case 'zh': {
  switch (locale.countryCode) {
    case 'TW': return AppLocalizationsZhTw();
   }
  break;
   }
  }

  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en': return AppLocalizationsEn();
    case 'ko': return AppLocalizationsKo();
    case 'zh': return AppLocalizationsZh();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.'
  );
}
