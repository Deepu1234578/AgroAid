package com.example.agroaid;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class ScanDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_scan_detail);

            // Back button
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());

            // Get data passed from adapter
            String plantName  = getIntent().getStringExtra("plantName");
            String disease    = getIntent().getStringExtra("disease");
            int healthScore   = getIntent().getIntExtra("healthScore", 0);
            boolean healthy   = getIntent().getBooleanExtra("healthy", true);
            String imageUrl   = getIntent().getStringExtra("imageUrl");
            String date       = getIntent().getStringExtra("date");

            // Set values safely
            setText(R.id.detailPlantName, plantName != null ? plantName : "Unknown Plant");
            setText(R.id.detailDisease,   disease   != null ? disease   : "—");
            setText(R.id.detailHealth,    healthScore + "%");
            setText(R.id.detailDate,      date      != null ? date      : "—");

            // Status badge
            TextView statusView = findViewById(R.id.detailStatus);
            if (healthy) {
                statusView.setText("✅  HEALTHY PLANT");
                statusView.setTextColor(android.graphics.Color.parseColor("#1B5E20"));
                statusView.setBackgroundColor(android.graphics.Color.parseColor("#C8E6C9"));
            } else {
                statusView.setText("⚠️  DISEASE DETECTED");
                statusView.setTextColor(android.graphics.Color.parseColor("#B71C1C"));
                statusView.setBackgroundColor(android.graphics.Color.parseColor("#FFCDD2"));
            }

            // Disease text color
            TextView diseaseView = findViewById(R.id.detailDisease);
            diseaseView.setTextColor(healthy
                    ? android.graphics.Color.parseColor("#2E7D32")
                    : android.graphics.Color.parseColor("#E65100"));

            // Load image
            ImageView img = findViewById(R.id.detailImage);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_plant_placeholder)
                        .error(R.drawable.ic_plant_placeholder)
                        .centerCrop()
                        .into(img);
            } else {
                img.setImageResource(R.drawable.ic_plant_placeholder);
            }

        } catch (Exception e) {
            android.util.Log.e("ScanDetail", "Error: " + e.getMessage(), e);
            finish(); // only close if truly broken
        }
    }

    private void setText(int viewId, String text) {
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText(text);
    }
}