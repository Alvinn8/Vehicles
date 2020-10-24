package me.alvin.vehicles.nms;

import com.comphenix.protocol.events.PacketEvent;
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

    /**
     * Set the yaw of an entity.
     *
     * @param entity The entity to change the yaw for
     * @param yaw The yaw to change to
     */
    void setEntityRotation(Entity entity, float yaw);

    /**
     * Mark the entity as "dirty", this tells the server it needs to resend information to the
     * clients. Internally this changes the "impulse" public variable of the entity to true,
     * causing the EntityTracker to send update packets.
     *
     * @param entity The entity to mark dirty.
     */
    void markDirty(Entity entity);

    /**
     * Handle an incoming steer vehicle packet for the specified {@link VehicleSteeringMovement}
     *
     * @param movement The {@link VehicleSteeringMovement} to apply the steering to
     * @param event The ProtocolLib PacketEvent that contains the packet
     */
    void handlePacket(VehicleSteeringMovement movement, PacketEvent event);

}
