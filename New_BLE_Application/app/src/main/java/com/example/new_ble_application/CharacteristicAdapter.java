package com.example.new_ble_application;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CharacteristicAdapter extends RecyclerView.Adapter<CharacteristicAdapter.CharacteristicViewHolder> {
    private final List<String> characteristics;
    private final Context context;

    public CharacteristicAdapter(Context context, List<String> characteristics) {
        this.context = context;
        this.characteristics = characteristics;
    }

    @NonNull
    @Override
    public CharacteristicAdapter.CharacteristicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.characteristic_item, parent, false);
        return new CharacteristicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacteristicAdapter.CharacteristicViewHolder holder, int position) {
        // Set characteristic names from BluetoothUtils
        String characteristicUUID = characteristics.get(position);
        String characteristicName = BluetoothUtils.getCharacteristicName(characteristicUUID);
        holder.characteristicName.setText(characteristicName + " (" + characteristicUUID + ")");

        // Handle click on characteristic item and send intent to Property.class
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Properties.class);
            intent.putExtra("characteristicUUID", characteristicUUID);  // Pass the UUID for lookup
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return characteristics.size();
    }

    public static class CharacteristicViewHolder extends RecyclerView.ViewHolder {
        TextView characteristicName;

        public CharacteristicViewHolder(View itemView) {
            super(itemView);
            characteristicName = itemView.findViewById(R.id.tv_characteristic_name);
        }
    }
}
