package me.alvin.vehicles.actions;

import me.alvin.vehicles.CustomItems;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.gui.fuel.FuelGui;
import me.alvin.vehicles.item.FuelItem;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FuelAction implements VehicleMenuAction {
    public final static FuelAction INSTANCE = new FuelAction();

    private FuelAction() {}

    @Override
    public void onRemove(Vehicle vehicle) {
        Location location = vehicle.getLocation();
        World world = location.getWorld();
        int fuel = vehicle.getCurrentFuel();
        while (fuel > FuelItem.FUEL_AMOUNT) {
            world.dropItemNaturally(location, CustomItems.FUEL.makeItemStack());
            fuel -= FuelItem.FUEL_AMOUNT;
        }
    }

    @Override
    public ItemStack getEntryItem(Vehicle vehicle, Player player) {
        ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:gui/vehicle_menu_icon/fuel");
        item.editMeta(meta -> meta.displayName(Component.text("Fuel", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
        return item;
    }

    @Override
    public void onMenuClick(Vehicle vehicle, Player player) {
        FuelGui gui = new FuelGui(vehicle);
        gui.show(player);

    }
}
