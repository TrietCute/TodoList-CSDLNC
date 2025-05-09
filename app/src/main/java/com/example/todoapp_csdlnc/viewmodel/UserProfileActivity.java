package com.example.todoapp_csdlnc.viewmodel;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.todoapp_csdlnc.R;
import com.example.todoapp_csdlnc.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    //Hình ảnh
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageButton imageButton;
    private TextView nameProfile;
    private FirebaseFirestore db;
    private Uri imageUri;
    FirebaseStorage firebaseStorage;
    StorageReference storageRef;
    SharedPreferences prefs;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        nameProfile = findViewById(R.id.nameProfile);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        uid = prefs.getString("uid", null);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        if (uid == null) {
            // Người dùng chưa đăng nhập → chuyển về login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Khởi tạo Firebase
        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();

        db.collection("User").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("Name");
                        nameProfile.setText(name);
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Initialize back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        //Đổi hình ảnh
        imageButton = findViewById(R.id.imageButton);
        ImageButton editIcon = findViewById(R.id.editIcon);
        FrameLayout imageFrame = findViewById(R.id.frameLayout2);

        // Khi bấm vào ảnh hoặc nút chỉnh sửa
        View.OnClickListener openImagePicker = v -> openImageChooser();
        imageFrame.setOnClickListener(openImagePicker);
        editIcon.setOnClickListener(openImagePicker);

        loadProfileImage();
        
        // Đổi tên tài khoản
        TextView Account = findViewById(R.id.Account);
        Account.setOnClickListener(v -> showAccountInfoDialog(new User()));

        // Đổi mật khẩu
        TextView changePassword = findViewById(R.id.changeAccountPassword);
        changePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Đăng xuất
        TextView logOut = findViewById(R.id.logOut);
        logOut.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadProfileImage() {
        db.collection("User").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("avatarUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).circleCrop().into(imageButton);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Lỗi khi tải ảnh", e));
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child("profile_images/" + uid + ".jpg");

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();

                                // Lưu URL vào Firestore
                                db.collection("User").document(uid)
                                        .update("avatarUrl", downloadUrl)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                                            Glide.with(this).load(downloadUrl).circleCrop().into(imageButton);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Không thể cập nhật URL", e);
                                            Toast.makeText(this, "Lỗi lưu URL ảnh", Toast.LENGTH_SHORT).show();
                                        });
                            }))
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Upload ảnh thất bại", e);
                        Toast.makeText(this, "Tải ảnh lên thất bại", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showAccountInfoDialog(User user) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_info, null);

        TextView textId = dialogView.findViewById(R.id.textId);
        TextView textUserName = dialogView.findViewById(R.id.textUserName);
        TextView textEmail = dialogView.findViewById(R.id.textEmail);
        TextView textPhone = dialogView.findViewById(R.id.textPhoneNumber);
        TextView textBio = dialogView.findViewById(R.id.textBio);
        TextView textCreatedAt = dialogView.findViewById(R.id.textCreatedAt);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);

        if (uid == null) {
            // Người dùng chưa đăng nhập → chuyển về login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db.collection("User").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userId = documentSnapshot.getId();
                        String name = documentSnapshot.getString("Name");
                        String email = documentSnapshot.getString("Email");
                        String phone = documentSnapshot.getString("Phone");
                        String bio = documentSnapshot.getString("Bio");
                        Timestamp createdAt = documentSnapshot.getTimestamp("createdAt");

                        // Gán dữ liệu vào các TextView
                        textId.setText(userId);
                        textUserName.setText(name);
                        textEmail.setText(email);
                        textPhone.setText(phone);
                        textBio.setText(bio);
                        if (createdAt != null) {
                            Date date = createdAt.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            String createdAtFormatted = sdf.format(date);
                            textCreatedAt.setText(createdAtFormatted);
                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Account Information")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dlg -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                String updatedName = textUserName.getText().toString();
                String updatedEmail = textEmail.getText().toString();
                String updatedPhone = textPhone.getText().toString();
                String updatedBio = textBio.getText().toString();

                Map<String, Object> updates = new HashMap<>();
                updates.put("Name", updatedName);
                updates.put("Email", updatedEmail);
                updates.put("Phone", updatedPhone);
                updates.put("Bio", updatedBio);

                db.collection("User").document(uid)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "User info updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error updating user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        });

        dialog.show();
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

                    if (newPass.isEmpty()) {
                        Toast.makeText(this, "New password cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ Lấy uid từ SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    String uid = prefs.getString("uid", null);

                    if (uid == null) {
                        Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("User").document(uid).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String currentPassword = documentSnapshot.getString("Password");

                                    if (currentPassword != null && currentPassword.equals(oldPass)) {
                                        // ✅ Cập nhật mật khẩu mới
                                        db.collection("User").document(uid)
                                                .update("Password", newPass)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(this, "Password updated!", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Error updating password", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error fetching user info", Toast.LENGTH_SHORT).show();
                            });
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
