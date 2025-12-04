package com.example.mhike.ui;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhike.R;
import com.example.mhike.data.HikeDao;
import com.example.mhike.model.Hike;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private HikeAdapter adapter;
    private HikeDao hikeDao;
    private String currentQuery = null;
    private List<Hike> cache = new ArrayList<>();
    
    private View filterSection;
    private CheckBox cbUseDateRange;
    private TextInputLayout tilDateFrom, tilDateTo;
    private TextInputEditText etFilterDateFrom, etFilterDateTo;
    private Spinner spinnerParking, spinnerDiffMin, spinnerDiffMax;
    private boolean isFilterVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hikeDao = new HikeDao(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        filterSection = findViewById(R.id.filterSection);
        cbUseDateRange = findViewById(R.id.cbUseDateRange);
        tilDateFrom = findViewById(R.id.tilDateFrom);
        tilDateTo = findViewById(R.id.tilDateTo);
        etFilterDateFrom = findViewById(R.id.etFilterDateFrom);
        etFilterDateTo = findViewById(R.id.etFilterDateTo);
        spinnerParking = findViewById(R.id.spinnerParking);
        spinnerDiffMin = findViewById(R.id.spinnerDiffMin);
        spinnerDiffMax = findViewById(R.id.spinnerDiffMax);

        setupSpinners();

        cbUseDateRange.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tilDateFrom.setEnabled(isChecked);
            tilDateTo.setEnabled(isChecked);
            etFilterDateFrom.setEnabled(isChecked);
            etFilterDateTo.setEnabled(isChecked);
            if (!isChecked) {
                etFilterDateFrom.setText("");
                etFilterDateTo.setText("");
            }
            applyCurrentFilters();
        });

        etFilterDateFrom.setOnClickListener(v -> {
            if (cbUseDateRange.isChecked()) {
                pickDate(etFilterDateFrom);
            }
        });
        etFilterDateTo.setOnClickListener(v -> {
            if (cbUseDateRange.isChecked()) {
                pickDate(etFilterDateTo);
            }
        });

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    applyCurrentFilters();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerParking.setOnItemSelectedListener(filterListener);
        spinnerDiffMin.setOnItemSelectedListener(filterListener);
        spinnerDiffMax.setOnItemSelectedListener(filterListener);

        MaterialButton btnClearFilters = findViewById(R.id.btnClearFilters);
        btnClearFilters.setOnClickListener(v -> clearFilters());

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
    }

    private void setupSpinners() {
        String[] parkingOptions = {"Any", "Yes", "No"};
        ArrayAdapter<String> parkingAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, parkingOptions);
        parkingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerParking.setAdapter(parkingAdapter);

        String[] difficultyOptions = {"Any", "1", "2", "3", "4", "5"};
        ArrayAdapter<String> diffMinAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, difficultyOptions);
        diffMinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiffMin.setAdapter(diffMinAdapter);

        ArrayAdapter<String> diffMaxAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, difficultyOptions);
        diffMaxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiffMax.setAdapter(diffMaxAdapter);
    }

    private void toggleFilterSection() {
        isFilterVisible = !isFilterVisible;
        filterSection.setVisibility(isFilterVisible ? View.VISIBLE : View.GONE);
    }

    private void pickDate(TextInputEditText target) {
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH), d = c.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            target.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
            applyCurrentFilters();
        }, y, m, d).show();
    }

    private void clearFilters() {
        cbUseDateRange.setChecked(false);
        etFilterDateFrom.setText("");
        etFilterDateTo.setText("");
        spinnerParking.setSelection(0);
        spinnerDiffMin.setSelection(0);
        spinnerDiffMax.setSelection(0);
        currentQuery = null;
        reload();
    }

    private void applyCurrentFilters() {
        String dateFrom = cbUseDateRange.isChecked() ? safe(etFilterDateFrom) : "";
        String dateTo = cbUseDateRange.isChecked() ? safe(etFilterDateTo) : "";
        
        Integer parking = null;
        int parkingPos = spinnerParking.getSelectedItemPosition();
        if (parkingPos == 1) parking = 1;
        else if (parkingPos == 2) parking = 0;

        Integer diffMin = null;
        Integer diffMax = null;
        int diffMinPos = spinnerDiffMin.getSelectedItemPosition();
        int diffMaxPos = spinnerDiffMax.getSelectedItemPosition();
        
        if (diffMinPos > 0) diffMin = diffMinPos;
        if (diffMaxPos > 0) diffMax = diffMaxPos;

        currentQuery = null;

        List<Hike> res = hikeDao.getFiltered(
                null, null,
                nullIfEmpty(dateFrom), nullIfEmpty(dateTo),
                null, null, diffMin, diffMax, parking
        );
        cache = res;
        adapter.submitList(new ArrayList<>(cache));
    }

    private String safe(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFilterVisible || !hasActiveFilters()) {
            reload();
        } else {
            applyCurrentFilters();
        }
    }

    private boolean hasActiveFilters() {
        return cbUseDateRange.isChecked() ||
               spinnerParking.getSelectedItemPosition() != 0 ||
               spinnerDiffMin.getSelectedItemPosition() != 0 ||
               spinnerDiffMax.getSelectedItemPosition() != 0;
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
                if (isFilterVisible && hasActiveFilters()) {
                    clearFilters();
                }
                reload();
                return true;
            }
        });

        MenuItem filters = menu.findItem(R.id.action_filters);
        filters.setOnMenuItemClickListener(item -> {
            toggleFilterSection();
            return true;
        });
        return true;
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