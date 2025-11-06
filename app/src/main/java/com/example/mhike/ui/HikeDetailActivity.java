package com.example.mhike.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mhike.R;
import com.example.mhike.data.HikeDao;
import com.example.mhike.model.Hike;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class HikeDetailActivity extends AppCompatActivity {

    private HikeDao hikeDao;
    private long hikeId = 0L;
    private Hike hike; // cached

    private ImageView imgCover;
    private TextView tvName, tvLocation, tvDate, tvMeta, tvDesc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_detail);

        hikeDao = new HikeDao(this);

        MaterialToolbar tb = findViewById(R.id.toolbarDetail);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        imgCover = findViewById(R.id.imgCoverDetail);
        tvName = findViewById(R.id.tvDName);
        tvLocation = findViewById(R.id.tvDLocation);
        tvDate = findViewById(R.id.tvDDate);
        tvMeta = findViewById(R.id.tvDMeta);
        tvDesc = findViewById(R.id.tvDDesc);

        hikeId = getIntent().getLongExtra("id", 0L);
        if (hikeId == 0L) {
            Toast.makeText(this, "Invalid hike", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        hike = hikeDao.findById(hikeId);
        if (hike == null) {
            Toast.makeText(this, "Hike not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) getSupportActionBar().setTitle(hike.name);

        // Cover photo
        if (hike.photoUri != null && !hike.photoUri.isEmpty()) {
            imgCover.setImageURI(Uri.parse(hike.photoUri));
        } else {
            imgCover.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // Text fields
        tvName.setText(hike.name);
        tvLocation.setText(hike.location);
        tvDate.setText(hike.date);

        String meta = (hike.parking ? "Parking • " : "No parking • ")
                + hike.lengthKm + " km • " + getDifficultyLabel(hike.difficulty);
        tvMeta.setText(meta);

        tvDesc.setText(
                hike.description == null || hike.description.trim().isEmpty()
                        ? "(no description)"
                        : hike.description
        );
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent i = new Intent(this, HikeFormActivity.class);
            i.putExtra("mode", "edit");
            i.putExtra("id", hikeId);
            startActivity(i);
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        } else if (id == R.id.action_observations) {
            Intent i = new Intent(this, ObservationListActivity.class);
            i.putExtra("hike_id", hikeId);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete hike")
                .setMessage("Are you sure you want to delete this hike?\n\n" + (hike != null ? hike.name : ""))
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.delete, (d, w) -> {
                    int rows = hikeDao.delete(hikeId);
                    if (rows > 0) {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}