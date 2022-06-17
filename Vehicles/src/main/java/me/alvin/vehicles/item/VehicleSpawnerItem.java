package me.alvin.vehicles.item;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.VehicleSpawnerTask;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import svcraft.core.item.CustomItem;
import svcraft.core.util.CustomInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VehicleSpawnerItem extends CustomItem {
    public static final VehicleType DEFAULT_VEHICLE_TYPE = VehicleTypes.SIMPLE_CAR;
    public static final NamespacedKey VEHICLE_TYPE_TAG = new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle_type");

    @Override
    public ItemStack makeItemStack() {
        ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/vehicle_spawner");
        ItemMeta meta = item.getItemMeta();
        this.addId(meta);
        this.setName(meta, "Vehicle Spawner");
        meta.lore(Arrays.asList(
            Component.text("Left-click to select a vehicle type").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY),
            Component.text("Right-click to spawn the vehicle").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY)
        ));
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(VEHICLE_TYPE_TAG, PersistentDataType.STRING, DEFAULT_VEHICLE_TYPE.getId());
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Open the vehicle selector gui.
     *
     * @param player The player to open the gui for
     * @param selectedItem The item the player has selected, must be a vehicle spawner
     */
    public void openVehicleTypeSelector(Player player, ItemStack selectedItem) {
        final List<VehicleType> vehicleTypes =  new ArrayList<>(SVCraftVehicles.getInstance().getRegistry().getRegisteredVehicles().values());
        int size = (int) Math.ceil((double) vehicleTypes.size() / 9.0D) * 9;
        Inventory inventory = Bukkit.createInventory(new CustomInventory() {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
                int slot = event.getSlot();
                if (slot >= 0 && slot < vehicleTypes.size()) {
                    VehicleType vehicleType = vehicleTypes.get(slot);
                    ItemMeta meta = selectedItem.getItemMeta();
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    container.set(VEHICLE_TYPE_TAG, PersistentDataType.STRING, vehicleType.getId());
                    selectedItem.setItemMeta(meta);
                    event.getWhoClicked().closeInventory();
                    VehicleSpawnerTask task = SVCraftVehicles.getInstance().getVehicleSpawnerTaskMap().get(player);
                    if (task != null) {
                        task.cancel();
                        SVCraftVehicles.getInstance().getVehicleSpawnerTaskMap().remove(player);
                    }
                    onSelect(player, selectedItem);
                }
            }
        }, size, Component.text("Select the vehicle type to spawn"));
        for (VehicleType vehicleType : vehicleTypes) {
            ItemStack item = new ItemStack(Material.DIAMOND);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false).append(vehicleType.getName()));
            item.setItemMeta(meta);
            inventory.addItem(item);
        }
        player.openInventory(inventory);
    }

    public void onSelect(Player player, ItemStack selectedItem) {
        if (!SVCraftVehicles.getInstance().getVehicleSpawnerTaskMap().containsKey(player)) {
            VehicleSpawnerTask task = new VehicleSpawnerTask(player);
            VehicleType vehicleType = task.getVehicleType(selectedItem);
            if (vehicleType != null) {
                player.sendActionBar(Component.text("Selected Vehicle Type: ").append(vehicleType.getName()));
                task.start();
                SVCraftVehicles.getInstance().getVehicleSpawnerTaskMap().put(player, task);
            } else {
                player.sendActionBar(Component.text("No selected vehicle type", NamedTextColor.RED));
            }
        }
    }
}
