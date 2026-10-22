package com.wangc.bill.reference;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.wangc.bill.reference.RecoveredValues.*;
public final class RecoveredValuesTest {
    @Test public void primitiveZeroAndBoxedZeroAreDifferent(){assertNull(dexR(dexN(0)));Integer object=Integer.valueOf(0);assertSame(object,dexR(object));assertFalse(dexZero(object));assertTrue(dexZero(dexN(0)));}
    @Test public void exactFloatingPointBitsSurviveRegisterMoves(){double value=12.30;assertEquals(value,dexD(dexN(Double.doubleToRawLongBits(value))),0.0);assertEquals(-7,dexI(dexN(-7)));assertEquals(Long.MIN_VALUE,dexL(dexN(Long.MIN_VALUE)));}
    @Test public void referenceAliasesResolveToOneConstructedObject(){DexReference original=new DexReference();Object alias=original;original.value=new Object();assertSame(original.value,dexR(alias));assertTrue(dexEqual(original,alias));assertFalse(dexEqual(new Object(),new Object()));}
    @Test public void dexFloatingComparisonTreatsSignedZerosEqually(){assertEquals(0,dexCompareD(dexN(Double.doubleToRawLongBits(-0.0)),dexN(0),-1));assertEquals(-1,dexCompareD(dexN(Double.doubleToRawLongBits(Double.NaN)),dexN(0),-1));}
}
