package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.actions.StorageAction;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.util.ni.NIArmorStand;
import me.alvin.vehicles.vehicle.AttachmentData;
import me.alvin.vehicles.vehicle.GroundVehicle;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TruckVehicle extends GroundVehicle {
    private static final RelativePos SMOKE_OFFSET = new RelativePos(0, 2.75, 2.5);

    public static final RelativePos MAIN_PART_OFFSET = new RelativePos(0, 1.6, 1);
    public static final RelativePos BACK_PART_OFFSET = new RelativePos(-0.15, 0.1, -6.2);

    protected @NotNull ArmorStand backEntity;
    protected @Nullable NIArmorStand backNiEntity;

    public TruckVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public TruckVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);

    }

    @Override
    protected void addParts() {
        this.mainPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/truck/truck"), MAIN_PART_OFFSET, false);
        this.addPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/truck/truck_back"), BACK_PART_OFFSET, false);
    }

    @Override
    protected void init() {
        super.init();

        this.setMaxFuel(40000);
        this.setFuelUsage(5);

        // Actions
        this.addAction(new StorageAction(54));
    }

    @Override
    public @NotNull VehicleType getType() {
        return VehicleTypes.TRUCK;
    }

    @Override
    public float getAccelerationSpeed() {
        return 1F;
    }

    @Override
    public float getMaxSpeed() {
        return 30;
    }

    @Override
    public boolean canBeColored() {
        return true;
    }

    @Override
    public boolean setColor(Color color) {
        if (!super.setColor(color)) return false;
        return colorArmorStand(this.backEntity, color);
    }

    @Override
    public void calculateVelocity() {
        super.calculateVelocity();

        if (this.speed < 0 && Bukkit.getCurrentTick() % 20 == 0 && !this.hasAttachedVehicles()) {
            List<Entity> nearbyEntities = this.backEntity.getNearbyEntities(10, 5, 10);
            for (Entity nearbyEntity : nearbyEntities) {
                Vehicle vehicle = SVCraftVehicles.getInstance().getVehiclePartMap().get(nearbyEntity);
                if (vehicle == null) continue;
                if (vehicle == this) continue;
                if (vehicle.isAttached()) continue;

                this.attachVehicle(vehicle, new AttachmentData(new RelativePos(-0.15, 1.6, -4.5)));
                break;
            }
        }
    }

    @Override
    public void spawnParticles() {
        if (this.health < 100) {
            this.smokeAt(SMOKE_OFFSET);
        }
    }
}
