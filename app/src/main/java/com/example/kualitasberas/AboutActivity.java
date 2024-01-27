package com.example.kualitasberas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void openExternalLink(View view) {
        // Gantilink_dengan_URL_yang_ingin_dibuka
        String url = "https://www.bulog.co.id/2021/07/30/beras-kita/";

        // Membuat Intent untuk membuka browser
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        // Memulai aktivitas browser
        startActivity(intent);
    }
}