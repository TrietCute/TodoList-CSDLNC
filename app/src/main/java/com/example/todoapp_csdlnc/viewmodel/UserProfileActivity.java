package com.example.todoapp_csdlnc.viewmodel;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.todoapp_csdlnc.R;

public class UserProfileActivity extends AppCompatActivity {
    //Hình ảnh
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageButton imageButton;

    private TextView nameProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        nameProfile = findViewById(R.id.nameProfile);

        //Đổi hình ảnh
        imageButton = findViewById(R.id.imageButton);
        ImageButton editIcon = findViewById(R.id.editIcon);
        FrameLayout imageFrame = findViewById(R.id.frameLayout2);

// Khi bấm vào ảnh hoặc nút chỉnh sửa
        View.OnClickListener openImagePicker = v -> openImageChooser();

        imageFrame.setOnClickListener(openImagePicker);
        editIcon.setOnClickListener(openImagePicker);


        // Đổi tên tài khoản
        TextView changeAccountName = findViewById(R.id.changeAccountName);
        changeAccountName.setOnClickListener(v -> showChangeNameDialog());

        // Đổi mật khẩu
        TextView changePassword = findViewById(R.id.changeAccountPassword);
        changePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Đăng xuất
        TextView logOut = findViewById(R.id.logOut);
        logOut.setOnClickListener(v -> showLogoutDialog());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void showChangeNameDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_name, null);
        EditText editTextName = dialogView.findViewById(R.id.editTextName);

        new AlertDialog.Builder(this)
                .setTitle("Change Account Name")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = editTextName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        nameProfile.setText(newName);
                        Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        EditText oldPassword = dialogView.findViewById(R.id.oldPassword);
        EditText newPassword = dialogView.findViewById(R.id.newPassword);

        setupPasswordToggle(oldPassword);
        setupPasswordToggle(newPassword);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String oldPass = oldPassword.getText().toString();
                    String newPass = newPassword.getText().toString();

                    if (oldPass.equals("123456")) { // giả lập mật khẩu cũ
                        if (!newPass.isEmpty()) {
                            // TODO: lưu mật khẩu mới
                            Toast.makeText(this, "Password updated!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "New password cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupPasswordToggle(EditText editText) {
        final Drawable visibilityOn = ContextCompat.getDrawable(this, R.drawable.baseline_visibility_24);
        final Drawable visibilityOff = ContextCompat.getDrawable(this, R.drawable.baseline_visibility_off_24);

        final Drawable[] drawables = editText.getCompoundDrawables();
        editText.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], visibilityOff, drawables[3]);

        final boolean[] isVisible = {false};

        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    isVisible[0] = !isVisible[0];

                    if (isVisible[0]) {
                        editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        editText.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], visibilityOn, drawables[3]);
                    } else {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editText.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], visibilityOff, drawables[3]);
                    }

                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Do you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Thực hiện log out (ví dụ xóa thông tin đăng nhập)
                    Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
