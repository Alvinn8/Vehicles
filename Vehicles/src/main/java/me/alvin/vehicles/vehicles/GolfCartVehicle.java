package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.GroundVehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GolfCartVehicle extends GroundVehicle {
    private static final RelativePos SMOKE_OFFSET = new RelativePos(0.4, 1.2, 1.5);

    public GolfCartVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public GolfCartVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    protected void init() {
        super.init();

        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/golf_cart"));
    }

    @Override
    public void becomeHologramImpl() {
        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/golf_cart_hologram"));
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

    @Override
    public void spawnParticles() {
        if (this.health < 50) {
            this.smokeAt(SMOKE_OFFSET);
        }
    }
}
