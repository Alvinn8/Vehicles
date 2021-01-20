package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.util.RelativePos;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.jetbrains.annotations.NotNull;

/**
 * A helicopter vehicle that can take off vertically. Helicopter vehicles do
 * not get affected by gravity.
 */
public abstract class HelicopterVehicle extends Vehicle {
    /**
     * The current yaw rotation for the rotor. The
     * {@link Vehicle#speed} is added to this value
     * when the vehicle is moving
     */
    protected float rotorRotation = 0;
    /**
     * The current speed the rotors are spinning at,
     * this value will be added to the {@link #rotorRotation}
     * each tick. When a driver is inside the vehicle this will
     * start increasing.
     */
    protected float rotorSpeed = 0;
    /**
     * Whether the vehicle is on the ground or not. Is updated in
     * {@link #calculateGravity()} and is used in some calculations.
     */
    protected boolean onGround = true;

    public HelicopterVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public HelicopterVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    /**
     * @return Whether the helicopter is on the ground or not.
     */
    public boolean isOnGround() {
        return this.onGround;
    }

    /**
     * @return Whether the helicopter is flying, is always the opposite of {@link #isOnGround()}
     */
    public boolean isFlying() {
        return !this.onGround;
    }

    @Override
    public void updateSpeed() {
        if (this.movement.forward != 0 && Math.abs(this.speed) < this.getMaxSpeed() && this.canAccelerate() && this.isFlying()) {
            this.speed += this.getAccelerationSpeed() * this.movement.forward;
        }

        if (Math.abs(this.speed) < 0.01) {
            this.speed = 0;
        }

        if (this.getDriver() != null && this.rotorSpeed < 20) {
            this.rotorSpeed += 0.25;
        } else if (this.rotorSpeed > 0) {
            this.rotorSpeed -= 0.25;
        } else if (this.rotorSpeed < 0) {
            this.rotorSpeed = 0;
        }

        if (Math.abs(this.rotorSpeed) < 0.1) {
            this.rotorSpeed = 0;
        }
    }

    @Override
    public void calculateVelocity() {
        // Rotor
        this.rotorRotation += this.rotorSpeed;

        // Y Velocity
        if (this.movement.space && this.rotorSpeed > 15) {
            this.velY += 0.01;
        } else if (this.isFlying()) {
            // Not pressing space and the vehicle is flying
            this.velY -= 0.01;
        }

        if (this.velY > 0.5)  this.velY =  0.5;
        if (this.velY < -0.5) this.velY = -0.5;

        this.calculateGravity();

        // Rotation
        if (this.getDriver() != null && this.isFlying()) {
            float driverYaw = this.getDriver().getLocation().getYaw() % 360.0F;
            float helicopterYaw = this.location.getYaw() % 360.0F;
            float difference = driverYaw - helicopterYaw;
            if (difference < -180.0F) {
                difference += 360.0F;
            }
            if (difference >= 180.0F) {
                difference -= 360.0F;
            }
            this.location.setYaw(helicopterYaw + difference * 0.15F);
        }

        // Movement
        double yawRadians = Math.toRadians(this.location.getYaw() + (this.movement.side * -90));
        double xz = Math.cos(Math.toRadians(this.location.getPitch()));

        double dirX = (-xz * Math.sin(yawRadians)) * this.speed / 20;
        double dirZ = (xz * Math.cos(yawRadians)) * this.speed / 20;

        this.velX += dirX * 0.05;
        this.velZ += dirZ * 0.05;

        // Drag
        if (this.isFlying()) {
            this.velX *= 0.98;
            this.velZ *= 0.98;
        } else {
            this.velX *= 0.85;
            this.velZ *= 0.85;
        }
        this.speed *= 0.95;
    }

    @Override
    public void calculateGravity() {
        if (this.velY == 0 && this.onGround) return;

        this.onGround = false;
        if (this.velY < 0) {
            for (RelativePos gravityPoint : this.getType().getGravityPoints()) {
                Location location = gravityPoint.relativeTo(this.location);
                location.subtract(0.0D, 0.001D, 0.0D);
                Block block = location.getBlock();
                if (!block.isPassable()) {
                    this.onGround = true;
                    this.velY = 0;
                    break;
                }
            }
        }
    }

    @Override
    public boolean isMoving() {
        return this.rotorSpeed != 0;
    }
}
