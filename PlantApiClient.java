package com.example.agroaid;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import okhttp3.*;

public class PlantApiClient {

    private final String apiKey;
    private final Context context;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(40, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(40, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(40, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    public PlantApiClient(Context context, String apiKey) {
        this.context = context;
        this.apiKey = apiKey;
    }

    public JSONObject analyze(Bitmap bitmap) throws Exception {

        if (bitmap == null)
            throw new Exception("Invalid image");

        SharedPreferences prefs =
                context.getSharedPreferences(
                        "AgroAid",
                        Context.MODE_PRIVATE
                );

        String newHash = getImageHash(bitmap);

        String oldHash =
                prefs.getString("last_hash", "");

        if (newHash.equals(oldHash)) {
            throw new Exception("Duplicate Image");
        }

        prefs.edit()
                .putString("last_hash", newHash)
                .apply();

        String base64 = bitmapToBase64(bitmap);

        JSONObject plant =
                identifyPlant(base64);

        JSONObject disease;

        if (!plant.optBoolean("valid", true)) {

            disease = new JSONObject();

            disease.put("detected", false);

        } else {

            disease = assessHealth(base64);
        }

        JSONObject result = new JSONObject();

        result.put("plant", plant);

        result.put("disease", disease);

        return result;
    }

    // 🌱 PLANT DETECTION
    private JSONObject identifyPlant(String base64)
            throws Exception {

        OkHttpClient client = new OkHttpClient();

        byte[] imageBytes =
                Base64.decode(base64, Base64.DEFAULT);

        RequestBody requestBody =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)

                        .addFormDataPart(
                                "images",
                                "plant.jpg",
                                RequestBody.create(
                                        imageBytes,
                                        MediaType.parse("image/jpeg")
                                )
                        )

                        .addFormDataPart(
                                "organs",
                                "leaf"
                        )

                        .build();

        Request request =
                new Request.Builder()

                        .url(
                                "https://my-api.plantnet.org/v2/identify/all?api-key=2b10bwXTSxMYh9iTvTOotZTCu"
                        )

                        .post(requestBody)

                        .build();

        Response response =
                client.newCall(request).execute();

        String responseData =
                response.body().string();

        android.util.Log.d(
                "PLANT_API",
                responseData
        );

        JSONObject obj =
                new JSONObject(responseData);

        JSONObject plant =
                new JSONObject();

        JSONArray results =
                obj.optJSONArray("results");

        if (results != null
                && results.length() > 0) {

            JSONObject first =
                    results.getJSONObject(0);

            JSONObject species =
                    first.optJSONObject(
                            "species"
                    );

            String name =
                    species.optString(
                            "scientificNameWithoutAuthor",
                            "Unknown Plant"
                    );
            JSONArray images =
                    first.optJSONArray("images");

            if (images != null && images.length() > 0) {

                JSONObject img =
                        images.getJSONObject(0);

                JSONObject urlObj =
                        img.optJSONObject("url");

                if (urlObj != null) {

                    String imageUrl =
                            urlObj.optString("m");

                    plant.put("image", imageUrl);
                }
            }

            double score =
                    first.optDouble(
                            "score",
                            0
                    );

            int confidence =
                    (int) (score * 100);

            plant.put("name", name);

            plant.put(
                    "confidence",
                    confidence
            );

            plant.put("valid", true);

        } else {

            plant.put(
                    "name",
                    "Unknown Plant"
            );

            plant.put(
                    "confidence",
                    0
            );

            plant.put(
                    "valid",
                    false
            );
        }

        return plant;
    }

    // 🦠 DISEASE DETECTION
    private JSONObject assessHealth(String base64)
            throws Exception {

        JSONObject body =
                new JSONObject();

        body.put(
                "images",
                new JSONArray().put(base64)
        );

        body.put(
                "organs",
                new JSONArray().put("leaf")
        );

        Request request =
                new Request.Builder()
                        .url(
                                "https://plant.id/api/v2/health_assessment"
                        )
                        .addHeader(
                                "Api-Key",
                                apiKey
                        )
                        .addHeader(
                                "Content-Type",
                                "application/json"
                        )
                        .post(
                                RequestBody.create(
                                        body.toString(),
                                        MediaType.parse(
                                                "application/json"
                                        )
                                )
                        )
                        .build();

        Response response =
                client.newCall(request).execute();

        String responseData =
                response.body().string();

        System.out.println(responseData);

        if (!responseData.startsWith("{")) {

            JSONObject error =
                    new JSONObject();

            error.put("error", responseData);

            return error;
        }

        JSONObject obj =
                new JSONObject(responseData);

        JSONObject disease =
                new JSONObject();

        JSONObject health =
                obj.optJSONObject(
                        "health_assessment"
                );

        if (health != null) {

            JSONArray diseases =
                    health.optJSONArray(
                            "diseases"
                    );

            if (diseases != null
                    && diseases.length() > 0) {

                JSONObject top =
                        diseases.getJSONObject(0);

                disease.put(
                        "detected",
                        true
                );

                disease.put(
                        "name",
                        top.optString("name")
                );

            } else {

                disease.put(
                        "detected",
                        false
                );
            }

        } else {

            disease.put(
                    "detected",
                    false
            );
        }

        return disease;
    }

    // 🔧 UTIL
    private String bitmapToBase64(Bitmap bitmap) {

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                baos
        );

        return Base64.encodeToString(
                baos.toByteArray(),
                Base64.NO_WRAP
        );
    }

    private String getImageHash(Bitmap bitmap) {

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                50,
                baos
        );

        return Base64.encodeToString(
                baos.toByteArray(),
                Base64.NO_WRAP
        );
    }
}