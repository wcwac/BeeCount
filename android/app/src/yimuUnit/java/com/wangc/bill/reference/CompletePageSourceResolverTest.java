package com.wangc.bill.reference;

import com.wangc.bill.reference.capture.EventSourceResolver;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public final class CompletePageSourceResolverTest {
    private static final class Node {
        final String pkg;final int window;int released;
        Node(String pkg,int window){this.pkg=pkg;this.window=window;}
    }
    private static final class Window {
        final int id;final Node root;boolean active;int released,reads;
        Window(int id,Node root){this.id=id;this.root=root;}
    }
    private static final class Provider implements EventSourceResolver.Provider<Node,Window>,EventSourceResolver.Clock {
        Node active;long now,activeDelay;int activeReads,windowReads;
        final List<Window> windows=new ArrayList<>();
        public long now(){return now;}
        public Node activeRoot(){activeReads++;now+=activeDelay;return active;}
        public List<Window> windows(){windowReads++;return windows;}
        public boolean isApplication(Window window){return true;}
        public int windowId(Window window){return window.id;}
        public boolean isActive(Window window){return window.active;}
        public boolean isFocused(Window window){return false;}
        public Node windowRoot(Window window){window.reads++;return window.root;}
        public String packageName(Node node){return node.pkg;}
        public int nodeWindowId(Node node){return node.window;}
        public void releaseNode(Node node){node.released++;}
        public void releaseWindow(Window window){window.released++;}
        EventSourceResolver.Result<Node> resolve(Node source,int window,long age){
            return EventSourceResolver.resolvePage(source,"selected.app",window,age,this,this);
        }
    }
    @Test public void aValidLeafSourceDoesNotPreventReadingTheCompleteSameWindow() {
        Provider p=new Provider();Node leaf=new Node("selected.app",7);p.active=new Node("selected.app",7);
        EventSourceResolver.Result<Node> result=p.resolve(leaf,7,0);
        assertSame(p.active,result.node);assertEquals(EventSourceResolver.Route.ACTIVE_ROOT,result.route);
        assertEquals(1,leaf.released);assertEquals(0,p.active.released);assertEquals(0,p.windowReads);
    }
    @Test public void aValidLeafCannotHideTheMatchingInteractiveWindowBehindAnotherActiveApp() {
        Provider p=new Provider();Node leaf=new Node("selected.app",7);p.active=new Node("another.app",8);
        Window match=new Window(7,new Node("selected.app",7));p.windows.add(match);
        EventSourceResolver.Result<Node> result=p.resolve(leaf,7,0);
        assertSame(match.root,result.node);assertEquals(EventSourceResolver.Route.INTERACTIVE_WINDOW,result.route);
        assertEquals(1,leaf.released);assertEquals(1,p.active.released);assertEquals(1,match.released);assertEquals(0,match.root.released);
    }
    @Test public void aSamePackageDifferentWindowCannotReplaceTheOriginalSource() {
        Provider p=new Provider();Node leaf=new Node("selected.app",7);p.active=new Node("selected.app",8);
        Window wrong=new Window(8,new Node("selected.app",8));p.windows.add(wrong);
        EventSourceResolver.Result<Node> result=p.resolve(leaf,7,0);
        assertSame(leaf,result.node);assertEquals(EventSourceResolver.Route.EVENT_SOURCE,result.route);
        assertEquals(0,wrong.reads);assertEquals(1,wrong.released);assertEquals(1,p.active.released);assertEquals(0,leaf.released);
    }
    @Test public void expiredEventsCannotReadANewPageButRetainTheirAlreadyOwnedSource() {
        Provider p=new Provider();Node leaf=new Node("selected.app",7);p.active=new Node("selected.app",7);
        assertSame(leaf,p.resolve(leaf,7,2501).node);assertEquals(0,p.activeReads);assertEquals(0,p.windowReads);
    }
    @Test public void aSlowCompleteRootIsReleasedWithoutExtendingTheBudgetOrDiscardingTheSource() {
        Provider p=new Provider();Node leaf=new Node("selected.app",7);p.active=new Node("selected.app",7);p.activeDelay=2001;
        assertSame(leaf,p.resolve(leaf,7,0).node);assertEquals(1,p.active.released);assertEquals(0,p.windowReads);assertEquals(0,leaf.released);
    }
    @Test public void unknownWindowNeverReadsABackgroundWindowJustBecauseThePackageMatches() {
        Provider p=new Provider();Window background=new Window(7,new Node("selected.app",7));p.windows.add(background);
        assertNull(p.resolve(null,-1,0).node);assertEquals(0,background.reads);assertEquals(1,background.released);
    }
}
