package com.example.budgetmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.util.Calendar;

public class RegistrationActivity extends AppCompatActivity {

    private EditText email, password, regusername, regage;
    private Button registerbtn;
    private TextView registerQn;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        getSupportActionBar().hide();

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        registerbtn = (Button) findViewById(R.id.registerbtn);
        registerQn = (TextView) findViewById(R.id.registerQn);
        regusername = (EditText) findViewById(R.id.username);
        regage = (EditText) findViewById(R.id.age);


        mAuth= FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailString = email.getText().toString();
                String passwordString = password.getText().toString();
                String userString = regusername.getText().toString();
                String ageString = regage.getText().toString();

                if (TextUtils.isEmpty(userString)) {
                    regusername.setError("Username is required");
                }

                if (TextUtils.isEmpty(emailString)) {
                    email.setError("Email is required");
                }

                if (TextUtils.isEmpty(ageString)) {
                    regage.setError("Age is required");
                }


                if (TextUtils.isEmpty(passwordString)) {
                    password.setError("Password is required");
                }

                if(password.length()<6)
                {
                    password.setError("Must contain at least 6 characters..");
                }

                else {
                    progressDialog.setMessage("Registration in Progress");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    mAuth.createUserWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                DateFormat dateformat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                                Calendar cal = Calendar.getInstance();
                                String date = dateformat.format(cal.getTime());

                                MutableDateTime epoch = new MutableDateTime();
                                epoch.setDate(0);
                                DateTime now = new DateTime();
                                Months months = Months.monthsBetween(epoch, now);

                                User user = new User(userString, ageString, emailString, date);

                                FirebaseUser mUser=mAuth.getCurrentUser();
                                String uid = mUser.getUid();
                                FirebaseDatabase.getInstance().getReference("User Details").child(uid).setValue(user);
                                progressDialog.dismiss();
                                sendEmailVerification();
                                email= findViewById(R.id.email);


                            } else {
                                Toast.makeText(RegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });


        registerQn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void sendEmailVerification() {
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if(firebaseUser!=null)
        {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(RegistrationActivity.this,"Registration Successful.Verification mail sent successfully..",Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        finish();
                        startActivity(new Intent(RegistrationActivity.this,LoginActivity.class));
                    }
                    else
                    {
                        Toast.makeText(RegistrationActivity.this,"Error occurred sending verification mail..",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }


                }
            });
        }
    }
}