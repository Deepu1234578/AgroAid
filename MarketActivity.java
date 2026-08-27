package com.example.agroaid;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.*;

public class MarketActivity extends AppCompatActivity {

    Spinner  spinnerStateMarket;
    RecyclerView rvCommodities;
    TextView tvMarketTitle, tvCropsListed, tvAvgTrend, tvHotPick;
    LineChart marketLineChart;

    // ── Commodity data (all states) ── same as before ──────────────────
    static final Map<String, List<CommodityItem>> STATE_MARKET_DATA = new HashMap<>();

    static {
        STATE_MARKET_DATA.put("Tamil Nadu", Arrays.asList(
                new CommodityItem("Rice (Ponni)",  "Cereals",    2400,  2600,  +8,  "High"),
                new CommodityItem("Banana",         "Fruit",        35,    50, +12, "Very High"),
                new CommodityItem("Coconut",        "Fruit",        18,    22,  +5, "Medium"),
                new CommodityItem("Groundnut",      "Oil Seeds",  5200,  5800,  +3, "Medium"),
                new CommodityItem("Tomato",         "Vegetables",   40,    60, +18, "High"),
                new CommodityItem("Sugarcane",      "Cash Crops",  290,   310,  +2, "Low"),
                new CommodityItem("Ragi",           "Millets",    3500,  3800,  +9, "High"),
                new CommodityItem("Turmeric",       "Spices",     8000,  9500, +15, "Very High")
        ));
        STATE_MARKET_DATA.put("Maharashtra", Arrays.asList(
                new CommodityItem("Soybean",      "Oil Seeds",  4200, 4600, +10, "High"),
                new CommodityItem("Cotton",       "Cash Crops", 6500, 7200,  +7, "High"),
                new CommodityItem("Onion",        "Vegetables",   25,   45, +22, "Very High"),
                new CommodityItem("Wheat",        "Cereals",    2015, 2200,  +4, "Medium"),
                new CommodityItem("Tomato",       "Vegetables",   38,   55, +16, "High"),
                new CommodityItem("Sugarcane",    "Cash Crops",  310,  330,  +3, "Medium"),
                new CommodityItem("Maize",        "Cereals",    1800, 2000,  +6, "Medium"),
                new CommodityItem("Pomegranate",  "Fruit",        90,  120, +20, "Very High")
        ));
        STATE_MARKET_DATA.put("Punjab", Arrays.asList(
                new CommodityItem("Wheat",           "Cereals",    2015, 2200, +15, "Very High"),
                new CommodityItem("Rice (Basmati)",  "Cereals",    3800, 4500, +12, "Very High"),
                new CommodityItem("Maize",           "Cereals",    1900, 2100,  +5, "Medium"),
                new CommodityItem("Mustard",         "Oil Seeds",  5200, 5600,  +8, "High"),
                new CommodityItem("Cotton",          "Cash Crops", 6200, 6800,  +6, "High"),
                new CommodityItem("Potato",          "Vegetables", 1200, 1500,  +9, "High"),
                new CommodityItem("Sunflower",       "Oil Seeds",  5500, 6000,  +4, "Medium"),
                new CommodityItem("Barley",          "Cereals",    1700, 1850,  +3, "Low")
        ));
        STATE_MARKET_DATA.put("Andhra Pradesh", Arrays.asList(
                new CommodityItem("Rice",          "Cereals",    2200,  2500,  +8, "High"),
                new CommodityItem("Chilli (Red)",  "Spices",    12000, 15000, +25, "Very High"),
                new CommodityItem("Tobacco",       "Cash Crops",  220,   260,  +5, "Medium"),
                new CommodityItem("Groundnut",     "Oil Seeds",  5000,  5600,  +7, "High"),
                new CommodityItem("Cotton",        "Cash Crops", 6400,  7000,  +8, "High"),
                new CommodityItem("Maize",         "Cereals",    1850,  2050,  +6, "Medium"),
                new CommodityItem("Tomato",        "Vegetables",   35,    55, +18, "High"),
                new CommodityItem("Mango",         "Fruit",        80,   120, +15, "High")
        ));
        STATE_MARKET_DATA.put("Karnataka", Arrays.asList(
                new CommodityItem("Coffee",    "Cash Crops", 22000, 26000, +18, "Very High"),
                new CommodityItem("Ragi",      "Millets",     3400,  3700, +10, "High"),
                new CommodityItem("Silk",      "Cash Crops", 45000, 52000, +12, "High"),
                new CommodityItem("Tomato",    "Vegetables",    42,    62, +20, "Very High"),
                new CommodityItem("Onion",     "Vegetables",    28,    48, +16, "High"),
                new CommodityItem("Coconut",   "Fruit",         20,    25,  +6, "Medium"),
                new CommodityItem("Sunflower", "Oil Seeds",   5400,  5900,  +5, "Medium"),
                new CommodityItem("Arecanut",  "Cash Crops", 35000, 40000, +22, "Very High")
        ));
        STATE_MARKET_DATA.put("Uttar Pradesh", Arrays.asList(
                new CommodityItem("Wheat",          "Cereals",    2015, 2180, +14, "Very High"),
                new CommodityItem("Sugarcane",      "Cash Crops",  340,  370,  +8, "High"),
                new CommodityItem("Potato",         "Vegetables", 1100, 1400, +11, "High"),
                new CommodityItem("Rice",           "Cereals",    2100, 2350,  +7, "High"),
                new CommodityItem("Mustard",        "Oil Seeds",  5100, 5500,  +9, "High"),
                new CommodityItem("Maize",          "Cereals",    1800, 2000,  +5, "Medium"),
                new CommodityItem("Mentha (Mint)",  "Spices",     1200, 1500, +20, "Very High"),
                new CommodityItem("Onion",          "Vegetables",   22,   40, +14, "High")
        ));
        STATE_MARKET_DATA.put("West Bengal", Arrays.asList(
                new CommodityItem("Rice",              "Cereals",    2150, 2400,  +9, "High"),
                new CommodityItem("Jute",              "Cash Crops", 5500, 6200, +15, "Very High"),
                new CommodityItem("Tea",               "Cash Crops",18000,22000, +12, "High"),
                new CommodityItem("Potato",            "Vegetables", 1050, 1350, +10, "High"),
                new CommodityItem("Mustard",           "Oil Seeds",  5000, 5400,  +7, "Medium"),
                new CommodityItem("Maize",             "Cereals",    1750, 1950,  +4, "Medium"),
                new CommodityItem("Vegetables (Mixed)","Vegetables",   30,   55, +18, "High"),
                new CommodityItem("Betel Leaf",        "Cash Crops", 6000, 7500, +16, "High")
        ));
        STATE_MARKET_DATA.put("Rajasthan", Arrays.asList(
                new CommodityItem("Wheat",          "Cereals",    2000, 2180, +11, "High"),
                new CommodityItem("Mustard",        "Oil Seeds",  5300, 5700, +12, "Very High"),
                new CommodityItem("Bajra",          "Millets",    2350, 2600,  +8, "High"),
                new CommodityItem("Cumin (Jeera)",  "Spices",    25000,30000, +30, "Very High"),
                new CommodityItem("Jowar",          "Millets",    2800, 3100,  +6, "Medium"),
                new CommodityItem("Groundnut",      "Oil Seeds",  5100, 5600,  +7, "Medium"),
                new CommodityItem("Barley",         "Cereals",    1650, 1800,  +3, "Low"),
                new CommodityItem("Coriander",      "Spices",     9000,11000, +20, "High")
        ));
        STATE_MARKET_DATA.put("Gujarat", Arrays.asList(
                new CommodityItem("Cotton",    "Cash Crops", 6800, 7500, +10, "Very High"),
                new CommodityItem("Groundnut", "Oil Seeds",  5500, 6000,  +8, "High"),
                new CommodityItem("Wheat",     "Cereals",    2020, 2200,  +6, "Medium"),
                new CommodityItem("Castor",    "Oil Seeds",  5200, 5800, +12, "High"),
                new CommodityItem("Potato",    "Vegetables", 1150, 1450,  +9, "High"),
                new CommodityItem("Cumin",     "Spices",    24000,29000, +28, "Very High"),
                new CommodityItem("Bajra",     "Millets",    2300, 2550,  +7, "Medium"),
                new CommodityItem("Sesame",    "Oil Seeds", 10000,12000, +15, "High")
        ));
        STATE_MARKET_DATA.put("Madhya Pradesh", Arrays.asList(
                new CommodityItem("Soybean",           "Oil Seeds", 4100, 4700, +11, "Very High"),
                new CommodityItem("Wheat",             "Cereals",   2010, 2190, +12, "High"),
                new CommodityItem("Chickpea (Chana)",  "Pulses",    5200, 5700, +14, "High"),
                new CommodityItem("Maize",             "Cereals",   1820, 2020,  +6, "Medium"),
                new CommodityItem("Cotton",            "Cash Crops",6300, 6900,  +7, "High"),
                new CommodityItem("Lentils (Masoor)",  "Pulses",    5800, 6400, +16, "High"),
                new CommodityItem("Garlic",            "Vegetables",8000,11000, +22, "Very High"),
                new CommodityItem("Tomato",            "Vegetables",  35,   52, +15, "High")
        ));
        STATE_MARKET_DATA.put("Bihar", Arrays.asList(
                new CommodityItem("Rice",      "Cereals",    2100, 2380,  +9, "High"),
                new CommodityItem("Wheat",     "Cereals",    2010, 2175, +10, "High"),
                new CommodityItem("Maize",     "Cereals",    1780, 1980,  +7, "Medium"),
                new CommodityItem("Lichi",     "Fruit",       120,  180, +25, "Very High"),
                new CommodityItem("Mustard",   "Oil Seeds",  5000, 5400,  +8, "High"),
                new CommodityItem("Potato",    "Vegetables", 1000, 1300, +11, "High"),
                new CommodityItem("Onion",     "Vegetables",   20,   38, +14, "High"),
                new CommodityItem("Sugarcane", "Cash Crops",  315,  340,  +4, "Medium")
        ));
        STATE_MARKET_DATA.put("Haryana", Arrays.asList(
                new CommodityItem("Wheat",           "Cereals",    2015, 2200, +13, "Very High"),
                new CommodityItem("Rice (Basmati)",  "Cereals",    3600, 4200, +10, "High"),
                new CommodityItem("Mustard",         "Oil Seeds",  5100, 5500,  +9, "High"),
                new CommodityItem("Cotton",          "Cash Crops", 6000, 6700,  +7, "High"),
                new CommodityItem("Sugarcane",       "Cash Crops",  350,  380,  +5, "Medium"),
                new CommodityItem("Potato",          "Vegetables", 1100, 1400,  +8, "High"),
                new CommodityItem("Sunflower",       "Oil Seeds",  5200, 5700,  +6, "Medium"),
                new CommodityItem("Maize",           "Cereals",    1800, 1980,  +4, "Medium")
        ));
        STATE_MARKET_DATA.put("Odisha", Arrays.asList(
                new CommodityItem("Rice",      "Cereals",    2050, 2300,  +9, "High"),
                new CommodityItem("Groundnut", "Oil Seeds",  4800, 5300,  +7, "Medium"),
                new CommodityItem("Sugarcane", "Cash Crops",  280,  310,  +3, "Low"),
                new CommodityItem("Maize",     "Cereals",    1750, 1950,  +5, "Medium"),
                new CommodityItem("Turmeric",  "Spices",     7500, 9000, +14, "High"),
                new CommodityItem("Tomato",    "Vegetables",   38,   55, +16, "High"),
                new CommodityItem("Jute",      "Cash Crops", 5000, 5800, +10, "High"),
                new CommodityItem("Mustard",   "Oil Seeds",  4900, 5300,  +6, "Medium")
        ));
        STATE_MARKET_DATA.put("Telangana", Arrays.asList(
                new CommodityItem("Rice",      "Cereals",    2200, 2480,  +9, "High"),
                new CommodityItem("Cotton",    "Cash Crops", 6300, 7000,  +8, "High"),
                new CommodityItem("Maize",     "Cereals",    1850, 2050,  +6, "Medium"),
                new CommodityItem("Groundnut", "Oil Seeds",  5100, 5600,  +7, "High"),
                new CommodityItem("Turmeric",  "Spices",     7800, 9200, +14, "High"),
                new CommodityItem("Chilli",    "Spices",    11000,14000, +20, "Very High"),
                new CommodityItem("Soybean",   "Oil Seeds",  4000, 4500,  +9, "High"),
                new CommodityItem("Tomato",    "Vegetables",   36,   56, +17, "High")
        ));
        STATE_MARKET_DATA.put("Kerala", Arrays.asList(
                new CommodityItem("Rubber",         "Cash Crops",18000,20000, +12, "High"),
                new CommodityItem("Coconut",        "Fruit",        22,   28,  +7, "High"),
                new CommodityItem("Tea",            "Cash Crops",19000,23000, +10, "High"),
                new CommodityItem("Coffee",         "Cash Crops",21000,25000, +15, "Very High"),
                new CommodityItem("Pepper (Black)", "Spices",    45000,52000, +18, "Very High"),
                new CommodityItem("Cardamom",       "Spices",   120000,150000,+22, "Very High"),
                new CommodityItem("Banana",         "Fruit",        32,   48, +10, "High"),
                new CommodityItem("Cashew",         "Fruit",     12000,15000, +14, "High")
        ));
        STATE_MARKET_DATA.put("Chhattisgarh", Arrays.asList(
                new CommodityItem("Rice",           "Cereals",    2000, 2250,  +8, "High"),
                new CommodityItem("Maize",          "Cereals",    1750, 1950,  +5, "Medium"),
                new CommodityItem("Soybean",        "Oil Seeds",  4000, 4500,  +9, "High"),
                new CommodityItem("Groundnut",      "Oil Seeds",  4800, 5300,  +6, "Medium"),
                new CommodityItem("Lakhori Chilli", "Spices",     8000,10000, +12, "High"),
                new CommodityItem("Tomato",         "Vegetables",   35,   52, +14, "High"),
                new CommodityItem("Sugarcane",      "Cash Crops",  280,  305,  +3, "Low"),
                new CommodityItem("Mustard",        "Oil Seeds",  4900, 5300,  +7, "Medium")
        ));
        STATE_MARKET_DATA.put("Assam", Arrays.asList(
                new CommodityItem("Tea",     "Cash Crops",20000,25000, +14, "Very High"),
                new CommodityItem("Rice",    "Cereals",    2000, 2250,  +8, "High"),
                new CommodityItem("Jute",    "Cash Crops", 5200, 5900, +12, "High"),
                new CommodityItem("Mustard", "Oil Seeds",  4800, 5200,  +7, "Medium"),
                new CommodityItem("Banana",  "Fruit",        28,   42,  +9, "High"),
                new CommodityItem("Ginger",  "Spices",     7000, 9500, +16, "High"),
                new CommodityItem("Lemon",   "Fruit",        60,   90, +11, "High"),
                new CommodityItem("Potato",  "Vegetables", 1000, 1300,  +8, "Medium")
        ));
        STATE_MARKET_DATA.put("Jharkhand", Arrays.asList(
                new CommodityItem("Rice",        "Cereals",    2000, 2240,  +7, "High"),
                new CommodityItem("Maize",       "Cereals",    1720, 1920,  +5, "Medium"),
                new CommodityItem("Tomato",      "Vegetables",   34,   52, +14, "High"),
                new CommodityItem("Potato",      "Vegetables",  980, 1280,  +9, "High"),
                new CommodityItem("Mustard",     "Oil Seeds",  4800, 5200,  +7, "Medium"),
                new CommodityItem("Lac",         "Cash Crops",35000,42000, +15, "High"),
                new CommodityItem("Onion",       "Vegetables",   22,   38, +12, "High"),
                new CommodityItem("Cauliflower", "Vegetables",   25,   45, +10, "Medium")
        ));
        STATE_MARKET_DATA.put("Himachal Pradesh", Arrays.asList(
                new CommodityItem("Apple",  "Fruit",        60,  100, +18, "Very High"),
                new CommodityItem("Potato", "Vegetables", 1200, 1600, +12, "High"),
                new CommodityItem("Pea",    "Vegetables", 3000, 4000, +14, "High"),
                new CommodityItem("Ginger", "Spices",     8000,11000, +16, "High"),
                new CommodityItem("Maize",  "Cereals",    1800, 2000,  +5, "Medium"),
                new CommodityItem("Wheat",  "Cereals",    2000, 2180,  +9, "High"),
                new CommodityItem("Cherry", "Fruit",       150,  220, +20, "Very High"),
                new CommodityItem("Plum",   "Fruit",        60,   90, +12, "High")
        ));
        STATE_MARKET_DATA.put("Uttarakhand", Arrays.asList(
                new CommodityItem("Basmati Rice",    "Cereals",  3500, 4200, +11, "Very High"),
                new CommodityItem("Wheat",           "Cereals",  2000, 2180,  +9, "High"),
                new CommodityItem("Apple",           "Fruit",      55,   95, +15, "High"),
                new CommodityItem("Lychee",          "Fruit",     100,  160, +18, "High"),
                new CommodityItem("Mandua (Ragi)",   "Millets",  3200, 3600, +10, "High"),
                new CommodityItem("Potato",          "Vegetables",1100, 1450, +10, "High"),
                new CommodityItem("Ginger",          "Spices",   7500,10000, +14, "High"),
                new CommodityItem("Turmeric",        "Spices",   7000, 8500, +12, "High")
        ));
        STATE_MARKET_DATA.put("Goa", Arrays.asList(
                new CommodityItem("Cashew",          "Fruit",    11000,14000, +13, "High"),
                new CommodityItem("Coconut",         "Fruit",       20,   27,  +6, "Medium"),
                new CommodityItem("Mango (Alphonso)","Fruit",      100,  160, +18, "Very High"),
                new CommodityItem("Rice",            "Cereals",   2200, 2450,  +7, "Medium"),
                new CommodityItem("Banana",          "Fruit",       30,   45,  +9, "Medium"),
                new CommodityItem("Jackfruit",       "Fruit",       25,   40, +11, "High"),
                new CommodityItem("Pineapple",       "Fruit",       35,   55, +10, "Medium"),
                new CommodityItem("Pepper",          "Spices",   40000,48000, +15, "High")
        ));
        STATE_MARKET_DATA.put("Arunachal Pradesh", Arrays.asList(
                new CommodityItem("Rice",           "Cereals",    2100, 2350,  +7, "High"),
                new CommodityItem("Maize",          "Cereals",    1800, 2000,  +5, "Medium"),
                new CommodityItem("Ginger",         "Spices",     8500,11500, +17, "Very High"),
                new CommodityItem("Orange",         "Fruit",        45,   75, +14, "High"),
                new CommodityItem("Apple",          "Fruit",        60,  100, +16, "High"),
                new CommodityItem("Kiwi",           "Fruit",        80,  130, +20, "Very High"),
                new CommodityItem("Cardamom",       "Spices",   110000,140000,+18, "Very High"),
                new CommodityItem("Large Cardamom", "Spices",    85000,110000,+15, "High")
        ));
        STATE_MARKET_DATA.put("Manipur", Arrays.asList(
                new CommodityItem("Rice",         "Cereals",    2050, 2300,  +8, "High"),
                new CommodityItem("Maize",        "Cereals",    1780, 1970,  +5, "Medium"),
                new CommodityItem("Ginger",       "Spices",     8000,10500, +15, "High"),
                new CommodityItem("Black Pepper", "Spices",    38000,46000, +14, "High"),
                new CommodityItem("Pineapple",    "Fruit",        30,   50, +11, "High"),
                new CommodityItem("Lemon",        "Fruit",        55,   85, +10, "Medium"),
                new CommodityItem("Bamboo Shoot", "Vegetables",   40,   70, +18, "High"),
                new CommodityItem("Banana",       "Fruit",        25,   40,  +8, "Medium")
        ));
        STATE_MARKET_DATA.put("Meghalaya", Arrays.asList(
                new CommodityItem("Potato",    "Vegetables", 1050, 1380, +11, "High"),
                new CommodityItem("Ginger",    "Spices",     9000,12000, +18, "Very High"),
                new CommodityItem("Turmeric",  "Spices",     7500, 9000, +13, "High"),
                new CommodityItem("Pineapple", "Fruit",        28,   48, +12, "High"),
                new CommodityItem("Banana",    "Fruit",        26,   40,  +8, "Medium"),
                new CommodityItem("Orange",    "Fruit",        42,   68, +13, "High"),
                new CommodityItem("Rice",      "Cereals",    2100, 2350,  +7, "High"),
                new CommodityItem("Maize",     "Cereals",    1800, 2000,  +5, "Medium")
        ));
        STATE_MARKET_DATA.put("Mizoram", Arrays.asList(
                new CommodityItem("Ginger",        "Spices",  8000,11000, +16, "High"),
                new CommodityItem("Turmeric",      "Spices",  7000, 8800, +12, "High"),
                new CommodityItem("Passion Fruit", "Fruit",     70,  110, +20, "Very High"),
                new CommodityItem("Banana",        "Fruit",     25,   38,  +8, "Medium"),
                new CommodityItem("Rice",          "Cereals", 2100, 2350,  +7, "High"),
                new CommodityItem("Maize",         "Cereals", 1780, 1980,  +5, "Medium"),
                new CommodityItem("Chilli",        "Spices",  9000,12000, +18, "Very High"),
                new CommodityItem("Orange",        "Fruit",     40,   65, +11, "High")
        ));
        STATE_MARKET_DATA.put("Nagaland", Arrays.asList(
                new CommodityItem("King Chilli (Bhut Jolokia)","Spices",20000,28000,+22,"Very High"),
                new CommodityItem("Ginger",   "Spices",     8200,11000, +16, "High"),
                new CommodityItem("Rice",     "Cereals",    2100, 2360,  +7, "High"),
                new CommodityItem("Maize",    "Cereals",    1800, 2000,  +5, "Medium"),
                new CommodityItem("Pineapple","Fruit",        30,   50, +11, "High"),
                new CommodityItem("Banana",   "Fruit",        25,   40,  +8, "Medium"),
                new CommodityItem("Potato",   "Vegetables", 1000, 1300,  +9, "High"),
                new CommodityItem("Soybean",  "Oil Seeds",  4200, 4700, +10, "High")
        ));
        STATE_MARKET_DATA.put("Tripura", Arrays.asList(
                new CommodityItem("Rice",      "Cereals",    2050, 2300,  +8, "High"),
                new CommodityItem("Pineapple", "Fruit",        28,   46, +13, "High"),
                new CommodityItem("Rubber",    "Cash Crops",16000,19000, +10, "High"),
                new CommodityItem("Banana",    "Fruit",        24,   38,  +9, "Medium"),
                new CommodityItem("Ginger",    "Spices",     7800,10200, +15, "High"),
                new CommodityItem("Sugarcane", "Cash Crops",  270,  300,  +3, "Low"),
                new CommodityItem("Jackfruit", "Fruit",        20,   38, +10, "Medium"),
                new CommodityItem("Mustard",   "Oil Seeds",  4700, 5100,  +6, "Medium")
        ));
        STATE_MARKET_DATA.put("Sikkim", Arrays.asList(
                new CommodityItem("Large Cardamom","Spices",80000,105000,+16,"Very High"),
                new CommodityItem("Ginger",  "Spices",     8500,11500, +17, "Very High"),
                new CommodityItem("Orange",  "Fruit",        40,   65, +12, "High"),
                new CommodityItem("Apple",   "Fruit",        58,   95, +15, "High"),
                new CommodityItem("Rice",    "Cereals",    2200, 2450,  +7, "High"),
                new CommodityItem("Maize",   "Cereals",    1850, 2050,  +5, "Medium"),
                new CommodityItem("Potato",  "Vegetables", 1100, 1450, +10, "High"),
                new CommodityItem("Turmeric","Spices",     7500, 9000, +13, "High")
        ));
        STATE_MARKET_DATA.put("Delhi", Arrays.asList(
                new CommodityItem("Wheat",       "Cereals",    2050, 2250,  +8, "High"),
                new CommodityItem("Potato",      "Vegetables", 1100, 1400, +10, "High"),
                new CommodityItem("Onion",       "Vegetables",   25,   42, +15, "High"),
                new CommodityItem("Tomato",      "Vegetables",   40,   62, +16, "High"),
                new CommodityItem("Mustard",     "Oil Seeds",  5100, 5500,  +8, "High"),
                new CommodityItem("Cauliflower", "Vegetables",   22,   40, +10, "Medium"),
                new CommodityItem("Spinach",     "Vegetables",   20,   35,  +9, "Medium"),
                new CommodityItem("Rice",        "Cereals",    2200, 2450,  +7, "Medium")
        ));
        STATE_MARKET_DATA.put("Jammu & Kashmir", Arrays.asList(
                new CommodityItem("Apple",             "Fruit",      65,  110, +20, "Very High"),
                new CommodityItem("Saffron",           "Spices",  300000,450000,+25,"Very High"),
                new CommodityItem("Walnut",            "Fruit",   12000, 18000, +18, "Very High"),
                new CommodityItem("Cherry",            "Fruit",     150,   240, +22, "Very High"),
                new CommodityItem("Pear",              "Fruit",      45,    80, +14, "High"),
                new CommodityItem("Plum",              "Fruit",      55,    90, +13, "High"),
                new CommodityItem("Rice (Mushkbudji)", "Cereals",  4500,  6000, +15, "High"),
                new CommodityItem("Wheat",             "Cereals",  2000,  2180,  +8, "Medium")
        ));
        STATE_MARKET_DATA.put("Ladakh", Arrays.asList(
                new CommodityItem("Apricot",        "Fruit",      120,  200, +18, "Very High"),
                new CommodityItem("Sea Buckthorn",  "Fruit",      200,  350, +25, "Very High"),
                new CommodityItem("Barley",         "Cereals",   1800, 2100,  +8, "High"),
                new CommodityItem("Pea",            "Vegetables",3200, 4200, +14, "High"),
                new CommodityItem("Potato",         "Vegetables",1200, 1600, +11, "High"),
                new CommodityItem("Wheat",          "Cereals",   2100, 2300,  +7, "Medium"),
                new CommodityItem("Turnip",         "Vegetables", 800, 1200,  +9, "Medium"),
                new CommodityItem("Buckwheat",      "Cereals",   4000, 5500, +12, "High")
        ));
        STATE_MARKET_DATA.put("Puducherry", Arrays.asList(
                new CommodityItem("Rice",      "Cereals",    2200, 2480,  +8, "High"),
                new CommodityItem("Sugarcane", "Cash Crops",  290,  315,  +3, "Low"),
                new CommodityItem("Groundnut", "Oil Seeds",  5100, 5700,  +6, "Medium"),
                new CommodityItem("Coconut",   "Fruit",        19,   24,  +5, "Medium"),
                new CommodityItem("Banana",    "Fruit",        32,   48, +10, "High"),
                new CommodityItem("Tomato",    "Vegetables",   38,   58, +15, "High"),
                new CommodityItem("Brinjal",   "Vegetables",   22,   38, +10, "Medium"),
                new CommodityItem("Mango",     "Fruit",        70,  110, +14, "High")
        ));
    }

    static final String[] ALL_STATES = {
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar",
            "Chhattisgarh", "Goa", "Gujarat", "Haryana",
            "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
            "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya",
            "Mizoram", "Nagaland", "Odisha", "Punjab",
            "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana",
            "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
            "Delhi", "Jammu & Kashmir", "Ladakh", "Puducherry"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        spinnerStateMarket = findViewById(R.id.spinnerStateMarket);
        rvCommodities      = findViewById(R.id.rvCommodities);
        tvMarketTitle      = findViewById(R.id.tvMarketTitle);
        tvCropsListed      = findViewById(R.id.tvCropsListed);
        tvAvgTrend         = findViewById(R.id.tvAvgTrend);
        tvHotPick          = findViewById(R.id.tvHotPick);
        marketLineChart    = findViewById(R.id.marketLineChart);

        rvCommodities.setLayoutManager(new LinearLayoutManager(this));
        setupChart();

        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ALL_STATES);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStateMarket.setAdapter(stateAdapter);
        spinnerStateMarket.setSelection(22); // Tamil Nadu default

        loadMarketForState("Tamil Nadu");

        spinnerStateMarket.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadMarketForState(ALL_STATES[position]);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        findViewById(R.id.btnBackMarket).setOnClickListener(v -> finish());
    }

    // ── Chart one-time style setup ────────────────────────────────────
    private void setupChart() {
        marketLineChart.setBackgroundColor(Color.TRANSPARENT);
        marketLineChart.getDescription().setEnabled(false);
        marketLineChart.getLegend().setEnabled(false);
        marketLineChart.setTouchEnabled(true);
        marketLineChart.setDragEnabled(true);
        marketLineChart.setScaleEnabled(false);
        marketLineChart.setPinchZoom(false);
        marketLineChart.setDrawGridBackground(false);
        marketLineChart.setExtraBottomOffset(8f);

        XAxis xAxis = marketLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#A5D6A7"));
        xAxis.setTextSize(9f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Nov", "Dec", "Jan", "Feb", "Mar", "Apr"}));

        YAxis left = marketLineChart.getAxisLeft();
        left.setTextColor(Color.parseColor("#A5D6A7"));
        left.setTextSize(9f);
        left.setDrawGridLines(true);
        left.setGridColor(Color.parseColor("#2E7D32"));
        left.setDrawAxisLine(false);
        left.setAxisLineColor(Color.TRANSPARENT);

        marketLineChart.getAxisRight().setEnabled(false);
    }

    // ── Load state data → update chart + list + metrics ──────────────
    private void loadMarketForState(String state) {
        tvMarketTitle.setText("Market: " + state);

        List<CommodityItem> items = STATE_MARKET_DATA.get(state);
        if (items == null || items.isEmpty()) {
            items = Collections.singletonList(
                    new CommodityItem("Data updating...", "—", 0, 0, 0, "—"));
        }

        // ── Update summary metrics ────────────────────────────────────
        tvCropsListed.setText(String.valueOf(items.size()));

        int totalTrend = 0;
        CommodityItem hotPick = items.get(0);
        for (CommodityItem c : items) {
            totalTrend += c.getDemandChangePercent();
            if (c.getDemandChangePercent() > hotPick.getDemandChangePercent()) hotPick = c;
        }
        int avgTrend = totalTrend / items.size();
        tvAvgTrend.setText((avgTrend >= 0 ? "↑ +" : "↓ ") + avgTrend + "%");
        tvAvgTrend.setTextColor(avgTrend >= 0
                ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
        tvHotPick.setText(hotPick.getName());

        // ── Build chart data from top commodity's simulated 6-month prices ──
        // We simulate Nov→Apr prices using minPrice as a base and demandChangePercent
        CommodityItem top  = hotPick;
        float base         = top.getMinPrice();
        float monthlyDelta = (base * top.getDemandChangePercent() / 100f) / 6f;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            entries.add(new Entry(i, base + monthlyDelta * i));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Price");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#A5D6A7"));
        dataSet.setCircleHoleColor(Color.parseColor("#1B5E20"));
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#2E7D32"));
        dataSet.setFillAlpha(60);

        marketLineChart.setData(new LineData(dataSet));
        marketLineChart.getXAxis().setLabelCount(6, true);
        marketLineChart.invalidate(); // refresh

        // ── RecyclerView ──────────────────────────────────────────────
        CommodityAdapter adapter = new CommodityAdapter(this, items);
        rvCommodities.setAdapter(adapter);
    }
}