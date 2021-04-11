package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.actions.StorageAction;
import me.alvin.vehicles.actions.TestArrowAction;
import me.alvin.vehicles.vehicle.GroundVehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TestVehicle extends GroundVehicle {
    public TestVehicle(ArmorStand entity) {
        super(entity);
    }

    public TestVehicle(Location location, Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);

        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/golf_cart"));
    }

    @Override
    protected void postInit() {
        super.postInit();

        this.setMaxFuel(10000);
        this.setFuelUsage(1);

        // Actions
        this.addAction(new TestArrowAction());
        this.addAction(new StorageAction(18));
    }

    @Override
    public void becomeHologramImpl() {
        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/golf_cart_hologram"));
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
