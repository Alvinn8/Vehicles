package me.alvin.vehicles.actions;

import me.alvin.vehicles.CustomItems;
import me.alvin.vehicles.item.FuelItem;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import svcraft.core.item.CustomItem;
import svcraft.core.resourcepack.modelmanagerdata.RPCModelManagerData;
import svcraft.core.util.CustomInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

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
        ItemStack item = SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:gui/vehicle_menu_icon/fuel");
        item.editMeta(meta -> meta.displayName(Component.text("Fuel").decoration(TextDecoration.ITALIC, false)));
        return item;
    }

    @Override
    public void onMenuClick(Vehicle vehicle, Player player) {
        Inventory inventory = Bukkit.createInventory(new CustomInventory() {
            @Override
            public void onClick(InventoryClickEvent event) {
                DebugUtil.debug("Event called");
                boolean isTop = event.getClickedInventory() == event.getView().getTopInventory();
                if (isTop && event.getSlot() == 19 && event.getAction() == InventoryAction.PLACE_ALL) {
                    CustomItem item = CustomItem.getItem(event.getCursor());
                    if (item == CustomItems.FUEL) {
                        fuelVehicle(vehicle, event.getWhoClicked(), event.getInventory());
                        return;
                    }
                }
                if (isTop || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                    event.setCancelled(true);
                }
            }
        }, 27, Component.text("Fuel"));

        renderInventory(inventory, vehicle);

        player.openInventory(inventory);

        new FuelGUITask(inventory, player, vehicle).runTaskTimer(SVCraftVehicles.getInstance(), 20, 20);
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

    private static void fuelVehicle(Vehicle vehicle, HumanEntity player, Inventory inventory) {
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, SoundCategory.PLAYERS, 1, 1);
        vehicle.setCurrentFuel(vehicle.getCurrentFuel() + FuelItem.FUEL_AMOUNT);
        Bukkit.getScheduler().runTaskLater(SVCraftVehicles.getInstance(), () -> {
            renderInventory(inventory, vehicle);
            DebugUtil.debug("re-rendering");
        }, 1);
    }

    public static class FuelGUITask extends BukkitRunnable {
        private final Inventory inventory;
        private final Player player;
        private final Vehicle vehicle;

        public FuelGUITask(Inventory inventory, Player player, Vehicle vehicle) {
            this.inventory = inventory;
            this.player = player;
            this.vehicle = vehicle;
        }

        @Override
        public void run() {
            if (!this.inventory.getViewers().contains(this.player)) {
                DebugUtil.debug("Stopping fuel gui task");
                this.cancel();
                return;
            }
            ItemStack itemStack = this.inventory.getItem(19);
            if (itemStack != null) DebugUtil.debug("Found item! "+ itemStack);
            CustomItem item = CustomItem.getItem(itemStack);
            if (item == CustomItems.FUEL) {
                FuelAction.fuelVehicle(this.vehicle, this.player, this.inventory);
            } else if (itemStack != null && itemStack.getType() != Material.AIR) {
                HashMap<Integer, ItemStack> couldntAdd = this.player.getInventory().addItem(itemStack);
                if (couldntAdd.size() > 0) {
                    this.player.getWorld().dropItem(this.player.getLocation(), itemStack);
                }
            }
            FuelAction.renderInventory(this.inventory, this.vehicle);
        }
    }
}
