package com.example.mhike.ui;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhike.R;
import com.example.mhike.model.Observation;

import java.util.ArrayList;
import java.util.List;

public class ObservationAdapter extends RecyclerView.Adapter<ObservationAdapter.VH> {

    public interface OnItemClick {
        void onClick(Observation o);
    }

    private final List<Observation> data = new ArrayList<>();
    private final OnItemClick onItemClick;

    public ObservationAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void submit(List<Observation> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_observation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Observation o = data.get(position);
        h.tvNote.setText(o.note);
        h.tvTime.setText("Time: " + o.datetime);
        h.tvComments.setText(o.comments == null || o.comments.isEmpty() ? "(no comments)" : o.comments);
        h.itemView.setOnClickListener(v -> onItemClick.onClick(o));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNote, tvTime, tvComments;

        VH(@NonNull View itemView) {
            super(itemView);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvComments = itemView.findViewById(R.id.tvComments);
        }
    }
    public Observation getItem(int pos) {
        return (pos >= 0 && pos < data.size()) ? data.get(pos) : null;
    }
}