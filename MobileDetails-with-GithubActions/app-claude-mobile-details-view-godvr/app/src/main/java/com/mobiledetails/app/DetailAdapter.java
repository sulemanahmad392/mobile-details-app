package com.mobiledetails.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER  = 0;
    private static final int TYPE_DETAIL  = 1;

    private final List<Object> items; // String = header, DeviceDetail = row

    public DetailAdapter(List<Object> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_DETAIL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(v);
        }
        View v = inf.inflate(R.layout.item_detail, parent, false);
        return new DetailViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).title.setText((String) items.get(position));
        } else {
            DeviceDetail d = (DeviceDetail) items.get(position);
            DetailViewHolder h = (DetailViewHolder) holder;
            h.label.setText(d.getLabel());
            h.value.setText(d.getValue());
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        HeaderViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tvHeader);
        }
    }

    static class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView label, value;
        DetailViewHolder(View v) {
            super(v);
            label = v.findViewById(R.id.tvLabel);
            value = v.findViewById(R.id.tvValue);
        }
    }
}
