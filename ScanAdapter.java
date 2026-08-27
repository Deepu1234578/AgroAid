package com.example.agroaid;

import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

/**
 * RecyclerView adapter that renders ScanModel items as scan cards.
 * Requires Glide in build.gradle:
 *   implementation 'com.github.bumptech.glide:glide:4.16.0'
 */
public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ScanViewHolder> {

    private final List<ScanModel> scanList;

    public ScanAdapter(List<ScanModel> scanList) {
        this.scanList = scanList;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemscancard, parent, false);
        return new ScanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        ScanModel scan = scanList.get(position);

        holder.tvPlantName.setText(scan.getPlantName());
        holder.tvDisease.setText(scan.getDisease());
        holder.tvDate.setText(scan.getFormattedDate());
        holder.tvHealthScore.setText(scan.getHealthScore() + "%");
        holder.tvHealthLabel.setText("Health");

        holder.itemView.setOnClickListener(v -> {
            android.content.Context ctx = v.getContext();
            android.content.Intent intent = new android.content.Intent(ctx, ScanDetailActivity.class);
            intent.putExtra("plantName", scan.getPlantName());
            intent.putExtra("disease", scan.getDisease());
            intent.putExtra("healthScore", scan.getHealthScore());
            intent.putExtra("healthy", scan.isHealthy());
            intent.putExtra("imageUrl", scan.getImageUrl());
            intent.putExtra("date", scan.getFormattedDate());
            ctx.startActivity(intent);
        });


        // Color the disease label and health badge based on healthy/diseased
        if (scan.isHealthy()) {
            holder.tvDisease.setTextColor(Color.parseColor("#2E7D32"));
            holder.healthBadge.setBackgroundColor(Color.parseColor("#F1F8E9"));
            holder.tvHealthScore.setTextColor(Color.parseColor("#2E7D32"));
            holder.tvHealthLabel.setTextColor(Color.parseColor("#1B5E20"));
        } else {
            holder.tvDisease.setTextColor(Color.parseColor("#E65100"));
            holder.healthBadge.setBackgroundColor(Color.parseColor("#FFF3E0"));
            holder.tvHealthScore.setTextColor(Color.parseColor("#E65100"));
            holder.tvHealthLabel.setTextColor(Color.parseColor("#BF360C"));
        }

        // Load thumbnail if imageUrl is present, otherwise show emoji
        if (scan.getImageUrl() != null && !scan.getImageUrl().isEmpty()) {
            holder.ivThumbnail.setVisibility(View.VISIBLE);
            holder.tvThumbnailEmoji.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext())
                    .load(scan.getImageUrl())
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setVisibility(View.GONE);
            holder.tvThumbnailEmoji.setVisibility(View.VISIBLE);
            holder.tvThumbnailEmoji.setText(scan.isHealthy() ? "🌿" : "🍂");
        }
    }


    @Override
    public int getItemCount() { return scanList.size(); }

    static class ScanViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlantName, tvDisease, tvDate, tvHealthScore, tvHealthLabel, tvThumbnailEmoji;
        ImageView ivThumbnail;
        LinearLayout healthBadge;

        ScanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlantName = itemView.findViewById(R.id.tvPlantName);
            tvDisease = itemView.findViewById(R.id.tvDisease);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvHealthScore = itemView.findViewById(R.id.tvHealthScore);
            tvHealthLabel = itemView.findViewById(R.id.tvHealthLabel);
            tvThumbnailEmoji = itemView.findViewById(R.id.tvThumbnailEmoji);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            healthBadge = itemView.findViewById(R.id.healthBadge);
        }
    }
}