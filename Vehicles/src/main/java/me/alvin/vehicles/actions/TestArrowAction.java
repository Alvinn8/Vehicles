package me.alvin.vehicles.actions;

import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleClickAction;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class TestArrowAction implements VehicleMenuAction, VehicleClickAction {
    @Override
    public ItemStack getEntryItem(Vehicle vehicle, Player player) {
        return new ItemStack(Material.ARROW);
    }

    @Override
    public void onClick(Vehicle vehicle, Player player) {
        Location location = player.getEyeLocation();
        Vector direction = location.getDirection();
        location.add(direction.clone().multiply(2));
        location.getWorld().spawnArrow(location, direction, 2, 0);
        location.getWorld().playSound(location, Sound.BLOCK_DISPENSER_LAUNCH, SoundCategory.PLAYERS, 1, 1);
    }
}
