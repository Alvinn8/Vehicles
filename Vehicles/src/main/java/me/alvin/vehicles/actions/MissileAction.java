package me.alvin.vehicles.actions;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.explosion.ExplodingProjectile;
import me.alvin.vehicles.item.MissileItem;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleClickAction;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import svcraft.core.item.CustomItem;
import svcraft.core.util.CustomInventory;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * An action for shooting and reloading regular missiles.
 */
public class MissileAction extends AbstractStorageAction implements VehicleMenuAction, VehicleClickAction {
    public static final NamespacedKey KEY = new NamespacedKey(SVCraftVehicles.getInstance(), "missiles");
    public static final DecimalFormat TIME_FORMAT = new DecimalFormat("0.0");
    /**
     * The time when the delay has expired and another missile can be shot.
     */
    private long delayExpire = System.currentTimeMillis();
    private long outOfAmmoMessageExpire = -1;

    public MissileAction() {
        super(KEY, Bukkit.createInventory(new MissileStorage(), 9, Component.text("Missiles")));
    }

    @Override
    public ItemStack getEntryItem(Vehicle vehicle, Player player) {
        return new ItemStack(Material.TNT);
    }

    @Override
    public void onMenuClick(Vehicle vehicle, Player player) {
        player.openInventory(this.getInventory());
    }

    @Override
    public Component getActionBarText(Vehicle vehicle, Player player) {
        long now = System.currentTimeMillis();
        if (this.delayExpire >= now) {
            float msLeft = this.delayExpire - now;
            return Component.text()
                .append(Component.text("Missile", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text(TIME_FORMAT.format(msLeft / 1000.0F)))
                .append(Component.text("s"))
                .build();
        }
        if (this.outOfAmmoMessageExpire >= now) {
            return Component.text()
                .append(Component.text("Missile"))
                .append(Component.text(" Out of Missiles", NamedTextColor.RED))
                .build();
        }
        return Component.text("Missile");
    }

    @Override
    public void onHotbarClick(Vehicle vehicle, Player player) {
        if (this.delayExpire < System.currentTimeMillis()) {
            ItemStack item = null;
            MissileItem missileItem = null;
            for (int index = 0; index < this.getInventory().getSize(); index++) {
                item = this.getInventory().getItem(index);
                if (item == null || item.getType().isAir()) continue;

                CustomItem customItem = CustomItem.getItem(item);
                if (customItem instanceof MissileItem) {
                    // Consume 1
                    item.setAmount(item.getAmount() - 1);
                    // Assign missileItem so we can reference it later.
                    missileItem = (MissileItem) customItem;
                    // Break out of the loop, item and missileItem have been set.
                    break;
                }

                // In case we find something that isn't a missile a player might
                // have the gui open and is adding invalid items, let's not shoot
                // a missile right now.
                return;
            }

            // If no missile item was found, there is no ammo.
            if (missileItem == null) {
                // Show the out of missiles message for 2.5 seconds
                this.outOfAmmoMessageExpire = System.currentTimeMillis() + 2500;
                return;
            }

            Location location = player.getEyeLocation();
            Vector direction = location.getDirection();

            new ExplodingProjectile(location, direction, false, missileItem.getExplosionPower(item), player, entity -> {
                // Do not blow up if there is collision with this vehicle
                if (SVCraftVehicles.getInstance().getVehiclePartMap().get(entity) == vehicle) return false;
                // Do not blow up if there is collision with a passenger of this vehicle
                if (SVCraftVehicles.getInstance().getVehicle(entity) == vehicle) return false;
                // Otherwise it's a valid entity collision
                return true;
            }).start();

            this.delayExpire = System.currentTimeMillis() + 500;
            player.getWorld().playSound(vehicle.getLocation(), Sound.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 3, 1);
        }
    }

    public static class MissileStorage extends CustomInventory {
        @Override
        public void onClick(InventoryClickEvent inventoryClickEvent) {}

        @Override
        public void onClose(InventoryCloseEvent event) {
            Inventory inventory = event.getInventory();
            for (int index = 0; index < inventory.getSize(); index++) {
                ItemStack item = inventory.getItem(index);
                if (item != null && !(CustomItem.getItem(item) instanceof MissileItem)) {
                    // The item wasn't a missile, let's return it

                    // Clear the item from the inventory
                    inventory.clear(index);

                    HumanEntity player = event.getPlayer();
                    // Attempt to give the item
                    HashMap<Integer, ItemStack> toDrop = player.getInventory().addItem(item);
                    // And drop all items that didn't fit
                    for (ItemStack itemToDrop : toDrop.values()) {
                        player.getWorld().dropItem(player.getLocation(), itemToDrop);
                    }
                }
            }
        }
    }
}
