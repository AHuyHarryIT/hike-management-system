package com.example.mhike.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mhike.R;
import com.example.mhike.data.HikeDao;
import com.example.mhike.model.Hike;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class HikeFormActivity extends AppCompatActivity {

    private TextInputEditText etName, etLocation, etDate, etLength, etDesc;
    private MaterialSwitch swParking;
    private Slider sliderDifficulty;
    private TextView tvDifficultyValue;
    private HikeDao hikeDao;

    private String mode = "add";
    private long editId = 0;
    private Hike existing; // when editing

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_form);

        hikeDao = new HikeDao(this);

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

        // Mode detection
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";
        if ("edit".equals(mode)) {
            editId = getIntent().getLongExtra("id", 0);
            if (editId != 0) {
                existing = hikeDao.findById(editId);
            }
        }

        if (existing != null) {
            getSupportActionBar().setTitle("Edit Hike");
            etName.setText(existing.name);
            etLocation.setText(existing.location);
            etDate.setText(existing.date);
            etLength.setText(String.valueOf(existing.lengthKm));
            etDesc.setText(existing.description);
            swParking.setChecked(existing.parking);
            sliderDifficulty.setValue(existing.difficulty);
        } else {
            getSupportActionBar().setTitle(getString(R.string.add_hike));
            // default slider value already set in XML; fine
        }

        tvDifficultyValue.setText("Selected: " + getDifficultyLabel((int) sliderDifficulty.getValue()));
        sliderDifficulty.addOnChangeListener((s, value, fromUser) ->
                tvDifficultyValue.setText("Selected: " + getDifficultyLabel((int) value)));

        etDate.setOnClickListener(v -> showDatePicker());

        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> trySave());
    }

    private void trySave() {
        String name = safe(etName);
        String location = safe(etLocation);
        String date = safe(etDate);
        String lengthStr = safe(etLength);
        String desc = safe(etDesc);
        boolean parking = swParking.isChecked();
        int difficulty = (int) sliderDifficulty.getValue();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Required");
            etLocation.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(date)) {
            etDate.setError("Required");
            etDate.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(lengthStr)) {
            etLength.setError("Required");
            etLength.requestFocus();
            return;
        }

        double lengthKm;
        try {
            lengthKm = Double.parseDouble(lengthStr);
        } catch (NumberFormatException e) {
            etLength.setError("Invalid number");
            etLength.requestFocus();
            return;
        }

        final Hike pending = new Hike(name, location, date, parking, lengthKm, difficulty, desc);
        final boolean isEdit = "edit".equals(mode) && existing != null;

        String preview = "Name: " + pending.name + "\n"
                + "Location: " + pending.location + "\n"
                + "Date: " + pending.date + "\n"
                + "Parking: " + (pending.parking ? "Yes" : "No") + "\n"
                + "Length: " + pending.lengthKm + " km\n"
                + "Difficulty: " + getDifficultyLabel(pending.difficulty) + " (D" + pending.difficulty + ")\n"
                + "Description: " + (TextUtils.isEmpty(pending.description) ? "(none)" : pending.description);

        new MaterialAlertDialogBuilder(this)
                .setTitle(isEdit ? "Save changes?" : getString(R.string.confirm))
                .setMessage(preview)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(isEdit ? "Update" : getString(R.string.confirm), (d, w) -> {
                    if (isEdit) {
                        pending.id = existing.id;
                        int rows = hikeDao.update(pending);
                        if (rows > 0) {
                            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        long id = hikeDao.insert(pending);
                        if (id > 0) {
                            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
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