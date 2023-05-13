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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class Account_User extends AppCompatActivity {
    ImageView Home, History, Analytics;
    Button Logout,Change_Img;
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
        Logout = findViewById(R.id.logout);


        firebaseAuth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(Account_User.this, GoogleSignInOptions.DEFAULT_SIGN_IN);


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

        Change_Img.setOnClickListener(view ->Change_Photo());

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