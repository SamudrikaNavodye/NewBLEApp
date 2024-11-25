package com.example.new_ble_application;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BLE";
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> devicesList;
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 3;
    private BluetoothGatt bluetoothGatt;
    private DeviceAdapter deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Starting up MainActivity");

        Button searchButton = findViewById(R.id.btn_search);
        RecyclerView deviceRecyclerView = findViewById(R.id.rv_device_list);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devicesList = new ArrayList<>();

        deviceAdapter = new DeviceAdapter(devicesList, deviceInfo -> {
            String[] deviceDetails = deviceInfo.split("\n");
            String deviceAddress = deviceDetails[1];
            connectToDevice(deviceAddress);
        });

        if (bluetoothAdapter == null) {
            Log.e(TAG, "No Bluetooth Adapter found");
            Toast.makeText(this, "No Bluetooth Available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        enableBluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        searchBluetoothDevices();
                    } else {
                        Toast.makeText(MainActivity.this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceRecyclerView.setAdapter(deviceAdapter);

        searchButton.setOnClickListener(view -> {
            // Check if bluetooth is enabled
            boolean isBluetoothEnabled = bluetoothAdapter.isEnabled();

            // check if location is enabled
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // if both are disabled, prompt the user to enable both
            if (!isBluetoothEnabled && !isLocationEnabled) {
                Log.d(TAG, "Both Bluetooth and location are disabled, requesting to enable both");

                // Request to enable Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBluetoothLauncher.launch(enableBtIntent);

                //Prompt to enable Bluetooth
                Toast.makeText(getApplicationContext(), "Please enable location services to discover devices", Toast.LENGTH_LONG).show();
                Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(enableLocationIntent);

            } else if (!isBluetoothEnabled) { // Only Bluetooth is disabled
                Log.d(TAG, "Only Bluetooth is disabled, requesting to enable it");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBluetoothLauncher.launch(enableBtIntent);

            } else if (!isLocationEnabled) { // Only location is disabled
                Log.d(TAG, "Only location is disabled, prompting user to enable it");
                Toast.makeText(getApplicationContext(), "Please enable location services to discover devices", Toast.LENGTH_LONG).show();
                Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(enableLocationIntent);

            } else { // Both are enabled
                Log.d(TAG, "Both Bluetooth and location are enabled, checking permissions and starting device discovery");
                checkPermissionsAndDiscover();
            }
        });


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, filter);
    }

    private void checkPermissionsAndDiscover() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                }, REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        searchBluetoothDevices();
    }

    private void searchBluetoothDevices() {
        devicesList.clear();
        deviceAdapter.notifyDataSetChanged();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        bluetoothAdapter.startDiscovery();
        Toast.makeText(this, "Searching for devices...", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> {
            if (devicesList.isEmpty()) {
                Toast.makeText(MainActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
            }
        }, 20000);
    }

    private void connectToDevice(String deviceAddress) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        if (device != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothGatt = device.connectGatt(this, false, gattCallback);
            Toast.makeText(MainActivity.this, "Connecting to device: " + device.getName(), Toast.LENGTH_SHORT).show();
            GattManager.setGatt(bluetoothGatt);
        }
    }

    private void updateDeviceStatus(String deviceAddress, String status) {
        Toast.makeText(this, "Device: " + deviceAddress + " is now " + status, Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    String deviceName = device.getName();
                    if (deviceName == null || deviceName.isEmpty()) {
                        deviceName = "Unknown Device";
                    }
                    String deviceAddress = device.getAddress();
                    devicesList.add(deviceName + "\n" + deviceAddress);
                    deviceAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    updateDeviceStatus(device.getAddress(), "Connected");
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    updateDeviceStatus(device.getAddress(), "Disconnected");
                }
            }
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                displayGattServices(services);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                // Pass the value to Properties to display
//                Intent intent = new Intent(MainActivity.this, Properties.class);
//                intent.putExtra("characteristic", characteristic);
//                startActivity(intent);
                // send this read to all who are subscribed
                GattManager.characteristicRead(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // Handle notifications
            String newValue = new String(characteristic.getValue());
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Notification received: " + newValue, Toast.LENGTH_SHORT).show());
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        if (gattServices != null) {
            Log.d(TAG, "gattServices:GATT services found." + gattServices.toString());
        }

        ArrayList<String> characteristicList = new ArrayList<>();
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                characteristicList.add(gattCharacteristic.getUuid().toString());
            }
        }

        // Start Characteristics activity
        Intent intent = new Intent(MainActivity.this, Characteristics.class);
        intent.putStringArrayListExtra("characteristics", characteristicList);
        startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (GattManager.getGatt() != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            GattManager.getGatt().disconnect();
            GattManager.getGatt().close();
            GattManager.setGatt(null);
        }
        unregisterReceiver(receiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS || requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionsAndDiscover();
            } else {
                Toast.makeText(this, "Permissions are required for Bluetooth operation.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
