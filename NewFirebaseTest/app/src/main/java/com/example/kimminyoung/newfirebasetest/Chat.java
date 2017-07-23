package com.example.kimminyoung.newfirebasetest;

/**
 * Created by KimMinYoung on 2017-07-23.
 */

public class Chat {
    public String recordText;

    public Chat() {

    }

    public Chat(String text) {
        this.recordText = text;
    }

    public String getText() {
        return recordText;
    }

    public void setText(String text) {
        this.recordText = text;
    }
}
