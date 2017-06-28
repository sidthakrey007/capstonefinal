package com.example.sthakrey.donote;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sthakrey.donote.data.MyCustomTaskAdapter;
import com.example.sthakrey.donote.data.Notes;
import com.example.sthakrey.donote.data.Task;
import com.example.sthakrey.donote.data.Todo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.sthakrey.donote.SettingsFragment.colorList;
import static com.example.sthakrey.donote.SettingsFragment.labeltextView;
import static com.example.sthakrey.donote.SettingsFragment.selectedItem;

public class EditTodoActivity extends AppCompatActivity {

    @BindView(R.id.task_recylerview)
    RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    FirebaseRecyclerAdapter<Task, TaskHolder> adapter = null;
    static int maxElement = 0;
    @BindView(R.id.todo_header)
    FrameLayout todoHeader;
    @BindView(R.id.mytoolbar)
    Toolbar toolbar;
    String label;
    @BindView(R.id.input_title)
    TextView title;
    @BindView(R.id.fab_settings)
    FloatingActionButton fab;
    @BindView(R.id.add_newtask_button)
    ImageButton newTaskButton;
    @BindView(R.id.fab_save)
    FloatingActionButton fabSave;
    String functionality;
    Map<String, Task> mymap;
    ArrayList<Task> arr = new ArrayList<Task>();
    MyCustomTaskAdapter customAdapter;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_edit_todo);
        ButterKnife.bind(this);
        mymap = new HashMap<String, Task>();
        mLinearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        if (savedInstanceState != null) {
            todoHeader.setBackgroundColor(savedInstanceState.getInt("Color"));

        }

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace("@", "").replace(".", "");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final String todokey = getIntent().getStringExtra("TodoKey");
        functionality = getIntent().getStringExtra("Functionality");

        if (functionality.equals("edit")) {

            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("/user/" + email + "/todos/" + todokey);
            final ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.print("sdsd");
                    Todo notes = dataSnapshot.getValue(Todo.class);
                    if (notes != null) {

                        label = notes.getLabel();
                        title.setText(notes.getTitle());
                        int color = (int) Long.parseLong(notes.getColor(), 16);
                        int r = (color >> 16) & 0xFF;
                        int g = (color >> 8) & 0xFF;
                        int b = (color >> 0) & 0xFF;

                        todoHeader.setBackgroundColor(Color.rgb(r, g, b));
                        if (colorList[0].equals(notes.getColor()))
                            selectedItem = 1;
                        else {
                            if (colorList[1].equals(notes.getColor()))
                                selectedItem = 2;
                            else {
                                if (colorList[2].equals(notes.getColor()))
                                    selectedItem = 3;
                                else {
                                    if (colorList[3].equals(notes.getColor()))
                                        selectedItem = 4;
                                    else {
                                        if (colorList[4].equals(notes.getColor()))
                                            selectedItem = 5;
                                        else {
                                            selectedItem = -1;
                                        }
                                    }

                                }
                            }
                        }// ...
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    // ...
                }
            };

            firebaseDatabase.addValueEventListener(postListener);

            firebaseDatabase = FirebaseDatabase.getInstance().getReference("/user/" + email + "/tasks/" + todokey + "/");


            adapter = new FirebaseRecyclerAdapter<Task, TaskHolder>(Task.class, R.layout.task_list_item, TaskHolder.class, firebaseDatabase) {


                @Override
                protected void populateViewHolder(final TaskHolder viewHolder, Task model, final int position) {
                    viewHolder.task.setText(model.getTask());

                    viewHolder.isChecked = model.getIsChecked();
                    if (viewHolder.isChecked) {
                        viewHolder.checkbox.setChecked(true);
                        viewHolder.task.setPaintFlags(viewHolder.task.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (viewHolder.getAdapterPosition() >= 0)
                                adapter.getRef(viewHolder.getAdapterPosition()).removeValue();

                        }


                    });
                }


            };


            mRecyclerView.setAdapter(adapter);

        } else {
            Log.e("ADD", "FUNCTIONALITY");

            customAdapter = new MyCustomTaskAdapter(arr);
            mRecyclerView.setAdapter(customAdapter);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (findViewById(R.id.label_selected) == null) {

                    FragmentTransaction ftr;
                    Fragment fra = null;
                    ftr = getSupportFragmentManager().beginTransaction();

                    ftr.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_out, R.anim.slide_in_up, R.anim.slide_in_out);
                    fra = new SettingsFragment();
                    Bundle message = new Bundle();
                    message.putString("Type", "todos");
                    message.putString("Key", todokey);
                    message.putString("Functionality", getIntent().getStringExtra("Functionality"));

                    fra.setArguments(message);
                    ftr.replace(R.id.settings_container, fra, "tag");
                    ftr.addToBackStack("nam");
                    ftr.commit();

                } else {
                    getSupportFragmentManager().popBackStack();

                }

            }
        });


        newTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (functionality.equals("edit")) {
                    FirebaseDatabase fdb = FirebaseDatabase.getInstance();
                    fdb.getReference("user/tasks/" + todokey).push().setValue(new Task("", false));
                } else {

                    Task t = new Task("", false);
                    arr.add(t);

                    customAdapter.notifyDataSetChanged();

                }
                newTaskButton.announceForAccessibility("New Task added");

            }
        });


        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference fdbref = firebaseDatabase.getReference("/user/" + email + "/todos");
                if (functionality.equals("add")) {
                    String key = fdbref.push().getKey();

                    if (labeltextView == null) {
                        labeltextView = new TextView(getBaseContext());
                        labeltextView.setText(R.string.nolabel);
                    }

                    String color;
                    if (selectedItem == -1)
                        color = "ffffff";
                    else
                        color = colorList[selectedItem - 1];

                    fdbref.child(key).setValue(new Todo(title.getText().toString(),
                            labeltextView.getText().toString(), color, mymap));

                    DatabaseReference fdb = FirebaseDatabase.getInstance().getReference("/user/" + email + "/tasks/" + key);
                    for (Task item : arr) {
                        fdb.push().setValue(item);
                    }
                    finish();
                } else {
                    if (functionality.equals("edit")) {


                        String color;
                        if (selectedItem != -1)
                            color = colorList[selectedItem - 1];
                        else
                            color = "ffffff";

                        Todo editTodo = new Todo(title.getText().toString(),
                                label, color, mymap);
                        fdbref.child(todokey).setValue(editTodo);
                        for (int i = 0; i < adapter.getItemCount(); i++) {
                            DatabaseReference db = FirebaseDatabase.getInstance().getReference("/user/" + email + "/tasks/" + todokey + "/" + adapter.getRef(i).getKey());
                            TaskHolder t = (TaskHolder) mRecyclerView.findViewHolderForAdapterPosition(i);

                            db.child("task").setValue(t.task.getText().toString());
                            db.child("isChecked").setValue(t.checkbox.isChecked());
                        }
                        finish();

                    }
                }

            }


        });


    }


    @Override
    protected void onPause() {
        super.onPause();
        onSaveInstanceState(new Bundle());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int backColor = Color.TRANSPARENT;
        Drawable background = todoHeader.getBackground();
        if (background instanceof ColorDrawable)
            backColor = ((ColorDrawable) background).getColor();

        outState.putInt("Color", backColor);
//        outState.putString("label", labeltextView.getText().toString());
    }


    public static class TaskHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.task_text)
        public EditText task;
        @BindView(R.id.mycheckbox)
        public CheckBox checkbox;
        public boolean isChecked;
        @BindView(R.id.delete_task_button)
        public ImageButton deleteButton;


        public TaskHolder(View itemView) {

            super(itemView);
            ButterKnife.bind(this, itemView);
//            checkbox = (CheckBox) itemView.findViewById(R.id.mycheckbox);
//            task = (EditText) itemView.findViewById(R.id.task_text);
//
//            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_task_button);
//
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        task.setPaintFlags(task.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        checkbox.announceForAccessibility("Task Checked");
                    } else {
                        task.setPaintFlags(task.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
                        checkbox.announceForAccessibility("Task UnChecked");
                    }
                }
            });
        }

    }

}
