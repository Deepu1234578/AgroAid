package com.example.agroaid;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import java.io.File;

public class AnalysisActivity extends AppCompatActivity {

    private static final String TAG = "AnalysisActivity";
    private static final int CAMERA_REQUEST = 1;

    // — Images —
    ImageView imgCaptured, imgPlantReference;

    // — Plant info —
    TextView txtPlantName, txtScientificName, txtConfidence, txtWeather;

    // — Health card views —
    TextView txtHealthReason, txtAnalysisDetail, txtPrevention;

    // — Disease card views —
    TextView txtDiseaseTitle, txtDiseaseReason, txtDiseaseAnalysis;
    TextView txtMedicine, txtPrevention2;

    // — Cards & loading —
    CardView cardMain, cardHealth, cardDisease;
    LinearLayout loadingLayout;
    ProgressBar progressBar;

    // — Buttons —
    MaterialButton btnScan, btnDashboard;

    // — State —
    File photoFile;
    PlantApiClient plantApiClient;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    double lat = 12.97, lon = 77.59;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        try {
            bindViews();
        } catch (Exception e) {
            Log.e(TAG, "bindViews failed: " + e.getMessage(), e);
            finish();
            return;
        }

        plantApiClient = new PlantApiClient(this, BuildConfig.PLANT_ID_API_KEY);

        // Get GPS — safe, won't crash if location unavailable
        try {
            LocationHelper.getLocation(this, location -> {
                if (location != null) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Location unavailable, using default");
        }

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) startCameraIntent();
                    else showError("Camera permission is required to scan plants.");
                }
        );

        btnScan.setOnClickListener(v -> openCamera());
        btnDashboard.setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));
    }

    private void bindViews() {
        imgCaptured         = findViewById(R.id.imgCaptured);
        imgPlantReference   = findViewById(R.id.imgPlantReference);
        txtPlantName        = findViewById(R.id.txtPlantName);
        txtScientificName   = findViewById(R.id.txtScientificName);
        txtConfidence       = findViewById(R.id.txtConfidence);
        txtWeather          = findViewById(R.id.txtWeather);

        txtHealthReason     = findViewById(R.id.txtHealthReason);
        txtAnalysisDetail   = findViewById(R.id.txtAnalysisDetail);
        txtPrevention       = findViewById(R.id.txtPrevention);

        txtDiseaseTitle     = findViewById(R.id.txtDiseaseTitle);
        txtDiseaseReason    = findViewById(R.id.txtDiseaseReason);
        txtDiseaseAnalysis  = findViewById(R.id.txtDiseaseAnalysis);
        txtMedicine         = findViewById(R.id.txtMedicine);
        txtPrevention2      = findViewById(R.id.txtPrevention2);

        cardMain            = findViewById(R.id.cardMain);
        cardHealth          = findViewById(R.id.cardHealth);
        cardDisease         = findViewById(R.id.cardDisease);

        loadingLayout       = findViewById(R.id.loadingLayout);
        progressBar         = findViewById(R.id.progressBar);
        btnScan             = findViewById(R.id.btnScan);
        btnDashboard        = findViewById(R.id.btnDashboard);

        if (btnScan == null)      throw new RuntimeException("btnScan not found in layout");
        if (btnDashboard == null) throw new RuntimeException("btnDashboard not found in layout");
        if (cardMain == null)     throw new RuntimeException("cardMain not found in layout");
        if (cardHealth == null)   throw new RuntimeException("cardHealth not found in layout");
        if (cardDisease == null)  throw new RuntimeException("cardDisease not found in layout");
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCameraIntent();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCameraIntent() {
        try {
            photoFile = new File(getExternalCacheDir(), "plant_scan.jpg");
            Uri photoURI = FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(intent, CAMERA_REQUEST);
        } catch (Exception e) {
            showError("Could not open camera: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                if (bitmap != null) {
                    imgCaptured.setImageBitmap(bitmap);
                    // ✅ Clear the reference image to avoid showing stale placeholder
                    imgPlantReference.setImageResource(R.drawable.ic_plant_placeholder);
                    analyzePlant(bitmap);
                } else {
                    showError("Could not read photo. Try again.");
                }
            } catch (Exception e) {
                showError("Photo error: " + e.getMessage());
            }
        }
    }

    private void analyzePlant(Bitmap bitmap) {
        showLoading(true);
        cardMain.setVisibility(View.GONE);
        cardHealth.setVisibility(View.GONE);
        cardDisease.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                // 1. Identify plant
                JSONObject apiResult   = plantApiClient.analyze(bitmap);
                JSONObject plantObj    = apiResult.optJSONObject("plant");

                String scientificName  = "Unknown Plant";
                String commonName      = "Unknown Plant";
                int    confidence      = 0;
                // ✅ Get image URL directly from Plant.id API response first
                String plantImageUrl   = "";

                if (plantObj != null) {
                    scientificName = plantObj.optString("name", "Unknown Plant");
                    commonName     = plantObj.optString("common", scientificName);
                    confidence     = plantObj.optInt("confidence", 0);
                    // Plant.id often returns a similar_images array — grab first one
                    plantImageUrl  = plantObj.optString("image", "");
                }

                // 2. Weather
                String weather = "";
                try { weather = WeatherHelper.getWeather(lat, lon); }
                catch (Exception e) { weather = "Weather unavailable"; }

                // 3. Claude AI analysis
                String aiAnalysis = AiHelper.analyzePlant(scientificName, confidence, weather);

                // 4. Parse sections
                String diseaseStatus  = extractSection(aiAnalysis, "STATUS");
                String healthReason   = extractSection(aiAnalysis, "REASON");
                String analysisDetail = extractSection(aiAnalysis, "ANALYSIS");
                String medicine       = extractSection(aiAnalysis, "MEDICINE");
                String prevention     = extractSection(aiAnalysis, "PREVENTION");

                boolean isHealthy = diseaseStatus.toLowerCase().contains("healthy");

                // 5. ✅ FIXED: Fetch reference image from Wikipedia ONLY if API didn't provide one.
                //    This ensures imgPlantReference shows a real species photo, not the user's scan.
                if (plantImageUrl.isEmpty()) {
                    try {
                        plantImageUrl = WikiImageHelper.fetchPlantImage(scientificName);
                        Log.d(TAG, "Wiki image URL for " + scientificName + ": " + plantImageUrl);
                    } catch (Exception e) {
                        Log.w(TAG, "Wiki image fetch failed: " + e.getMessage());
                    }
                }

                // 6. Fallback: try common name if scientific name returned nothing
                if (plantImageUrl.isEmpty() && !commonName.equals(scientificName)) {
                    try {
                        plantImageUrl = WikiImageHelper.fetchPlantImage(commonName);
                        Log.d(TAG, "Wiki image (common name) URL: " + plantImageUrl);
                    } catch (Exception e) {
                        Log.w(TAG, "Wiki image (common) failed: " + e.getMessage());
                    }
                }



                saveStats(!isHealthy);
                saveHistory(scientificName + " | " + confidence + "% | " + diseaseStatus);

// ✅ ADD THIS — saves scan to Firestore so it appears on home screen
                // Supabase save — crops table
                try {
                    JSONObject json = new JSONObject();
                    json.put("crop_name", commonName);
                    json.put("disease",   diseaseStatus);
                    json.put("temperature", weather);
                    SupabaseHelper.insertData("crops", json);
                } catch (Exception e) {
                    e.printStackTrace();
                }

// ✅ Supabase save — scans table (shows on home screen)
                String currentUid = com.google.firebase.auth.FirebaseAuth
                        .getInstance().getCurrentUser() != null
                        ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                        : "anonymous";

                SupabaseHelper.saveScan(
                        currentUid,
                        commonName,
                        diseaseStatus.isEmpty() ? (isHealthy ? "Healthy" : "Issue Detected") : diseaseStatus,
                        plantImageUrl,
                        confidence,
                        isHealthy
                );
                // Final values for UI thread
                final String fScientific = scientificName;
                final String fCommon     = commonName;
                final int    fConf       = confidence;
                final String fWeather    = weather;
                final String fStatus     = diseaseStatus.isEmpty()
                        ? (isHealthy ? "✅ Healthy" : "⚠ Issue Detected")
                        : diseaseStatus;
                final String fReason     = healthReason.isEmpty() ? "No visible issues detected." : healthReason;
                final String fAnalysis   = analysisDetail.isEmpty() ? "Plant analyzed successfully." : analysisDetail;
                final String fMedicine   = medicine;
                final String fPrevention = prevention;
                final String fImgUrl     = plantImageUrl;
                final boolean fHealthy   = isHealthy;

                runOnUiThread(() -> {
                    showLoading(false);

                    // Plant info card
                    cardMain.setVisibility(View.VISIBLE);
                    txtPlantName.setText(fCommon.equals(fScientific) ? fScientific : fCommon);
                    txtScientificName.setText(fScientific);
                    txtConfidence.setText("⭐ " + fConf + "% Match");
                    txtWeather.setText("🌦 " + fWeather);

                    if (fHealthy) {
                        txtHealthReason.setText(fReason);
                        txtAnalysisDetail.setText(fAnalysis);
                        txtPrevention.setText(fPrevention.isEmpty() ? "No specific prevention needed." : fPrevention);
                        cardHealth.setVisibility(View.VISIBLE);
                        cardDisease.setVisibility(View.GONE);
                    } else {
                        txtDiseaseTitle.setText(fStatus);
                        txtDiseaseReason.setText(fReason);
                        txtDiseaseAnalysis.setText(fAnalysis);
                        txtMedicine.setText(fMedicine.isEmpty() ? "Consult a local agronomist." : fMedicine);
                        txtPrevention2.setText(fPrevention.isEmpty() ? "Monitor plant regularly." : fPrevention);
                        cardDisease.setVisibility(View.VISIBLE);
                        cardHealth.setVisibility(View.GONE);
                    }

                    // ✅ FIXED: Load reference image into imgPlantReference (NOT imgCaptured)
                    // imgCaptured already has the user's photo set in onActivityResult.
                    if (!fImgUrl.isEmpty()) {
                        Glide.with(AnalysisActivity.this)
                                .load(fImgUrl)
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.ic_plant_placeholder)
                                        .error(R.drawable.ic_plant_placeholder)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                                .transition(DrawableTransitionOptions.withCrossFade(400))
                                .into(imgPlantReference);
                    } else {
                        // No image found — keep placeholder
                        imgPlantReference.setImageResource(R.drawable.ic_plant_placeholder);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Analysis error: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Analysis failed: " + e.getMessage());
                });
            }
        }).start();
    }

    private String extractSection(String text, String label) {
        if (text == null || text.isEmpty()) return "";
        for (String line : text.split("\n")) {
            if (line.toUpperCase().startsWith(label + ":")) {
                return line.substring(label.length() + 1).trim();
            }
        }
        return "";
    }

    private void showLoading(boolean show) {
        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        btnScan.setEnabled(!show);
    }

    private void showError(String msg) {
        showLoading(false);
        cardMain.setVisibility(View.VISIBLE);
        cardHealth.setVisibility(View.VISIBLE);
        cardDisease.setVisibility(View.GONE);
        txtAnalysisDetail.setText("❌ " + msg);
        txtHealthReason.setText("");
        txtPrevention.setText("");
    }

    private void saveStats(boolean diseased) {
        SharedPreferences prefs = getSharedPreferences("AgroAid", MODE_PRIVATE);
        String key = diseased ? "diseased" : "healthy";
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply();
    }

    private void saveHistory(String data) {
        SharedPreferences prefs = getSharedPreferences("AgroAid", MODE_PRIVATE);
        String old = prefs.getString("history", "");
        prefs.edit().putString("history", old + "\n\n" + data).apply();
    }
    private void saveScanToFirestore(String plantName, String disease,
                                     String imageUrl, int healthScore, boolean isHealthy) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> scan = new HashMap<>();
        scan.put("plantName", plantName);
        scan.put("disease", disease);
        scan.put("imageUrl", imageUrl);
        scan.put("healthScore", healthScore);
        scan.put("healthy", isHealthy);
        scan.put("timestamp", FieldValue.serverTimestamp());

        db.collection("users")
                .document(uid)
                .collection("scans")
                .add(scan)
                .addOnSuccessListener(ref ->
                        Log.d("SCANS", "✅ Scan saved: " + ref.getId()))
                .addOnFailureListener(e ->
                        Log.e("SCANS", "❌ Save failed: " + e.getMessage()));
    }
}