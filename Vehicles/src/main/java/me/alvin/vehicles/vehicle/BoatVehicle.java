package me.alvin.vehicles.vehicle;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public abstract class BoatVehicle extends Vehicle {
    protected boolean inWater = false;

    public BoatVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public BoatVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    public void updateSpeed() {
        if (!this.inWater) {
            if (this.movement.forward != 0 && Math.abs(this.speed) < this.getMaxSpeed() && this.canAccelerate()) {
                this.speed += this.getAccelerationSpeed() * 0.2 * this.movement.forward;
            }

            if (Math.abs(this.speed) < 0.01) {
                this.speed = 0;
            }
            return;
        }
        super.updateSpeed();
    }

    @Override
    public void calculateVelocity() {
        if (this.movement.side != 0) {
            this.location.setYaw(this.location.getYaw() + this.movement.side * -5);
        }
        this.calculateGravity();

        this.velX *= 0.95;
        this.velZ *= 0.95;
        Vector direction = this.location.getDirection();
        direction.multiply(this.speed / 20);
        direction.multiply(0.05);
        this.velX += direction.getX();
        this.velZ += direction.getZ();

        if (this.inWater) {
            this.speed *= 0.95;
        } else {
            this.speed *= 0.8;
        }

        // Prepare for collision checks
        this.inWater = false;
    }

    @Override
    protected boolean doCollisionBlockCheck(double x, double y, double z, Axis axis) {
        Block block = this.location.getWorld().getBlockAt(Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z));
        boolean check;
        if (axis == Axis.Y || axis == null) {
            // On only the Y axis can water be collided with
            // Or if the axis is null its being spawned using the vehicle spawner
            boolean inWater = block.getType() == Material.WATER;
            if (!inWater) {
                // Check water logged too (but only if we didn't already find water)
                BlockData blockData = block.getBlockData();
                inWater = blockData instanceof Waterlogged && ((Waterlogged) blockData).isWaterlogged();
            }

            check = inWater || !block.isPassable();
            if (!this.inWater && inWater) this.inWater = true;
        } else {
            check = !block.isPassable();
        }

        if (check) {
            if (this.highestCollisionBlock == null || block.getY() > this.highestCollisionBlock.getY()) this.highestCollisionBlock = block;
            return true;
        }
        return false;
    }
}
