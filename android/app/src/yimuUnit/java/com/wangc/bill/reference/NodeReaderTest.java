package com.wangc.bill.reference;

import com.wangc.bill.reference.capture.NodeReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public final class NodeReaderTest {
    private static final class Node {
        final String key;
        String text, description;
        final List<Node> children=new ArrayList<>();
        int releases;
        Node(String key, String text) { this.key=key; this.text=text; }
        Node add(Node node) { children.add(node); return this; }
    }
    private static final class Clock implements NodeReader.Clock {
        long time;
        public long now() { return time; }
    }
    private static final class Observer implements NodeReader.Observer {
        int starts, finishes, aborts, visited, texts, branches;
        public void started() { starts++; }
        public void progress(int value) { assertTrue(value>visited); visited=value; }
        public void finished(int texts, int branches) { finishes++; this.texts=texts; this.branches=branches; }
        public void aborted() { aborts++; }
    }
    private static final class ProviderFailure extends RuntimeException {}
    private static final class Adapter implements NodeReader.Adapter<Node> {
        final Clock clock;
        int childCalls;
        long childDuration, textDuration;
        Node countFailure, textFailure, descriptionFailure, childFailure;
        Adapter(Clock clock) { this.clock=clock; }
        public int childCount(Node node) { if(node==countFailure)throw new ProviderFailure(); return node.children.size(); }
        public Node child(Node node, int index) { childCalls++; if(node==childFailure)throw new ProviderFailure(); clock.time+=childDuration; return node.children.get(index); }
        public CharSequence text(Node node) { if(node==textFailure)throw new ProviderFailure(); clock.time+=textDuration; return node.text; }
        public CharSequence description(Node node) { if(node==descriptionFailure)throw new ProviderFailure(); return node.description; }
        public boolean same(Node first, Node second) { return first.key.equals(second.key); }
        public void release(Node node) { node.releases++; }
    }
    private static final class Reading {
        final Clock clock=new Clock();
        final Observer observer=new Observer();
        final Adapter adapter=new Adapter(clock);
        NodeReader.Result read(Node root, boolean descriptions) { return NodeReader.read(root,descriptions,adapter,clock,observer); }
        void aborts(Node root, NodeReader.Reason reason) {
            try { read(root,true); fail("A partial tree reached the parser"); }
            catch(NodeReader.Aborted error) { assertEquals(reason,error.reason); }
            assertEquals(1,observer.starts); assertEquals(0,observer.finishes); assertEquals(1,observer.aborts);
            assertEquals(0,root.releases);
        }
    }

    @Test public void preservesLeafOrderAndDescriptionFallbackWithoutReadingRootOrContainerText() {
        Node root=new Node("root","not a leaf");
        Node container=new Node("container","also ignored");
        Node first=new Node("first","支付成功"),second=new Node("second",""); second.description=" 12.30 ";
        Node third=new Node("third","实验商户"); third.description="ignored description";
        root.add(container.add(first).add(second)).add(third);
        Reading reading=new Reading();
        NodeReader.Result result=reading.read(root,true);
        assertEquals(Arrays.asList("支付成功"," 12.30 ","实验商户"),result.texts);
        assertEquals(5,result.visited); assertEquals(2,result.branches);
        assertEquals(1,reading.observer.finishes); assertEquals(0,reading.observer.aborts);
        assertEquals(0,root.releases);
        for(Node child:Arrays.asList(container,first,second,third))assertEquals(1,child.releases);
    }
    @Test public void textOnlyReadersDoNotUseDescriptions() {
        Node root=new Node("root",null),leaf=new Node("leaf",null); leaf.description="支付成功"; root.add(leaf);
        assertTrue(new Reading().read(root,false).texts.isEmpty());
    }
    @Test public void aLeafSourceStillDoesNotSupplyItsOwnText() {
        Node root=new Node("root","支付成功");
        NodeReader.Result result=new Reading().read(root,true);
        assertTrue(result.texts.isEmpty()); assertEquals(1,result.visited); assertEquals(0,root.releases);
    }
    @Test public void everyWideLeafCountsAgainstTheGlobalNodeLimit() {
        Node root=new Node("root",null);
        for(int i=0;i<3000;i++)root.add(new Node("leaf"+i,null));
        Reading reading=new Reading(); reading.aborts(root,NodeReader.Reason.NODE_LIMIT);
        assertEquals(2048,reading.observer.visited); assertEquals(2047,reading.adapter.childCalls);
        for(int i=0;i<root.children.size();i++)assertEquals(i<2047?1:0,root.children.get(i).releases);
    }
    @Test public void wideTextLeavesRejectTheWholeTreeInsteadOfReturningATruncatedAmountList() {
        Node root=new Node("root",null);
        for(int i=0;i<600;i++)root.add(new Node("leaf"+i,""+i));
        Reading reading=new Reading(); reading.aborts(root,NodeReader.Reason.TEXT_LIMIT);
        assertEquals(513,reading.adapter.childCalls);
        for(int i=0;i<root.children.size();i++)assertEquals(i<513?1:0,root.children.get(i).releases);
    }
    @Test public void depthLimitPreventsRecursiveStackGrowthAndReleasesEveryAcquiredNode() {
        Node root=new Node("root",null),last=root; List<Node> children=new ArrayList<>();
        for(int i=0;i<1000;i++){Node child=new Node("level"+i,null);last.add(child);last=child;children.add(child);}
        Reading reading=new Reading();reading.aborts(root,NodeReader.Reason.DEPTH_LIMIT);
        assertEquals(64,reading.adapter.childCalls);
        for(int i=0;i<children.size();i++)assertEquals(i<64?1:0,children.get(i).releases);
    }
    @Test public void logicallyEqualAncestorHandlesAreRejectedAndOwnedHandleIsReleased() {
        Node root=new Node("root",null),child=new Node("child",null),alias=new Node("root",null);
        root.add(child.add(alias));Reading reading=new Reading();reading.aborts(root,NodeReader.Reason.CYCLE);
        assertEquals(1,child.releases);assertEquals(1,alias.releases);
    }
    @Test public void anAncestorReturnedByIdentityIsNotReleasedTwice() {
        Node root=new Node("root",null),child=new Node("child",null); root.add(child.add(root));
        Reading reading=new Reading();reading.aborts(root,NodeReader.Reason.CYCLE);
        assertEquals(1,child.releases);assertEquals(0,root.releases);
    }
    @Test public void elapsedTimeIsCheckedAfterAnExpensiveChildLookupAndReturnedNodeIsReleased() {
        Node root=new Node("root",null),child=new Node("child","12.30");root.add(child);
        Reading reading=new Reading();reading.adapter.childDuration=2000;
        reading.aborts(root,NodeReader.Reason.TIME_LIMIT);assertEquals(1,child.releases);
    }
    @Test public void elapsedTimeIsCheckedAfterTextRetrievalBeforeReturningToTheParser() {
        Node root=new Node("root",null),child=new Node("child","12.30");root.add(child);
        Reading reading=new Reading();reading.adapter.textDuration=2000;
        reading.aborts(root,NodeReader.Reason.TIME_LIMIT);assertEquals(1,child.releases);
    }
    @Test public void providerCountFailureReleasesTheOwnedNodeAndDoesNotHideTheError() {
        Node root=new Node("root",null),child=new Node("child","12.30");root.add(child);
        Reading reading=new Reading();reading.adapter.countFailure=child;
        providerFails(reading,root);assertEquals(1,child.releases);
    }
    @Test public void providerTextFailureReleasesNodesStillOnTheTraversalStack() {
        Node root=new Node("root",null),container=new Node("container",null),leaf=new Node("leaf","12.30");root.add(container.add(leaf));
        Reading reading=new Reading();reading.adapter.textFailure=leaf;
        providerFails(reading,root);assertEquals(1,container.releases);assertEquals(1,leaf.releases);
    }
    @Test public void providerDescriptionFailureAlsoAbortsTheTree() {
        Node root=new Node("root",null),leaf=new Node("leaf",null);root.add(leaf);
        Reading reading=new Reading();reading.adapter.descriptionFailure=leaf;
        providerFails(reading,root);assertEquals(1,leaf.releases);
    }
    @Test public void nullChildLookupsCannotBypassTheNodeLimit() {
        Node root=new Node("root",null); for(int i=0;i<3000;i++)root.add(null);
        Reading reading=new Reading();reading.aborts(root,NodeReader.Reason.NODE_LIMIT);
        assertEquals(2047,reading.adapter.childCalls);
    }
    @Test public void providerChildFailureCleansUpAncestors() {
        Node root=new Node("root",null),container=new Node("container",null);root.add(container.add(new Node("leaf",null)));
        Reading reading=new Reading();reading.adapter.childFailure=container;
        providerFails(reading,root);assertEquals(1,container.releases);
    }
    @Test public void interruptionStopsBeforeReadingAndPreservesTheInterruptFlag() {
        Node root=new Node("root",null);Reading reading=new Reading();Thread.currentThread().interrupt();
        try{reading.aborts(root,NodeReader.Reason.INTERRUPTED);assertTrue(Thread.currentThread().isInterrupted());}
        finally{Thread.interrupted();}
    }
    private static void providerFails(Reading reading,Node root) {
        try{reading.read(root,true);fail("Provider failure was suppressed");}catch(ProviderFailure expected){}
        assertEquals(1,reading.observer.aborts);assertEquals(0,reading.observer.finishes);assertEquals(0,root.releases);
    }
}
