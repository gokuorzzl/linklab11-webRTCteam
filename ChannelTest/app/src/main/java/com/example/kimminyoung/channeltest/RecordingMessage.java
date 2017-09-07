package com.example.kimminyoung.channeltest;

/**
 * Created by KimMinYoung on 2017-07-23.
 */

public class RecordingMessage {
    public String recordText;

    public RecordingMessage() {

    }

    public RecordingMessage(String text) {
        this.recordText = text;
    }

    public String getText() {
        return recordText;
    }

    public void setText(String text) {
        this.recordText = text;
    }
}