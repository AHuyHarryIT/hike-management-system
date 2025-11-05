package com.example.mhike.ui;


import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mhike.R;
import com.google.android.material.appbar.MaterialToolbar;

public class HikeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_detail);

        MaterialToolbar tb = findViewById(R.id.toolbarDetail);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        ((TextView) findViewById(R.id.tvDName)).setText(getIntent().getStringExtra("name"));
        ((TextView) findViewById(R.id.tvDLocation)).setText(getIntent().getStringExtra("location"));
        ((TextView) findViewById(R.id.tvDDate)).setText(getIntent().getStringExtra("date"));

        boolean parking = getIntent().getBooleanExtra("parking", false);
        double length = getIntent().getDoubleExtra("lengthKm", 0.0);
        int diff = getIntent().getIntExtra("difficulty", 3);

        String meta = (parking ? "Parking • " : "No parking • ") +
                length + " km • " + getDifficultyLabel(diff);
        ((TextView) findViewById(R.id.tvDMeta)).setText(meta);

        ((TextView) findViewById(R.id.tvDDesc))
                .setText(getIntent().getStringExtra("description"));
    }

    private String getDifficultyLabel(int diff) {
        switch (diff) {
            case 1:
                return "Very Easy";
            case 2:
                return "Easy";
            case 3:
                return "Medium";
            case 4:
                return "Hard";
            case 5:
                return "Very Hard";
            default:
                return "Unknown";
        }
    }
}