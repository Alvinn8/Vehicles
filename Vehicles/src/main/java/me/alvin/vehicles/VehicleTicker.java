package me.alvin.vehicles;

import me.alvin.vehicles.vehicle.Vehicle;
import org.bukkit.scheduler.BukkitRunnable;

public class VehicleTicker extends BukkitRunnable {
    @Override
    public void run() {
        for (Vehicle vehicle : SVCraftVehicles.getInstance().getLoadedVehicles().values()) {
            vehicle.tick();
        }
    }

    public void start() {
        this.runTaskTimer(SVCraftVehicles.getInstance(), 1, 1);
    }
}
