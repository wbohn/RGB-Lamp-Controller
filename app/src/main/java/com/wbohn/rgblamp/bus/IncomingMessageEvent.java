package com.wbohn.rgblamp.bus;

/**
 * Created by William on 7/3/2015.
 */
public class IncomingMessageEvent {
    public static final char SEQUENCE_RECEIVED = '#';
    public static final char LEVEL_SHOWN = '+';

    public char text;

    public IncomingMessageEvent(char text) {
        this.text = text;
    }
}
