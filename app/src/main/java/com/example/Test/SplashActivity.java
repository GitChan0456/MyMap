package com.example.Test; // 본인 프로젝트의 패키지명

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // 1단계에서 만든 레이아웃 표시

        // 2초 후에 MainActivity로 이동하는 로직
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // MainActivity로 이동하기 위한 Intent 생성
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);

                // SplashActivity를 종료하여 뒤로 가기 버튼을 눌렀을 때 다시 나타나지 않도록 함
                finish();
            }
        }, 1000); // 1000 밀리초 = 1초
    }
}