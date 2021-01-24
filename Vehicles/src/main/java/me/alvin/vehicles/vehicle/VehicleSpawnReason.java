package me.alvin.vehicles.vehicle;

/**
 * The reason the vehicle was spawned.
 */
public enum VehicleSpawnReason {
    /**
     * The vehicle was spawned using /vehicles spawn
     */
    COMMAND,
    /**
     * The vehicle was spawned using the creative vehicle
     * spawning tool.
     * TODO
     */
    CREATIVE,
    /**
     * The vehicle was spawned as it was crafted using a
     * vehicle crafting table.
     * TODO
     */
    CRAFTING
}