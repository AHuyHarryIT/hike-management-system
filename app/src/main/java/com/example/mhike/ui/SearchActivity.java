package com.example.mhike.ui;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mhike.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class SearchActivity extends AppCompatActivity {

    private TextInputEditText etName, etLocation, etDateFrom, etDateTo, etLenMin, etLenMax, etDiffMin, etDiffMax;
    private RadioGroup rgParking;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        MaterialToolbar tb = findViewById(R.id.toolbarSearch);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        etName = findViewById(R.id.etName);
        etLocation = findViewById(R.id.etLocation);
        etDateFrom = findViewById(R.id.etDateFrom);
        etDateTo = findViewById(R.id.etDateTo);
        etLenMin = findViewById(R.id.etLenMin);
        etLenMax = findViewById(R.id.etLenMax);
        etDiffMin = findViewById(R.id.etDiffMin);
        etDiffMax = findViewById(R.id.etDiffMax);
        rgParking = findViewById(R.id.rgParking);

        etDateFrom.setOnClickListener(v -> pickDate(etDateFrom));
        etDateTo.setOnClickListener(v -> pickDate(etDateTo));

        MaterialButton btnClear = findViewById(R.id.btnClear);
        MaterialButton btnApply = findViewById(R.id.btnApply);

        btnClear.setOnClickListener(v -> {
            etName.setText("");
            etLocation.setText("");
            etDateFrom.setText("");
            etDateTo.setText("");
            etLenMin.setText("");
            etLenMax.setText("");
            etDiffMin.setText("");
            etDiffMax.setText("");
            rgParking.check(R.id.rbAny);
        });

        btnApply.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra("name", safe(etName));
            data.putExtra("location", safe(etLocation));
            data.putExtra("dateFrom", safe(etDateFrom));
            data.putExtra("dateTo", safe(etDateTo));
            data.putExtra("lenMin", safe(etLenMin));
            data.putExtra("lenMax", safe(etLenMax));
            data.putExtra("diffMin", safe(etDiffMin));
            data.putExtra("diffMax", safe(etDiffMax));
            int parkingSel = rgParking.getCheckedRadioButtonId();
            Integer parking = null;
            if (parkingSel == R.id.rbYes) parking = 1;
            else if (parkingSel == R.id.rbNo) parking = 0;
            data.putExtra("parking", parking == null ? -1 : parking);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void pickDate(TextInputEditText target) {
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH), d = c.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            target.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
        }, y, m, d).show();
    }

    private String safe(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}