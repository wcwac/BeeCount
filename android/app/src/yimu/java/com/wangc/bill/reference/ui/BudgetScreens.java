package com.wangc.bill.reference.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.wangc.bill.reference.*;
import java.util.Calendar;
import java.util.List;
import org.json.JSONObject;

/** Complete local month/category/date-range budget routes using recovered layouts. */
public final class BudgetScreens {
    private static final String MONTH = "local_budget_month", SELECTED = "local_budget_id";
    private BudgetScreens() {}

    public static void render(ManagerActivity activity) {
        LocalBudgets budgets = new LocalBudgets(activity.store());
        long selected = activity.getIntent().getLongExtra(SELECTED, 0);
        JSONObject value = selected > 0 ? budgets.snapshot(selected)
                : budgets.month(activity.bookId(), activity.getIntent().getLongExtra(MONTH, System.currentTimeMillis()));
        if (value.optLong("book_id") != activity.bookId()) throw new IllegalArgumentException("预算属于其他账本，请返回重新选择");
        View root = activity.original("预算管理", "activity_budget_manager");
        String currency = value.optString("currency"); boolean monthly = value.optString("period_type").equals("month");
        Ui.text(root, "title", monthly ? "月预算" : value.optString("name"));
        Ui.text(root, "month", value.optInt("year") + "年" + value.optInt("month") + "月");
        Ui.show(root, "month", monthly); Ui.show(root, "self_date_layout", !monthly);
        Ui.text(root, "start_time", Ui.date(value.optLong("start_time"), "yyyy.MM.dd"));
        Ui.text(root, "end_time", Ui.date(value.optLong("end_time") - 1, "yyyy.MM.dd"));
        Ui.text(root, "month_budget", value.optBoolean("configured") ? Money.text(value.optLong("amount"), currency) : "点击设置");
        Ui.text(root, "month_pay", Money.text(value.optLong("spent"), currency));
        long remaining = value.optLong("remaining");
        Ui.text(root, "budget_surplus", value.optBoolean("configured") ? (remaining < 0 ? "超支 " : "") + Money.text(Math.abs(remaining), currency) : "未设置");
        TextView surplus = Ui.view(root, "budget_surplus");
        if (surplus != null) surplus.setTextColor(remaining < 0 ? Ui.RED : Ui.GREEN);
        long elapsed = elapsedDays(value.optLong("start_time"), value.optLong("end_time"));
        Ui.text(root, "day_pay", Money.text(value.optLong("spent") / elapsed, currency));
        // These labels otherwise promise a separate daily budget algorithm.
        Ui.hide(root, "day_data_layout", "budget_line_chart", "bill_line_title", "line_menu", "budget_line_time_layout");
        Ui.show(root, "budget_progress", value.optBoolean("configured"));
        progress(root, "progress_view_one", value.optLong("spent"), value.optLong("amount"));
        Ui.hide(root, "progress_view_two");
        Ui.click(root, "date_layout", () -> { if (monthly) chooseMonth(activity, value); else editCustom(activity, value); });
        Ui.click(root, "right_icon", () -> options(activity, budgets, value));
        Ui.click(root, "month_budget_layout", () -> { if (monthly) editMonth(activity, budgets, value); else editCustom(activity, value); });
        Ui.click(root, "month_pay_layout", () -> showBills(activity, budgets, value, 0, "预算支出明细"));
        Ui.click(root, "add_category_budget", () -> chooseCategory(activity, budgets, value));
        Ui.click(root, "tip", () -> new AlertDialog.Builder(activity).setTitle("预算统计口径")
                .setMessage("只统计本账本、所选日期内未删除且参与预算的账单。支出计金额及手续费，其他账单仅计手续费；不受“不计入收支”开关影响。\n父分类预算包含其子分类。父、子预算可分别设置，总预算独立计算，不把分类预算重复相加。\n退款按账单中保留的支出金额统计；不自动把回款收入抵扣预算。")
                .setPositiveButton("知道了", null).show());
        RecyclerView list = Ui.view(root, "category_budget_list");
        List<JSONObject> rows = value.optLong("id") > 0 ? budgets.categories(value.optLong("id")) : java.util.Collections.emptyList();
        Ui.show(root, "tip_layout", rows.isEmpty());
        CatalogScreens.list(list, rows, "item_category_budget", (item, category) -> {
            String name = category.optString("name") + (category.optString("level").equals(CategoryRepository.PARENT) ? "（含子分类）" : "");
            Ui.text(item, "category", name); Ui.image(item, "icon", category.optString("icon"));
            Ui.text(item, "budget_total", "预算 " + Money.text(category.optLong("amount"), currency));
            long left = category.optLong("remaining");
            Ui.text(item, "pay_num", "支出 " + Money.text(category.optLong("spent"), currency) + " · "
                    + (left < 0 ? "超支 " : "剩余 ") + Money.text(Math.abs(left), currency));
            TextView pay = Ui.view(item, "pay_num"); if (pay != null) pay.setTextColor(left < 0 ? Ui.RED : Ui.GREEN);
            Ui.hide(item, "child_category_budget", "progress_view_two");
            progress(item, "progress_view_one", category.optLong("spent"), category.optLong("amount"));
            item.setOnClickListener(view -> categoryOptions(activity, budgets, value, category));
        });
    }

    private static void options(ManagerActivity activity, LocalBudgets budgets, JSONObject selected) {
        String[] choices = selected.optString("period_type").equals("month")
                ? new String[]{"选择年月", "自定义日期预算", "新增日期预算", "清除本月预算"}
                : new String[]{"返回月预算", "自定义日期预算", "修改此预算", "删除此预算"};
        new AlertDialog.Builder(activity).setTitle("预算管理").setItems(choices, (dialog, index) -> {
            boolean monthly = selected.optString("period_type").equals("month");
            if (index == 0) { if (monthly) chooseMonth(activity, selected); else { activity.getIntent().removeExtra(SELECTED); activity.reload(); } }
            else if (index == 1) customList(activity, budgets);
            else if (index == 2) editCustom(activity, monthly ? null : selected);
            else if (selected.optLong("id") == 0) Ui.toast(activity, "本月尚未设置预算");
            else Ui.confirm(activity, "删除预算", "仅删除预算设置和所属分类预算，账单保持不变。", () -> {
                budgets.remove(selected.optLong("id")); activity.getIntent().removeExtra(SELECTED); activity.reload();
            });
        }).show();
    }

    private static void chooseMonth(ManagerActivity activity, JSONObject current) {
        Calendar calendar = Calendar.getInstance();
        if (current.optString("period_type").equals("month")) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.YEAR, current.optInt("year")); calendar.set(Calendar.MONTH, current.optInt("month") - 1);
        }
        new DatePickerDialog(activity, (picker, year, month, day) -> {
            try { long time = LocalBudgets.monthRange(year, month + 1)[0]; activity.getIntent().putExtra(MONTH, time); activity.getIntent().removeExtra(SELECTED); activity.reload(); }
            catch (Exception failure) { Ui.error(activity, failure); }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1).show();
    }

    private static void editMonth(ManagerActivity activity, LocalBudgets budgets, JSONObject value) {
        Dialogs.input(activity, value.optInt("year") + "年" + value.optInt("month") + "月预算", Money.text(value.optLong("amount"), value.optString("currency")), true, text -> {
            budgets.saveMonth(activity.bookId(), value.optInt("year"), value.optInt("month"), Money.signed(text, value.optString("currency")));
            activity.reload();
        });
    }

    private static void customList(ManagerActivity activity, LocalBudgets budgets) {
        LinearLayout rows = Ui.column(activity); List<JSONObject> values = budgets.customs(activity.bookId());
        if (values.isEmpty()) rows.addView(Ui.row(activity, "暂无日期预算", "例如旅行预算、装修预算，选择起止日期后统计。", null));
        AlertDialog dialog = new AlertDialog.Builder(activity).setTitle("自定义日期预算").setView(scroll(activity, rows))
                .setNegativeButton("返回", null).setPositiveButton("新增", (d, which) -> editCustom(activity, null)).create();
        for (JSONObject value : values) {
            View row = Ui.layout(activity, "item_budget_self"); String currency = value.optString("currency");
            Ui.show(row, "budget_name", true); Ui.text(row, "budget_name", value.optString("name"));
            Ui.text(row, "budget_time", Ui.date(value.optLong("start_time"), "yyyy.MM.dd") + " — " + Ui.date(value.optLong("end_time") - 1, "yyyy.MM.dd"));
            Ui.text(row, "budget_num", "预算 " + Money.text(value.optLong("amount"), currency));
            long left = value.optLong("remaining");
            Ui.text(row, "remain_budget_num", (left < 0 ? "超支 " : "剩余 ") + Money.text(Math.abs(left), currency));
            Ui.click(row, "book_edit", () -> { dialog.dismiss(); editCustom(activity, value); });
            row.setOnClickListener(view -> { dialog.dismiss(); activity.getIntent().putExtra(SELECTED, value.optLong("id")); activity.reload(); });
            rows.addView(row, new LinearLayout.LayoutParams(-1, -2));
        }
        dialog.show();
    }

    private static void editCustom(ManagerActivity activity, JSONObject old) {
        LocalBudgets budgets = new LocalBudgets(activity.store());
        LinearLayout fields = Ui.column(activity); int pad = Ui.dp(activity, 18); fields.setPadding(pad, pad, pad, pad);
        fields.addView(Ui.label(activity, "预算名称", 14)); EditText name = new EditText(activity); name.setSingleLine(true); name.setHint("例如旅行预算"); name.setText(old == null ? "" : old.optString("name")); fields.addView(name);
        fields.addView(Ui.label(activity, "预算金额（" + activity.currency() + "）", 14)); EditText amount = new EditText(activity);
        amount.setSingleLine(true); amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amount.setText(Money.text(old == null ? 0 : old.optLong("amount"), activity.currency())); fields.addView(amount);
        long[] month = Ui.month(System.currentTimeMillis());
        long[] dates = {old == null ? month[0] : old.optLong("start_time"), old == null ? month[1] : old.optLong("end_time")};
        TextView from = Ui.label(activity, "", 16), to = Ui.label(activity, "", 16); from.setPadding(0, pad, 0, pad); to.setPadding(0, pad, 0, pad); fields.addView(from); fields.addView(to);
        Runnable display = () -> { from.setText("开始日期：" + Ui.date(dates[0], "yyyy-MM-dd") + "　点击修改"); to.setText("结束日期：" + Ui.date(dates[1] - 1, "yyyy-MM-dd") + "　点击修改"); };
        display.run(); from.setOnClickListener(view -> pickDay(activity, dates[0], chosen -> { dates[0] = chosen; display.run(); }));
        to.setOnClickListener(view -> pickDay(activity, dates[1] - 1, chosen -> { Calendar next = Calendar.getInstance(); next.setTimeInMillis(chosen); next.add(Calendar.DATE, 1); dates[1] = next.getTimeInMillis(); display.run(); }));
        fields.addView(Ui.label(activity, "起止日期均包含在统计中。可为此日期预算继续添加分类限额；与月预算独立计算。", 13));
        AlertDialog dialog = new AlertDialog.Builder(activity).setTitle(old == null ? "新增日期预算" : "修改日期预算").setView(scroll(activity, fields))
                .setNegativeButton("取消", null).setPositiveButton("保存", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            try { long id = budgets.saveCustom(old == null ? 0 : old.optLong("id"), activity.bookId(), name.getText().toString(), dates[0], dates[1], Money.signed(amount.getText().toString(), activity.currency()));
                dialog.dismiss(); activity.getIntent().putExtra(SELECTED, id); activity.reload(); }
            catch (Exception failure) { Ui.error(activity, failure); }
        })); dialog.show();
    }

    private static void chooseCategory(ManagerActivity activity, LocalBudgets budgets, JSONObject value) {
        List<JSONObject> categories = budgets.availableCategories(activity.bookId());
        if (categories.isEmpty()) { Ui.toast(activity, "请先建立支出分类"); return; }
        String[] labels = new String[categories.size()];
        for (int i = 0; i < labels.length; i++) { JSONObject category = categories.get(i);
            labels[i] = category.optString("level").equals(CategoryRepository.CHILD)
                    ? category.optString("parent_name") + " / " + category.optString("name")
                    : category.optString("name") + (category.optString("level").equals(CategoryRepository.PARENT) ? "（含子分类）" : "");
        }
        new AlertDialog.Builder(activity).setTitle("添加或修改分类预算").setItems(labels, (dialog, which) -> {
            JSONObject selected = categories.get(which); JSONObject old = null;
            if (value.optLong("id") > 0) for (JSONObject row : budgets.categories(value.optLong("id"))) if (row.optLong("category_id") == selected.optLong("id")) { old = row; break; }
            long existing = old == null ? 0 : old.optLong("amount");
            Dialogs.input(activity, labels[which] + "预算", Money.text(existing, value.optString("currency")), true, text -> {
                long minor = Money.signed(text, value.optString("currency")); if (minor < 0) throw new IllegalArgumentException("预算不能为负数");
                AccountingCommitScope.run(activity.store(), () -> {
                    long id = value.optLong("id");
                    if (id == 0) id = budgets.saveMonth(activity.bookId(), value.optInt("year"), value.optInt("month"), value.optLong("amount"));
                    budgets.saveCategory(id, selected.optLong("id"), minor); return null;
                }); activity.reload();
            });
        }).show();
    }

    private static void categoryOptions(ManagerActivity activity, LocalBudgets budgets, JSONObject budget, JSONObject category) {
        new AlertDialog.Builder(activity).setTitle(category.optString("name")).setItems(new String[]{"查看所属账单", "修改预算", "删除分类预算"}, (dialog, which) -> {
            if (which == 0) showBills(activity, budgets, budget, category.optLong("category_id"), category.optString("name") + "预算明细");
            else if (which == 1) Dialogs.input(activity, "分类预算", Money.text(category.optLong("amount"), budget.optString("currency")), true, text -> {
                budgets.saveCategory(budget.optLong("id"), category.optLong("category_id"), Money.signed(text, budget.optString("currency"))); activity.reload();
            });
            else Ui.confirm(activity, "删除分类预算", "账单和总预算保持不变。", () -> { budgets.removeCategory(budget.optLong("id"), category.optLong("category_id")); activity.reload(); });
        }).show();
    }

    private static void showBills(ManagerActivity activity, LocalBudgets budgets, JSONObject budget, long category, String title) {
        try {
            List<JSONObject> bills = budget.optLong("id") == 0
                    ? budgets.billsForMonth(activity.bookId(), budget.optInt("year"), budget.optInt("month"), category)
                    : budgets.bills(budget.optLong("id"), category);
            if (bills.isEmpty()) { Ui.toast(activity, "所选预算范围内暂无计入预算的账单"); return; }
            RecyclerView list = new RecyclerView(activity); list.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(activity));
            list.setAdapter(new RecordAdapter(bills, null, row -> {
                JSONObject owner = AccountingLinks.ownerOfBill(activity.store(), row.optLong("id")); Ui.record(activity, owner == null ? row : owner);
            }));
            AlertDialog dialog = new AlertDialog.Builder(activity).setTitle(title + " · " + bills.size() + "笔").setView(list).setPositiveButton("关闭", null).create();
            dialog.setOnShowListener(ignored -> { if (dialog.getWindow() != null) dialog.getWindow().setLayout(-1, activity.getResources().getDisplayMetrics().heightPixels * 4 / 5); }); dialog.show();
        } catch (Exception failure) { Ui.error(activity, failure); }
    }

    private static ScrollView scroll(Context context, View contents) { ScrollView scroll = new ScrollView(context); scroll.addView(contents); return scroll; }
    private interface Day { void accept(long midnight); }
    private static void pickDay(Context context, long value, Day callback) {
        Calendar calendar = Calendar.getInstance(); calendar.setTimeInMillis(value);
        new DatePickerDialog(context, (picker, year, month, day) -> { Calendar result = Calendar.getInstance(); result.clear(); result.set(year, month, day); callback.accept(result.getTimeInMillis()); },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    private static long elapsedDays(long start, long end) {
        long through = Math.min(end, System.currentTimeMillis()); if (through <= start) return 1;
        // Date ranges can be long; use elapsed milliseconds only for the harmless average label.
        return Math.max(1, (through - start) / 86_400_000L + (through < end ? 1 : 0));
    }
    private static void progress(View root, String id, long spent, long budget) {
        View found = Ui.view(root, id); if (!(found instanceof FrameLayout group)) return;
        group.removeAllViews(); android.widget.ProgressBar bar = new android.widget.ProgressBar(root.getContext(), null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(1000); bar.setProgress(budget <= 0 ? (spent > 0 ? 1000 : 0) : (int) Math.min(1000d, spent * 1000d / budget));
        group.addView(bar, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
}
