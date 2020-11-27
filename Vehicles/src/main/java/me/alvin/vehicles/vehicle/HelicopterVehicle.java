package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.util.RelativePos;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public abstract class HelicopterVehicle extends Vehicle {
    // protected double velX = 0;
    // // velY is defined in Vehicle
    // protected double velZ = 0;
    /**
     * The current yaw rotation for the rotor. The
     * {@link Vehicle#speed} is added to this value
     * when the vehicle is moving
     */
    protected float rotorRotation = 0;

    public HelicopterVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public HelicopterVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void calculateLocation() {
        this.rotorRotation += this.speed;
        if (this.movement.space && this.speed > 1) {
            this.velY += 0.02;
        }
        this.calculateGravity();
        if (this.getDriver() != null) {
            this.location.setYaw(this.getDriver().getLocation().getYaw());
        }
        Vector direction = this.location.getDirection();
        direction.multiply(this.speed / 20);
        this.location.add(direction);

        this.speed *= 0.95;
    }

    @Override
    public void calculateGravity() {
        boolean fall = true;
        if (this.velY <= 0) {
            for (RelativePos gravityPoint : this.getType().getGravityPoints()) {
                Location location = gravityPoint.relativeTo(this.location);
                location.subtract(0.0D, 0.001D, 0.0D);
                Block block = location.getBlock();
                if (!block.isPassable()) {
                    fall = false;
                    break;
                }
            }
        } else {
            fall = false;
        }
        if (fall) {
            this.velY -= GRAVITY;
        } else {
            this.velY -= 0.01;
        }
        if (!fall && this.velY <= 0) this.velY = 0;
        this.location.add(0, this.velY, 0);
    }
}
