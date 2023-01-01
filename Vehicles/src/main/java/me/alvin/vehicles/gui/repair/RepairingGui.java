package me.alvin.vehicles.gui.repair;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import me.alvin.vehicles.gui.Timer;
import me.alvin.vehicles.gui.health.HealthIcon;
import me.alvin.vehicles.vehicle.RepairProgress;
import me.alvin.vehicles.vehicle.Vehicle;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RepairingGui extends CustomGui {
    private static final Timer TIMER = new Timer(0, 0);
    private static final HealthIcon ICON = new HealthIcon(5, 0);
    private static final Button CANCEL = new Button("Cancel", 7, 0, 2, 1);

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .title(Component.text("Repairing"))
        .height(1)
        .add(TIMER, ICON, CANCEL)
        .build();

    private final Vehicle vehicle;
    private final RepairProgress repairProgress;

    public RepairingGui(Vehicle vehicle, RepairProgress repairProgress) {
        super(TYPE);
        this.vehicle = vehicle;
        this.repairProgress = repairProgress;

        ICON.get(this).setVehicle(vehicle);

        this.updateTimer();

        CANCEL.get(this).setOnClick(context -> {
            ItemStack[] items = this.vehicle.getType().getRepairData().repairIngredients();
            for (ItemStack item : items) {
                if (item != null) {
                    this.vehicle.getLocation().getWorld().dropItemNaturally(this.vehicle.getLocation(), item);
                }
            }
            this.vehicle.setRepairProgress(null);
            getViewers().forEach(Player::closeInventory);
        });
    }

    public void updateTimer() {
        long completionTime = this.repairProgress.getCompletionTime();

        int ms = (int) (completionTime - System.currentTimeMillis());
        int totalSeconds = ms / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        int minutes1 = minutes / 10; // first digit
        int minutes2 = minutes % 10; // second digit

        int seconds1 = seconds / 10; // first digit
        int seconds2 = seconds % 10; // second digit

        TIMER.get(this).set(minutes1, minutes2, seconds1, seconds2);
        TIMER.get(this).setHoverText(List.of(
            Component.text("Repair finishes in " + minutes + " " + (minutes == 1 ? "minute" : "minutes") + " and " + seconds + " " + (seconds == 1 ? "second" : "seconds") + "."),
            Component.text("+" + this.vehicle.getType().getRepairData().repairAmount() + " health points")
        ));
    }
}
