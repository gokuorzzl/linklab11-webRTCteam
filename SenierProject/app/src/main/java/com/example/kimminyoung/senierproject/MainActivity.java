package com.example.kimminyoung.senierproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private static final String API_KEY = "AIzaSyBtSgSJHX8rqviGq7NNMNe63Cng0w_LJXY";

    private MyAsyncTask asynctask;
    ImageButton recordButton;
    Button languageButton;
    TextView recordText;

    final Handler textViewHandler = new Handler();
    final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
    String recordLanguage = "ko-KR", translateLanguage = "en";
    String translateText = "";

    Intent recordIntent;
    SpeechRecognizer mRecognizer;
    TextToSpeech TTS;
    boolean isPermissionInternet = false, isPermissionRecordAudio = false;
    boolean RECORDING_STATE = false;

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
            String key;
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            recordText.setText("" + rs[0]);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };

    public class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            TranslateOptions options = TranslateOptions.newBuilder()
                    .setApiKey(API_KEY)
                    .build();
            Translate translate = options.getService();
            final Translation translation =
                    translate.translate(translateText,
                            Translate.TranslateOption.targetLanguage(translateLanguage));
            textViewHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (recordText.getText().toString() != null) {
                        recordText.setText(translation.getTranslatedText());
                    }
                }
            });
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordText = (TextView) findViewById(R.id.txtRecord);
        recordButton = (ImageButton) findViewById(R.id.btnRecord);
        languageButton = (Button) findViewById(R.id.btnLanguage);

        requestRuntimePermission();

    }

    public void onClick (View view){
        Log.d(TAG, "onClick");

        if (view.getId() == R.id.btnRecord){
            if (RECORDING_STATE){
                mRecognizer.stopListening();

                recordButton.setImageResource(R.drawable.recordbutton_stopped);
                RECORDING_STATE = false;
            } else {
                if(isPermissionRecordAudio == true && isPermissionInternet == true) {
                    recordIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // 음성인식 활동을 시작한다는 ACTION
                    recordIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());   // 음성 검색의 음성 인식기에 의도에서 사용된 여분의 키(패키지 이름을 왜 받아오는 지 모르겟음)
                    recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, recordLanguage);   // 선택 언어 태그

                    recordText.setText("");
                    mRecognizer = SpeechRecognizer.createSpeechRecognizer(this); // 객체 생성
                    mRecognizer.setRecognitionListener(listener);
                    mRecognizer.startListening(recordIntent);
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "엑세스 권한이 없습니다..", Toast.LENGTH_LONG).show();
                }

                recordButton.setImageResource(R.drawable.recordbutton_doing);
                RECORDING_STATE = true;
            }
        } else if (view.getId() == R.id.btnLanguage){
            final PopupMenu languagePopup = new PopupMenu(this, view);
            languagePopup.getMenuInflater().inflate(R.menu.language, languagePopup.getMenu());

            languagePopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.korean:
                            recordLanguage = "ko-KR";
                            languageButton.setText("한국어");
                            break;

                        case R.id.english:
                            recordLanguage = "en-US";
                            languageButton.setText("영어");
                            break;

                        case R.id.japanish:
                            recordLanguage = "ja-JP";
                            languageButton.setText("일본어");
                            break;

                        case R.id.chinese:
                            recordLanguage = "zh-CN";
                            languageButton.setText("중국어");
                            break;
                    }
                    return true;
                }
            });
            languagePopup.show();
        } else if (view.getId() == R.id.btnReset){
            recordText.setText("");
        } else if (view.getId() == R.id.btnSend){
            translateText = recordText.getText().toString();
            asynctask = new MyAsyncTask();
            asynctask.execute();
        }

    }

    private void requestRuntimePermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale (MainActivity.this,
                    Manifest.permission.INTERNET)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_INTERNET);
            }
        } else {
            isPermissionInternet = true;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale (MainActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        } else {
            isPermissionRecordAudio = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // ACCESS_FINE_LOCATION 권한을 얻음
                    isPermissionInternet = true;
                } else {
                    // 권한을 얻지 못 하였으므로 location 요청 작업을 수행할 수 없다
                    isPermissionInternet = false;
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ACCESS_FINE_LOCATION 권한을 얻음
                    isPermissionRecordAudio = true;
                } else {
                    // 권한을 얻지 못 하였으므로 location 요청 작업을 수행할 수 없다
                    isPermissionRecordAudio = false;
                }
                return;
            }
        }
    }
}
