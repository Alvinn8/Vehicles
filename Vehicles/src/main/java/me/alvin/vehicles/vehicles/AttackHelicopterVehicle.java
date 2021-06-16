package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.actions.MissileAction;
import me.alvin.vehicles.actions.TestArrowAction;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.util.ni.NIArmorStand;
import me.alvin.vehicles.util.ni.NIE;
import me.alvin.vehicles.vehicle.HelicopterVehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttackHelicopterVehicle extends HelicopterVehicle {
    public static final RelativePos TAIL_OFFSET = new RelativePos(-0.13, 0.6, -4);
    public static final RelativePos ROTOR_OFFSET = new RelativePos(-0.15, 2.275, -1.65);

    protected @NotNull ArmorStand tailEntity;
    protected @Nullable NIArmorStand tailNiEntity;
    protected @NotNull ArmorStand rotorEntity;
    protected @Nullable NIArmorStand rotorNiEntity;

    public AttackHelicopterVehicle(@NotNull ArmorStand entity) {
        super(entity);

        this.spawnExtraEntities();
    }

    public AttackHelicopterVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);

        this.spawnExtraEntities();
        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/attack_helicopter/attack_helicopter_front"));
    }

    private void spawnExtraEntities() {
        this.tailEntity = spawnArmorStand(TAIL_OFFSET.relativeTo(this.location, this.getRoll()));
        this.tailEntity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/attack_helicopter/attack_helicopter_tail"));
        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.tailEntity, this);
        this.rotorEntity = spawnArmorStand(ROTOR_OFFSET.relativeTo(this.location, this.getRoll()));
        this.rotorEntity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/helicopter/helicopter_rotor"));
        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.rotorEntity, this);
    }

    @Override
    protected void init() {
        super.init();

        this.setMaxFuel(20000);
        this.setFuelUsage(5);

        this.addAction(new TestArrowAction());
        this.addAction(new MissileAction());
    }

    @Override
    public void becomeHologramImpl() {
        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/attack_helicopter/attack_helicopter_front_hologram"));
        this.tailEntity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/attack_helicopter/attack_helicopter_tail_hologram"));
        this.rotorEntity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/helicopter/helicopter_rotor_hologram"));
    }

    @Override
    public @NotNull VehicleType getType() {
        return VehicleTypes.ATTACK_HELICOPTER;
    }

    @Override
    public float getAccelerationSpeed() {
        return 0.5F;
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
    public Color getDefaultColor() {
        return Color.fromRGB(0x5c5b5a);
    }

    @Override
    public boolean setColor(Color color) {
        if (!super.setColor(color)) return false;
        if (!this.colorArmorStand(this.tailEntity, color)) return false;
        return this.colorArmorStand(this.rotorEntity, color);
    }

    @Override
    public void calculateVelocity() {
        super.calculateVelocity();

        float desiredPitch = this.speed * 3;
        if (this.onGround) desiredPitch = -3; // Slight tilt backwards so the back wheel hits the ground

        this.location.setPitch(interpolatedRotation(this.location.getPitch(), desiredPitch));
    }

    @Override
    public void updateRenderedLocation() {
        NIArmorStand.setLocation(this.niEntity, this.entity, this.location.getX(), this.location.getY() + 0.75, this.location.getZ(), this.location.getYaw(), this.location.getPitch());
        this.entity.setHeadPose(new EulerAngle(Math.toRadians(this.location.getPitch()), 0, /*roll*/0));
        NIE.setLocation(this.niSlime, this.slime, this.location.getX(), this.location.getY() + 0.75, this.location.getZ(), 0, 0);

        Location tailLocation = new RelativePos(0, 0.475, -7).relativeTo(this.location, this.getRoll());
        NIArmorStand.setLocation(this.tailNiEntity, this.tailEntity, tailLocation.getX(), tailLocation.getY(), tailLocation.getZ(), tailLocation.getYaw(), 0);
        this.tailEntity.setHeadPose(new EulerAngle(Math.toRadians(this.location.getPitch()), 0, /*roll*/0));

        Location rotorLocation = new RelativePos(0, 2.75, -2.65).relativeTo(this.location, this.getRoll());
        NIArmorStand.setLocation(this.rotorNiEntity, this.rotorEntity, rotorLocation.getX(), rotorLocation.getY(), rotorLocation.getZ(), this.rotorRotation, 0);
    }

    private void removeExtraEntities() {
        SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.tailEntity);
        SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.rotorEntity);

        if (this.tailNiEntity != null) {
            SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.tailNiEntity.getAreaEffectCloud());
            this.tailNiEntity.remove();
        }
        else this.tailEntity.remove();

        if (this.rotorNiEntity != null) {
            SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.rotorNiEntity.getAreaEffectCloud());
            this.rotorNiEntity.remove();
        }
        else this.rotorEntity.remove();
    }

    @Override
    public void unload() {
        super.unload();

        this.removeExtraEntities();
    }

    @Override
    public void remove() {
        super.remove();

        this.removeExtraEntities();
    }

    @Override
    public void setNonInterpolating(boolean nonInterpolating) {
        super.setNonInterpolating(nonInterpolating);

        if (nonInterpolating) {
            this.tailNiEntity = new NIArmorStand(this.tailEntity);
            this.rotorNiEntity = new NIArmorStand(this.rotorEntity);
        } else {
            this.tailNiEntity.toArmorStand();
            this.tailNiEntity = null;
            this.rotorNiEntity.toArmorStand();
            this.rotorNiEntity = null;
        }
    }
}
