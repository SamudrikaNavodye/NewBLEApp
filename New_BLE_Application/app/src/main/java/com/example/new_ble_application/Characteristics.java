package com.example.new_ble_application;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class Characteristics extends AppCompatActivity {

    private static final String TAG = "Characteristics";


    private BluetoothGatt bluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_characteristics);

        // Get the characteristics list from the intent
        ArrayList<String> characteristics = getIntent().getStringArrayListExtra("characteristics");

        // Setup the recycler view
        RecyclerView characteristicsRecyclerView = findViewById(R.id.rv_rv_characteristics);
        characteristicsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pass 'this' as the context and list of characteristics
        CharacteristicAdapter adapter = new CharacteristicAdapter(this, characteristics);
        characteristicsRecyclerView.setAdapter(adapter);

    }


}