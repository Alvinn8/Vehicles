package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.util.ni.NIE;
import me.alvin.vehicles.vehicle.HelicopterVehicle;
import me.alvin.vehicles.vehicle.VehiclePart;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// TODO: Make the helicopter offset the main entity instead of the
//       model being offset backwards.

public class SimpleHelicopterVehicle extends HelicopterVehicle {
    private static final RelativePos TAIL_SMOKE_OFFSET = new RelativePos(-0.8, 1.7, -6.5);
    private static final RelativePos TAIL_OFFSET = new RelativePos(-0.13, 0.6, -4);
    private static final RelativePos ROTOR_OFFSET = new RelativePos(-0.15, 2.275, -1.65);

    private VehiclePart rotorPart;

    public SimpleHelicopterVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public SimpleHelicopterVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    protected void init() {
        super.init();

        this.setMaxFuel(40000);
        this.setFuelUsage(5);
    }

    @Override
    protected void addParts() {
        this.mainPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/helicopter/helicopter_front"), RelativePos.ZERO, false);
        this.addPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/helicopter/helicopter_tail"), TAIL_OFFSET, false);
        this.rotorPart = this.addPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/helicopter/helicopter_rotor"), ROTOR_OFFSET, false);
    }

    @NotNull
    @Override
    public VehicleType getType() {
        return VehicleTypes.SIMPLE_HELICOPTER;
    }

    @Override
    public float getAccelerationSpeed() {
        return 0.5F;
    }

    @Override
    public float getMaxSpeed() {
        return 30;
    }


    @Override
    public void calculateVelocity() {
        super.calculateVelocity();

        /*
        if (this.health > 0) {
            float desiredPitch = this.speed * 2;

            this.location.setPitch(interpolatedRotation(this.location.getPitch(), desiredPitch));
        }
        */
    }

    @Override
    public void updateRenderedLocation() {
        super.updateRenderedLocation();

        Location rotorLocation = ROTOR_OFFSET.relativeTo(this.location, this.getRoll());
        NIE.setLocation(this.rotorPart.getNiEntity(), this.rotorPart.getEntity(), rotorLocation.getX(), rotorLocation.getY(), rotorLocation.getZ(), this.rotorRotation, 0);

        // EulerAngle propellerAngles = new EulerAngle(0, Math.toRadians(this.rotorRotation), 0);
        // this.rotorPart.getEntity().setHeadPose(propellerAngles);
    }

    @Override
    public boolean canBeColored() {
        return true;
    }

    @Override
    public void spawnParticles() {
        if (this.health < 75) {
            this.smokeAt(TAIL_SMOKE_OFFSET);
            this.smokeAt(ROTOR_OFFSET);
        }
    }
}
