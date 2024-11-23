package com.example.new_ble_application;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private final List<String> devicesList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String deviceInfo);
    }

    public DeviceAdapter(List<String> devicesList, OnItemClickListener listener) {
        this.devicesList = devicesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        String deviceInfo = devicesList.get(position);
        String[] details = deviceInfo.split("\n");
        holder.deviceName.setText(details[0]);
        holder.deviceAddress.setText(details[1]);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(deviceInfo));
    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName, deviceAddress;

        DeviceViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
        }
    }
}

