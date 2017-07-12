package com.example.kimminyoung.stt_test2;
import java.util.ArrayList;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

// 음성 인식
public class RecognizeSpeechEx extends Activity
        implements View.OnClickListener {
    private static final int REQUEST_CODE=0;
    private EditText editText; // 에디트 텍스트
    private Button button; // 버튼

    // 어플리케이션의 초기화
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 레이아웃의 생성
        LinearLayout layout=new LinearLayout(this);
        layout.setBackgroundColor(Color.rgb(255,255,255));
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        // 에디트 텍스트의 생성
        editText=new EditText(this);
        editText.setText("",
                EditText.BufferType.NORMAL);
        setLLParams(editText,
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(editText);

        // 버튼의 생성
        button=new Button(this);
        button.setText("음성인식");
        button.setOnClickListener(this);
        setLLParams(button);
        layout.addView(button);
    }

    // 버튼 클릭 이벤트의 처리
    public void onClick(View v) {
        try {
            // 음성 인식의 실행 (1)
            Intent intent=new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "RecognizeSpeechEx");
            startActivityForResult(intent,REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(RecognizeSpeechEx.this,
                    "ActivityNotFoundException",Toast.LENGTH_LONG).show();
        }
    }

    // 액티비티 종료 시 불린다.
    protected void onActivityResult(int requestCode,
                                    int resultCode,Intent data) {
        // 음성 인식 결과의 취득 (2)
        if (requestCode==REQUEST_CODE && resultCode==RESULT_OK) {
            String str="";
            ArrayList<String> results=
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);
            for (int i=0;i<results.size();i++) {
                str+=results.get(i)+" ";
            }
            editText.setText(str);
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    // 리니어 레이아웃의 파라미터 지정
    private static void setLLParams(View view) {
        view.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    // 리니어 레이아웃의 파라미터 지정
    private static void setLLParams(View view,int w,int h) {
        view.setLayoutParams(new LinearLayout.LayoutParams(w,h));
    }
}