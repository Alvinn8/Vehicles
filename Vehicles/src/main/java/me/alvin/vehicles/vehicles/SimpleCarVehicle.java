package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.vehicle.GroundVehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SimpleCarVehicle extends GroundVehicle {
    public SimpleCarVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public SimpleCarVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);

        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/car"));
    }

    @Override
    protected void postInit() {
        super.postInit();

        this.setMaxFuel(20000);
        this.setFuelUsage(2);
    }

    @Override
    public @NotNull VehicleType getType() {
        return VehicleTypes.SIMPLE_CAR_VEHICLE;
    }

    @Override
    public float getAccelerationSpeed() {
        return 1.0F;
    }

    @Override
    public float getMaxSpeed() {
        return 30;
    }

    @Override
    public boolean canBeColored() {
        return true;
    }
}