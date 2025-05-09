package com.example.todoapp_csdlnc.viewmodel;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp_csdlnc.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TaskDetailActivity extends AppCompatActivity {
private TextView textViewName;
    private TextView textViewDate;
    private TextView textViewStatus;
    private TextView textViewRelatedPerson;
    private TextView textViewNoteContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Khởi tạo các TextView
        textViewName = findViewById(R.id.nameTask);
        textViewDate = findViewById(R.id.textViewDate);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewRelatedPerson = findViewById(R.id.textViewRelatedPerson);
        textViewNoteContent = findViewById(R.id.textViewNoteContent);


        // Initialize back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Lấy taskId từ Intent
        String taskId = getIntent().getStringExtra("id");
        if (taskId != null) {
            loadTaskDetail(taskId);
        } else {
            Toast.makeText(this, "Không tìm thấy ID công việc", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTaskDetail(String taskId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference taskRef = db.collection("Task").document(taskId);

        taskRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("Name");
                String deadline = documentSnapshot.getString("Deadline");
                String description = documentSnapshot.getString("Description");
                Boolean isCompleted = documentSnapshot.getBoolean("isCompleted");

                textViewName.setText(name != null ? name : "Không có tên");
                textViewDate.setText(deadline != null ? deadline : "Không có hạn chót");
                textViewNoteContent.setText(description != null ? description : "Không có ghi chú");
                textViewStatus.setText(isCompleted != null && isCompleted ? "Đã hoàn thành" : "Chưa hoàn thành");

                // Hiển thị tên các người liên quan
                List<Map<String, Object>> relatedList = (List<Map<String, Object>>) documentSnapshot.get("relatedPerson");
                if (relatedList != null && !relatedList.isEmpty()) {
                    StringBuilder namesBuilder = new StringBuilder();
                    for (Map<String, Object> person : relatedList) {
                        String personName = (String) person.get("name");
                        if (personName != null) {
                            namesBuilder.append("- ").append(personName).append("\n");
                        }
                    }

                    textViewRelatedPerson.setText(namesBuilder.toString().trim());
                }
            } else {
                Toast.makeText(this, "Không tìm thấy công việc", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
