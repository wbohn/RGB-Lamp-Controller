package com.wbohn.rgblamp.bus;

import com.wbohn.rgblamp.bus.Message;

/**
 * Created by William on 7/2/2015.
 */
public class MessageBuilder {

    public static final String MODE_CHANGE_HEADER = "@";
    public static final String FADE_TYPE_CHANGE_HEADER = "#";
    public static final String BULB_CHANGE_HEADER = "<";
    public static final String BULB_CHANGE_FOOTER = ">";
    public static final String BULB_CHANGE_DELIM = ":";
    public static final String GAME_OVER = "*";
    public static final String SHOW_LEVEL = "+";

    public static Message createBulbChangeMessage(int index, int color) {
        String indexStr = Integer.toString(index);
        String colorStr = Integer.toString(color);

        String text =
                BULB_CHANGE_HEADER
                + indexStr
                + BULB_CHANGE_DELIM
                + colorStr
                + BULB_CHANGE_FOOTER;

        return createMessage(text);
    }

    public static final Message createModeChangeMessage(int mode) {
        String modeStr = Integer.toString(mode);

        String text =
                MODE_CHANGE_HEADER
                + modeStr;

        return createMessage(text);
    }
    public static Message createFadeChangeMessage(String fadeType) {

        String text =
                FADE_TYPE_CHANGE_HEADER
                + fadeType;

        return createMessage(text);
    }

    public static Message createMessage(String text) {
        Message message = new Message(text);
        return message;
    }
}
