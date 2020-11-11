package me.alvin.vehicles.vehicle;

import org.bukkit.Location;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface VehicleSpawnConstructorFunction {
    Vehicle apply(Location location, Player creator, VehicleSpawnReason reason);
}
