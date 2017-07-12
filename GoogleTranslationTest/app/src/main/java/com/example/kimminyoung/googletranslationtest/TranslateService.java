package com.example.kimminyoung.googletranslationtest;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Kim Min Young on 2017-07-04.
 */

public class TranslateService extends AsyncTask {

    private final static String URL = "http://www.googleapis.com/language/translate/v2?key=";
    private final static String KEY = "Your App API Key";
    private final static String TARGET = "&target=ko";
    private final static String SOURCE = "&source=en";
    private final static String QUERY = "&q=";

    String englishString = "Original English String";
    String koreaString;

    protected Object doInBackground(Object... params){
        StringBuilder result = new StringBuilder();

        try{
            String encodedText = URLEncoder.encode(englishString, "UTF-8");
            URL url = new URL(URL + KEY + SOURCE + TARGET + QUERY + encodedText);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            InputStream stream;

            if(conn.getResponseCode() == 200){
                stream = conn.getInputStream();
            } else {
                stream = conn.getErrorStream();
            }
        } catch(IOException | JsonSyntaxException ex) {
            Log.e("Google Translate Task", ex.getMessage());
        }

        return null;
    }
}
