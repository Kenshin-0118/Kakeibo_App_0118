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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Set_Spend_Limit extends AppCompatActivity {

    EditText Set_amountLimit;
    Button Set_Limit;
    ProgressBar progressBar;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_spend_limit);
        Set_amountLimit = findViewById(R.id.set_limit_amount);
        Set_Limit = findViewById(R.id.set_limit);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(Set_Spend_Limit.this);
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
                else{Set_Current_Limit();}

            }
        });
    }

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(Set_Spend_Limit.this);
            builder.setView(customLayout);
            AlertDialog dialog = builder.create();

            // Add a click listener to the "Yes" button
            Ok.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        }
    }

    public void Set_Current_Limit(){
        Set_Limit.setEnabled(false);
        String valueAsString = Set_amountLimit.getText().toString();
// Convert the String value to a double
        double value = Double.parseDouble(valueAsString);
// Creaet a DecimalFormat object to format the value with two decimal places
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        String monthYear = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        Map<String, Object> data = new HashMap<>();
        data.put("Period", monthYear);
        data.put("User_Id", uid);
        data.put("Year", String.valueOf(year));
        data.put("Spend_Limit", decimalFormat.format(value));
        data.put("Created", FieldValue.serverTimestamp());

        db.collection("Spend_limit")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    displayToast("Spend limit have been set");
                    Intent resultIntent = new Intent();
                    int resultCode = RESULT_OK; // set result code to indicate success
                    setResult(resultCode, resultIntent);
                    finish();
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Log.w(TAG, "Error adding document", e);
                        displayToast("Error adding document");
                        Set_Limit.setEnabled(true);
                    }
                });
    }
}