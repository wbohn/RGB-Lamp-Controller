package com.wbohn.rgblamp.bus;

import com.wbohn.rgblamp.MainActivity;

/**
 * Created by William on 7/2/2015.
 */
public class ModeChangeEvent {
    public int mode;

    public ModeChangeEvent(int mode) {
        this.mode = mode;
    }
}
