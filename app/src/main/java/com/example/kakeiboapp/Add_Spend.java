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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

public class Add_Spend extends AppCompatActivity {
    ImageView backbtn;
    EditText amount, description;
    Button submit;
    private String spinnervalue;
    FirebaseFirestore db;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spend);

        backbtn = findViewById(R.id.back);
        amount = findViewById(R.id.food_amount);
        description = findViewById(R.id.description);
        submit = findViewById(R.id.submit);
        db = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Spend_Cat,R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item from the spinner
                spinnervalue = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

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
        amount.setFilters(new InputFilter[]{decimalFilter});
        backbtn.setOnClickListener(view -> finish());
        submit.setOnClickListener(view -> {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                LayoutInflater inflater = getLayoutInflater();
                View customLayout = inflater.inflate(R.layout.no_internet_layout, null);

                // Find views in the custom layout
                Button Ok = customLayout.findViewById(R.id.Ok);
                // Create and show the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Add_Spend.this);
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
                if (amount.getText().toString().equals("") || description.getText().toString().equals("") || spinnervalue.equals("")){displayToast("Fill all Fields"); return;}
                else{AddSpend_to_doc();}

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
            AlertDialog.Builder builder = new AlertDialog.Builder(Add_Spend.this);
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
        }
    }
    public void AddSpend_to_doc(){
        submit.setEnabled(false);

        String valueAsString = amount.getText().toString();

// Convert the String value to a double
        double value = Double.parseDouble(valueAsString);

// Create a DecimalFormat object to format the value with two decimal places
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
        data.put("Category", spinnervalue);
        data.put("Amount", decimalFormat.format(value));
        data.put("Description", description.getText().toString());
        data.put("Created", FieldValue.serverTimestamp());

        db.collection("Spends")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    displayToast("Document added successfully");
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
                        submit.setEnabled(true);
                    }
                });
    }
}