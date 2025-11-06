package com.example.mhike.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhike.R;
import com.example.mhike.data.HikeDao;
import com.example.mhike.model.Hike;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private HikeAdapter adapter;
    private HikeDao hikeDao;
    private String currentQuery = null;
    private List<Hike> cache = new ArrayList<>();
    private ActivityResultLauncher<Intent> searchLauncher;
    // cache current filters (so list survives rotation or returning from detail)
    private Intent lastFilterIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hikeDao = new HikeDao(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView rv = findViewById(R.id.rvHikes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HikeAdapter(item -> {
            Intent i = new Intent(this, HikeDetailActivity.class);
            i.putExtra("id", item.id);
            i.putExtra("name", item.name);
            i.putExtra("location", item.location);
            i.putExtra("date", item.date);
            i.putExtra("parking", item.parking);
            i.putExtra("lengthKm", item.lengthKm);
            i.putExtra("difficulty", item.difficulty);
            i.putExtra("description", item.description);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, HikeFormActivity.class)));

        searchLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        lastFilterIntent = result.getData();
                        applyFilters(lastFilterIntent);
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lastFilterIntent == null) reload(); // plain load
        else applyFilters(lastFilterIntent);     // keep filters after returning
    }

    private void reload() {
        cache = hikeDao.getAll(currentQuery);
        adapter.submitList(new ArrayList<>(cache));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) searchItem.getActionView();
        sv.setQueryHint(getString(R.string.search_hint));
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                // Simple search overrides advanced filters quickly:
                lastFilterIntent = null;
                reload();
                return true;
            }
        });

        // handle advanced search open
        MenuItem adv = menu.findItem(R.id.action_advanced_search);
        adv.setOnMenuItemClickListener(item -> {
            searchLauncher.launch(new Intent(this, SearchActivity.class));
            return true;
        });
        return true;
    }

    private void applyFilters(Intent data) {
        String name = data.getStringExtra("name");
        String location = data.getStringExtra("location");
        String dateFrom = data.getStringExtra("dateFrom");
        String dateTo = data.getStringExtra("dateTo");

        Double lenMin = parseDoubleOrNull(data.getStringExtra("lenMin"));
        Double lenMax = parseDoubleOrNull(data.getStringExtra("lenMax"));
        Integer diffMin = parseIntOrNull(data.getStringExtra("diffMin"));
        Integer diffMax = parseIntOrNull(data.getStringExtra("diffMax"));

        int parkingRaw = data.getIntExtra("parking", -1);
        Integer parking = (parkingRaw == -1) ? null : parkingRaw;

        // simple search text no longer applies when using advanced
        currentQuery = null;

        List<Hike> res = hikeDao.getFiltered(
                nullIfEmpty(name), nullIfEmpty(location),
                nullIfEmpty(dateFrom), nullIfEmpty(dateTo),
                lenMin, lenMax, diffMin, diffMax, parking
        );
        cache = res;
        adapter.submitList(new ArrayList<>(cache));
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String nullIfEmpty(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}