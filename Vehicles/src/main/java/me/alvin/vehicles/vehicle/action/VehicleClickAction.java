package me.alvin.vehicles.vehicle.action;

import me.alvin.vehicles.vehicle.Vehicle;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * An interface that can be added to a {@link VehicleAction} class which can be
 * clicked directly trough the player selecting a certain hotbar slot and left clicking.
 */
public interface VehicleClickAction extends VehicleAction {
    /**
     * Get the text to display on the player's actionbar when they have
     * selected this click action.
     *
     * <p>Can change depending on various factors, for example cooldowns.
     * But the name of the action should preferably be in the component,
     * preferably at the start. Changing it's color depending on whether
     * it can be used for example is also fine.</p>
     *
     * @param vehicle The vehicle the player is in
     * @param player THe player that has selected the action
     * @return The component to display.
     */
    Component getActionBarText(Vehicle vehicle, Player player);

    /**
     * Called when a player left clicks with their hotbar slot being set to
     * this action.
     *
     * @param vehicle The vehicle the player is in
     * @param player The player that has opened the menu
     */
    void onHotbarClick(Vehicle vehicle, Player player);
}
