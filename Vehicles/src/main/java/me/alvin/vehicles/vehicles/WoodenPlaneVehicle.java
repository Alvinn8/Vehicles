package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.vehicle.PlaneVehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WoodenPlaneVehicle extends PlaneVehicle {
    public WoodenPlaneVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public WoodenPlaneVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);

        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/wooden_plane"));
    }

    @Override
    protected void init() {
        super.init();

        this.setMaxFuel(10000);
        this.setFuelUsage(2);
    }

    @Override
    public float getMinTakeoffSpeed() {
        return 9;
    }

    @Override
    protected void becomeHologramImpl() {
        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/wooden_plane_hologram"));
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
}
