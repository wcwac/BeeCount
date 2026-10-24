package com.wangc.bill.reference.ui;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;

/**
 * Host bridge for the unresolved native UtilsTransActivity onCreate/result dispatch.
 * The readable c0 delegate supplies request1, finish-before-check and fresh-state
 * callbacks; this Activity's registration/lifecycle policy is deliberately named host code.
 */
public final class OriginalPermissionRequestActivity extends Activity {
    static final String EXTRA_TOKEN = "reference_notification_request";
    static final int REQUEST_CODE = 1;
    private String token;
    private boolean requested, resolved;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        token = state == null ? getIntent().getStringExtra(EXTRA_TOKEN) : state.getString(EXTRA_TOKEN);
        requested = state != null && state.getBoolean("requested");
        if (!OriginalNotificationPermission.contains(token)) {
            resolved = true; OriginalNotificationPermission.missingTransientState(); finish(); return;
        }
        if (!requested) {
            requested = true;
            // i2.u installs no optional rationale or settings hook, including
            // when a previous denial makes the OS return without another dialog.
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE);
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (resolved) return;
        resolved = true; finish();
        // c0.e ignores all three supplied result arguments and rechecks permission.
        OriginalNotificationPermission.permissionResult(token);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString(EXTRA_TOKEN, token); state.putBoolean("requested", requested);
        super.onSaveInstanceState(state);
    }

    @Override protected void onDestroy() {
        // A normal result already consumed the callback. A configuration-only
        // transient recreation retains its token; process death cannot restore it.
        if (!resolved && !isChangingConfigurations()) OriginalNotificationPermission.transientDestroyed(token);
        super.onDestroy();
    }
}
