package com.wangc.bill.reference;
import org.junit.Test;
import static org.junit.Assert.*;
public final class MoneyTest {
    @Test public void decimalsNeverUseBinaryFloatingPoint(){assertEquals(29,Money.signed("0.29","CNY"));assertEquals(123456,Money.signed("1,234.56","CNY"));assertEquals("-0.01",Money.text(-1,"CNY"));}
    @Test public void currencyPrecisionIsEnforced(){assertEquals(12,Money.positive("12","JPY"));assertEquals(1234,Money.positive("1.234","KWD"));reject(()->Money.signed("1.1","JPY"));reject(()->Money.signed("1.001","CNY"));}
    @Test public void rejectsAmbiguousOrUnboundedInput(){for(String bad:new String[]{"NaN","Infinity","1e5","1,23.00","","99999999999999999999999"})reject(()->Money.signed(bad,"CNY"));reject(()->Money.positive("0","CNY"));reject(()->Money.positive("-1","CNY"));}
    static void reject(Runnable task){try{task.run();fail("Expected invalid input");}catch(IllegalArgumentException expected){}}
}
