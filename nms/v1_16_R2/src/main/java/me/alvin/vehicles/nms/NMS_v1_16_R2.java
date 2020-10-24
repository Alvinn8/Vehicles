package me.alvin.vehicles.nms;

import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.server.v1_16_R2.PacketPlayInSteerVehicle;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class NMS_v1_16_R2 implements NMS {
    public String getVersion() {
        return "v1_16_R2";
    }

    public void setEntityLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        ((CraftEntity) entity).getHandle().setLocation(x, y, z, yaw, pitch);
    }

    public void setEntityRotation(Entity entity, float yaw) {
        net.minecraft.server.v1_16_R2.Entity handle = ((CraftEntity) entity).getHandle();
        handle.yaw = yaw;
        handle.setHeadRotation(yaw);
    }

    public void markDirty(Entity entity) {
        ((CraftEntity) entity).getHandle().impulse = true;
    }

    public void handlePacket(VehicleSteeringMovement movement, PacketEvent event) {
        PacketPlayInSteerVehicle packet = (PacketPlayInSteerVehicle) event.getPacket().getHandle();
        movement.forward = packet.c();
        movement.side = packet.b();
        movement.space = packet.d();
        movement.shift = packet.e();
    }
}
