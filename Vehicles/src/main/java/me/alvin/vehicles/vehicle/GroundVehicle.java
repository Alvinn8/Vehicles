package me.alvin.vehicles.vehicle;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public abstract class GroundVehicle extends Vehicle {
    public GroundVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public GroundVehicle(@NotNull Location location, @NotNull Player creator) {
        super(location, creator);
    }

    @Override
    public void updateSpeed() {
        if (this.movement.forward != 0 && this.speed < this.getMaxSpeed()) {
            this.speed += this.getAccelerationSpeed() * this.movement.forward;
        }

        if (Math.abs(this.speed) < 0.01) {
            this.speed = 0;
        }
    }

    @Override
    public void calculateLocation() {
        if (this.movement.side != 0) {
            this.location.setYaw(this.location.getYaw() + this.movement.side * -5);
        }
        this.calculateGravity();
        Vector direction = this.location.getDirection();
        direction.multiply(this.speed / 20);
        this.location.add(direction);

        this.speed *= 0.9;
    }
}
