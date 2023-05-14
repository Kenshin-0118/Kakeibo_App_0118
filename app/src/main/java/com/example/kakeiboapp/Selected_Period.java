package com.example.kakeiboapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class Selected_Period extends AppCompatActivity  implements RecycleviewInterface{
    TextView Period_head;
    ArrayList<Spends> SpendList = new ArrayList<>();
    ArrayList<Spends> AllRecordList = new ArrayList<>();
    RecyclerView recycleview;
    SpendAdapter spendAdapter;
    FirebaseFirestore db;
    ProgressDialog progressDialog;
    ImageView all, food, house, travel, health, entertainment, clothing, education, others;
    String Period = "???";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_period);

        Intent intent = getIntent();
        Period = intent.getStringExtra("period_key");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data.....");
        progressDialog.show();

        Period_head = findViewById(R.id.selected);
        db = FirebaseFirestore.getInstance();

        all = findViewById(R.id.category_all);
        food = findViewById(R.id.category_food);
        house = findViewById(R.id.category_house);
        travel = findViewById(R.id.category_traveler);
        health = findViewById(R.id.category_heart);
        entertainment = findViewById(R.id.category_entertain);
        clothing = findViewById(R.id.category_clothing);
        education = findViewById(R.id.category_education);
        others = findViewById(R.id.category_others);

        Button close = findViewById(R.id.close);
        close.setOnClickListener(view ->finish());
        Period_head.setText(Period);

        recycleview = findViewById(R.id.history_view);
        recycleview.setHasFixedSize(true);
        recycleview.setLayoutManager(new LinearLayoutManager(this));
        SpendList = new ArrayList<>();
        spendAdapter = new SpendAdapter(Selected_Period.this, SpendList,this);

        recycleview.setAdapter(spendAdapter);
        FetchSpends();

        all.setOnClickListener(view -> {String category = "All Category";FilterByCategory(category);});
        food.setOnClickListener(view -> {String category = "Food and Dining";FilterByCategory(category);});
        house.setOnClickListener(view -> {String category = "Housing and Utilities";FilterByCategory(category);});
        travel.setOnClickListener(view -> {String category = "Transportation and Travel";FilterByCategory(category);});
        health.setOnClickListener(view -> {String category = "Personal Care and Health";FilterByCategory(category);});
        entertainment.setOnClickListener(view -> {String category = "Entertainment and Recreation";FilterByCategory(category);});
        clothing.setOnClickListener(view -> {String category = "Clothing and Accessories";FilterByCategory(category);});
        education.setOnClickListener(view -> {String category = "Education and Learning";FilterByCategory(category);});
        others.setOnClickListener(view -> {String category = "Others";FilterByCategory(category);});

    }
    private void FilterByCategory(String category) {
        ArrayList<Spends> filteredList = new ArrayList<>();
        for (Spends spend : AllRecordList) {
            if (spend.getCategory().equals(category)) {
                filteredList.add(spend);
            }
        }
        if (category != "All Category") {
            SpendList.clear();
            SpendList.addAll(filteredList);
            spendAdapter.notifyDataSetChanged();
        }else{
            SpendList.clear();
            SpendList.addAll(AllRecordList);
            spendAdapter.notifyDataSetChanged();
        }

    }
    public void FetchSpends(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("Spends")
                .whereEqualTo("User_Id", uid)
                .whereEqualTo("Period", Period)
                .orderBy("Created", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Log.e("Firestore Error", error.getMessage());
                        return;
                    }

                    if (value != null) { // Add null check here
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String category = dc.getDocument().getString("Category");
                                String amount = dc.getDocument().getString("Amount");
                                String description = dc.getDocument().getString("Description");
                                Date created = dc.getDocument().getDate("Created");
                                Spends spends= new Spends(description,amount,category,created);
                                SpendList.add(spends);
                                AllRecordList.add(spends);
                            }
                            spendAdapter.notifyDataSetChanged();
                        }
                    }

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
    }
    protected void onStart() {
        super.onStart();

        // Check internet connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            LayoutInflater inflater = getLayoutInflater();
            View customLayout = inflater.inflate(R.layout.no_internet_layout, null);

            // Find views in the custom layout
            Button Ok = customLayout.findViewById(R.id.Ok);
            // Create and show the AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(Selected_Period.this, R.style.TransparentDialog);
            builder.setView(customLayout);
            AlertDialog dialog = builder.create();

            // Add a click listener to the "Yes" button
            Ok.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        }
    }

    @Override
    public void OnItemClick(int position) {
        LayoutInflater inflater = getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.spend_item_layout, null);

        // Find views in the custom layout
        CircleImageView circleImageView = customLayout.findViewById(R.id.profile_image);
        TextView Category = customLayout.findViewById(R.id.category_food);
        TextView Amount = customLayout.findViewById(R.id.food_amount);
        TextView Description = customLayout.findViewById(R.id.description);
        TextView Datetime = customLayout.findViewById(R.id.datetime);

        switch (SpendList.get(position).getCategory()) {
            case "Food and Dining":
                circleImageView.setImageResource(R.drawable.category_food);
                break;
            case "Housing and Utilities":
                circleImageView.setImageResource(R.drawable.category_house);
                break;
            case "Transportation and Travel":
                circleImageView.setImageResource(R.drawable.category_traveler);
                break;
            case "Personal Care and Health":
                circleImageView.setImageResource(R.drawable.category_heart);
                break;
            case "Entertainment and Recreation":
                circleImageView.setImageResource(R.drawable.category_entertain);
                break;
            case "Clothing and Accessories":
                circleImageView.setImageResource(R.drawable.category_hoodie);
                break;
            case "Education and Learning":
                circleImageView.setImageResource(R.drawable.category_book);
                break;
            case "Others":
                circleImageView.setImageResource(R.drawable.category_others);
                break;
            default:
                circleImageView.setImageResource(R.drawable.category_all);
                break;
        }
        DecimalFormat decimalFormat = new DecimalFormat("₱#,##0.00");
        // Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Selected_Period.this, R.style.TransparentDialog);
        builder.setView(customLayout);
        AlertDialog dialog = builder.create();
        Category.setText(SpendList.get(position).getCategory());
        Amount.setText(decimalFormat.format(Double.parseDouble(SpendList.get(position).getAmount())));
        Description.setText(SpendList.get(position).getDescription());
        Datetime.setText(SpendList.get(position).getCreatedAsString());
        dialog.show();
    }
}