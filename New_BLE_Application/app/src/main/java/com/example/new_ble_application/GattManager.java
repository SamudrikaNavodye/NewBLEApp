package com.example.new_ble_application;

import android.bluetooth.BluetoothGatt;

public class GattManager {
    private static BluetoothGatt gatt;

    public static BluetoothGatt getGatt() {
        return gatt;
    }

    public static void setGatt(BluetoothGatt gattInstance) {
        gatt = gattInstance;
    }

}
