package com.example.kimminyoung.channeltest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private Button button, createChBtn;

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> channel = new ArrayList<>();
    private DatabaseReference reference = FirebaseDatabase.getInstance()
            .getReference().getRoot();
    private String channel_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);
        createChBtn = (Button) findViewById(R.id.createChannel);

        arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_view, channel);

        listView.setAdapter(arrayAdapter);

        createChBtn.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View view) {
                createChannel();
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<String>();
                Iterator i = dataSnapshot.getChildren().iterator();

                while (i.hasNext()) {
                    set.add(((DataSnapshot) i.next()).getKey());
                }

                channel.clear();
                channel.addAll(set);

                arrayAdapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), CommunicationActivity.class);
                intent.putExtra("Channel Name", ((TextView) view).getText().toString());
                intent.putExtra("Entrance Time", System.currentTimeMillis());
                startActivity(intent);
            }
        });
    }


    private void createChannel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("채널명을 입력하세요");

        final EditText builder_input = new EditText(this);

        builder.setView(builder_input);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                channel_name = builder_input.getText().toString();
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(channel_name, "");
                reference.updateChildren(map);
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                //createUserName();
            }
        });

        builder.show();
    }
}
