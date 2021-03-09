package com.huyhoang.hoangchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.huyhoang.hoangchatapp.Common.Common;
import com.huyhoang.hoangchatapp.Model.UserModel;
import com.huyhoang.hoangchatapp.databinding.ActivityRegisterBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class RegisterActivity extends AppCompatActivity {



    FirebaseDatabase database;
    DatabaseReference userRef;

    MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker().build();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    Calendar calendar = Calendar.getInstance();
    boolean isSelectBirthDate = false;
    private ActivityRegisterBinding binding;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        init();
        setDefaultData();
    }

    private void setDefaultData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        binding.edtPhone.setText(user.getPhoneNumber());
        binding.edtPhone.setEnabled(false);

        binding.edtDateOfBirth.setOnFocusChangeListener((view, b) -> {
            if(b){
                materialDatePicker.show(getSupportFragmentManager(),materialDatePicker.toString());
            }
        });

        binding.btnRegister.setOnClickListener(view -> {
            if(!isSelectBirthDate){
                Toast.makeText(this,"Xin hãy nhập ngày sinh",Toast.LENGTH_LONG).show();
                return;
            }

            UserModel userModel = new UserModel();
            userModel.setFirstName(binding.edtFirstName.getText().toString());
            userModel.setLastName(binding.edtLastName.getText().toString());
            userModel.setPhone(binding.edtPhone.getText().toString());
            userModel.setBio(binding.edtBio.getText().toString());
            userModel.setBirthDate(calendar.getTimeInMillis());
            userModel.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());

            userRef.child(userModel.getUid())
                    .setValue(userModel)
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Chúc mừng bạn đã đăng ký thành công",Toast.LENGTH_SHORT).show();
                        Common.currentUser = userModel;
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    });

        });
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference(Common.USER_REFERENCES);
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
           calendar.setTimeInMillis(selection);
           binding.edtDateOfBirth.setText(simpleDateFormat.format(selection));
           isSelectBirthDate = true;
        });

    }
}