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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Selected_Category extends AppCompatActivity implements RecycleviewInterface{

    ArrayList<Selected> SelectedList = new ArrayList<>();
    RecyclerView recycleview;
    SelectedAdapter selectedAdapter;
    FirebaseFirestore db;
    ProgressDialog progressDialog;
    TextView Head;
    Button Close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_category);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data.....");
        progressDialog.show();

        Head = findViewById(R.id.selected_head);
        Intent intent = getIntent();
        String Category = intent.getStringExtra("category_key");

        db = FirebaseFirestore.getInstance();

        Close = findViewById(R.id.close);

        Close.setOnClickListener(view -> finish());


        recycleview = findViewById(R.id.history_view);
        recycleview.setHasFixedSize(true);
        recycleview.setLayoutManager(new LinearLayoutManager(this));
        SelectedList= new ArrayList<>();
        selectedAdapter = new SelectedAdapter(Selected_Category.this, SelectedList,this);

        recycleview.setAdapter(selectedAdapter);
        FetchSelected(Category);

        switch (Category) {
            case "Food and Dining":
                Head.setText("Food and Dining");
                break;
            case "Housing and Utilities":
                Head.setText("Housing and Utilities");
                break;
            case "Transportation and Travel":
                Head.setText("Transportation and Travel");
                break;
            case "Personal Care and Health":
                Head.setText("Personal Care and Health");
                break;
            case "Entertainment and Recreation":
                Head.setText("Entertainment and Recreation");
                break;
            case "Clothing and Accessories":
                Head.setText("Clothing and Accessories");
                break;
            case "Education and Learning":
                Head.setText("Education and Learning");
                break;
            case "Others":
                Head.setText("Others");
                break;
            default:
                Head.setText("Unidentified Expenses");
                break;
        }
    }

    public void FetchSelected(String Category){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        String monthYear = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        db.collection("Spends")
                .whereEqualTo("User_Id", uid)
                .whereEqualTo("Period", monthYear)
                .whereEqualTo("Category", Category)
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
                                Selected selected= new Selected(description,amount,category,created);
                                SelectedList.add(selected);
                            }
                            selectedAdapter.notifyDataSetChanged();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(Selected_Category.this, R.style.TransparentDialog);
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

        switch (SelectedList.get(position).getCategory()) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(Selected_Category.this, R.style.TransparentDialog);
        builder.setView(customLayout);
        AlertDialog dialog = builder.create();
        Category.setText(SelectedList.get(position).getCategory());
        Amount.setText(decimalFormat.format(Double.parseDouble(SelectedList.get(position).getAmount())));
        Description.setText(SelectedList.get(position).getDescription());
        Datetime.setText(SelectedList.get(position).getCreatedAsString());
        dialog.show();
    }
}