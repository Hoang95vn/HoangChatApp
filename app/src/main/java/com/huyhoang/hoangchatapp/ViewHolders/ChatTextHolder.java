package com.huyhoang.hoangchatapp.ViewHolders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huyhoang.hoangchatapp.databinding.LayoutMessageTextFriendBinding;
import com.huyhoang.hoangchatapp.databinding.LayoutMessageTextOwnBinding;

public class ChatTextHolder extends RecyclerView.ViewHolder {
    public LayoutMessageTextOwnBinding binding;

    public ChatTextHolder(@NonNull View itemView) {
        super(itemView);
        binding = LayoutMessageTextOwnBinding.bind(itemView);
    }
}
