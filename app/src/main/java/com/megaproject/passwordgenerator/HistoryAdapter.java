package com.megaproject.passwordgenerator;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<HistoryItem> items;
    private final OnItemActionListener listener;

    public interface OnItemActionListener {
        void onCopy(String password);
        void onDelete(HistoryItem item);
        void onEdit(HistoryItem item);
    }

    public HistoryAdapter(List<HistoryItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<HistoryItem> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HistoryItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_password, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item);
        } else if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).bind(item, position, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        HeaderViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_header_title);
        }

        void bind(HistoryItem item) {
            tvTitle.setText(item.getHeaderTitle());
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPassword, tvTimestamp;
        ImageView ivIcon;
        View statusDot, iconContainer;
        ImageButton btnCopy, btnDelete, btnEdit;

        ItemViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPassword = itemView.findViewById(R.id.tv_password);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            statusDot = itemView.findViewById(R.id.status_dot);
            iconContainer = itemView.findViewById(R.id.icon_container);
            btnCopy = itemView.findViewById(R.id.btn_copy);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }

        void bind(HistoryItem item, int position, OnItemActionListener listener) {
            tvName.setText(item.getName() != null && !item.getName().isEmpty() ? item.getName() : "Unnamed");
            tvPassword.setText(item.getPassword());
            tvTimestamp.setText(item.getTimestamp());
            
            ivIcon.setImageResource(item.getIconResId());
            
            Context context = itemView.getContext();
            int color = ContextCompat.getColor(context, item.getColorResId()); 
            int tint = ContextCompat.getColor(context, item.getIconTintResId());

            iconContainer.setBackgroundTintList(ColorStateList.valueOf(color));
            ivIcon.setImageTintList(ColorStateList.valueOf(tint));
            statusDot.setBackgroundTintList(ColorStateList.valueOf(tint));

            btnCopy.setOnClickListener(v -> listener.onCopy(item.getPassword()));
            btnDelete.setOnClickListener(v -> listener.onDelete(item));
            btnEdit.setOnClickListener(v -> listener.onEdit(item));
        }
    }
}
