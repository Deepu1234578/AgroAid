package com.example.agroaid;

/**
 * Represents a single commodity in the market.
 */
public class CommodityItem {

    private String name;
    private String category;      // e.g. "Cereals", "Oil Seeds", "Vegetables"
    private int minPrice;         // ₹ per quintal (or per kg for vegetables/fruits)
    private int maxPrice;
    private int demandChangePercent; // +ve = rising, -ve = falling
    private String demandLevel;   // "Low", "Medium", "High", "Very High"

    public CommodityItem(String name, String category, int minPrice, int maxPrice,
                         int demandChangePercent, String demandLevel) {
        this.name = name;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.demandChangePercent = demandChangePercent;
        this.demandLevel = demandLevel;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getMinPrice() { return minPrice; }
    public int getMaxPrice() { return maxPrice; }
    public int getDemandChangePercent() { return demandChangePercent; }
    public String getDemandLevel() { return demandLevel; }

    public String getPriceRange() {
        if (minPrice == 0) return "N/A";
        if (minPrice < 100) {
            return "₹" + minPrice + "–" + maxPrice + "/kg";
        }
        return "₹" + minPrice + "–" + maxPrice + "/qtl";
    }

    public String getTrendArrow() {
        if (demandChangePercent > 0) return "▲ +" + demandChangePercent + "%";
        if (demandChangePercent < 0) return "▼ " + demandChangePercent + "%";
        return "— 0%";
    }

    public int getTrendColor() {
        if (demandChangePercent > 0) return android.graphics.Color.parseColor("#2E7D32");
        if (demandChangePercent < 0) return android.graphics.Color.parseColor("#C62828");
        return android.graphics.Color.parseColor("#757575");
    }

    public int getDemandBadgeColor() {
        switch (demandLevel) {
            case "Very High": return android.graphics.Color.parseColor("#E65100");
            case "High":      return android.graphics.Color.parseColor("#2E7D32");
            case "Medium":    return android.graphics.Color.parseColor("#1565C0");
            default:          return android.graphics.Color.parseColor("#757575");
        }
    }
}