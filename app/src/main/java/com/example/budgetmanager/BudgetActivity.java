package com.example.budgetmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BudgetActivity extends AppCompatActivity {

    private TextView totalExpenses, totalBudget;
    private RecyclerView recyclerView;

    private FloatingActionButton search;
    private Button totalBdgtBtn;
    private ImageButton fab, logout;

    private DatabaseReference budgetRef, totalBudgetRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    private String post_key = "";
    private String item = "";
    private  int amount = 0;

    private String note = "";

    private EditText newsearch;

    ArrayList<Data> arrayList;

    private ListView listdata;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        mAuth = FirebaseAuth.getInstance();
        budgetRef = FirebaseDatabase.getInstance().getReference().child("budget").child(mAuth.getCurrentUser().getUid());
        totalBudgetRef = FirebaseDatabase.getInstance().getReference().child("user").child(mAuth.getCurrentUser().getUid());
        loader = new ProgressDialog(this);

        recyclerView = findViewById(R.id.recyclerView);

        ArrayList<Data> arrayList;

        getSupportActionBar().hide();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        totalBudget = findViewById(R.id.budgetAmount);
        totalBudgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (snap != null) {
                        int totalBudgetAmount = 0;
                        com.example.budgetmanager.totalBudget budget = snap.getValue(com.example.budgetmanager.totalBudget.class);
                        totalBudgetAmount += budget.getTotalBudget();
                        totalBudget.setText(String.valueOf(totalBudgetAmount));
                    } else {
                        int totalBudgetAmount = 0;
                        totalBudget.setText(String.valueOf(totalBudgetAmount));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        totalExpenses = findViewById(R.id.expenseAmount);
        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmount = 0;
                if(snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Data data = snap.getValue(Data.class);
                        totalAmount += data.getAmount();
                        totalExpenses.setText(String.valueOf(totalAmount));
                        if (totalAmount > Integer.parseInt(totalBudget.getText().toString())) {
                            totalExpenses.setTextColor(getResources().getColor(R.color.red));
                        } else {
                            totalExpenses.setTextColor(getResources().getColor(R.color.black));
                        }
                    }
                }
                else{
                    totalExpenses.setText("0.00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        totalBdgtBtn = findViewById(R.id.addBudget);

        totalBdgtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBudget();
            }
        });

        logout = findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(BudgetActivity.this)
                        .setMessage("Are you sure you want to log out?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAuth.signOut();
                                Toast.makeText(BudgetActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
                                Intent logout = new Intent(BudgetActivity.this, LoginActivity.class);
                                logout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(logout);
                                finish();
                            }
                        })
                        .setNegativeButton("No",null)
                        .show();
            }
        });

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                additem();
            }
        });

        search = findViewById(R.id.search);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BudgetActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void search(String newsearch) {

        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();

        if(mAuth!=null)
        {String uid = mUser.getUid();

            //mref= FirebaseDatabase.getInstance().getReference("IncomeData");
            Query query = FirebaseDatabase.getInstance().getReference().child("budget").child(uid).orderByChild("item")
                    .startAt(newsearch).endAt(newsearch + "\uf8ff");


            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild("")){
                        arrayList.clear();
                        for (DataSnapshot dss: snapshot.getChildren()){
                            final Data data = dss.getValue(Data.class);
                            arrayList.add(data);
                        }


                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }


    private void addBudget() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this, R.style.CustomDialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_budget, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText amount = myView.findViewById(R.id.amountBdgt);
        final Button cancel = myView.findViewById(R.id.cancelBdgt);
        final Button save = myView.findViewById(R.id.saveBdgt);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String budgetAmount = amount.getText().toString();

                if (TextUtils.isEmpty(budgetAmount)) {
                    amount.setError("Budget Amount is Required");
                    amount.requestFocus();
                }
                else {
                    loader.setMessage("Setting up!");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    com.example.budgetmanager.totalBudget budget = new totalBudget(Integer.parseInt(budgetAmount));

                    totalBudgetRef.child("totalBudget").setValue(budget).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(BudgetActivity.this, "Total budget has been set.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }

                            loader.dismiss();
                        }
                    });
                }

                dialog.dismiss();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void additem() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this, R.style.CustomDialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final Spinner itemspinner = myView.findViewById(R.id.itemspinner);
        final EditText amount = myView.findViewById(R.id.amount);
        final Button cancel = myView.findViewById(R.id.cancel);
        final Button save = myView.findViewById(R.id.save);
        final EditText notes = myView.findViewById(R.id.notes);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String budgetAmount = amount.getText().toString();
                String budgetItem = itemspinner.getSelectedItem().toString();
                String budgetNote = notes.getText().toString();

                if (TextUtils.isEmpty(budgetAmount)) {
                    Toast.makeText(BudgetActivity.this, "Amount is Required", Toast.LENGTH_SHORT).show();
                    amount.setError("Amount is Required");
                }
                else if (TextUtils.isEmpty(budgetNote)) {
                    Toast.makeText(BudgetActivity.this, "Notes is Required", Toast.LENGTH_SHORT).show();
                    notes.setError("Notes is Required");
                }
                else if (budgetItem.equals("Select Category")) {
                    Toast.makeText(BudgetActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                }
                else {
                    loader.setMessage("adding a budget item");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String id = budgetRef.push().getKey();
                    DateFormat dateformat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                    Calendar cal = Calendar.getInstance();
                    String date = dateformat.format(cal.getTime());

                    MutableDateTime epoch = new MutableDateTime();
                    epoch.setDate(0);
                    DateTime now = new DateTime();
                    Months months = Months.monthsBetween(epoch, now);


                    Data data = new Data(budgetItem, date, id, budgetNote, Integer.parseInt(budgetAmount), months.getMonths());
                    budgetRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(BudgetActivity.this, "Budget item added successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }

                            loader.dismiss();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateRecycler();
    }

    private void updateRecycler() {
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(budgetRef, Data.class)
                .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Data model)
            {

                holder.setItemAmount("₱" + model.getAmount());
                holder.setDate("Date: " +model.getDate());
                holder.setItemName(model.getItem());
                holder.setItemNote("Note: " +model.getNotes());

                switch (model.getItem()){

                    case "Transport":
                        holder.imageview.setImageResource(R.drawable.transport);
                        break;

                    case "Food":
                        holder.imageview.setImageResource(R.drawable.food);
                        break;

                    case "House":
                        holder.imageview.setImageResource(R.drawable.house);
                        break;

                    case "Entertainment":
                        holder.imageview.setImageResource(R.drawable.entertainment);
                        break;

                    case "Education":
                        holder.imageview.setImageResource(R.drawable.education);
                        break;

                    case "Charity":
                        holder.imageview.setImageResource(R.drawable.charity);
                        break;

                    case "Apparel":
                        holder.imageview.setImageResource(R.drawable.apparel);
                        break;

                    case "Health":
                        holder.imageview.setImageResource(R.drawable.health);
                        break;

                    case "Personal":
                        holder.imageview.setImageResource(R.drawable.personal);
                        break;

                    case "Other":
                        holder.imageview.setImageResource(R.drawable.other);
                        break;
                }

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        post_key =  getRef(position).getKey();
                        item = model.getItem();
                        amount =model.getAmount();
                        note = model.getNotes();
                        updateData();

                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();
    }

    //UPDATE
    private void updateData() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this, R.style.CustomDialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.update_layout, null);

        myDialog.setView(mView);
        final AlertDialog dialog =myDialog.create();

        final TextView mItem= mView.findViewById(R.id.itemname);
        final EditText mAmount= mView.findViewById(R.id.amount);
        final EditText mNote= mView.findViewById(R.id.notes);

        mItem.setText(item);
        mNote.setText(String.valueOf(note));
        mNote.setSelection(String.valueOf(note).length());
        mAmount.setText(String.valueOf(amount));
        mAmount.setSelection(String.valueOf(amount).length());

        Button delbut =mView.findViewById(R.id.deletebtn);
        Button upbut = mView.findViewById(R.id.updatebtn);

        upbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                amount= Integer.parseInt(mAmount.getText().toString());
                String note = mNote.getText().toString();

                DateFormat dateformat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                Calendar cal = Calendar.getInstance();
                String date = dateformat.format(cal.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = new DateTime();
                Months months = Months.monthsBetween(epoch, now);

                Data data = new Data(item, date, post_key, note, amount, months.getMonths());
                budgetRef.child(post_key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(BudgetActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.dismiss();
            }
        });

        delbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                budgetRef.child(post_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(BudgetActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            updateRecycler();
                        } else {
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public ImageView imageview;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            imageview = itemView.findViewById(R.id.imageview);

        }

        public void setItemNote (String itemNote){
            TextView note = mView.findViewById(R.id.note);
            note.setText(itemNote);
        }
        public void setItemName (String itemName){
            TextView item = mView.findViewById(R.id.item);
            item.setText(itemName);
        }

        public void setItemAmount (String itemAmount){
            TextView amount = mView.findViewById(R.id.amount);
            amount.setText(itemAmount);
        }

        public void setDate (String itemDate){
            TextView date = mView.findViewById(R.id.date);
            date.setText(itemDate);
        }
    }
}