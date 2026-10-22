package com.wangc.bill.reference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;

/** Bounded decimal arithmetic only; no expression engine, scripts or reflection. */
public final class Calculator {
    private Calculator(){}
    public static String evaluate(String text,int scale){
        if(text==null||text.length()>80||!text.matches("[0-9.+×÷-]+"))throw new IllegalArgumentException("计算式无效");
        ArrayDeque<BigDecimal> values=new ArrayDeque<>();ArrayDeque<Character> operators=new ArrayDeque<>();int index=0;
        while(index<text.length()){
            int start=index;if(index==0&&text.charAt(index)=='-')index++;
            while(index<text.length()&&(Character.isDigit(text.charAt(index))||text.charAt(index)=='.'))index++;
            if(index==start)throw new IllegalArgumentException("计算式不完整");
            try{values.push(new BigDecimal(text.substring(start,index)));}catch(NumberFormatException e){throw new IllegalArgumentException("数字格式无效");}
            if(index==text.length())break;
            char op=text.charAt(index++);while(!operators.isEmpty()&&precedence(operators.peek())>=precedence(op))apply(values,operators.pop());operators.push(op);
        }
        while(!operators.isEmpty())apply(values,operators.pop());if(values.size()!=1)throw new IllegalArgumentException("计算式不完整");
        return values.pop().setScale(scale,RoundingMode.HALF_UP).toPlainString();
    }
    private static int precedence(char op){return op=='×'||op=='÷'?2:1;}
    private static void apply(ArrayDeque<BigDecimal> values,char op){if(values.size()<2)throw new IllegalArgumentException("计算式不完整");BigDecimal b=values.pop(),a=values.pop();BigDecimal result=switch(op){case '+'->a.add(b);case '-'->a.subtract(b);case '×'->a.multiply(b);case '÷'->{if(b.signum()==0)throw new IllegalArgumentException("不能除以零");yield a.divide(b,18,RoundingMode.HALF_UP);}default->throw new IllegalArgumentException("计算符号无效");};if(result.precision()>40)throw new IllegalArgumentException("计算结果过大");values.push(result);}
}
