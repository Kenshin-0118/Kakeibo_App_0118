package com.example.kakeiboapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class Account_User extends AppCompatActivity {
    ImageView Home, History, Analytics;
    Button Logout,Change_Img, Change_Name,Change_Pass;
    TextView tvName, UID;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;
    private static final int REQUEST_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_user);

        tvName = findViewById(R.id.username);
        UID = findViewById(R.id.UID);
        Home = findViewById(R.id.home);
        History = findViewById(R.id.history);
        Analytics = findViewById(R.id.analytics);
        Change_Img = findViewById(R.id.change_photo);
        Change_Name = findViewById(R.id.change_name);
        Change_Pass = findViewById(R.id.change_pass);
        Logout = findViewById(R.id.logout);


        firebaseAuth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(Account_User.this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // User is signed in with Google, hide the button
            Change_Pass.setVisibility(View.GONE);
        } else {
            // User is signed in with email and password, show the button
            Change_Pass.setVisibility(View.VISIBLE);
        }


        Home.setOnClickListener(view -> Home_UserClicked());

        History.setOnClickListener(view -> History_UserClicked());

        Analytics.setOnClickListener(view -> Analytics_UserClicked());

        Logout.setOnClickListener(view -> SignOut());

        UID.setOnClickListener(view -> {
            String textToCopy = UID.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Account ID", textToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Account ID copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        Change_Img.setOnClickListener(view ->{
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (!isConnected) {
                LayoutInflater inflater = getLayoutInflater();
                View customLayout = inflater.inflate(R.layout.no_internet_layout, null);

                // Find views in the custom layout
                Button Ok = customLayout.findViewById(R.id.Ok);
                // Create and show the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Account_User.this, R.style.TransparentDialog);
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
                }else{Change_Photo();}
            });
        Change_Name.setOnClickListener(view ->{
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (!isConnected) {
                LayoutInflater inflater = getLayoutInflater();
                View customLayout = inflater.inflate(R.layout.no_internet_layout, null);

                // Find views in the custom layout
                Button Ok = customLayout.findViewById(R.id.Ok);
                // Create and show the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Account_User.this, R.style.TransparentDialog);
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
            }else{Change_Username();}
        });
        Change_Pass.setOnClickListener(view ->{
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (!isConnected) {
                LayoutInflater inflater = getLayoutInflater();
                View customLayout = inflater.inflate(R.layout.no_internet_layout, null);

                // Find views in the custom layout
                Button Ok = customLayout.findViewById(R.id.Ok);
                // Create and show the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Account_User.this, R.style.TransparentDialog);
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
            }else{Change_Password();}
        });


    }

    public void SignOut() {
        LayoutInflater inflater = getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.logout_layout, null);

        // Find views in the custom layout
        Button ConfirmLogout = customLayout.findViewById(R.id.Logout_yes);
        Button CancelLogout= customLayout.findViewById(R.id.Logout_no);
        // Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Account_User.this, R.style.TransparentDialog);
        builder.setView(customLayout);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Add a click listener to the "Yes" button
        ConfirmLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignInClient.signOut().addOnCompleteListener(task -> {
                    // Check condition
                    if (task.isSuccessful()) {
                        // When task is successful sign out from firebase
                        firebaseAuth.signOut();
                        // Display Toast
                        Toast.makeText(getApplicationContext(), "Logout Successfully", Toast.LENGTH_SHORT).show();
                        // Finish activity
                        Logout();
                    }
                });
            }
        });

        // Add a click listener to the "No" button
        CancelLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        CircleImageView profileImageView = findViewById(R.id.profile_image);
        if (firebaseUser != null) {
            // When firebase user is not equal to null set image on image view
            Glide.with(Account_User.this).load(firebaseUser.getPhotoUrl()).placeholder(R.drawable.default_user_icon).into(profileImageView);
            // set name on text view
            tvName.setText((firebaseUser.getDisplayName()));
            UID.setText(firebaseUser.getUid());
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
            AlertDialog.Builder builder = new AlertDialog.Builder(Account_User.this, R.style.TransparentDialog);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(Account_User.this, R.style.TransparentDialog);
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

    public void Change_Photo() {
// Use a file picker or camera to allow the user to select or take a photo
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference photoRef = storageRef.child("users/" + user.getUid() + "/photo.jpg");

        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            // Get the selected image URI
            Uri selectedImage = data.getData();

            // Check if the photo already exists and delete it if it does
            photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // The photo exists, so delete it
                photoRef.delete().addOnSuccessListener(aVoid -> {
                    // File deleted successfully, upload the new photo
                    UploadTask uploadTask = photoRef.putFile(selectedImage);

                    // Once the upload is complete, obtain the download URL and update the user's authentication profile
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setPhotoUri(uri)
                                        .build();

                                user.updateProfile(profileUpdates);
                                Toast.makeText(getApplicationContext(), "Updating profile picture, please wait...", Toast.LENGTH_SHORT).show();
                                new Handler().postDelayed(() -> recreate(), 3000);
                            }
                        });
                    });
                }).addOnFailureListener(e -> {
                    // Failed to delete the existing photo, log the error
                    Log.e(TAG, "Error deleting existing photo: " + e.getMessage());
                });
            }).addOnFailureListener(e -> {
                // The photo doesn't exist, so just upload the new photo
                UploadTask uploadTask = photoRef.putFile(selectedImage);

                // Once the upload is complete, obtain the download URL and update the user's authentication profile
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uri)
                                    .build();

                            user.updateProfile(profileUpdates);
                            Toast.makeText(getApplicationContext(), "Updating profile picture, please wait...", Toast.LENGTH_SHORT).show();
                            new Handler().postDelayed(() -> recreate(), 3000);
                        }
                    });
                });
            });
        }
    }

    public void Change_Username() {
        LayoutInflater inflater = getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.change_username_layout, null);

        // Find views in the custom layout
        EditText Username = customLayout.findViewById(R.id.new_username);
        Button Confirm = customLayout.findViewById(R.id.confirm_button);
        Button Cancel = customLayout.findViewById(R.id.cancel_button);
        // Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Account_User.this, R.style.TransparentDialog);
        builder.setView(customLayout);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        Confirm.setOnClickListener(view -> {
            if(Username.getText().toString().equals("")){
                Toast.makeText(getApplicationContext(), "Enter New Username", Toast.LENGTH_SHORT).show();
            }else{
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String new_username = Username.getText().toString();

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(new_username)
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Username update successful
                                Toast.makeText(getApplicationContext(), "Username updated!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                recreate(); // recreate activity to update UI
                            } else {
                                // Username update failed
                                Toast.makeText(getApplicationContext(), "Failed to update username.", Toast.LENGTH_SHORT).show();
                            }
                        });

            }

        });

        Cancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    public void Change_Password() {
        LayoutInflater inflater = getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.change_password_layout, null);

        // Find views in the custom layout
        EditText Prev_pass = customLayout.findViewById(R.id.prev_pass);
        EditText New_pass= customLayout.findViewById(R.id.new_pass);
        EditText Conf_pass = customLayout.findViewById(R.id.conf_pass);
        Button Confirm = customLayout.findViewById(R.id.confirm_button);
        Button Cancel = customLayout.findViewById(R.id.cancel_button);
        // Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Account_User.this, R.style.TransparentDialog);
        builder.setView(customLayout);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        Confirm.setOnClickListener(view -> {
            if(Prev_pass.getText().toString().equals("")||New_pass.getText().equals("")||Conf_pass.getText().toString().equals("")){
                Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }else if (!New_pass.getText().toString().equals(Conf_pass.getText().toString())){
                Toast.makeText(getApplicationContext(), "Password didn't match", Toast.LENGTH_SHORT).show();
            }else{
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String prev_password = Prev_pass.getText().toString();
                String new_password = New_pass.getText().toString();
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), prev_password);

// Prompt the user to re-authenticate with their previous password
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // The user has provided the correct previous password, update their password
                            user.updatePassword(new_password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Password updated successfully
                                        Log.d(TAG, "Password updated successfully");
                                        Toast.makeText(getApplicationContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        // Failed to update password
                                        Log.e(TAG, "Error updating password: " + task.getException().getMessage());
                                        Toast.makeText(getApplicationContext(), "Error updating password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            // The user has provided the wrong previous password
                            Log.e(TAG, "Error re-authenticating user: " + task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), "Previous password invalid", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }

        });

        Cancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }


    public void Home_UserClicked() {
        Intent Intent = new Intent(this,Main_Interface.class);
        Intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(Intent);
        finish();
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

    public void Logout() {
        Intent Intent = new Intent(this, MainActivity.class);
        startActivity(Intent);
        finish();
    }

}