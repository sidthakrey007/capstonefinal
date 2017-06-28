package com.example.sthakrey.donote;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ManageLabelActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    FirebaseRecyclerAdapter<String, LabelHolder> adapter;
    @BindView(R.id.labelmrecyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.mytoolbar)
    Toolbar toolbar;
    @BindView(R.id.search)
    SearchView sv;
    @BindView(R.id.fab_addlabel)
    FloatingActionButton fab_add;
    @BindView(R.id.fab_save)
    FloatingActionButton fab_save;
    static Context c;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_label);
        ButterKnife.bind(this);


        email = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace("@", "").replace(".", "");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                layoutManager.getOrientation());

        mRecyclerView.addItemDecoration(dividerItemDecoration);

        c = this;
        setSupportActionBar(toolbar);

        SearchManager sm = (SearchManager) getSystemService(SEARCH_SERVICE);
        sv.setSearchableInfo(sm.getSearchableInfo(getComponentName()));
        sv.setOnQueryTextListener(this);


        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("/user/" + email + "/labels");


        adapter = new FirebaseRecyclerAdapter<String, LabelHolder>(String.class, R.layout.manage_label_list_item, LabelHolder.class, firebaseDatabase) {
            @Override
            protected void populateViewHolder(final LabelHolder viewHolder, String model, final int position) {
                viewHolder.labelName.setText(model);
                viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String key = adapter.getRef(viewHolder.getAdapterPosition()).getKey();
                        DatabaseReference fdb = FirebaseDatabase.getInstance().getReference("/user/" + email + "/labels/" + key);
                        fdb.removeValue();
                        String old_val = viewHolder.labelName.getText().toString();
                        DatabaseReference notesDbref = FirebaseDatabase.getInstance().getReference("/user/" + email + "/notes/");
                        notesDbref.orderByChild("label").equalTo(old_val).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot fruitSnapshot : dataSnapshot.getChildren())
                                    fruitSnapshot.getRef().child("label").setValue(getString(R.string.nolabel));

                                Intent message = new Intent();
                                message.setAction(EditNoteActivity.ACTION_DATA_UPDATE);
                                sendBroadcast(message);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        DatabaseReference todosDbref = FirebaseDatabase.getInstance().getReference("/user/" + email + "/todos/");
                        todosDbref.orderByChild("label").equalTo(old_val).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot fruitSnapshot : dataSnapshot.getChildren())
                                    fruitSnapshot.getRef().child("label").setValue(getString(R.string.nolabel));

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });

                viewHolder.editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        builder.setTitle(R.string.label_dialog_heading);

// Set up the input
                        final EditText input = new EditText(c);

// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        builder.setView(input);

// Set up the buttons
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String m_Text = input.getText().toString();
                                String old_val = viewHolder.labelName.getText().toString();
                                String key = adapter.getRef(viewHolder.getAdapterPosition()).getKey();
                                DatabaseReference fdb = FirebaseDatabase.getInstance().getReference("/user/" + email + "/labels/" + key);

                                fdb.setValue(m_Text);
                                DatabaseReference notesDbref = FirebaseDatabase.getInstance().getReference("/user/" + email + "/notes/");
                                notesDbref.orderByChild("label").equalTo(old_val).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot fruitSnapshot : dataSnapshot.getChildren())
                                            fruitSnapshot.getRef().child("label").setValue(m_Text);

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                DatabaseReference todosDbref = FirebaseDatabase.getInstance().getReference("/user/" + email + "/todos/");
                                todosDbref.orderByChild("label").equalTo(old_val).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot fruitSnapshot : dataSnapshot.getChildren())
                                            fruitSnapshot.getRef().child("label").setValue(m_Text);

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                });

            }

        };
        mRecyclerView.setAdapter(adapter);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(c);
                builder.setTitle("Enter label name");

                final EditText input = new EditText(c);

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString();
                        DatabaseReference fdb = FirebaseDatabase.getInstance().getReference("/user/" + email + "/labels");
                        fdb.push().setValue(m_Text);

                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        fab_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    public static class LabelHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.edit_label)
        public ImageButton editButton;
        @BindView(R.id.delete_label)
        public ImageButton deleteButton;
        @BindView(R.id.label_text)
        public TextView labelName;


        public LabelHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    public boolean onQueryTextChange(String newText) {


        String newText2 = new String(newText);
        Query firebaseDatabase = null;
        int length = newText2.length();
        if (length != 0) {
            int c = newText2.charAt(length - 1);
            c = c + 1;
            StringBuilder myName = new StringBuilder(newText2);
            myName.setCharAt(length - 1, (char) c);
            newText2 = myName.toString();

            firebaseDatabase = FirebaseDatabase.getInstance().getReference("/user/" + email + "/labels").orderByValue().startAt(newText).endAt(newText2);
        } else
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("/user/" + email + "/labels").orderByValue();
        adapter.cleanup();
        adapter = new FirebaseRecyclerAdapter<String, LabelHolder>(String.class, R.layout.manage_label_list_item, LabelHolder.class, firebaseDatabase) {
            @Override
            protected void populateViewHolder(LabelHolder viewHolder, String model, int position) {
                viewHolder.labelName.setText(model);
            }

        };


        mRecyclerView.getRecycledViewPool().clear();
        mRecyclerView.swapAdapter(adapter, true);


        return false;
    }

}
