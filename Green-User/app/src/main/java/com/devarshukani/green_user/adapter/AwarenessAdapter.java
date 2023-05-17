package com.devarshukani.green_user.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devarshukani.green_user.R;
import com.devarshukani.green_user.model.AwarenessModel;

import java.util.List;

public class AwarenessAdapter extends RecyclerView.Adapter<AwarenessAdapter.CardViewHolder> {
    private List<AwarenessModel> cardList;

    public void setData(List<AwarenessModel> cardList) {
        this.cardList = cardList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_awareness_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        AwarenessModel card = cardList.get(position);
        holder.titleTextView.setText(card.getTitle());
        holder.descriptionTextView.setText(card.getDescription());
    }

    @Override
    public int getItemCount() {
        return cardList != null ? cardList.size() : 0;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }
    }
}
