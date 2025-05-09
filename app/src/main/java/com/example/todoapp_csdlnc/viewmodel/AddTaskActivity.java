package com.example.todoapp_csdlnc.viewmodel;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.todoapp_csdlnc.R;
import com.example.todoapp_csdlnc.model.RelatedPerson;
import com.example.todoapp_csdlnc.model.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {
    private EditText taskName, taskDescription;
    private TextView taskDeadline, taskRelatedPersons;
    private Button addRelatedPersonButton, saveButton;
    private final ArrayList<RelatedPerson> relatedPersonList = new ArrayList<>();
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showContactsDialog();
                } else {
                    Toast.makeText(this, "Bạn cần cấp quyền để truy cập danh bạ", Toast.LENGTH_SHORT).show();
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
        Calendar calendar = Calendar.getInstance();
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        // Deadline Button
        findViewById(R.id.task_deadline_button).setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddTaskActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                AddTaskActivity.this,
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

            if (!name.isEmpty() && !deadline.isEmpty()) {
                // Lấy userId từ SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String userId = prefs.getString("uid", null);
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("Name", name);
                taskMap.put("Description", description);
                taskMap.put("Deadline", deadline);
                taskMap.put("isCompleted", false);
                taskMap.put("userId", userId);

                // Convert RelatedPerson to Map
                ArrayList<Map<String, String>> relatedPersonsMap = new ArrayList<>();
                for (RelatedPerson person : relatedPersonList) {
                    Map<String, String> personMap = new HashMap<>();
                    personMap.put("id", person.getId());
                    personMap.put("name", person.getName());
                    personMap.put("phoneNumber", person.getPhoneNumber());
                    relatedPersonsMap.add(personMap);
                }
                taskMap.put("relatedPerson", relatedPersonsMap);

                FirebaseFirestore.getInstance()
                        .collection("Task")
                        .add(taskMap)
                        .addOnSuccessListener(documentReference -> {
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi khi lưu Task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Tên và deadline không được để trống", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showContactsDialog() {
        ArrayList<String> contactsList = new ArrayList<>();
        HashMap<String, String> contactIdMap = new HashMap<>();

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor == null) {
            Log.e("DEBUG", "Cursor is null! Không truy cập được danh bạ");
            Toast.makeText(this, "Không thể truy cập danh bạ", Toast.LENGTH_SHORT).show();
            return;
        }

        int nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int phoneIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int idIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);

        while (cursor.moveToNext()) {
            String id = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);
            String phoneNumber = cursor.getString(phoneIndex);

            String display = name + " (" + phoneNumber + ")";
            if (!contactsList.contains(display)) {
                contactsList.add(display);
                contactIdMap.put(display, id + "||" + name + "||" + phoneNumber);
            }
        }
        cursor.close();

        if (contactsList.isEmpty()) {
            Log.w("DEBUG", "Danh bạ rỗng hoặc không lấy được dữ liệu hợp lệ");
            Toast.makeText(this, "Không có liên hệ nào", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_contact, null);
        ListView listView = dialogView.findViewById(R.id.contact_list_view);
        LinearLayout selectedContainer = dialogView.findViewById(R.id.selected_contacts_container);
        selectedContainer.removeAllViews();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, contactsList);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Chọn liên hệ")
                .setNegativeButton("Đóng", null)
                .create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = contactsList.get(position).trim();

            if (!contactIdMap.containsKey(selected)) {
                Toast.makeText(this, "Lỗi dữ liệu liên hệ", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] info = contactIdMap.get(selected).split("\\|\\|");

            if (info.length < 3 || info[0] == null || info[0].trim().isEmpty()) {
                Toast.makeText(this, "Thông tin liên hệ không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            String personId = info[0].trim();
            String name = info[1].trim();
            String phone = info[2].trim();

            boolean exists = false;
            for (RelatedPerson person : relatedPersonList) {
                if (personId.equals(person.getId())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                RelatedPerson newPerson = new RelatedPerson(personId, name, phone);
                relatedPersonList.add(newPerson);
                updateSelectedContactsView(selectedContainer, dialog);
            } else {
                Toast.makeText(this, "Đã chọn người này", Toast.LENGTH_SHORT).show();
            }
        });
        updateSelectedContactsView(selectedContainer, dialog);
        dialog.show();
    }

    private void updateSelectedContactsView(LinearLayout container, AlertDialog dialog) {
        container.removeAllViews();

        for (RelatedPerson person : relatedPersonList) {
            View personView = getLayoutInflater().inflate(R.layout.seleted_person_item, container, false);

            TextView nameText = personView.findViewById(R.id.selected_contact_name);
            Button removeButton = personView.findViewById(R.id.remove_contact_button);

            nameText.setText(person.getName());

            removeButton.setOnClickListener(v -> {
                relatedPersonList.remove(person);
                updateSelectedContactsView(container, dialog);

                // Cập nhật hiển thị TextView
                List<String> names = new ArrayList<>();
                for (RelatedPerson p : relatedPersonList) {
                    names.add(p.getName());
                }
                taskRelatedPersons.setText(String.join(", ", names));
            });

            container.addView(personView);
        }

        // Cập nhật hiển thị bên ngoài dialog nếu muốn
        List<String> names = new ArrayList<>();
        for (RelatedPerson p : relatedPersonList) {
            names.add(p.getName());
        }
        taskRelatedPersons.setText(String.join(", ", names));
    }
}