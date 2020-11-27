package me.alvin.vehicles.util;

import me.alvin.vehicles.SVCraftVehicles;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EntityDataType implements PersistentDataType<PersistentDataContainer, Entity> {
    private static final NamespacedKey UUID = new NamespacedKey(SVCraftVehicles.getInstance(), "uuid");
    private static final NamespacedKey LOCATION = new NamespacedKey(SVCraftVehicles.getInstance(), "location");

    @NotNull
    @Override
    public Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @NotNull
    @Override
    public Class<Entity> getComplexType() {
        return Entity.class;
    }

    @NotNull
    @Override
    public PersistentDataContainer toPrimitive(@NotNull Entity complex, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();
        container.set(UUID, ExtraPersistentDataTypes.UUID, complex.getUniqueId());
        container.set(LOCATION, ExtraPersistentDataTypes.LOCATION, complex.getLocation());
        return container;
    }

    @NotNull
    @Override
    public Entity fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
        UUID uuid = primitive.get(UUID, ExtraPersistentDataTypes.UUID);
        if (uuid == null) throw new RuntimeException("Invalid entity when loading entity using EntityDataType");
        Location location = primitive.get(LOCATION, ExtraPersistentDataTypes.LOCATION);
        if (location == null) throw new RuntimeException("Invalid entity when loading entity using EntityDataType");
        for (Entity entity : location.getChunk().getEntities()) {
            if (uuid.equals(entity.getUniqueId())) return entity;
        }
        throw new RuntimeException("Unable to find entity when loading entity using EntityDataType");
    }
}
