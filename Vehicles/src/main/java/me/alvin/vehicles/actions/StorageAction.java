package me.alvin.vehicles.actions;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import svcraft.core.SVCraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class StorageAction implements VehicleMenuAction {
    public static final NamespacedKey KEY = new NamespacedKey(SVCraftVehicles.getInstance(), "storage");

    private final Inventory inventory;

    public StorageAction(int slots) {
        this.inventory = Bukkit.createInventory(null, slots, Component.text("Vehicle Storage"));
    }

    @Override
    public void onLoad(Vehicle vehicle, PersistentDataContainer data) {
        PersistentDataContainer[] containers = data.get(KEY, PersistentDataType.TAG_CONTAINER_ARRAY);
        if (containers != null) {
            for (int i = 0; i < containers.length; i++) {
                PersistentDataContainer container = containers[i];
                ItemStack item = SVCraft.getInstance().getNMS().loadItemStack(container);
                this.inventory.setItem(i, item);
            }
        }
    }

    @Override
    public void onSave(Vehicle vehicle, PersistentDataContainer data) {
        ItemStack[] contents = this.inventory.getContents();
        PersistentDataContainer[] containers = new PersistentDataContainer[contents.length];
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            PersistentDataContainer container = data.getAdapterContext().newPersistentDataContainer();
            if (item != null) {
                SVCraft.getInstance().getNMS().saveItemStack(item, container);
            }
            containers[i] = container;
        }
        data.set(KEY, PersistentDataType.TAG_CONTAINER_ARRAY, containers);
    }

    @Override
    public void onRemove(Vehicle vehicle) {
        Location location = vehicle.getLocation();
        World world = location.getWorld();
        for (ItemStack item : this.inventory) {
            if (item != null) {
                world.dropItemNaturally(location, item);
            }
        }
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
        player.openInventory(this.inventory);
    }

    public Inventory getInventory() {
        return this.inventory;
    }
}
