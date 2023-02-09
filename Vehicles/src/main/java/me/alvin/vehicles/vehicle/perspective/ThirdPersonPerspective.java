package me.alvin.vehicles.vehicle.perspective;

import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.Vehicle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * A {@link Perspective} where the player sees the vehicle from the third person.
 * <p>
 * An example ThirdPersonPerspective could be:
 * <br>
 * {@code new ThirdPersonPerspective(new RelativePos(0, 0, -15))}
 */
public final class ThirdPersonPerspective implements Perspective {
    private final RelativePos relativePos;

    /**
     * @param relativePos The relative position to use for the camera, relative to the player.
     */
    public ThirdPersonPerspective(RelativePos relativePos) {
        this.relativePos = relativePos;
    }

    private float yaw;
    private float pitch;

    @Override
    public Location getCameraLocation(Vehicle vehicle, Player player) {
        Location playerLocation = player.getLocation();
        this.yaw = Vehicle.interpolatedRotation(this.yaw, playerLocation.getYaw());
        this.pitch = Vehicle.interpolatedRotation(this.pitch, playerLocation.getPitch());
        playerLocation.setYaw(this.yaw);
        playerLocation.setPitch(this.pitch);
        return this.relativePos.relativeTo(playerLocation, 0);
    }

    @Override
    public String getName() {
        return "Third Person";
    }

    @Override
    public boolean interpolate() {
        return false;
    }
}
