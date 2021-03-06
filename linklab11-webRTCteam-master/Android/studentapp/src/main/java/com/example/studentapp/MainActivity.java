package com.example.studentapp;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
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
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity{

    //화면 세팅
    private Button sms_send;
    private LocationManager locationManager;
    private LocationListener listener;
    private String location_check = null;
    private String location1 = null;
    private String address_1= null;
    private static final int RESULT_SPEECH =1;
    private Intent o;
    private TextView tv;
    private ImageButton bt;

    //버튼 및 글 생성
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Snackbar.make(view,"Replace with your own action", Snackbar.LENGTH_LONG).show();
            }
        });
        tv = (TextView) findViewById(R.id.textView);
        bt=(ImageButton)findViewById(R.id.button2);
        user_bt=(ImageButton) findViewById(R.id.user_button);

        bt.setOnClickListener(new View.OnClickListener(){
          @Override
            public void onClick(View v){
              if(v.getId()==R.id.button2){
                  o = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                  o.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                  o.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                  o.putExtra(RecognizerIntent.EXTRA_PROMPT, "말해주세요");
                  Toast.makeText(MainActivity.this, "start speak", Toast.LENGTH_SHORT).show();
                  try{
                      startActivityForResult(i, RESULT_SPEECH);
                  }catch(ActivityNotFoundException e){
                      Toast.makeText(getApplicationContext(), "Speech To Text를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                      e.getStackTrace();
                  }
              }
          }
        });
        sms_send = (Button) findViewById(R.id.button);

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
                location1="좌표 " + location.getLongitude() + " " + location.getLatitude() + "\n" + getAddress(location.getLatitude(), location.getLongitude()) + "\n조난당하였습니다 도와주세요";
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
    }

    //버튼 누를시 gps 정보 받아오도록 하는 코드

    void enable_buttons() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,100);
            }
            return;
        }
        locationManager.requestLocationUpdates("gps", 5000, 0, listener);

        sms_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(location1!= null)
                {
                    location_check = location1;
                    sendSMS("01050411987", location1);
                }
                else
                {
                    sendSMS("01050411987", location_check);
                }
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && (requestCode == RESULT_SPEECH)){
            ArrayList<String> sstResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        }
    }
}