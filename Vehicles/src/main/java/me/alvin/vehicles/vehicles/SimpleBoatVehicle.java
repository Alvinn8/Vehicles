package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.BoatVehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SimpleBoatVehicle extends BoatVehicle {
    public static final RelativePos PARTICLE_OFFSET = new RelativePos(0, 0, -2);

    public SimpleBoatVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public SimpleBoatVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);

        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/boat"));
    }

    @Override
    protected void postInit() {
        super.postInit();

        this.setMaxFuel(10000);
        this.setFuelUsage(1);
    }

    @NotNull
    @Override
    public VehicleType getType() {
        return VehicleTypes.SIMPLE;
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
    public void spawnParticles() {
        if (this.inWater) {
            this.location.getWorld().spawnParticle(Particle.CLOUD, PARTICLE_OFFSET.relativeTo(this.location, this.getRoll()), 5, 0.5, 0.5, 0.5, 0);
        }
    }

    @Override
    public boolean canBeColored() {
        return true;
    }
}
