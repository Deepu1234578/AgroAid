package com.example.agroaid;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Fetches a real reference image for a plant from the Wikipedia REST API.
 *
 * Strategy (in order of quality):
 *  1. Wikipedia page-summary thumbnail  — usually the main species photo
 *  2. Wikipedia /page/media-list        — first full image from the article
 *  3. Wikimedia Commons search          — broader fallback
 *
 * All network calls are synchronous — call from a background thread only.
 */
public class WikiImageHelper {

    private static final String TAG = "WikiImageHelper";

    /**
     * Returns a non-empty HTTPS image URL for the given plant name,
     * or an empty string if nothing is found.
     *
     * @param plantName scientific or common name, e.g. "Rosa abietina"
     */
    public static String fetchPlantImage(String plantName) {
        if (plantName == null || plantName.trim().isEmpty()) return "";

        String encoded = urlEncode(plantName.replace(" ", "_"));

        // ── Strategy 1: Wikipedia page summary (fastest, best quality) ──
        String url1 = fetchSummaryThumb(encoded);
        if (!url1.isEmpty()) {
            Log.d(TAG, "Strategy 1 succeeded: " + url1);
            return url1;
        }

        // ── Strategy 2: Wikipedia media list ──
        String url2 = fetchMediaList(encoded);
        if (!url2.isEmpty()) {
            Log.d(TAG, "Strategy 2 succeeded: " + url2);
            return url2;
        }

        // ── Strategy 3: Wikimedia Commons search ──
        String url3 = fetchCommonsSearch(plantName);
        if (!url3.isEmpty()) {
            Log.d(TAG, "Strategy 3 succeeded: " + url3);
            return url3;
        }

        Log.w(TAG, "No image found for: " + plantName);
        return "";
    }

    // ── Strategy 1 ────────────────────────────────────────────────────────────
    // GET https://en.wikipedia.org/api/rest_v1/page/summary/{title}
    // Returns the lead image thumbnail used at the top of the article.
    private static String fetchSummaryThumb(String encodedTitle) {
        try {
            String url = "https://en.wikipedia.org/api/rest_v1/page/summary/"
                    + encodedTitle;
            String json = httpGet(url);
            if (json.isEmpty()) return "";

            JSONObject obj = new JSONObject(json);
            if (obj.has("thumbnail")) {
                JSONObject thumb = obj.getJSONObject("thumbnail");
                String src = thumb.optString("source", "");
                if (!src.isEmpty()) return ensureHttps(src);
            }
            // Also try originalimage
            if (obj.has("originalimage")) {
                String src = obj.getJSONObject("originalimage").optString("source", "");
                if (!src.isEmpty()) return ensureHttps(src);
            }
        } catch (Exception e) {
            Log.w(TAG, "Strategy 1 error: " + e.getMessage());
        }
        return "";
    }

    // ── Strategy 2 ────────────────────────────────────────────────────────────
    // GET https://en.wikipedia.org/api/rest_v1/page/media-list/{title}
    // Returns the full list of images in the article; pick the first photo.
    private static String fetchMediaList(String encodedTitle) {
        try {
            String url = "https://en.wikipedia.org/api/rest_v1/page/media-list/"
                    + encodedTitle;
            String json = httpGet(url);
            if (json.isEmpty()) return "";

            JSONObject obj  = new JSONObject(json);
            JSONArray items = obj.optJSONArray("items");
            if (items == null) return "";

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String type = item.optString("type", "");
                if (!"image".equals(type)) continue;

                // srcset or src
                JSONObject srcset = item.optJSONObject("srcset");
                if (srcset != null) {
                    String src = srcset.optString("src", "");
                    if (!src.isEmpty() && isPhotoUrl(src))
                        return ensureHttps(src);
                }

                String src = item.optString("src", "");
                if (!src.isEmpty() && isPhotoUrl(src))
                    return ensureHttps(src);

                // titles → canonical
                JSONObject titles = item.optJSONObject("titles");
                if (titles != null) {
                    String canonical = titles.optString("canonical", "");
                    if (!canonical.isEmpty()) {
                        String fileUrl = resolveCommonsFile(canonical);
                        if (!fileUrl.isEmpty()) return fileUrl;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Strategy 2 error: " + e.getMessage());
        }
        return "";
    }

    // ── Strategy 3 ────────────────────────────────────────────────────────────
    // Wikimedia Commons search API — finds images by plant name keyword.
    private static String fetchCommonsSearch(String plantName) {
        try {
            String query = urlEncode(plantName + " plant");
            String url = "https://commons.wikimedia.org/w/api.php"
                    + "?action=query&format=json&prop=imageinfo"
                    + "&generator=search&gsrsearch=File:" + query
                    + "&gsrnamespace=6&gsrlimit=5"
                    + "&iiprop=url&iiurlwidth=800";

            String json = httpGet(url);
            if (json.isEmpty()) return "";

            JSONObject obj   = new JSONObject(json);
            JSONObject query2 = obj.optJSONObject("query");
            if (query2 == null) return "";
            JSONObject pages = query2.optJSONObject("pages");
            if (pages == null) return "";

            for (String key : jsonKeys(pages)) {
                JSONObject page  = pages.getJSONObject(key);
                JSONArray  info  = page.optJSONArray("imageinfo");
                if (info == null || info.length() == 0) continue;
                String src = info.getJSONObject(0).optString("url", "");
                if (!src.isEmpty() && isPhotoUrl(src))
                    return ensureHttps(src);
            }
        } catch (Exception e) {
            Log.w(TAG, "Strategy 3 error: " + e.getMessage());
        }
        return "";
    }

    // ── Helper: resolve a Commons file title to a direct URL ──────────────────
    // GET https://commons.wikimedia.org/w/api.php?action=query&titles=File:Xyz&prop=imageinfo
    private static String resolveCommonsFile(String fileTitle) {
        try {
            String encoded = urlEncode(fileTitle);
            String url = "https://commons.wikimedia.org/w/api.php"
                    + "?action=query&format=json&prop=imageinfo"
                    + "&titles=" + encoded
                    + "&iiprop=url&iiurlwidth=800";
            String json = httpGet(url);
            if (json.isEmpty()) return "";

            JSONObject obj   = new JSONObject(json);
            JSONObject query = obj.optJSONObject("query");
            if (query == null) return "";
            JSONObject pages = query.optJSONObject("pages");
            if (pages == null) return "";

            for (String key : jsonKeys(pages)) {
                if (key.equals("-1")) continue;
                JSONObject page = pages.getJSONObject(key);
                JSONArray  info = page.optJSONArray("imageinfo");
                if (info == null || info.length() == 0) continue;
                String src = info.getJSONObject(0).optString("url", "");
                if (!src.isEmpty()) return ensureHttps(src);
            }
        } catch (Exception e) {
            Log.w(TAG, "resolveCommonsFile error: " + e.getMessage());
        }
        return "";
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static String httpGet(String urlString) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent",
                    "AgroAid/1.0 (Android; plant-disease-app)");

            int code = conn.getResponseCode();
            if (code != 200) return "";

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            return sb.toString();

        } catch (Exception e) {
            Log.w(TAG, "httpGet error for " + urlString + ": " + e.getMessage());
            return "";
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** Only accept JPEG/PNG/WEBP images — exclude SVG, OGG, etc. */
    private static boolean isPhotoUrl(String url) {
        String lower = url.toLowerCase();
        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".webp")
                || lower.contains(".jpg/")
                || lower.contains(".jpeg/")
                || lower.contains(".png/");
    }

    private static String ensureHttps(String url) {
        if (url.startsWith("//")) return "https:" + url;
        if (url.startsWith("http://")) return url.replace("http://", "https://");
        return url;
    }

    private static String urlEncode(String s) {
        try { return URLEncoder.encode(s, "UTF-8"); }
        catch (Exception e) { return s; }
    }

    /** Iterates JSONObject keys as an iterable (avoids Iterator boilerplate). */
    private static Iterable<String> jsonKeys(JSONObject obj) {
        java.util.List<String> keys = new java.util.ArrayList<>();
        java.util.Iterator<String> it = obj.keys();
        while (it.hasNext()) keys.add(it.next());
        return keys;
    }
}