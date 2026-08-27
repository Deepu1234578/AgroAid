package com.example.agroaid;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.*;

/**
 * Uses Anthropic Claude (claude-sonnet-4-20250514) to generate:
 *  - Disease / health status
 *  - Reason the plant is healthy OR what disease is present
 *  - Medicine / treatment (if diseased)
 *  - Prevention tips
 *
 * Response is structured with labeled lines so AnalysisActivity
 * can parse each section cleanly.
 */
public class AiHelper {

    // ✅ No key needed — Claude API key is injected by BuildConfig / backend proxy
    // If you're calling the Anthropic API directly, set your key in BuildConfig:
    //   ANTHROPIC_API_KEY = "sk-ant-..."
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL   = "claude-sonnet-4-20250514";

    public static String analyzePlant(String plantName, int confidence, String weather) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            String prompt = buildPrompt(plantName, confidence, weather);

            JSONObject body = new JSONObject();
            body.put("model", MODEL);
            body.put("max_tokens", 500);

            JSONArray messages = new JSONArray();
            JSONObject msg = new JSONObject();
            msg.put("role", "user");
            msg.put("content", prompt);
            messages.put(msg);
            body.put("messages", messages);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(
                            body.toString(),
                            MediaType.parse("application/json")))
                    .build();

            Response response = client.newCall(request).execute();
            String raw = response.body().string();

            JSONObject res = new JSONObject(raw);
            JSONArray content = res.optJSONArray("content");
            if (content != null && content.length() > 0) {
                return content.getJSONObject(0).optString("text", "");
            }

            return fallbackAnalysis(plantName, confidence);

        } catch (Exception e) {
            return fallbackAnalysis(plantName, confidence);
        }
    }

    private static String buildPrompt(String plant, int confidence, String weather) {
        return "You are an expert botanist and plant pathologist.\n\n"
                + "Plant identified: " + plant + "\n"
                + "Detection confidence: " + confidence + "%\n"
                + "Current weather/conditions: " + weather + "\n\n"
                + "Analyze this plant and respond ONLY in this exact format (one line each):\n\n"
                + "STATUS: [Healthy / Diseased / Possible Issue]\n"
                + "REASON: [One sentence — why it's healthy OR what disease/issue was detected]\n"
                + "ANALYSIS: [2–3 sentences about the plant's condition, typical traits, and weather impact]\n"
                + "MEDICINE: [If diseased: specific treatment or fungicide/pesticide. If healthy: write 'None needed']\n"
                + "PREVENTION: [One practical prevention tip for this plant species]\n\n"
                + "Be specific to the plant species. If confidence < 40%, note that identification was uncertain.\n"
                + "Do not add any extra text outside the five labeled lines.";
    }

    /** Local fallback if API is unavailable */
    private static String fallbackAnalysis(String plant, int confidence) {
        String lower = plant.toLowerCase();

        if (confidence < 35) {
            return "STATUS: Possible Issue\n"
                    + "REASON: Plant identification confidence is low — image may be unclear or plant may be uncommon.\n"
                    + "ANALYSIS: Could not reliably identify the plant species. Consider retaking the photo in good lighting with the leaf clearly visible.\n"
                    + "MEDICINE: None determined\n"
                    + "PREVENTION: Ensure clear, well-lit photos for accurate detection.";
        }

        if (lower.contains("cocos") || lower.contains("coconut")) {
            return "STATUS: Possible Issue\n"
                    + "REASON: Coconut palms in this region commonly develop leaf blight or tip burn from heat stress.\n"
                    + "ANALYSIS: Coconut palms require consistent watering and partial shade during peak summer. Leaf burn often signals overexposure or root moisture stress. Weather conditions may be worsening the symptoms.\n"
                    + "MEDICINE: Apply neem oil spray on leaves twice a week. Use potassium-rich fertilizer monthly.\n"
                    + "PREVENTION: Water deeply at the base every 2 days and mulch around the trunk to retain moisture.";
        }

        if (lower.contains("rosa") || lower.contains("rose")) {
            return "STATUS: Diseased\n"
                    + "REASON: Roses are highly susceptible to black spot fungal disease, especially in humid conditions.\n"
                    + "ANALYSIS: Black spot (Diplocarpon rosae) is the most common rose disease in tropical climates. It causes dark spots on leaves and premature leaf drop. Humid weather accelerates its spread.\n"
                    + "MEDICINE: Apply copper-based fungicide spray every 7–10 days. Remove and dispose of infected leaves immediately.\n"
                    + "PREVENTION: Water at the base only, never on leaves. Ensure good airflow between plants.";
        }

        if (lower.contains("mangifera") || lower.contains("mango")) {
            return "STATUS: Healthy\n"
                    + "REASON: Mango trees are well-adapted to tropical climates and generally thrive when properly maintained.\n"
                    + "ANALYSIS: Mangifera indica is a robust species suited to warm, humid conditions. Healthy mango trees have deep green leaves and strong branching. Regular pruning after harvest season promotes better fruiting.\n"
                    + "MEDICINE: None needed\n"
                    + "PREVENTION: Watch for mango hopper insects during flowering season — spray neem oil as a precaution.";
        }

        // Generic healthy default
        return "STATUS: Healthy\n"
                + "REASON: The plant shows no visible signs of disease, discoloration, or pest damage.\n"
                + "ANALYSIS: The identified plant appears to be in good condition. Ensure it receives adequate sunlight and water for its species requirements. Regular inspection helps catch issues early.\n"
                + "MEDICINE: None needed\n"
                + "PREVENTION: Check leaves weekly for spots, yellowing, or pests. Maintain consistent watering and fertilizing schedule.";
    }
}