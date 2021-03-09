package com.huyhoang.hoangchatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.huyhoang.hoangchatapp.Common.Common;
import com.huyhoang.hoangchatapp.Listener.IFirebaseLoadFailed;
import com.huyhoang.hoangchatapp.Listener.ILoadTimeFromFirebaseListener;
import com.huyhoang.hoangchatapp.Model.ChatInfoModel;
import com.huyhoang.hoangchatapp.Model.ChatMessageModel;
import com.huyhoang.hoangchatapp.ViewHolders.ChatPictureHolder;
import com.huyhoang.hoangchatapp.ViewHolders.ChatPictureReceiveHolder;
import com.huyhoang.hoangchatapp.ViewHolders.ChatTextHolder;
import com.huyhoang.hoangchatapp.ViewHolders.ChatTextReceiveHolder;
import com.huyhoang.hoangchatapp.databinding.ActivityChatBinding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ChatActivity extends AppCompatActivity implements ILoadTimeFromFirebaseListener, IFirebaseLoadFailed {

    private static final int MY_CAMERA_REQUEST_CODE = 7171;
    private static final int MY_RESULTS_LOAD_IMAGE = 7172;

    private ActivityChatBinding binding;


    FirebaseDatabase database;
    DatabaseReference chatRef, offsetRef;

    ILoadTimeFromFirebaseListener listener;
    IFirebaseLoadFailed errorListener;

    FirebaseRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder> adapter;
    FirebaseRecyclerOptions<ChatMessageModel> options;

    Uri fileUri;
    StorageReference storageReference;

    LinearLayoutManager layoutManager;


    public void onSelectImageClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, MY_RESULTS_LOAD_IMAGE);
    }


    public void onCaptureImageClick(View view) {
        ContentValues contentValues =  new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"New Picture");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"From your Camera");
        fileUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        );
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, MY_CAMERA_REQUEST_CODE);
    }


    public void onSubmitChatClick(View view) {
        //Toast.makeText(this, "Click được ảnh", Toast.LENGTH_SHORT).show();
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeInMs = System.currentTimeMillis() + offset;

                listener.onLoadOnlyTimeSuccess(estimatedServerTimeInMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorListener.onError(error.getMessage());
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MY_CAMERA_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK) {
                try {
                    Bitmap thumbail = MediaStore.Images.Media.getBitmap(
                            getContentResolver(),
                            fileUri
                    );
                    binding.imgReview.setImageBitmap(thumbail);
                    binding.imgReview.setVisibility(View.VISIBLE);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode == MY_RESULTS_LOAD_IMAGE)
        {
            if(resultCode == RESULT_OK){
                try {
                    final Uri imageUri =  data.getData();
                    InputStream inputStream = getContentResolver()
                            .openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                    binding.imgReview.setImageBitmap(selectedImage);
                    binding.imgReview.setVisibility(View.VISIBLE);
                    fileUri = imageUri;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        else
            Toast.makeText(this, "Xin hãy chọn ảnh", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        if(adapter!=null) adapter.stopListening();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null) adapter.startListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        initView();
        loadChatContent();
    }

    private void loadChatContent() {
        String receiverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new FirebaseRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder>(options) {
            @Override
            public int getItemViewType(int position) {
                if(adapter.getItem(position).getSenderId().equals(receiverId)){
                    // nếu tin nhắn của ta
                    return !adapter.getItem(position).isPicture()?0:1;
                }
                else return !adapter.getItem(position).isPicture()?2:3;
            }

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull ChatMessageModel model) {
                if(holder instanceof ChatTextHolder){
                    ChatTextHolder chatTextHolder = (ChatTextHolder) holder;
                    chatTextHolder.binding.txtChatMessage.setText(model.getContent());
                    chatTextHolder.binding.txtTime.setText(
                            DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                                    Calendar.getInstance().getTimeInMillis(),0)
                            .toString());

                }
                else if(holder instanceof ChatTextReceiveHolder){
                    ChatTextReceiveHolder chatTextHolder = (ChatTextReceiveHolder) holder;
                    chatTextHolder.binding.txtChatMessage.setText(model.getContent());
                    chatTextHolder.binding.txtTime.setText(
                            DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                                    Calendar.getInstance().getTimeInMillis(),0)
                                    .toString());
                }
                else if(holder instanceof ChatPictureHolder){
                    ChatPictureHolder chatPictureHolder = (ChatPictureHolder) holder;
                    chatPictureHolder.binding.txtChatMessage.setText(model.getContent());
                    chatPictureHolder.binding.txtTime.setText(
                            DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                                    Calendar.getInstance().getTimeInMillis(),0)
                                    .toString());
                    Glide.with(ChatActivity.this)
                            .load(model.getPictureLink())
                            .into(chatPictureHolder.binding.imgReview);
                }
                else if(holder instanceof ChatPictureReceiveHolder){
                    ChatPictureReceiveHolder chatPictureHolder = (ChatPictureReceiveHolder) holder;
                    chatPictureHolder.binding.txtChatMessage.setText(model.getContent());
                    chatPictureHolder.binding.txtTime.setText(
                            DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                                    Calendar.getInstance().getTimeInMillis(),0)
                                    .toString());
                    Glide.with(ChatActivity.this)
                            .load(model.getPictureLink())
                            .into(chatPictureHolder.binding.imgReview);
                }

            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;
                if(viewType == 0) // tin nhắn của bản thân
                {
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.layout_message_text_own, parent, false);
                    return new ChatTextReceiveHolder(view);
                }
                else if (viewType == 1) // tin nhắn ảnh của bản thân
                {
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.layout_message_picture_friend, parent, false);
                    return new ChatPictureReceiveHolder(view);
                }
                else if(viewType ==2) // tin nhắn của bạn bè
                {
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.layout_message_text_friend, parent, false);
                    return new ChatTextHolder(view);
                }
                else // tinh nhắn ảnh của bạn bè
                    {
                        view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.layout_message_picture_own, parent, false);
                        return new ChatPictureHolder(view);
                }
            }
        };

        //Tự động scroll khi nhận tin nhắn mới
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount =  adapter.getItemCount();
                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                if(lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount-1) && lastVisiblePosition == (positionStart-1))){
                    binding.recyclerChat.scrollToPosition(positionStart);

                }
            }
        });
        binding.recyclerChat.setAdapter(adapter);
    }

    private void initView() {
        listener = this;
        errorListener = this;
        database =  FirebaseDatabase.getInstance();
        chatRef = database.getReference(Common.CHAT_REFERENCES);

        offsetRef = database.getReference(".info/serverTimeOffset");

        Query query = chatRef.child(Common.generateChatRoomid(
                Common.chatUser.getUid(),
                FirebaseAuth.getInstance().getCurrentUser().getUid()
        )).child(Common.CHAT_DETAIL_REFERENCES);

        options = new FirebaseRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();
        layoutManager = new LinearLayoutManager(this);
        binding.recyclerChat.setLayoutManager(layoutManager);

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(Common.chatUser.getUid());
        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .endConfig()
                .round();
        TextDrawable drawable = builder.build(Common.chatUser.getFirstName().substring(0,1),color);
        binding.imgAvatar.setImageDrawable(drawable);
        binding.txtName.setText(Common.getName(Common.chatUser));
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        binding.toolbar.setNavigationOnClickListener(view -> {
            finish();
        });



    }

    @Override
    public void onLoadOnlyTimeSuccess(long estimateTimeInMS) {
        ChatMessageModel chatMessageModel = new ChatMessageModel();
        chatMessageModel.setName(Common.getName(Common.currentUser));
        chatMessageModel.setContent(binding.edtChat.getText().toString());
        chatMessageModel.setTimeStamp(estimateTimeInMS);
        chatMessageModel.setSenderId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if(fileUri == null) {
            chatMessageModel.setPicture(false);
            submitChatToFirebase(chatMessageModel, chatMessageModel.isPicture(), estimateTimeInMS);
        }
        else {
            uploadPicture(fileUri, chatMessageModel, estimateTimeInMS);
        }
    }

    private void uploadPicture(Uri fileUri, ChatMessageModel chatMessageModel, long estimateTimeInMS) {
        AlertDialog alertDialog = new AlertDialog.Builder(ChatActivity.this)
                .setCancelable(false)
                .setMessage("Xin hãy chờ 1 chút...")
                .create();
        alertDialog.show();

        String fileName = Common.getFileName(getContentResolver(), fileUri);
        String path = new StringBuilder(Common.chatUser.getUid())
                .append("/")
                .append(fileName)
                .toString();
        storageReference = FirebaseStorage.getInstance()
                .getReference()
                .child(path);
        UploadTask uploadTask = storageReference.putFile(fileUri);
        //Tạo task
        Task<Uri> task =  uploadTask.continueWithTask(task1 -> {
            if (!task1.isSuccessful()){
                Toast.makeText(this, "Upload thất bại", Toast.LENGTH_SHORT).show();
            }
            return storageReference.getDownloadUrl();
        }).addOnCompleteListener(task12 ->{
            if(task12.isSuccessful()){
                String url = task12.getResult().toString();
                alertDialog.dismiss();

                chatMessageModel.setPicture(true);
                chatMessageModel.setPictureLink(url);

                submitChatToFirebase(chatMessageModel, chatMessageModel.isPicture(), estimateTimeInMS);
            }
        } ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitChatToFirebase(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMS) {
        chatRef.child(Common.generateChatRoomid(Common.chatUser.getUid()
        , FirebaseAuth.getInstance().getCurrentUser().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            appendChat(chatMessageModel,isPicture, estimateTimeInMS );
                        }
                        else createChat(chatMessageModel,isPicture, estimateTimeInMS);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void appendChat(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMS) {
        Map<String,Object> update_data = new HashMap<>();
        update_data.put("lastUpdate", estimateTimeInMS);

        if(isPicture)
            update_data.put("lastMessage","<Image>");
        else
            update_data.put("lastMessage", chatMessageModel.getContent());

        //Update
        //Update trên user list
        FirebaseDatabase.getInstance()
                .getReference(Common.CHAT_LIST_REFERENCES)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Common.chatUser.getUid())
                .updateChildren(update_data)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(aVoid -> {
                    //Submit thành công chatInfoModel
                    //copy vào danh sách chat của bạn bè
                    FirebaseDatabase.getInstance()
                            .getReference(Common.CHAT_LIST_REFERENCES)
                            .child(Common.chatUser.getUid())
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(update_data)
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnSuccessListener(aVoid1 -> {
                                chatRef.child(Common.generateChatRoomid(Common.chatUser.getUid()
                                        , FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                        .child(Common.CHAT_DETAIL_REFERENCES)
                                        .push()
                                        .setValue(chatMessageModel)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    //Xóa
                                                    binding.edtChat.setText("");
                                                    binding.edtChat.requestFocus();
                                                    if(adapter!=null)
                                                        adapter.notifyDataSetChanged();

                                                    // xóa preview thumbail
                                                    if(isPicture){
                                                        fileUri = null;
                                                        binding.imgReview.setVisibility(View.GONE);
                                                    }
                                                }
                                            }
                                        });


                            });
                });

    }

    private void createChat(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMS) {
        ChatInfoModel chatInfoModel = new ChatInfoModel();
        chatInfoModel.setCreateId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        chatInfoModel.setFiendName(Common.getName(Common.chatUser));
        chatInfoModel.setFriendId(Common.chatUser.getUid());
        chatInfoModel.setCreateName(Common.getName(Common.currentUser));

        if (isPicture)
            chatInfoModel.setLastMessage("<Image>");
        else
            chatInfoModel.setLastMessage(chatMessageModel.getContent());

        chatInfoModel.setLastUpdate(estimateTimeInMS);
        chatInfoModel.setCreateDate(estimateTimeInMS);

        //Submit trên Firebase
        // Thêm vào danh sách chat của User
        FirebaseDatabase.getInstance()
                .getReference(Common.CHAT_LIST_REFERENCES)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Common.chatUser.getUid())
                .setValue(chatInfoModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(aVoid -> {
                    //Submit thành công chatInfoModel
                    //copy vào danh sách chat của bạn bè
                    FirebaseDatabase.getInstance()
                            .getReference(Common.CHAT_LIST_REFERENCES)
                            .child(Common.chatUser.getUid())
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(chatInfoModel)
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnSuccessListener(aVoid1 -> {
                                chatRef.child(Common.generateChatRoomid(Common.chatUser.getUid()
                                , FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                        .child(Common.CHAT_DETAIL_REFERENCES)
                                        .push()
                                        .setValue(chatMessageModel)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    //Xóa
                                                    binding.edtChat.setText("");
                                                    binding.edtChat.requestFocus();
                                                    if(adapter!=null)
                                                        adapter.notifyDataSetChanged();

                                                    // xóa preview thumbail
                                                    if(isPicture){
                                                        fileUri = null;
                                                        binding.imgReview.setVisibility(View.GONE);
                                                    }
                                                }
                                            }
                                        });


                            });
                });
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }



}