package me.alvin.vehicles.nms;

import org.bukkit.entity.Entity;

public interface NMS {

    String getVersion();

    /**
     * Set the location of an entity. Similar to {@link Entity#teleport(Location)} but allowing
     * location changes even for vehicles (entities with passengers). Please make sure the entity
     * is in the right world before setting their location.
     *
     * @param entity The entity to set the location for
     * @param x The x location of the entity
     * @param y The y location of the entity
     * @param z The z location of the entity
     * @param yaw The yaw rotation of the entity
     * @param pitch The pitch rotation of the entity
     */
    void setEntityLocation(Entity entity, double x, double y, double z, float yaw, float pitch);

}
