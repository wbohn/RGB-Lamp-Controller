package com.wbohn.rgblamp.color_circle;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.wbohn.rgblamp.MainActivity;
import com.wbohn.rgblamp.R;

/**
 * TODO: document your custom view class.
 */
public class ColorCircleButton extends Button {
    private static final String TAG = "ColorCircleButton";

    private int bulbIndex;
    private int color;

    private int mode;

    private GradientDrawable background;

    public ColorCircleButton(Context context) {
        super(context);
    }

    public ColorCircleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorCircleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mode == MainActivity.MODE_FADE) {
                    int complementaryColor = getComplementaryColor(Color.GRAY);
                    int[] gradientColors = new int[]{Color.GRAY, complementaryColor};
                    background.setColors(gradientColors);
                    setBackground(background);
                } else {
                    background.setColor(Color.GRAY);
                    setBackground(background);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mode == MainActivity.MODE_FADE) {
                    int complementaryColor = getComplementaryColor(getColor());
                    int[] gradientColors = new int[]{getColor(), complementaryColor};
                    background.setColors(gradientColors);
                    setBackground(background);

                } else {
                    background.setColor(getColor());
                    setBackground(background);
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    public void setBulbIndex(int index) {
        bulbIndex = index;
    }

    public int getBulbIndex() {
        return bulbIndex;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void makeSolidBackground(int selectedColor) {
        background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(selectedColor);
        background.setStroke(1, R.color.secondary_text);

        setColor(selectedColor);
        setBackground(background);
    }

    public void makeGradientBackground(int selectedColor) {
        int complementaryColor = getComplementaryColor(selectedColor);
        int[] gradientColors = new int[]{selectedColor, complementaryColor};

        background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColors(gradientColors);
        background.setStroke(1, R.color.secondary_text);
        setBackground(background);
    }

    private int getComplementaryColor(int selectedColor) {

        return Color.rgb(
                255-Color.red(selectedColor),
                255-Color.green(selectedColor),
                255-Color.blue(selectedColor));
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
