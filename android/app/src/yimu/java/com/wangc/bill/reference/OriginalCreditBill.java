package com.wangc.bill.reference;

import android.os.Parcel;
import android.os.Parcelable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** CreditBill is a mutable projection, never a second persisted asset balance. */
public final class OriginalCreditBill implements Comparable<OriginalCreditBill>,Parcelable {
    private long assetId,time,maxTime,minTime;
    private String inDate,title,repaymentDay;
    private int year,month;
    private boolean current;
    private double number,instalmentNumber,serviceCost,remainServiceCost,remindNumber;
    private List<OriginalCreditBillInfo> billInfoList,instalmentInfoList;
    public OriginalCreditBill(){}
    private OriginalCreditBill(Parcel p){
        inDate=p.readString();time=p.readLong();title=p.readString();number=p.readDouble();instalmentNumber=p.readDouble();year=p.readInt();month=p.readInt();maxTime=p.readLong();minTime=p.readLong();current=p.readByte()!=0;assetId=p.readLong();repaymentDay=p.readString();remindNumber=p.readDouble();serviceCost=p.readDouble();billInfoList=p.createTypedArrayList(OriginalCreditBillInfo.CREATOR);instalmentInfoList=p.createTypedArrayList(OriginalCreditBillInfo.CREATOR);
        // Original Parcel omits remainServiceCost; its freshly unparcelled value is0.
    }
    private static double add(double old,double delta){return new BigDecimal(Double.toString(old)).add(new BigDecimal(Double.toString(delta))).doubleValue();}
    public void addNumber(double value){number=add(number,value);}
    public void reduceNumber(double value){number=new BigDecimal(Double.toString(number)).subtract(new BigDecimal(Double.toString(value))).doubleValue();}
    public void addInstalmentNumber(double value){instalmentNumber=add(instalmentNumber,value);}
    public void addServiceCost(double value){serviceCost=add(serviceCost,value);}
    public void addBill(OriginalCreditBillInfo value){if(billInfoList==null)billInfoList=new ArrayList<>();billInfoList.add(value);}
    public void addInstalment(OriginalCreditBillInfo value){if(instalmentInfoList==null)instalmentInfoList=new ArrayList<>();instalmentInfoList.add(value);}
    public long getAssetId(){return assetId;} public void setAssetId(long value){assetId=value;}
    public List<OriginalCreditBillInfo> getBillInfoList(){return billInfoList;} public void setBillInfoList(List<OriginalCreditBillInfo> value){billInfoList=value;}
    public String getInDate(){return inDate;} public void setInDate(String value){inDate=value;}
    public List<OriginalCreditBillInfo> getInstalmentInfoList(){return instalmentInfoList;} public void setInstalmentInfoList(List<OriginalCreditBillInfo> value){instalmentInfoList=value;}
    public double getInstalmentNumber(){return instalmentNumber;} public void setInstalmentNumber(double value){instalmentNumber=value;}
    public long getMaxTime(){return maxTime;} public void setMaxTime(long value){maxTime=value;}
    public long getMinTime(){return minTime;} public void setMinTime(long value){minTime=value;}
    public int getMonth(){return month;} public void setMonth(int value){month=value;}
    public double getNumber(){return number;} public void setNumber(double value){number=value;}
    public double getRemainServiceCost(){return remainServiceCost;} public void setRemainServiceCost(double value){remainServiceCost=value;}
    public double getRemindNumber(){return remindNumber;} public void setRemindNumber(double value){remindNumber=value;}
    public String getRepaymentDay(){return repaymentDay;} public void setRepaymentDay(String value){repaymentDay=value;}
    public double getServiceCost(){return serviceCost;} public void setServiceCost(double value){serviceCost=value;}
    public long getTime(){return time;} public void setTime(long value){time=value;}
    public String getTitle(){return title;} public void setTitle(String value){title=value;}
    public int getYear(){return year;} public void setYear(int value){year=value;}
    public boolean isCurrent(){return current;} public void setCurrent(boolean value){current=value;}
    @Override public int compareTo(OriginalCreditBill other){long difference=other.time-time;return difference>0?1:difference<0?-1:0;}
    public OriginalCreditBill copy(){
        OriginalCreditBill v=new OriginalCreditBill();v.assetId=assetId;v.time=time;v.maxTime=maxTime;v.minTime=minTime;v.inDate=inDate;v.title=title;v.repaymentDay=repaymentDay;v.year=year;v.month=month;v.current=current;v.number=number;v.instalmentNumber=instalmentNumber;v.serviceCost=serviceCost;v.remainServiceCost=remainServiceCost;v.remindNumber=remindNumber;
        if(billInfoList!=null){v.billInfoList=new ArrayList<>();for(OriginalCreditBillInfo row:billInfoList)v.billInfoList.add(row==null?null:row.copy());}
        if(instalmentInfoList!=null){v.instalmentInfoList=new ArrayList<>();for(OriginalCreditBillInfo row:instalmentInfoList)v.instalmentInfoList.add(row==null?null:row.copy());}return v;
    }
    @Override public int describeContents(){return 0;}
    @Override public void writeToParcel(Parcel p,int flags){p.writeString(inDate);p.writeLong(time);p.writeString(title);p.writeDouble(number);p.writeDouble(instalmentNumber);p.writeInt(year);p.writeInt(month);p.writeLong(maxTime);p.writeLong(minTime);p.writeByte((byte)(current?1:0));p.writeLong(assetId);p.writeString(repaymentDay);p.writeDouble(remindNumber);p.writeDouble(serviceCost);p.writeTypedList(billInfoList);p.writeTypedList(instalmentInfoList);}
    public static final Parcelable.Creator<OriginalCreditBill> CREATOR=new Parcelable.Creator<>(){
        @Override public OriginalCreditBill createFromParcel(Parcel p){return new OriginalCreditBill(p);}
        @Override public OriginalCreditBill[] newArray(int size){return new OriginalCreditBill[size];}
    };
}
