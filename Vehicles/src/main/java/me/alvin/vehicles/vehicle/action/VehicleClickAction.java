package me.alvin.vehicles.vehicle.action;

import me.alvin.vehicles.vehicle.Vehicle;
import org.bukkit.entity.Player;

/**
 * An interface that can be added to a {@link VehicleAction} class which can be
 * clicked directly trough the player selecting a certain hotbar slot and left clicking.
 */
public interface VehicleClickAction extends VehicleAction {
    /**
     * Called when a player left clicks with their hotbar slot being set to
     * this action.
     *
     * @param vehicle The vehicle the player is in
     * @param player The player that has opened the menu
     */
    void onClick(Vehicle vehicle, Player player);
}
