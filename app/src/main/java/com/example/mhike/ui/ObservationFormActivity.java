package com.example.mhike.ui;


import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mhike.R;
import com.example.mhike.data.ObservationDao;
import com.example.mhike.model.Observation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.view.Menu;
import android.view.MenuItem;

public class ObservationFormActivity extends AppCompatActivity {

    private TextInputEditText etNote, etComments;
    private TextView tvTimeNow;
    private ObservationDao obsDao;

    private String mode = "add";
    private long hikeId = 0;
    private long editId = 0;
    private Observation existing;
    private long nowSec;

    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_form);

        obsDao = new ObservationDao(this);

        MaterialToolbar tb = findViewById(R.id.toolbarObsForm);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        etNote = findViewById(R.id.etNote);
        etComments = findViewById(R.id.etComments);
        tvTimeNow = findViewById(R.id.tvTimeNow);

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";
        hikeId = getIntent().getLongExtra("hike_id", 0);

        if ("edit".equals(mode)) {
            editId = getIntent().getLongExtra("id", 0);
            existing = obsDao.findById(editId);
            if (existing == null) {
                Toast.makeText(this, "Observation not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            etNote.setText(existing.note);
            etComments.setText(existing.comments);
            nowSec = existing.timeSec;
            tvTimeNow.setText("Time: " + fmt.format(new Date(nowSec * 1000L)));
            getSupportActionBar().setTitle("Edit Observation");
        } else {
            nowSec = System.currentTimeMillis() / 1000L;
            tvTimeNow.setText("Time: " + fmt.format(new Date(nowSec * 1000L)));
            getSupportActionBar().setTitle(getString(R.string.add_observation));
        }

        MaterialButton btnSave = findViewById(R.id.btnSaveObs);
        btnSave.setOnClickListener(v -> trySave());
    }

    private void trySave() {
        String note = safe(etNote);
        String comments = safe(etComments);

        if (TextUtils.isEmpty(note)) {
            etNote.setError("Required");
            etNote.requestFocus();
            return;
        }

        final boolean isEdit = "edit".equals(mode) && existing != null;
        final Observation pending = isEdit
                ? new Observation(existing.id, existing.hikeId, note, existing.timeSec, comments)
                : new Observation(hikeId, note, nowSec, comments);

        String preview = "Note: " + pending.note + "\n"
                + "Time: " + fmt.format(new Date(pending.timeSec * 1000L)) + "\n"
                + "Comments: " + (TextUtils.isEmpty(pending.comments) ? "(none)" : pending.comments);

        new MaterialAlertDialogBuilder(this)
                .setTitle(isEdit ? "Save changes?" : getString(R.string.confirm))
                .setMessage(preview)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(isEdit ? "Update" : getString(R.string.save), (d, w) -> {
                    if (isEdit) {
                        int rows = obsDao.update(pending);
                        Toast.makeText(this, rows > 0 ? "Updated" : "Update failed", Toast.LENGTH_SHORT).show();
                    } else {
                        long id = obsDao.insert(pending);
                        Toast.makeText(this, id > 0 ? "Saved" : "Save failed", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .show();
    }

    private String safe(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ("edit".equals(mode) && existing != null) {
            getMenuInflater().inflate(R.menu.menu_observation_form, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_obs) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.delete_observation))
                .setMessage(getString(R.string.delete_confirm_msg))
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.delete, (d, w) -> {
                    int rows = obsDao.delete(existing.id);
                    Toast.makeText(this, rows > 0 ? "Deleted" : "Delete failed", Toast.LENGTH_SHORT).show();
                    if (rows > 0) finish();
                })
                .show();
    }
}