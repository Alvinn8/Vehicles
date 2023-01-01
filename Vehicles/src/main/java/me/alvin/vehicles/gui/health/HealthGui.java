package me.alvin.vehicles.gui.health;

import ca.bkaw.praeter.gui.components.DisableableButton;
import ca.bkaw.praeter.gui.components.StaticSlot;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import me.alvin.vehicles.gui.repair.RepairingGui;
import me.alvin.vehicles.vehicle.RepairData;
import me.alvin.vehicles.vehicle.RepairProgress;
import me.alvin.vehicles.vehicle.Vehicle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HealthGui extends CustomGui {

    private static final StaticSlot SLOT_1 = new StaticSlot(0, 0);
    private static final StaticSlot SLOT_2 = new StaticSlot(1, 0);
    private static final StaticSlot SLOT_3 = new StaticSlot(2, 0);
    private static final DisableableButton BUTTON = new DisableableButton("Repair", 3, 0, 2, 1);
    private static final HealthIcon ICON = new HealthIcon(7, 0);

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .title(Component.text("Health and repairs"))
        .height(1)
        .add(SLOT_1, SLOT_2, SLOT_3, BUTTON, ICON)
        .build();

    public HealthGui(Vehicle vehicle, Player player) {
        super(TYPE);

        ICON.get(this).setVehicle(vehicle);

        ItemStack[] items = vehicle.getType().getRepairData().repairIngredients();
        SLOT_1.get(this).setItemStack(items[0]);
        SLOT_2.get(this).setItemStack(items[1]);
        SLOT_3.get(this).setItemStack(items[2]);

        boolean canRepair = canRepair(vehicle, player);
        BUTTON.get(this).setEnabled(canRepair);
        if (!(vehicle.getHealth() < vehicle.getType().getMaxHealth())) {
            BUTTON.get(this).setHoverText(List.of(
                Component.text("Vehicle has full health.")
            ));
        } else if (canRepair) {
            BUTTON.get(this).setHoverText(List.of(
                Component.text("+" + vehicle.getType().getRepairData().repairAmount() + " health points")
            ));
        } else {
            BUTTON.get(this).setHoverText(List.of(
                Component.text("Insufficient materials to repair.", NamedTextColor.RED)
            ));
        }

        BUTTON.get(this).setOnClick(context -> {
            if (canRepair(vehicle, player)) {
                RepairData repairData = vehicle.getType().getRepairData();
                for (ItemStack itemStack : repairData.repairIngredients()) {
                    if (itemStack != null) {
                        player.getInventory().removeItemAnySlot(itemStack);
                    }
                }
                long completionTime = System.currentTimeMillis() + repairData.repairTime();
                RepairProgress repairProgress = new RepairProgress(vehicle, completionTime);
                vehicle.setRepairProgress(repairProgress);
                repairProgress.startTimer();
                RepairingGui repairingGui = repairProgress.getRepairingGui();
                getViewers().forEach(repairingGui::show);
            }
        });
    }

    private static boolean canRepair(Vehicle vehicle, Player player) {
        if (!(vehicle.getHealth() < vehicle.getType().getMaxHealth())) {
            return false;
        }
        ItemStack[] items = vehicle.getType().getRepairData().repairIngredients();
        boolean canAfford = true;
        for (ItemStack item : items) {
            if (item != null) {
                if (!player.getInventory().containsAtLeast(item, item.getAmount())) {
                    canAfford = false;
                }
            }
        }
        return canAfford;
    }
}
