package me.alvin.vehicles.actions;

import me.alvin.vehicles.CustomItems;
import me.alvin.vehicles.FuelItem;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import me.svcraft.minigames.item.CustomItem;
import me.svcraft.minigames.resourcepack.modelmanagerdata.RPCModelManagerData;
import me.svcraft.minigames.util.CustomInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

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
        return SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:gui/vehicle_menu_icon/fuel");
    }

    @Override
    public void onClick(Vehicle vehicle, Player player) {
        Inventory inventory = Bukkit.createInventory(new CustomInventory() {
            @Override
            public void onClick(InventoryClickEvent event) {
                DebugUtil.debug("Event called");
                if (event.getSlot() == 19 && event.getAction() == InventoryAction.PLACE_ALL) {
                    CustomItem item = CustomItem.getItem(event.getCursor());
                    if (item == CustomItems.FUEL) {
                        HumanEntity player = event.getWhoClicked();
                        player.getWorld().playSound(player.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, SoundCategory.PLAYERS, 1, 1);
                        vehicle.setCurrentFuel(vehicle.getCurrentFuel() + FuelItem.FUEL_AMOUNT);
                        Bukkit.getScheduler().runTaskLater(SVCraftVehicles.getInstance(), () -> {
                            renderInventory(event.getInventory(), vehicle);
                            DebugUtil.debug("re-rendering");
                        }, 1);
                        DebugUtil.debug("added fuel");
                        return;
                    }
                }
                event.setCancelled(true);
            }
        }, 27, Component.text("Fuel"));

        renderInventory(inventory, vehicle);

        player.openInventory(inventory);
    }

    private static void renderInventory(Inventory inventory, Vehicle vehicle) {
        inventory.clear();

        RPCModelManagerData resourcepackData = SVCraftVehicles.getInstance().getResourcepackData();
        double decimalCurrentFuel = vehicle.getMaxFuel() != 0 ? (double) vehicle.getCurrentFuel() / (double) vehicle.getMaxFuel() : 0;
        int percentage = (int) Math.round(decimalCurrentFuel * 100.0D);
        TextComponent percentageComponent = Component.text(percentage + "%").color(percentage < 0.1 ? NamedTextColor.RED : NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);

        ItemStack percentageItem = resourcepackData.generateItem("svcraftvehicles:item/transparent");
        ItemMeta percentageMeta = percentageItem.getItemMeta();
        percentageMeta.displayName(percentageComponent);
        percentageMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        percentageItem.setItemMeta(percentageMeta);

        inventory.setItem(0, percentageItem.clone());
        inventory.setItem(8, percentageItem.clone());

        int fuelBarLeft = (int) Math.round(decimalCurrentFuel * 7 * 4); // 7 as there are 7 items and 4 as each item can hold 4 different amounts
        for (int i = 1; i <= 7; i++) {
            String texture;
            if (fuelBarLeft >= 4) {
                texture = "svcraftvehicles:gui/fuel/fuel_bar_full";
                fuelBarLeft -= 4;
            } else if (fuelBarLeft == 3) {
                texture = "svcraftvehicles:gui/fuel/fuel_bar_three_quarters";
                fuelBarLeft = 0;
            } else if (fuelBarLeft == 2) {
                texture = "svcraftvehicles:gui/fuel/fuel_bar_half";
                fuelBarLeft = 0;
            } else if (fuelBarLeft == 1) {
                texture = "svcraftvehicles:gui/fuel/fuel_bar_quarter";
                fuelBarLeft = 0;
            } else {
                texture = "svcraftvehicles:item/transparent";
            }
            ItemStack item = resourcepackData.generateItem(texture);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(percentageComponent);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }

        inventory.setItem(26, resourcepackData.generateItem("svcraftvehicles:gui/fuel/fuel"));
    }
}
