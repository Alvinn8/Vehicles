package me.alvin.vehicles.util.ni;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.nms.NMS;
import me.alvin.vehicles.util.DebugUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

/**
 * A Non Interpolating (NI) Armor Stand entity.
 * Consists of an AreaEffectCloud
 */
public class NIArmorStand {
    private final AreaEffectCloud aec;
    private final ArmorStand armorStand;

    public NIArmorStand(Location location) {
        World world = location.getWorld();
        if (world == null) throw new IllegalArgumentException("The specified location has to have a world");
        this.aec = spawnAEC(location);
        this.armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        this.aec.addPassenger(this.armorStand);
    }

    /**
     * Convert the specified armor stand into a non interpolating one.
     *
     * @param armorStand The armor stand to convert
     */
    public NIArmorStand(ArmorStand armorStand) {
        this.aec = spawnAEC(armorStand.getLocation());
        this.armorStand = armorStand;
        this.aec.addPassenger(this.armorStand);
        DebugUtil.debug("Created new NIArmorStand!");
    }

    private static AreaEffectCloud spawnAEC(Location location) {
        AreaEffectCloud aec = (AreaEffectCloud) location.getWorld().spawnEntity(location.clone().subtract(0, 0.5, 0), EntityType.AREA_EFFECT_CLOUD);
        aec.setDuration(-1);
        aec.setWaitTime(Integer.MIN_VALUE);
        aec.setRadius(0);
        return aec;
    }

    public AreaEffectCloud getAreaEffectCloud() {
        return this.aec;
    }

    public ArmorStand getArmorStand() {
        return this.armorStand;
    }

    public void setLocation(double x, double y, double z, float yaw, float pitch) {
        NMS nms = SVCraftVehicles.getInstance().getNMS();
        nms.setEntityLocation(this.aec, x, y - 0.5D, z, yaw, pitch);
        nms.setEntityRotation(this.armorStand, yaw);
        nms.markDirty(this.aec);
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
        location.add(0, 0.5, 0);
        SVCraftVehicles.getInstance().getNMS().setEntityLocation(this.armorStand, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.aec.remove();
        return this.armorStand;
    }
}
