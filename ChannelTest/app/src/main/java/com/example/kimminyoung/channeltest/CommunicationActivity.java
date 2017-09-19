package com.example.kimminyoung.channeltest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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

public class CommunicationActivity extends AppCompatActivity {

    private ListView listView;
    private EditText editText;
    private Button btnSendVib, btnCacnelVib, btnSenderLanguage, btnReceiverLanguage;
    ImageButton btnRecord;
    TextView recordTextView;
    final Handler textViewHandler = new Handler();

    List<RecordingMessage> mRecordingMessage;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Vibrator vibe;
    private String chName;
    private String address_1= null, location1 = null, location_check = null;
    String senderLanguage = "", receiverLanguage = "", TTSLanguage ="";
    String recordText = "", translationText;

    long entranceTime, currentTime;

    FirebaseDatabase database;
    private DatabaseReference alertRef;

    private MyAsyncTask asynctask;

    Intent recordIntent;
    SpeechRecognizer mRecognizer;
    TextToSpeech translatedTTS;
    LocationManager locationManager;
    LocationListener locationListener;

    boolean isPermissionInternet = false, isPermissionRecordAudio = false;

    boolean EXCEPTION_MYSELF_STATE = false; // 녹음한 본인은 데이터 출력을 수신 받지 않도록 하기 위한 flag
    boolean RECORDING_STATE = false;

    private static final String API_KEY = "AIzaSyBtSgSJHX8rqviGq7NNMNe63Cng0w_LJXY";
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
                            Translate.TranslateOption.targetLanguage(receiverLanguage));
            textViewHandler.post(new Runnable() {
                @Override
                public void run() {
                    translationText = translation.getTranslatedText();
                    Toast.makeText(CommunicationActivity.this, translationText, Toast.LENGTH_SHORT).show();
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

            btnRecord.setImageResource(R.drawable.mic);
            RECORDING_STATE = false;

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
            DatabaseReference myRef = database.getReference(chName).child("Recording Message").child(formattedDate);  // child: Real Database 내에서 하위 디렉토리 추가

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);

        listView = (ListView) findViewById(R.id.list);
        btnRecord = (ImageButton) findViewById(R.id.btnRecord);
        btnCacnelVib = (Button) findViewById(R.id.btnCancelVib);
        btnSendVib = (Button) findViewById(R.id.btnSendVib);
        btnSenderLanguage = (Button) findViewById(R.id.btnSenderLanguage);
        btnReceiverLanguage = (Button) findViewById(R.id.btnReceiverLanguage);
        recordTextView = (TextView) findViewById(R.id.txtRecord);

        translatedTTS = new TextToSpeech(this, listenerTTS);

        chName = getIntent().getExtras().get("Channel Name").toString();
        entranceTime = getIntent().getExtras().getLong("Entrance Time") / 1000;  // 채널 입장 시간을 초 단위로 변환

        setTitle("채널 " + chName);

        requestRuntimePermission();
        database = FirebaseDatabase.getInstance();  // Firebase DB를 불러옴

        checkEntrance();
        //arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, room);
        //listView.setAdapter(arrayAdapter);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);         // RecyclerView의 id, layout, size 설정

        mRecordingMessage = new ArrayList<>();          // 데이터(메시지)를 담기 위한 ArrayList
        mAdapter = new MyAdapter(mRecordingMessage);    // Adapter와 List 연동
        mRecyclerView.setAdapter(mAdapter);             // RecylerView에 Adapter 설정

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

        locationListener = new LocationListener() {

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

        // Firedase DB에 녹음된 data가 추가될 시 asynctask를 이용하여 번역과 STT 작업 수행
        DatabaseReference recordingDBref = database.getReference(chName).child("Recording Message");
        recordingDBref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                currentTime = System.currentTimeMillis() / 1000;
                if(currentTime - entranceTime > 10) {
                    RecordingMessage message = dataSnapshot.getValue(RecordingMessage.class); // Database에 있는 data를 불러옴
                    mRecordingMessage.add(message); // 불러온 메시지를 List에 순차적으로 추가
                    mAdapter.notifyItemInserted(mRecordingMessage.size() - 1);

                    if (EXCEPTION_MYSELF_STATE)
                        EXCEPTION_MYSELF_STATE = false;
                    else {
                        recordText = mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString();  // 어느 기기에서 사용해도 가장 최근 인식한 음성데이터를 대상으로 작용
                        asynctask = new MyAsyncTask();
                        asynctask.execute();
                    }
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

        // Firebase DB에 AlertAction이 추가될 경우 알람 및 진동 처리
        DatabaseReference alertDBref = database.getReference(chName).child("Alert Action");
        alertDBref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                currentTime = System.currentTimeMillis() / 1000;
                if(currentTime - entranceTime > 10) {
                    vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {500, 100, 100, 100, 500, 100, 100, 100};
                    // 홀수 : 진동시간,  짝수 : 대기시간
                    // 0 : 무한반복, -1 반복없음
                    Toast.makeText(CommunicationActivity.this, "긴급호출", Toast.LENGTH_SHORT).show();
                    vibe.vibrate(pattern, 0);
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

        // Alert Action을 Firebase로 전송
        btnSendVib.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                currentTime = System.currentTimeMillis() / 1000;
                if(currentTime - entranceTime > 10) {
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String formattedDate = df.format(c.getTime());
                    String alText = "긴급호출";
                    alertRef = database.getReference(chName).child("Alert Action").child(formattedDate);  // child: Real Database 내에서 하위 디렉토리 추가
                    Hashtable<String, String> alertMessage = new Hashtable<String, String>();       // 여러 개의 값을 테이블로 저장할 경우 대비
                    alertMessage.put("alertText", alText);      // "alertText"는 키 값으로 Alert Message 클래스의 alertText 변수와 일치해야 오류없이 정상적으로 작동
                    alertRef.setValue(alertMessage);    // 참조한 데이터베이스에 값을 저장한다.
                } else{
                    Toast.makeText(CommunicationActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 진동 중지
        btnCacnelVib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibe.cancel();  // 패턴 진동을 취소
            }
        });
    }

    public void onClick (View view)  {
        if (view.getId() == R.id.btnRecord) {
            if (RECORDING_STATE) {
                mRecognizer.stopListening();
                btnRecord.setImageResource(R.drawable.mic);
                RECORDING_STATE = false;
            } else {
                if (isPermissionRecordAudio == true && isPermissionInternet == true) {
                    currentTime = System.currentTimeMillis() / 1000;     // 현재 시각을 초 단위로 변환
                    if(currentTime - entranceTime > 10) {
                        recordIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // 음성인식 활동을 시작한다는 ACTION
                        recordIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());   // 음성 검색의 음성 인식기에 의도에서 사용된 여분의 키(패키지 이름을 왜 받아오는 지 모르겟음)
                        recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, senderLanguage);   // 선택 언어 태그

                        recordTextView.setText("");
                        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this); // 객체 생성
                        mRecognizer.setRecognitionListener(listenerSTT);
                        mRecognizer.startListening(recordIntent);

                        btnRecord.setImageResource(R.drawable.mic);
                        RECORDING_STATE = true;
                        EXCEPTION_MYSELF_STATE = true;
                    } else {
                        Toast.makeText(CommunicationActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                }
            }
        } else if (view.getId() == R.id.btnSenderLanguage) {
            final PopupMenu languagePopup = new PopupMenu(this, view);
            languagePopup.getMenuInflater().inflate(R.menu.language_record, languagePopup.getMenu());

            languagePopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.korean:
                            senderLanguage = "ko-KR";
                            btnSenderLanguage.setText("송신 언어: 한국어");
                            break;

                        case R.id.english:
                            senderLanguage = "en-US";
                            btnSenderLanguage.setText("송신 언어: 영어");
                            break;

                        case R.id.japanish:
                            senderLanguage = "ja-JP";
                            btnSenderLanguage.setText("송신 언어: 일본어");
                            break;

                        case R.id.chinese:
                            senderLanguage = "zh-CN";
                            btnSenderLanguage.setText("송신 언어: 중국어");
                            break;
                    }
                    return true;
                }
            });
            languagePopup.show();
        }  else if (view.getId() == R.id.btnReceiverLanguage) {
            final PopupMenu languagePopup = new PopupMenu(this, view);
            languagePopup.getMenuInflater().inflate(R.menu.language_translation, languagePopup.getMenu());

            languagePopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.korean:
                            receiverLanguage = "ko";
                            TTSLanguage = "KOREAN";
                            translatedTTS.setLanguage(Locale.KOREAN);
                            btnReceiverLanguage.setText("수신언어: 한국어");
                            break;

                        case R.id.english:
                            receiverLanguage = "en";
                            TTSLanguage = "ENGLISH";
                            translatedTTS.setLanguage(Locale.ENGLISH);
                            btnReceiverLanguage.setText("수신언어: 영어");
                            break;

                        case R.id.japanish:
                            receiverLanguage = "ja";
                            TTSLanguage = "JAPANESE";
                            translatedTTS.setLanguage(Locale.JAPANESE);
                            btnReceiverLanguage.setText("수신언어: 일본어");
                            break;

                        case R.id.chinese:
                            receiverLanguage = "zh";
                            TTSLanguage = "CHINESE";
                            translatedTTS.setLanguage(Locale.CHINESE);
                            btnReceiverLanguage.setText("수신언어: 중국어");
                            break;
                    }
                    return true;
                }
            });
            languagePopup.show();
        }else if(view.getId() == R.id.btn119){
            if (location1 != null) {
                location1+=" 긴급상황입니다";
                sendSMS("01047199044", location1);
            } else {
                location_check+=" 긴급상황입니다";
                sendSMS("01047199044", location_check);
            }
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

    void enable_buttons() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                        ,100);
            }
            return;
        }
        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);

        if (location1 != null) {
            location_check = location1;
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

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // 채널 입장 전 입장 여부 확인
    private void checkEntrance() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("채널" + chName + "에 입장하시겠습니까?");

        builder.setNegativeButton("" +
                "예", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                finish();
            }
        });
        builder.show();
    }

    private void requestRuntimePermission() {
        if (ContextCompat.checkSelfPermission(CommunicationActivity.this,
                android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale (CommunicationActivity.this,
                    android.Manifest.permission.INTERNET)) {
            } else {
                ActivityCompat.requestPermissions(CommunicationActivity.this, new String[]{android.Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_INTERNET);
            }
        } else {
            isPermissionInternet = true;
        }

        if (ContextCompat.checkSelfPermission(CommunicationActivity.this,
                android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale (CommunicationActivity.this,
                    android.Manifest.permission.RECORD_AUDIO)) {
            } else {
                ActivityCompat.requestPermissions(CommunicationActivity.this, new String[]{android.Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        } else {
            isPermissionRecordAudio = true;
        }
    }
}
