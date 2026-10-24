package com.wangc.bill.reference;

import com.wangc.bill.reference.capture.CaptureEvidence;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public final class CaptureEvidenceTest {
    @Test public void dispatchCleanupPreventsAnotherApplicationInheritingItsPaymentId() {
        try {
            CaptureEvidence.begin("wechat","detail",7,2048,"ACTIVE_ROOT");
            CaptureEvidence.observeTexts(Arrays.asList("交易单号","PAY202610230001"));
            assertEquals(2,CaptureEvidence.current().tokens().size());
            CaptureEvidence.clear();assertNull(CaptureEvidence.current());
            CaptureEvidence.begin("meituan","payment",8,32,"INTERACTIVE_WINDOW");
            assertTrue(CaptureEvidence.current().tokens().isEmpty());
        } finally {CaptureEvidence.clear();}
    }
    @Test public void snapshotsAreBoundedAndCannotBeMutatedByTheParser() {
        try {
            CaptureEvidence.begin("wechat","detail",7,2048,"ACTIVE_ROOT");
            String[] tokens=new String[5000];Arrays.fill(tokens,new String(new char[2000]).replace('\0','a'));
            CaptureEvidence.observeTexts(Arrays.asList(tokens));
            int characters=0;for(String token:CaptureEvidence.current().tokens()){assertTrue(token.length()<=512);characters+=token.length();}
            assertTrue(characters<=65536);assertTrue(CaptureEvidence.current().tokens().size()<=2048);
            try{CaptureEvidence.current().tokens().clear();fail("Evidence must be immutable");}catch(UnsupportedOperationException expected){}
        } finally {CaptureEvidence.clear();}
    }
    @Test public void evidenceIsThreadLocalAndRequiresAnActiveDispatch() throws Exception {
        CaptureEvidence.clear();CaptureEvidence.observeTexts(Arrays.asList("交易单号","PAY202610230001"));assertNull(CaptureEvidence.current());
        try {
            CaptureEvidence.begin("wechat","detail",7,2048,"ACTIVE_ROOT");
            java.util.concurrent.atomic.AtomicReference<CaptureEvidence.Context> other=new java.util.concurrent.atomic.AtomicReference<>();
            Thread thread=new Thread(()->other.set(CaptureEvidence.current()));thread.start();thread.join();assertNull(other.get());
        } finally {CaptureEvidence.clear();}
    }
}
