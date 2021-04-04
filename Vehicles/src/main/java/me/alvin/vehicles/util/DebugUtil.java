package me.alvin.vehicles.util;

import me.alvin.vehicles.SVCraftVehicles;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.Nullable;

public final class DebugUtil {
    private DebugUtil() {};

    public static void debug(@Nullable String message) {
        if (SVCraftVehicles.getInstance().isInDebugMode()) {
            System.out.println("[SVCraftVehicles debug] "+ message);
        }
    }

    public static void debugVariable(@Nullable String name, @Nullable Object value) {
        DebugUtil.debug(name +" = "+ value);
    }

    public static void debugLocation(@Nullable Location location) {
        if (location == null) {
            DebugUtil.debug("<null location>");
        } else if (location.getWorld() == null) {
            DebugUtil.debug("location: "+ location);
        } else {
            location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 1, 0, 0, 0, 0.0D);
        }
    }
}
