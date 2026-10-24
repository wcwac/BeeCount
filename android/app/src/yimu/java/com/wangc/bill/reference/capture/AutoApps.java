package com.wangc.bill.reference.capture;
import java.util.*;
import com.wangc.bill.reference.ReferenceApplication;
public final class AutoApps {
 public static final class App { public final String packageName,name,description; App(String p,String n,String d){packageName=p;name=n;description=d;} }
 public static final App[] ALL = {
        new App("com.eg.android.AlipayGphone","支付宝","扫码付款、转账、红包、提现、账单详情"),
        new App("com.tencent.mm","微信","扫码付款、转账、红包、提现、账单详情"),
        new App("com.unionpay","云闪付","扫码付款、转账、账单详情"),
        new App("com.jd.jrapp","京东金融","扫码付款、我的账单-账单详情"),
        new App("com.jingdong.app.mall","京东商城","购物、我的钱包-账单详情"),
        new App("com.xunmeng.pinduoduo","拼多多","购物"),
        new App("com.sankuai.meituan","美团","团购、点外卖"),
        new App("com.dianping.v1","大众点评","团购、点外卖"),
        new App("com.sankuai.meituan.takeoutnew","美团外卖","点外卖"),
        new App("com.alibaba.wireless","阿里巴巴","购物"),
        new App("com.taobao.idlefish","闲鱼","购物"),
        new App("com.wudaokou.hippo","盒马","付款码付款"),
        new App("me.ele","饿了么","点外卖"),
        new App("com.huawei.wallet","华为钱包","付款，账单详情"),
        new App("com.ccb.longjiLife","建行生活","付款，钱包-支付记录"),
        new App("com.smile.gifmaker","快手","直播购物"),
        new App("com.kuaishou.nebula","快手极速版","直播购物"),
        new App("com.ss.android.ugc.aweme","抖音","直播购物（官方限制，无法完整使用）"),
        new App("com.ss.android.ugc.livelite","抖音商城","购物（官方限制，无法完整使用）"),
        new App("com.ss.android.ugc.aweme.lite","抖音极速版","直播购物（官方限制，无法完整使用）"),
        new App("com.ss.android.yumme.video","抖音精选","直播购物（官方限制，无法完整使用）"),
        new App("com.taobao.taobao","淘宝","购物（官方限制，无法完整使用）")
 };
 public static Set<String> allPackages(){Set<String>s=new LinkedHashSet<>();for(App a:ALL)s.add(a.packageName);return s;}
 public static Set<String> selected(){Set<String> s=new LinkedHashSet<>(ReferenceApplication.prefs().getStringSet("enabled_packages",allPackages()));s.retainAll(allPackages());return s;}
 public static boolean enabled(){return ReferenceApplication.prefs().getBoolean("capture_enabled",false);}
 public static boolean accepts(String p){return enabled()&&selected().contains(p);}
 public static String name(String p){for(App a:ALL)if(a.packageName.equals(p))return a.name;return p;}
 public static void select(String p,boolean value){if(!allPackages().contains(p))throw new IllegalArgumentException();Set<String>s=selected();if(value)s.add(p);else s.remove(p);ReferenceApplication.prefs().edit().putStringSet("enabled_packages",s).commit();ReferenceCaptureService.refresh();}
 /** Host restore/fixture bridge: changes the filter without revoking accessibility. */
 public static void enable(boolean value){ReferenceApplication.prefs().edit().putBoolean("capture_enabled",value).commit();ReferenceCaptureService.refresh();}
 /** Original AutoActivity.r0 persists first, then posts the service's MAIN master-OFF command. */
 public static void setOriginalMasterEnabled(boolean value){
  ReferenceApplication.prefs().edit().putBoolean("capture_enabled",value).commit();
  if(value)ReferenceCaptureService.refresh();else ReferenceCaptureService.requestOriginalMasterOff();
 }
}
