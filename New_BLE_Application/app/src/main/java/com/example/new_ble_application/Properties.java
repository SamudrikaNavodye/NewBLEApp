package com.example.new_ble_application;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

public class Properties extends AppCompatActivity implements GattManager.CharacteristicReadListener {
    private static final String TAG = "Properties";
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGatt gatt;

    private Button btnRead, btnWrite, btnNotify;
    private boolean isNotifying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_properties);

        btnRead = findViewById(R.id.btnRead);
        btnWrite = findViewById(R.id.btnWrite);
        btnNotify = findViewById(R.id.btnNotify);

        String characteristicUUID = getIntent().getStringExtra("characteristicUUID");
        gatt = GattManager.getGatt();

        if (gatt == null) {
            Log.e(TAG, "No Gatt instance received from MainActivity to Properties");
            Toast.makeText(this, "Error: No Bluetooth connection", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            Log.d(TAG, "Gatt instance received from MainActivity to Properties");
        }

        characteristic = findCharacteristicByUUID(characteristicUUID);
        if (characteristic == null) {
            Log.e(TAG, "Characteristic not found.");
            Toast.makeText(this, "Error: Characteristic not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            Log.d(TAG, "Gatt characteristic received from MainActivity to Properties");
        }

        setupButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GattManager.addCharacteristicReadListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GattManager.removeCharacteristicReadListener(this);
    }

    private BluetoothGattCharacteristic findCharacteristicByUUID(String uuid) {
        if (gatt != null) {
            for (BluetoothGattService service : gatt.getServices()) {
                for (BluetoothGattCharacteristic charac : service.getCharacteristics()) {
                    if (charac.getUuid().toString().equals(uuid)) {
                        return charac;
                    }
                }
            }
        }
        return null;
    }

    private void setupButtons() {
        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
            btnRead.setVisibility(View.VISIBLE);
            btnRead.setOnClickListener(view -> readCharacteristic());
        }
        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
            btnWrite.setVisibility(View.VISIBLE);
            btnWrite.setOnClickListener(view -> writeCharacteristic());
        }
        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            btnNotify.setVisibility(View.VISIBLE);
            btnNotify.setOnClickListener(view -> toggleNotify());
        }
    }

    private void readCharacteristic() {
        if (gatt == null) {
            Log.e(TAG, "BluetoothGatt is null");
            Toast.makeText(this, "Null Gatt Cannot read characteristic", Toast.LENGTH_SHORT).show();
            return;
        } else if (characteristic == null) {
            Log.e(TAG, "Characteristic is null");
            Toast.makeText(this, "Null Character Cannot read characteristic", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Log.d(TAG, "BluetoothGatt && Characteristic is not null.");
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Bluetooth connect permission not granted");
            return;
        }

        boolean success = gatt.readCharacteristic(characteristic);
        if (!success) {
            Log.e(TAG, "Characteristic read failed");
            Toast.makeText(this, "Failed to initiate characteristic read", Toast.LENGTH_SHORT).show();
        } else {
            //displayCharacteristicValue(characteristic);
            // we can't read here, we have to wait for the read to complete
//            characteristicRead(characteristic);
        }
    }

    @Override
    public void characteristicRead(BluetoothGattCharacteristic characteristic) {
        // this gets called from GattManager now
        String value = characteristic.getStringValue(0);
        new AlertDialog.Builder(this)
                .setTitle("Characteristic Value")
                .setMessage("Value: " + value)
                .setPositiveButton("OK", null)
                .show();
    }


//    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic charac, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                runOnUiThread(() -> displayCharacteristicValue(charac));
//            } else {
//                Log.e(TAG, "Failed to read characteristic, status: " + status);
//                runOnUiThread(() -> Toast.makeText(Properties.this, "Read failed", Toast.LENGTH_SHORT).show());
//            }
//        }
//    };

    private void displayCharacteristicValue(BluetoothGattCharacteristic characteristic) {
        String value = characteristic.getStringValue(0);
        new AlertDialog.Builder(this)
                .setTitle("Characteristic Value")
                .setMessage("Value: " + value)
                .setPositiveButton("OK", null)
                .show();
    }

    private void writeCharacteristic() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Write Value");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String value = input.getText().toString();
            characteristic.setValue(value.getBytes());

            if (gatt == null) {
                Log.e(TAG, "BluetoothGatt is null");
                Toast.makeText(this, "Failed to write characteristic", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Bluetooth connect permission not granted");
                return;
            }

            boolean success = gatt.writeCharacteristic(characteristic);
            Toast.makeText(this, success ? "Write successful" : "Write failed", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void toggleNotify() {
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (descriptor == null) {
            Log.e(TAG, "Notification descriptor not found");
            Toast.makeText(this, "Notification descriptor not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Bluetooth connect permission not granted");
            return;
        }

        gatt.setCharacteristicNotification(characteristic, !isNotifying);
        descriptor.setValue(isNotifying ? BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        boolean success = gatt.writeDescriptor(descriptor);
        if (success) {
            btnNotify.setText(isNotifying ? "Start Notify" : "Stop Notify");
            Toast.makeText(this, isNotifying ? "Notifications disabled" : "Notifications enabled", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Failed to set notification descriptor");
            Toast.makeText(this, "Failed to change notification setting", Toast.LENGTH_SHORT).show();
        }

        isNotifying = !isNotifying;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gatt != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            gatt.close();
            gatt = null;
        }
    }
}
