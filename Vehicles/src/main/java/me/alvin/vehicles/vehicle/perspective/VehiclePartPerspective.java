package me.alvin.vehicles.vehicle.perspective;

import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.Vehicle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * A {@link Perspective} where the player sees the vehicle from a part of the
 * vehicle, for example helicopter skids or plane wheels.
 *
 * @param name The name of this perspective to show on the action.
 * @param relativePos The relative position to use for the camera, relative to the vehicle.
 */
public record VehiclePartPerspective(String name, RelativePos relativePos) implements Perspective {
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Location getCameraLocation(Vehicle vehicle, Player player) {
        return this.relativePos.relativeTo(vehicle.getLocation(), vehicle.getRoll());
    }

    @Override
    public boolean interpolate() {
        return false;
    }
}
