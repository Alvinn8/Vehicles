package me.alvin.vehicles.registry;

import me.alvin.vehicles.vehicle.VehicleType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VehicleRegistry {
    private final Map<String, VehicleType> data = new HashMap<>();

    /**
     * Register the specified vehicle
     *
     * @param vehicleType The vehicle type to register
     */
    public void registerVehicle(VehicleType vehicleType) {
        this.data.put(vehicleType.getId(), vehicleType);
    }

    /**
     * Get the registered vehicle with the specified id
     *
     * @param id The id of the vehicle to get
     * @return The {@link VehicleType} for the vehicle
     */
    public VehicleType getVehicle(String id) {
        return this.data.get(id);
    }

    /**
     * Get an unmodifiable map of all the registered vehicles and their ids.
     *
     * @return The map of registered vehicles
     */
    public Map<String, VehicleType> getRegisteredVehicles() {
        return Collections.unmodifiableMap(this.data);
    }
}
