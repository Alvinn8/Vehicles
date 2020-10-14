package me.alvin.vehicles.actions;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import me.svcraft.minigames.util.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FuelAction implements VehicleMenuAction {
    public final static FuelAction INSTANCE = new FuelAction();

    @Override
    public ItemStack getEntryItem(Vehicle vehicle, Player player) {
        return new ItemStack(Material.LAVA_BUCKET);
    }

    @Override
    public void onClick(Vehicle vehicle, Player player) {
        Inventory inventory = Bukkit.createInventory(new CustomInventory() {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
            }
        }, 18, SVCraftVehicles.getInstance().msg("action.fuel.title"));

        // TODO: Fueling
        ItemStack temp = new ItemStack(Material.BUCKET);
        ItemMeta meta = temp.getItemMeta();
        int percentage = vehicle.getCurrentFuel() / vehicle.getMaxFuel();
        meta.setDisplayName(percentage + "%");
        temp.setItemMeta(meta);
        inventory.addItem(temp);

        player.openInventory(inventory);
    }
}
