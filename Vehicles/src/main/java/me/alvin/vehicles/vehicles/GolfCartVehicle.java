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

public class GolfCartVehicle extends GroundVehicle {
    public GolfCartVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public GolfCartVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);

        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/golf_cart"));
    }

    @Override
    protected void postInit() {
        super.postInit();

        this.setMaxFuel(10000);
        this.setFuelUsage(1);
    }

    @Override
    public @NotNull VehicleType getType() {
        return VehicleTypes.GOLF_CART_VEHICLE;
    }

    @Override
    public float getAccelerationSpeed() {
        return 0.75F;
    }

    @Override
    public float getMaxSpeed() {
        return 20;
    }

    @Override
    public boolean canBeColored() {
        return true;
    }
}
