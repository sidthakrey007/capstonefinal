package com.example.sthakrey.donote;


import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContentResolverCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.SwappingHolder;
import com.example.sthakrey.donote.data.Notes;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class NotesFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    FirebaseRecyclerAdapter<Notes, NotesViewHolder> adapter = null;

    String email;

    public NotesFragment() {
        // Required empty public constructor
    }

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
        // Inflate the layout for this fragment

        final View view = inflater.inflate(R.layout.notes_fragment, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setNextFocusDownId(R.id.fab_addnote);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace("@", "").replace(".", "");


        Log.e("BOO", email);


        Query firebaseDatabase;
        if (getArguments() == null)
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("/user/" + email + "/notes");
        else {
            String label = getArguments().getString("label");
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("/user/" + email + "/notes").orderByChild("label").equalTo(label);
        }


        emptyText = (TextView) view.findViewById(R.id.empty_note_text);

        adapter = new FirebaseRecyclerAdapter<Notes, NotesViewHolder>(Notes.class, R.layout.notes_list_item, NotesViewHolder.class, firebaseDatabase) {


            @Override
            protected void populateViewHolder(final NotesViewHolder viewHolder, Notes model, final int position) {

                String key = adapter.getRef(position).getKey();
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("/user/" + email + "/notes/" + key);
                ValueEventListener ve = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Notes n = dataSnapshot.getValue(Notes.class);
                        if (n != null) {
                            int color = (int) Long.parseLong(n.getColor(), 16);
                            int r = (color >> 16) & 0xFF;
                            int g = (color >> 8) & 0xFF;
                            int b = (color >> 0) & 0xFF;

                            viewHolder.mView.setBackgroundColor(Color.rgb(r, g, b));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                db.addValueEventListener(ve);


                viewHolder.title.setText(model.getTitle());

                viewHolder.description.setText(model.getDescription());
                viewHolder.mView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String key = adapter.getRef(viewHolder.getAdapterPosition()).getKey();
                        Intent edit = new Intent(getActivity(), EditNoteActivity.class);
                        edit.putExtra("Functionality", "edit");
                        edit.putExtra("NotesKey", key);
                        Log.e("key is ", key);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            startActivity(edit, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                        else
                            startActivity(edit);

                    }
                });

                viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (viewHolder.getAdapterPosition() >= 0) {
                            adapter.getRef(viewHolder.getAdapterPosition()).removeValue();
                            Intent message = new Intent();
                            message.setAction(EditNoteActivity.ACTION_DATA_UPDATE);
                            getActivity().sendBroadcast(message);

                        }

                    }
                });

                viewHolder.mView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus)
                            viewHolder.mView.setNextFocusRightId(R.id.delete_note);
                    }
                });
                viewHolder.label = model.getLabel();
                viewHolder.color = model.getColor();
                viewHolder.labelView.setText(model.getLabel());
            }


        };


        mRecyclerView.setAdapter(adapter);


        //mRecyclerView.setfocus
        firebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //onDataChange called so remove progress bar

                //make a call to dataSnapshot.hasChildren() and based
                //on returned value show/hide empty view

                //use helper method to add an Observer to RecyclerView

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

        FloatingActionButton addnote_fab = (FloatingActionButton) view.findViewById(R.id.fab_addnote);
        addnote_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(getActivity(), EditNoteActivity.class);
                editIntent.putExtra("Functionality", "add");

                startActivity(editIntent);
            }
        });
        return view;
    }


    public static class NotesViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView description;
        public String label, color;
        public View mView;
        public TextView labelView;
        public ImageButton deleteButton;


        public NotesViewHolder(View itemView) {

            super(itemView);
            mView = itemView;
            labelView = (TextView) itemView.findViewById(R.id.note_label);
            title = (TextView) itemView.findViewById(R.id.note_title);
            description = (TextView) itemView.findViewById(R.id.note_description);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_note);


        }

    }
}
