package com.example.kakeiboapp;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class Register extends AppCompatActivity {
    Button Register;
    EditText Username, Email, Password, ConfPass;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    //PHOTO URL: https://i.pinimg.com/564x/a9/4d/56/a94d56e8826358940f2e19fd092d1c85.jpg
//PHOTO URL: https://i.pinimg.com/564x/98/35/71/9835712e6320514ada2187a3640ed91d.jpg
//PHOTO URL: https://i.pinimg.com/564x/3f/ca/33/3fca338198e7e34ff01a94e4ca0668a7.jpg
 //   https://www.google.com/search?q=create+new+user+firebase+android&oq=create+new+&aqs=chrome.0.69i59j69i57j0i512l5j69i60.15191j0j7&sourceid=chrome&ie=UTF-8#fpstate=ive&vld=cid:64563369,vid:QAKq8UBv4GI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Register = findViewById(R.id.register);
        Username= findViewById(R.id.user_name);
        Email = findViewById(R.id.user_email);
        Password= findViewById(R.id.user_password);
        ConfPass = findViewById(R.id.user_conf_pass);
        progressBar = findViewById(R.id.progressBar);


        Register.setOnClickListener(view -> {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                LayoutInflater inflater = getLayoutInflater();
                View customLayout = inflater.inflate(R.layout.no_internet_layout, null);

                // Find views in the custom layout
                Button Ok = customLayout.findViewById(R.id.Ok);
                // Create and show the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
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
            if (Username.getText().toString().equals("") || Email.getText().toString().equals("") || Password.getText().toString().equals("") || ConfPass.getText().toString().equals("")){displayToast("Fill all Fields"); return;}
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(Email.getText().toString()).matches()) {  displayToast("Email Invalid"); return;}
            if (!Password.getText().toString().equals(ConfPass.getText().toString())){ displayToast("Password Didn't Match");  Log.w(TAG, Password.getText().toString() +"  "+ ConfPass.getText().toString());return;}
            if (Password.getText().toString().length() < 8) {  displayToast("Password Should be 8 Characters");return;}
            progressBar.setVisibility(View.VISIBLE); CreateUser();
        }});
    }

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
    private void CreateUser(){
        String email = Email.getText().toString();
        String password = Password.getText().toString();
        String displayName = Username.getText().toString();
        String photoURL = "https://i.pinimg.com/564x/3f/ca/33/3fca338198e7e34ff01a94e4ca0668a7.jpg";


        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.w(TAG,"Account Created Successfully");
                        // User created successfully, update their profile with display name and photo URL

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName)
                                .setPhotoUri(Uri.parse(photoURL))
                                .build();
                        assert user != null;
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.w(TAG,"User Photo URL and Username Have Been Updated Successfully");
                                        progressBar.setVisibility(View.GONE);
                                        displayToast("Account Created Successfully");

                                            Intent intent = new Intent(Register.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();

                                    } else {
                                        Log.w(TAG,"User Photo URL and Username Update Failed");
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });


                    } else {
                        // User creation failed
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Register.this, "Email have already been taken",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}