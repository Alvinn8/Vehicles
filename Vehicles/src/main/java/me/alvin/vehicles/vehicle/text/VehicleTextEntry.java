package me.alvin.vehicles.vehicle.text;

import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleClickAction;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * An entry in the vehicle's action bar text.
 */
public interface VehicleTextEntry {
    /**
     * Whether the text entry should display right now.
     *
     * <p>Will always be called before {@link #getMessage(Vehicle, Player)}
     * and if this method returns false, getMessage will never be called.</p>
     *
     * @param vehicle The vehicle the player is in
     * @param player The player to display the text for, if shown
     * @return Whether to show
     */
    default boolean shouldDisplay(Vehicle vehicle, Player player) {
        return true;
    }

    /**
     * Get the text to display.
     *
     * @param vehicle The vehicle the player is in
     * @param player The player to display the text for
     * @return The text component to display
     */
    Component getMessage(Vehicle vehicle, Player player);

    // Implementations
    VehicleTextEntry ACTION = new VehicleTextEntry() {
        private VehicleClickAction cashedAction;

        @Override
        public boolean shouldDisplay(Vehicle vehicle, Player player) {
            this.cashedAction = vehicle.getClickAction(player.getInventory().getHeldItemSlot());
            return this.cashedAction != null;
        }

        @Override
        public Component getMessage(Vehicle vehicle, Player player) {
            return Component.empty()
                .append(Component.text("Selected Action: "))
                .append(this.cashedAction.getActionBarText(vehicle, player));
        }
    };
}
