package com.wangc.bill.reference.capture;

import com.wangc.bill.reference.Json;
import org.json.JSONObject;

/** Fixed-size metadata only. No node text, monetary values, merchants or account names. */
public final class ReferenceDiagnostics {
    private static long started = System.currentTimeMillis();
    private static long events, reads, attempts, drafts, skipped, lastEvent, lastRead, lastResult;
    private static int texts, visited;
    private static String sourcePackage = "", page = "", result = "NO_EVENT";
    private static String failureCode="",failureType="";
    private static long failures,lastFailure;
    private static long readsStarted,readsAborted,coalesced,evicted,activeSince,queueWaitMs;
    private static int liveVisited;
    private static int activeEventType;
    private static boolean readInProgress;
    private static String activeStage="IDLE",activePackage="",activePage="";
    private static long eventSourceNull,eventSourceErrors,sourceLookups,sourceWindows,sourceErrors,fallbackAttempts,fallbackSuccess;
    private static String lastEventSourceErrorType="",lastSourceRoute="NONE",lastSourceReason="UNAVAILABLE",lastSourceErrorType="",lastDraftSourceRoute="NONE",activeSourceRoute="NONE";
    private static int lastSourceWindowId=-1;
    private static Thread activeThread;
    private static final java.util.Set<String> KNOWN_PACKAGES=java.util.Collections.unmodifiableSet(AutoApps.allPackages());
    private static final java.util.Map<String,Long> eventsByPackage=new java.util.LinkedHashMap<>();
    private static final java.util.Map<String,Long> widgetContentReadsByPackage=new java.util.LinkedHashMap<>();
    private ReferenceDiagnostics() {}
    public static synchronized void clear(){started=System.currentTimeMillis();events=reads=attempts=drafts=skipped=lastEvent=lastRead=lastResult=failures=lastFailure=readsStarted=readsAborted=coalesced=evicted=0;texts=visited=0;sourcePackage=page=failureCode=failureType="";result="NO_EVENT";eventsByPackage.clear();widgetContentReadsByPackage.clear();eventSourceNull=eventSourceErrors=sourceLookups=sourceWindows=sourceErrors=fallbackAttempts=fallbackSuccess=0;lastEventSourceErrorType=lastSourceErrorType="";lastSourceRoute=lastDraftSourceRoute="NONE";lastSourceReason="UNAVAILABLE";lastSourceWindowId=-1;}
    public static synchronized void failure(String code,Exception error){result(code);failureCode=code;failureType=error.getClass().getName();lastFailure=System.currentTimeMillis();failures++;}
    public static synchronized void event(String pkg, String cls) {
        events++; lastEvent = System.currentTimeMillis(); sourcePackage = pkg;
        String bucket=KNOWN_PACKAGES.contains(pkg)?pkg:"other";Long previous=eventsByPackage.get(bucket);eventsByPackage.put(bucket,previous==null?1:previous+1);
        page = cls != null && cls.matches("[A-Za-z0-9_.$]+") ? cls.substring(0, Math.min(200, cls.length())) : "unknown";
        result = "EVENT_RECEIVED";
    }
    public static synchronized void read(int count, int nodes) { reads++; lastRead = System.currentTimeMillis(); texts = count; visited = nodes; result = count == 0 ? "EMPTY_TREE" : "TEXT_READ"; }
    public static synchronized void eventSource(boolean present,RuntimeException error){
        if(error!=null){eventSourceErrors++;lastEventSourceErrorType=className(error.getClass().getName());}
        else if(!present)eventSourceNull++;
    }
    public static synchronized void processingStarted(String pkg,String cls,long waited,int eventType){activeSince=System.nanoTime();queueWaitMs=waited;activePackage=pkg;activePage=cls!=null&&cls.matches("[A-Za-z0-9_.$]+")?cls.substring(0,Math.min(200,cls.length())):"unknown";activeEventType=eventType;activeStage="DISPATCHING_RULES";activeSourceRoute="NONE";activeThread=Thread.currentThread();}
    public static synchronized void resolvingSource(){activeStage="RESOLVING_SOURCE";}
    public static synchronized void sourceResolved(String route,String reason,int lookups,int windows,int errors,String errorType,int windowId){
        // These values describe fixed resolver states, never app-provided strings or node text.
        lastSourceRoute=enumName(route,EventSourceResolver.Route.class,"NONE");
        lastSourceReason=enumName(reason,EventSourceResolver.Reason.class,"UNAVAILABLE");
        activeSourceRoute=lastSourceRoute;lastSourceWindowId=windowId;
        sourceLookups+=Math.max(0,lookups);sourceWindows+=Math.max(0,windows);sourceErrors+=Math.max(0,errors);
        lastSourceErrorType=className(errorType);
        if(lookups>0)fallbackAttempts++;
        if(lastSourceRoute.equals("ACTIVE_ROOT")||lastSourceRoute.equals("INTERACTIVE_WINDOW"))fallbackSuccess++;
        activeStage="DISPATCHING_RULES";
    }
    public static synchronized void processingFinished(){activeStage="IDLE";activeSince=0;activePackage=activePage="";activeEventType=0;activeThread=null;readInProgress=false;activeSourceRoute="NONE";}
    public static synchronized void readStarted(){readsStarted++;readInProgress=true;liveVisited=0;activeStage="READING_NODES";}
    public static synchronized void readProgress(int nodes){liveVisited=nodes;}
    public static synchronized void readFinished(int count,int branches){readInProgress=false;activeStage="PARSING_RULES";read(count,branches);if(count>0&&activeEventType==2048&&activePage.startsWith("android.widget.")){String bucket=KNOWN_PACKAGES.contains(activePackage)?activePackage:"other";Long previous=widgetContentReadsByPackage.get(bucket);widgetContentReadsByPackage.put(bucket,previous==null?1:previous+1);}}
    public static synchronized void readAborted(){readsAborted++;readInProgress=false;activeStage="READ_ABORTED";}
    public static synchronized void queueChanged(int merged,int removed){coalesced+=merged;evicted+=removed;}
    public static synchronized long readCount() { return reads; }
    public static synchronized long attemptCount() { return attempts; }
    public static synchronized void attempt() { attempts++; }
    public static synchronized void result(String value) { result = value; lastResult = System.currentTimeMillis(); if (value.equals("DRAFT_SAVED")){drafts++;lastDraftSourceRoute=activeSourceRoute;} }
    public static synchronized void skipped() { skipped++; result("QUEUE_FULL"); }
    public static synchronized JSONObject snapshot() {
        return Json.object("startedAt", started, "events", events, "reads", reads, "submissionAttempts", attempts,
            "drafts", drafts, "skipped", skipped, "lastEventAt", lastEvent, "lastReadAt", lastRead,
            "lastResultAt", lastResult, "package", sourcePackage, "pageClass", page, "textCount", texts,
            "visitedBranches", visited, "result", result,"failures",failures,"lastFailureAt",lastFailure,"lastFailureCode",failureCode,"lastFailureType",failureType,"eventsByPackage",new JSONObject(new java.util.LinkedHashMap<>(eventsByPackage)),
            "readsStarted",readsStarted,"readsAborted",readsAborted,"readInProgress",readInProgress,"liveVisitedNodes",liveVisited,
            "coalesced",coalesced,"evicted",evicted,"activeStage",activeStage,"activePackage",activePackage,"activePageClass",activePage,"activeEventType",activeEventType,"widgetContentReadsByPackage",new JSONObject(widgetContentReadsByPackage),
            "eventSourceNull",eventSourceNull,"eventSourceErrors",eventSourceErrors,"lastEventSourceErrorType",lastEventSourceErrorType,
            "lastSourceRoute",lastSourceRoute,"lastSourceReason",lastSourceReason,"lastSourceWindowId",lastSourceWindowId,"sourceLookups",sourceLookups,"sourceWindows",sourceWindows,"sourceErrors",sourceErrors,"lastSourceErrorType",lastSourceErrorType,
            "fallbackAttempts",fallbackAttempts,"fallbackSuccess",fallbackSuccess,"lastDraftSourceRoute",lastDraftSourceRoute,
            "activeForMs",activeSince==0?0:(System.nanoTime()-activeSince)/1_000_000,"queueWaitMs",queueWaitMs,"workerStack",workerStack());
    }
    private static String className(String value){return value!=null&&value.matches("[A-Za-z0-9_.$]+")?value.substring(0,Math.min(200,value.length())):"";}
    private static <E extends Enum<E>> String enumName(String value,Class<E> type,String fallback){try{return Enum.valueOf(type,value).name();}catch(IllegalArgumentException|NullPointerException ignored){return fallback;}}
    private static org.json.JSONArray workerStack(){org.json.JSONArray frames=new org.json.JSONArray();Thread thread=activeThread;if(thread==null)return frames;try{StackTraceElement[] stack=thread.getStackTrace();for(int i=0;i<Math.min(8,stack.length);i++){StackTraceElement frame=stack[i];String cls=frame.getClassName(),method=frame.getMethodName();if(cls.matches("[A-Za-z0-9_.$]+")&&method.matches("[A-Za-z0-9_$<>]+"))frames.put(cls+"."+method+":"+frame.getLineNumber());}}catch(RuntimeException ignored){}return frames;}
}
