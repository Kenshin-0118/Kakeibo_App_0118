package com.example.kakeiboapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Start_Screen extends AppCompatActivity {

    Button start;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        start = findViewById(R.id.start_btn);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Start();
            }
        });
    }

    public void Start() {
        Intent Intent = new Intent(this,Main_Interface.class);
        startActivity(Intent);
        finish();
    }
}