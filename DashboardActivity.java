package com.example.agroaid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    PieChart pieChart;
    TextView txtWeather, txtHealthyCount, txtDiseasedCount;
    LinearLayout historyContainer;   // dynamic container for scan history cards

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // ── Bind views ──
        pieChart         = findViewById(R.id.pieChart);
        txtWeather       = findViewById(R.id.txtWeather);
        txtHealthyCount  = findViewById(R.id.txtHealthyCount);
        txtDiseasedCount = findViewById(R.id.txtDiseasedCount);
        historyContainer = findViewById(R.id.historyContainer);   // <-- add this id to layout

        // ── Back-to-scan button ──
        findViewById(R.id.btnGoScan).setOnClickListener(v ->
                startActivity(new Intent(this, AnalysisActivity.class)));

        // ── Weather ──
        LocationHelper.getLocation(this, location -> {
            if (location == null) {
                txtWeather.setText("🌦 Location unavailable");
                return;
            }
            new Thread(() -> {
                String weather = WeatherHelper.getWeather(
                        location.getLatitude(),
                        location.getLongitude()
                );
                runOnUiThread(() -> txtWeather.setText("🌦 " + weather));
            }).start();
        });

        // ── Stats ──
        SharedPreferences prefs = getSharedPreferences("AgroAid", MODE_PRIVATE);
        int healthy  = prefs.getInt("healthy", 0);
        int diseased = prefs.getInt("diseased", 0);

        txtHealthyCount.setText(String.valueOf(healthy));
        txtDiseasedCount.setText(String.valueOf(diseased));

        // ── Pie Chart ──
        setupPieChart(healthy, diseased);

        // ── History ──
        String rawHistory = prefs.getString("history", "").trim();
        populateHistory(rawHistory);
    }

    /**
     * Parses the stored history string and renders each entry as a
     * polished card instead of dumping raw text.
     *
     * Each entry is saved by AnalysisActivity as:
     *   "ScientificName | confidence% | StatusText"
     * e.g. "Rosa abietina | 43% | Diseased"
     */
    private void populateHistory(String rawHistory) {
        if (historyContainer == null) return;
        historyContainer.removeAllViews();

        if (rawHistory.isEmpty()) {
            addEmptyState();
            return;
        }

        // Split on blank lines (entries are joined with "\n\n")
        String[] entries = rawHistory.split("\n\n");

        // Show newest first
        int shown = 0;
        for (int i = entries.length - 1; i >= 0 && shown < 20; i--) {
            String entry = entries[i].trim();
            if (entry.isEmpty()) continue;
            addHistoryCard(entry);
            shown++;
        }

        if (shown == 0) addEmptyState();
    }

    /**
     * Parses one history entry string and inflates a card for it.
     * Format: "PlantName | confidence% | StatusText"
     */
    private void addHistoryCard(String entry) {
        // ── Parse ──
        String[] parts = entry.split("\\|");
        String plantName   = parts.length > 0 ? parts[0].trim() : "Unknown Plant";
        String confidence  = parts.length > 1 ? parts[1].trim() : "—";
        String status      = parts.length > 2 ? parts[2].trim() : "—";

        boolean isHealthy  = status.toLowerCase().contains("healthy");

        // ── Card ──
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(10));
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(16));
        card.setCardElevation(dpToPx(3));
        card.setCardBackgroundColor(Color.WHITE);
        card.setUseCompatPadding(true);

        // ── Row inside card ──
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pad = dpToPx(14);
        row.setPadding(pad, pad, pad, pad);

        // Left icon circle
        LinearLayout iconCircle = new LinearLayout(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(48), dpToPx(48));
        iconCircle.setLayoutParams(iconParams);
        iconCircle.setGravity(Gravity.CENTER);
        int bgColor = isHealthy
                ? Color.parseColor("#E8F5E9")
                : Color.parseColor("#FFEBEE");
        iconCircle.setBackgroundColor(bgColor);

        TextView iconView = new TextView(this);
        iconView.setText(isHealthy ? "🌿" : "🦠");
        iconView.setTextSize(22);
        iconCircle.addView(iconView);

        // Center: plant name + status
        LinearLayout textCol = new LinearLayout(this);
        LinearLayout.LayoutParams textColParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textColParams.setMarginStart(dpToPx(12));
        textCol.setLayoutParams(textColParams);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView tvName = new TextView(this);
        tvName.setText(plantName);
        tvName.setTextColor(Color.parseColor("#1A2E1A"));
        tvName.setTextSize(14);
        tvName.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        TextView tvStatus = new TextView(this);
        tvStatus.setText(status);
        tvStatus.setTextSize(12);
        tvStatus.setTextColor(isHealthy
                ? Color.parseColor("#2E7D32")
                : Color.parseColor("#C62828"));
        tvStatus.setPadding(0, dpToPx(2), 0, 0);

        textCol.addView(tvName);
        textCol.addView(tvStatus);

        // Right: confidence chip
        TextView tvConf = new TextView(this);
        int chipBg = isHealthy
                ? Color.parseColor("#F1F8E9")
                : Color.parseColor("#FFF3E0");
        int chipText = isHealthy
                ? Color.parseColor("#1B5E20")
                : Color.parseColor("#E65100");
        tvConf.setText(confidence);
        tvConf.setTextColor(chipText);
        tvConf.setBackgroundColor(chipBg);
        tvConf.setTextSize(12);
        tvConf.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tvConf.setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6));

        row.addView(iconCircle);
        row.addView(textCol);
        row.addView(tvConf);
        card.addView(row);

        historyContainer.addView(card);
    }

    private void addEmptyState() {
        TextView tv = new TextView(this);
        tv.setText("No scans yet.\nTap Scan a Plant to get started.");
        tv.setTextColor(Color.parseColor("#9E9E9E"));
        tv.setTextSize(14);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dpToPx(16), 0, dpToPx(16));
        tv.setLayoutParams(params);
        historyContainer.addView(tv);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // ── Pie chart (unchanged) ──
    private void setupPieChart(int healthy, int diseased) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        if (healthy == 0 && diseased == 0) {
            entries.add(new PieEntry(1f, "No data yet"));
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColor(Color.parseColor("#E0E0E0"));
            dataSet.setValueTextColor(Color.parseColor("#9E9E9E"));
            dataSet.setValueTextSize(14f);
            dataSet.setDrawValues(false);
            pieChart.setData(new PieData(dataSet));
            pieChart.setCenterText("No Scans Yet");
            pieChart.setCenterTextSize(16f);
            pieChart.setCenterTextColor(Color.parseColor("#9E9E9E"));
        } else {
            if (healthy > 0)  entries.add(new PieEntry(healthy,  "Healthy"));
            if (diseased > 0) entries.add(new PieEntry(diseased, "Diseased"));

            PieDataSet dataSet = new PieDataSet(entries, "");
            ArrayList<Integer> colors = new ArrayList<>();
            if (healthy > 0)  colors.add(Color.parseColor("#43A047"));
            if (diseased > 0) colors.add(Color.parseColor("#E53935"));
            dataSet.setColors(colors);
            dataSet.setValueTextSize(16f);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(6f);
            pieChart.setData(new PieData(dataSet));
            int total = healthy + diseased;
            pieChart.setCenterText(total + "\nTotal Scans");
            pieChart.setCenterTextSize(16f);
            pieChart.setCenterTextColor(Color.parseColor("#1A2E1A"));
        }

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(48f);
        pieChart.setTransparentCircleRadius(52f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.parseColor("#F7F9F5"));
        pieChart.setTransparentCircleAlpha(80);
        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelTextSize(13f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(13f);
        legend.setTextColor(Color.parseColor("#37474F"));
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(20f);

        pieChart.animateY(1200, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }
}