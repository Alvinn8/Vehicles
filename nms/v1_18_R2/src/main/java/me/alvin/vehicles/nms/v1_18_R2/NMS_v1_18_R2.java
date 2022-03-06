package me.alvin.vehicles.nms.v1_18_R2;

import com.comphenix.protocol.events.PacketEvent;
import io.netty.buffer.Unpooled;
import me.alvin.vehicles.nms.NMS;
import me.alvin.vehicles.nms.VehicleSteeringMovement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Consumer;

public class NMS_v1_18_R2 implements NMS {
    public String getVersion() {
        return "v1_18_R2";
    }

    public void setEntityLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        ((CraftEntity) entity).getHandle().absMoveTo(x, y, z, yaw, pitch);
    }

    public void setEntityRotation(Entity entity, float yaw) {
        net.minecraft.world.entity.Entity handle = ((CraftEntity) entity).getHandle();
        handle.yRot = yaw;
        handle.setYHeadRot(yaw);
    }

    public void markDirty(Entity entity) {
        ((CraftEntity) entity).getHandle().hasImpulse = true;
    }

    public void handlePacket(VehicleSteeringMovement movement, PacketEvent event) {
        ServerboundPlayerInputPacket packet = (ServerboundPlayerInputPacket) event.getPacket().getHandle();
        movement.forward = packet.getZza();
        movement.side = packet.getXxa();
        movement.space = packet.isJumping();
        movement.shift = packet.isShiftKeyDown();
    }

    @Override
    public Mule spawnSeatEntity(Location location, Consumer<Mule> consumer) {
        ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();

        SeatEntity entity = new SeatEntity(world);
        entity.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        entity.setYHeadRot(location.getYaw());

        return ((CraftWorld) location.getWorld()).addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM, consumer, false);
    }

    @Override
    public Slime spawnHitboxEntity(Location location, Consumer<Slime> consumer) {
        ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();

        HitboxEntity entity = new HitboxEntity(world);
        entity.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        entity.setYHeadRot(location.getYaw());

        return ((CraftWorld) location.getWorld()).addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM, consumer, false);
    }

    @Override
    public void setClientSidePassenger(Player passenger, Entity vehicle) {
        // The packet has no constructor that allows us to directly pass
        // values, we therefore create a serialized data that we can then
        // pass to the constructor which deserializes it.

        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        // The vehicle has this entity id
        data.writeVarInt(vehicle.getEntityId());
        // The length of the following array of entity ids is 1
        data.writeVarInt(1);
        // The entity to add as a passenger has this entity id
        data.writeVarInt(passenger.getEntityId());

        // Create a packet from the data
        ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(data);

        // Send the packet
        ((CraftPlayer) passenger).getHandle().connection.send(packet);
    }
}
