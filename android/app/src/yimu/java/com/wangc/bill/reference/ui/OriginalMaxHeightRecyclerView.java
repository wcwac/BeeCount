package com.wangc.bill.reference.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

/** Recovered MaxHeightRecyclerView measurement, including its unconditional AT_MOST spec. */
public final class OriginalMaxHeightRecyclerView extends RecyclerView {
    private int maxHeight;

    public OriginalMaxHeightRecyclerView(Context context){super(context);}
    public OriginalMaxHeightRecyclerView(Context context,AttributeSet attributes){super(context,attributes);read(context,attributes);}
    public OriginalMaxHeightRecyclerView(Context context,AttributeSet attributes,int style){super(context,attributes,style);read(context,attributes);}

    private void read(Context context,AttributeSet attributes){
        TypedArray values=context.obtainStyledAttributes(attributes,new int[]{com.wangc.bill.R.attr.maxHeight});
        try{maxHeight=values.getLayoutDimension(0,maxHeight);}finally{values.recycle();}
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        if(maxHeight>0)heightSpec=View.MeasureSpec.makeMeasureSpec(maxHeight,View.MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec,heightSpec);
    }
}
