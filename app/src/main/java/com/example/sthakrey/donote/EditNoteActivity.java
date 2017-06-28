package com.example.sthakrey.donote;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sthakrey.donote.data.Notes;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.sthakrey.donote.SettingsFragment.colorList;
import static com.example.sthakrey.donote.SettingsFragment.labeltextView;
import static com.example.sthakrey.donote.SettingsFragment.selectedItem;

public class EditNoteActivity extends AppCompatActivity {

    @BindView(R.id.input_title)
    EditText title;
    @BindView(R.id.input_description)
    EditText description;
    @BindView(R.id.fab_settings)
    FloatingActionButton fab;
    @BindView(R.id.fab_save)
    FloatingActionButton fab_save;
    public static String ACTION_DATA_UPDATE = "com.example.sthakrey.donote.widget.ACTION_DATA_UPDATE";
    @BindView(R.id.note_header)
    FrameLayout note_header;
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private String notesKey;
    private String label;
    String email;
    private String functionality;
    ImageView myphoto = null;
    @BindView(R.id.mytoolbar)
    Toolbar toolbar;
    static String newNoteKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        ButterKnife.bind(this);
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
        notesKey = getIntent().getStringExtra("NotesKey");
        functionality = getIntent().getStringExtra("Functionality");

        if (savedInstanceState != null) {
            note_header.setBackgroundColor(savedInstanceState.getInt("Color"));
            if (labeltextView != null)
                labeltextView.setText(savedInstanceState.getString("label"));

        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace("@", "").replace(".", "");
        else {
            startActivity(new Intent(getBaseContext(), MainActivity.class));
            finish();
        }

        if (functionality.equals("edit")) {
            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("/user/" + email + "/notes/" + notesKey);
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    System.out.print("sdsd");
                    Notes notes = dataSnapshot.getValue(Notes.class);
                    if (notes != null) {
                        title.setText(notes.getTitle());
                        label = notes.getLabel();
                        description.setText(notes.getDescription());
                        int color = (int) Long.parseLong(notes.getColor(), 16);
                        int r = (color >> 16) & 0xFF;
                        int g = (color >> 8) & 0xFF;
                        int b = (color >> 0) & 0xFF;

                        note_header.setBackgroundColor(Color.rgb(r, g, b));

                        if (colorList[0].equals(notes.getColor()))
                            selectedItem = 1;
                        else


                        {
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
                                        else
                                            selectedItem = -1;

                                    }
                                }
                            }
                        }
                    }
                    // ...

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    // ...
                }
            };
            firebaseDatabase.addValueEventListener(postListener);
        }

        if (functionality.equals("add"))
            selectedItem = -1;


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment x = getSupportFragmentManager().findFragmentByTag("tag");
                if (x == null) {

                    FragmentTransaction ftr;
                    Fragment fra = null;
                    ftr = getSupportFragmentManager().beginTransaction();


                    ftr.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_out, R.anim.slide_in_up, R.anim.slide_in_out);
                    fra = new SettingsFragment();
                    Bundle message = new Bundle();
                    message.putString("Type", "notes");
                    message.putString("Key", notesKey);
                    message.putString("Functionality", getIntent().getStringExtra("Functionality"));
                    fra.setArguments(message);
                    ftr.addToBackStack("lah");

                    ftr.replace(R.id.settings_container, fra, "tag");
                    Log.e("sf", "Rotated");
                    ftr.commit();


                } else {
                    getSupportFragmentManager().popBackStack();

                }

            }
        });


        fab_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Email", email);
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference fdbref = firebaseDatabase.getReference("/user/" + email + "/notes");
                if (functionality.equals("add")) {
                    if (labeltextView == null) {
                        labeltextView = new TextView(getBaseContext());
                        labeltextView.setText(R.string.nolabel);
                    }
                    String color;
                    if (selectedItem == -1)
                        color = "ffffff";
                    else
                        color = colorList[selectedItem - 1];
                    fdbref.push().setValue(new Notes(title.getText().toString(), description.getText().toString(),
                            labeltextView.getText().toString(), color));
                } else {
                    if (functionality.equals("edit")) {
                        String color;
                        if (selectedItem != -1)
                            color = colorList[selectedItem - 1];
                        else
                            color = "ffffff";

                        Notes editNote = new Notes(title.getText().toString(), description.getText().toString(),
                                label, color);
                        fdbref.child(notesKey).setValue(editNote);

                    }
                }

                Intent myaction = new Intent();
                myaction.setAction(ACTION_DATA_UPDATE);
                sendBroadcast(myaction);
                finish();


            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            myphoto.setImageBitmap(imageBitmap);
            myphoto.setVisibility(View.VISIBLE);
        }
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
        Drawable background = note_header.getBackground();
        if (background instanceof ColorDrawable)
            backColor = ((ColorDrawable) background).getColor();

        outState.putInt("Color", backColor);
    }


}
