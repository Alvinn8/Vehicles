package me.alvin.vehicles.vehicle.action;

import me.alvin.vehicles.vehicle.Vehicle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * An interface that can be added to a {@link VehicleAction} class which will make
 * an entry in the vehicle menu that can be clicked.
 */
public interface VehicleMenuAction extends VehicleAction {
    /**
     * Get the item to render in the vehicle menu. When this item is clicked the
     * {@link #onClick(Vehicle, Player)} method will be called.
     *
     * @param vehicle The vehicle the player is in
     * @param player The player that has opened the menu
     * @return The item to render in the menu
     */
    ItemStack getEntryItem(Vehicle vehicle, Player player);

    /**
     * Called when the entry item made in {@link #getEntryItem(Vehicle, Player)} is
     * clicked. Can for example be used to open another inventory gui with further
     * buttons, etc.
     *
     * @param vehicle The vehicle the player is in
     * @param player The player that has opened the menu
     */
    void onClick(Vehicle vehicle, Player player);
}
