package com.example.kimminyoung.newfirebasetest;

/**
 * Created by User on 2017-07-24.
 */

public class AlertMessage {
    public String alertText;

    public AlertMessage() {
    }

    public AlertMessage(String text) {
        this.alertText = text;
    }

    public String getText() {
        return alertText;
    }

    public void setText(String text) {
        this.alertText = text;
    }
}
