package com.huyhoang.hoangchatapp.ViewHolders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huyhoang.hoangchatapp.databinding.LayoutMessagePictureFriendBinding;
import com.huyhoang.hoangchatapp.databinding.LayoutMessagePictureOwnBinding;

public class ChatPictureReceiveHolder extends RecyclerView.ViewHolder {
    public LayoutMessagePictureFriendBinding binding;

    public ChatPictureReceiveHolder(@NonNull View itemView) {
        super(itemView);
        binding = LayoutMessagePictureFriendBinding.bind(itemView);
    }
}
