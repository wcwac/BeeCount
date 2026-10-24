package com.wangc.bill.reference.capture;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

/** Evidence strength, not a clock-based debounce. Weak keys can group PENDING records only. */
public final class CaptureIdentity {
    private static final java.util.regex.Pattern CLOCK=java.util.regex.Pattern.compile("[0-9]{1,2}[:时点][0-9]{1,2}");
    public static final String PAYMENT_ID="PAYMENT_ID", WINDOW_RECEIPT="WINDOW_RECEIPT", UNRESOLVED="UNRESOLVED";
    private static final String[] PAYMENT_LABELS={"微信支付交易单号","支付宝交易号","交易单号","支付单号","付款单号","收款单号","退款单号","转账单号","交易号"};
    private static final String[] REFUND_LABELS={"退款交易单号","退款交易号","退款单号"};
    private static final String[] ORDER_LABELS={"商户订单号","商家订单号","订单编号","订单号"};
    private static final String[] TIME_LABELS={"交易时间","支付时间","付款时间","创建时间","退款时间","转账时间","收款时间","交易日期"};
    public static final class Identity {
        public final String key, windowKey, kind, paymentId, orderReference, businessType;
        public final boolean requiresConfirmation;
        private Identity(String key,String windowKey,String kind,String payment,String order,String business,boolean manual) {
            this.key=key;this.windowKey=windowKey; this.kind=kind; paymentId=payment; orderReference=order; businessType=business; requiresConfirmation=manual;
        }
    }
    private CaptureIdentity() {}
    /** Some recovered builders synthesize now(). Only a time rendered on the page identifies a payment. */
    public static long visibleTransactionTime(long parsedTime,List<String> texts) {
        if(parsedTime<=0||texts==null)return 0;
        for(int i=0;i<texts.size();i++) {
            String token=texts.get(i);if(token==null)continue;
            for(String label:TIME_LABELS)if(token.trim().startsWith(label)) {
                String value=token.substring(token.indexOf(label)+label.length());
                if(containsClock(value))return parsedTime;
                if(i+1<texts.size()&&containsClock(texts.get(i+1)))return parsedTime;
            }
        }
        return 0;
    }
    private static boolean containsClock(String value) {
        return value!=null&&CLOCK.matcher(value).find();
    }
    public static Identity identify(String pkg,int windowId,String kind,long amountMinor,long capturedTime,
            String merchant,List<String> tokens) {
        return identify(pkg,windowId,kind,amountMinor,capturedTime,merchant,"",tokens);
    }
    public static Identity identify(String pkg,int windowId,String kind,long amountMinor,long capturedTime,
            String merchant,String paymentMethod,List<String> tokens) {
        return identify(pkg,"",windowId,kind,amountMinor,capturedTime,merchant,paymentMethod,tokens);
    }
    public static Identity identify(String pkg,String session,int windowId,String kind,long amountMinor,long capturedTime,
            String merchant,String paymentMethod,List<String> tokens) {
        if(pkg==null || pkg.isEmpty())throw new IllegalArgumentException("Capture package is required");
        List<String> read=tokens==null?Collections.emptyList():tokens;
        String order=labeledId(read,ORDER_LABELS);
        boolean refund=has(read,"退款成功") || has(read,"退款完成") || has(read,"退款单号") || has(read,"退款交易");
        String payment=labeledId(read,refund?REFUND_LABELS:PAYMENT_LABELS);
        String business=kind==null?"expense":kind;
        if(refund)business+="/refund";
        else business+="/payment";
        // A window is an observation session, never proof that two completed payments are one.
        // The store's partial unique index restricts this key to PENDING candidates.
        String weak=windowId<0?null:"window:v1:"+digest(pkg,session==null?"":session,""+windowId,business,""+amountMinor,
                ""+Math.max(0,capturedTime),normalize(merchant),normalize(paymentMethod),order);
        if(!payment.isEmpty())return new Identity("payment:v1:"+digest(pkg,business,payment),weak,PAYMENT_ID,payment,order,business,false);
        return new Identity(weak,weak,weak==null?UNRESOLVED:WINDOW_RECEIPT,"",order,business,true);
    }
    private static boolean has(List<String> texts,String phrase) {
        for(String text:texts)if(text!=null&&text.contains(phrase))return true;
        return false;
    }
    private static String labeledId(List<String> texts,String[] labels) {
        String found="";
        for(int i=0;i<texts.size();i++) {
            String token=texts.get(i);if(token==null)continue;
            for(String label:labels) {
                // Only a standalone field label or label-prefixed value is evidence. Narrative
                // text containing "订单号" must not promote an unrelated number to a payment ID.
                String trimmed=token.trim();
                if(!trimmed.startsWith(label))continue;
                String suffix=trimmed.substring(label.length()).trim();
                if(!suffix.isEmpty() && suffix.charAt(0)!=':' && suffix.charAt(0)!='：'
                        && !Character.isWhitespace(trimmed.charAt(label.length())))continue;
                suffix=suffix.replaceFirst("^[:：]\\s*","");
                String candidate=validId(suffix);
                if(candidate.isEmpty() && suffix.isEmpty() && i+1<texts.size())candidate=validId(texts.get(i+1));
                if(candidate.isEmpty())continue;
                if(!found.isEmpty()&&!found.equals(candidate))return ""; // Multiple payments on one screen: manual.
                found=candidate;
            }
        }
        return found;
    }
    private static String validId(String value) {
        if(value==null)return "";
        String id=value.trim();
        if(id.length()<8 || id.length()>128 || !id.matches("[A-Za-z0-9][A-Za-z0-9_-]*"))return "";
        // Masks, dates, money and short bank-card suffixes are intentionally not accepted.
        if(id.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))return "";
        return id;
    }
    static String normalize(String value) { return value==null?"":value.trim().replaceAll("\\s+"," "); }
    private static String digest(String... fields) {
        try {
            MessageDigest hash=MessageDigest.getInstance("SHA-256");
            for(String field:fields) {
                byte[] bytes=field.getBytes(StandardCharsets.UTF_8);
                hash.update((byte)(bytes.length>>>24));hash.update((byte)(bytes.length>>>16));
                hash.update((byte)(bytes.length>>>8));hash.update((byte)bytes.length);hash.update(bytes);
            }
            StringBuilder hex=new StringBuilder();
            for(byte value:hash.digest()){hex.append(Character.forDigit((value>>>4)&15,16));hex.append(Character.forDigit(value&15,16));}
            return hex.toString();
        } catch(NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
}
