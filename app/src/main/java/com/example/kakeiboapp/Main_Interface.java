package com.example.kakeiboapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Main_Interface extends AppCompatActivity {
    TextView tvName;
    ImageView History, Analytics, Account_User;
    Button add_spend;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;
    CircleImageView profileImageView;
    TextView food_amount, house_amount, travel_amount, health_amount, entertainment_amount,clothing_amount,education_amount,others_amount;
    TextView food_percent, house_percent, travel_percent, health_percent, entertainment_percent,clothing_percent,education_percent,others_percent;
    TextView Spend_Total,Spend_Max;
    Button AddSpendLimit, UpdateLimit;
    LinearLayout Spend_Ratio;
    SimpleGaugeView gaugeView;
    ProgressDialog progressDialog;
    FirebaseFirestore db;
    Double Spend_Sum,Spend_Limit;
    Boolean HaveLimit;
    LinearLayout food, house, travel, health, entertainment, clothing, education, others;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);

        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data.....");
        progressDialog.show();

        gaugeView = findViewById(R.id.spendGaugeView);

        // Assign variable
        profileImageView = findViewById(R.id.profile_image);
        tvName = findViewById(R.id.username);
        add_spend = findViewById(R.id.Add_Spend);
        History = findViewById(R.id.history);
        Analytics = findViewById(R.id.analytics);
        Account_User = findViewById(R.id.account);

        Spend_Total = findViewById(R.id.spend_value);
        Spend_Max= findViewById(R.id.spend_max);
        AddSpendLimit = findViewById(R.id.add_spend_limit);
        UpdateLimit = findViewById(R.id.update_spend_limit);
        Spend_Ratio = findViewById(R.id.Spend_Ratio);

        food_amount = findViewById(R.id.food_amount);
        food_percent = findViewById(R.id.food_percentage);
        house_amount = findViewById(R.id.house_amount);
        house_percent = findViewById(R.id.house_percentage);
        travel_amount  = findViewById(R.id.travel_amount);
        travel_percent = findViewById(R.id.travel_percentage);
        health_amount = findViewById(R.id.health_amount);
        health_percent = findViewById(R.id.health_percentage);
        entertainment_amount = findViewById(R.id.entertainment_amount);
        entertainment_percent = findViewById(R.id.entertainment_percentage);
        clothing_amount = findViewById(R.id.clothing_amount);
        clothing_percent = findViewById(R.id.clothing_percentage);
        education_amount = findViewById(R.id.education_amount);
        education_percent = findViewById(R.id.education_percentage);
        others_amount = findViewById(R.id.others_amount);
        others_percent = findViewById(R.id.others_percentage);

        food = findViewById(R.id.food_layout);
        house = findViewById(R.id.house_layout);
        travel = findViewById(R.id.travel_layout);
        health = findViewById(R.id.health_layout);
        entertainment = findViewById(R.id.entertainment_layout);
        clothing = findViewById(R.id.clothing_layout);
        education = findViewById(R.id.education_layout);
        others = findViewById(R.id.others_layout);


        // Initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        food.setOnClickListener(view -> Selected_Cat("Food and Dining"));
        house.setOnClickListener(view -> Selected_Cat("Housing and Utilities"));
        travel.setOnClickListener(view -> Selected_Cat("Transportation and Travel"));
        health.setOnClickListener(view -> Selected_Cat("Personal Care and Health"));
        entertainment.setOnClickListener(view -> Selected_Cat("Entertainment and Recreation"));
        clothing.setOnClickListener(view -> Selected_Cat("Clothing and Accessories"));
        education.setOnClickListener(view -> Selected_Cat("Education and Learning"));
        others.setOnClickListener(view -> Selected_Cat("Others"));

        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(Main_Interface.this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        add_spend.setOnClickListener(view -> AddSpendClicked());

        History.setOnClickListener(view -> History_UserClicked());

        Analytics.setOnClickListener(view -> Analytics_UserClicked());

        Account_User.setOnClickListener(view -> Account_UserClicked());

        AddSpendLimit.setOnClickListener(view -> Set_Limit());

        UpdateLimit.setOnClickListener(view -> Update_Limit());

        Spend_Ratio.setOnClickListener(view -> {
            if (UpdateLimit.getVisibility() == View.VISIBLE && HaveLimit != false) {
                UpdateLimit.setVisibility(View.GONE); // hide the button
                Spend_Max.setVisibility(View.VISIBLE);
                AddSpendLimit.setVisibility(View.VISIBLE);
            } else if (UpdateLimit.getVisibility() == View.GONE && HaveLimit != false){
               UpdateLimit.setVisibility(View.VISIBLE); // show the button
                Spend_Max.setVisibility(View.GONE);
                AddSpendLimit.setVisibility(View.GONE);
            }
        });
        fetchSpends();

    }

    public void fetchSpends() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        String monthYear = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        db.collection("Spends")
                .whereEqualTo("User_Id", uid)
                .whereEqualTo("Period", monthYear)
                .get()
                .addOnCompleteListener(task -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (task.isSuccessful()) {
                        // create a new HashMap to store the category totals
                        Map<String, Double> categoryTotals = new HashMap<>();

                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String category = document.getString("Category");
                                String rawamount = document.getString("Amount");

                                Double amount = Double.parseDouble(rawamount);

                                // check if the category already exists in the map
                                if (categoryTotals.containsKey(category)) {
                                    // if it does, add the amount to the existing total
                                    Double total = categoryTotals.get(category) + amount;
                                    categoryTotals.put(category, total);
                                } else {
                                    // if it doesn't, create a new entry in the map
                                    categoryTotals.put(category, amount);
                                }
                            }

                            DecimalFormat decimalFormat = new DecimalFormat("₱#,##0.00");
                            // access the category totals from the map
                            Double foodTotal = categoryTotals.getOrDefault("Food and Dining", 0.00);
                            Double housingTotal = categoryTotals.getOrDefault("Housing and Utilities", 0.00);
                            Double transportationTotal = categoryTotals.getOrDefault("Transportation and Travel", 0.00);
                            Double personalCareTotal = categoryTotals.getOrDefault("Personal Care and Health", 0.00);
                            Double entertainmentTotal = categoryTotals.getOrDefault("Entertainment and Recreation", 0.00);
                            Double clothingTotal = categoryTotals.getOrDefault("Clothing and Accessories", 0.00);
                            Double educationTotal = categoryTotals.getOrDefault("Education and Learning", 0.00);
                            Double othersTotal = categoryTotals.getOrDefault("Others", 0.00);

                            food_amount.setText((decimalFormat.format(foodTotal)));
                            house_amount.setText((decimalFormat.format(housingTotal)));
                            travel_amount.setText(decimalFormat.format(transportationTotal));
                            health_amount.setText((decimalFormat.format(personalCareTotal)));
                            entertainment_amount.setText((decimalFormat.format(entertainmentTotal)));
                            clothing_amount.setText((decimalFormat.format(clothingTotal)));
                            education_amount.setText((decimalFormat.format(educationTotal)));
                            others_amount.setText((decimalFormat.format(othersTotal)));

                            Spend_Sum = foodTotal + housingTotal + transportationTotal + personalCareTotal + entertainmentTotal + clothingTotal + educationTotal + othersTotal;

                            Spend_Total.setText((decimalFormat.format(Spend_Sum)));

                            double foodPercentage = (foodTotal / Spend_Sum) * 100;
                            double housingPercentage = (housingTotal / Spend_Sum) * 100;
                            double transportationPercentage = (transportationTotal / Spend_Sum) * 100;
                            double personalCarePercentage = (personalCareTotal / Spend_Sum) * 100;
                            double entertainmentPercentage = (entertainmentTotal / Spend_Sum) * 100;
                            double clothingPercentage = (clothingTotal / Spend_Sum) * 100;
                            double educationPercentage = (educationTotal / Spend_Sum) * 100;
                            double othersPercentage = (othersTotal / Spend_Sum) * 100;

                            setPercentageText(foodPercentage, food_percent);
                            setPercentageText(housingPercentage, house_percent);
                            setPercentageText(transportationPercentage, travel_percent);
                            setPercentageText(personalCarePercentage, health_percent);
                            setPercentageText(entertainmentPercentage, entertainment_percent);
                            setPercentageText(clothingPercentage, clothing_percent);
                            setPercentageText(educationPercentage, education_percent);
                            setPercentageText(othersPercentage, others_percent);


                        }
                    } else {
                        Log.e("Firestore Error", task.getException().getMessage());
                    }
                });

        DecimalFormat decimalFormat = new DecimalFormat("₱#,##0.00");
        Query query = db.collection("Spend_limit").whereEqualTo("User_Id", uid).whereEqualTo("Period", monthYear);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                String SP = documentSnapshot.getString("Spend_Limit");
                Spend_Limit = Double.parseDouble(SP);
                Spend_Max.setText((decimalFormat.format(Spend_Limit)));
                Spend_Max.setVisibility(View.VISIBLE);
                int heightInDp = 3;
                int heightInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());
                AddSpendLimit.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightInPixels));
                AddSpendLimit.setEnabled(false);
                HaveLimit = true;
                Spend_Average();
            } else {
                Toast.makeText(getApplicationContext(), "Empty result", Toast.LENGTH_SHORT).show();
                gaugeView.setmValue(0);
                gaugeView.setmFillColorEnd(Color.parseColor("#FF4EF123"));
                gaugeView.setmTextColor(Color.parseColor("#FF4EF123"));
                Spend_Max.setVisibility(View.GONE);
                HaveLimit = false;
                int heightInDp = 35;
                int heightInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());
                AddSpendLimit.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightInPixels));
                AddSpendLimit.setEnabled(true);
            }

        }).addOnFailureListener(e -> {
            displayToast("Spend Max Query Failed");
        });


    }
    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
    private void setPercentageText(double percentage, TextView textView) {
        if (percentage == 100) {
            textView.setText("100%");
        } else if(percentage == 0){
            textView.setText("0%");
        }
        else{
            textView.setText(String.format("%.1f", percentage) + "%");
        }
    }

    public void Spend_Average() {
        double percentage;
        if (Spend_Limit == null || Spend_Limit == 0) {
            percentage = 0;
            gaugeView.setmFillColorEnd(Color.parseColor("#FF4EF123"));
            gaugeView.setmTextColor(Color.parseColor("#FF4EF123"));
        } else {
            double percent = Spend_Sum / Spend_Limit * 100;
            if (percent > 100) {
                percentage = 100;
            } else {
                percentage = percent;
            }
            if (percentage >= 75) {
                gaugeView.setmFillColorEnd(Color.RED);
                gaugeView.setmTextColor(Color.RED);
            } else if (percentage >= 50) {
                gaugeView.setmFillColorEnd(Color.parseColor("#FFF38424"));
                gaugeView.setmTextColor(Color.parseColor("#FFF38424"));
            } else if (percentage >= 25) {
                gaugeView.setmFillColorEnd(Color.parseColor("#FFEDD118"));
                gaugeView.setmTextColor(Color.parseColor("#FFEDD118"));
            } else {
                gaugeView.setmFillColorEnd(Color.parseColor("#FF4EF123"));
                gaugeView.setmTextColor(Color.parseColor("#FF4EF123"));
            }
        }
        gaugeView.setmValue((int) percentage);
    }

    protected void onStart() {
        super.onStart();


        // Initialize firebase user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Check condition
        if (firebaseUser != null) {
            // When firebase user is not equal to null set image on image view
            Glide.with(Main_Interface.this).load(firebaseUser.getPhotoUrl()).placeholder(R.drawable.default_user_icon).into(profileImageView);
            // set name on text view
            tvName.setText(("Welcome\n"+firebaseUser.getDisplayName()));
        }


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
            AlertDialog.Builder builder = new AlertDialog.Builder(Main_Interface.this, R.style.TransparentDialog);
            builder.setView(customLayout);
            AlertDialog dialog = builder.create();

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            // Add a click listener to the "Yes" button
            Ok.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        }
    }
    public void onBackPressed() {
        LayoutInflater inflater = getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.close_layout, null);

        Button yesButton = customLayout.findViewById(R.id.yes_button);
        Button noButton = customLayout.findViewById(R.id.no_button);

        AlertDialog.Builder builder = new AlertDialog.Builder(Main_Interface.this, R.style.TransparentDialog);
        builder.setView(customLayout);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                builder.setCancelable(true);
            }
        });

        dialog.show();

    }
    public void Set_Limit() {
        Intent intent = new Intent(this, Set_Spend_Limit.class);
        int requestCode = 123; // any unique request code
        startActivityForResult(intent, requestCode);
    }

    public void Update_Limit() {
        Intent intent = new Intent(this, Update_Spend_Limit.class);
        int requestCode = 123; // any unique request code
        startActivityForResult(intent, requestCode);
    }

    public void Selected_Cat(String category) {
        Intent intent = new Intent(this, Selected_Category.class);
        intent.putExtra("category_key", category);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode == RESULT_OK) {
            fetchSpends();
            AddSpendLimit.setVisibility(View.VISIBLE);
            UpdateLimit.setVisibility(View.GONE);
        }
    }

    public void AddSpendClicked() {
        Intent Intent = new Intent(this, Add_Spend.class);
        int requestCode = 123; // any unique request code
        startActivityForResult(Intent, requestCode);
    }

    public void History_UserClicked() {
        Intent Intent = new Intent(this, History.class);
        Intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(Intent);
        finish();
    }

    public void Analytics_UserClicked() {
        Intent Intent = new Intent(this, Analytics.class);
        Intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(Intent);
        finish();
    }

    public void Account_UserClicked() {
        Intent Intent = new Intent(this, Account_User.class);
        Intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(Intent);
        finish();
    }
}