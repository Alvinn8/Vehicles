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
    public @NotNull VehicleType getType() {
        return VehicleTypes.GOLF_CART;
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

    @Override
    public boolean usesFuel() {
        return false;
    }
}
