package com.example.agroaid;

public class ScanModel {

    private String plantName;
    private String disease;
    private String imageUrl;
    private int    healthScore;
    private boolean healthy;
    private String dateString; // plain string date from Supabase

    public ScanModel() {}

    public ScanModel(String plantName, String disease, String imageUrl,
                     int healthScore, boolean healthy, Object timestamp) {
        this.plantName   = plantName;
        this.disease     = disease;
        this.imageUrl    = imageUrl;
        this.healthScore = healthScore;
        this.healthy     = healthy;
        this.dateString  = ""; // Supabase returns created_at as string
    }

    // Constructor with date string from Supabase
    public ScanModel(String plantName, String disease, String imageUrl,
                     int healthScore, boolean healthy, String dateString) {
        this.plantName   = plantName;
        this.disease     = disease;
        this.imageUrl    = imageUrl;
        this.healthScore = healthScore;
        this.healthy     = healthy;
        this.dateString  = dateString != null ? dateString : "";
    }

    public String  getPlantName()   { return plantName   != null ? plantName   : "Unknown"; }
    public String  getDisease()     { return disease     != null ? disease     : "—"; }
    public String  getImageUrl()    { return imageUrl    != null ? imageUrl    : ""; }
    public int     getHealthScore() { return healthScore; }
    public boolean isHealthy()      { return healthy; }

    public String getFormattedDate() {
        if (dateString == null || dateString.isEmpty()) return "Recent";
        try {
            // Supabase returns: "2026-05-17T11:27:39.000Z"
            String[] parts = dateString.split("T")[0].split("-");
            String[] months = {"","Jan","Feb","Mar","Apr","May","Jun",
                    "Jul","Aug","Sep","Oct","Nov","Dec"};
            int month = Integer.parseInt(parts[1]);
            return parts[2] + " " + months[month] + " " + parts[0];
        } catch (Exception e) {
            return dateString.substring(0, Math.min(10, dateString.length()));
        }
    }
}