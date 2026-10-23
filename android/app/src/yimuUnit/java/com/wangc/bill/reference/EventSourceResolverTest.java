package com.wangc.bill.reference;

import com.wangc.bill.reference.capture.EventSourceResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public final class EventSourceResolverTest {
    private static final String APP="com.eg.android.AlipayGphone";
    private static final class Node {
        String packageName;
        int windowId;
        int releases;
        Node(String packageName,int windowId) { this.packageName=packageName; this.windowId=windowId; }
    }
    private static final class Window {
        final int id;
        final Node root;
        boolean application=true, active, focused, fail;
        int rootCalls, releases;
        Window(int id,Node root) { this.id=id; this.root=root; }
    }
    private static final class Clock implements EventSourceResolver.Clock {
        long time;
        @Override public long now() { return time; }
    }
    private static final class LookupFailure extends RuntimeException {}
    private static final class Provider implements EventSourceResolver.Provider<Node,Window> {
        final Clock clock=new Clock();
        final List<Window> all=new ArrayList<>();
        final List<String> operations=new ArrayList<>();
        Node active, packageFailure, nodeReleaseFailure, pooled;
        Window windowReleaseFailure;
        boolean activeFails, windowsFail, simulateRecyclePool;
        long activeDuration, windowsDuration;
        int activeCalls, windowsCalls, poolReuses;
        @Override public Node activeRoot() {
            operations.add("active"); activeCalls++; clock.time+=activeDuration;
            if(activeFails)throw new LookupFailure();
            return active;
        }
        @Override public List<Window> windows() {
            operations.add("windows"); windowsCalls++; clock.time+=windowsDuration;
            if(windowsFail)throw new LookupFailure();
            return all;
        }
        @Override public boolean isApplication(Window window) { return window.application; }
        @Override public int windowId(Window window) { return window.id; }
        @Override public boolean isActive(Window window) { return window.active; }
        @Override public boolean isFocused(Window window) { return window.focused; }
        @Override public Node windowRoot(Window window) {
            operations.add("root"); window.rootCalls++;
            if(window.fail)throw new LookupFailure();
            if(simulateRecyclePool && pooled != null && window.root != null) {
                Node recycled=pooled; pooled=null; poolReuses++;
                recycled.packageName=window.root.packageName; recycled.windowId=window.root.windowId;
                return recycled;
            }
            return window.root;
        }
        @Override public String packageName(Node node) { if(node==packageFailure)throw new LookupFailure(); return node.packageName; }
        @Override public int nodeWindowId(Node node) { return node.windowId; }
        @Override public void releaseNode(Node node) {
            operations.add("releaseNode"); node.releases++;
            if(node==nodeReleaseFailure)throw new LookupFailure();
            if(simulateRecyclePool)pooled=node;
        }
        @Override public void releaseWindow(Window window) {
            operations.add("releaseWindow"); window.releases++;
            if(window==windowReleaseFailure)throw new LookupFailure();
        }
        EventSourceResolver.Result<Node> resolve(Node source,int windowId,long age) {
            return EventSourceResolver.resolve(source,APP,windowId,age,this,clock);
        }
        EventSourceResolver.Result<Node> preferActive(Node source,int windowId,long age) {
            return EventSourceResolver.resolve(source,APP,windowId,age,this,clock,true);
        }
    }

    @Test public void missingEventSourceUsesTheSameApplicationsActiveRoot() {
        Provider provider=new Provider(); provider.active=new Node(APP,7);
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertSame(provider.active,result.node); assertEquals(EventSourceResolver.Route.ACTIVE_ROOT,result.route);
        assertEquals(EventSourceResolver.Reason.RESOLVED,result.reason);
        assertEquals(1,provider.activeCalls); assertEquals(0,provider.windowsCalls);
        assertEquals(0,result.node.releases);
    }
    @Test public void validEventSourceIsPreservedWithoutLookingUpTodaysWindow() {
        Provider provider=new Provider(); Node source=new Node(APP,7);
        EventSourceResolver.Result<Node> result=provider.resolve(source,7,100000);
        assertSame(source,result.node); assertEquals(EventSourceResolver.Route.EVENT_SOURCE,result.route);
        assertEquals(0,provider.activeCalls); assertEquals(0,provider.windowsCalls); assertEquals(0,source.releases);
    }
    @Test public void nodesWithoutAPackageAreRejectedAndReleasedAfterResolution() {
        Provider provider=new Provider(); Node source=new Node(null,7); provider.active=new Node(APP,7);
        EventSourceResolver.Result<Node> result=provider.resolve(source,7,0);
        assertSame(provider.active,result.node); assertEquals(1,source.releases); assertEquals(0,provider.active.releases);
    }
    @Test public void foreignSourcesAndActiveRootsCannotEscapeTheEventPackageScope() {
        Provider provider=new Provider(); Node source=new Node("foreign.source",7); provider.active=new Node("foreign.active",7);
        Window unrelated=new Window(6,new Node(APP,6)); Window matching=new Window(7,new Node(APP,7));
        provider.all.addAll(Arrays.asList(unrelated,matching));
        EventSourceResolver.Result<Node> result=provider.resolve(source,7,0);
        assertSame(matching.root,result.node); assertEquals(EventSourceResolver.Route.INTERACTIVE_WINDOW,result.route);
        assertEquals(1,source.releases); assertEquals(1,provider.active.releases);
        assertEquals(0,unrelated.rootCalls); assertEquals(1,matching.rootCalls);
        assertEquals(1,unrelated.releases); assertEquals(1,matching.releases); assertEquals(0,matching.root.releases);
    }
    @Test public void knownWindowIdRejectsANewerSamePackageWindow() {
        Provider provider=new Provider(); provider.active=new Node(APP,8);
        Window matching=new Window(7,new Node(APP,7)); provider.all.add(matching);
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertSame(matching.root,result.node); assertEquals(1,provider.active.releases);
    }
    @Test public void anEventSourceFromADifferentWindowIsReleasedAfterFallback() {
        Provider provider=new Provider(); Node source=new Node(APP,8); provider.active=new Node(APP,7);
        EventSourceResolver.Result<Node> result=provider.resolve(source,7,0);
        assertSame(provider.active,result.node); assertEquals(1,source.releases); assertEquals(0,provider.active.releases);
    }
    @Test public void metadataAndRootWindowIdsMustBothMatchTheEvent() {
        Provider provider=new Provider(); Node root=new Node(APP,8); Window window=new Window(7,root); provider.all.add(window);
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertNull(result.node); assertEquals(EventSourceResolver.Reason.WINDOW_MISMATCH,result.reason);
        assertEquals(1,root.releases); assertEquals(1,window.releases);
    }
    @Test public void unknownWindowIdNeverRequestsBackgroundOrNonApplicationRoots() {
        Provider provider=new Provider();
        Window background=new Window(1,new Node(APP,1));
        Window keyboard=new Window(2,new Node(APP,2)); keyboard.application=false; keyboard.active=true;
        Window foreground=new Window(3,new Node(APP,3)); foreground.focused=true;
        provider.all.addAll(Arrays.asList(background,keyboard,foreground));
        EventSourceResolver.Result<Node> result=provider.resolve(null,-1,0);
        assertSame(foreground.root,result.node);
        assertEquals(0,background.rootCalls); assertEquals(0,keyboard.rootCalls); assertEquals(1,foreground.rootCalls);
        for(Window window:provider.all)assertEquals(1,window.releases);
    }
    @Test public void absentRootsReturnUnavailableAndReleaseAllWindowMetadata() {
        Provider provider=new Provider(); Window window=new Window(7,null); provider.all.add(window);
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertNull(result.node); assertEquals(EventSourceResolver.Reason.UNAVAILABLE,result.reason);
        assertEquals(1,window.releases); assertEquals(1,result.candidateWindows);
    }
    @Test public void anExpiredEventCannotBePairedWithACurrentWindow() {
        Provider provider=new Provider(); Node stale=new Node("different.app",7); provider.active=new Node(APP,7);
        EventSourceResolver.Result<Node> result=provider.resolve(stale,7,2501);
        assertNull(result.node); assertEquals(EventSourceResolver.Reason.EXPIRED,result.reason);
        assertEquals(1,stale.releases); assertEquals(0,provider.activeCalls); assertEquals(0,provider.windowsCalls);
    }
    @Test public void aSlowRootLookupIsReleasedWithoutStartingMoreLookups() {
        Provider provider=new Provider(); provider.active=new Node(APP,7); provider.activeDuration=2001;
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertNull(result.node); assertEquals(EventSourceResolver.Reason.DEADLINE,result.reason);
        assertEquals(1,provider.active.releases); assertEquals(0,provider.windowsCalls);
    }
    @Test public void slowWindowEnumerationStillReleasesEveryReturnedWindow() {
        Provider provider=new Provider(); Window window=new Window(7,new Node(APP,7)); provider.all.add(window); provider.windowsDuration=2001;
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertNull(result.node); assertEquals(EventSourceResolver.Reason.DEADLINE,result.reason);
        assertEquals(1,window.releases); assertEquals(0,window.rootCalls); assertEquals(0,window.root.releases);
    }
    @Test public void lookupTimeCountsAgainstTheEventsFreshnessAsWellAsTheLookupBudget() {
        Provider provider=new Provider(); provider.active=new Node(APP,7); provider.activeDuration=101;
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,2400);
        assertNull(result.node); assertEquals(EventSourceResolver.Reason.EXPIRED,result.reason);
        assertEquals(1,provider.active.releases); assertEquals(0,provider.windowsCalls);
    }
    @Test public void anActiveRootLookupFailureDoesNotPreventAMatchingInteractiveWindow() {
        Provider provider=new Provider(); provider.activeFails=true; Window window=new Window(7,new Node(APP,7)); provider.all.add(window);
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertSame(window.root,result.node); assertEquals(1,result.errors);
        assertEquals(LookupFailure.class.getName(),result.lastErrorType); assertEquals(EventSourceResolver.Reason.RESOLVED,result.reason);
        assertEquals(1,window.releases); assertEquals(0,window.root.releases);
    }
    @Test public void aMalformedActiveRootIsReleasedAndDoesNotPreventFallback() {
        Provider provider=new Provider(); provider.active=new Node(APP,7); provider.packageFailure=provider.active;
        Window window=new Window(7,new Node(APP,7)); provider.all.add(window);
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertSame(window.root,result.node); assertEquals(1,provider.active.releases); assertEquals(1,result.errors);
    }
    @Test public void aFailingWindowProviderDoesNotPreventTheNextMatchingWindow() {
        Provider provider=new Provider(); Window failing=new Window(7,null); failing.fail=true;
        Window matching=new Window(7,new Node(APP,7)); provider.all.addAll(Arrays.asList(failing,matching));
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertSame(matching.root,result.node); assertEquals(1,result.errors);
        assertEquals(1,failing.releases); assertEquals(1,matching.releases);
    }
    @Test public void onlyEightInteractiveRootCandidatesCanBeRequested() {
        Provider provider=new Provider();
        for(int i=0;i<9;i++)provider.all.add(new Window(7,new Node(i==8?APP:"foreign.app",7)));
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertNull(result.node); assertEquals(8,result.candidateWindows);
        for(int i=0;i<provider.all.size();i++) {
            Window window=provider.all.get(i);
            assertEquals(i<8?1:0,window.rootCalls); assertEquals(i<8?1:0,window.root.releases); assertEquals(1,window.releases);
        }
    }
    @Test public void failedWindowEnumerationExposesOnlyTheExceptionType() {
        Provider provider=new Provider(); provider.windowsFail=true;
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertNull(result.node); assertEquals(EventSourceResolver.Reason.LOOKUP_FAILED,result.reason);
        assertEquals(1,result.errors); assertEquals(LookupFailure.class.getName(),result.lastErrorType);
    }
    @Test public void anInteractiveRootWithoutAPackageIsNeverReturned() {
        Provider provider=new Provider(); Window window=new Window(7,new Node(null,7)); provider.all.add(window);
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertNull(result.node); assertEquals(EventSourceResolver.Reason.PACKAGE_MISMATCH,result.reason);
        assertEquals(1,window.root.releases); assertEquals(1,window.releases);
    }
    @Test public void duplicateWindowMetadataIsReleasedExactlyOnce() {
        Provider provider=new Provider(); Window window=new Window(6,null); provider.all.addAll(Arrays.asList(window,window));
        assertNull(provider.resolve(null,7,0).node); assertEquals(1,window.releases); assertEquals(0,window.rootCalls);
    }
    @Test public void comparisonModePrefersTheMatchingActiveRootAndReleasesTheEventSource() {
        Provider provider=new Provider(); Node source=new Node(APP,7); provider.active=new Node(APP,7);
        EventSourceResolver.Result<Node> result=provider.preferActive(source,7,0);
        assertSame(provider.active,result.node); assertEquals(EventSourceResolver.Route.ACTIVE_ROOT,result.route);
        assertEquals(1,source.releases); assertEquals(0,provider.active.releases); assertEquals(0,provider.windowsCalls);
    }
    @Test public void comparisonModeFallsBackToTheValidEventWhenTheActiveWindowChanged() {
        Provider provider=new Provider(); Node source=new Node(APP,7); provider.active=new Node(APP,8);
        EventSourceResolver.Result<Node> result=provider.preferActive(source,7,0);
        assertSame(source,result.node); assertEquals(EventSourceResolver.Route.EVENT_SOURCE,result.route);
        assertEquals(EventSourceResolver.Reason.RESOLVED,result.reason); assertEquals(1,provider.active.releases);
        assertEquals(0,source.releases); assertEquals(0,provider.windowsCalls);
    }
    @Test public void comparisonModeDoesNotLoseTheValidEventWhenTheActiveLookupThrows() {
        Provider provider=new Provider(); Node source=new Node(APP,7); provider.activeFails=true;
        EventSourceResolver.Result<Node> result=provider.preferActive(source,7,0);
        assertSame(source,result.node); assertEquals(EventSourceResolver.Route.EVENT_SOURCE,result.route);
        assertEquals(1,result.errors); assertEquals(LookupFailure.class.getName(),result.lastErrorType);
        assertEquals(0,source.releases); assertEquals(0,provider.windowsCalls);
    }
    @Test public void comparisonModeRetainsAValidOldEventEvenWhenFreshWindowLookupIsExpired() {
        Provider provider=new Provider(); Node source=new Node(APP,7); provider.active=new Node(APP,7);
        EventSourceResolver.Result<Node> result=provider.preferActive(source,7,2501);
        assertSame(source,result.node); assertEquals(EventSourceResolver.Route.EVENT_SOURCE,result.route);
        assertEquals(EventSourceResolver.Reason.RESOLVED,result.reason);
        assertEquals(0,source.releases); assertEquals(0,provider.activeCalls); assertEquals(0,provider.windowsCalls);
    }
    @Test public void pooledNodesCannotBeRecycledAndReconfiguredWhileRootLookupIsStillRunning() {
        Provider provider=new Provider(); provider.simulateRecyclePool=true;
        Node initial=new Node("foreign.event",7); provider.active=new Node("foreign.active",7);
        Window window=new Window(7,new Node(APP,7)); provider.all.add(window);
        EventSourceResolver.Result<Node> result=provider.resolve(initial,7,0);
        assertSame(window.root,result.node); assertEquals(EventSourceResolver.Route.INTERACTIVE_WINDOW,result.route);
        assertEquals(0,provider.poolReuses); assertEquals("foreign.event",initial.packageName);
        assertEquals("foreign.active",provider.active.packageName);
        assertEquals(1,initial.releases); assertEquals(1,provider.active.releases); assertEquals(0,window.root.releases);
        assertTrue(provider.operations.indexOf("releaseNode")>provider.operations.lastIndexOf("root"));
    }
    @Test public void unsuccessfulResolutionAlsoKeepsAllCandidatesOwnedUntilTheLastLookup() {
        Provider provider=new Provider(); Node initial=new Node("foreign.event",7); provider.active=new Node("foreign.active",7);
        Window foreign=new Window(7,new Node("foreign.window",7)); Window empty=new Window(7,null);
        provider.all.addAll(Arrays.asList(foreign,empty));
        EventSourceResolver.Result<Node> result=provider.resolve(initial,7,0);
        assertNull(result.node);
        assertTrue(provider.operations.indexOf("releaseNode")>provider.operations.lastIndexOf("root"));
        assertEquals(1,initial.releases); assertEquals(1,provider.active.releases); assertEquals(1,foreign.root.releases);
        assertEquals(1,foreign.releases); assertEquals(1,empty.releases);
    }
    @Test public void rejectedNodeCleanupFailuresAreIncludedBeforeTheResultIsFrozen() {
        Provider provider=new Provider(); Node initial=new Node(APP,7); provider.active=new Node(APP,7);
        provider.nodeReleaseFailure=initial;
        EventSourceResolver.Result<Node> result=provider.preferActive(initial,7,0);
        assertSame(provider.active,result.node); assertEquals(1,result.errors);
        assertEquals(LookupFailure.class.getName(),result.lastErrorType); assertEquals(EventSourceResolver.Reason.RESOLVED,result.reason);
        assertEquals(1,initial.releases); assertEquals(0,provider.active.releases);
    }
    @Test public void windowCleanupFailuresAreIncludedAndDoNotReleaseTheSelectedRoot() {
        Provider provider=new Provider(); Window window=new Window(7,new Node(APP,7)); provider.all.add(window);
        provider.windowReleaseFailure=window;
        EventSourceResolver.Result<Node> result=provider.resolve(null,7,0);
        assertSame(window.root,result.node); assertEquals(1,result.errors);
        assertEquals(LookupFailure.class.getName(),result.lastErrorType); assertEquals(EventSourceResolver.Reason.RESOLVED,result.reason);
        assertEquals(1,window.releases); assertEquals(0,window.root.releases);
    }
}
