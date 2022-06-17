package me.alvin.vehicles.nms;

import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.util.Consumer;

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
    @Deprecated
    void setEntityLocation(Entity entity, double x, double y, double z, float yaw, float pitch);

    /**
     * Set the yaw of an entity.
     *
     * @param entity The entity to change the yaw for
     * @param yaw The yaw to change to
     */
    @Deprecated
    void setEntityRotation(Entity entity, float yaw);

    /**
     * Mark the entity as "dirty", this tells the server it needs to resend information to the
     * clients.
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

    /**
     * Spawn a seat entity mule.
     *
     * <p>This mule will not stand up in contrast to normal mules, this creates a
     * weird effect where the seat flys backwards. This is avoided with this custom
     * entity.</p>
     *
     * <p>This mule will also allow passengers even though they are under water. This
     * is needed for vehicles to be driveable under water which is required for some
     * under water vehicles.</p>
     *
     * <p>This entity should be marked as {@link Entity#setPersistent(boolean)} {@code false}
     * as this is simply a subclass of the normal mule, not an actual new entity. Meaning
     * it won't get saved into the world, and when the chunk is loaded it will become a
     * normal mule instead as it does not have it's own registered entity id.</p>
     *
     * @param location The location to spawn the seat entity at.
     * @param consumer A consumer that is ran before the entity is spawned.
     * @return The spawned entity.
     */
    Mule spawnSeatEntity(Location location, Consumer<Mule> consumer);

    /**
     * Spawn a hitbox slime.
     *
     * <p>Iron Golems and other entities capable of attacking mobs will not attack
     * this slime in contrast to normal slimes. This prevents for example Iron
     * Golems from attacking and blowing up vehicles in villages.</p>
     *
     * <p>This entity should be marked as {@link Entity#setPersistent(boolean)} {@code false}
     * as this is simply a subclass of the normal slime, not an actual new entity. Meaning
     * it won't get saved into the world, and when the chunk is loaded it will become a
     * normal slime instead as it does not have it's own registered entity id.</p>
     *
     * @param location The location to spawn the hitbox entity at.
     * @param consumer A consumer that is ran before the entity is spawned.
     * @return The spawned entity.
     */
    Slime spawnHitboxEntity(Location location, Consumer<Slime> consumer);

    /**
     * Set the player as a passenger on the specified entity, but it's only visible
     * for the player being set as a passenger.
     *
     * @param passenger The player to make a passenger.
     * @param vehicle The entity to put the player on.
     */
    void setClientSidePassenger(Player passenger, Entity vehicle);
}
