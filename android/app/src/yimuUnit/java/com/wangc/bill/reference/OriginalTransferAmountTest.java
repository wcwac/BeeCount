package com.wangc.bill.reference;

import org.junit.Test;
import java.util.Locale;
import static org.junit.Assert.*;

public final class OriginalTransferAmountTest {
    @Test public void signedAndZeroValuesAreNotChangedIntoPositiveMoney() {
        for (String value : new String[]{"0", "-0", "+0", "-12.5", "+12.5", "0001"}) assertTrue(value, OriginalTransferAmount.isJ(value));
        assertEquals(-12.5, OriginalTransferAmount.parseS("-12.5"), 0);
        assertEquals(Double.doubleToRawLongBits(-0.0d), Double.doubleToRawLongBits(OriginalTransferAmount.parseS("-0")));
    }
    @Test public void leadingDotAndWhitespacePassJButDoNotCreateAnNFeeBill() {
        for (String value : new String[]{".5", "-.5", " 1", "1\n"}) {
            assertTrue(value, OriginalTransferAmount.isJ(value)); assertFalse(value, OriginalTransferAmount.isN(value));
        }
        assertEquals(.5, OriginalTransferAmount.parseS(".5"), 0);
    }
    @Test public void commaCanCreateAFeeBillWithoutPassingThePrincipalOrServiceChargeGate() {
        assertFalse(OriginalTransferAmount.isJ("1,2")); assertTrue(OriginalTransferAmount.isN("1,2"));
        assertEquals(1.2, OriginalTransferAmount.parseS("1,2"), 0);
        assertTrue(OriginalTransferAmount.isN("1,2,3")); assertEquals(0, OriginalTransferAmount.parseS("1,2,3"), 0);
    }
    @Test public void scientificAndHexadecimalDoubleFormsFollowTheSourceParser() {
        for (String value : new String[]{"1e1", "0x1.0p2", "1D"}) {
            assertTrue(value, OriginalTransferAmount.isJ(value)); assertTrue(value, OriginalTransferAmount.isN(value));
        }
        assertEquals(10, OriginalTransferAmount.parseS("1e1"), 0);
        assertEquals(4, OriginalTransferAmount.parseS("0x1.0p2"), 0);
    }
    @Test public void sourcePredicateAcceptsNonfiniteDoublesWithoutClaimingTheyAreStorableMoney() {
        assertTrue(OriginalTransferAmount.isJ("NaN")); assertTrue(Double.isNaN(OriginalTransferAmount.parseS("NaN")));
        assertTrue(OriginalTransferAmount.isJ("Infinity")); assertEquals(Double.POSITIVE_INFINITY, OriginalTransferAmount.parseS("Infinity"), 0);
        assertFalse(OriginalTransferAmount.isN("NaN")); assertFalse(OriginalTransferAmount.isN("Infinity"));
    }
    @Test public void NCanAcceptMalformedNumericSuffixesWhoseSValueIsZero() {
        for (String value : new String[]{"1usd", "+-2"}) {
            assertTrue(value, OriginalTransferAmount.isN(value)); assertFalse(value, OriginalTransferAmount.isJ(value));
            assertEquals(0, OriginalTransferAmount.parseS(value), 0);
        }
        assertFalse(OriginalTransferAmount.isJ("")); assertFalse(OriginalTransferAmount.isN(""));
        assertEquals(0, OriginalTransferAmount.parseS(""), 0);
    }
    @Test public void nullIsNotSilentlyConvertedIntoTheEmptyTextProvidedByTheSourceEditText() {
        try { OriginalTransferAmount.isJ(null); fail(); } catch (NullPointerException expected) {}
        try { OriginalTransferAmount.isN(null); fail(); } catch (NullPointerException expected) {}
        try { OriginalTransferAmount.parseS(null); fail(); } catch (NullPointerException expected) {}
    }
    @Test public void balanceFormattingUsesAtMostTwoHalfUpDecimalsWithoutGrouping() {
        Locale previous = Locale.getDefault(); Locale.setDefault(Locale.US);
        try {
            assertEquals("1234.5", OriginalTransferAmount.formatBalance(1234.5));
            assertEquals("2", OriginalTransferAmount.formatBalance(2));
            assertEquals("1.13", OriginalTransferAmount.formatBalance(1.125));
            assertEquals("-1.13", OriginalTransferAmount.formatBalance(-1.125));
        } finally { Locale.setDefault(previous); }
    }
    @Test public void deltaFormattingPreservesGroupingTwoDigitsAndSourceZeroGate() {
        Locale previous = Locale.getDefault(); Locale.setDefault(Locale.US);
        try {
            assertEquals("1,234.50", OriginalTransferAmount.formatDelta(1234.5));
            assertEquals("0.13", OriginalTransferAmount.formatDelta(.125));
            assertEquals("-0.13", OriginalTransferAmount.formatDelta(-.125));
            assertEquals("0.00", OriginalTransferAmount.formatDelta(-0.0));
            assertEquals("0.00", OriginalTransferAmount.formatDelta(-.001));
        } finally { Locale.setDefault(previous); }
    }
    @Test public void formattingRetainsTheSourceLocaleAndLiteralPointQuirks() {
        Locale previous = Locale.getDefault(); Locale.setDefault(Locale.GERMANY);
        try {
            assertEquals("1234,5", OriginalTransferAmount.formatBalance(1234.5));
            assertEquals("1.234,50", OriginalTransferAmount.formatDelta(1234.5));
            assertEquals(",50", OriginalTransferAmount.formatDelta(.5));
            assertEquals("-,50", OriginalTransferAmount.formatDelta(-.5));
        } finally { Locale.setDefault(previous); }
    }
}
