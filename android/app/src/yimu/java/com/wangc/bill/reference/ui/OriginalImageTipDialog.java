package com.wangc.bill.reference.ui;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.fragment.app.DialogFragment;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Recovered ImageTip window/arguments/buttons, plus an explicit local image-loading host bridge.
 * The Canvas RoundedCorners algorithm is source-backed. Decoder sampling, bitmap ownership and
 * cancellation are this host's bounded bridge, not a claim to reproduce Glide's complete pipeline.
 */
public final class OriginalImageTipDialog extends DialogFragment {
    private static final ExecutorService DECODER = Executors.newSingleThreadExecutor();
    private static final ReentrantLock DRAW_LOCK = new ReentrantLock();
    private final Handler main = new Handler(Looper.getMainLooper());
    private int imageResource;
    private long generation;
    private ImageView image;
    private View.OnLayoutChangeListener layoutListener;
    private Future<?> decoding;
    private Bitmap displayed;

    public static OriginalImageTipDialog create(int imageRes) {
        OriginalImageTipDialog dialog = new OriginalImageTipDialog();
        Bundle arguments = new Bundle();
        arguments.putInt("imageRes", imageRes);
        dialog.setArguments(arguments);
        return dialog;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        imageResource = requireArguments().getInt("imageRes");
        OriginalAutoTipDialog.style(this);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View root = OriginalAutoTipDialog.inflate(inflater, container, "dialog_image_tip");
        View target = Ui.view(root, "tip_image");
        if (!(target instanceof ImageView)) throw new IllegalStateException("原版说明图片控件缺失");
        image = (ImageView) target;
        OriginalAutoTipDialog.text(root, "confirm_btn").setOnClickListener(button -> {
            Dialog dialog = getDialog();
            if (dialog != null && dialog.isShowing()) dismiss();
        });
        long request = ++generation;
        layoutListener = (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (image != null && image.getWidth() > 0 && image.getHeight() > 0) {
                image.removeOnLayoutChangeListener(layoutListener);
                layoutListener = null;
                startLocalDecode(image, request);
            }
        };
        image.addOnLayoutChangeListener(layoutListener);
        return root;
    }

    @Override public void onResume() {
        Dialog dialog = requireDialog();
        Window window = OriginalAutoTipDialog.window(this);
        WindowManager.LayoutParams parameters = window.getAttributes();
        parameters.width = OriginalAutoTipDialog.screenWidth(requireContext())
                - OriginalDimensions.dp(60f);
        parameters.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(parameters);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        super.onResume();
    }

    private void startLocalDecode(ImageView target, long request) {
        Resources resources = target.getResources();
        int width = target.getWidth();
        int height = target.getHeight();
        int resource = imageResource;
        int radius = OriginalDimensions.dp(10f);
        decoding = DECODER.submit(() -> {
            Bitmap decoded = null;
            Bitmap result = null;
            try {
                // Host bridge: target-aware power-of-two decoding with Android resource density.
                // Glide Downsampler's exact density/downsample defaults have NOT been equated.
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(resources, resource, bounds);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = sample(bounds.outWidth, bounds.outHeight, width, height);
                decoded = BitmapFactory.decodeResource(resources, resource, options);
                if (decoded == null) throw new IllegalStateException("本地说明图片解码失败");
                if (Thread.currentThread().isInterrupted()) return;
                result = roundCorners(decoded, radius);
                Bitmap ready = result;
                result = null;
                main.post(() -> {
                    if (generation != request || image != target) {
                        ready.recycle();
                        return;
                    }
                    displayed = ready;
                    target.setImageBitmap(ready);
                });
            } catch (RuntimeException failure) {
                // A cancelled old view must not fail the next view. Active request
                // failures remain an explicit host fail-fast policy, not Glide parity.
                main.post(() -> {
                    if (generation == request && image == target) throw failure;
                });
            } catch (Error failure) {
                // Fatal VM/resource errors are not ordinary request cancellation.
                // This separate host fail-fast policy applies even after detachment.
                main.post(() -> { throw failure; });
            } finally {
                if (decoded != null && !decoded.isRecycled()) decoded.recycle();
                if (result != null && !result.isRecycled()) result.recycle();
            }
        });
    }

    private static int sample(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        int value = 1;
        while (value <= Integer.MAX_VALUE / 2
                && sourceWidth / (value * 2) >= targetWidth
                && sourceHeight / (value * 2) >= targetHeight) value *= 2;
        return value;
    }

    /** g0.c -> i0.q/s -> i0$a.a: same-size CLAMP shader and antialiased round rect. */
    static Bitmap roundCorners(Bitmap input, int radius) {
        if (radius <= 0) throw new IllegalArgumentException("roundingRadius must be greater than 0.");
        Bitmap.Config config = input.getConfig() == Bitmap.Config.RGBA_F16
                ? Bitmap.Config.RGBA_F16 : Bitmap.Config.ARGB_8888;
        Bitmap safe = input;
        if (config != input.getConfig()) {
            safe = Bitmap.createBitmap(input.getWidth(), input.getHeight(), config);
            new Canvas(safe).drawBitmap(input, 0f, 0f, null);
        }
        Bitmap output = Bitmap.createBitmap(safe.getWidth(), safe.getHeight(), config);
        output.setHasAlpha(true);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(safe, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        RectF bounds = new RectF(0f, 0f, output.getWidth(), output.getHeight());
        boolean completed = false;
        DRAW_LOCK.lock();
        try {
            Canvas canvas = new Canvas(output);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.drawRoundRect(bounds, radius, radius, paint);
            canvas.setBitmap(null);
            completed = true;
        } finally {
            DRAW_LOCK.unlock();
            // Host owns allocations rather than reusing Glide's bitmap pool.
            if (safe != input) safe.recycle();
            if (!completed) output.recycle();
        }
        return output;
    }

    @Override public void onDestroyView() {
        ++generation;
        if (decoding != null) decoding.cancel(true);
        decoding = null;
        if (image != null) {
            if (layoutListener != null) image.removeOnLayoutChangeListener(layoutListener);
            image.setImageDrawable(null);
        }
        layoutListener = null;
        image = null;
        if (displayed != null && !displayed.isRecycled()) displayed.recycle();
        displayed = null;
        super.onDestroyView();
    }
}
