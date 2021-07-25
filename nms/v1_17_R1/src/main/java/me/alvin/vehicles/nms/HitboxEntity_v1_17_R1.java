package me.alvin.vehicles.nms;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntitySlime;
import net.minecraft.world.level.World;
import org.bukkit.Location;
import org.bukkit.util.Consumer;

/**
 * @see NMS#spawnHitboxEntity(Location, Consumer)
 */
public class HitboxEntity_v1_17_R1 extends EntitySlime {
    public HitboxEntity_v1_17_R1(World world) {
        super(EntityTypes.aD, world);
    }

    @Override
    public boolean dO() {
        // This makes the entity not visible to other entities' AI. Stopping iron
        // golems and other mobs that attack from attacking the hitbox entity.
        return false;
    }
}
