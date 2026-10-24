package com.wangc.bill.reference.capture;

import android.app.ActivityManager;
import android.content.Context;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Original n0.e/f running probe, independent of system enablement and connection callbacks. */
public final class OriginalServiceState {
    private OriginalServiceState() {}

    @FunctionalInterface interface RunningServices {
        List<ActivityManager.RunningServiceInfo> read(int maximum) throws Exception;
    }

    @SuppressWarnings("deprecation")
    public static boolean isRunning(Context context) {
        return isRunning(maximum -> ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getRunningServices(maximum), ReferenceCaptureService.class.getName());
    }

    /** The original catches any Exception across lookup and iteration, but never Error. */
    static boolean isRunning(RunningServices provider, String expectedClass) {
        try {
            List<ActivityManager.RunningServiceInfo> services = provider.read(Integer.MAX_VALUE);
            if (services != null && !services.isEmpty()) {
                for (ActivityManager.RunningServiceInfo service : services)
                    if (expectedClass.equals(service.service.getClassName())) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * A host MAIN adapter for the original onCreate..onDestroy subscription.
     * Capture the existing target now: an OFF sent with no subscriber must not
     * disable a later service, and a destroyed subscriber must not be replaced.
     */
    static <T> void dispatchMasterOff(T target, Supplier<T> registered,
            Consumer<T> disable, Consumer<Runnable> main) {
        if (target == null) return;
        main.accept(() -> { if (registered.get() == target) disable.accept(target); });
    }
}
