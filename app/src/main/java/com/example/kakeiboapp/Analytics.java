package com.example.kakeiboapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Analytics extends AppCompatActivity {
    ImageView History, Home, Account_User;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        History = findViewById(R.id.history);
        Home = findViewById(R.id.home);
        Account_User = findViewById(R.id.account);

        History.setOnClickListener(view -> History_UserClicked());

        Home.setOnClickListener(view -> Home_UserClicked());

        Account_User.setOnClickListener(view -> Account_UserClicked());
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

    public void History_UserClicked() {
        Intent Intent = new Intent(this, History.class);
        Intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(Intent);
        finish();
    }

    public void Home_UserClicked() {
        Intent Intent = new Intent(this, Main_Interface.class);
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