package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.actions.StorageAction;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.util.ni.NIArmorStand;
import me.alvin.vehicles.util.ni.NIE;
import me.alvin.vehicles.vehicle.AttachmentData;
import me.alvin.vehicles.vehicle.GroundVehicle;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TruckVehicle extends GroundVehicle {
    private static final RelativePos SMOKE_OFFSET = new RelativePos(0, 2.75, 2.5);

    public static final RelativePos MAIN_PART_OFFSET = new RelativePos(0, 1.6, 1);
    public static final RelativePos BACK_PART_OFFSET = new RelativePos(-0.15, 0.1, -6.2);

    protected @NotNull ArmorStand backEntity;
    protected @Nullable NIArmorStand backNiEntity;

    public TruckVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public TruckVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);

    }

    @Override
    protected void init() {
        super.init();

        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/truck/truck"));

        this.backEntity = spawnArmorStand(BACK_PART_OFFSET.relativeTo(this.location, this.getRoll()));
        this.backEntity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/truck/truck_back"));
        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.backEntity, this);

        this.setMaxFuel(20000);
        this.setFuelUsage(5);

        // Actions
        this.addAction(new StorageAction(54));
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
    public void becomeHologramImpl() {
        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/truck/truck_hologram"));
        this.backEntity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/truck/truck_back_hologram"));
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

    @Override
    public void calculateVelocity() {
        super.calculateVelocity();

        if (this.speed < 0 && Bukkit.getCurrentTick() % 20 == 0 && !this.hasAttachedVehicles()) {
            List<Entity> nearbyEntities = this.backEntity.getNearbyEntities(10, 5, 10);
            for (Entity nearbyEntity : nearbyEntities) {
                Vehicle vehicle = SVCraftVehicles.getInstance().getVehiclePartMap().get(nearbyEntity);
                if (vehicle == null) continue;
                if (vehicle == this) continue;
                if (vehicle.isAttached()) continue;

                this.attachVehicle(vehicle, new AttachmentData(new RelativePos(-0.15, 1.6, -4.5)));
                break;
            }
        }
    }

    @Override
    public void spawnParticles() {
        if (this.health < 100) {
            this.smokeAt(SMOKE_OFFSET);
        }
    }
}
