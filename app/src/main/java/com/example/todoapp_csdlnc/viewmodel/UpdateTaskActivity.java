package com.example.todoapp_csdlnc.viewmodel;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.todoapp_csdlnc.R;
import com.example.todoapp_csdlnc.model.Task;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class UpdateTaskActivity extends AppCompatActivity {
    private EditText taskName, taskDescription;
    private TextView taskDeadline, taskRelatedPersons;
    private Button addRelatedPersonButton, saveButton;
    private ArrayList<String> selectedPersons = new ArrayList<>();
    private Task task;
    private int taskPosition;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showContactsDialog();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskName = findViewById(R.id.task_name);
        taskDescription = findViewById(R.id.task_description);
        taskDeadline = findViewById(R.id.task_deadline);
        taskRelatedPersons = findViewById(R.id.task_related_persons);
        addRelatedPersonButton = findViewById(R.id.add_related_person_button);
        saveButton = findViewById(R.id.save_task_fab);
        saveButton.setText("Update");

        // Retrieve task and position from Intent
        Intent intent = getIntent();
        task = (Task) intent.getSerializableExtra("task");
        taskPosition = intent.getIntExtra("position", -1);

        // Populate fields with existing task data
        if (task != null) {
            taskName.setText(task.getName());
            taskDescription.setText(task.getDescription());
            taskDeadline.setText(task.getDeadline());
            String relatedPersons = task.getRelatedPersons();
            if (relatedPersons != null && !relatedPersons.isEmpty()) {
                String[] personsArray = relatedPersons.split(", ");
                selectedPersons = new ArrayList<>(java.util.Arrays.asList(personsArray));
                taskRelatedPersons.setText(relatedPersons);
            }
        }

        Calendar calendar = Calendar.getInstance();

        // Deadline Button
        findViewById(R.id.task_deadline_button).setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    UpdateTaskActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                UpdateTaskActivity.this,
                                (timeView, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                    taskDeadline.setText(sdf.format(calendar.getTime()));
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                        );
                        timePickerDialog.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Add Related Person Button
        addRelatedPersonButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                showContactsDialog();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
            }
        });

        // Save Button
        saveButton.setOnClickListener(v -> {
            String name = taskName.getText().toString().trim();
            String description = taskDescription.getText().toString().trim();
            String deadline = taskDeadline.getText().toString().trim();
            String relatedPersons = taskRelatedPersons.getText().toString().trim();

            if (!name.isEmpty() && !deadline.isEmpty()) {
                Task updatedTask = new Task(name, description, deadline, relatedPersons);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updated_task", updatedTask);
                resultIntent.putExtra("position", taskPosition);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void showContactsDialog() {
        ArrayList<String> contactsList = new ArrayList<>();
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                contactsList.add(name + " (" + phoneNumber + ")");
            }
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, contactsList);
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chọn Liên Hệ")
                .setView(listView)
                .setNegativeButton("Hủy", null)
                .create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedContact = contactsList.get(position);
            String contactName = selectedContact.split(" \\(")[0];
            if (!selectedPersons.contains(contactName)) {
                selectedPersons.add(contactName);
                taskRelatedPersons.setText(String.join(", ", selectedPersons));
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}