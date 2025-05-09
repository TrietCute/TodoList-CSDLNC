package com.example.todoapp_csdlnc.viewmodel;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp_csdlnc.R;
import com.example.todoapp_csdlnc.adapter.TaskAdapter;
import com.example.todoapp_csdlnc.model.RelatedPerson;
import com.example.todoapp_csdlnc.model.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements TaskAdapter.OnTaskDeleteListener {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;
    private TextView notificationText, instructionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Khởi tạo RecyclerView
        taskList = new ArrayList<>();
        recyclerView = findViewById(R.id.task_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, this);
        recyclerView.setAdapter(taskAdapter);

        loadTasks(); // Lần đầu load khi mở HomeActivity

        // Khởi tạo TextView
        notificationText = findViewById(R.id.notification);
        instructionText = findViewById(R.id.instruction);

        // Cập nhật trạng thái hiển thị ban đầu
        updateTextVisibility();

        // Thiết lập FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Thiết lập Sort Icon
        ImageView sortIcon = findViewById(R.id.sort_icon);
        sortIcon.setOnClickListener(v -> showSortDialog());

        ShapeableImageView avatar = findViewById(R.id.avatar);
        avatar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); // Tự động load lại khi quay về từ AddTaskActivity
    }

    private void loadTasks() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("uid", null);

        if (userId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Task")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        taskList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String taskId = doc.getId();
                            String name = doc.getString("Name");
                            String deadline = doc.getString("Deadline");
                            String description = doc.getString("Description");
                            boolean isCompleted = doc.getBoolean("isCompleted") != null ? doc.getBoolean("isCompleted") : false;
                            String uid = doc.getString("userId");

                            Task task = new Task();
                            task.setId(taskId);
                            task.setName(name);
                            task.setDeadline(deadline);
                            task.setDescription(description);
                            task.setCompleted(isCompleted);
                            task.setUserId(uid);

                            taskList.add(task);
                        }

                        taskAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load tasks", Toast.LENGTH_SHORT).show());
        }
    }

    private void showSortDialog() {
        String[] sortOptions = {"Sort by Earliest Deadline", "Sort by Latest Deadline"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort Tasks")
                .setSingleChoiceItems(sortOptions, -1, (dialog, which) -> {
                    if (which == 0) {
                        sortTasksByDeadline(true); // Nearest first
                    } else {
                        sortTasksByDeadline(false); // Farthest first
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sortTasksByDeadline(boolean nearestFirst) {
        Collections.sort(taskList, new Comparator<Task>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            @Override
            public int compare(Task task1, Task task2) {
                try {
                    Date date1 = sdf.parse(task1.getDeadline());
                    Date date2 = sdf.parse(task2.getDeadline());
                    if (date1 == null || date2 == null) return 0;
                    return nearestFirst ? date1.compareTo(date2) : date2.compareTo(date1);
                } catch (ParseException e) {
                    return 0; // Keep order unchanged for invalid dates
                }
            }
        });
        taskAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Task newTask = (Task) data.getSerializableExtra("new_task");
            if (newTask != null) {
                taskList.add(newTask);
                taskAdapter.notifyItemInserted(taskList.size() - 1);
                updateTextVisibility();
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Task updatedTask = (Task) data.getSerializableExtra("updated_task");
            int position = data.getIntExtra("position", -1);
            if (updatedTask != null && position != -1) {
                taskList.set(position, updatedTask);
                taskAdapter.notifyItemChanged(position);
                updateTextVisibility();
            }
        }
    }

    @Override
    public void onTaskDeleted() {
        updateTextVisibility();
    }

    private void updateTextVisibility() {
        if (taskList.isEmpty()) {
            notificationText.setVisibility(View.VISIBLE);
            instructionText.setVisibility(View.VISIBLE);
        } else {
            notificationText.setVisibility(View.GONE);
            instructionText.setVisibility(View.GONE);
        }
    }
}