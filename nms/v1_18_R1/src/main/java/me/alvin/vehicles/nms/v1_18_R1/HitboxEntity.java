package me.alvin.vehicles.nms.v1_18_R1;

import me.alvin.vehicles.nms.NMS;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.util.Consumer;

/**
 * @see NMS#spawnHitboxEntity(Location, Consumer)
 */
public class HitboxEntity extends Slime {
    public HitboxEntity(Level world) {
        super(EntityType.SLIME, world);
    }

    @Override
    public boolean canBeSeenByAnyone() {
        // This makes the entity not visible to other entities' AI. Stopping iron
        // golems and other mobs that attack from attacking the hitbox entity.
        return false;
    }
}
