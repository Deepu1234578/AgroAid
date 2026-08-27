package com.example.agroaid;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class SupabaseHelper {

    private static final String SUPABASE_URL = "https://awcnrwhctdttdhumbyaj.supabase.co";
    private static final String SUPABASE_KEY = "sb_publishable_KTojkZq6lmRWy-RBUoGcVQ_RJwjEP35";
    private static final OkHttpClient client = new OkHttpClient();

    // ── Insert any table ──────────────────────────────────────────
    public static void insertData(String table, JSONObject json) {
        RequestBody body = RequestBody.create(
                json.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + table)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                android.util.Log.e("Supabase", "Insert failed: " + e.getMessage());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                android.util.Log.d("Supabase", "Insert response: " + response.code());
                response.close();
            }
        });
    }

    // ── Save scan ─────────────────────────────────────────────────
    public static void saveScan(String userId, String plantName, String disease,
                                String imageUrl, int healthScore, boolean healthy) {
        try {
            JSONObject json = new JSONObject();
            json.put("user_id",      userId);
            json.put("plant_name",   plantName);
            json.put("disease",      disease);
            json.put("image_url",    imageUrl != null ? imageUrl : "");
            json.put("health_score", healthScore);
            json.put("healthy",      healthy);
            insertData("scans", json);
        } catch (Exception e) {
            android.util.Log.e("Supabase", "saveScan error: " + e.getMessage());
        }
    }

    // ── Fetch recent scans for a user ─────────────────────────────
    public interface ScansCallback {
        void onSuccess(JSONArray scans);
        void onFailure(String error);
    }

    public static void getRecentScans(String userId, ScansCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/scans"
                + "?user_id=eq." + userId
                + "&order=created_at.desc"
                + "&limit=5";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    android.util.Log.d("Supabase", "Scans: " + body);
                    callback.onSuccess(new JSONArray(body));
                } catch (Exception e) {
                    callback.onFailure(e.getMessage());
                }
            }
        });
    }
}