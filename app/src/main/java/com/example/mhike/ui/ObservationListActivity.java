package com.example.mhike.ui;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhike.R;
import com.example.mhike.data.ObservationDao;
import com.example.mhike.model.Observation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.recyclerview.widget.ItemTouchHelper;

public class ObservationListActivity extends AppCompatActivity {

    private long hikeId;
    private ObservationDao obsDao;
    private ObservationAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_list);

        hikeId = getIntent().getLongExtra("hike_id", 0);
        if (hikeId == 0) {
            Toast.makeText(this, "Invalid hike", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        obsDao = new ObservationDao(this);

        MaterialToolbar tb = findViewById(R.id.toolbarObs);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        RecyclerView rv = findViewById(R.id.rvObs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ObservationAdapter(o -> {
            Intent i = new Intent(this, ObservationFormActivity.class);
            i.putExtra("mode", "edit");
            i.putExtra("id", o.id);
            i.putExtra("hike_id", hikeId);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddObs);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(this, ObservationFormActivity.class);
            i.putExtra("mode", "add");
            i.putExtra("hike_id", hikeId);
            startActivity(i);
        });


        ItemTouchHelper ith = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder tgt) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                Observation o = adapterPositionToItem(pos);
                if (o == null) {
                    adapter.notifyItemChanged(pos);
                    return;
                }
                new MaterialAlertDialogBuilder(ObservationListActivity.this)
                        .setTitle(getString(R.string.delete_observation))
                        .setMessage(getString(R.string.delete_confirm_msg))
                        .setNegativeButton(R.string.cancel, (d, w) -> {
                            d.dismiss();
                            adapter.notifyItemChanged(pos);
                        })
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            obsDao.delete(o.id);
                            adapter.submit(new ArrayList<>(obsDao.listByHike(hikeId)));
                            Toast.makeText(ObservationListActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
        });
        ith.attachToRecyclerView(findViewById(R.id.rvObs));
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.submit(new ArrayList<>(obsDao.listByHike(hikeId)));
    }

    private Observation adapterPositionToItem(int position) {
        return adapter.getItem(position);
    }
}