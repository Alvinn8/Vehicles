package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.actions.StorageAction;
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

public class SimpleCarVehicle extends GroundVehicle {
    private static final RelativePos SMOKE_OFFSET = new RelativePos(0, 1.4, 2);

    public SimpleCarVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public SimpleCarVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    protected void addParts() {
        this.mainPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/car"), RelativePos.ZERO, false);
    }

    @Override
    protected void init() {
        super.init();

        this.addAction(new StorageAction(9));

        this.setMaxFuel(40000);
        this.setFuelUsage(2);
    }

    @Override
    public @NotNull VehicleType getType() {
        return VehicleTypes.SIMPLE_CAR;
    }

    @Override
    public float getAccelerationSpeed() {
        return 1.5F;
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
    public void spawnParticles() {
        if (this.health < 75) {
            this.smokeAt(SMOKE_OFFSET);
        }
    }
}
