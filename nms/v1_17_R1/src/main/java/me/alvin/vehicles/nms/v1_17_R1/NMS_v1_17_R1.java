package me.alvin.vehicles.nms.v1_17_R1;

import com.comphenix.protocol.events.PacketEvent;
import io.netty.buffer.Unpooled;
import me.alvin.vehicles.nms.NMS;
import me.alvin.vehicles.nms.VehicleSteeringMovement;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayInSteerVehicle;
import net.minecraft.network.protocol.game.PacketPlayOutMount;
import net.minecraft.server.level.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Consumer;

public class NMS_v1_17_R1 implements NMS {
    public String getVersion() {
        return "v1_17_R1";
    }

    public void setEntityLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        ((CraftEntity) entity).getHandle().setLocation(x, y, z, yaw, pitch);
    }

    public void setEntityRotation(Entity entity, float yaw) {
        net.minecraft.world.entity.Entity handle = ((CraftEntity) entity).getHandle();
        handle.ay = yaw;
        handle.setHeadRotation(yaw);
    }

    public void markDirty(Entity entity) {
        ((CraftEntity) entity).getHandle().af = true;
    }

    public void handlePacket(VehicleSteeringMovement movement, PacketEvent event) {
        PacketPlayInSteerVehicle packet = (PacketPlayInSteerVehicle) event.getPacket().getHandle();
        movement.forward = packet.c();
        movement.side = packet.b();
        movement.space = packet.d();
        movement.shift = packet.e();
    }

    @Override
    public Mule spawnSeatEntity(Location location, Consumer<Mule> consumer) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        SeatEntity entity = new SeatEntity(world);
        entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        entity.setHeadRotation(location.getYaw());

        return ((CraftWorld) location.getWorld()).addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM, consumer, false);
    }

    @Override
    public Slime spawnHitboxEntity(Location location, Consumer<Slime> consumer) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        HitboxEntity entity = new HitboxEntity(world);
        entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        entity.setHeadRotation(location.getYaw());

        return ((CraftWorld) location.getWorld()).addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM, consumer, false);
    }

    @Override
    public void setClientSidePassenger(Player passenger, Entity vehicle) {
        // The packet has no constructor that allows us to directly pass
        // values, we therefore create a serialized data that we can then
        // pass to the constructor which deserializes it.

        PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
        // The vehicle has this entity id
        data.d(vehicle.getEntityId());
        // The length of the following array of entity ids is 1
        data.d(1);
        // The entity to add as a passenger has this entity id
        data.d(passenger.getEntityId());

        // Create a packet from the data
        PacketPlayOutMount packet = new PacketPlayOutMount(data);

        // Send the packet
        ((CraftPlayer) passenger).getHandle().b.sendPacket(packet);
    }
}
