package com.example.sthakrey.donote;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.sthakrey.donote.data.Label;
import com.example.sthakrey.donote.data.Notes;
import com.example.sthakrey.donote.data.Task;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditLabelActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static String selectedLabel = "";
    private String noteKey = "";
    @BindView(R.id.labelrecyclerview)
    RecyclerView mRecyclerView;
    private static int selectedPosition = -1;
    FirebaseRecyclerAdapter<String, LabelHolder> adapter;
    @BindView(R.id.mytoolbar)
    Toolbar toolbar;
    @BindView(R.id.search)
    SearchView sv;
    @BindView(R.id.edit_label_root)
    View v;
    @BindView(R.id.fab_save)
    FloatingActionButton fab;
    @BindView(R.id.fab_addlabel)
    FloatingActionButton fab_add;

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_label);
        ButterKnife.bind(this);
        v.announceForAccessibility("Select one label to set");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                layoutManager.getOrientation());

        final Context c = this;
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        setSupportActionBar(toolbar);

        SearchManager sm = (SearchManager) getSystemService(SEARCH_SERVICE);
        sv.setSearchableInfo(sm.getSearchableInfo(getComponentName()));
        sv.setOnQueryTextListener(this);

        selectedLabel = getIntent().getStringExtra("Label");
        noteKey = getIntent().getStringExtra("Key");

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace("@", "").replace(".", "");
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("/user/" + email + "/labels");


        adapter = new FirebaseRecyclerAdapter<String, LabelHolder>(String.class, R.layout.edit_label_list_item, LabelHolder.class, firebaseDatabase) {
            @Override
            protected void populateViewHolder(final LabelHolder viewHolder, String model, int position) {
                viewHolder.labelName.setText(model);
                if (selectedLabel.equals(model)) {
                    viewHolder.radioButton.setChecked(true);
                    selectedPosition = position;
                } else
                    viewHolder.radioButton.setChecked(false);
                viewHolder.radioButton.announceForAccessibility(viewHolder.labelName + " selected");

                viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewHolder.radioButton.setChecked(true);
                        if (selectedPosition != -1 && selectedPosition != viewHolder.getAdapterPosition()) {
                            LabelHolder m = (LabelHolder) mRecyclerView.findViewHolderForAdapterPosition(selectedPosition);
                            if (m != null)
                                m.radioButton.setChecked(false);
                        }
                        selectedPosition = viewHolder.getAdapterPosition();
                        selectedLabel = viewHolder.labelName.getText().toString();

                    }

                });


            }


        };
        mRecyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference = null;
                LabelHolder holder = (LabelHolder) mRecyclerView.findViewHolderForAdapterPosition(selectedPosition);
                String labelSelected = holder.labelName.getText().toString();
                if (getIntent().getStringExtra("Type").equals("notes")) {
                    if (getIntent().getStringExtra("Functionality").equals("add")) {

                        SettingsFragment.labeltextView.setText(labelSelected);
                    } else {
                        databaseReference = FirebaseDatabase.getInstance().getReference("/user/" + email + "/notes/" + noteKey + "/label");
                        databaseReference.setValue(labelSelected);

                    }
                } else {

                    if (getIntent().getStringExtra("Functionality").equals("add")) {

                        SettingsFragment.labeltextView.setText(labelSelected);
                    } else {
                        databaseReference = FirebaseDatabase.getInstance().getReference("/user/" + email + "/todos/" + noteKey + "/label");
                        databaseReference.setValue(labelSelected);


                    }

                }


                finish();

            }
        });


        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(c);
                builder.setTitle(R.string.label_dialog_message);

// Set up the input
                final EditText input = new EditText(c);

// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton(
                        R.string.ok, new DialogInterface.OnClickListener() {
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
    }


    public static class LabelHolder extends RecyclerView.ViewHolder {
        public RadioButton radioButton;
        public TextView labelName;


        public LabelHolder(View itemView) {
            super(itemView);
            radioButton = (RadioButton) itemView.findViewById(R.id.radiobutton);
            labelName = (TextView) itemView.findViewById(R.id.label_text);


        }

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
        adapter = new FirebaseRecyclerAdapter<String, LabelHolder>(String.class, R.layout.edit_label_list_item, LabelHolder.class, firebaseDatabase) {
            @Override
            protected void populateViewHolder(LabelHolder viewHolder, String model, int position) {
                viewHolder.labelName.setText(model);
                if (selectedLabel.equals(model)) {
                    Log.e("FILTER, Passed", selectedLabel);
                    viewHolder.radioButton.setChecked(true);
                    selectedPosition = position;
                    selectedLabel = viewHolder.labelName.getText().toString();
                } else
                    viewHolder.radioButton.setChecked(false);

            }

        };


        mRecyclerView.getRecycledViewPool().clear();
        mRecyclerView.swapAdapter(adapter, true);


        return false;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
}
