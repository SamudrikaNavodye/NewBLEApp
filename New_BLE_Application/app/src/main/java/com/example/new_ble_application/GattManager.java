package com.example.new_ble_application;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.HashSet;
import java.util.Set;

public class GattManager {
    private static BluetoothGatt gatt;

    public static BluetoothGatt getGatt() {
        return gatt;
    }

    public static void setGatt(BluetoothGatt gattInstance) {
        gatt = gattInstance;
    }

    private static final Set<CharacteristicReadListener> listeners = new HashSet<>();

    public static void addCharacteristicReadListener(CharacteristicReadListener listener) {
        listeners.add(listener);
    }

    public static void removeCharacteristicReadListener(CharacteristicReadListener listener) {
        listeners.remove(listener);
    }

    public static void characteristicRead(BluetoothGattCharacteristic characteristic) {
        for (CharacteristicReadListener listener : listeners) {
            listener.characteristicRead(characteristic);
        }
    }

    public interface CharacteristicReadListener {
        void characteristicRead(BluetoothGattCharacteristic characteristic);
    }

}
