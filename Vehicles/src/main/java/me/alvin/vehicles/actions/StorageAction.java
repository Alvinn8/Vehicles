package me.alvin.vehicles.actions;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StorageAction extends AbstractStorageAction implements VehicleMenuAction {
    public static final NamespacedKey KEY = new NamespacedKey(SVCraftVehicles.getInstance(), "storage");

    public StorageAction(int slots) {
        super(KEY, Bukkit.createInventory(null, slots, Component.text("Vehicle Storage")));
    }

    @Override
    public ItemStack getEntryItem(Vehicle vehicle, Player player) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Storage").decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void onMenuClick(Vehicle vehicle, Player player) {
        player.openInventory(this.getInventory());
    }
}
