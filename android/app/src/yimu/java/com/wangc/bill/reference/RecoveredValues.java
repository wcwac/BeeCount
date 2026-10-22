package com.wangc.bill.reference;

/** Register values used only by statically transcribed Java methods.
 * There is no opcode reader, bytecode input, class loading or target VM here.
 * Primitive bits are kept separate from real boxed objects, as in the DEX verifier.
 */
public final class RecoveredValues {
    private RecoveredValues(){}
    private static final class Bits {final long value;Bits(long value){this.value=value;}}
    public static final class DexReference {public Object value;}
    private static final Bits ZERO=new Bits(0),ONE=new Bits(1),MINUS_ONE=new Bits(-1);
    public static Object dexN(long value){return value==0?ZERO:value==1?ONE:value==-1?MINUS_ONE:new Bits(value);}
    public static Object dexR(Object value){if(value instanceof DexReference reference)return reference.value;return value instanceof Bits bits&&bits.value==0?null:value;}
    public static int dexI(Object value){return (int)((Bits)value).value;}
    public static long dexL(Object value){return ((Bits)value).value;}
    public static double dexD(Object value){return Double.longBitsToDouble(dexL(value));}
    public static float dexF(Object value){return Float.intBitsToFloat(dexI(value));}
    public static boolean dexZero(Object value){return value==null||value instanceof Bits bits&&bits.value==0;}
    public static boolean dexZ(Object value){return !dexZero(value);}
    public static boolean dexEqual(Object a,Object b){if(a instanceof Bits x&&b instanceof Bits y)return x.value==y.value;return dexR(a)==dexR(b);}
    public static int dexCompareD(Object x,Object y,int nan){double a=dexD(x),b=dexD(y);return Double.isNaN(a)||Double.isNaN(b)?nan:a==b?0:a<b?-1:1;}
    public static int dexCompareF(Object x,Object y,int nan){float a=dexF(x),b=dexF(y);return Float.isNaN(a)||Float.isNaN(b)?nan:a==b?0:a<b?-1:1;}
    public static int dexLength(Object value){if(value instanceof Object[] a)return a.length;if(value instanceof int[] a)return a.length;if(value instanceof long[] a)return a.length;if(value instanceof byte[] a)return a.length;if(value instanceof char[] a)return a.length;if(value instanceof short[] a)return a.length;if(value instanceof float[] a)return a.length;if(value instanceof double[] a)return a.length;if(value instanceof boolean[] a)return a.length;throw new IllegalArgumentException("Expected recovered array");}
}
