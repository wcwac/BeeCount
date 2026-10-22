// Yimu 6.3.0 recovered DTO; removed duplicate synthetic Parcelable bridges for javac.
package com.wangc.bill.http.entity;

import android.os.Parcel;
import android.os.Parcelable;
import com.blankj.utilcode.util.y;
import com.wangc.bill.utils.h2;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class BillInfo implements Parcelable {
    public static final Parcelable.Creator<BillInfo> CREATOR = new Parcelable.Creator<BillInfo>() { // from class: com.wangc.bill.http.entity.BillInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public BillInfo createFromParcel(Parcel parcel) {
            return new BillInfo(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public BillInfo[] newArray(int i6) {
            return new BillInfo[i6];
        }
    };
    private String asset;
    private long bookId;
    private int childCategoryId;
    private boolean containRemark;
    private String dataStr;
    private String discount;
    private double discountNumber;
    private String fromAsset;
    private boolean income;
    private boolean isRefund = false;
    private String message;
    private boolean notIntoBudget;
    private boolean notIntoTotal;
    private String number;
    private String origin;
    private int parentCategoryId;
    private String remark;
    private double serviceCharge;
    private String shopName;
    private List<Long> tagList;
    private long time;
    private String toAsset;
    private boolean transfer;
    private String type;

    public BillInfo() {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getAsset() {
        return this.asset;
    }

    public String getAutoRepeatString() {
        if (h2.J(this.number)) {
            return "BillInfo{number='" + h2.t(h2.S(this.number)) + "', time=" + this.time + '}';
        }
        return "BillInfo{number='" + this.number + "', time=" + this.time + '}';
    }

    public long getBookId() {
        return this.bookId;
    }

    public int getChildCategoryId() {
        return this.childCategoryId;
    }

    public String getDataStr() {
        return this.dataStr;
    }

    public String getDiscount() {
        return this.discount;
    }

    public double getDiscountNumber() {
        return this.discountNumber;
    }

    public String getFromAsset() {
        return this.fromAsset;
    }

    public String getMessage() {
        return this.message;
    }

    public String getNumber() {
        return this.number;
    }

    public String getOrigin() {
        return this.origin;
    }

    public int getParentCategoryId() {
        return this.parentCategoryId;
    }

    public String getRemark() {
        return this.remark;
    }

    public double getServiceCharge() {
        return this.serviceCharge;
    }

    public String getShopName() {
        return this.shopName;
    }

    public List<Long> getTagList() {
        return this.tagList;
    }

    public long getTime() {
        return this.time;
    }

    public String getToAsset() {
        return this.toAsset;
    }

    public String getType() {
        return this.type;
    }

    public boolean isContainRemark() {
        return this.containRemark;
    }

    public boolean isIncome() {
        return this.income;
    }

    public boolean isNotIntoBudget() {
        return this.notIntoBudget;
    }

    public boolean isNotIntoTotal() {
        return this.notIntoTotal;
    }

    public boolean isTransfer() {
        return this.transfer;
    }

    public void readFromParcel(Parcel parcel) {
        this.number = parcel.readString();
        this.message = parcel.readString();
        this.type = parcel.readString();
        this.remark = parcel.readString();
        this.asset = parcel.readString();
        this.time = parcel.readLong();
        this.origin = parcel.readString();
        this.shopName = parcel.readString();
        this.discountNumber = parcel.readDouble();
        this.parentCategoryId = parcel.readInt();
        this.childCategoryId = parcel.readInt();
        this.income = parcel.readByte() != 0;
        this.notIntoTotal = parcel.readByte() != 0;
        this.notIntoBudget = parcel.readByte() != 0;
        this.fromAsset = parcel.readString();
        this.toAsset = parcel.readString();
        this.serviceCharge = parcel.readDouble();
        this.transfer = parcel.readByte() != 0;
        this.containRemark = parcel.readByte() != 0;
        this.bookId = parcel.readLong();
        this.discount = parcel.readString();
    }

    public void setAsset(String str) {
        this.asset = str;
    }

    public void setBookId(long j6) {
        this.bookId = j6;
    }

    public void setChildCategoryId(int i6) {
        this.childCategoryId = i6;
    }

    public void setContainRemark(boolean z5) {
        this.containRemark = z5;
    }

    public void setDataStr(String str) {
        this.dataStr = str;
    }

    public void setDiscount(String str) {
        this.discount = str;
    }

    public void setDiscountNumber(double d6) {
        this.discountNumber = d6;
    }

    public void setFromAsset(String str) {
        this.fromAsset = str;
    }

    public void setIncome(boolean z5) {
        this.income = z5;
    }

    public void setMessage(String str) {
        this.message = str;
    }

    public void setNotIntoBudget(boolean z5) {
        this.notIntoBudget = z5;
    }

    public void setNotIntoTotal(boolean z5) {
        this.notIntoTotal = z5;
    }

    public void setNumber(String str) {
        y.l("setNumber:" + str);
        this.number = str;
    }

    public void setOrigin(String str) {
        this.origin = str;
    }

    public void setParentCategoryId(int i6) {
        this.parentCategoryId = i6;
    }

    public void setRemark(String str) {
        this.remark = str;
    }

    public void setServiceCharge(double d6) {
        this.serviceCharge = d6;
    }

    public void setShopName(String str) {
        this.shopName = str;
    }

    public void setTagList(List<Long> list) {
        this.tagList = list;
    }

    public void setTime(long j6) {
        this.time = j6;
    }

    public void setToAsset(String str) {
        this.toAsset = str;
    }

    public void setTransfer(boolean z5) {
        this.transfer = z5;
    }

    public void setType(String str) {
        this.type = str;
    }

    public String toString() {
        return "BillInfo{number='" + this.number + "', message='" + this.message + "', type='" + this.type + "', remark='" + this.remark + "', asset='" + this.asset + "', time=" + this.time + ", origin='" + this.origin + "', shopName='" + this.shopName + "', parentCategoryId=" + this.parentCategoryId + ", childCategoryId=" + this.childCategoryId + ", income=" + this.income + ", notIntoTotal=" + this.notIntoTotal + ", notIntoBudget=" + this.notIntoBudget + ", fromAsset='" + this.fromAsset + "', toAsset='" + this.toAsset + "', serviceCharge=" + this.serviceCharge + ", transfer=" + this.transfer + ", discountNumber=" + this.discount + '}';
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i6) {
        parcel.writeString(this.number);
        parcel.writeString(this.message);
        parcel.writeString(this.type);
        parcel.writeString(this.remark);
        parcel.writeString(this.asset);
        parcel.writeLong(this.time);
        parcel.writeString(this.origin);
        parcel.writeString(this.shopName);
        parcel.writeDouble(this.discountNumber);
        parcel.writeInt(this.parentCategoryId);
        parcel.writeInt(this.childCategoryId);
        parcel.writeByte(this.income ? (byte) 1 : (byte) 0);
        parcel.writeByte(this.notIntoTotal ? (byte) 1 : (byte) 0);
        parcel.writeByte(this.notIntoBudget ? (byte) 1 : (byte) 0);
        parcel.writeString(this.fromAsset);
        parcel.writeString(this.toAsset);
        parcel.writeDouble(this.serviceCharge);
        parcel.writeByte(this.transfer ? (byte) 1 : (byte) 0);
        parcel.writeByte(this.containRemark ? (byte) 1 : (byte) 0);
        parcel.writeLong(this.bookId);
        parcel.writeString(this.discount);
    }

    protected BillInfo(Parcel parcel) {
        this.number = parcel.readString();
        this.message = parcel.readString();
        this.type = parcel.readString();
        this.remark = parcel.readString();
        this.asset = parcel.readString();
        this.time = parcel.readLong();
        this.origin = parcel.readString();
        this.shopName = parcel.readString();
        this.discountNumber = parcel.readDouble();
        this.parentCategoryId = parcel.readInt();
        this.childCategoryId = parcel.readInt();
        this.income = parcel.readByte() != 0;
        this.notIntoTotal = parcel.readByte() != 0;
        this.notIntoBudget = parcel.readByte() != 0;
        this.fromAsset = parcel.readString();
        this.toAsset = parcel.readString();
        this.serviceCharge = parcel.readDouble();
        this.transfer = parcel.readByte() != 0;
        this.containRemark = parcel.readByte() != 0;
        this.bookId = parcel.readLong();
        this.discount = parcel.readString();
    }
}
