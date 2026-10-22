package com.wangc.bill.reference.capture;

import com.wangc.bill.reference.Json;
import org.json.JSONObject;

/** Fixed-size metadata only. No node text, monetary values, merchants or account names. */
public final class ReferenceDiagnostics {
    private static long started = System.currentTimeMillis();
    private static long events, reads, attempts, drafts, skipped, lastEvent, lastRead, lastResult;
    private static int texts, visited;
    private static String sourcePackage = "", page = "", result = "NO_EVENT";
    private ReferenceDiagnostics() {}
    public static synchronized void clear(){started=System.currentTimeMillis();events=reads=attempts=drafts=skipped=lastEvent=lastRead=lastResult=0;texts=visited=0;sourcePackage=page="";result="NO_EVENT";}
    public static synchronized void event(String pkg, String cls) {
        events++; lastEvent = System.currentTimeMillis(); sourcePackage = pkg;
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
            "visitedBranches", visited, "result", result);
    }
}
