package com.wbohn.rgblamp.color_circle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.wbohn.rgblamp.R;

/**
 * TODO: document your custom view class.
 */
public class ColorCircleGrid extends LinearLayout {

    public ColorCircleGrid(Context context) {
        super(context);
    }

    public ColorCircleGrid(final Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.color_circles_grid, this, true);
    }

    public ColorCircleGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
