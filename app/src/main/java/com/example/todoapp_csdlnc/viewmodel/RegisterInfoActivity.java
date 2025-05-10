package com.example.todoapp_csdlnc.viewmodel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp_csdlnc.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class RegisterInfoActivity extends AppCompatActivity {
    private Uri selectedImageUri;
    private ImageView avatarImageView;
    private ImageView chooseAvatarImageView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_register);

        EditText nameEditText = findViewById(R.id.txtName);
        EditText phoneEditText = findViewById(R.id.txtPhoneNumber);
        EditText bioEditText = findViewById(R.id.txtBio);
        chooseAvatarImageView = findViewById(R.id.editIcon);
        avatarImageView = findViewById(R.id.imageButton);
        Button doneButton = findViewById(R.id.btnDone);

        chooseAvatarImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1001);
        });

        doneButton.setOnClickListener(v -> {
            String email = getIntent().getStringExtra("Email");
            String password = getIntent().getStringExtra("Password");
            String name = nameEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            String bio = bioEditText.getText().toString();
            Timestamp createdAt = Timestamp.now();

            // Bước 1: Tạo document rỗng để lấy userId (documentId)
            DocumentReference newUserRef = db.collection("User").document(); // <-- tự sinh id
            String userId = newUserRef.getId();

            if (selectedImageUri != null) {
                // Bước 2: Upload ảnh với tên là user_profiles/userId.jpg
                StorageReference ref = storage.getReference().child("profile_images/" + userId + ".jpg");
                ref.putFile(selectedImageUri)
                        .continueWithTask(task -> ref.getDownloadUrl())
                        .addOnSuccessListener(uri -> {
                            String avatarUrl = uri.toString();

                            // Bước 3: Lưu thông tin user với avatarUrl
                            saveUserToFirestore(newUserRef, email, password, name, phone, bio, avatarUrl, createdAt);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            } else {
                // Không có ảnh, lưu luôn
                saveUserToFirestore(newUserRef, email, password, name, phone, bio, "", createdAt);
            }
        });
    }

    private void saveUserToFirestore(DocumentReference userRef, String email, String password, String name, String phone, String bio, String avatarUrl, Timestamp createdAt) {
        Map<String, Object> user = new HashMap<>();
        user.put("Email", email);
        user.put("Password", password);
        user.put("Name", name);
        user.put("Phone", phone);
        user.put("Bio", bio);
        user.put("avatarUrl", avatarUrl);
        user.put("createdAt", createdAt);

        userRef.set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterInfoActivity.this, LoginActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));

                    // Chuyển về LoginActivity
                    Intent intent = new Intent(RegisterInfoActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            avatarImageView.setImageURI(selectedImageUri);
        }
    }
}
