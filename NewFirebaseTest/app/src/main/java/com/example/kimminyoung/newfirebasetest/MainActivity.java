package com.example.kimminyoung.newfirebasetest;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    EditText etText;
    Button btnSend, btnStartCh;
    final Handler textViewHandler = new Handler();
    private MyAsyncTask asynctask;
    FirebaseDatabase database;
    List<RecordingMessage> mRecordingMessage;

    TextToSpeech translatedTTS;
    Vibrator vib;

    String recordLanguage = "ko-KR", translateLanguage = "en";
    String recordText = "", translationText;


    boolean isChannelStarted = false;       // send 버튼을 누를 때의 시점과 onChildAdded가 호출되는 시점이 일치하지가 않아 순차적 실행을 하기 위한 flag
    boolean isPermissionInternet = false, isPermissionRecordAudio = false;

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
                translatedTTS.setLanguage(Locale.ENGLISH);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        database = FirebaseDatabase.getInstance();      // Firebase에서 데이터베이스 불러오기
        translatedTTS = new TextToSpeech(this, listenerTTS);

        etText = (EditText) findViewById(R.id.etText);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnStartCh = (Button) findViewById(R.id.btnStartChannel);
        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        btnStartCh.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                isChannelStarted = true;    // 앱을 시작하면 기존 디비를 불러오를 과정에서도 onChildAdded가 호출되는 문제 때문에 설정한 flag
            }
        });

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
                    });


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
                                recordText = mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString();
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
