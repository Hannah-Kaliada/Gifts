package com.example.gifts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {

    private List<Gift> giftList;

    public GiftAdapter(List<Gift> giftList) {
        this.giftList = giftList;
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        Gift gift = giftList.get(position);
        holder.textViewGiftName.setText(gift.getName());
        holder.textViewGiftLink.setText(gift.getLink());
        holder.textViewGiftStore.setText(gift.getStore());
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public static class GiftViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGiftName, textViewGiftLink, textViewGiftStore;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGiftName = itemView.findViewById(R.id.textViewGiftName);
            textViewGiftLink = itemView.findViewById(R.id.textViewGiftLink);
            textViewGiftStore = itemView.findViewById(R.id.textViewGiftStore);
        }
    }
}
