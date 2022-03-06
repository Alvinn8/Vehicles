package me.alvin.vehicles.nms.v1_18_R2;

import me.alvin.vehicles.nms.NMS;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.util.Consumer;

/**
 * @see NMS#spawnSeatEntity(Location, Consumer)
 */
public class SeatEntity extends Mule {
    public SeatEntity(Level world) {
        super(EntityType.MULE, world);
    }

    @Override
    public boolean rideableUnderWater() {
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
