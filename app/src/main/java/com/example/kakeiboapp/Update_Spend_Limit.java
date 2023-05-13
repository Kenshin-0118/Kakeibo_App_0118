package com.example.kakeiboapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Update_Spend_Limit extends AppCompatActivity {


    EditText Default_limit,Set_amountLimit;
    Button Set_Limit;
    ImageView Back;
    ProgressBar progressBar;
    FirebaseFirestore db;
    String Limit_Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_spend_limit);
        Set_amountLimit = findViewById(R.id.set_limit_amount);
        Set_Limit = findViewById(R.id.set_limit);
        Default_limit = findViewById(R.id.spend_value);
        Back = findViewById(R.id.back);
        progressBar = findViewById(R.id.progressBar);
        db = FirebaseFirestore.getInstance();

        InputFilter decimalFilter = (source, start, end, dest, dstart, dend) -> {
            // Only allow digits, a single decimal point, and at most 2 decimal places
            String input = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
            if (input.isEmpty()) {
                return null; // Empty input is allowed
            } else if (input.equals(".")) {
                return "0."; // Add leading zero if input starts with a decimal point
            } else if (!input.matches("^\\d+(\\.\\d{0,2})?$")) {
                return ""; // Reject invalid input
            }
            return null; // Accept valid input
        };
        Set_amountLimit.setFilters(new InputFilter[]{decimalFilter});
        Set_Limit.setOnClickListener(view -> {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                LayoutInflater inflater = getLayoutInflater();
                View customLayout = inflater.inflate(R.layout.no_internet_layout, null);

                // Find views in the custom layout
                Button Ok = customLayout.findViewById(R.id.Ok);
                // Create and show the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Update_Spend_Limit.this);
                builder.setView(customLayout);
                AlertDialog dialog = builder.create();

                // Add a click listener to the "Yes" button
                Ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }else{
                if (Set_amountLimit.getText().toString().equals("")){displayToast("Fill Spending Limit Field"); return;}
                else{Update_Current_Limit();}

            }
        });
        Back.setOnClickListener(view -> finish());
    }

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    protected void onStart() {
        super.onStart();
        Fetch_Data();

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
            AlertDialog.Builder builder = new AlertDialog.Builder(Update_Spend_Limit.this);
            builder.setView(customLayout);
            AlertDialog dialog = builder.create();

            // Add a click listener to the "Yes" button
            Ok.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        }
    }

    public void Fetch_Data() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        String monthYear = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        Query query = db.collection("Spend_limit").whereEqualTo("User_Id", uid).whereEqualTo("Period", monthYear);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                String SP = documentSnapshot.getString("Spend_Limit");
                DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
                double Spend_Limit = Double.parseDouble(SP);
                Default_limit.setText((decimalFormat.format(Spend_Limit)));
                Limit_Id = documentSnapshot.getId();
            } else {
                Toast.makeText(getApplicationContext(), "Empty result", Toast.LENGTH_SHORT).show();
            }

        }).addOnFailureListener(e -> {
            displayToast("Spend Max Query Failed");
        });
    }

    public void Update_Current_Limit(){
        Set_Limit.setEnabled(false);
        String valueAsString = Set_amountLimit.getText().toString();
        double value = Double.parseDouble(valueAsString);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        progressBar.setVisibility(View.VISIBLE);

        DocumentReference documentReference = db.collection("Spend_limit").document(Limit_Id);
        Map<String, Object> updates = new HashMap<>();
        updates.put("Spend_Limit", decimalFormat.format(value));

        documentReference.update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    displayToast("Document added successfully");
                    Intent resultIntent = new Intent();
                    int resultCode = RESULT_OK; // set result code to indicate success
                    setResult(resultCode, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "Error adding document", e);
                    displayToast("Error adding document");
                    Set_Limit.setEnabled(true);
                });

    }

}