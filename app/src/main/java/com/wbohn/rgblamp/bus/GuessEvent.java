package com.wbohn.rgblamp.bus;

/**
 * Created by William on 7/3/2015.
 */
public class GuessEvent {
    public int bulbIndex;

    public GuessEvent(int bulbIndex) {
        this.bulbIndex = bulbIndex;
    }
}
