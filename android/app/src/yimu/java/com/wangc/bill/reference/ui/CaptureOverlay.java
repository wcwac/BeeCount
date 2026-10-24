package com.wangc.bill.reference.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wangc.bill.auto.x1;
import com.wangc.bill.reference.*;
import com.wangc.bill.reference.capture.*;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/** Original automatic-entry layout and its recovered, local confirmation lifecycle. */
public final class CaptureOverlay {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    // A database replacement invalidates old queued saves, even after their window detached.
    private static final AtomicLong CLOSE_GENERATION = new AtomicLong();
    private static volatile Session active;
    private static volatile String lastResult = "NOT_REQUESTED";
    private static volatile long lastChanged;
    private CaptureOverlay() {}

    private static void state(String value) { lastResult = value; lastChanged = System.currentTimeMillis(); }

    public static JSONObject status() {
        Session session = active;
        return Json.object("attached", session != null && session.view.isAttachedToWindow(),
            "lastResult", lastResult, "lastChangedAt", lastChanged,
            "windowType", WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            "permissionGranted", OverlayPermission.granted(),
            "countdownActive", session != null && !session.closeRequested && session.countdownActive,
            "countdownRemainingSeconds", session == null ? 0 : session.remainingSeconds,
            "repeatDetected", session != null && session.repeatDetected);
    }

    private static boolean ready(ReferenceCaptureService service) {
        if (!OverlayPermission.granted()) { state("OVERLAY_PERMISSION_REQUIRED"); return false; }
        if (service == null) { state("SERVICE_DISCONNECTED"); return false; }
        if (!AutoApps.enabled()) { state("CAPTURE_DISABLED"); return false; }
        if (!ReferenceApplication.prefs().getBoolean("show_overlay", true)) { state("OVERLAY_DISABLED"); return false; }
        return true;
    }

    /** This explicit window preview never starts a timer or writes a test candidate. */
    public static void preview() {
        ReferenceCaptureService service = ReferenceCaptureService.current();
        if (!ready(service)) {
            Ui.toast(ReferenceApplication.app(), OverlayPermission.granted()
                ? "请先开启自动记账、连接无障碍服务，并开启确认浮窗" : "请先在自动记账页面点击“授权悬浮窗”");
            return;
        }
        long book = ReferenceApplication.prefs().getLong("capture_book", 1);
        JSONObject target = ReferenceApplication.store().book(book);
        if (target == null || target.optInt("archived") != 0 || !"CNY".equals(target.optString("currency"))) {
            state("BOOK_UNAVAILABLE"); Ui.toast(service, "请先选择可用的人民币账本"); return;
        }
        JSONObject data = Json.object("book_id", book, "kind", "expense", "amount", 123, "discount", 0,
            "fee", 0, "currency", "CNY", "time", System.currentTimeMillis(), "remark", "测试浮窗，不生成账单",
            "account_id", 0, "category_id", ReferenceApplication.store().defaultCategory("expense",book));
        display(null, Json.object("book_id", book, "payload", data.toString()), true);
    }

    public static void show(String id, x1.a callback) {
        if (Looper.myLooper() != Looper.getMainLooper()) { MAIN.post(() -> show(id, callback)); return; }
        if (!ready(ReferenceCaptureService.current())) return;
        JSONObject row = ReferenceApplication.store().candidate(id);
        if (row == null || !"PENDING".equals(row.optString("state"))) { state("NO_PENDING_DRAFT"); return; }
        display(id, row, false);
    }

    private static void display(String id, JSONObject row, boolean preview) {
        if (!preview && !ready(ReferenceCaptureService.current())) return;
        // Original x1.h also treats an added, not-yet-attached view as an existing window.
        Session previous = active;
        if (previous != null && (previous.view.getParent() != null || previous.view.isAttachedToWindow())) {
            state("WINDOW_ALREADY_SHOWING"); return;
        }
        // Internal cleanup must not invalidate an unrelated, normally submitted save.
        if (previous != null) { previous.closeRequested = true; previous.detach(); }
        state("CREATING_WINDOW");
        Session session = null;
        try {
            session = new Session(id, row, preview); active = session;
            session.bind();
            // Inflation/database reads can overlap a background restore's close request.
            if (session.closeRequested || session.generation != CLOSE_GENERATION.get()
                || (!preview && !ready(ReferenceCaptureService.current()))) {
                session.closeRequested = true; session.detach(); return;
            }
            // x1.i, used by x1.c/addView: 2038, flags 0x40120, RGBA_8888; softInputMode stays default.
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(-1, -1,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.RGBA_8888);
            session.manager.addView(session.view, params);
            state("SHOWN");
        } catch (RuntimeException error) {
            if (session != null) session.detach();
            ReferenceDiagnostics.failure("OVERLAY_UNAVAILABLE", error); state("SHOW_FAILED");
        }
    }

    /** Original d1 replaces a parser remark only while the user has left it untouched. */
    public static void enrichRemark(String id, String remark) {
        Session session = active;
        if (session == null || session.preview || session.closed || session.closeRequested || !session.id.equals(id)) return;
        EditText input = Ui.view(session.view, "remark");
        if (!session.originalRemark.isEmpty() && session.originalRemark.equals(input.getText().toString())) {
            Ui.text(session.view, "remark", remark); session.originalRemark = remark;
            Json.put(session.data, "remark", remark);
        }
    }

    public static void complete(String id) {
        YimuKernel.complete(id);
        Session session = active;
        if (session != null && id != null && id.equals(session.id)) close();
    }

    /** Shutdown/restore invalidates queued saves immediately and removes its captured window on main. */
    public static void close() {
        Session target = active;
        if (target != null) {
            // These volatile writes are safe from the file worker and precede UI cleanup.
            target.closeRequested = true; target.countdownActive = false; target.remainingSeconds = 0;
        }
        if (target == null) return;
        Runnable cleanup = () -> {
            boolean current = active == target;
            target.detach();
            if (current) state("CLOSED");
        };
        if (Looper.myLooper() == Looper.getMainLooper()) cleanup.run(); else MAIN.post(cleanup);
    }

    /** Database restoration is a boundary for all previously accepted window saves. */
    public static void closeForRestore() {
        CLOSE_GENERATION.incrementAndGet();
        close();
    }

    private static String accountName(long id) {
        JSONObject account = ReferenceApplication.store().one("SELECT name FROM accounts WHERE id=?", "" + id);
        return account == null ? "无账户" : account.optString("name");
    }

    private static final class Session {
        final String id;
        final boolean preview;
        final Context context;
        final CaptureOverlayLayout view;
        final int normalCostColor, normalSymbolColor;
        final WindowManager manager;
        final JSONObject data;
        final CategoryPicker.State categoryState;
        final List<JSONObject> repeats;
        final boolean repeatDetected;
        final long generation;
        volatile boolean closeRequested;
        volatile boolean countdownActive;
        volatile long remainingSeconds;
        CountDownTimer timer;
        Dialog dialog;
        PopupWindow repeatPopup;
        boolean closed, saving;
        String originalRemark;
        final double capturedNumber;
        final String capturedRemark;
        final boolean capturedIncome;
        boolean refundMode;
        OriginalBillRecord refundParent;
        View transferPage;
        long repaymentDestination = Long.MIN_VALUE;
        boolean transferRepayment;
        ViewTreeObserver.OnGlobalLayoutListener keyboardListener;
        Runnable keyboardStateChange;

        Session(String id, JSONObject row, boolean preview) {
            this.id = id; this.preview = preview;
            generation = CLOSE_GENERATION.get();
            context = new ContextThemeWrapper(ReferenceApplication.app(), com.wangc.bill.R.style.ReferenceTheme);
            view = (CaptureOverlayLayout) Ui.layout(context, "view_auto_add_new");
            normalCostColor = ((TextView) Ui.view(view, "cost")).getCurrentTextColor();
            normalSymbolColor = ((TextView) Ui.view(view, "num_symbol")).getCurrentTextColor();
            manager = (WindowManager) ReferenceApplication.app().getSystemService(Context.WINDOW_SERVICE);
            data = Json.parse(row.optString("payload"));
            Json.put(data, "book_id", row.optLong("book_id"));
            Json.put(data, "currency", "CNY");
            categoryState=CategoryPicker.State.from(null,ReferenceApplication.store(),data);
            originalRemark = data.optString("remark");
            capturedRemark = data.isNull("original_remark") ? originalRemark : data.optString("original_remark");
            capturedIncome = data.optBoolean("original_income", "income".equals(data.optString("kind")));
            capturedNumber = data.has("original_number")
                ? com.wangc.bill.utils.h2.S(data.optString("original_number"))
                : BigDecimal.valueOf(data.optLong("amount"), 2).doubleValue();
            repeats = preview ? new ArrayList<>() : ReferenceApplication.store().findRepeatTransactions(data);
            repeatDetected = !repeats.isEmpty();
        }

        void bind() {
            Ui.image(view, "account_book_icon", "ic_main_account_book");
            Ui.text(view, "cost", Money.text(data.optLong("amount"), "CNY"));
            Ui.text(view, "remark", originalRemark);
            Ui.text(view, "discount_edit", Money.text(data.optLong("discount"), "CNY"));
            Ui.text(view, "service_charge_edit", Money.text(data.optLong("fee"), "CNY"));
            Ui.hide(view, "lend_date_layout", "refund_in_date_layout", "reimbursement_layout", "file_layout",
                "tag_list", "file_list", "asset_num_tip");
            Ui.show(view, "repeat_tip", repeatDetected);
            if (repeatDetected) {
                View save = Ui.view(view, "btn_save");
                save.setBackgroundResource(Ui.res(context, "drawable", "selectable_item_light_grey_background"));
            }
            ReferenceTabs tabs = Ui.view(view, "tab_layout");
            String[] kinds = {"expense", "income", "transfer"};
            tabs.configure(new String[]{"支出", "收入", "转账", "退款"}, "income".equals(data.optString("kind")) ? 1
                : "transfer".equals(data.optString("kind")) ? 2 : 0, index -> {
                    cancelTimer(); refundMode = index == 3;
                    if (refundMode) matchRefundIfMissing(); else Json.put(data, "kind", kinds[index]);
                    if (index == 2) repaymentDestination = Long.MIN_VALUE;
                    render();
                });
            Ui.click(view, "account_book_layout", this::chooseBook);
            Ui.click(view, "asset_layout", () -> chooseAccount("account_id"));
            Ui.click(view, "date_layout", this::chooseDate);
            Ui.click(view, "refund_in_date_layout", () -> chooseDate("in_time"));
            Ui.click(view, "close_btn", this::cancel);
            Ui.click(view, "btn_save", () -> save(false));
            // The original background is bound only as a field, without a click-to-dismiss listener.
            view.setBackAction(this::cancel);
            if (preview) { Ui.text(view, "btn_save_text", "关闭测试浮窗"); Ui.text(view, "close_btn", "关闭测试"); }
            installKeyboardObserver(); render();
            android.text.TextWatcher transferNumbers = new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateTransferNumbers(); }
                @Override public void afterTextChanged(android.text.Editable s) {}
            };
            ((EditText) Ui.view(view, "cost")).addTextChangedListener(transferNumbers);
            ((EditText) Ui.view(view, "service_charge_edit")).addTextChangedListener(transferNumbers);
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override public void onViewAttachedToWindow(View attached) { startTimer(); }
                @Override public void onViewDetachedFromWindow(View detached) { release(); }
            });
        }

        void render() {
            JSONObject book = ReferenceApplication.store().book(data.optLong("book_id"));
            Ui.text(view, "account_book_name", book == null ? "请选择账本" : book.optString("name"));
            Ui.text(view, "date", Ui.date(data.optLong("time"), "MM.dd"));
            String kind = data.optString("kind");
            Ui.text(view, "num_symbol", refundMode ? refundParent != null && refundParent.parentCategoryId == 9 ? "-" : "+"
                : "income".equals(kind) ? "+" : "expense".equals(kind) ? "-" : "");
            TextView costInput = Ui.view(view, "cost"), symbol = Ui.view(view, "num_symbol");
            if (refundMode) {
                // Original J0/n0 use these same two colors independently of p0.H.
                boolean pay = refundParent != null && refundParent.parentCategoryId == 9;
                int color = Ui.color(context, pay ? "moneyPay" : "moneyIncome", pay ? Ui.RED : Ui.GREEN);
                costInput.setTextColor(color); symbol.setTextColor(color);
            } else {
                // Restore the existing non-refund appearance after leaving this tab.
                costInput.setTextColor(normalCostColor); symbol.setTextColor(normalSymbolColor);
            }
            Ui.text(view, "asset_name", accountName(data.optLong("account_id")));
            Ui.show(view, "asset_layout", refundMode || !"transfer".equals(kind));
            Ui.show(view, "discount_layout", !refundMode && "expense".equals(kind));
            Ui.show(view, "service_charge_layout", !refundMode && ("transfer".equals(kind) || data.optLong("fee") > 0));
            JSONObject receiver = ReferenceApplication.store().assetRepository().byRowId(data.optLong("account_id"));
            Ui.show(view, "refund_in_date_layout", refundMode && receiver != null && receiver.optInt("asset_type") == 2);
            Ui.text(view, "refund_in_date", data.optLong("in_time") == 0 ? "入账日期" : Ui.date(data.optLong("in_time"), "MM.dd"));
            Ui.show(view, "repeat_tip", repeatDetected);
            Ui.view(view, "btn_save").setBackgroundResource(Ui.res(context, "drawable", repeatDetected
                ? "selectable_item_light_grey_background" : "selectable_item_primary_background"));
            FrameLayout container = Ui.view(view, "category_pager"); container.removeAllViews(); transferPage = null;
            if (refundMode) {
                container.addView(refundView(), new FrameLayout.LayoutParams(-1, -2)); return;
            }
            if ("transfer".equals(kind)) {
                container.addView(transferView(), new FrameLayout.LayoutParams(-1, -2)); return;
            }
            // Original AutoCategoryPagerAdapter uses six columns; initialization must
            // not invoke I0, which cancels the automatic-add timer only after a click.
            container.addView(new CategoryPicker(context,ReferenceApplication.store(),data.optLong("book_id"),kind,
                data,categoryState,true,this::cancelTimer,null).view(),new FrameLayout.LayoutParams(-1,-2));
        }

        View transferView() {
            View page = Ui.layout(context, "item_auto_transfer"); transferPage = page;
            ReferenceStore store = ReferenceApplication.store();
            JSONObject from = store.assetRepository().byRowId(data.optLong("account_id"));
            JSONObject to = store.assetRepository().byRowId(data.optLong("to_account_id"));
            bindTransferAsset(page, from, "from"); bindTransferAsset(page, to, "to");
            Ui.click(page, "choice_from_asset", () -> chooseAccount("account_id"));
            Ui.click(page, "choice_to_asset", () -> chooseAccount("to_account_id"));
            Ui.click(page, "check_asset", () -> {
                cancelTimer(); long oldFrom = data.optLong("account_id"), oldTo = data.optLong("to_account_id");
                store.selectAccountForInput(data, "account_id", oldTo, true);
                store.selectAccountForInput(data, "to_account_id", oldFrom, true);
                repaymentDestination = Long.MIN_VALUE; render();
            });
            if (repaymentDestination != data.optLong("to_account_id")) {
                repaymentDestination = data.optLong("to_account_id");
                transferRepayment = to != null && to.optInt("asset_type") == 2;
            }
            Ui.view(page, "set_repayment_layout").setVisibility(to != null && to.optInt("asset_type") == 2 ? View.VISIBLE : View.INVISIBLE);
            CheckBox repayment = Ui.view(page, "set_repayment"); repayment.setChecked(transferRepayment);
            repayment.setOnCheckedChangeListener((button, checked) -> { transferRepayment = checked; cancelTimer(); });
            RadioGroup group = Ui.view(page, "radio_group");
            int mode = ReferenceApplication.prefs().getInt("original_transfer_mode", 0);
            group.check(Ui.res(context, "id", mode == 0 ? "radio_one" : "radio_two"));
            group.setOnCheckedChangeListener((radio, checked) -> {
                ReferenceApplication.prefs().edit().putInt("original_transfer_mode", checked == Ui.res(context,"id","radio_one") ? 0 : 1).apply();
                cancelTimer(); updateTransferNumbers();
            });
            updateTransferNumbers(); return page;
        }

        void bindTransferAsset(View page, JSONObject asset, String side) {
            boolean original = asset != null && AssetRepository.ORIGINAL.equals(asset.optString("asset_origin"));
            Ui.text(page, side + "_asset_name", original ? asset.optString("name") : "");
            Ui.show(page, side + "_asset_num", original);
            Ui.image(page, side + "_asset_icon", original && !asset.optString("asset_icon").isEmpty() ? asset.optString("asset_icon") : "ic_no_asset");
            if (original) {
                double number = new OriginalFinancialOrchestrator(ReferenceApplication.store(), ReferenceStore.LOCAL_OFFLINE_OWNER,
                        ReferenceStore.LOCAL_OFFLINE_OWNER).readBalance(asset.optLong("semantic_id"));
                Ui.text(page, side + "_asset_num", "余额：" + OriginalTransferAmount.formatBalance(number));
            }
        }

        void updateTransferNumbers() {
            if (transferPage == null || refundMode || !"transfer".equals(data.optString("kind"))) return;
            double amount = OriginalTransferAmount.parseS(((EditText) Ui.view(view, "cost")).getText().toString());
            double fee = OriginalTransferAmount.parseS(((EditText) Ui.view(view, "service_charge_edit")).getText().toString());
            int mode = ReferenceApplication.prefs().getInt("original_transfer_mode", 0);
            double debit = mode == 0 ? fee + amount : amount;
            double credit = mode == 0 ? amount : Math.max(0.0d, amount - fee);
            Ui.show(transferPage, "reduce_num", debit != 0.0d); Ui.text(transferPage, "reduce_num", "-" + OriginalTransferAmount.formatDelta(debit));
            Ui.show(transferPage, "add_num", credit != 0.0d); Ui.text(transferPage, "add_num", "+" + OriginalTransferAmount.formatDelta(credit));
            Ui.show(transferPage, "radio_layout", fee != 0.0d);
            String format = context.getString(Ui.res(context, "string", "transfer_info"));
            Ui.text(transferPage, "radio_one_text", String.format(format, OriginalTransferAmount.formatBalance(fee + amount),
                    OriginalTransferAmount.formatBalance(amount), OriginalTransferAmount.formatBalance(fee)));
            Ui.text(transferPage, "radio_two_text", String.format(format, OriginalTransferAmount.formatBalance(amount),
                    OriginalTransferAmount.formatBalance(amount - fee), OriginalTransferAmount.formatBalance(fee)));
        }

        void matchRefundIfMissing() {
            if (refundParent != null) return;
            if (!Double.isFinite(capturedNumber)) return;
            // AutoAdd.X0 uses the original capture, not edited amount/remark or selected book.
            // Adapter.s1 stores the parent without the manual-selection receiving-account callback.
            refundParent = new OriginalRefundMatcher(ReferenceApplication.store())
                .findParent(capturedNumber, capturedRemark, capturedIncome);
        }

        View refundView() {
            View page = Ui.layout(context, "item_auto_add_refund");
            Ui.hide(page, "end_reimbursement", "tag_one", "tag_two", "tag_three", "reimbursement", "refund", "discount");
            Ui.text(page, "choice_bill_title", "退款账单");
            Ui.text(page, "bill_count", refundParent == null ? "已选择0条账单" : "已选择1条账单");
            Ui.show(page, "choice_bill_tip", refundParent == null);
            Ui.show(page, "bill_layout", refundParent != null);
            Ui.click(page, "total_layout", this::chooseRefundParent);
            if (refundParent == null) return page;
            CategoryRepository categories = ReferenceApplication.store().categoryRepository();
            JSONObject category = categories.child(refundParent.parentCategoryId, refundParent.childCategoryId);
            if (category == null) category = categories.parent(refundParent.parentCategoryId);
            Ui.text(page, "category", category == null ? "其他" : category.optString("name"));
            if (category != null) Ui.image(page, "icon", category.optString("icon"));
            Ui.text(page, "bill_remark", refundParent.remark == null ? "" : refundParent.remark);
            Ui.text(page, "time", Ui.date(refundParent.time, "MM.dd HH:mm"));
            TextView amount = Ui.view(page, "bill_cost");
            amount.setText((refundParent.parentCategoryId == 9 ? "+" : "-") + com.wangc.bill.utils.h2.t(Math.abs(refundParent.cost)));
            amount.setTextColor(Ui.color(context, refundParent.parentCategoryId == 9 ? "moneyIncome" : "moneyPay", Ui.RED));
            JSONObject asset = ReferenceApplication.store().assetRepository().p(refundParent.assetId, ReferenceStore.LOCAL_OFFLINE_OWNER);
            Ui.show(page, "asset", asset != null); Ui.text(page, "asset", asset == null ? "" : asset.optString("name"));
            return page;
        }

        void chooseRefundParent() {
            cancelTimer();
            // BillChoiceDialog.H starts at MyApplication's current book, which is
            // independent of this automatic-entry window's selected header book.
            JSONObject current = ReferenceApplication.store().bookRepository().resolveCurrent(
                ReferenceStore.LOCAL_OFFLINE_OWNER, ReferenceApplication.prefs().getLong("current_book", 1));
            if (current == null) { Ui.toast(context, "当前账本不可用"); return; }
            chooseRefundParent(current.optLong("id"));
        }

        void chooseRefundParent(long bookRowId) {
            cancelTimer();
            try {
                ReferenceStore store = ReferenceApplication.store();
                List<OriginalBillRecord> parents = new OriginalRefundMatcher(store).manualParents(
                    ReferenceStore.LOCAL_OFFLINE_OWNER, bookRowId, refundParent);
                String[] labels = new String[parents.size()];
                for (int i = 0; i < parents.size(); i++) {
                    OriginalBillRecord bill = parents.get(i);
                    labels[i] = (bill.parentCategoryId == 9 ? "+" : "-")
                        + com.wangc.bill.utils.h2.t(Math.abs(bill.cost)) + " · "
                        + (bill.remark == null ? "" : bill.remark) + "\n" + Ui.date(bill.time, "MM.dd HH:mm");
                }
                int[] selected = {-1};
                // This is the recovered P=0, empty-search page (100 before
                // removeAll(selected)); search/asset/date controls are not ported.
                AlertDialog picker = new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)
                    .setTitle(bookRowId == 0 ? "退款账单 · 所有账本" : "退款账单 · 当前账本")
                    .setSingleChoiceItems(labels, -1, (d, index) -> selected[0] = index)
                    .setNeutralButton(bookRowId == 0 ? "当前账本" : "所有账本", (d, which) -> {
                        if (bookRowId == 0) chooseRefundParent(); else chooseRefundParent(0);
                    })
                    .setNegativeButton("取消", null).setPositiveButton("确定", null).create();
                showDialog(picker);
                if (!picker.isShowing()) return;
                picker.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (selected[0] < 0) { Ui.toast(context, "请至少选择一条账单"); return; }
                    refundParent = parents.get(selected[0]);
                    // Manual T0 -> J0 replaces an existing receiving account only
                    // for a positive raw asset ID resolving through f.P. X0 does not.
                    JSONObject asset = refundParent.assetId > 0 ? store.assetRepository().p(
                        refundParent.assetId, ReferenceStore.LOCAL_OFFLINE_OWNER) : null;
                    if (asset != null) store.selectAccountForInput(data, "account_id", asset.optLong("id"), true);
                    picker.dismiss(); render();
                });
            } catch (RuntimeException error) { Ui.error(context, error); }
        }

        void startTimer() {
            if (preview || closed || closeRequested || active != this || repeatDetected
                || !com.wangc.bill.database.action.s.i() || timer != null) return;
            // AutoAddLayout.onAttachedToWindow and its $a: 5000ms, 1000ms, then the same btnSave path.
            timer = new CountDownTimer(5000, 1000) {
                @Override public void onTick(long millisUntilFinished) {
                    if (closed || closeRequested || active != Session.this || !view.isAttachedToWindow()) { cancelTimer(); return; }
                    remainingSeconds = millisUntilFinished / 1000;
                    Ui.text(view, "btn_save_text", "新增(" + remainingSeconds + ")");
                }
                @Override public void onFinish() {
                    if (closed || closeRequested || active != Session.this || !view.isAttachedToWindow()) return;
                    if (!ready(ReferenceCaptureService.current())) { cancelTimer(); return; }
                    save(true);
                }
            };
            countdownActive = true; remainingSeconds = 5; timer.start();
        }

        void cancelTimer() {
            CountDownTimer old = timer; timer = null; countdownActive = false; remainingSeconds = 0;
            if (old != null) old.cancel();
            if (!preview) Ui.text(view, "btn_save_text", "新增");
        }

        void chooseBook() {
            cancelTimer();
            List<JSONObject> books = new ArrayList<>();
            for (JSONObject book : ReferenceApplication.store().books()) if ("CNY".equals(book.optString("currency"))) books.add(book);
            if (books.isEmpty()) { Ui.toast(context, "请先建立人民币账本"); return; }
            String[] labels = new String[books.size()];
            for (int i = 0; i < books.size(); i++) labels[i] = books.get(i).optString("name");
            showDialog(new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setTitle("选择账本").setItems(labels, (d, index) -> {
                    Json.put(data, "book_id", books.get(index).optLong("id")); render();
                }).create());
        }

        void chooseAccount(String key) {
            cancelTimer();
            if (!refundMode && "transfer".equals(data.optString("kind"))) {
                List<JSONObject> assets = ReferenceApplication.store().assetRepository().picker(ReferenceStore.LOCAL_OFFLINE_OWNER, null);
                String[] labels = new String[assets.size()];
                for (int i = 0; i < labels.length; i++) labels[i] = assets.get(i).optString("name");
                showDialog(new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)
                    .setTitle("account_id".equals(key) ? "选择转出账户" : "选择转入账户")
                    .setItems(labels, (d, index) -> {
                        ReferenceApplication.store().selectAccountForInput(data, key, assets.get(index).optLong("id"), true);
                        if ("to_account_id".equals(key)) repaymentDestination = Long.MIN_VALUE;
                        render();
                    }).create());
                return;
            }
            List<JSONObject> accounts = ReferenceApplication.store().selectableAccounts(data.optLong("book_id"));
            String[] labels = new String[accounts.size() + 1]; labels[accounts.size()] = "无账户";
            for (int i = 0; i < accounts.size(); i++) labels[i] = accounts.get(i).optString("name");
            showDialog(new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setTitle("选择账户").setItems(labels, (d, index) -> {
                    ReferenceApplication.store().selectAccountForInput(data,key,index == accounts.size()?0:accounts.get(index).optLong("id"),true); render();
                }).create());
        }

        void chooseDate() { chooseDate("time"); }

        void chooseDate(String field) {
            cancelTimer(); Calendar calendar = Calendar.getInstance();
            long selected = data.optLong(field); calendar.setTimeInMillis(selected == 0 ? System.currentTimeMillis() : selected);
            DatePickerDialog picker = new DatePickerDialog(context, (p, year, month, day) -> {
                calendar.set(year, month, day);
                // ChoiceDatePopupManager.c includes hour/minute and clears seconds/milliseconds.
                showDialog(new TimePickerDialog(context, (clock, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour); calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0);
                    Json.put(data, field, calendar.getTimeInMillis()); render();
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            showDialog(picker);
        }

        void showDialog(Dialog next) {
            if (closed || closeRequested || !view.isAttachedToWindow()) return;
            if (!OverlayPermission.granted()) { state("OVERLAY_PERMISSION_REQUIRED"); return; }
            if (dialog != null) dialog.dismiss();
            dialog = next; next.setOnDismissListener(d -> { if (dialog == next) dialog = null; });
            try { next.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY); next.show(); }
            catch (RuntimeException error) {
                if (dialog == next) dialog = null;
                try { next.dismiss(); } catch (RuntimeException ignored) {}
                ReferenceDiagnostics.failure("OVERLAY_UNAVAILABLE", error); state("AUXILIARY_SHOW_FAILED");
                Ui.toast(context, "选择窗口未能显示，请在待确认列表中编辑");
            }
        }

        void save(boolean automatic) {
            cancelTimer();
            if (closed || closeRequested || saving) return;
            if (preview) { detach(); state("CLOSED"); return; }
            try {
                JSONObject value = Json.copy(data);
                EditText cost = Ui.view(view, "cost"), remark = Ui.view(view, "remark");
                EditText discount = Ui.view(view, "discount_edit"), fee = Ui.view(view, "service_charge_edit");
                if (!refundMode && "transfer".equals(value.optString("kind"))) {
                    if (cost.getText().toString().isEmpty()) { Ui.toast(context, "金额不能为空"); return; }
                    if (value.optLong("account_id") <= 0 || value.optLong("to_account_id") <= 0) {
                        Ui.toast(context, "请选择转出或转入账户"); return;
                    }
                    Json.put(value, "transfer_amount_text", cost.getText().toString());
                    Json.put(value, "transfer_fee_text", fee.getText().toString());
                    Json.put(value, "transfer_repayment", transferRepayment);
                    Json.put(value, "remark", remark.getText().toString());
                    // Transfer skips the Bill repeat popup, preserves signed/zero raw
                    // numbers, and lets Z distinguish its two fee predicates.
                    persist(value, 0, automatic); return;
                }
                long entered = refundMode ? Money.signed(cost.getText().toString(), "CNY") : Money.positive(cost.getText().toString(), "CNY");
                if (refundMode && entered == 0) throw new IllegalArgumentException("请输入非零退款金额");
                Json.put(value, "amount", entered);
                Json.put(value, "remark", remark.getText().toString());
                if (refundMode) {
                    if (refundParent == null) { state("REFUND_PARENT_REQUIRED"); Ui.toast(context, "请选择退款账单"); return; }
                    Long receiver = null;
                    if (value.optLong("account_id") > 0) {
                        JSONObject account = ReferenceApplication.store().assetRepository().byRowId(value.optLong("account_id"));
                        if (account == null || !AssetRepository.ORIGINAL.equals(account.optString("asset_origin"))
                            || account.optInt("owner_semantic_id") != ReferenceStore.LOCAL_OFFLINE_OWNER)
                            throw new IllegalArgumentException("该账户暂不支持退款，请选择其他账户或无账户");
                        receiver = account.optLong("semantic_id");
                    }
                    Json.put(value, "discount", 0); Json.put(value, "fee", 0);
                    if (repeatDetected) { confirmRepeat(value, automatic, refundParent, receiver); return; }
                    persist(value, 0, automatic, refundParent, receiver); return;
                }
                Json.put(value, "discount", optionalAmount(discount)); Json.put(value, "fee", optionalAmount(fee));
                if ("transfer".equals(value.optString("kind")) && (value.optLong("account_id") <= 0
                    || value.optLong("to_account_id") <= 0 || value.optLong("account_id") == value.optLong("to_account_id")))
                    throw new IllegalArgumentException("请选择两个不同的转账账户");
                if (repeatDetected && !"transfer".equals(value.optString("kind"))) { confirmRepeat(value, automatic); return; }
                persist(value, 0, automatic);
            } catch (RuntimeException error) { Ui.error(context, error); }
        }

        long optionalAmount(EditText field) {
            String text = field.getText().toString().trim();
            long value = text.isEmpty() ? 0 : Money.signed(text, "CNY");
            if (value < 0) throw new IllegalArgumentException("优惠和手续费不能为负数");
            return value;
        }

        void confirmRepeat(JSONObject value, boolean automatic) {
            confirmRepeat(value, automatic, null, null);
        }

        void confirmRepeat(JSONObject value, boolean automatic, OriginalBillRecord parent, Long receiver) {
            if (closed || closeRequested) return;
            if (repeatPopup != null && repeatPopup.isShowing()) return;
            View content = Ui.layout(context, "popup_repeat_tip");
            RecyclerView list = Ui.view(content, "bill_list"); list.setLayoutManager(new LinearLayoutManager(context));
            list.setAdapter(new RecyclerView.Adapter<RepeatHolder>() {
                @Override public int getItemCount() { return repeats.size(); }
                @Override public RepeatHolder onCreateViewHolder(ViewGroup parent, int type) { return new RepeatHolder(Ui.row(context, "", "", null)); }
                @Override public void onBindViewHolder(RepeatHolder holder, int index) {
                    JSONObject bill = repeats.get(index); LinearLayout row = (LinearLayout) holder.itemView;
                    ((TextView) row.getChildAt(0)).setText(Money.label(bill.optLong("amount"), bill.optString("currency", "CNY")));
                    ((TextView) row.getChildAt(1)).setText(bill.optString("book_name") + " · " + Ui.date(bill.optLong("time"), "MM.dd HH:mm") + "\n" + bill.optString("remark"));
                }
            });
            ViewGroup.LayoutParams listSize = list.getLayoutParams();
            listSize.height = Ui.dp(context, Math.min(300, Math.max(75, repeats.size() * 75))); list.setLayoutParams(listSize);
            boolean[] replace = {false}; Ui.show(content, "check_layout", repeats.size() == 1);
            Ui.click(content, "check_layout", () -> { replace[0] = !replace[0]; Ui.image(content, "check_btn", replace[0] ? "ic_check" : "ic_not_check"); });
            PopupWindow popup = new PopupWindow(content, -1, -1, true); repeatPopup = popup;
            popup.setTouchable(true); popup.setOutsideTouchable(true); popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
            popup.setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            popup.setOnDismissListener(() -> { if (repeatPopup == popup) repeatPopup = null; });
            Ui.click(content, "cancel", popup::dismiss);
            Ui.click(content, "confirm", () -> { popup.dismiss(); if (!closed) persist(value, replace[0] && repeats.size() == 1 ? repeats.get(0).optLong("id") : 0, automatic, parent, receiver); });
            try { popup.showAtLocation(view, Gravity.CENTER, 0, 0); }
            catch (RuntimeException error) {
                repeatPopup = null; try { popup.dismiss(); } catch (RuntimeException ignored) {}
                ReferenceDiagnostics.failure("OVERLAY_UNAVAILABLE", error); state("AUXILIARY_SHOW_FAILED");
                Ui.toast(context, "重复账单确认窗口未能显示，账单仍在待确认列表");
            }
        }

        void persist(JSONObject value, long replacing, boolean automatic) {
            persist(value, replacing, automatic, null, null);
        }

        void persist(JSONObject value, long replacing, boolean automatic, OriginalBillRecord parent, Long receiver) {
            if (closed || closeRequested || saving) return;
            saving = true;
            try {
                // Original Z0 queues the same local save for both manual clicks and timer expiry.
                YimuKernel.enqueue(() -> {
                    try {
                        // Original Z0 retains an accepted manual save after its window closes.
                        // Restoration alone invalidates that save; an unaccepted click/timer
                        // is already stopped by closeRequested on the main thread.
                        if (generation != CLOSE_GENERATION.get()
                            || (automatic && (closeRequested || !AutoApps.enabled() || ReferenceCaptureService.current() == null
                                || !ReferenceApplication.prefs().getBoolean("show_overlay", true)
                                || !OverlayPermission.granted()))) {
                            ReferenceDiagnostics.result("SAVE_CANCELLED");
                            MAIN.post(() -> { if (active == null) state("SAVE_CANCELLED"); CaptureNotifications.update(); });
                            return;
                        }
                        if (parent != null && replacing > 0) ReferenceApplication.store().saveCandidateRefundReplacing(value, id, parent, receiver, replacing);
                        else if (parent != null) ReferenceApplication.store().saveCandidateRefund(value, id, parent, receiver);
                        else if ("transfer".equals(value.optString("kind"))) ReferenceApplication.store().saveCandidateTransfer(value, id,
                            () -> MAIN.post(() -> Ui.toast(context, "到账金额需要大于0")));
                        else if (replacing > 0) ReferenceApplication.store().saveCandidateReplacing(value, id, replacing);
                        else ReferenceApplication.store().saveTransaction(value, 0, id);
                        ReferenceDiagnostics.result(parent != null ? "REFUND_SAVED" : automatic ? "AUTO_SAVED" : "BILL_SAVED");
                        MAIN.post(() -> {
                            if (active == null) state(parent != null ? "REFUND_SAVED" : automatic ? "AUTO_SAVED" : "SAVED");
                            CaptureNotifications.update(); Ui.toast(context, parent != null ? "退款已记账" : "新增成功");
                        });
                    } catch (RuntimeException error) {
                        ReferenceDiagnostics.failure("STORE_FAILED", error);
                        MAIN.post(() -> { if (active == null) state("SAVE_FAILED"); CaptureNotifications.update(); CaptureNotifications.error();
                            Ui.toast(context, parent != null ? "退款保存未完成，请先核对余额、资产历史和原账单，勿重复提交"
                                : "transfer".equals(value.optString("kind")) ? "转账保存未完成，请先核对余额、手续费和转账记录，勿重复提交" : "新增失败，账单仍在待确认列表"); });
                    }
                });
                // Original Z0/Y removes the window immediately after submitting its save task.
                detach(); YimuKernel.complete(id); state("SAVING");
            } catch (RejectedExecutionException error) {
                saving = false; ReferenceDiagnostics.failure("QUEUE_FULL", error); state("SAVE_QUEUE_FULL");
                Ui.toast(context, "采集任务繁忙，请稍后再点新增");
            }
        }

        void cancel() {
            cancelTimer(); if (closed || closeRequested || saving) return;
            if (preview) { detach(); state("CLOSED"); return; }
            try { ReferenceApplication.store().dismiss(id); YimuKernel.complete(id); detach(); state("CLOSED"); CaptureNotifications.update(); }
            catch (RuntimeException error) { Ui.error(context, error); }
        }

        void installKeyboardObserver() {
            // n7.a/b uses real display height and a 60dp threshold, notifying only changes.
            DisplayMetrics real = new DisplayMetrics(); manager.getDefaultDisplay().getRealMetrics(real);
            int threshold = Math.round(60 * android.content.res.Resources.getSystem().getDisplayMetrics().density);
            int[] previousHeight = {0};
            keyboardListener = () -> {
                if (closed || closeRequested || !view.isAttachedToWindow()) return;
                Rect visible = new Rect(); view.getWindowVisibleDisplayFrame(visible);
                int keyboardHeight = real.heightPixels - visible.bottom;
                boolean keyboard;
                if (previousHeight[0] != keyboardHeight && keyboardHeight > threshold) {
                    previousHeight[0] = keyboardHeight; keyboard = true;
                } else if (previousHeight[0] != 0 && keyboardHeight <= threshold) {
                    previousHeight[0] = 0; keyboard = false;
                } else return;
                boolean editing = keyboard && keyboardHeight > 200;
                if (keyboardStateChange != null) MAIN.removeCallbacks(keyboardStateChange);
                keyboardStateChange = () -> { if (!closed && !closeRequested) view.setKeyboardVisible(editing); };
                MAIN.postDelayed(keyboardStateChange, 100);
                View content = Ui.view(view, "content_layout"), edit = Ui.view(view, "edit_layout");
                EditText fee = Ui.view(view, "service_charge_edit"), discount = Ui.view(view, "discount_edit");
                int[] location = new int[2]; view.getLocationOnScreen(location);
                if (editing) {
                    if (fee.isFocused()) fee.setHint(""); else if (discount.isFocused()) discount.setHint("");
                    cancelTimer(); content.setY(visible.bottom - location[1] - edit.getBottom());
                } else {
                    fee.setHint("手续费"); discount.setHint("优惠");
                    content.setY(visible.bottom - location[1] - content.getHeight() - Ui.dp(context, 10));
                }
            };
            view.getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
        }

        void release() {
            if (closed) return;
            closed = true; cancelTimer();
            if (keyboardListener != null && view.getViewTreeObserver().isAlive()) view.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
            keyboardListener = null; view.setBackAction(null);
            if (keyboardStateChange != null) MAIN.removeCallbacks(keyboardStateChange);
            keyboardStateChange = null;
            if (dialog != null) { Dialog old = dialog; dialog = null; try { old.dismiss(); } catch (RuntimeException ignored) {} }
            if (repeatPopup != null) { PopupWindow old = repeatPopup; repeatPopup = null; try { old.dismiss(); } catch (RuntimeException ignored) {} }
            if (active == this) active = null;
        }

        void detach() {
            release();
            if (view.getParent() != null || view.isAttachedToWindow()) try { manager.removeView(view); } catch (RuntimeException ignored) {}
        }
    }

    private static final class RepeatHolder extends RecyclerView.ViewHolder {
        RepeatHolder(View item) { super(item); }
    }
}
