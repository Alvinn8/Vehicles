package me.alvin.vehicles.vehicle.perspective;

import me.alvin.vehicles.vehicle.Vehicle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * A camera perspective the user can use when they are in a vehicle.
 */
public interface Perspective {
    /**
     * Get the name of this perspective to show on the action.
     *
     * @return The name of this perspective.
     */
    String getName();

    /**
     * Get the location to put the camera.
     * <p>
     * The yaw and pitch of the returned location will be ignored as that is
     * controlled by the player.
     *
     * @param vehicle The vehicle the player is in.
     * @param player The player that is viewing the "camera".
     * @return The location to put the camera.
     */
    Location getCameraLocation(Vehicle vehicle, Player player);
}
