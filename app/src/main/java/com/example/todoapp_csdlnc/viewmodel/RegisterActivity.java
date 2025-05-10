package com.example.todoapp_csdlnc.viewmodel;

import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp_csdlnc.R;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Xử lý nút quay lại
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Lấy các EditText mật khẩu
        Button nextButton = findViewById(R.id.btnNext);
        EditText emailEditText = findViewById(R.id.txtEmailRegister);
        EditText passwordEditText = findViewById(R.id.editTextTextPassword);
        EditText confirmPasswordEditText = findViewById(R.id.editTextTextPassword2);

        // Gắn chức năng hiện/ẩn mật khẩu
        setupPasswordToggle(passwordEditText);
        setupPasswordToggle(confirmPasswordEditText);

        nextButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(RegisterActivity.this, RegisterInfoActivity.class);
            intent.putExtra("Email", email);
            intent.putExtra("Password", password);
            startActivity(intent);
        });
    }

    // Hàm dùng chung để gắn tính năng hiện/ẩn mật khẩu
    private void setupPasswordToggle(EditText editText) {
        final boolean[] isVisible = {false};

        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);

        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    isVisible[0] = !isVisible[0];

                    // Cập nhật kiểu hiển thị mật khẩu
                    if (isVisible[0]) {
                        editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_24, 0);
                    } else {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);
                    }

                    // Đặt lại vị trí con trỏ
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }
}
