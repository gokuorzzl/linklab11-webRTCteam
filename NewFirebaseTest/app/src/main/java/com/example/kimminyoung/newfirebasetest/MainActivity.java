// firebase 일종의 디비같은거고
// no sql, DB에 바로넣으면 바로바로 갱신됨.
// 민영이의 파베

// 처음 등록시 패키지명으로 등록하고
// 지금은 TEST용으로 

package com.example.kimminyoung.newfirebasetest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
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

    //gps문자전송
    private LocationManager locationManager;
    private LocationListener listener;
    private String address_1= null;
    private String location1 = null;
    private String location_check = null;

    private RecyclerView mRecyclerView, mRecyclerView2;
    private RecyclerView.Adapter mAdapter, mAdapter2;
    private RecyclerView.LayoutManager mLayoutManager, mLayouManager2;

    // 명상 : 긴급호출
    private Vibrator vibe;
    Button btnSend2, cancelVibe;


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
//    Vibrator vib;

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
            if(recordText.contains("119")!= false) {
                if (location1 != null) {
                    location1+=recordText;
                    sendSMS("01047199044", location1);
                } else {
                    location_check+=recordText;
                    sendSMS("01047199044", location_check);
                }
            }

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
        vibe = (Vibrator) getSystemService(VIBRATOR_SERVICE);

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

        //메시지 전송 시작

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria c = new Criteria();
        address_1 = locationManager.getBestProvider(c, true);
        if(address_1 == null || !locationManager.isProviderEnabled(address_1)){
            List<String> list = locationManager.getAllProviders();

            for(int i=0; i<list.size(); i++){
                String temp = list.get(i);
                if(locationManager.isProviderEnabled(temp)){
                    address_1 = temp;
                    break;
                }
            }
        }

        listener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                location1="좌표 " + location.getLongitude() + " " + location.getLatitude() + "\n" + getAddress(location.getLatitude(), location.getLongitude()) + "\n";
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        enable_buttons();
        //메시지전송끝

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);         // RecyclerView의 id, layout, size 설정

        mRecordingMessage = new ArrayList<>();          // 데이터(메시지)를 담기 위한 ArrayList
        mAdapter = new MyAdapter(mRecordingMessage);    // Adapter와 List 연동
        mRecyclerView.setAdapter(mAdapter);             // RecylerView에 Adapter 설정


        // 민영
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

        // 명상 : 긴급호출 버튼 layout id 연결
        btnSend2 = (Button) findViewById(R.id.btnSend2);
        cancelVibe = (Button) findViewById(R.id.cancelVibe);

        btnSend2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                String alText = "긴급호출";
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());
                DatabaseReference myRef = database.getReference("Alert Message").child(formattedDate);  // child: Real Database 내에서 하위 디렉토리 추가
                Hashtable<String, String> alertMessage = new Hashtable<String, String>();       // 여러 개의 값을 테이블로 저장할 경우 대비
                alertMessage.put("alertText", alText);      // "alertText"는 키 값으로 Alert Message 클래스의 alertText 변수와 일치해야 오류없이 정상적으로 작동
                myRef.setValue(alertMessage);                       // 참조한 데이터베이스에 값을 저장한다.

                //Toast.makeText(MainActivity.this, mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        cancelVibe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   // 패턴 진동을 취소
                vibe.cancel();
            }
        });

        //혹시몰라 데이터값을 실제로 넘겨주는게 필요할시 넣을 코드

//        mRecyclerView2 = (RecyclerView) findViewById(R.id.my_recycler_view2);
//        mRecyclerView2.setHasFixedSize(true);
//        mLayoutManager2 = new LinearLayoutManager(this);
//        mRecyclerView2.setLayoutManager(mLayoutManager2);         // RecyclerView의 id, layout, size 설정

//        // 긴급알람
//        mAlertMessage = new ArrayList<>();
//        mAdapter2 = new MyAdapter2(mAlertMessage);
//        mRecyclerView2.setAdapter(mAdapter2);

        // 긴급호출DB내용 불러오기
        DatabaseReference chatDBref2 = database.getReference("Alert Message");
        chatDBref2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                AlertMessage message = dataSnapshot.getValue(AlertMessage.class); // Database에 있는 data를 불러옴
//                mAlertMessage.add(message); // 불러온 메시지를 List에 순차적으로 추가
//                mAdapter2.notifyItemInserted(mAlertMessage.size() - 1 );

                // 진동세기 표시는 따로없기때문에, 보통 패턴을 이용하여 진동세기를 표현한다.
                // LG나 삼성폰의 경우는 따로 API를 이용하는 것 같다고 함.
                vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                Toast.makeText(MainActivity.this, mAlertMessage.get(mAlertMessage.size() - 1).getText().toString(), Toast.LENGTH_SHORT).show();
                long[] pattern = {500, 100, 100, 100, 500, 100, 100, 100};
                //홀수 : 진동시간,  짝수 : 대기시간
                Toast.makeText(MainActivity.this, "긴급호출", Toast.LENGTH_SHORT).show();
                vibe.vibrate(pattern, 0); // 0 : 무한반복, -1 반복없음

                /*if (isChannelStarted == true){      // Send 버튼 클릭 -> onChildAdded가 호출 -> if 문 실행(각자 이 부분 수정 필요)
                    Toast.makeText(MainActivity.this, "긴급호출", Toast.LENGTH_SHORT).show();
                    vibe.vibrate(pattern, 0); // 0 : 무한반복, -1 반복없음
                }*/
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

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
                            translatedTTS.setLanguage(Locale.KOREAN);
                            btnTranlateLanguage.setText("수신언어: 한국어");
                            break;

                        case R.id.english:
                            translateLanguage = "en";
                            TTSLanguage = "ENGLISH";
                            translatedTTS.setLanguage(Locale.ENGLISH);
                            btnTranlateLanguage.setText("수신언어: 영어");
                            break;

                        case R.id.japanish:
                            translateLanguage = "ja";
                            TTSLanguage = "JAPANESE";
                            translatedTTS.setLanguage(Locale.JAPANESE);
                            btnTranlateLanguage.setText("수신언어: 일본어");
                            break;

                        case R.id.chinese:
                            translateLanguage = "zh";
                            TTSLanguage = "CHINESE";
                            translatedTTS.setLanguage(Locale.CHINESE);
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

    //이 아래로는 gps메시지 전송 코드
    void enable_buttons() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                        ,100);
            }
            return;
        }
        locationManager.requestLocationUpdates("gps", 5000, 0, listener);

        if (location1 != null) {
            location_check = location1;
        }

    }

    //여기서 미리 사용자에게 요청했던 값을 반환하여 허락이면 그대로 실행 아니면 다시 허락요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                enable_buttons();
                break;
            default:
                break;
        }
    }

    public String getAddress(double lat, double lng){
        String address = null;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> list = null;

        try{
            list = geocoder.getFromLocation(lat, lng, 1);
        }catch(Exception e){
            e.printStackTrace();
        }

        if(list == null){
            Log.e("getAddress", "주소 데이터 얻기 실패");
            return null;
        }
        if(list.size()>0){
            Address addr = list.get(0);
            address = addr.getCountryName() + " " + addr.getPostalCode() + " " + addr.getLocality() + " " + addr.getThoroughfare() + " " + addr.getFeatureName();
        }
        return address;
    }

    //문자 전송
    private void sendSMS(String phoneNumber, String message) {

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";


        // 문자 보내는 상태를 감지하는 PendingIntent
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        // 문자 받은 상태를 감지하는 PendingIntent
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);


        // 문자 보내는 상태를 감지하는 BroadcastReceiver를 등록한다.
        registerReceiver(new BroadcastReceiver() {

            // 문자를 수신하면, 발생.
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        // 문자를 받는 상태를 확인하는 BroadcastReceiver를 등록.
        registerReceiver(new BroadcastReceiver() {


            // 문자를 받게 되면, 불린다.
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));


        // SmsManager를 가져온다.
        SmsManager sms = SmsManager.getDefault();
        // sms를 보낸다.
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

    }
}
