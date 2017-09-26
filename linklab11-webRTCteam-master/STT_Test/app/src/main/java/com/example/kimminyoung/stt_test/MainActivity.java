package com.example.kimminyoung.stt_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClick(View view) {
        if (view.getId() == R.id.btnCh1) {
            Intent intent = new Intent(this, ChannelActivity.class);
            startActivity(intent);
        }
    }
}
