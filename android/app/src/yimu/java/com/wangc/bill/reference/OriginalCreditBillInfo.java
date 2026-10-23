package com.wangc.bill.reference;

import android.os.Parcel;
import android.os.Parcelable;

/** Recovered CreditBillInfo value and Parcel order; no database or SDK dependencies. */
public final class OriginalCreditBillInfo implements Comparable<OriginalCreditBillInfo>,Parcelable {
    private int billId;
    private long date;
    private String icon,remark,title;
    private double number;
    public OriginalCreditBillInfo(){}
    private OriginalCreditBillInfo(Parcel p){title=p.readString();remark=p.readString();number=p.readDouble();icon=p.readString();billId=p.readInt();date=p.readLong();}
    public int getBillId(){return billId;} public void setBillId(int value){billId=value;}
    public long getDate(){return date;} public void setDate(long value){date=value;}
    public String getIcon(){return icon;} public void setIcon(String value){icon=value;}
    public double getNumber(){return number;} public void setNumber(double value){number=value;}
    public String getRemark(){return remark;} public void setRemark(String value){remark=value;}
    public String getTitle(){return title;} public void setTitle(String value){title=value;}
    @Override public int compareTo(OriginalCreditBillInfo other){long difference=other.date-date;return difference>0?1:difference<0?-1:0;}
    public OriginalCreditBillInfo copy(){OriginalCreditBillInfo v=new OriginalCreditBillInfo();v.billId=billId;v.date=date;v.icon=icon;v.remark=remark;v.title=title;v.number=number;return v;}
    @Override public int describeContents(){return 0;}
    @Override public void writeToParcel(Parcel p,int flags){p.writeString(title);p.writeString(remark);p.writeDouble(number);p.writeString(icon);p.writeInt(billId);p.writeLong(date);}
    public static final Parcelable.Creator<OriginalCreditBillInfo> CREATOR=new Parcelable.Creator<>(){
        @Override public OriginalCreditBillInfo createFromParcel(Parcel p){return new OriginalCreditBillInfo(p);}
        @Override public OriginalCreditBillInfo[] newArray(int size){return new OriginalCreditBillInfo[size];}
    };
}
