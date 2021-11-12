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
    private static final RelativePos SMOKE_OFFSET = new RelativePos(0, 1, 1.5);
    private static final RelativePos PARTICLE_OFFSET = new RelativePos(0, 0, -2);

    public SimpleBoatVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public SimpleBoatVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    protected void init() {
        super.init();

        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/boat"));

        this.setMaxFuel(10000);
        this.setFuelUsage(1);
    }

    @Override
    public void becomeHologramImpl() {
        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/boat_hologram"));
    }

    @NotNull
    @Override
    public VehicleType getType() {
        return VehicleTypes.SIMPLE_BOAT;
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
        if (this.health < 50) {
            this.smokeAt(SMOKE_OFFSET);
        }
    }

    @Override
    public boolean canBeColored() {
        return true;
    }
}
