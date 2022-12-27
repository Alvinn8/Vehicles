package me.alvin.vehicles.actions;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.gui.repair.RepairGui;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HealthAction implements VehicleMenuAction {
    public static final HealthAction INSTANCE = new HealthAction();

    private HealthAction() {}

    @Override
    public ItemStack getEntryItem(Vehicle vehicle, Player player) {
        ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:gui/vehicle_menu_icon/health");
        item.editMeta(meta -> meta.displayName(Component.text("Health and repairs", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
        return item;
    }

    @Override
    public void onMenuClick(Vehicle vehicle, Player player) {
        RepairGui gui = new RepairGui(vehicle, player);
        gui.show(player);
    }
}
