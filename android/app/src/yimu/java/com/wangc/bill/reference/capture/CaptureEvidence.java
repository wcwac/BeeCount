package com.wangc.bill.reference.capture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Ephemeral evidence for one dispatch. Never traverses nodes or writes page text to diagnostics. */
public final class CaptureEvidence {
    private static final int MAX_TOKENS = 2048, MAX_CHARACTERS = 65536, MAX_TOKEN = 512;
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();
    public static final class Context {
        public final String packageName, pageClass, sourceRoute, sessionId;
        public final int windowId, eventType;
        private List<String> tokens = Collections.emptyList();
        private Context(String pkg, String page, int window, int event, String route, String session) {
            packageName=pkg; pageClass=page; windowId=window; eventType=event; sourceRoute=route;sessionId=session;
        }
        public List<String> tokens() { return tokens; }
    }
    private CaptureEvidence() {}
    public static void begin(String pkg, String page, int window, int event, String route) {
        begin(pkg,page,window,event,route,"");
    }
    public static void begin(String pkg, String page, int window, int event, String route, String session) {
        CURRENT.set(new Context(pkg,page,window,event,route,session));
    }
    public static Context current() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }
    /** Called only after the existing reader has completed; newest successful full read wins. */
    public static void observeTexts(List<String> texts) {
        Context context=CURRENT.get();
        if(context==null || texts==null || texts.isEmpty())return;
        ArrayList<String> bounded=new ArrayList<>(); int characters=0;
        for(String value:texts) {
            if(value==null)continue;
            if(bounded.size()>=MAX_TOKENS || characters>=MAX_CHARACTERS)break;
            int length=Math.min(value.length(),Math.min(MAX_TOKEN,MAX_CHARACTERS-characters));
            bounded.add(value.substring(0,length)); characters+=length;
        }
        context.tokens=Collections.unmodifiableList(bounded);
    }
}
