package com.wangc.bill.reference;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONObject;

/** Bounded delivery metadata only: never names, amounts, remarks, SQL or remote input. */
public final class OriginalRepaymentWidgetDiagnostics {
    private OriginalRepaymentWidgetDiagnostics(){}
    private static final Map<Integer,Trace> TRACES=new LinkedHashMap<>();
    private static final class Trace {int requested,created,changed,rows,rowRequests,emptyRequests,destroyed;String failure="";}
    private static Trace trace(int id){Trace value=TRACES.get(id);if(value==null){if(TRACES.size()>=16){Iterator<Integer> oldest=TRACES.keySet().iterator();oldest.next();oldest.remove();}value=new Trace();TRACES.put(id,value);}return value;}
    static synchronized void requested(int id){trace(id).requested++;}
    static synchronized void created(int id){trace(id).created++;}
    static synchronized void changed(int id,int rows){Trace t=trace(id);t.changed++;t.rows=rows;}
    static synchronized void row(int id,boolean empty){Trace t=trace(id);t.rowRequests++;if(empty)t.emptyRequests++;}
    static synchronized void failed(int id,Exception error){trace(id).failure=error.getClass().getName();}
    static synchronized void destroyed(int id){trace(id).destroyed++;}
    public static synchronized JSONObject snapshot(int id){Trace t=TRACES.get(id);if(t==null)return Json.object("factoryRequested",0);return Json.object("factoryRequested",t.requested,"factoryCreated",t.created,"dataChanges",t.changed,"rows",t.rows,"rowRequests",t.rowRequests,"emptyRequests",t.emptyRequests,"destroyed",t.destroyed,"failureType",t.failure);}
}
