package com.example.kakeiboapp;


import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Button btSignIn, btRegister, btLogin;
    EditText Email,Password;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;
    ProgressBar progressBar;

    final ActivityResultLauncher<Intent> GoogleSignInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    activityResult -> {
                        int result = activityResult.getResultCode();
                        Intent data = activityResult.getData();
                        Log.w(TAG, "ActivityResultLauncher have start");
                        Log.w(TAG, "Result Value = "+result);
                        if (result == RESULT_OK) {
                            progressBar.setVisibility(View.VISIBLE);
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            Log.w(TAG,"Google sign in successful");
                            try {
                                // Google Sign In was successful, authenticate with Firebase
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                Log.w(TAG, "firebaseAuthWithGoogle:" + account.getId());
                                firebaseAuthWithGoogle(account.getIdToken());
                            } catch (ApiException e) {
                                // Google Sign In failed, update UI appropriately
                                Log.w(TAG, "Google sign in failed", e);
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btSignIn = findViewById(R.id.bt_sign_in);
        btRegister = findViewById(R.id.bt_reg);
        btLogin= findViewById(R.id.bt_login);
        Email = findViewById(R.id.user_email);
        Password = findViewById(R.id.user_password);
        progressBar = findViewById(R.id.progressBar);


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, googleSignInOptions);

        btRegister.setOnClickListener(view -> Register());
        btLogin.setOnClickListener(view -> {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                LayoutInflater inflater = getLayoutInflater();
                View customLayout = inflater.inflate(R.layout.no_internet_layout, null);

                // Find views in the custom layout
                Button Ok = customLayout.findViewById(R.id.Ok);
                // Create and show the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.TransparentDialog);
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
            else {

            if (Email.getText().toString().equals("") ){displayToast("Enter Email"); return;}
            if (Password.getText().toString().equals("") ){displayToast("Enter Password"); return;}
            Login();
        }});
        btSignIn.setOnClickListener(view -> {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                LayoutInflater inflater = getLayoutInflater();
                View customLayout = inflater.inflate(R.layout.no_internet_layout, null);

                // Find views in the custom layout
                Button Ok = customLayout.findViewById(R.id.Ok);
                // Create and show the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.TransparentDialog);
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
            else{
            Intent intent = googleSignInClient.getSignInIntent();
            GoogleSignInLauncher.launch(intent);
            Log.w(TAG, "Clicked");
        }});

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            Log.w(TAG, "Firebase Current User is Not Null");
            startActivity(new Intent(MainActivity.this, Start_Screen.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(MainActivity.this, Start_Screen.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        displayToast("Login Successfully");
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        displayToast("Login Failed Error: " + Objects.requireNonNull(task.getException()).getMessage());
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
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.TransparentDialog);
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

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    public void Register() {
        Intent Intent = new Intent(this, Register.class);
        startActivity(Intent);
    }



    public void Login() {
        Email = findViewById(R.id.user_email);
        Password= findViewById(R.id.user_password);
        String email = Email.getText().toString();
        String password = Password.getText().toString();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(MainActivity.this, Start_Screen.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        displayToast("Login Successfully");
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        displayToast("Login Failed: Credentials Invalid" );
                    }
                });
    }
}