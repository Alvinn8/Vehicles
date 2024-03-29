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
 * Consists of an AreaEffectCloud and an entity of the type in the generic type.
 * <p>
 * The client does not do interpolation with area effect clouds, meaning
 * teleportations happen instantly which is required for vehicles with multiple
 * parts (seats, and some vehicles have more than 1 armor stand to render the
 * model).
 * <p>
 * This class abstracts the area effect cloud stuff away so all that you need to
 * do is construct a new instance of this class and the entity will be converted
 * to a non interpolating one.
 *
 * @param <T> The EntityType class of the entity to make non interpolating
 */
public class NIE<T extends Entity> {
    private final AreaEffectCloud aec;
    private final T entity;

    public static final double AEC_Y_OFFSET = 0.515;

    public NIE(Location location, Class<T> entityType) {
        World world = location.getWorld();
        if (world == null) throw new IllegalArgumentException("The specified location has to have a world");

        this.aec = spawnAEC(location);
        this.entity = world.spawn(location, entityType);
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
     * Utility method to spawn an Area Effect Cloud.
     *
     * @param location The location to spawn the entity at
     * @return The newly created Area Effect Cloud set up and ready for working as a NIE base
     */
    public static AreaEffectCloud spawnAEC(Location location) {
        return location.getWorld().spawn(location.clone().subtract(0, AEC_Y_OFFSET, 0), AreaEffectCloud.class, aec -> {
            aec.setRadius(0);
            aec.setDuration(-1);
            aec.setWaitTime(Integer.MIN_VALUE);
            aec.setPersistent(false);
        });
    }

    public AreaEffectCloud getAreaEffectCloud() {
        return this.aec;
    }

    public T getEntity() {
        return this.entity;
    }

    public void setLocation(double x, double y, double z, float yaw, float pitch) {
        NMS nms = SVCraftVehicles.getInstance().getNMS();
        nms.setEntityLocation(this.aec, x, y - AEC_Y_OFFSET, z, yaw, pitch);
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
     * Convert this non interpolating entity into a regular entity.
     * This will invalidate this entity making {@link #isValid()} return false.
     *
     * @return The regular entity. This is the same one as {@link #getEntity()}
     */
    public T toNormalEntity() {
        this.entity.leaveVehicle();
        Location location = this.aec.getLocation();
        location.add(0, AEC_Y_OFFSET, 0);
        SVCraftVehicles.getInstance().getNMS().setEntityLocation(this.entity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.aec.remove();
        return this.entity;
    }

    /**
     * Set the location of the {@code niEntity} if it exists, otherwise
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
