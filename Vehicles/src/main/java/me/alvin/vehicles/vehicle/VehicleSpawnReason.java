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
     */
    CREATIVE,
    /**
     * The vehicle was spawned as a hologram to preview the vehicle being placed.
     */
    CREATIVE_HOLOGRAM,
    /**
     * The vehicle was spawned as it was crafted using a
     * vehicle crafting table.
     */
    CRAFTING,
    /**
     * The vehicle was spawned as a hologram to preview a vehicle in a vehicle
     * crafting table.
     */
    CRAFTING_HOLOGRAM
}
