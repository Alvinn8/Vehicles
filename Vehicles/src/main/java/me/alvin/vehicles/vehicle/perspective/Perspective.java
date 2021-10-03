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

    /**
     * Whether this perspective should use a normal interpolating entity or a non
     * interpolating entity.
     *
     * <p>Perspectives that are close to the vehicle or are at a part of the vehicle
     * should be non-interpolating to avoid the camera shaking and getting gradually
     * further away from the vehicle due to interpolation.</p>
     *
     * <p>For perspectives that are controlled by the player and that can move rapidly
     * (third person perspective), they should be interpolated to make harch movements
     * smoother. These perspectives should avoid being too close to the vehicle though
     * to avoid the problems described in the paragraph above.</p>
     *
     * @return Whether to use an interpolated (normal) entity.
     * @see me.alvin.vehicles.util.ni.NIE
     */
    boolean interpolate();
}
