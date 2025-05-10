package com.example.todoapp_csdlnc.adapter;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp_csdlnc.R;
import com.example.todoapp_csdlnc.model.Task;
import com.example.todoapp_csdlnc.viewmodel.TaskDetailActivity;
import com.example.todoapp_csdlnc.viewmodel.UpdateTaskActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private ArrayList<Task> taskList;
    private OnTaskDeleteListener deleteListener;

    public interface OnTaskDeleteListener {
        void onTaskDeleted();
    }

    public TaskAdapter(ArrayList<Task> taskList, OnTaskDeleteListener deleteListener) {
        this.taskList = taskList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskTitle.setText(task.getName());
        holder.taskTime.setText(task.getDeadline());

        // Handle task item click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), TaskDetailActivity.class);
            intent.putExtra("id", task.getId());
            holder.itemView.getContext().startActivity(intent);
        });

        // Handle edit button click
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), UpdateTaskActivity.class);
            intent.putExtra("id", task.getId());
            holder.itemView.getContext().startActivity(intent);
        });

        // Handle delete
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Xóa Task")
                    .setMessage("Bạn có chắc muốn xóa task này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        String taskId = task.getId();
                        FirebaseFirestore.getInstance().collection("Task")
                                .document(taskId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    taskList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, taskList.size());
                                    Toast.makeText(holder.itemView.getContext(), "Đã xóa task", Toast.LENGTH_SHORT).show();
                                    if (deleteListener != null) deleteListener.onTaskDeleted();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(holder.itemView.getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Remove listener trước khi setChecked để tránh callback không mong muốn
        holder.taskCheckbox.setOnCheckedChangeListener(null);
        holder.taskCheckbox.setChecked(task.isCompleted());

        // Gắn listener mới
        holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FirebaseFirestore.getInstance()
                    .collection("Task")
                    .document(task.getId())
                    .update("isCompleted", isChecked)
                    .addOnSuccessListener(aVoid -> {
                        task.setCompleted(isChecked);
                        Toast.makeText(holder.itemView.getContext(), "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        holder.taskCheckbox.setChecked(task.isCompleted());
                    });
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskTime;
        CheckBox taskCheckbox;
        ImageButton editButton, deleteButton;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.task_title);
            taskTime = itemView.findViewById(R.id.task_time);
            taskCheckbox = itemView.findViewById(R.id.task_checkbox);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}