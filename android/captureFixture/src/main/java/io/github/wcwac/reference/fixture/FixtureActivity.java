package io.github.wcwac.reference.fixture;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.*;
/** Synthetic cloud-only payment UI. Contains no original payment application code. */
public abstract class FixtureActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    protected abstract String[] lines();
    @Override protected void onCreate(Bundle state){super.onCreate(state);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(24,48,24,24);for(String line:lines()){TextView text=new TextView(this);text.setText(line);text.setTextSize(22);text.setPadding(0,15,0,15);if(android.os.Build.VERSION.SDK_INT>=34)text.setAccessibilityDataSensitive(View.ACCESSIBILITY_DATA_SENSITIVE_YES);root.addView(text);}setContentView(root);
        root.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityEvent(View view,AccessibilityEvent event){super.onInitializeAccessibilityEvent(view,event);event.setClassName(FixtureActivity.this.getClass().getName());event.setPackageName(getPackageName());}});
        // New detail workflow cases use only the real Activity/widget events;
        // legacy fixture burst scenarios retain all three delayed updates.
        if (!getIntent().getBooleanExtra("fixture_single_event", false)) {
            for(long delay:new long[]{350,1200,2300})handler.postDelayed(()->root.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED),delay);
        }
    }
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
}
