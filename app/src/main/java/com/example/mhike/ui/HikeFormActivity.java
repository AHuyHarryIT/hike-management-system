package com.example.mhike.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.mhike.R;
import com.example.mhike.data.HikeDao;
import com.example.mhike.model.Hike;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.Calendar;

public class HikeFormActivity extends AppCompatActivity {

    private TextInputEditText etName, etLocation, etDate, etLength, etDesc;
    private MaterialSwitch swParking;
    private Slider sliderDifficulty;
    private TextView tvDifficultyValue;
    private ImageView imgCover;

    private HikeDao hikeDao;
    private String mode = "add";
    private long editId = 0L;
    private Hike existing;

    private Uri selectedPhotoUri = null;
    private Uri cameraOutputUri = null;

    // --- Launchers ---
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openCamera();
                else toast("Camera permission denied");
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode() == RESULT_OK && res.getData() != null) {
                    Uri uri = res.getData().getData();
                    if (uri != null) {
                        int takeFlags = res.getData().getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        if (takeFlags == 0)
                            takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;

                        try {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (SecurityException ignored) {
                            // Some providers may not allow persistable permissions; safe to ignore
                        }

                        selectedPhotoUri = uri;
                        imgCover.setImageURI(uri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode() == RESULT_OK && cameraOutputUri != null) {
                    selectedPhotoUri = cameraOutputUri;
                    imgCover.setImageURI(selectedPhotoUri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_form);

        hikeDao = new HikeDao(this);

        MaterialToolbar tb = findViewById(R.id.toolbarForm);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // --- Bind UI ---
        etName = findViewById(R.id.etName);
        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        etLength = findViewById(R.id.etLength);
        etDesc = findViewById(R.id.etDesc);
        swParking = findViewById(R.id.swParking);
        sliderDifficulty = findViewById(R.id.sliderDifficulty);
        tvDifficultyValue = findViewById(R.id.tvDifficultyValue);
        imgCover = findViewById(R.id.imgCover);

        // --- Mode check (add/edit) ---
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";
        if ("edit".equals(mode)) {
            editId = getIntent().getLongExtra("id", 0L);
            if (editId != 0L) existing = hikeDao.findById(editId);
        }

        if (existing != null) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Hike");
            etName.setText(existing.name);
            etLocation.setText(existing.location);
            etDate.setText(existing.date);
            etLength.setText(String.valueOf(existing.lengthKm));
            etDesc.setText(existing.description);
            swParking.setChecked(existing.parking);
            sliderDifficulty.setValue(existing.difficulty);

            if (existing.photoUri != null && !existing.photoUri.isEmpty()) {
                selectedPhotoUri = Uri.parse(existing.photoUri);
                imgCover.setImageURI(selectedPhotoUri);
            }
        } else {
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(getString(R.string.add_hike));
        }

        // --- Difficulty slider ---
        tvDifficultyValue.setText("Selected: " + getDifficultyLabel((int) sliderDifficulty.getValue()));
        sliderDifficulty.addOnChangeListener((s, value, fromUser) ->
                tvDifficultyValue.setText("Selected: " + getDifficultyLabel((int) value)));

        // --- Date picker ---
        etDate.setOnClickListener(v -> showDatePicker());

        // --- Photo buttons ---
        findViewById(R.id.btnChoose).setOnClickListener(v -> openGallery());
        findViewById(R.id.btnCamera).setOnClickListener(v -> ensureCameraThenOpen());

        // --- Save button ---
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

        final Hike pending = new Hike(
                name, location, date, parking, lengthKm, difficulty, desc,
                selectedPhotoUri == null ? null : selectedPhotoUri.toString()
        );

        final boolean isEdit = "edit".equals(mode) && existing != null;

        String preview = "Name: " + pending.name + "\n"
                + "Location: " + pending.location + "\n"
                + "Date: " + pending.date + "\n"
                + "Parking: " + (pending.parking ? "Yes" : "No") + "\n"
                + "Length: " + pending.lengthKm + " km\n"
                + "Difficulty: " + getDifficultyLabel(pending.difficulty)
                + " (D" + pending.difficulty + ")\n"
                + "Description: " + (TextUtils.isEmpty(pending.description) ? "(none)" : pending.description);

        new MaterialAlertDialogBuilder(this)
                .setTitle(isEdit ? "Save changes?" : getString(R.string.confirm))
                .setMessage(preview)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(isEdit ? "Update" : getString(R.string.confirm), (d, w) -> {
                    if (isEdit) {
                        pending.id = existing.id;
                        int rows = hikeDao.update(pending);
                        Toast.makeText(this, rows > 0 ? "Updated" : "Update failed", Toast.LENGTH_SHORT).show();
                        if (rows > 0) finish();
                    } else {
                        long id = hikeDao.insert(pending);
                        Toast.makeText(this, id > 0 ? "Saved" : "Save failed", Toast.LENGTH_SHORT).show();
                        if (id > 0) finish();
                    }
                })
                .show();
    }

    // --- Helpers ---
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

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("image/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickImageLauncher.launch(i);
    }

    private void ensureCameraThenOpen() {
        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
    }

    private void openCamera() {
        try {
            File imagesDir = new File(getCacheDir(), "images");
            if (!imagesDir.exists()) imagesDir.mkdirs();
            File temp = File.createTempFile("hike_", ".jpg", imagesDir);
            cameraOutputUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", temp);
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri);
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePhotoLauncher.launch(i);
        } catch (Exception e) {
            toast("Cannot open camera: " + e.getMessage());
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}