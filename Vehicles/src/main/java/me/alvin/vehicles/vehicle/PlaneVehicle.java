package me.alvin.vehicles.vehicle;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

/**
 * A plane that needs a runway to take off and can fly in the air.
 */
public abstract class PlaneVehicle extends Vehicle {
    public static final DecimalFormat SPEED_FORMAT = new DecimalFormat("0.00");

    protected double gravityVelY = 0.0;
    protected float roll = 0.0F;

    public PlaneVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public PlaneVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    /**
     * Get the minimum speed the plane has to have to be able to
     * take off.
     *
     * @return The speed required
     */
    public abstract float getMinTakeoffSpeed();

    /**
     * @return Whether the plane is flying, is always the opposite of {@link #isOnGround()}
     */
    public boolean isFlying() {
        return !this.onGround;
    }

    @Override
    public float getRoll() {
        return this.roll;
    }

    @Override
    public void createText() {
        super.createText();

        this.text.addEntry((vehicle, player) -> {
            float speed = this.getSpeed();
            TextComponent.Builder component = Component.text();
            component.content("Speed: " + SPEED_FORMAT.format(speed));
            if (speed > this.getMinTakeoffSpeed() && this.onGround) {
                component.color(NamedTextColor.GREEN);
            }
            return component.build();
        });
    }

    @Override
    public void updateSpeed() {
        float oldSpeed = this.speed;

        if (this.movement.forward != 0 && this.canAccelerate()) {
            this.speed += this.getAccelerationSpeed() * this.movement.forward;
        }

        if (this.speed > this.getMaxSpeed()) this.speed = this.getMaxSpeed();
        if (this.speed < -this.getMaxSpeed()) this.speed = -this.getMaxSpeed();

        if (this.isFlying()) {
            if (this.speed < this.getMinTakeoffSpeed() && oldSpeed >= this.getMinTakeoffSpeed()) {
                // Speeding down while in air
                this.speed = this.getMinTakeoffSpeed();
            }
        }

        if (Math.abs(this.speed) < 0.01) {
            this.speed = 0;
        }
    }

    @Override
    public void calculateVelocity() {
        boolean canFly = this.speed >= this.getMinTakeoffSpeed();

        // Rotation
        LivingEntity driver = this.getDriver();
        if (driver != null) {
            Location driverLocation = driver.getLocation();
            this.location.setYaw(interpolatedRotation(this.location.getYaw(), driverLocation.getYaw()));
            if (canFly) {
                float driverPitch = driverLocation.getPitch();
                if (!this.canAccelerate()) {
                    driverPitch = Math.min(Math.max(this.location.getPitch(), 0) + 5, 90);
                }
                if (this.onGround && driverPitch > 0) {
                    // Tried to face into the ground
                    driverPitch = 0;
                }
                this.location.setPitch(interpolatedRotation(this.location.getPitch(), driverPitch));
            } else {
                this.location.setPitch(interpolatedRotation(this.location.getPitch(), 0));
            }
            this.roll = interpolatedRotation(this.roll, this.onGround ? 0 : this.location.getYaw() - driverLocation.getYaw());
        }

        // Velocity
        Vector direction = this.location.getDirection();
        direction.multiply(this.speed / 20);
        this.velX = direction.getX();
        this.velY = direction.getY();
        this.velZ = direction.getZ();

        // Gravity
        if (!canFly) {
            // If the plane isn't going fast enough to fly, apply gravity
            this.calculateGravity();
        }
        if (this.onGround) this.gravityVelY = 0;

        // Drag
        if (this.onGround || !canFly || !this.canAccelerate()) {
            // Not flying
            if (this.speed < 5 && this.movement.forward <= 0) {
                this.speed *= 0.95;
            } else {
                this.speed *= 0.99;
            }
        }
    }

    @Override
    public void calculateGravity() {
        this.gravityVelY -= GRAVITY;
        this.velY += this.gravityVelY;
    }

    @Override
    public void updateRenderedLocation() {
        super.updateRenderedLocation();

        this.entity.setHeadPose(new EulerAngle(Math.toRadians(this.location.getPitch()), 0, Math.toRadians(this.roll)));
    }
}
