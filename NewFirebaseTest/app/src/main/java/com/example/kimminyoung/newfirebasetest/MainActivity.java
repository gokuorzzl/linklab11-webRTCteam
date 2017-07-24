package com.example.kimminyoung.newfirebasetest;

import android.os.Bundle;
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

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    EditText etText;
    Button btnSend, btnStartCh;

    FirebaseDatabase database;
    List<RecordingMessage> mRecordingMessage;

    boolean isChannelStarted = false;       // send 버튼을 누를 때의 시점과 onChildAdded가 호출되는 시점이 일치하지가 않아 순차적 실행을 하기 위한 flag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();      // Firebase에서 데이터베이스 불러오기

        etText = (EditText) findViewById(R.id.etText);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnStartCh = (Button) findViewById(R.id.btnStartChannel);

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

        btnStartCh.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                isChannelStarted = true;    // 앱을 시작하면 기존 디비를 불러오를 과정에서도 onChildAdded가 호출되는 문제 때문에 설정한 flag
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

                if (isChannelStarted == true){      // Send 버튼 클릭 -> onChildAdded가 호출 -> if 문 실행(각자 이 부분 수정 필요)
                    Toast.makeText(MainActivity.this, mRecordingMessage.get(mRecordingMessage.size() - 1).getText().toString(), Toast.LENGTH_SHORT).show();
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
    }
}
