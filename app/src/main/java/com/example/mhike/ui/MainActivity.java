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
import com.example.mhike.model.Hike;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<Hike> all = new ArrayList<>();
    private HikeAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView rv = findViewById(R.id.rvHikes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HikeAdapter(item -> {
            Intent i = new Intent(this, HikeDetailActivity.class);
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

        // Dummy data (until DB is added)
        seed();

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, HikeFormActivity.class)));
    }

    private void seed() {
        all.clear();
        all.add(new Hike("Langbiang Peak", "Da Lat", "2025-09-12", true, 12.5, 5, "Beautiful pine forest."));
        all.add(new Hike("Ba Den Mountain", "Tay Ninh", "2025-10-02", false, 6.3, 3, "Great sunrise view."));
        all.add(new Hike("Fansipan Trail", "Lao Cai", "2025-11-01", true, 14.0, 5, "Roof of Indochina!"));
        adapter.submitList(new ArrayList<>(all));
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
                filter(newText);
                return true;
            }
        });
        return true;
    }

    private void filter(String q) {
        if (q == null || q.trim().isEmpty()) {
            adapter.submitList(new ArrayList<>(all));
            return;
        }
        String s = q.toLowerCase();
        List<Hike> res = new ArrayList<>();
        for (Hike h : all) {
            if (h.name.toLowerCase().contains(s) || h.location.toLowerCase().contains(s)) {
                res.add(h);
            }
        }
        adapter.submitList(res);
    }
}