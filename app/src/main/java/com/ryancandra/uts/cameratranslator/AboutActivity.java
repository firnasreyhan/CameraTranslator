package com.ryancandra.uts.cameratranslator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Button buttonKunjungiWebsiteStiki;
    private TextView textViewVersi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.BlueTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        buttonKunjungiWebsiteStiki = findViewById(R.id.buttonKunjungiWebsiteStiki);
        toolbar = findViewById(R.id.toolbar);
        textViewVersi = findViewById(R.id.textViewVersi);
        textViewVersi.setText("Versi " + BuildConfig.VERSION_NAME);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        buttonKunjungiWebsiteStiki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.stiki.ac.id/"));
                startActivity(browserIntent);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}