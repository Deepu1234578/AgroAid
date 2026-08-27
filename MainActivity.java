package com.example.agroaid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.*;
import com.google.firebase.auth.FirebaseAuth;


import android.location.Address;
import android.location.Geocoder;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    // Cards
    CardView btnAnalysis, btnServices;

    // Logout
    ImageView btnLogout;

    // Bottom Navigation
    LinearLayout navHome, navFields, navMarket, navProfile;

    // Firebase
    FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;


    // Weather Text
    TextView txtTemp, txtCondition, txtLocation;

    // Scans RecyclerView
    RecyclerView rvRecentScans;
    LinearLayout emptyScansView;
    ScanAdapter scanAdapter;
    List<ScanModel> scanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();


        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Bind views
        txtTemp = findViewById(R.id.txtTemp);
        txtCondition = findViewById(R.id.txtCondition);
        txtLocation = findViewById(R.id.txtLocation);
        btnAnalysis = findViewById(R.id.btnAnalysis);
        btnServices = findViewById(R.id.btnServices);
        btnLogout = findViewById(R.id.btnLogout);
        navHome = findViewById(R.id.navHome);
        navFields = findViewById(R.id.navFields);
        navMarket = findViewById(R.id.navMarket);
        navProfile = findViewById(R.id.navProfile);
        rvRecentScans = findViewById(R.id.rvRecentScans);
        emptyScansView = findViewById(R.id.emptyScansView);

        // RecyclerView setup
        scanAdapter = new ScanAdapter(scanList);
        rvRecentScans.setLayoutManager(new LinearLayoutManager(this));
        rvRecentScans.setAdapter(scanAdapter);
        rvRecentScans.setNestedScrollingEnabled(false);

        // Location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            loadWeather();
        }

        // Load real scans from Firestore
        loadRecentScans();

        // ANALYSIS (Scan Plant)
        btnAnalysis.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AnalysisActivity.class)));

        // MARKET (was Services)
        btnServices.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MarketActivity.class)));

        // NAV - HOME
        navHome.setOnClickListener(v -> recreate());

        // NAV - SCAN
        navFields.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AnalysisActivity.class)));

        // NAV - MARKET
        navMarket.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MarketActivity.class)));

        // NAV - PROFILE
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileSetupActivity.class)));

        // View All Scans
        findViewById(R.id.tvViewAllScans).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AnalysisActivity.class)));

        // See Market from demand strip
        findViewById(R.id.tvSeeAllDemand).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MarketActivity.class)));

        // LOGOUT
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            loadRecentScans();
        }
    }


    /**
     * Loads the last 5 scans for the current user from Firestore.
     * Collection: users/{uid}/scans  — ordered by timestamp descending, limit 5
     */
    private void loadRecentScans() {
        String uid = mAuth.getCurrentUser().getUid();

        SupabaseHelper.getRecentScans(uid, new SupabaseHelper.ScansCallback() {
            @Override
            public void onSuccess(org.json.JSONArray scans) {
                runOnUiThread(() -> {
                    scanList.clear();
                    try {
                        for (int i = 0; i < scans.length(); i++) {
                            org.json.JSONObject obj = scans.getJSONObject(i);

                            ScanModel scan = new ScanModel(
                                    obj.optString("plant_name", "Unknown"),
                                    obj.optString("disease", "—"),
                                    obj.optString("image_url", ""),
                                    obj.optInt("health_score", 0),
                                    obj.optBoolean("healthy", true),
                                    null  // no Firestore timestamp needed
                            );
                            scanList.add(scan);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("SCANS", "Parse error: " + e.getMessage());
                    }

                    if (scanList.isEmpty()) {
                        rvRecentScans.setVisibility(View.GONE);
                        emptyScansView.setVisibility(View.VISIBLE);
                    } else {
                        rvRecentScans.setVisibility(View.VISIBLE);
                        emptyScansView.setVisibility(View.GONE);
                        scanAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e("SCANS", "Supabase error: " + error);
                    rvRecentScans.setVisibility(View.GONE);
                    emptyScansView.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void loadWeather() {
        LocationHelper.getLocation(this, location -> {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    txtLocation.setText("📍 " + addresses.get(0).getLocality());
                }
            } catch (Exception e) {
                txtLocation.setText("📍 Location Error");
            }
            new Thread(() -> {
                String weather = WeatherHelper.getWeather(lat, lon);
                runOnUiThread(() -> {
                    txtTemp.setText(weather);
                    txtCondition.setText("Live Weather");
                });
            }).start();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadWeather();
        } else {
            txtLocation.setText("Location Permission Denied");
        }

    }
}
