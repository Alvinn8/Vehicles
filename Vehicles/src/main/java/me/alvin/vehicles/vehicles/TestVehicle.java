package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.actions.StorageAction;
import me.alvin.vehicles.actions.TestArrowAction;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.GroundVehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TestVehicle extends GroundVehicle {
    public TestVehicle(ArmorStand entity) {
        super(entity);
    }

    public TestVehicle(Location location, Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    protected void addParts() {
        this.mainPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/golf_cart"), RelativePos.ZERO, false);
    }

    @Override
    protected void init() {
        super.init();

        this.setMaxFuel(10000);
        this.setFuelUsage(1);

        // Actions
        this.addAction(new TestArrowAction());
        this.addAction(new StorageAction(18));
    }

    @NotNull
    @Override
    public VehicleType getType() {
        return VehicleTypes.TEST;
    }

    @Override
    public float getAccelerationSpeed() {
        return 0.75F;
    }

    @Override
    public float getMaxSpeed() {
        return 40;
    }

    @Override
    public boolean canBeColored() {
        return true;
    }
}
