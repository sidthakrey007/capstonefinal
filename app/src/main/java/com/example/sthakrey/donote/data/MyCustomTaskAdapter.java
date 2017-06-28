package com.example.sthakrey.donote.data;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.sthakrey.donote.R;

import java.util.List;

/**
 * Created by sthakrey on 6/24/2017.
 */
public class MyCustomTaskAdapter extends
        RecyclerView.Adapter<MyCustomTaskAdapter.TaskHolder> {

    private List<Task> taskList;

    /**
     * View holder class
     */
    public class TaskHolder extends RecyclerView.ViewHolder {
        public EditText task;
        public CheckBox checkbox;
        public boolean isChecked;
        public ImageButton deleteButton;


        public TaskHolder(final View itemView) {

            super(itemView);
            checkbox = (CheckBox) itemView.findViewById(R.id.mycheckbox);
            task = (EditText) itemView.findViewById(R.id.task_text);

            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_task_button);
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                    int position = getAdapterPosition();
                    taskList.get(position).setTask(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            };

            task.addTextChangedListener(textWatcher);


        }

    }

    public MyCustomTaskAdapter(List<Task> countryList) {
        this.taskList = countryList;
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {
        Task c = taskList.get(position);
        holder.task.setText(c.getTask());
        if (holder.isChecked)
            holder.checkbox.setChecked(true);
        else
            holder.checkbox.setChecked(false);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_item, parent, false);
        return new TaskHolder(v);
    }


    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}

