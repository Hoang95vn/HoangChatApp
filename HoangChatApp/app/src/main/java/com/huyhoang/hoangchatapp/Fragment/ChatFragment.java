package com.huyhoang.hoangchatapp.Fragment;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.huyhoang.hoangchatapp.ChatActivity;
import com.huyhoang.hoangchatapp.Common.Common;
import com.huyhoang.hoangchatapp.Model.ChatInfoModel;
import com.huyhoang.hoangchatapp.Model.UserModel;
import com.huyhoang.hoangchatapp.R;
import com.huyhoang.hoangchatapp.ViewHolders.ChatInfoHolder;
import com.huyhoang.hoangchatapp.databinding.ChatFragmentBinding;

import java.text.SimpleDateFormat;

public class ChatFragment extends Fragment {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    FirebaseRecyclerAdapter adapter;

    private ChatFragmentBinding binding;

    private ChatViewModel mViewModel;

    static ChatFragment instance;

    public static ChatFragment getInstance(){
        return instance == null ? new ChatFragment() : instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ChatFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        InitView(view);
        loadChatList();
        return view;
    }

    private void loadChatList() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(Common.CHAT_LIST_REFERENCES)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseRecyclerOptions<ChatInfoModel> options =  new FirebaseRecyclerOptions
                .Builder<ChatInfoModel>()
                .setQuery(query, ChatInfoModel.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<ChatInfoModel, ChatInfoHolder>(options) {

            @NonNull
            @Override
            public ChatInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_chat_item, parent, false);
                return new ChatInfoHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatInfoHolder holder, int position, @NonNull ChatInfoModel model) {
                if(!adapter.getRef(position)
                .getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                    // Ẩn bản thân\
                    ColorGenerator generator = ColorGenerator.MATERIAL;
                    int color = generator.getColor(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    TextDrawable.IBuilder builder = TextDrawable.builder()
                            .beginConfig()
                            .withBorder(4)
                            .endConfig()
                            .round();
                    String displayName = FirebaseAuth.getInstance().getCurrentUser().getUid()
                            .equals(model.getCreateId()) ? model.getFiendName() : model.getCreateName();

                    TextDrawable drawable = builder.build(displayName.substring(0,1),color);
                    holder.binding.imgAvatar.setImageDrawable(drawable);

                    holder.binding.txtName.setText(displayName);
                    holder.binding.txtLastMessage.setText(model.getLastMessage());
                    holder.binding.txtTime.setText(simpleDateFormat.format(model.getLastUpdate()));

                    holder.itemView.setOnClickListener(view -> {

                        //Đi đến chi tiết cuộc trò truyện
                        FirebaseDatabase.getInstance()
                                .getReference(Common.USER_REFERENCES)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()
                                        .equals(model.getCreateId()) ?
                                        model.getFriendId() : model.getCreateId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            UserModel userModel = snapshot.getValue(UserModel.class);
                                            Common.chatUser = userModel;
                                            Common.chatUser.setUid(snapshot.getKey());

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



                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                    });

                }
                else {
                    //nếu giống key thì ẩn bản thân
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }
            }
        };
        adapter.startListening();
        binding.recyclerChat.setAdapter(adapter);
    }

    private void InitView(View view) {
        binding = ChatFragmentBinding.bind(view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerChat.setLayoutManager(layoutManager);
        binding.recyclerChat.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
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