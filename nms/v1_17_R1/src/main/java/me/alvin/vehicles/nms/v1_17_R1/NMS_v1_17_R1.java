package me.alvin.vehicles.nms.v1_17_R1;

import com.comphenix.protocol.events.PacketEvent;
import me.alvin.vehicles.nms.NMS;
import me.alvin.vehicles.nms.VehicleSteeringMovement;
import net.minecraft.network.protocol.game.PacketPlayInSteerVehicle;
import net.minecraft.server.level.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mule;
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
}
