package com.example.agroaid;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommodityAdapter extends RecyclerView.Adapter<CommodityAdapter.CommodityVH> {

    private final Context context;
    private final List<CommodityItem> items;

    public CommodityAdapter(Context context, List<CommodityItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public CommodityVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_commodity, parent, false);
        return new CommodityVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommodityVH h, int position) {
        CommodityItem item = items.get(position);

        h.tvName.setText(item.getName());
        h.tvCategory.setText(item.getCategory());
        h.tvPrice.setText(item.getPriceRange());
        h.tvTrend.setText(item.getTrendArrow());
        h.tvTrend.setTextColor(item.getTrendColor());
        h.tvDemand.setText(item.getDemandLevel());
        h.tvDemand.setTextColor(Color.WHITE);
        h.tvDemand.setBackgroundColor(item.getDemandBadgeColor());

        // Icon based on category
        h.tvIcon.setText(iconForCategory(item.getCategory()));
    }

    private String iconForCategory(String category) {
        switch (category) {
            case "Cereals":   return "🌾";
            case "Millets":   return "🌱";
            case "Oil Seeds": return "🫚";
            case "Vegetables":return "🥦";
            case "Fruit":     return "🍎";
            case "Spices":    return "🌶";
            case "Cash Crops":return "💰";
            case "Pulses":    return "🫘";
            default:          return "📦";
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class CommodityVH extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvPrice, tvTrend, tvDemand, tvIcon;

        CommodityVH(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvCommodityName);
            tvCategory = itemView.findViewById(R.id.tvCommodityCategory);
            tvPrice    = itemView.findViewById(R.id.tvCommodityPrice);
            tvTrend    = itemView.findViewById(R.id.tvCommodityTrend);
            tvDemand   = itemView.findViewById(R.id.tvCommodityDemand);
            tvIcon     = itemView.findViewById(R.id.tvCommodityIcon);
        }
    }
}