package me.alvin.vehicles.vehicle;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * A vehicle that drives on the ground.
 */
public abstract class GroundVehicle extends Vehicle {
    public GroundVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public GroundVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    public void calculateVelocity() {
        if (this.movement.side != 0) {
            this.location.setYaw(this.location.getYaw() + this.movement.side * -5);
        }
        this.calculateGravity();
        Vector direction = this.location.getDirection();
        direction.multiply(this.speed / 20);
        this.velX = direction.getX();
        this.velZ = direction.getZ();

        this.speed *= 0.9;
    }
}
