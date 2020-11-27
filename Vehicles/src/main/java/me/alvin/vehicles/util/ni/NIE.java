package me.alvin.vehicles.util.ni;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.nms.NMS;
import me.alvin.vehicles.util.DebugUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Non Interpolating Entity (NIE).
 * Consists of an AreaEffectCloud and an entity of the type specified below
 * @param <T> The EntityType class of the entity to make non interpolating
 */
public class NIE<T extends Entity> {
    private final AreaEffectCloud aec;
    private final T entity;

    public NIE(Location location, EntityType entityType) {
        World world = location.getWorld();
        if (world == null) throw new IllegalArgumentException("The specified location has to have a world");

        this.aec = spawnAEC(location);
        this.entity = (T) world.spawnEntity(location, entityType);
        this.aec.addPassenger(this.entity);
    }

    /**
     * Convert the specified entity into a non interpolating one.
     *
     * @param entity The entity to convert
     */
    public NIE(T entity) {
        this.aec = spawnAEC(entity.getLocation());
        this.entity = entity;
        this.aec.addPassenger(this.entity);
        DebugUtil.debug("Created new NIE");
    }

    /**
     * Utility method to spawn an Area Effect Cloud
     * @param location The location to spawn the entity at
     * @return The newly created Area Effect Cloud set up and ready for working as an NIE base
     */
    public static AreaEffectCloud spawnAEC(Location location) {
        AreaEffectCloud aec = (AreaEffectCloud) location.getWorld().spawnEntity(location.clone().subtract(0, 0.5, 0), EntityType.AREA_EFFECT_CLOUD);
        aec.setRadius(0);
        aec.setDuration(-1);
        aec.setWaitTime(Integer.MIN_VALUE);
        return aec;
    }

    public AreaEffectCloud getAreaEffectCloud() {
        return this.aec;
    }

    public T getEntity() {
        return this.entity;
    }

    public void setLocation(double x, double y, double z, float yaw, float pitch) {
        NMS nms = SVCraftVehicles.getInstance().getNMS();
        nms.setEntityLocation(this.aec, x, y - 0.5D, z, yaw, pitch);
        nms.setEntityRotation(this.entity, yaw);
        nms.markDirty(this.aec);
    }

    public boolean isValid() {
        return this.aec.isValid() && this.entity.isValid();
    }

    public void remove() {
        this.entity.remove();
        this.aec.remove();
    }

    /**
     * Convert this non interpolating armor stand into a regular armor stand.
     * This will invalidate this entity making {@link #isValid()} return false.
     *
     * @return The regular armor stand. This is the same one as {@link #getEntity()}
     */
    public T toNormalEntity() {
        this.entity.leaveVehicle();
        Location location = this.aec.getLocation();
        location.add(0, 0.5, 0);
        SVCraftVehicles.getInstance().getNMS().setEntityLocation(this.entity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.aec.remove();
        return this.entity;
    }

    /**
     * Set the location of the {@code niEntity} if it exist, otherwise
     * set it for the {@code entity}
     */
    public static <T extends Entity> void setLocation(@Nullable NIE<T> niEntity, @NotNull T entity, double x, double y, double z, float yaw, float pitch) {
        if (niEntity != null) {
            niEntity.setLocation(x, y, z, yaw, pitch);
        } else {
            SVCraftVehicles.getInstance().getNMS().setEntityLocation(entity, x, y, z, yaw ,pitch);
        }
    }
}
