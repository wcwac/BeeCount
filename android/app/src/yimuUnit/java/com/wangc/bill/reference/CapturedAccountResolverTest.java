package com.wangc.bill.reference;

import com.wangc.bill.reference.capture.CapturedAccountResolver;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public final class CapturedAccountResolverTest {
    private static final CapturedAccountResolver.Account CASH=new CapturedAccountResolver.Account(3,"零钱",true);
    private static final CapturedAccountResolver.Account BANK=new CapturedAccountResolver.Account(4,"储蓄卡",true);
    @Test public void explicitMappingWinsOverRulesDefaultAndRecognizedName() {
        CapturedAccountResolver.Resolution result=CapturedAccountResolver.resolve("零钱",4L,3L,3L,Arrays.asList(CASH,BANK));
        assertEquals(4,result.accountId);assertEquals("USER_MAPPING",result.reason);assertFalse(result.requiresConfirmation);
    }
    @Test public void staleExplicitMappingIsNeverSilentlyReplacedByAnotherAccount() {
        CapturedAccountResolver.Resolution result=CapturedAccountResolver.resolve("零钱",99L,null,3L,Arrays.asList(CASH,BANK));
        assertEquals(0,result.accountId);assertEquals("USER_MAPPING_MISSING",result.reason);assertTrue(result.requiresConfirmation);
    }
    @Test public void aUserCanExplicitlyChooseNoAccount() {
        CapturedAccountResolver.Resolution result=CapturedAccountResolver.resolve("零钱",0L,4L,3L,Arrays.asList(CASH,BANK));
        assertEquals(0,result.accountId);assertFalse(result.requiresConfirmation);
    }
    @Test public void merchantRuleAndConfiguredDefaultAreUserChoicesInThatOrder() {
        assertEquals(4,CapturedAccountResolver.resolve("零钱",null,4L,3L,Arrays.asList(CASH,BANK)).accountId);
        assertEquals(4,CapturedAccountResolver.resolve("零钱",null,null,4L,Arrays.asList(CASH,BANK)).accountId);
    }
    @Test public void onlyOneEligibleExactNameCanBeInferred() {
        CapturedAccountResolver.Resolution result=CapturedAccountResolver.resolve("  零钱  ",null,null,null,Arrays.asList(CASH,BANK));
        assertEquals(3,result.accountId);assertEquals("UNIQUE_ACCOUNT_NAME",result.reason);assertFalse(result.requiresConfirmation);
    }
    @Test public void duplicateNamesAreAmbiguousEvenWhenBothAccountsAreUsable() {
        CapturedAccountResolver.Resolution result=CapturedAccountResolver.resolve("零钱",null,null,null,
                Arrays.asList(CASH,new CapturedAccountResolver.Account(8,"零钱",true)));
        assertEquals(0,result.accountId);assertEquals("AMBIGUOUS_NAME",result.reason);assertTrue(result.requiresConfirmation);
    }
    @Test public void aPaymentChannelDoesNotImplyAnUnderlyingBankAccount() {
        CapturedAccountResolver.Resolution result=CapturedAccountResolver.resolve("支付宝",null,null,null,Arrays.asList(CASH,BANK));
        assertEquals(0,result.accountId);assertEquals("CHANNEL_REQUIRES_MAPPING",result.reason);assertTrue(result.requiresConfirmation);
        assertTrue(CapturedAccountResolver.resolve("支付宝",null,null,null,
                Collections.singletonList(new CapturedAccountResolver.Account(9,"支付宝",true))).requiresConfirmation);
    }
    @Test public void unavailableOrUnknownAssetsRequireConfirmation() {
        CapturedAccountResolver.Resolution result=CapturedAccountResolver.resolve("零钱",null,null,null,
                Collections.singletonList(new CapturedAccountResolver.Account(3,"零钱",false)));
        assertEquals(0,result.accountId);assertTrue(result.requiresConfirmation);
        assertTrue(CapturedAccountResolver.resolve("",null,null,null,Collections.emptyList()).requiresConfirmation);
    }
    @Test public void oneHiddenIneligibleNameMatchPreventsInferringAnotherSameNamedAccount() {
        CapturedAccountResolver.Resolution result=CapturedAccountResolver.resolve("零钱",null,null,null,
                Arrays.asList(CASH,new CapturedAccountResolver.Account(8,"零钱",false)));
        assertEquals(0,result.accountId);assertTrue(result.requiresConfirmation);
    }
}
