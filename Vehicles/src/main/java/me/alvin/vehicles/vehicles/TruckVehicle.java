package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.actions.StorageAction;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.util.ni.NIArmorStand;
import me.alvin.vehicles.util.ni.NIE;
import me.alvin.vehicles.vehicle.GroundVehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TruckVehicle extends GroundVehicle {
    public static final RelativePos MAIN_PART_OFFSET = new RelativePos(0, 1.6, 1);
    public static final RelativePos BACK_PART_OFFSET = new RelativePos(-0.15, 0.1, -6.2);

    protected @NotNull ArmorStand backEntity;
    protected @Nullable NIArmorStand backNiEntity;

    public TruckVehicle(@NotNull ArmorStand entity) {
        super(entity);

        this.spawnExtraEntities();
    }

    public TruckVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);

        this.spawnExtraEntities();
        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/truck/truck"));
    }

    private void spawnExtraEntities() {
        this.backEntity = spawnArmorStand(BACK_PART_OFFSET.relativeTo(this.location, this.getRoll()));
        this.backEntity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:vehicle/truck/truck_back"));
        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.backEntity, this);
    }

    private void removeExtraEntities() {
        SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.backEntity);

        if (this.backNiEntity != null) {
            SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.backNiEntity.getAreaEffectCloud());
            this.backNiEntity.remove();
        }
        else this.backEntity.remove();
    }

    @Override
    protected void postInit() {
        super.postInit();

        this.setMaxFuel(20000);
        this.setFuelUsage(5);

        // Actions
        this.addAction(new StorageAction(54));
    }

    @Override
    public @NotNull VehicleType getType() {
        return VehicleTypes.TRUCK;
    }

    @Override
    public float getAccelerationSpeed() {
        return 1F;
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
    public boolean setColor(Color color) {
        if (!super.setColor(color)) return false;
        return this.colorArmorStand(this.backEntity, color);
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
            this.backNiEntity = new NIArmorStand(this.backEntity);
        } else {
            this.backNiEntity.toArmorStand();
            this.backNiEntity = null;
        }
    }

    @Override
    public void updateRenderedLocation() {
        NIE.setLocation(this.niSlime, this.slime, this.location.getX(), this.location.getY(), this.location.getZ(), 0, 0);

        Location mainLocation = MAIN_PART_OFFSET.relativeTo(this.location, this.getRoll());
        NIArmorStand.setLocation(this.niEntity, this.entity, mainLocation.getX(), mainLocation.getY(), mainLocation.getZ(), this.location.getYaw(), this.location.getPitch());

        Location backLocation = BACK_PART_OFFSET.relativeTo(this.location, this.getRoll());
        NIArmorStand.setLocation(this.backNiEntity, this.backEntity, backLocation.getX(), backLocation.getY(), backLocation.getZ(), backLocation.getYaw(), 0);
    }
}
