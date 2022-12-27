package me.alvin.vehicles.gui.fuel;

import me.alvin.vehicles.util.DebugUtil;
import org.bukkit.scheduler.BukkitRunnable;

public class FuelGuiTask extends BukkitRunnable {
    private final FuelGui fuelGui;

    public FuelGuiTask(FuelGui fuelGui) {
        this.fuelGui = fuelGui;
    }

    @Override
    public void run() {
        if (this.fuelGui.getViewers().size() == 0) {
            DebugUtil.debug("Stopping fuel gui task");
            this.cancel();
            return;
        }
        float newPercentage = this.fuelGui.calculatePercentage();
        float currentPercentage = this.fuelGui.getPercentage();
        if (Math.abs(newPercentage - currentPercentage) > 0.005) {
            this.fuelGui.setPercentage(newPercentage);
            this.fuelGui.update();
        }
    }
}
