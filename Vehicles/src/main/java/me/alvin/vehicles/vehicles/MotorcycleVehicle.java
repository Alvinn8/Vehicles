package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.util.ni.NIArmorStand;
import me.alvin.vehicles.util.ni.NIE;
import me.alvin.vehicles.vehicle.GroundVehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;

public class MotorcycleVehicle extends GroundVehicle {
    private static final RelativePos SMOKE_OFFSET = new RelativePos(-0.2, 1, -0.1);

    private float roll;
    private float rollChange;

    public MotorcycleVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public MotorcycleVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    protected void init() {
        super.init();

        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/motorcycle"));

        this.setMaxFuel(40000);
        this.setFuelUsage(4);
    }

    @Override
    public void becomeHologramImpl() {
        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/motorcycle_hologram"));
    }

    @Override
    public @NotNull VehicleType getType() {
        return VehicleTypes.MOTORCYCLE;
    }

    @Override
    public float getAccelerationSpeed() {
        return 1.5F;
    }

    @Override
    public float getMaxSpeed() {
        return 35;
    }

    @Override
    public float getRoll() {
        return this.roll * 1.75F;
    }

    @Override
    public boolean canBeColored() {
        return true;
    }

    @Override
    public void updateRenderedLocation() {
        NIArmorStand.setLocation(this.niEntity, this.entity, this.location.getX(), this.location.getY() - 1.5D, this.location.getZ(), this.location.getYaw(), this.location.getPitch());
        if (this.slime != null) NIE.setLocation(this.niSlime, this.slime, this.location.getX(), this.location.getY(), this.location.getZ(), 0, 0);

        if (this.roll != 0) {
            if (Math.abs(this.roll) < 1) this.roll = 0;
            else this.roll *= 0.75;
        }

        // Calculate roll
        if (this.movement.side < 0) {
            this.rollChange--;
        } else if (this.movement.side > 0) {
            this.rollChange++;
        }
        float oldRoll = this.roll;
        this.roll += this.rollChange;
        this.rollChange *= 0.9;
        if (this.roll > 180)  this.roll -= 360;
        if (this.roll < -180) this.roll += 360;
        if (this.roll > 70)   this.roll = 70;
        if (this.roll < -70)  this.roll = -70;

        // Apply roll
        if (oldRoll != this.roll) {
            this.entity.setHeadPose(new EulerAngle(0, 0, Math.toRadians(this.roll)));
        }
    }

    @Override
    public void spawnParticles() {
        if (this.health < 25) {
            this.smokeAt(SMOKE_OFFSET);
        }
    }
}
