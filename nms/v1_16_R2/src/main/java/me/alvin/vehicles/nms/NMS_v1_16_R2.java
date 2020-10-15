package me.alvin.vehicles.nms;

import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class NMS_v1_16_R2 implements NMS {
    public String getVersion() {
        return "v1_16_R2";
    }

    public void setEntityLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        ((CraftEntity) entity).getHandle().setPositionRotation(x, y, z, yaw, pitch);
    }
}
