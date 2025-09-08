package com.example.Test;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class SignupActivity extends AppCompatActivity {

    // UI 요소 변수 선언
    private TextInputEditText etId, etPassword, etPasswordConfirm, etName, etEmail, etNickname;
    private Button btnCheckId, btnSignupSubmit;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // UI 요소 초기화
        etId = findViewById(R.id.et_signup_id);
        etPassword = findViewById(R.id.et_signup_password);
        etPasswordConfirm = findViewById(R.id.et_signup_password_confirm);
        etName = findViewById(R.id.et_signup_name);
        etEmail = findViewById(R.id.et_signup_email);
        etNickname = findViewById(R.id.et_signup_nickname);
        btnCheckId = findViewById(R.id.btn_check_id);
        btnSignupSubmit = findViewById(R.id.btn_signup_submit);
        btnBack = findViewById(R.id.btn_back);

        // 아이디 중복 확인 버튼 클릭 리스너
        btnCheckId.setOnClickListener(v -> {
            Toast.makeText(this, "아이디 중복 확인", Toast.LENGTH_SHORT).show();
            // 여기에 실제 중복 확인 로직 구현 예정
        });

        // 가입하기 버튼 클릭 리스너
        btnSignupSubmit.setOnClickListener(v -> {
            Toast.makeText(this, "가입하기 버튼 클릭됨", Toast.LENGTH_SHORT).show();
            // 여기에 실제 회원가입 로직 구현 예정
        });
        // 뒤로가기 리스너
        btnBack.setOnClickListener(v -> {
            finish(); // 현재 액티비티를 종료하고 이전 화면으로 돌아갑니다.
        });

    }
}