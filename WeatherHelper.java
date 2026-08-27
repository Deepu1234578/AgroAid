package com.example.agroaid;

import org.json.JSONObject;
import okhttp3.*;

public class WeatherHelper {
    public static String getWeather(double lat, double lon) {

        try {
            OkHttpClient client = new OkHttpClient();

            String url = "https://api.open-meteo.com/v1/forecast?latitude=" +
                    lat + "&longitude=" + lon + "&current_weather=true";

              Request request = new Request.Builder().url(url).build();

            Response response = client.newCall(request).execute();

            JSONObject obj = new JSONObject(response.body().string());
            JSONObject current = obj.getJSONObject("current_weather");

            double temp = current.getDouble("temperature");

            return "🌡 " + temp + "°C";

        } catch (Exception e) {
            return "⚠ Weather error";
        }
    }
}

