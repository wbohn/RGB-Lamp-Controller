package com.wbohn.rgblamp.bus;

/**
 * Created by William on 7/2/2015.
 */
public class Message {

    public String text;

    public Message(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
