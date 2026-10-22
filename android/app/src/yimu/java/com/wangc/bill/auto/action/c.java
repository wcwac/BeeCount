// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 20f7c529a95df463f4b93aba6229eec44232b6ac8206d03688af417bec2a1030
package com.wangc.bill.auto.action;

import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import com.blankj.utilcode.util.y;
import java.util.ArrayList;
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
        this.c = 0;
        List<String> observed = i(accessibilityNodeInfo);
        com.wangc.bill.reference.capture.ReferenceDiagnostics.read(observed.size(), this.c);
        return observed;
    }

    protected List<String> h(AccessibilityNodeInfo accessibilityNodeInfo) {
        this.c = 0;
        List<String> observed = j(accessibilityNodeInfo);
        com.wangc.bill.reference.capture.ReferenceDiagnostics.read(observed.size(), this.c);
        return observed;
    }

    protected List<String> i(AccessibilityNodeInfo accessibilityNodeInfo) {
        this.c++;
        ArrayList arrayList = new ArrayList();
        if (accessibilityNodeInfo != null) {
            for (int i6 = 0; i6 < accessibilityNodeInfo.getChildCount(); i6++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i6);
                if (child != null && child.getChildCount() > 0) {
                    if (arrayList.size() > 99 || this.c > 999) {
                        return arrayList;
                    }
                    if (child != accessibilityNodeInfo) {
                        arrayList.addAll(i(child));
                    }
                } else if (child != null && !TextUtils.isEmpty(child.getText())) {
                    s6.b.a("nodeInfo1:" + ((Object) child.getText()));
                    arrayList.add(child.getText().toString());
                }
            }
        }
        return arrayList;
    }

    protected List<String> j(AccessibilityNodeInfo accessibilityNodeInfo) {
        this.c++;
        ArrayList arrayList = new ArrayList();
        if (accessibilityNodeInfo != null) {
            for (int i6 = 0; i6 < accessibilityNodeInfo.getChildCount(); i6++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i6);
                if (child != null && child.getChildCount() > 0) {
                    if (arrayList.size() > 99 || this.c > 999) {
                        return arrayList;
                    }
                    if (child != accessibilityNodeInfo) {
                        arrayList.addAll(j(child));
                    }
                } else if (child != null && !TextUtils.isEmpty(child.getText())) {
                    y.l("nodeInfo1:" + ((Object) child.getText()));
                    arrayList.add(child.getText().toString());
                }
            }
        }
        return arrayList;
    }

    protected List<String> k(AccessibilityNodeInfo accessibilityNodeInfo) {
        this.c++;
        ArrayList arrayList = new ArrayList();
        if (accessibilityNodeInfo != null) {
            for (int i6 = 0; i6 < accessibilityNodeInfo.getChildCount(); i6++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i6);
                if (child != null && child.getChildCount() > 0) {
                    if (arrayList.size() > 99 || this.c > 199) {
                        return arrayList;
                    }
                    if (child != accessibilityNodeInfo) {
                        arrayList.addAll(k(child));
                    }
                } else if (child != null && !TextUtils.isEmpty(child.getText())) {
                    s6.b.a("nodeInfo1:" + ((Object) child.getText()));
                    arrayList.add(child.getText().toString());
                }
            }
        }
        return arrayList;
    }

    protected List<String> l(AccessibilityNodeInfo accessibilityNodeInfo) {
        this.c = 0;
        List<String> observed = k(accessibilityNodeInfo);
        com.wangc.bill.reference.capture.ReferenceDiagnostics.read(observed.size(), this.c);
        return observed;
    }

    protected List<String> m(AccessibilityNodeInfo accessibilityNodeInfo) {
        this.c = 0;
        List<String> observed = n(accessibilityNodeInfo);
        com.wangc.bill.reference.capture.ReferenceDiagnostics.read(observed.size(), this.c);
        return observed;
    }

    protected List<String> n(AccessibilityNodeInfo accessibilityNodeInfo) {
        this.c++;
        ArrayList arrayList = new ArrayList();
        if (accessibilityNodeInfo != null) {
            for (int i6 = 0; i6 < accessibilityNodeInfo.getChildCount(); i6++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i6);
                if (child != null && child.getChildCount() > 0) {
                    if (arrayList.size() > 99 || this.c > 999) {
                        return arrayList;
                    }
                    if (child != accessibilityNodeInfo) {
                        arrayList.addAll(n(child));
                    }
                } else if (child != null && !TextUtils.isEmpty(child.getText())) {
                    y.l("nodeInfo1:" + ((Object) child.getText()));
                    arrayList.add(child.getText().toString());
                } else if (child != null && !TextUtils.isEmpty(child.getContentDescription())) {
                    y.l("nodeInfo1:" + child.getContentDescription().toString().replace(" ", ""));
                    arrayList.add(child.getContentDescription().toString());
                }
            }
        }
        return arrayList;
    }

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
