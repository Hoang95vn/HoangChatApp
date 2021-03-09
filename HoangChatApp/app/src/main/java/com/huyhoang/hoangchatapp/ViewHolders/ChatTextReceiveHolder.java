package com.huyhoang.hoangchatapp.ViewHolders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huyhoang.hoangchatapp.databinding.LayoutMessageTextFriendBinding;
import com.huyhoang.hoangchatapp.databinding.LayoutMessageTextOwnBinding;

public class ChatTextReceiveHolder extends RecyclerView.ViewHolder {
    public LayoutMessageTextFriendBinding binding;
    public ChatTextReceiveHolder(@NonNull View itemView) {
        super(itemView);
        binding = LayoutMessageTextFriendBinding.bind(itemView);
    }
}
