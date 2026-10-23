package com.wangc.bill.reference;

import android.app.Activity;
import androidx.test.core.app.ActivityScenario;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/** Captures only the disposable CI emulator's project-owned screen checkpoints. */
public final class ScreenEvidenceRule extends TestWatcher {
    private ScreenEvidence.Session session;

    @Override protected void starting(Description description) {
        try { session=ScreenEvidence.open(description); }
        catch(Exception error) { throw new AssertionError("Cannot initialize cloud screen evidence",error); }
    }

    public <A extends Activity> void checkpoint(ActivityScenario<A> scenario,String scene) {
        try { session.checkpoint(scenario,scene); }
        catch(Exception error) { throw new AssertionError("Cannot capture actual screen checkpoint: "+scene,error); }
    }

    public void unshownTemplates(String... names) {
        try { session.unshownTemplates(names); }
        catch(Exception error) { throw new AssertionError("Cannot record unshown template coverage",error); }
    }

    @Override protected void failed(Throwable error,Description description) {
        if(session!=null)session.failed(error);
    }

    @Override protected void succeeded(Description description) {
        if(session!=null)session.succeeded();
    }

    @Override protected void finished(Description description) {
        if(session!=null)session.close();
    }
}
