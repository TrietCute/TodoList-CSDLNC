package com.example.todoapp_csdlnc.adapter;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todoapp_csdlnc.R;
import com.example.todoapp_csdlnc.model.Task;
import com.example.todoapp_csdlnc.viewmodel.UpdateTaskActivity;
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
        holder.taskCheckbox.setChecked(false); // Mặc định chưa chọn

        // Handle edit button click
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), UpdateTaskActivity.class);
            intent.putExtra("task", task);
            intent.putExtra("position", position);
            holder.itemView.getContext().startActivity(intent);
        });

        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Xóa Task")
                    .setMessage("Bạn có chắc muốn xóa task này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        taskList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, taskList.size());
                        if (deleteListener != null) {
                            deleteListener.onTaskDeleted();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
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