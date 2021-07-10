package me.alvin.vehicles.nms;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.horse.EntityHorseMule;
import net.minecraft.world.level.World;
import org.bukkit.Location;
import org.bukkit.util.Consumer;

/**
 * @see NMS#spawnSeatEntity(Location, Consumer)
 */
public class SeatEntity_v1_17_R1 extends EntityHorseMule {
    public SeatEntity_v1_17_R1(World world) {
        super(EntityTypes.ag, world);
    }

    @Override
    public boolean bC() {
        // This stops the entity from dismounting passengers
        // when they go under water.
        return true;
    }

    @Override
    public void setStanding(boolean standing) {
        // Stop the horse from being able to stand, making the seat fly backwards
        // which we don't want.
        if (standing) return;
        super.setStanding(false);
    }
}
