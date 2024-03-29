package me.alvin.vehicles.vehicle;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public HelicopterVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public HelicopterVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
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

        if (this.getDriver() != null && this.rotorSpeed < 20 && this.canAccelerate()) {
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
        if (this.movement.space && this.rotorSpeed > 15 && this.canAccelerate()) {
            this.velY += 0.01;
        } else {
            // Not pressing space and the vehicle is flying
            this.velY -= 0.01;
        }

        if (this.health <= 0) {
            this.velY -= 0.05;
            if (this.rotorSpeed < 10) this.rotorSpeed = 10; // Ensure it keeps ticking
            this.location.setYaw(this.location.getYaw() + 5);
            float pitch = this.location.getPitch() + 5;
            if (pitch > 30) pitch = 30;
            this.location.setPitch(pitch);
            if (this.isOnGround()) this.explode(null);
        }

        if (this.velY >  0.5) this.velY =  0.5;
        if (this.velY < -0.5) this.velY = -0.5;

        this.calculateGravity();

        // Rotation
        if (this.getDriver() != null && this.isFlying() && this.health > 0) {
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
        if (Math.abs(this.velX) < 0.0001) this.velX = 0;
        if (Math.abs(this.velY) < 0.0001) this.velY = 0;
        if (Math.abs(this.velZ) < 0.0001) this.velZ = 0;
        this.speed *= 0.95;
    }

    @Override
    public void calculateGravity() {
        // no gravity :)
        // this.velY -= 0.0001;
    }

    @Override
    public boolean isMoving() {
        return this.rotorSpeed != 0;
    }

    @Override
    public void onZeroHealth(@Nullable Entity source) {
        // Only explode instantly if the conditions which would cause
        // calculateVelocity to not fire (not moving), which would mean
        // that the crashing logic in calculateVelocity would never be
        // called.
        // Otherwise, don't explode now and let the calculateVelocity
        // method handle the crash.

        if (!this.isMoving()) this.explode(source);
    }
}
