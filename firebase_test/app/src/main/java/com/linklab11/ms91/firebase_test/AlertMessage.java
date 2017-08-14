package com.linklab11.ms91.firebase_test;

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
