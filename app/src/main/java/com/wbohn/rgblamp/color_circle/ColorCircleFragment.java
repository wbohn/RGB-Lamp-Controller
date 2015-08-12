package com.wbohn.rgblamp.color_circle;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.squareup.otto.Subscribe;
import com.wbohn.rgblamp.App;
import com.wbohn.rgblamp.LampManager;
import com.wbohn.rgblamp.MainActivity;
import com.wbohn.rgblamp.R;
import com.wbohn.rgblamp.bus.GuessEvent;
import com.wbohn.rgblamp.bus.ModeChangeEvent;

public class ColorCircleFragment extends Fragment  implements LampManager.LampInterface, OnColorSelectedListener {
    /* stored in order of appearance on screen,
     which differs from ordered declared in xml */
    private ColorCircleButton[] colorCircleButtons;

    private int mode = MainActivity.MODE_SOLID;

    private LampManager lampManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lampManager = new LampManager();
        lampManager.setLampInterface(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_color_circle, container, false);

        colorCircleButtons = new ColorCircleButton[]{
                // The bulbs were wired incorrectly, array ordered to
                // correct L-R display
                (ColorCircleButton) rootView.findViewById(R.id.button_circle_three),
                (ColorCircleButton) rootView.findViewById(R.id.button_circle_one),
                (ColorCircleButton) rootView.findViewById(R.id.button_circle_five),
                (ColorCircleButton) rootView.findViewById(R.id.button_circle_four),
                (ColorCircleButton) rootView.findViewById(R.id.button_circle_two)
        };

        for (int i = 0; i < colorCircleButtons.length; i++) {
            colorCircleButtons[i].setOnClickListener(colorCircleButtonListener);
            colorCircleButtons[i].setBulbIndex(i);
            if (mode == MainActivity.MODE_FADE) {
                colorCircleButtons[i].makeGradientBackground(App.getAppPreferences().getBulbColor(i));
            } else {
                colorCircleButtons[i].makeSolidBackground(App.getAppPreferences().getBulbColor(i));
            }
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getEventBus().register(this);
        App.getEventBus().register(lampManager);
    }

    @Override
    public void onPause() {
        super.onPause();
        App.getEventBus().unregister(this);
        App.getEventBus().unregister(lampManager);
    }

    @Override
    public void onColorSelected(int selectedColor) {
        /* do nothing because selectedColor will be passed
        when dialog dismisses */
    }

    @Subscribe
    public void onModeChange(ModeChangeEvent event) {
        setMode(event.mode);
    }

    public void setMode(int mode) {
        this.mode = mode;
        for (int i = 0; i < colorCircleButtons.length; i++) {
            colorCircleButtons[i].setMode(mode);
            if (mode == MainActivity.MODE_FADE) {
                colorCircleButtons[i].makeGradientBackground(App.getAppPreferences().getBulbColor(i));
            } else {
                colorCircleButtons[i].makeSolidBackground(App.getAppPreferences().getBulbColor(i));
            }
        }
    }

    public int[] getColors() {
        int[] colors = new int[5];
        int i = 0;
        for (ColorCircleButton button : colorCircleButtons) {
            colors[i] = button.getColor();
            i++;
        }
        return colors;
    }

    private View.OnClickListener colorCircleButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ColorCircleButton clickedButton = (ColorCircleButton) v;

            if (mode == MainActivity.MODE_GAME) {
                App.getEventBus().post(new GuessEvent(clickedButton.getBulbIndex()));
            } else {
                showColorPicker(clickedButton);
            }
        }
    };

    public void showColorPicker(final ColorCircleButton clickedButton) {
        ColorPickerDialogBuilder
                .with(getActivity()).setTitle("Choose color")
                .initialColor(clickedButton.getColor())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .noSliders()
                .setOnColorSelectedListener(this)
                .setPositiveButton("Ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                        if (allColors != null) {
                            if (mode == MainActivity.MODE_FADE) {
                                clickedButton.makeGradientBackground(lastSelectedColor);
                            } else {
                                clickedButton.makeSolidBackground(lastSelectedColor);
                            }
                            clickedButton.setColor(lastSelectedColor);
                            lampManager.updateBulb(clickedButton.getBulbIndex(), lastSelectedColor);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // dialog auto-dismisses
                    }
                })
                .build().show();
    }
}