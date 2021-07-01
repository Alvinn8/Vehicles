package me.alvin.vehicles.explosion;

import me.alvin.vehicles.SVCraftVehicles;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.List;

public class ExplosionDebrisRunnable extends BukkitRunnable {
    private final List<ExplosionDebris> debrisList;

    public ExplosionDebrisRunnable(List<ExplosionDebris> debrisList) {
        this.debrisList = debrisList;
    }

    @Override
    public void run() {
        Iterator<ExplosionDebris> iterator = this.debrisList.iterator();

        while (iterator.hasNext()) {
            ExplosionDebris debris = iterator.next();
            debris.tick();

            // If it collided with something, remove from the list
            if (!debris.getLocation().getBlock().isPassable()) {
                iterator.remove();
            }
        }

        if (this.debrisList.isEmpty()) {
            this.cancel();
        }
    }

    public void start() {
        this.runTaskTimer(SVCraftVehicles.getInstance(), 1, 1);
    }
}
