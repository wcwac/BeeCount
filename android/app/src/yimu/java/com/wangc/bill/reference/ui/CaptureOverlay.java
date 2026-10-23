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
            "account_id", 0, "category_id", ReferenceApplication.store().defaultCategory("expense"));
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
        final WindowManager manager;
        final JSONObject data;
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
        ViewTreeObserver.OnGlobalLayoutListener keyboardListener;
        Runnable keyboardStateChange;

        Session(String id, JSONObject row, boolean preview) {
            this.id = id; this.preview = preview;
            generation = CLOSE_GENERATION.get();
            context = new ContextThemeWrapper(ReferenceApplication.app(), com.wangc.bill.R.style.ReferenceTheme);
            view = (CaptureOverlayLayout) Ui.layout(context, "view_auto_add_new");
            manager = (WindowManager) ReferenceApplication.app().getSystemService(Context.WINDOW_SERVICE);
            data = Json.parse(row.optString("payload"));
            Json.put(data, "book_id", row.optLong("book_id"));
            Json.put(data, "currency", "CNY");
            originalRemark = data.optString("remark");
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
            tabs.configure(new String[]{"支出", "收入", "转账"}, "income".equals(data.optString("kind")) ? 1
                : "transfer".equals(data.optString("kind")) ? 2 : 0, index -> {
                    cancelTimer(); Json.put(data, "kind", kinds[index]);
                    Json.put(data, "category_id", ReferenceApplication.store().defaultCategory(index == 1 ? "income" : "expense"));
                    render();
                });
            Ui.click(view, "account_book_layout", this::chooseBook);
            Ui.click(view, "asset_layout", () -> chooseAccount("account_id"));
            Ui.click(view, "date_layout", this::chooseDate);
            Ui.click(view, "close_btn", this::cancel);
            Ui.click(view, "btn_save", () -> save(false));
            // The original background is bound only as a field, without a click-to-dismiss listener.
            view.setBackAction(this::cancel);
            if (preview) { Ui.text(view, "btn_save_text", "关闭测试浮窗"); Ui.text(view, "close_btn", "关闭测试"); }
            installKeyboardObserver(); render();
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
            Ui.text(view, "num_symbol", "income".equals(kind) ? "+" : "expense".equals(kind) ? "-" : "");
            Ui.text(view, "asset_name", accountName(data.optLong("account_id")));
            Ui.show(view, "asset_layout", !"transfer".equals(kind));
            Ui.show(view, "discount_layout", "expense".equals(kind));
            Ui.show(view, "service_charge_layout", "transfer".equals(kind) || data.optLong("fee") > 0);
            FrameLayout container = Ui.view(view, "category_pager"); container.removeAllViews();
            if ("transfer".equals(kind)) {
                LinearLayout column = Ui.column(context);
                column.addView(Ui.row(context, "转出账户", accountName(data.optLong("account_id")), () -> chooseAccount("account_id")));
                column.addView(Ui.row(context, "转入账户", accountName(data.optLong("to_account_id")), () -> chooseAccount("to_account_id")));
                container.addView(column); return;
            }
            List<JSONObject> categories = ReferenceApplication.store().categories(kind);
            GridView grid = new GridView(context); grid.setNumColumns(5); grid.setVerticalSpacing(Ui.dp(context, 4));
            grid.setAdapter(new BaseAdapter() {
                @Override public int getCount() { return categories.size(); }
                @Override public Object getItem(int index) { return categories.get(index); }
                @Override public long getItemId(int index) { return categories.get(index).optLong("id"); }
                @Override public View getView(int index, View recycled, ViewGroup parent) {
                    JSONObject category = categories.get(index);
                    LinearLayout cell = new LinearLayout(context); cell.setOrientation(LinearLayout.VERTICAL);
                    cell.setGravity(Gravity.CENTER); cell.setPadding(0, Ui.dp(context, 7), 0, Ui.dp(context, 7));
                    ImageView icon = new ImageView(context);
                    int image = Ui.res(context, "mipmap", category.optString("icon"));
                    if (image == 0) image = Ui.res(context, "mipmap", "ic_parent_other");
                    icon.setImageResource(image);
                    cell.addView(icon, new LinearLayout.LayoutParams(Ui.dp(context, 26), Ui.dp(context, 26)));
                    cell.addView(Ui.label(context, category.optString("name"), 11));
                    if (category.optLong("id") == data.optLong("category_id")) cell.setBackground(Ui.rounded(0x2248ab93, 8, context));
                    return cell;
                }
            });
            grid.setOnItemClickListener((parent, cell, index, rowId) -> {
                // Original I0 cancels only on a category selection, not on arbitrary touches.
                cancelTimer(); Json.put(data, "category_id", categories.get(index).optLong("id"));
                ((BaseAdapter) grid.getAdapter()).notifyDataSetChanged();
            });
            container.addView(grid, new FrameLayout.LayoutParams(-1, Ui.dp(context, 135)));
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
            List<JSONObject> accounts = ReferenceApplication.store().query("SELECT * FROM accounts WHERE archived=0 AND currency='CNY' ORDER BY id");
            String[] labels = new String[accounts.size() + 1]; labels[accounts.size()] = "无账户";
            for (int i = 0; i < accounts.size(); i++) labels[i] = accounts.get(i).optString("name");
            showDialog(new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setTitle("选择账户").setItems(labels, (d, index) -> {
                    Json.put(data, key, index == accounts.size() ? 0 : accounts.get(index).optLong("id")); render();
                }).create());
        }

        void chooseDate() {
            cancelTimer(); Calendar calendar = Calendar.getInstance(); calendar.setTimeInMillis(data.optLong("time"));
            DatePickerDialog picker = new DatePickerDialog(context, (p, year, month, day) -> {
                calendar.set(year, month, day);
                // ChoiceDatePopupManager.c includes hour/minute and clears seconds/milliseconds.
                showDialog(new TimePickerDialog(context, (clock, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour); calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0);
                    Json.put(data, "time", calendar.getTimeInMillis()); render();
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
                Json.put(value, "amount", Money.positive(cost.getText().toString(), "CNY"));
                Json.put(value, "remark", remark.getText().toString());
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
            Ui.click(content, "confirm", () -> { popup.dismiss(); if (!closed) persist(value, replace[0] && repeats.size() == 1 ? repeats.get(0).optLong("id") : 0, automatic); });
            try { popup.showAtLocation(view, Gravity.CENTER, 0, 0); }
            catch (RuntimeException error) {
                repeatPopup = null; try { popup.dismiss(); } catch (RuntimeException ignored) {}
                ReferenceDiagnostics.failure("OVERLAY_UNAVAILABLE", error); state("AUXILIARY_SHOW_FAILED");
                Ui.toast(context, "重复账单确认窗口未能显示，账单仍在待确认列表");
            }
        }

        void persist(JSONObject value, long replacing, boolean automatic) {
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
                        if (replacing > 0) ReferenceApplication.store().saveCandidateReplacing(value, id, replacing);
                        else ReferenceApplication.store().saveTransaction(value, 0, id);
                        ReferenceDiagnostics.result(automatic ? "AUTO_SAVED" : "BILL_SAVED");
                        MAIN.post(() -> {
                            if (active == null) state(automatic ? "AUTO_SAVED" : "SAVED");
                            CaptureNotifications.update(); Ui.toast(context, "新增成功");
                        });
                    } catch (RuntimeException error) {
                        ReferenceDiagnostics.failure("STORE_FAILED", error);
                        MAIN.post(() -> { if (active == null) state("SAVE_FAILED"); CaptureNotifications.update(); CaptureNotifications.error(); Ui.toast(context, "新增失败，账单仍在待确认列表"); });
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
