package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.actions.TestArrowAction;
import me.alvin.vehicles.vehicle.GroundVehicle;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class TestVehicle extends GroundVehicle {
    public TestVehicle(ArmorStand entity) {
        super(entity);
    }

    public TestVehicle(Location location, Player creator) {
        super(location, creator);

        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/golf_cart"));

        this.setMaxFuel(10000);
        this.setFuelUsage(1);
    }

    @Override
    protected void setupActions() {
        super.setupActions();
        this.addAction(new TestArrowAction());
    }

    @Override
    public VehicleType getType() {
        return VehicleTypes.TEST_VEHICLE;
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
