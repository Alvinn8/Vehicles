package me.alvin.vehicles.actions;

import me.alvin.vehicles.Missile;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleClickAction;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;

/**
 * An action for shooting and reloading regular missiles.
 */
public class MissileAction implements VehicleMenuAction, VehicleClickAction {
    public static final DecimalFormat TIME_FORMAT = new DecimalFormat("0.0");
    /**
     * The time when the delay has expired and another missile can be shot
     */
    private long delayExpire = System.currentTimeMillis();


    @Override
    public ItemStack getEntryItem(Vehicle vehicle, Player player) {
        return new ItemStack(Material.TNT);
    }

    @Override
    public void onMenuClick(Vehicle vehicle, Player player) {

    }

    @Override
    public Component getActionBarText(Vehicle vehicle, Player player) {
        if (this.delayExpire >= System.currentTimeMillis()) {
            float msLeft = this.delayExpire - System.currentTimeMillis();
            return Component.empty()
                .append(Component.text("Missile").color(NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text(TIME_FORMAT.format(msLeft / 1000.0F)))
                .append(Component.text("s"));
        }
        return Component.text("Missile");
    }

    @Override
    public void onHotbarClick(Vehicle vehicle, Player player) {
        if (this.delayExpire < System.currentTimeMillis()) {
            Location location = player.getEyeLocation();
            Vector direction = location.getDirection();
            location.add(direction.clone().multiply(5));
            new Missile(location, direction, 3, player).start();
            this.delayExpire = System.currentTimeMillis() + 500;
            player.getWorld().playSound(vehicle.getLocation(), Sound.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 3, 1);
        }
    }
}
