package com.example.budgetmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("User Details");
        userID = user.getUid();


        final TextView NameofAccount = (TextView) findViewById(R.id.accountname);
        final TextView EmailofAccount = (TextView) findViewById(R.id.accountemail);
        final TextView AgeoftheUser = (TextView) findViewById(R.id.accountage);
        final TextView DateCreated = (TextView) findViewById(R.id.date);
        final TextView IDuser = (TextView) findViewById(R.id.nouser);

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if(userProfile != null){
                    String emailString = userProfile.email;
                    String userString = userProfile.username;
                    String ageString = userProfile.age;
                    String dateString =userProfile.date;

                    NameofAccount.setText("Email: " + userString);
                    EmailofAccount.setText("Username: "+ emailString);
                    AgeoftheUser.setText("Age: "+ageString);
                    DateCreated.setText("Date Created: "+dateString);
                    IDuser.setText("User ID: "+userID);

                }
                else{
                    Toast.makeText(ProfileActivity.this, "Something wrong happened!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}