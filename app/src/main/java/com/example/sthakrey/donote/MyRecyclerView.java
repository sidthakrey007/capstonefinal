package com.example.sthakrey.donote;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by sthakrey on 6/24/2017.
 */

public class MyRecyclerView extends RecyclerView {


    MyRecyclerView(Context c) {
        super(c);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }
}
