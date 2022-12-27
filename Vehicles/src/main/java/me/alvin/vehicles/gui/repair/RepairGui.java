package me.alvin.vehicles.gui.repair;

import ca.bkaw.praeter.gui.components.DisableableButton;
import ca.bkaw.praeter.gui.components.StaticSlot;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import me.alvin.vehicles.vehicle.Vehicle;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RepairGui extends CustomGui {

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

    public RepairGui(Vehicle vehicle, Player player) {
        super(TYPE);

        ICON.get(this).setVehicle(vehicle);

        ItemStack[] items = vehicle.getType().getRepairData().repairIngredients();
        SLOT_1.get(this).setItemStack(items[0]);
        SLOT_2.get(this).setItemStack(items[1]);
        SLOT_3.get(this).setItemStack(items[2]);

        boolean hasEnough = true;
        for (ItemStack item : items) {
            if (item != null) {
                if (!player.getInventory().containsAtLeast(item, item.getAmount())) {
                    hasEnough = false;
                }
            }
        }

        BUTTON.get(this).setEnabled(hasEnough);
    }
}
