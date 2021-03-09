package com.huyhoang.hoangchatapp.ViewHolders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huyhoang.hoangchatapp.databinding.LayoutMessagePictureFriendBinding;
import com.huyhoang.hoangchatapp.databinding.LayoutMessagePictureOwnBinding;

public class ChatPictureHolder extends RecyclerView.ViewHolder {
    public LayoutMessagePictureOwnBinding binding;

    public ChatPictureHolder(@NonNull View itemView) {
        super(itemView);
        binding = LayoutMessagePictureOwnBinding.bind(itemView);
    }
}
