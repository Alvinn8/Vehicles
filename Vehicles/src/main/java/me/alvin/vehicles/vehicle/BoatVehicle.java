package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.util.RelativePos;
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
        if (false && !this.inWater) {
            this.speed *= 0.8;
            if (Math.abs(this.speed) < 0.01) {
                this.speed = 0;
            }
            return;
        }
        super.updateSpeed();
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

        this.speed *= 0.95;
    }

    @Override
    public void calculateGravity() {
        boolean fall = true;
        for (RelativePos gravityPoint : this.getType().getGravityPoints()) {
            Location location = gravityPoint.relativeTo(this.location);
            location.subtract(0.0D, 0.001D, 0.0D);
            Block block = location.getBlock();
            BlockData blockData = block.getBlockData();
            this.inWater = block.getType() == Material.WATER
                    || (blockData instanceof Waterlogged && ((Waterlogged) blockData).isWaterlogged());
            if (this.inWater || !block.isPassable()) {
                fall = false;
                break;
            }
        }
        if (fall) {
            this.velY -= GRAVITY;
            this.location.add(0, this.velY, 0);
        } else if (this.velY != 0) {
            this.velY = 0;
            this.location.setY(this.location.getBlockY() + 1);
        }
    }
}
