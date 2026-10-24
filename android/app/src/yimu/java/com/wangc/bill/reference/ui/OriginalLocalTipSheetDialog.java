package com.wangc.bill.reference.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/** Shared recovered h1/b8/view.l behavior for the six-entry and five-entry local guides. */
final class OriginalLocalTipSheetDialog extends BottomSheetDialog {
    private final String[][] rows;

    OriginalLocalTipSheetDialog(Context context, String[][] values) {
        // Explicit existing skin bridge: configuration night follows current host
        // resources; the original predicate is its separate skin-name == "night".
        super(context, requiredStyle(context,
                (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                        == Configuration.UI_MODE_NIGHT_YES ? "SheetDialogStyleNight" : "SheetDialogStyle"));
        if (values == null) throw new IllegalArgumentException("Local guide data is required");
        rows = new String[values.length][2];
        for (int index = 0; index < values.length; index++) {
            if (values[index] == null || values[index].length != 2) throw new IllegalArgumentException("Local guide row requires title and content");
            rows[index][0] = values[index][0]; rows[index][1] = values[index][1];
        }
        View root = Ui.layout(context, "dialog_bottom_tip");
        View close = OriginalAutoWorkScreen.required(root, "btn_close");
        close.setOnClickListener(view -> dismiss());
        View list = OriginalAutoWorkScreen.required(root, "tip_list");
        if (!(list instanceof RecyclerView recycler)) throw new IllegalStateException("原版机型提示列表缺失");
        recycler.setLayoutManager(new LinearLayoutManager(context)); recycler.setAdapter(new Adapter());
        setContentView(root);
        getBehavior().setPeekHeight((int) ((float) OriginalAutoTipDialog.screenHeight(context) * 0.5f));
        setCancelable(true); setCanceledOnTouchOutside(true);
    }

    @Override public void show() {
        super.show(); Window window = getWindow(); if (window == null) return;
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        View root = findViewById(Ui.id(getContext(), "total_layout"));
        if (root == null) root = findViewById(Ui.id(getContext(), "parent_layout"));
        if (root != null) {
            int id = getContext().getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            int navigation = id == 0 ? 0 : getContext().getResources().getDimensionPixelSize(id);
            root.setPadding(root.getPaddingLeft(), root.getPaddingTop(), root.getPaddingRight(), root.getPaddingTop() + navigation);
        }
    }

    private static int requiredStyle(Context context, String name) {
        int id = Ui.res(context, "style", name);
        if (id == 0) throw new IllegalStateException("原版机型提示样式缺失：" + name);
        return id;
    }
    private static final class Holder extends RecyclerView.ViewHolder { Holder(View view) { super(view); } }
    private final class Adapter extends RecyclerView.Adapter<Holder> {
        @Override public Holder onCreateViewHolder(ViewGroup parent, int type) {
            return new Holder(OriginalAutoTipDialog.inflate(LayoutInflater.from(parent.getContext()), parent, "item_dialog_tip"));
        }
        @Override public void onBindViewHolder(Holder holder, int position) {
            TextView title = OriginalAutoTipDialog.text(holder.itemView, "title");
            title.setVisibility(View.VISIBLE); title.setText(rows[position][0]);
            TextView content = OriginalAutoTipDialog.text(holder.itemView, "content"); content.setText(rows[position][1]);
            int id = Ui.res(content.getContext(), "color", "darkGrey");
            if (id == 0) throw new IllegalStateException("原版机型提示颜色缺失");
            content.setTextColor(content.getContext().getColor(id));
        }
        @Override public int getItemCount() { return rows.length; }
    }
}
