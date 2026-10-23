package com.wangc.bill.reference;

import com.wangc.bill.reference.capture.CapturedAmount;
import java.util.Locale;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Expected values follow the original amount shown before confirmation, not raw input precision. */
public final class CapturedAmountTest {
    private Locale previous;
    @Before public void locale() { previous=Locale.getDefault();Locale.setDefault(Locale.US); }
    @After public void restoreLocale() { Locale.setDefault(previous); }

    @Test public void aSignedParserNumberKeepsItsMagnitude() {
        assertEquals(1234,CapturedAmount.minorUnits("-12.34"));
        assertEquals(1234,CapturedAmount.minorUnits("+12.34"));
    }
    @Test public void precisionIsRoundedAsDisplayedInsteadOfDiscardingTheAmount() {
        assertEquals(1235,CapturedAmount.minorUnits("12.349"));
        assertEquals(1000,CapturedAmount.minorUnits("9.999"));
        assertEquals(2230,CapturedAmount.minorUnits("22.301"));
    }
    @Test public void anAcceptedExponentNumberIsShownAsADecimalAmount() {
        assertEquals(10000,CapturedAmount.minorUnits("1e2"));
    }
    @Test public void decimalCommaFormattingDoesNotLoseAnOtherwiseValidAmount() {
        Locale.setDefault(Locale.GERMANY);
        assertEquals(1235,CapturedAmount.minorUnits("-12.349"));
    }
    @Test public void anInvalidOrZeroDisplayedAmountRequiresCorrection() {
        for(String raw:new String[]{null,"invalid","0","0.001","NaN","Infinity"}) {
            try { CapturedAmount.minorUnits(raw);fail("Accepted invalid displayed amount: "+raw); }
            catch(IllegalArgumentException expected) {}
        }
    }
}
