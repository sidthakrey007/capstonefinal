package com.example.sthakrey.donote;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.sthakrey.donote.data.Task;
import com.example.sthakrey.donote.data.Todo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class TodoFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    static FirebaseRecyclerAdapter<Todo, TodoViewHolder> adapter = null;
    private static Context s;
    String email;

    TextView emptyText;
    RecyclerView.AdapterDataObserver mObserver;

    public void RegisterObserver() {
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                //perform check and show/hide empty view
                if (itemCount == 0)
                    emptyText.setVisibility(View.VISIBLE);
                else
                    emptyText.setVisibility(View.GONE);

            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                //perform check and show/hide empty view
                if (itemCount == 0)
                    emptyText.setVisibility(View.VISIBLE);
                else
                    emptyText.setVisibility(View.GONE);

            }
        };
        adapter.registerAdapterDataObserver(mObserver);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_todo, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewtodo);
        mRecyclerView.setNextFocusDownId(R.id.fab_addtodo);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace("@", "").replace(".", "");

        Query firebaseDatabase = null;
        if (getArguments() == null)
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("/user/" + email + "todos");
        else {
            String label = getArguments().getString("label");

            firebaseDatabase = FirebaseDatabase.getInstance().getReference("/user/" + email + "todos").orderByChild("label").equalTo(label);
        }

        s = getContext();
        adapter = new FirebaseRecyclerAdapter<Todo, TodoViewHolder>(Todo.class, R.layout.todos_list_item, TodoViewHolder.class, firebaseDatabase) {


            @Override
            protected void populateViewHolder(final TodoViewHolder viewHolder, Todo model, final int position) {
                viewHolder.title.setText(model.getTitle());
                int color = (int) Long.parseLong(model.getColor(), 16);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = (color >> 0) & 0xFF;

                viewHolder.mView.setBackgroundColor(Color.rgb(r, g, b));
                viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (viewHolder.getAdapterPosition() >= 0)
                            adapter.getRef(viewHolder.getAdapterPosition()).removeValue();


                    }
                });


                DatabaseReference nesteddbr = FirebaseDatabase.getInstance().getReference("/user/" + email + "tasks/" + adapter.getRef(position).getKey());
                FirebaseRecyclerAdapter<Task, TaskHolder> nestedadp = new FirebaseRecyclerAdapter<Task, TaskHolder>(Task.class, R.layout.task_list_main_item, TaskHolder.class, nesteddbr) {


                    @Override
                    protected void populateViewHolder(TaskHolder viewHol, Task mod, int pos) {
                        viewHol.task.setText(mod.getTask());

                        viewHol.isChecked = mod.getIsChecked();
                        if (viewHol.isChecked) {
                            viewHol.checkbox.setChecked(true);
                            viewHol.checkbox.setContentDescription("Check box checked");
                            viewHol.task.setPaintFlags(viewHol.task.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                        } else
                            viewHol.checkbox.setContentDescription("Check box unchecked");
                    }

                };


                LinearLayoutManager lm = new LinearLayoutManager(getContext());
                lm.setOrientation(LinearLayoutManager.VERTICAL);
                viewHolder.recyclerView.setLayoutManager(lm);
                viewHolder.recyclerView.setAdapter(nestedadp);
                viewHolder.label = model.getLabel();
                viewHolder.color = model.getColor();
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent editTodo = new Intent(getActivity(), EditTodoActivity.class);
                        String key = adapter.getRef(viewHolder.getAdapterPosition()).getKey();
                        editTodo.putExtra("TodoKey", key);
                        editTodo.putExtra("Label", "NoLabel");
                        editTodo.putExtra("Functionality", "edit");
                        startActivity(editTodo);
                    }
                });

                viewHolder.mView.setFocusable(true);
                viewHolder.mView.setNextFocusRightId(R.id.delete_todo);

                viewHolder.labelView.setText(model.getLabel());

            }


        };

        mRecyclerView.setAdapter(adapter);

        emptyText = (TextView) view.findViewById(R.id.empty_todo_text);

        firebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //onDataChange called so remove progress bar

                //make a call to dataSnapshot.hasChildren() and based
                //on returned value show/hide empty view

                //use helper method to add an Observer to RecyclerView

                Log.e("Reference is ", dataSnapshot.getRef().toString());
                if (dataSnapshot.hasChildren() == false) {

                    emptyText.setVisibility(View.VISIBLE);
                } else
                    emptyText.setVisibility(View.GONE);

                RegisterObserver();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FloatingActionButton addnote_fab = (FloatingActionButton) view.findViewById(R.id.fab_addtodo);
        addnote_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(getActivity(), EditTodoActivity.class);
                editIntent.putExtra("Functionality", "add");
                startActivity(editIntent);

            }
        });
        return view;
    }


    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public String label, color;
        public View mView;
        public MyRecyclerView recyclerView;
        public TextView labelView;
        public ImageButton deleteButton;


        public TodoViewHolder(View itemView) {

            super(itemView);
            mView = itemView;
            title = (TextView) itemView.findViewById(R.id.todo_title);
            recyclerView = (MyRecyclerView) itemView.findViewById(R.id.tasklist);
            labelView = (TextView) itemView.findViewById(R.id.todo_label);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_todo);


        }

    }

    public static class TaskHolder extends RecyclerView.ViewHolder {
        public TextView task;
        public CheckBox checkbox;
        public boolean isChecked;


        public TaskHolder(View itemView) {

            super(itemView);
            checkbox = (CheckBox) itemView.findViewById(R.id.mycheckbox);
            task = (TextView) itemView.findViewById(R.id.task_text);


        }


    }
}
