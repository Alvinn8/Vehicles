package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.PlaneVehicle;
import me.alvin.vehicles.vehicle.VehiclePart;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimplePlaneVehicle extends PlaneVehicle {
    public static final RelativePos MAIN_OFFSET = RelativePos.ZERO;
    public static final RelativePos FRONT_OFFSET = MAIN_OFFSET;
    public static final RelativePos BACK_OFFSET = new RelativePos(0, 0.24, -4.5);
    public static final RelativePos LEFT_WING_OFFSET = new RelativePos(2.65, -0.28, -1);
    public static final RelativePos RIGHT_WING_OFFSET = new RelativePos(-2.8, -0.28, -1);
    public static final RelativePos PROPELLER_OFFSET = new RelativePos(0, -0.2, 1.8);

    private VehiclePart propellerPart;
    private double propellerAngle;
    private double propellerSpeed;

    public SimplePlaneVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public SimplePlaneVehicle(@NotNull Location location, @Nullable Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    protected void addParts() {
        this.mainPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/plane/plane_front"), FRONT_OFFSET, true);
        this.addPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/plane/plane_back"), BACK_OFFSET, true);
        this.addPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/plane/plane_left_wing"), LEFT_WING_OFFSET, true);
        this.addPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/plane/plane_right_wing"), RIGHT_WING_OFFSET, true);
        this.propellerPart = this.addPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/plane/plane_propeller"), PROPELLER_OFFSET, false);
    }

    @Override
    protected void init() {
        super.init();

        this.setMaxFuel(20000);
        this.setFuelUsage(5);
    }

    @Override
    public void updateSpeed() {
        super.updateSpeed();

        this.propellerSpeed = Math.min(this.speed * 5, 25);
    }

    @Override
    public void calculateVelocity() {
        super.calculateVelocity();

        this.propellerAngle += this.propellerSpeed;
    }

    @Override
    public void updateRenderedLocation() {
        super.updateRenderedLocation();

        EulerAngle propellerAngles = new EulerAngle(Math.toRadians(90), 0, Math.toRadians(this.propellerAngle));
        this.propellerPart.getEntity().setHeadPose(propellerAngles);
    }

    @Override
    public boolean canBeColored() {
        return true;
    }

    @Override
    public float getMinTakeoffSpeed() {
        return 20;
    }

    @Override
    public @NotNull VehicleType getType() {
        return VehicleTypes.PLANE;
    }

    @Override
    public float getAccelerationSpeed() {
        return 0.3F;
    }

    @Override
    public float getMaxSpeed() {
        return /* 40 */ 100;
    }

    @Override
    public void spawnParticles() {
        // TODO
    }
}
