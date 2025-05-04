package com.example.todoapp_csdlnc.viewmodel;

import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

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
        EditText passwordEditText = findViewById(R.id.editTextTextPassword);
        EditText confirmPasswordEditText = findViewById(R.id.editTextTextPassword2);

        // Gắn chức năng hiện/ẩn mật khẩu
        setupPasswordToggle(passwordEditText);
        setupPasswordToggle(confirmPasswordEditText);
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
