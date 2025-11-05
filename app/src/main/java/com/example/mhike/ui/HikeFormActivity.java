package com.example.mhike.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mhike.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class HikeFormActivity extends AppCompatActivity {

    private TextInputEditText etName, etLocation, etDate, etLength, etDesc;
    private MaterialSwitch swParking;
    private Slider sliderDifficulty;
    private TextView tvDifficultyValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_form);

        MaterialToolbar tb = findViewById(R.id.toolbarForm);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        etName = findViewById(R.id.etName);
        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        etLength = findViewById(R.id.etLength);
        etDesc = findViewById(R.id.etDesc);
        swParking = findViewById(R.id.swParking);

        sliderDifficulty = findViewById(R.id.sliderDifficulty);
        tvDifficultyValue = findViewById(R.id.tvDifficultyValue);

        // Initial label from current slider value (default is 3)
        tvDifficultyValue.setText("Selected: " + getDifficultyLabel((int) sliderDifficulty.getValue()));

        // Live updates
        sliderDifficulty.addOnChangeListener((s, value, fromUser) ->
                tvDifficultyValue.setText("Selected: " + getDifficultyLabel((int) value)));

        etDate.setOnClickListener(v -> showDatePicker());

        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            int difficulty = (int) sliderDifficulty.getValue();
            String msg = "Ready to save:\n"
                    + "Name=" + safe(etName)
                    + ", Location=" + safe(etLocation)
                    + ", Date=" + safe(etDate)
                    + ", Parking=" + (swParking.isChecked() ? "Yes" : "No")
                    + ", Length=" + safe(etLength)
                    + ", Difficulty=" + getDifficultyLabel(difficulty) + " (D" + difficulty + ")";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            // Next: validate & insert into SQLite, then finish()
        });
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

    private String safe(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH), d = c.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String val = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etDate.setText(val);
        }, y, m, d).show();
    }
}