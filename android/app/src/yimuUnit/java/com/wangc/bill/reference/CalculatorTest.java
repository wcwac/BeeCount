package com.wangc.bill.reference;
import org.junit.Test;
import static org.junit.Assert.*;
public final class CalculatorTest {
    @Test public void precedenceAndRounding(){assertEquals("7.00",Calculator.evaluate("1+2×3",2));assertEquals("0.30",Calculator.evaluate("0.1+0.2",2));assertEquals("0.67",Calculator.evaluate("2÷3",2));assertEquals("-3.00",Calculator.evaluate("-1-2",2));}
    @Test public void rejectsCodeAndIncompleteExpressions(){for(String bad:new String[]{"1+","1÷0","alert(1)","1++2","1.2.3","1;2"})MoneyTest.reject(()->Calculator.evaluate(bad,2));}
}
