package com.huyhoang.hoangchatapp.ViewHolders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huyhoang.hoangchatapp.databinding.LayoutPeopleBinding;


public class UserViewHolder extends RecyclerView.ViewHolder {

    public LayoutPeopleBinding binding;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = LayoutPeopleBinding.bind(itemView);
    }
}
