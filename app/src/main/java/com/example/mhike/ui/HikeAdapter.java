package com.example.mhike.ui;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mhike.R;
import com.example.mhike.model.Hike;
import java.util.ArrayList;
import java.util.List;

public class HikeAdapter extends RecyclerView.Adapter<HikeAdapter.VH> {

    public interface OnItemClick {
        void onClick(Hike item);
    }

    private final List<Hike> data = new ArrayList<>();
    private final OnItemClick onItemClick;

    public HikeAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void submitList(List<Hike> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hike, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Hike item = data.get(position);
        h.tvName.setText(item.name);
        h.tvLocation.setText(item.location);
        h.tvDate.setText(item.date);
        String meta = (item.parking ? "Parking • " : "No parking • ") +
                item.lengthKm + " km • " + getDifficultyLabel(item.difficulty);
        h.tvMeta.setText(meta);
        h.itemView.setOnClickListener(v -> onItemClick.onClick(item));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String getDifficultyLabel(int diff) {
        switch (diff) {
            case 1: return "Very Easy";
            case 2: return "Easy";
            case 3: return "Medium";
            case 4: return "Hard";
            case 5: return "Very Hard";
            default: return "Unknown";
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvDate, tvMeta;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMeta = itemView.findViewById(R.id.tvMeta);
        }
    }
}