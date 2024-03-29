package com.example.kakeiboapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.checkerframework.checker.units.qual.A;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Analytics extends AppCompatActivity {
    ImageView History, Home, Account_User;
    List<MonthlyExpense> monthlyExpenses = new ArrayList<>();
    BarChart chart;
    LinearLayout Summary;
    Boolean isFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        Intent intent = getIntent();
        isFirstRun = intent.getBooleanExtra("isFirstRun_key",false);


        History = findViewById(R.id.history);
        Home = findViewById(R.id.home);
        Account_User = findViewById(R.id.account);
        Summary = findViewById(R.id.summary);

        History.setOnClickListener(view -> History_UserClicked());

        Home.setOnClickListener(view -> Home_UserClicked());

        Account_User.setOnClickListener(view -> Account_UserClicked());

        FetchForChart();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(Analytics.this, R.style.TransparentDialog);
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

    public void onBackPressed() {
        LayoutInflater inflater = getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.close_layout, null);

        Button yesButton = customLayout.findViewById(R.id.yes_button);
        Button noButton = customLayout.findViewById(R.id.no_button);

        AlertDialog.Builder builder = new AlertDialog.Builder(Analytics.this, R.style.TransparentDialog);
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
    public void Chart() {
        chart = findViewById(R.id.chart);

        Log.d("Monthly Expenses: ", monthlyExpenses.toString());

// Create a list of BarEntry objects for the expenses
        List<BarEntry> expenseEntries = new ArrayList<>();
        for (int i = 0; i < monthlyExpenses.size(); i++) {
            expenseEntries.add(new BarEntry(i, (float) monthlyExpenses.get(i).getExpense()));
        }

// Create a list of BarEntry objects for the savings
        List<BarEntry> savingsEntries = new ArrayList<>();
        for (int i = 0; i < monthlyExpenses.size(); i++) {
            savingsEntries.add(new BarEntry(i, (float) monthlyExpenses.get(i).getSaved()));
        }

// Create a BarDataSet object for the expenses
        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Expenses");
        expenseDataSet.setColor(Color.parseColor("#43F3E1"));

// Create a BarDataSet object for the savings
        BarDataSet savingsDataSet = new BarDataSet(savingsEntries, "Savings");
        savingsDataSet.setColor(Color.parseColor("#3ECDBF"));

// Create a BarData object and add the BarDataSet objects to it
        BarData barData = new BarData(expenseDataSet, savingsDataSet);

// Set the BarData object to the chart
        chart.setData(barData);

// Set the x-axis labels to the months
        int visibleBarCount = 4; // set the number of visible bars
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setLabelCount(visibleBarCount);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < monthlyExpenses.size()) {
                    // Get the period at the current index
                    String period = monthlyExpenses.get(index).getPeriod();

                    // Return the period as the label
                    return period;
                } else {
                    return "";
                }
            }
        });
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Format the value as desired, e.g. add a currency symbol
                return "₱" + value;
            }
        });


        chart.getDescription().setText(" ");
        chart.getDescription().setTextSize(16f);
        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextSize(10f);
        chart.getBarData().setBarWidth(0.8f);
        chart.setVisibleXRangeMaximum(4);
        chart.moveViewToX(monthlyExpenses.size() - 1);


// Set an onClick listener for the bars
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Summary.setVisibility(View.VISIBLE);
                BarEntry barEntry = (BarEntry) e;
                int index = (int) barEntry.getX();
                MonthlyExpense monthlyExpense = monthlyExpenses.get(index);
                DecimalFormat decimalFormat = new DecimalFormat("₱#,##0.00");
                TextView period = findViewById(R.id.Period);
                EditText expenses = findViewById(R.id.user_expenses);
                EditText savings = findViewById(R.id.user_savings);
                Button show = findViewById(R.id.view_record);
                period.setText(monthlyExpense.getPeriod());
                expenses.setText(decimalFormat.format(monthlyExpense.getExpense()));
                savings.setText(decimalFormat.format(monthlyExpense.getSaved()));

                show.setOnClickListener(view -> Selected_Period(monthlyExpense.getPeriod()));
            }

            @Override
            public void onNothingSelected() {
                Summary.setVisibility(View.GONE);
            }
        });

        chart.animateY(1000);
// Refresh the chart
        chart.invalidate();

    }

    private List<String> getLabelsFromExpenseData(List<MonthlyExpense> expenses) {
        List<String> labels = new ArrayList<>();
        for (MonthlyExpense expense : expenses) {
            labels.add(expense.getPeriod());
        }
        return labels;
    }

    public void FetchForChart(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Spend_limit").whereEqualTo("User_Id", uid)
                .orderBy("Created", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Create a HashMap to temporarily store the MonthlyExpense objects
                    HashMap<String, MonthlyExpense> tempMap = new HashMap<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String period = document.getString("Period");
                        double limit = Double.parseDouble(document.getString("Spend_Limit"));

                        // Calculate the sum of all "Amount" fields in the matching documents in collection 2 (Spends)
                        db.collection("Spends").whereEqualTo("User_Id", uid)
                                .whereEqualTo("Period", period)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    double expenses = 0.00;
                                    for (DocumentSnapshot document2 : queryDocumentSnapshots1) {
                                        String amountStr = document2.getString("Amount");
                                        double amount = Double.parseDouble(amountStr);
                                        expenses += amount;
                                    }

                                    // Create a MonthlyExpense object and add it to the temporary HashMap
                                    MonthlyExpense monthlyExpense = new MonthlyExpense(period, expenses, limit);
                                    tempMap.put(period, monthlyExpense);

                                    // If the size of the HashMap matches the size of the query result, add the values to the ArrayList in the correct order
                                    if (tempMap.size() == queryDocumentSnapshots.size()) {
                                        for (DocumentSnapshot document3 : queryDocumentSnapshots) {
                                            String tempPeriod = document3.getString("Period");
                                            MonthlyExpense tempExpense = tempMap.get(tempPeriod);
                                            monthlyExpenses.add(tempExpense);
                                        }
                                        Chart();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });

    }
    public void Selected_Period(String period){
        Intent intent = new Intent(this, Selected_Period.class);
        intent.putExtra("period_key", period);
        startActivity(intent);
    }

    public void History_UserClicked() {
        Intent Intent = new Intent(this, History.class);
        Intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Intent.putExtra("isFirstRun_key", isFirstRun);
        startActivity(Intent);
        finish();
    }

    public void Home_UserClicked() {
        Intent Intent = new Intent(this, Main_Interface.class);
        Intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Intent.putExtra("isFirstRun_key", isFirstRun);
        startActivity(Intent);
        finish();
    }

    public void Account_UserClicked() {
        Intent Intent = new Intent(this, Account_User.class);
        Intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Intent.putExtra("isFirstRun_key", isFirstRun);
        startActivity(Intent);
        finish();
    }
}