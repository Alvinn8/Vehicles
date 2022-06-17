package me.alvin.vehicles.util.ni;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.nms.NMS;
import me.alvin.vehicles.util.DebugUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Non Interpolating (NI) Armor Stand entity.
 * Consists of an AreaEffectCloud and an armor stand
 */
public class NIArmorStand {
    private final AreaEffectCloud aec;
    private final ArmorStand armorStand;

    public NIArmorStand(Location location) {
        World world = location.getWorld();
        if (world == null) throw new IllegalArgumentException("The specified location has to have a world");
        this.aec = NIE.spawnAEC(location);
        this.armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        this.aec.addPassenger(this.armorStand);
    }

    /**
     * Convert the specified armor stand into a non interpolating one.
     *
     * @param armorStand The armor stand to convert
     */
    public NIArmorStand(ArmorStand armorStand) {
        this.aec = NIE.spawnAEC(armorStand.getLocation());
        this.armorStand = armorStand;
        this.aec.addPassenger(this.armorStand);
        DebugUtil.debug("Created new NIArmorStand!");
    }

    public AreaEffectCloud getAreaEffectCloud() {
        return this.aec;
    }

    public ArmorStand getArmorStand() {
        return this.armorStand;
    }

    @Deprecated
    public void setLocation(double x, double y, double z, float yaw, float pitch) {
        NMS nms = SVCraftVehicles.getInstance().getNMS();
        nms.setEntityLocation(this.aec, x, y - NIE.AEC_Y_OFFSET, z, yaw, pitch);
        nms.setEntityRotation(this.armorStand, yaw);
        nms.markDirty(this.aec);
    }

    public void setLocation(Location location) {
        this.aec.teleport(location.clone().subtract(0, NIE.AEC_Y_OFFSET, 0), true, false);
        this.armorStand.setRotation(location.getYaw(), location.getPitch());
        SVCraftVehicles.getInstance().getNMS().markDirty(this.aec);
    }

    public boolean isValid() {
        return this.aec.isValid() && this.armorStand.isValid();
    }

    public void remove() {
        this.armorStand.remove();
        this.aec.remove();
    }

    /**
     * Convert this non interpolating armor stand into a regular armor stand.
     * This will invalidate this entity making {@link #isValid()} return false.
     *
     * @return The regular armor stand. This is the same one as {@link #getArmorStand()}
     */
    public ArmorStand toArmorStand() {
        this.armorStand.leaveVehicle();
        Location location = this.aec.getLocation();
        location.add(0, NIE.AEC_Y_OFFSET, 0);
        this.armorStand.teleport(location, true, false);
        this.aec.remove();
        return this.armorStand;
    }

    /**
     * Set the location of the {@code niEntity} if it exist, otherwise
     * set it for the {@code entity}
     */
    @Deprecated
    public static void setLocation(@Nullable NIArmorStand niEntity, @NotNull ArmorStand entity, double x, double y, double z, float yaw, float pitch) {
        if (niEntity != null) {
            niEntity.setLocation(x, y, z, yaw, pitch);
        } else {
            SVCraftVehicles.getInstance().getNMS().setEntityLocation(entity, x, y, z, yaw, pitch);
        }
    }

    public static void setLocation(@Nullable NIArmorStand niEntity, @NotNull ArmorStand entity, Location location) {
        if (niEntity != null) {
            niEntity.setLocation(location);
        } else {
            entity.teleport(location, true, false);
        }
    }
}
