package com.example.cau;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.editText).requestFocus();
//        new Handler().postDelayed(()->{
//            ((TyperHintEditText)findViewById(R.id.editText)).animateText("thử một cái gì đó xem sao");
//        },1000L);
    }
}