package com.wangc.bill.reference;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import com.wangc.bill.auto.action.p;
/** Regressions for joins that JADX's original structured output dropped. */
public final class WechatStateTest {
    private int state(boolean group,String... nodes){return p.recoveredPageKind("android.widget.FrameLayout",Arrays.asList(nodes),group);}
    @Test public void unrelatedContentDoesNotArmBillDetailState(){assertEquals(0,state(false,"普通网页","测试"));assertEquals(0,state(false,"支付成功","当前状态"));}
    @Test public void withdrawalAndTransactionDetailHaveDifferentParsers(){assertEquals(11,state(false,"当前状态","提现单号"));assertEquals(3,state(false,"当前状态","交易单号"));assertEquals(11,state(false,"转入零钱通","交易单号"));}
    @Test public void groupCollectionRetainsStateFour(){assertEquals(4,state(true,"张三发起的群收款"));assertEquals(0,state(false,"张三发起的群收款"));}
    @Test public void walletTransferDoesNotFallIntoPaymentParser(){assertEquals(10,state(false,"充值成功","完成"));assertEquals(10,state(false,"零钱提现","到账成功"));}
    @Test public void confirmationControlsAndSuccessArmPaymentState(){assertEquals(1,state(false,"支付成功"));assertEquals(1,state(false,"付款方式","更改","1","2"));assertEquals(1,state(false,"二维码收款","优先付款方式"));}
}
