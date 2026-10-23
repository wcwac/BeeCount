// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 20f7c529a95df463f4b93aba6229eec44232b6ac8206d03688af417bec2a1030
package com.wangc.bill.auto.action;

import android.os.Build;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
import com.wangc.bill.reference.capture.NodeReader;
import com.wangc.bill.reference.capture.ReferenceDiagnostics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class c {
    protected int a;
    protected boolean b;
    public int c;

    /* JADX INFO: renamed from: d, reason: collision with root package name */
    public String f13681d;

    protected boolean a(List<String> list, String str, boolean z5) {
        for (String str2 : list) {
            if (str2 != null) {
                if (z5) {
                    if (str2.equals(str)) {
                        return true;
                    }
                } else if (str2.contains(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean b(HashMap<String, Boolean> map, List<String> list) {
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            if (map.containsKey(it.next())) {
                return true;
            }
        }
        return false;
    }

    protected int c(List<String> list, String str, int i6, boolean z5) {
        int i7 = 1;
        for (int i8 = 0; i8 < list.size(); i8++) {
            String str2 = list.get(i8);
            if (z5) {
                if (str2.equals(str)) {
                    if (i7 >= i6) {
                        return i8;
                    }
                    i7++;
                } else {
                    continue;
                }
            } else if (str2.contains(str)) {
                if (i7 >= i6) {
                    return i8;
                }
                i7++;
            } else {
                continue;
            }
        }
        return -1;
    }

    protected int d(List<String> list, String str, boolean z5) {
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str2 = list.get(i6);
            if (z5) {
                if (str2.equals(str)) {
                    return i6;
                }
            } else if (str2.contains(str)) {
                return i6;
            }
        }
        return -1;
    }

    protected int e(List<String> list, String str, int i6, boolean z5) {
        if (i6 >= list.size() - 1) {
            return -1;
        }
        while (i6 < list.size()) {
            String str2 = list.get(i6);
            if (z5) {
                if (str2.equals(str)) {
                    return i6;
                }
            } else if (str2.contains(str)) {
                return i6;
            }
            i6++;
        }
        return -1;
    }

    protected int f(List<String> list, String str, boolean z5) {
        for (int size = list.size() - 1; size >= 0; size--) {
            String str2 = list.get(size);
            if (z5) {
                if (str2.equals(str)) {
                    return size;
                }
            } else if (str2.contains(str)) {
                return size;
            }
        }
        return -1;
    }

    protected List<String> g(AccessibilityNodeInfo accessibilityNodeInfo) {
        return i(accessibilityNodeInfo);
    }

    protected List<String> h(AccessibilityNodeInfo accessibilityNodeInfo) {
        return j(accessibilityNodeInfo);
    }

    protected List<String> i(AccessibilityNodeInfo accessibilityNodeInfo) {
        return read(accessibilityNodeInfo, false);
    }

    protected List<String> j(AccessibilityNodeInfo accessibilityNodeInfo) {
        return read(accessibilityNodeInfo, false);
    }

    protected List<String> k(AccessibilityNodeInfo accessibilityNodeInfo) {
        return read(accessibilityNodeInfo, false);
    }

    protected List<String> l(AccessibilityNodeInfo accessibilityNodeInfo) {
        return k(accessibilityNodeInfo);
    }

    protected List<String> m(AccessibilityNodeInfo accessibilityNodeInfo) {
        return n(accessibilityNodeInfo);
    }

    protected List<String> n(AccessibilityNodeInfo accessibilityNodeInfo) {
        return read(accessibilityNodeInfo, true);
    }

    private List<String> read(AccessibilityNodeInfo root, boolean descriptions) {
        this.c = 0;
        NodeReader.Result result=NodeReader.read(root, descriptions, NODES, SystemClock::elapsedRealtime, new NodeReader.Observer() {
            public void started() { ReferenceDiagnostics.readStarted(); }
            public void progress(int visited) { ReferenceDiagnostics.readProgress(visited); }
            public void finished(int texts, int branches) { c.this.c=branches; ReferenceDiagnostics.readFinished(texts, branches); }
            public void aborted() { ReferenceDiagnostics.readAborted(); }
        });
        return result.texts;
    }

    private static final NodeReader.Adapter<AccessibilityNodeInfo> NODES=new NodeReader.Adapter<>() {
        public int childCount(AccessibilityNodeInfo node) { return node.getChildCount(); }
        public AccessibilityNodeInfo child(AccessibilityNodeInfo node, int index) { return node.getChild(index); }
        public CharSequence text(AccessibilityNodeInfo node) { return node.getText(); }
        public CharSequence description(AccessibilityNodeInfo node) { return node.getContentDescription(); }
        public boolean same(AccessibilityNodeInfo first, AccessibilityNodeInfo second) { return first.equals(second); }
        @SuppressWarnings("deprecation") public void release(AccessibilityNodeInfo node) { if (Build.VERSION.SDK_INT < 33) node.recycle(); }
    };

    protected boolean o(HashMap<String, Boolean> map, String str) {
        Iterator<Map.Entry<String, Boolean>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getKey().startsWith(str)) {
                return true;
            }
        }
        return false;
    }

    public void p(String str) {
        this.f13681d = str;
    }
}
