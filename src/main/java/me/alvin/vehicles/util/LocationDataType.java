package me.alvin.vehicles.util;

import me.alvin.vehicles.SVCraftVehicles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class LocationDataType implements PersistentDataType<PersistentDataContainer, Location> {
    private static final NamespacedKey X = new NamespacedKey(SVCraftVehicles.getInstance(), "x");
    private static final NamespacedKey Y = new NamespacedKey(SVCraftVehicles.getInstance(), "y");
    private static final NamespacedKey Z = new NamespacedKey(SVCraftVehicles.getInstance(), "z");
    private static final NamespacedKey YAW = new NamespacedKey(SVCraftVehicles.getInstance(), "yaw");
    private static final NamespacedKey PITCH = new NamespacedKey(SVCraftVehicles.getInstance(), "pitch");
    private static final NamespacedKey WORLD = new NamespacedKey(SVCraftVehicles.getInstance(), "world");

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public @NotNull Class<Location> getComplexType() {
        return Location.class;
    }

    @NotNull
    @Override
    public PersistentDataContainer toPrimitive(@NotNull Location complex, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();
        container.set(X, PersistentDataType.DOUBLE, complex.getX());
        container.set(Y, PersistentDataType.DOUBLE, complex.getY());
        container.set(Z, PersistentDataType.DOUBLE, complex.getZ());
        container.set(YAW, PersistentDataType.FLOAT, complex.getYaw());
        container.set(PITCH, PersistentDataType.FLOAT, complex.getPitch());
        if (complex.getWorld() != null) {
            container.set(WORLD, PersistentDataType.STRING, complex.getWorld().getName());
        }
        return container;
    }

    @NotNull
    @Override
    public Location fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
        Double x = primitive.get(X, PersistentDataType.DOUBLE);
        Double y = primitive.get(Y, PersistentDataType.DOUBLE);
        Double z = primitive.get(Z, PersistentDataType.DOUBLE);
        Float yaw = primitive.get(YAW, PersistentDataType.FLOAT);
        Float pitch = primitive.get(PITCH, PersistentDataType.FLOAT);
        String worldName = primitive.get(WORLD, PersistentDataType.STRING);
        World world = worldName == null ? null : Bukkit.getWorld(worldName);
        if (x == null || y == null || z == null || yaw == null || pitch == null) throw new RuntimeException("Invalid location when loading location using LocationDataType");
        return new Location(world, x, y, z, yaw, pitch);
    }
}
