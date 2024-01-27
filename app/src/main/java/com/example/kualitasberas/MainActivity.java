package com.example.kualitasberas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    ImageButton btnKlasifikasi;
    ImageButton btnTentang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnKlasifikasi = findViewById(R.id.btnKlasifikasi);
        btnTentang = findViewById(R.id.btnTentang);

        btnKlasifikasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), KlasifikasiActivity.class));
            }
        });

        btnTentang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            }
        });
    }
}