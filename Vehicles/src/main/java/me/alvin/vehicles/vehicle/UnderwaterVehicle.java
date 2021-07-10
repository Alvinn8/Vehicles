package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.vehicle.text.TemporaryMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A vehicle that can submerge under the water.
 */
public abstract class UnderwaterVehicle extends Vehicle {
    protected boolean inWater = false;

    public UnderwaterVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public UnderwaterVehicle(@NotNull Location location, @Nullable Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    protected void init() {
        super.init();

        this.slime.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 1000000, 0, false, false));
    }

    @Override
    public void calculateVelocity() {
        // Rotation
        if (this.getDriver() != null) {
            Location driverLocation = this.getDriver().getLocation();
            // Yaw
            float vehicleYaw = this.location.getYaw();
            float driverYaw  = driverLocation.getYaw();
            this.location.setYaw(interpolatedRotation(vehicleYaw, driverYaw));

            // Pitch
            float vehiclePitch = this.location.getPitch();
            float driverPitch  = driverLocation.getPitch();
            this.location.setPitch(interpolatedRotation(vehiclePitch, driverPitch));
        }

        // Gravity
        if (!this.inWater) this.calculateGravity();

        // Movement
        this.velX *= 0.95;
        this.velY *= 0.95;
        this.velZ *= 0.95;

        Vector direction = this.location.getDirection();
        direction.multiply(this.speed / 20);
        direction.multiply(0.05);

        this.velX += direction.getX();
        this.velY += direction.getY();
        this.velZ += direction.getZ();

        // Drag
        if (this.inWater) {
            this.speed *= 0.98;
        } else {
            this.speed *= 0.8;
        }

        // Prepare for collision checks
        this.inWater = false;
    }

    @Override
    public void updateRenderedLocation() {
        super.updateRenderedLocation();

        this.entity.setHeadPose(new EulerAngle(Math.toRadians(this.location.getPitch()), 0, /*roll*/0));
    }

    @Override
    protected boolean doCollisionBlockCheck(double x, double y, double z, Axis axis) {
        Block block = this.location.getWorld().getBlockAt(Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z));
        if (!this.inWater) {
            // Look for water
            boolean inWater = block.getType() == Material.WATER;
            if (!inWater) {
                // Check water logged too (but only if we didn't already find water)
                BlockData blockData = block.getBlockData();
                inWater = blockData instanceof Waterlogged && ((Waterlogged) blockData).isWaterlogged();
            }

            // We found water! Change the inWater field
            if (inWater) this.inWater = true;
        }

        if (!block.isPassable()) {
            if (this.highestCollisionBlock == null || block.getY() > this.highestCollisionBlock.getY()) this.highestCollisionBlock = block;
            return true;
        }
        return false;
    }
}
