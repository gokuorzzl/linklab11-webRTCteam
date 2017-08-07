// firebase 일종의 디비같은거고
// no sql, DB에 바로넣으면 바로바로 갱신됨.
// 민영이의 파베

// 처음 등록시 패키지명으로 등록하고
// 지금은 TEST용으로 

package com.example.kimminyoung.newfirebasetest;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static final String API_KEY = "AIzaSyBtSgSJHX8rqviGq7NNMNe63Cng0w_LJXY";

    TextView recordTextView;
    Button btnSend, btnStartCh;
    Button btnRecordLanguage, btnTranlateLanguage;
    ImageButton btnRecord;
    final Handler textViewHandler = new Handler();
    private MyAsyncTask asynctask;
    FirebaseDatabase database;
    List<RecordingMessage> mRecordingMessage;

    TextToSpeech translatedTTS;
    Vibrator vib;

    String recordLanguage = "ko-KR", translateLanguage = "en", TTSLanguage = "ENGLISH";
    String recordText = "", translationText;

    Intent recordIntent;
    SpeechRecognizer mRecognizer;

    boolean isChannelStarted = false;       // send 버튼을 누를 때의 시점과 onChildAdded가 호출되는 시점이 일치하지가 않아 순차적 실행을 하기 위한 flag
    boolean isPermissionInternet = false, isPermissionRecordAudio = false;
    boolean RECORDING_STATE = false;

    final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2;


    public class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            TranslateOptions options = TranslateOptions.newBuilder()
                    .setApiKey(API_KEY)
                    .build();
            Translate translate = options.getService();
            final Translation translation =
                    translate.translate(recordText,
                            Translate.TranslateOption.targetLanguage(translateLanguage));
            textViewHandler.post(new Runnable() {
                @Override
                public void run() {
                    translationText = translation.getTranslatedText();
                    Toast.makeText(MainActivity.this, translationText, Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        latestVersionTTS(translationText);
                    } else {
                        previousVersionTTS(translationText);

                    }
                }
            });
            return null;
        }
    }
    private TextToSpeech.OnInitListener listenerTTS =  new TextToSpeech.OnInitListener() {
        public void onInit(int status){
            if(status != TextToSpeech.ERROR) {
                if (TTSLanguage == "ENGLISH")
                    translatedTTS.setLanguage(Locale.ENGLISH);
                else if (TTSLanguage == "KOREAN")
                    translatedTTS.setLanguage(Locale.KOREAN);
                else if (TTSLanguage == "JAPANESE")
                    translatedTTS.setLanguage(Locale.JAPANESE);
                else if (TTSLanguage == "CHINESE")
                    translatedTTS.setLanguage(Locale.CHINESE);
            }
        }
    };
    private RecognitionListener listenerSTT = new RecognitionListener() {
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
            recordTextView.setText("" + rs[0]);
            recordText = "" + rs[0];

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());
            DatabaseReference myRef = database.getReference("Recording Message").child(formattedDate);  // child: Real Database 내에서 하위 디렉토리 추가

            Hashtable<String, String> recordMessage = new Hashtable<String, String>();       // 여러 개의 값을 테이블로 저장할 경우 대비
            recordMessage.put("recordText", recordText);      // "recordText"는 키 값으로 Recording Message 클래스의 recordText 변수와 일치해야 오류없이 정상적으로 작동
            myRef.setValue(recordMessage);                       // 참조한 데이터베이스에 값을 저장한다.
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        database = FirebaseDatabase.getInstance();      // Firebase에서 데이터베이스 불러오기
        translatedTTS = new TextToSpeech(this, listenerTTS);

        recordTextView = (TextView) findViewById(R.id.txtRecord);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnRecord = (ImageButton) findViewById(R.id.btnRecord);
        btnRecordLanguage = (Button) findViewById(R.id.btnRecordLanguage);
        btnTranlateLanguage = (Button) findViewById(R.id.btnTranslateLanguage);
        btnStartCh = (Button) findViewById(R.id.btnStartChannel);
        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        btnStartCh.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                isChannelStarted = true;    // 앱을 시작하면 기존 디비를 불러오를 과정에서도 onChildAdded가 호출되는 문제 때문에 설정한 flag
            }
        });

        /*
        btnSend.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                recordText = etText.getText().toString();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());
                DatabaseReference myRef = database.getReference("Recording Message").child(formattedDate);  // child: Real Database 내에서 하위 디렉토리 추가

                Hashtable<String, String> recordMessage = new Hashtable<String, String>();       // 여러 개의 값을 테이블로 저장할 경우 대비
                recordMessage.put("recordText", recordText);      // "recordText"는 키 값으로 Recording Message 클래스의 recordText 변수와 일치해야 오류없이 정상적으로 작동
                myRef.setValue(recordMessage);                       // 참조한 데이터베이스에 값을 저장한다.

                //Toast.makeText(MainActivity.this, mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });*/


        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);         // RecyclerView의 id, layout, size 설정

        mRecordingMessage = new ArrayList<>();          // 데이터(메시지)를 담기 위한 ArrayList
        mAdapter = new MyAdapter(mRecordingMessage);    // Adapter와 List 연동
        mRecyclerView.setAdapter(mAdapter);             // RecylerView에 Adapter 설정

        DatabaseReference chatDBref = database.getReference("Recording Message");
        chatDBref.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            RecordingMessage message = dataSnapshot.getValue(RecordingMessage.class); // Database에 있는 data를 불러옴
                            mRecordingMessage.add(message); // 불러온 메시지를 List에 순차적으로 추가
                            mAdapter.notifyItemInserted(mRecordingMessage.size() - 1 );

                            if (isChannelStarted == true){     // Send 버튼 클릭 -> onChildAdded가 호출 -> if 문 실행(각자 이 부분 수정 필요)
                                //Toast.makeText(MainActivity.this, mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString(), Toast.LENGTH_SHORT).show();
                                //String text = mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString();
                                recordText = mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString();  // 어느 기기에서 사용해도 가장 최근 인식한 음성데이터를 대상으로 작용
                                asynctask = new MyAsyncTask();
                                asynctask.execute();
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        requestRuntimePermission();
    }

    public void onClick (View view) {
        if (view.getId() == R.id.btnRecord) {
            if (RECORDING_STATE) {
                mRecognizer.stopListening();

                btnRecord.setImageResource(R.drawable.recordbutton_stopped);
                RECORDING_STATE = false;
            } else {
                if (isPermissionRecordAudio == true && isPermissionInternet == true) {
                    recordIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // 음성인식 활동을 시작한다는 ACTION
                    recordIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());   // 음성 검색의 음성 인식기에 의도에서 사용된 여분의 키(패키지 이름을 왜 받아오는 지 모르겟음)
                    recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, recordLanguage);   // 선택 언어 태그

                    recordTextView.setText("");
                    mRecognizer = SpeechRecognizer.createSpeechRecognizer(this); // 객체 생성
                    mRecognizer.setRecognitionListener(listenerSTT);
                    mRecognizer.startListening(recordIntent);
                } else {
                    //Toast.makeText(MainActivity.this, "엑세스 권한이 없습니다..", Toast.LENGTH_SHORT).show();
                }

                btnRecord.setImageResource(R.drawable.recordbutton_doing);
                RECORDING_STATE = true;
            }
        } else if (view.getId() == R.id.btnRecordLanguage) {
            final PopupMenu languagePopup = new PopupMenu(this, view);
            languagePopup.getMenuInflater().inflate(R.menu.language_record, languagePopup.getMenu());

            languagePopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.korean:
                            recordLanguage = "ko-KR";
                            btnRecordLanguage.setText("송신 언어: 한국어");
                            break;

                        case R.id.english:
                            recordLanguage = "en-US";
                            btnRecordLanguage.setText("송신 언어: 영어");
                            break;

                        case R.id.japanish:
                            recordLanguage = "ja-JP";
                            btnRecordLanguage.setText("송신 언어: 일본어");
                            break;

                        case R.id.chinese:
                            recordLanguage = "zh-CN";
                            btnRecordLanguage.setText("송신 언어: 중국어");
                            break;
                    }
                    return true;
                }
            });
            languagePopup.show();
        }  else if (view.getId() == R.id.btnTranslateLanguage) {
            final PopupMenu languagePopup = new PopupMenu(this, view);
            languagePopup.getMenuInflater().inflate(R.menu.language_translation, languagePopup.getMenu());

            languagePopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.korean:
                            translateLanguage = "ko";
                            TTSLanguage = "KOREAN";
                            btnTranlateLanguage.setText("수신언어: 한국어");
                            break;

                        case R.id.english:
                            translateLanguage = "en";
                            TTSLanguage = "ENGLISH";
                            btnTranlateLanguage.setText("수신언어: 영어");
                            break;

                        case R.id.japanish:
                            translateLanguage = "ja";
                            TTSLanguage = "JAPANESE";
                            btnTranlateLanguage.setText("수신언어: 일본어");
                            break;

                        case R.id.chinese:
                            translateLanguage = "zh";
                            TTSLanguage = "CHINESE";
                            btnTranlateLanguage.setText("수신언어: 중국어");
                            break;
                    }
                    return true;
                }
            });
            languagePopup.show();
        }
    }

    @SuppressWarnings("deprecation")
    private void previousVersionTTS(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        translatedTTS.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void latestVersionTTS(String text) {
        String utteranceId = this.hashCode() + "";
        translatedTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void requestRuntimePermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale (MainActivity.this,
                    android.Manifest.permission.INTERNET)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_INTERNET);
            }
        } else {
            isPermissionInternet = true;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale (MainActivity.this,
                    android.Manifest.permission.RECORD_AUDIO)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.RECORD_AUDIO},
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
                    isPermissionInternet = true;
                } else {
                    isPermissionInternet = false;
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionRecordAudio = true;
                } else {
                    isPermissionRecordAudio = false;
                }
                return;
            }
        }
    }
}
