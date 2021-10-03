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
 *
 * @param relativePos The relative position to use for the camera, relative to the player.
 */
public record ThirdPersonPerspective(RelativePos relativePos) implements Perspective {
    @Override
    public String getName() {
        return "Third Person";
    }

    @Override
    public Location getCameraLocation(Vehicle vehicle, Player player) {
        return this.relativePos.relativeTo(player.getLocation(), 0);
    }

    @Override
    public boolean interpolate() {
        return true;
    }
}
