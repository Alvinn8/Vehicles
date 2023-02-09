package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.util.ni.NIE;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A part of the vehicle that is rendered using an armor stand.
 */
public class VehiclePart {
    private final Vehicle vehicle;
    private final NamespacedKey hologramModel;
    private final RelativePos relativePos;
    private final boolean rotate;
    private final @NotNull ArmorStand entity;
    private @Nullable NIE<ArmorStand> niEntity;

    public VehiclePart(Vehicle vehicle, NamespacedKey model, RelativePos relativePos, boolean rotate, @Nullable ArmorStand entity) {
        this.vehicle = vehicle;
        this.hologramModel = getHologramModel(model);
        this.relativePos = relativePos;
        this.rotate = rotate;
        if (entity != null) {
            this.entity = entity;
        } else {
            Location location = relativePos.relativeTo(vehicle.getLocation(), vehicle.getRoll());
            this.entity = Vehicle.spawnArmorStand(location);
        }
        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem(
            model.toString()
        ));
        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.entity, this.vehicle);
    }

    private static NamespacedKey getHologramModel(NamespacedKey model) {
        return new NamespacedKey(model.getNamespace(), model.getKey() + "_hologram");
    }

    /**
     * Set whether this part should be non interpolating.
     *
     * @param nonInterpolating Whether to be non interpolating.
     */
    public void setNonInterpolating(boolean nonInterpolating) {
        if (nonInterpolating) {
            if (this.niEntity == null) {
                this.niEntity = new NIE<>(this.entity);
                SVCraftVehicles.getInstance().getVehiclePartMap().put(this.niEntity.getAreaEffectCloud(), this.vehicle);
            }
        } else if (this.niEntity != null) {
            this.niEntity.toNormalEntity();
            this.niEntity = null;
        }
    }

    /**
     * Become a hologram.
     */
    public void becomeHologram() {
        this.entity.getEquipment().setHelmet(SVCraftVehicles.getInstance().getModelDB().generateItem(
            this.hologramModel.toString()
        ));
    }

    /**
     * Set the color of this part.
     *
     * @param color The color.
     * @return Whether successful.
     */
    public boolean setColor(Color color) {
        return Vehicle.colorArmorStand(this.entity, color);
    }

    /**
     * Update the rendered location of this part.
     */
    public void updateRenderedLocation() {
        Location location = this.relativePos.relativeTo(this.vehicle.getLocation(), this.vehicle.getRoll());
        NIE.setLocation(this.niEntity, this.entity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        if (this.rotate) {
            this.entity.setHeadPose(new EulerAngle(Math.toRadians(location.getPitch()), 0, Math.toRadians(this.vehicle.getRoll())));
        }
    }

    /**
     * Remove this part.
     */
    public void remove() {
        SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.entity);
        if (this.niEntity != null) {
            SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.niEntity.getAreaEffectCloud());
            this.niEntity.remove();
        } else {
            this.entity.remove();
        }
    }

    /**
     * Get the entity of this part.
     *
     * @return The entity.
     */
    @NotNull
    public ArmorStand getEntity() {
        return this.entity;
    }

    @Nullable
    public NIE<ArmorStand> getNiEntity() {
        return this.niEntity;
    }

    /**
     * Get whether this part is in non-interpolating mode.
     *
     * @return Whether non interpolating.
     */
    public boolean isNonInterpolating() {
        return this.niEntity != null;
    }
}
