package com.example.sthakrey.donote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sthakrey.donote.data.ExpandableListAdapter;
import com.example.sthakrey.donote.data.ExpandedMenuModel;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    List<ExpandedMenuModel> listDataHeader;
    HashMap<ExpandedMenuModel, List<String>> listDataChild;
    ExpandableListAdapter mMenuAdapter;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.mytoolbar)
    Toolbar t;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.navigationmenu)
    ExpandableListView expandableList;
    public static List<String> heading2;


    public static GoogleApiClient mGoogleApiClient = null;
    public static GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putBoolean("persistence", false);
        if (sp.getBoolean("persistence", false)) {
            // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            sp.edit().putBoolean("persistence", true);
        }


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        setSupportActionBar(t);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, drawer, t, R.string.openDrawer, R.string.closeDrawer) {

            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
                //drawerOpened = false;
            }


            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
                //drawerOpened = true;
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();


        //email.setText();

        prepareListData();
        if (savedInstanceState != null) {
            getSupportActionBar().setTitle(savedInstanceState.getString("actionbartext"));

            getSupportActionBar().setLogo(R.drawable.ic_event_note_white_actionbar);

        }

        mMenuAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, expandableList);

        // setting list adapter
        expandableList.setAdapter(mMenuAdapter);
        expandableList.setGroupIndicator(null);


        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                //Log.d("DEBUG", "heading clicked");

                switch (i) {
                    case 0: {
                        drawer.closeDrawers();
                        t.setTitle("Notes");
                        t.setTitleMarginStart(80);
                        t.setLogo(R.drawable.ic_event_note_white_actionbar);
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new NotesFragment()).commit();

                        break;
                    }
                    case 1: {
                        drawer.closeDrawers();
                        t.setTitle("ToDo");
                        t.setTitleMarginStart(80);
                        t.setLogo(R.drawable.ic_format_list_bulleted_white_36dp);

                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new TodoFragment()).commit();
                        break;
                    }

                    case 3: {
                        Intent message = new Intent(getBaseContext(), ManageLabelActivity.class);
                        startActivity(message);
                        break;
                    }
                    case 4: {
                        signOut();
                        break;
                    }
                }
                return false;
            }
        });
        Log.e("SDsds", String.valueOf(expandableList.getSelectedPosition()));


        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                TextView labelView = (TextView) v.findViewById(R.id.submenu);
                String label = labelView.getText().toString();
                Bundle message = new Bundle();
                message.putString("label", label);
                LabelFragment lf = new LabelFragment();
                lf.setArguments(message);
                drawer.closeDrawers();
                t.setTitle(label);
                t.setLogo(R.drawable.ic_label_white_36dp);
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, lf).commit();

                return false;
            }
        });
        Uri urlProfileImg = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        View navHeader = navigationView.getHeaderView(0);
        ImageView imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

//        @InjectView(R.id.website)
        TextView emailName = (TextView) navHeader.findViewById(R.id.website);
        TextView name = (TextView) navHeader.findViewById(R.id.name);

        Glide.with(this).load(urlProfileImg)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile);


        emailName.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        name.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());


        if (savedInstanceState == null) {
            drawer.closeDrawers();
            getSupportActionBar().setTitle("Notes");
            t.setTitleMarginStart(80);
            t.setLogo(R.drawable.ic_event_note_white_actionbar);
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new NotesFragment()).commit();

        }
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<ExpandedMenuModel>();
        listDataChild = new HashMap<ExpandedMenuModel, List<String>>();


        ExpandedMenuModel item1 = new ExpandedMenuModel();
        item1.setIconName("Notes");
        item1.setIconImg(android.R.drawable.ic_delete);
        // Adding data header
        listDataHeader.add(item1);

        ExpandedMenuModel item2 = new ExpandedMenuModel();
        item2.setIconName("Todos");
        item2.setIconImg(android.R.drawable.ic_delete);
        listDataHeader.add(item2);


        ExpandedMenuModel item3 = new ExpandedMenuModel();
        item3.setIconName("Labels");
        item3.setIconImg(android.R.drawable.ic_delete);
        listDataHeader.add(item3);


        heading2 = new ArrayList<>();

        heading2.add("Personal");
        heading2.add("NoLabel");
        heading2.add("Experience");

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace("@", "").replace(".", "");

        FirebaseDatabase.getInstance().getReference("/user/" + email + "/labels/111").setValue("Personal");
        FirebaseDatabase.getInstance().getReference("/user/" + email + "/labels/2222").setValue("Work");
        FirebaseDatabase.getInstance().getReference("/user/" + email + "/labels/3333").setValue("NoLabel");


        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("/user/" + email + "/labels");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<String> replacesList = new ArrayList<String>();
                for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                    String areaName = areaSnapshot.getValue(String.class);
                    replacesList.add(areaName);
                }


                listDataChild.put(listDataHeader.get(2), replacesList);

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ExpandedMenuModel item5 = new ExpandedMenuModel();
        item5.setIconName("Manage Labels");
        item5.setIconImg(android.R.drawable.ic_delete);
        listDataHeader.add(item5);
        ExpandedMenuModel item4 = new ExpandedMenuModel();
        item4.setIconName("Log Out");
        item4.setIconImg(android.R.drawable.ic_delete);
        listDataHeader.add(item4);

        listDataChild.put(listDataHeader.get(0), new ArrayList<String>());
        listDataChild.put(listDataHeader.get(1), new ArrayList<String>());
        listDataChild.put(listDataHeader.get(2), heading2);
        listDataChild.put(listDataHeader.get(3), new ArrayList<String>());

    }


    private void signOut() {

        FirebaseAuth.getInstance().signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Intent message = new Intent();
                        message.setAction(EditNoteActivity.ACTION_DATA_UPDATE);
                        sendBroadcast(message);

                        updateUI();
                    }
                });

    }

    private void updateUI() {
        Intent logout = new Intent(this, MainActivity.class);
        startActivity(logout);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {

        super.onPause();
        onSaveInstanceState(new Bundle());
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String title = t.getTitle().toString();
        outState.putString("actionbartext", title);
        super.onSaveInstanceState(outState);
    }
}
