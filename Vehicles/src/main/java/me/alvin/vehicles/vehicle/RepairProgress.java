package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.gui.repair.RepairingGui;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Progress for a vehicle that is being repaired.
 */
public class RepairProgress extends BukkitRunnable {
    private final Vehicle vehicle;
    private final long completionTime;
    private final RepairingGui repairingGui;

    public RepairProgress(Vehicle vehicle, long completionTime) {
        this.vehicle = vehicle;
        this.completionTime = completionTime;
        this.repairingGui = new RepairingGui(vehicle, this);
    }

    public long getCompletionTime() {
        return this.completionTime;
    }

    public RepairingGui getRepairingGui() {
        return this.repairingGui;
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() >= this.completionTime) {
            this.cancel();
            int repairAmount = this.vehicle.getType().getRepairData().repairAmount();
            this.vehicle.repair(repairAmount);
            this.vehicle.setRepairProgress(null);
            this.repairingGui.getViewers().forEach(Player::closeInventory);
            return;
        }

        if (this.repairingGui.getViewers().size() > 0) {
            this.repairingGui.updateTimer();
            this.repairingGui.update();
        }
    }

    public void startTimer() {
        this.runTaskTimer(SVCraftVehicles.getInstance(), 20L, 20L);
    }
}
