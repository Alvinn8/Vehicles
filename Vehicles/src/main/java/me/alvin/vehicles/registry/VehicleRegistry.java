package me.alvin.vehicles.registry;

import me.alvin.vehicles.vehicle.VehicleType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public Map<String, VehicleType> getMap() {
        return Collections.unmodifiableMap(this.data);
    }

    /**
     * Get an unmodifiable set of all registered vehicle ids.
     *
     * @return The set of ids
     */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(this.data.keySet());
    }

    /**
     * Get an unmodifiable collection of all registered vehicle types.
     *
     * @return The collection of vehicle types.
     */
    public Collection<VehicleType> getValues() {
        return Collections.unmodifiableCollection(this.data.values());
    }
}
