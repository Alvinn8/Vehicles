package me.alvin.vehicles.vehicle.action;

import me.alvin.vehicles.vehicle.Vehicle;
import org.bukkit.persistence.PersistentDataContainer;

/**
 * The base vehicle action interface, vehicle actions should implement
 * {@link VehicleMenuAction}, {@link VehicleClickAction} or both.
 */
public interface VehicleAction {
    /**
     * Called when a vehicle is loading data.
     *
     * @param vehicle The vehicle that is loading
     * @param data The data the vehicle is reading data from
     */
    default void onLoad(Vehicle vehicle, PersistentDataContainer data) {}

    /**
     * Called when the vehicle is saving it's data.
     *
     * @param vehicle The vehicle that is saving
     * @param data The container the vehicle is using to save data to.
     */
    default void onSave(Vehicle vehicle, PersistentDataContainer data) {}

    /**
     * Called when the vehicle is being removed.
     *
     * @param vehicle The vehicle that is being removed.
     */
    default void onRemove(Vehicle vehicle) {};
}
