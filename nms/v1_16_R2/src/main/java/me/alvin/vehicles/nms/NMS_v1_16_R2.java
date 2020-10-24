package me.alvin.vehicles.nms;

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
}
