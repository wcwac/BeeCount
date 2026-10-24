package com.wangc.bill.reference.capture;

import java.util.List;

/** Local account selection; payment platforms are never heuristically translated to bank accounts. */
public final class CapturedAccountResolver {
    public static final class Account {
        public final long id; public final String name; public final boolean eligible;
        public Account(long id,String name,boolean eligible){this.id=id;this.name=name;this.eligible=eligible;}
    }
    public static final class Resolution {
        public final long accountId; public final String reason; public final boolean requiresConfirmation;
        private Resolution(long id,String reason,boolean manual){accountId=id;this.reason=reason;requiresConfirmation=manual;}
    }
    private CapturedAccountResolver() {}
    /** null means no explicit setting, 0 means an explicit user choice of "no account". */
    public static Resolution resolve(String recognizedAsset,Long alias,Long merchantRule,Long configuredDefault,List<Account> accounts) {
        if(alias!=null)return explicit(alias,"USER_MAPPING",accounts);
        if(merchantRule!=null)return explicit(merchantRule,"MERCHANT_RULE",accounts);
        if(configuredDefault!=null)return explicit(configuredDefault,"USER_DEFAULT",accounts);
        String name=CaptureIdentity.normalize(recognizedAsset);
        if(name.isEmpty())return new Resolution(0,"NO_PAYMENT_ACCOUNT",true);
        if(name.equals("支付宝")||name.equals("微信")||name.equals("微信支付")||name.equals("银联")
                ||name.equals("云闪付")||name.equals("银行卡")||name.equals("信用卡"))
            return new Resolution(0,"CHANNEL_REQUIRES_MAPPING",true);
        long match=0; int matches=0; boolean unavailable=false;
        for(Account account:accounts)if(name.equals(CaptureIdentity.normalize(account.name))) {
            if(account.eligible){match=account.id;matches++;}else unavailable=true;
        }
        if(matches>1)return new Resolution(0,"AMBIGUOUS_NAME",true);
        if(matches==1 && !unavailable)return new Resolution(match,"UNIQUE_ACCOUNT_NAME",false);
        return new Resolution(0,unavailable?"ACCOUNT_UNAVAILABLE":"UNMAPPED_PAYMENT_METHOD",true);
    }
    private static Resolution explicit(long id,String reason,List<Account> accounts) {
        if(id==0)return new Resolution(0,reason+"_NONE",false);
        for(Account account:accounts)if(account.id==id)
            return account.eligible?new Resolution(id,reason,false):new Resolution(0,reason+"_UNAVAILABLE",true);
        return new Resolution(0,reason+"_MISSING",true);
    }
}
