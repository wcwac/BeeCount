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
    private static final java.util.Set<String> KNOWN_PACKAGES=java.util.Collections.unmodifiableSet(AutoApps.allPackages());
    private static final java.util.Map<String,Long> eventsByPackage=new java.util.LinkedHashMap<>();
    private ReferenceDiagnostics() {}
    public static synchronized void clear(){started=System.currentTimeMillis();events=reads=attempts=drafts=skipped=lastEvent=lastRead=lastResult=failures=lastFailure=0;texts=visited=0;sourcePackage=page=failureCode=failureType="";result="NO_EVENT";eventsByPackage.clear();}
    public static synchronized void failure(String code,Exception error){result(code);failureCode=code;failureType=error.getClass().getName();lastFailure=System.currentTimeMillis();failures++;}
    public static synchronized void event(String pkg, String cls) {
        events++; lastEvent = System.currentTimeMillis(); sourcePackage = pkg;
        String bucket=KNOWN_PACKAGES.contains(pkg)?pkg:"other";Long previous=eventsByPackage.get(bucket);eventsByPackage.put(bucket,previous==null?1:previous+1);
        page = cls != null && cls.matches("[A-Za-z0-9_.$]+") ? cls.substring(0, Math.min(200, cls.length())) : "unknown";
        result = "EVENT_RECEIVED";
    }
    public static synchronized void read(int count, int nodes) { reads++; lastRead = System.currentTimeMillis(); texts = count; visited = nodes; result = count == 0 ? "EMPTY_TREE" : "TEXT_READ"; }
    public static synchronized long readCount() { return reads; }
    public static synchronized long attemptCount() { return attempts; }
    public static synchronized void attempt() { attempts++; }
    public static synchronized void result(String value) { result = value; lastResult = System.currentTimeMillis(); if (value.equals("DRAFT_SAVED")) drafts++; }
    public static synchronized void skipped() { skipped++; result("QUEUE_FULL"); }
    public static synchronized JSONObject snapshot() {
        return Json.object("startedAt", started, "events", events, "reads", reads, "submissionAttempts", attempts,
            "drafts", drafts, "skipped", skipped, "lastEventAt", lastEvent, "lastReadAt", lastRead,
            "lastResultAt", lastResult, "package", sourcePackage, "pageClass", page, "textCount", texts,
            "visitedBranches", visited, "result", result,"failures",failures,"lastFailureAt",lastFailure,"lastFailureCode",failureCode,"lastFailureType",failureType,"eventsByPackage",new JSONObject(new java.util.LinkedHashMap<>(eventsByPackage)));
    }
}
