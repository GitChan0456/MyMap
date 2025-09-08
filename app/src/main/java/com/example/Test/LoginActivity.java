package com.example.Test;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

public class LoginActivity extends AppCompatActivity {

    private EditText etId, etPassword;
    private Button btnLogin;
    private TextView tvFindCredentials, tvSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI 요소 초기화
        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvFindCredentials = findViewById(R.id.tv_find_credentials);
        tvSignup = findViewById(R.id.tv_signup);

        // 로그인 버튼 클릭 리스너
        btnLogin.setOnClickListener(v -> {
            // 실제 앱에서는 여기서 아이디와 비밀번호가 유효한지 서버와 통신하여 확인하는 로직이 필요합니다.
            // 지금은 버튼을 누르면 무조건 로그인 성공으로 간주하고 화면을 이동시킵니다.

            // 1. MainActivity로 이동하기 위한 "통행증" (Intent)을 생성합니다.
            //    (출발지: LoginActivity.this, 도착지: MainActivity.class)
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

            // 2. 생성한 통행증으로 새로운 액티비티를 시작합니다.
            startActivity(intent);

            // 3. 현재 로그인 화면(LoginActivity)을 종료합니다.
            //    이렇게 해야 MainActivity에서 뒤로가기 버튼을 눌렀을 때 로그인 화면으로 다시 돌아오지 않고 앱이 종료됩니다.
            finish();
        });

        // 회원가입 클릭 리스너
        tvSignup.setOnClickListener(v -> {
            // 기존 Toast 메시지 대신 Intent를 사용하여 화면 이동
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });



        // 아이디/비밀번호 찾기 클릭 리스너
        tvFindCredentials.setOnClickListener(v -> {
            Toast.makeText(this, "아이디/비밀번호 찾기 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
            // 여기에 실제 찾기 화면으로 이동하는 로직 구현 예정
        });
    }
}