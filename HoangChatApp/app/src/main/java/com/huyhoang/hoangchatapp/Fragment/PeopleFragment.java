package com.huyhoang.hoangchatapp.Fragment;

import androidx.lifecycle.ViewModelProviders;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.UserManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.huyhoang.hoangchatapp.ChatActivity;
import com.huyhoang.hoangchatapp.Common.Common;
import com.huyhoang.hoangchatapp.Model.UserModel;
import com.huyhoang.hoangchatapp.R;
import com.huyhoang.hoangchatapp.ViewHolders.UserViewHolder;
import com.huyhoang.hoangchatapp.databinding.PeopleFragmentBinding;

public class PeopleFragment extends Fragment {

    private PeopleFragmentBinding binding;

    FirebaseRecyclerAdapter adapter;

    private PeopleViewModel mViewModel;

    static PeopleFragment instance;

    public static PeopleFragment getInstance(){
        return instance == null ? new PeopleFragment() : instance;
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = PeopleFragmentBinding.inflate(inflater, container, false);
        View itemView = binding.getRoot();
        initView(itemView);
        loadPeople();
        return itemView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadPeople() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(Common.USER_REFERENCES);
        FirebaseRecyclerOptions<UserModel> options =  new FirebaseRecyclerOptions
                .Builder<UserModel>()
                .setQuery(query, UserModel.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<UserModel, UserViewHolder>(options) {

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_people, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserModel model) {
                if(!adapter.getRef(position).getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    // Ẩn bản thân\
                    ColorGenerator generator = ColorGenerator.MATERIAL;
                    int color = generator.getColor(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    TextDrawable.IBuilder builder = TextDrawable.builder()
                            .beginConfig()
                            .withBorder(4)
                            .endConfig()
                            .round();
                    TextDrawable drawable = builder.build(model.getFirstName().substring(0,1),color);
                    holder.binding.imgAvatar.setImageDrawable(drawable);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(model.getFirstName()).append(" ").append(model.getLastName());
                    holder.binding.txtName.setText(stringBuilder.toString());
                    holder.binding.txtBio.setText(model.getBio());

                    holder.itemView.setOnClickListener(view -> {
                        Common.chatUser = model;
                        Common.chatUser.setUid(adapter.getRef(position).getKey());

                        String roomId = Common.generateChatRoomid(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                Common.chatUser.getUid());
                        Common.roomSelected = roomId;

                        Log.d("ROOMID",roomId);

                        //Register Topic
                        FirebaseMessaging.getInstance()
                                .subscribeToTopic(roomId)
                                .addOnSuccessListener(aVoid -> {
                                    startActivity(new Intent(getContext(), ChatActivity.class));
                                });
                    });
                }
                else {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }
            }
        };
        adapter.startListening();
        binding.recyclerPeople.setAdapter(adapter);
    }

    private void initView(View itemView) {
        binding = PeopleFragmentBinding.bind(itemView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerPeople.setLayoutManager(layoutManager);
        binding.recyclerPeople.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(PeopleViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onStart() {
        super.onStart();
        if(adapter!= null) adapter.startListening();
    }

    @Override
    public void onStop() {
        if(adapter!= null) adapter.stopListening();
        super.onStop();
    }
}