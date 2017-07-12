package com.example.kimminyoung.stt_test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class ChannelActivity extends AppCompatActivity {

    Intent i;
    SpeechRecognizer mRecognizer;
    TextToSpeech TTS;
    EditText textView;

    boolean pmsInternet = false, pmsRecordAudio = false;
    final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
    //boolean RECORD_PROCESSING = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermission();


        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // 음성인식 활동을 시작한다는 ACTION
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());   // 음성 검색의 음성 인식기에 의도에서 사용된 여분의 키(패키지 이름을 왜 받아오는 지 모르겟음)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");   // 선택 언어 태그

        //TTS = new TextToSpeech(this, listenerTTS);
        textView = (EditText) findViewById(R.id.textView);
    }

    public void onClick(View view){
        if(view.getId() == R.id.btnRecord) {
            if(pmsRecordAudio == true && pmsInternet == true) {
                textView.setText("");
                mRecognizer = SpeechRecognizer.createSpeechRecognizer(this); // 객체 생성
                mRecognizer.setRecognitionListener(listener);
                mRecognizer.startListening(i);
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "엑세스 권한이 없습니다..", Toast.LENGTH_LONG).show();
            }
        }

        if (view.getId() == R.id.btnBack) {
            finish();
        }
        /*
        if(view.getId() == R.id.btnRecordStop){
            mRecognizer.stopListening();
        }

        if(view.getId() == R.id.btnSpeech){
            //String text = textView.getText().toString();
            String text = "I'm so tired";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                latestVersionTTS(text);
            } else {
                previousVersionTTS(text);
            }
        }
        */
    }

    private RecognitionListener listener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle params) {
        }
        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
        }

        @Override
        public void onResults(Bundle results) {
            ///
            String key= "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            textView.setText("" + rs[0]);
            ///
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };

    /*
    private OnInitListener listenerTTS =  new OnInitListener() {
        public void onInit(int status){
            if(status != TextToSpeech.ERROR) {
                TTS.setLanguage(Locale.ENGLISH);
            }
        }

    };

    @SuppressWarnings("deprecation")
    private void previousVersionTTS(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        TTS.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void latestVersionTTS(String text) {
        String utteranceId = this.hashCode() + "";
        TTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }*/

    private void requestRuntimePermission() {

        if (ContextCompat.checkSelfPermission(ChannelActivity.this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale (ChannelActivity.this,
                    Manifest.permission.INTERNET)) {
            } else {
                ActivityCompat.requestPermissions(ChannelActivity.this, new String[]{Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_INTERNET);
            }
        } else {
            pmsInternet = true;
        }

        if (ContextCompat.checkSelfPermission(ChannelActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale (ChannelActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {
            } else {
                ActivityCompat.requestPermissions(ChannelActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        } else {
            pmsRecordAudio = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // read_external_storage-related task you need to do.

                    // ACCESS_FINE_LOCATION 권한을 얻음
                    pmsInternet = true;

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    // 권한을 얻지 못 하였으므로 location 요청 작업을 수행할 수 없다
                    // 적절히 대처한다
                    pmsInternet = false;

                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // read_external_storage-related task you need to do.

                    // ACCESS_FINE_LOCATION 권한을 얻음
                    pmsRecordAudio = true;

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    // 권한을 얻지 못 하였으므로 location 요청 작업을 수행할 수 없다
                    // 적절히 대처한다
                    pmsRecordAudio = false;

                }
                return;
            }
        }
    }
}
