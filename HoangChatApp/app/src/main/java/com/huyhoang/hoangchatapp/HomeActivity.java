package com.huyhoang.hoangchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.messaging.FirebaseMessaging;
import com.huyhoang.hoangchatapp.Adapter.MyViewPagerAdapter;
import com.huyhoang.hoangchatapp.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        init();
        setUpViewPager();



    }

    private void setUpViewPager() {
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager(), new Lifecycle() {
            @Override
            public void addObserver(@NonNull LifecycleObserver observer) {

            }

            @Override
            public void removeObserver(@NonNull LifecycleObserver observer) {

            }

            @NonNull
            @Override
            public State getCurrentState() {
                return null;
            }
        }));
        new TabLayoutMediator(binding.tabDots, binding.viewPager, (tab, position) -> {
            if(position == 0){
                tab.setText("Trò chuyện");
            }
            else {
                tab.setText("Mọi người");
            }
        }).attach();
    }

    private void init() {

    }
}