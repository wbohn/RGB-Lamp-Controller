package com.wbohn.rgblamp;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wbohn.rgblamp.prefs.AppPreferences;
import com.wbohn.rgblamp.color_circle.ColorCircleButton;

public class MainFragment extends Fragment  implements ColorCircleButton.ColorCircleButtonInterface {
    public static final String TAG = "MainFragment";

    /* stored in order of appearance on screen,
     which differs from ordered declared in xml */
    private ColorCircleButton[] colorCircleButtons;

    private AppPreferences appPrefs;

    private int mode = MainActivity.MODE_SOLID;

    public interface MainFragmentInterface {
        void updateBulb(int index, int color);
        void guessMade(int id);
    }
    public MainFragmentInterface mainFragmentInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        appPrefs = new AppPreferences(activity);
        mainFragmentInterface = (MainFragmentInterface) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mainFragmentInterface = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        colorCircleButtons = new ColorCircleButton[]{
                (ColorCircleButton) rootView.findViewById(R.id.button_circle_three),
                (ColorCircleButton) rootView.findViewById(R.id.button_circle_one),
                (ColorCircleButton) rootView.findViewById(R.id.button_circle_five),
                (ColorCircleButton) rootView.findViewById(R.id.button_circle_four),
                (ColorCircleButton) rootView.findViewById(R.id.button_circle_two)
        };

        for (int i = 0; i < colorCircleButtons.length; i++) {
            colorCircleButtons[i].setOnClickListener(colorCircleButonnListener);
            colorCircleButtons[i].setBulbIndex(i);
            colorCircleButtons[i].setColorCircleButtonInterface(this);
            if (mode == MainActivity.MODE_FADE) {
                colorCircleButtons[i].makeGradientBackground(appPrefs.getBulbColor(i));
            } else {
                colorCircleButtons[i].makeSolidBackground(appPrefs.getBulbColor(i));
            }
        }
        return rootView;
    }

    private View.OnClickListener colorCircleButonnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ColorCircleButton clickedButton = (ColorCircleButton) v;
            if (mode == MainActivity.MODE_GAME) {
                mainFragmentInterface.guessMade(clickedButton.getBulbIndex());
            } else {
                clickedButton.showColorPicker();
            }
        }
    };

    public int[] getColors() {
        int[] colors = new int[5];
        int i = 0;
        for (ColorCircleButton button : colorCircleButtons) {
            colors[i] = button.getColor();
            i++;
        }
        return colors;
    }

    public void setMode(int mode) {
        this.mode = mode;
        for (int i = 0; i < colorCircleButtons.length; i++) {
            colorCircleButtons[i].setMode(mode);
            if (mode == MainActivity.MODE_FADE) {
                colorCircleButtons[i].makeGradientBackground(appPrefs.getBulbColor(i));
            } else {
                colorCircleButtons[i].makeSolidBackground(appPrefs.getBulbColor(i));
            }
        }
     }

    @Override
    public void lampColorChanged(int index, int color) {
        mainFragmentInterface.updateBulb(index, color);
    }
}
