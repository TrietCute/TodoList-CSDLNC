package com.example.todoapp_csdlnc.viewmodel;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp_csdlnc.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView textViewDate;  // TextView hiển thị ngày Due Date
    private TextView textView12;    // TextView khác (bên cạnh textViewDate)
    private TextView textViewTime;  // TextView "Time and Remind"
    private TextView textViewNotes; // TextView "Notes"

    private TextView textViewNoteContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Khởi tạo các TextView
        textViewDate = findViewById(R.id.textViewDate);
        textView12 = findViewById(R.id.textView12);
        textViewTime = findViewById(R.id.textView13);
        textViewNotes = findViewById(R.id.textView16);
        textViewNoteContent = findViewById(R.id.textViewNoteContent);


        // Lắng nghe sự kiện click vào các TextView
        View.OnClickListener dateClickListener = v -> openDatePicker();
        textViewDate.setOnClickListener(dateClickListener);
        textView12.setOnClickListener(dateClickListener);

        // Lắng nghe sự kiện click vào "Time and Remind"
        textViewTime.setOnClickListener(v -> openTimePickerDialog());

        // Lắng nghe sự kiện click vào "Notes"
        textViewNotes.setOnClickListener(v -> openNoteDialog());
    }

    private void openDatePicker() {
        // Mở DatePickerDialog (giống như mã trước đây của bạn)
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    String formattedDate = sdf.format(selectedDate.getTime());

                    textViewDate.setText(formattedDate);
                    Toast.makeText(this, "Date selected: " + formattedDate, Toast.LENGTH_SHORT).show();
                }, year, month, day);

        datePickerDialog.show();
    }

    private void openTimePickerDialog() {
        // Lấy thời gian hiện tại
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Mở TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    // Định dạng thời gian
                    String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);

                    // Hiển thị thời gian đã chọn trên textViewTime
                    textViewTime.setText(formattedTime);

                    // Mở AlertDialog để chọn nhắc nhở
                    openReminderOptions(selectedHour, selectedMinute);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void openReminderOptions(int hour, int minute) {
        // Các lựa chọn nhắc nhở (hàng ngày, hàng tuần)
        String[] reminderOptions = {"Daily", "Weekly", "No"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Reminder")
                .setItems(reminderOptions, (dialog, which) -> {
                    String selectedOption = reminderOptions[which];
                    Toast.makeText(this, "Reminder set to: " + selectedOption, Toast.LENGTH_SHORT).show();

                    // Bạn có thể lưu lại thời gian và nhắc nhở ở đây nếu cần
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Hiển thị dialog
        builder.show();
    }

    private void openNoteDialog() {
        // Tạo một AlertDialog với EditText cho người dùng nhập ghi chú
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_note, null);

        EditText editTextNote = dialogView.findViewById(R.id.editNote);

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Note")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String note = editTextNote.getText().toString();
                    textViewNoteContent.setText(note); // Ghi vào TextView mới
                    Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Hiển thị dialog
        builder.show();
    }
}
