package com.linklab11.ms91.firebase_test;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView, mRecyclerView2;
    private RecyclerView.Adapter mAdapter, mAdapter2;
    private RecyclerView.LayoutManager mLayoutManager, mLayoutManager2;
    private Vibrator vibe;

    EditText etText, etText2;
    Button btnSend, btnSend2, btnStartCh, cancelVibe;


    FirebaseDatabase database;
    List<RecordingMessage> mRecordingMessage;
    List<RecordingMessage2> mRecordingMessage2;

    boolean isChannelStarted = false;       // send 버튼을 누를 때의 시점과 onChildAdded가 호출되는 시점이 일치하지가 않아 순차적 실행을 하기 위한 flag

    // 순서
    // 1. 디비를 새로만들고
    // 2. 디비값을

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();      // Firebase에서 데이터베이스 불러오기

        etText = (EditText) findViewById(R.id.etText);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnStartCh = (Button) findViewById(R.id.btnStartChannel);
        cancelVibe = (Button) findViewById(R.id.cancelVibe);

        etText2 = (EditText) findViewById(R.id.etText2); // Layout의 id와 연결
        btnSend2 = (Button) findViewById(R.id.btnSend2);

        btnSend.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                String stText = etText.getText().toString();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());
                DatabaseReference myRef = database.getReference("Recording Message").child(formattedDate);  // child: Real Database 내에서 하위 디렉토리 추가
                Hashtable<String, String> recordMessage = new Hashtable<String, String>();       // 여러 개의 값을 테이블로 저장할 경우 대비
                recordMessage.put("recordText", stText);      // "recordText"는 키 값으로 Recording Message 클래스의 recordText 변수와 일치해야 오류없이 정상적으로 작동
                myRef.setValue(recordMessage);                       // 참조한 데이터베이스에 값을 저장한다.

                //Toast.makeText(MainActivity.this, mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        btnSend2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                String stText = etText2.getText().toString();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());
                DatabaseReference myRef = database.getReference("Recording Message2").child(formattedDate);  // child: Real Database 내에서 하위 디렉토리 추가
                Hashtable<String, String> recordMessage2 = new Hashtable<String, String>();       // 여러 개의 값을 테이블로 저장할 경우 대비
                recordMessage2.put("recordText", stText);      // "recordText"는 키 값으로 Recording Message 클래스의 recordText 변수와 일치해야 오류없이 정상적으로 작동
                myRef.setValue(recordMessage2);                       // 참조한 데이터베이스에 값을 저장한다.

                //Toast.makeText(MainActivity.this, mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        btnStartCh.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                isChannelStarted = true;    // 앱을 시작하면 기존 디비를 불러오를 과정에서도 onChildAdded가 호출되는 문제 때문에 설정한 flag
            }
        });

        cancelVibe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   // 패턴 진동을 취소
                vibe.cancel();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);         // RecyclerView의 id, layout, size 설정

        mRecordingMessage = new ArrayList<>();          // 데이터(메시지)를 담기 위한 ArrayList
        mAdapter = new MyAdapter(mRecordingMessage);    // Adapter와 List 연동
        mRecyclerView.setAdapter(mAdapter);             // RecylerView에 Adapter 설정

        mRecyclerView2 = (RecyclerView) findViewById(R.id.my_recycler_view2);
        mRecyclerView2.setHasFixedSize(true);
        mLayoutManager2 = new LinearLayoutManager(this);
        mRecyclerView2.setLayoutManager(mLayoutManager2);         // RecyclerView의 id, layout, size 설정

        // 긴급알람
        mRecordingMessage2 = new ArrayList<>();
        mAdapter2 = new MyAdapter2(mRecordingMessage2);
        mRecyclerView2.setAdapter(mAdapter2);

        DatabaseReference chatDBref = database.getReference("Recording Message");
        chatDBref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                RecordingMessage message = dataSnapshot.getValue(RecordingMessage.class); // Database에 있는 data를 불러옴
                mRecordingMessage.add(message); // 불러온 메시지를 List에 순차적으로 추가
                mAdapter.notifyItemInserted(mRecordingMessage.size() - 1 );

                // 진동세기 표시는 따로없기때문에, 보통 패턴을 이용하여 진동세기를 표현한다.
                // LG나 삼성폰의 경우는 따로 API를 이용하는 것 같다고 함.
                vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                Toast.makeText(MainActivity.this, mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString(), Toast.LENGTH_SHORT).show();
                long[] pattern = {500, 100, 100, 100, 500, 100, 100, 100};
                //홀수 : 진동시간,  짝수 : 대기시간,

                if (isChannelStarted == true){      // Send 버튼 클릭 -> onChildAdded가 호출 -> if 문 실행(각자 이 부분 수정 필요)
                    Toast.makeText(MainActivity.this, mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString(), Toast.LENGTH_SHORT).show();
                    vibe.vibrate(pattern, 0); // 0 : 무한반복, -1 반복없음
                }
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

        // 긴급호출DB내용 불러오기
        DatabaseReference chatDBref2 = database.getReference("Recording Message2");
        chatDBref2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                RecordingMessage2 message = dataSnapshot.getValue(RecordingMessage2.class); // Database에 있는 data를 불러옴
                mRecordingMessage2.add(message); // 불러온 메시지를 List에 순차적으로 추가
                mAdapter2.notifyItemInserted(mRecordingMessage.size() - 1 );

                // 진동세기 표시는 따로없기때문에, 보통 패턴을 이용하여 진동세기를 표현한다.
                // LG나 삼성폰의 경우는 따로 API를 이용하는 것 같다고 함.
                vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                Toast.makeText(MainActivity.this, mRecordingMessage2.get(mRecordingMessage2.size() - 1).getText().toString(), Toast.LENGTH_SHORT).show();
                long[] pattern = {500, 100, 100, 100, 500, 100, 100, 100};
                //홀수 : 진동시간,  짝수 : 대기시간,

                if (isChannelStarted == true){      // Send 버튼 클릭 -> onChildAdded가 호출 -> if 문 실행(각자 이 부분 수정 필요)
                    Toast.makeText(MainActivity.this, mRecordingMessage2.get(mRecordingMessage2.size() - 1).getText().toString(), Toast.LENGTH_SHORT).show();
                    vibe.vibrate(pattern, 0); // 0 : 무한반복, -1 반복없음
                }
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
}

