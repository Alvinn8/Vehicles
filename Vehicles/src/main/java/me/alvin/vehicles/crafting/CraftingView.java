package me.alvin.vehicles.crafting;

public enum CraftingView {
    /**
     * Selecting what vehicle to craft.
     */
    SELECTING,
    /**
     * Viewing a vehicle, but hasn't pressed the craft button yet.
     */
    VIEWING,
    /**
     * Crafting a vehicle.
     */
    CRAFTING,
    /**
     * Has finished a step and the timer is now counting down.
     */
    TIMER
}
