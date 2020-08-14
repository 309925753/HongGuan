package com.sk.weichat.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {
    private List<Item> data;
    private OnItemClickListener listener;

    public SearchHistoryAdapter(List<Item> data, OnItemClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    public static List<Item> toData(Collection<String> list) {
        List<Item> data = new ArrayList<>(list.size());
        for (String s : list) {
            Item item = new Item(s);
            data.add(item);
        }
        return data;
    }

    public void setData(List<Item> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_search_history, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Item item = data.get(i);
        viewHolder.tvSearchHistory.setText(item.chatHistory);
        viewHolder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
        viewHolder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    interface OnItemClickListener {
        void onItemClick(Item item);

        void onItemDelete(Item item);
    }

    static class Item {
        String chatHistory;

        Item(String chatHistory) {
            this.chatHistory = chatHistory;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSearchHistory = itemView.findViewById(R.id.tvSearchHistory);
        View ivDelete = itemView.findViewById(R.id.ivDelete);

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
