package com.huyhoang.hoangchatapp.ViewHolders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huyhoang.hoangchatapp.databinding.LayoutChatItemBinding;

public class ChatInfoHolder extends RecyclerView.ViewHolder {

    public LayoutChatItemBinding binding;

    public ChatInfoHolder(@NonNull View itemView) {
        super(itemView);
        binding = LayoutChatItemBinding.bind(itemView);
    }
}
