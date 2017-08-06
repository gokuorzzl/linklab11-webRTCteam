package com.linklab11.ms91.firebase_test;

/**
 * Created by User on 2017-07-24.
 */

public class RecordingMessage2 {
    public String recordText;

    public RecordingMessage2() {
    }

    public RecordingMessage2(String text) {
        this.recordText = text;
    }

    public String getText() {
        return recordText;
    }

    public void setText(String text) {
        this.recordText = text;
    }
}
