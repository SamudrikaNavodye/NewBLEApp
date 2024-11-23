package com.example.new_ble_application;

import java.util.HashMap;
import java.util.Map;

public class BluetoothUtils {

    private static final Map<String, String> characteristicNames;
    private static int unknownCharacteristicCounter = 1;  // Counter for unknown characteristics
    private static final Map<String, String> uuidToDeviceName = new HashMap<>();  // Cache for device names



    static {
        characteristicNames = new HashMap<>();
        characteristicNames.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        characteristicNames.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        characteristicNames.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
        characteristicNames.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        characteristicNames.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        // Add other standard UUIDs here
    }

    public static String getCharacteristicName(String uuid) {
//        String name = characteristicNames.get(uuid);
//        return name != null ? name : "Unknown Characteristic";

        if (characteristicNames.containsKey(uuid)) {
            return characteristicNames.get(uuid);
        } else {
            // Check if we already assigned a "Device" name to this UUID
            if (!uuidToDeviceName.containsKey(uuid)) {
                // Assign a new "Device" name for this unknown UUID
                uuidToDeviceName.put(uuid, "Device " + unknownCharacteristicCounter);
                unknownCharacteristicCounter++;
            }
            return uuidToDeviceName.get(uuid); // Return the assigned device name
        }
    }

}
