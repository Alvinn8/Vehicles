package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.PlaneVehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WoodenPlaneVehicle extends PlaneVehicle {
    private static final RelativePos SMOKE_OFFSET = new RelativePos(0, 1.35, 0.75);

    public WoodenPlaneVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public WoodenPlaneVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    protected void addParts() {
        this.mainPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/wooden_plane"), RelativePos.ZERO, false);
    }

    @Override
    protected void init() {
        super.init();

        this.setMaxFuel(30000);
        this.setFuelUsage(2);
    }

    @Override
    public float getMinTakeoffSpeed() {
        return 9;
    }

    @Override
    public @NotNull VehicleType getType() {
        return VehicleTypes.WOODEN_PLANE;
    }

    @Override
    public float getAccelerationSpeed() {
        return 0.15F;
    }

    @Override
    public float getMaxSpeed() {
        return 20;
    }

    @Override
    public void spawnParticles() {
        if (this.health < 40) {
            this.smokeAt(SMOKE_OFFSET);
        }
    }
}
