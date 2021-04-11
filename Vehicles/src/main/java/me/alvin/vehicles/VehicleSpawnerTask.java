package me.alvin.vehicles;

import me.alvin.vehicles.item.VehicleSpawnerItem;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.svcraft.minigames.item.CustomItem;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VehicleSpawnerTask extends BukkitRunnable {
    public static final double MAX_DISTANCE = 20;

    private final @NotNull Player player;
    private @Nullable Location lastLocation;
    private @Nullable Vehicle vehicle;

    public VehicleSpawnerTask(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        ItemStack selectedItem = this.player.getInventory().getItemInMainHand();
        if (CustomItem.getItem(selectedItem) != CustomItems.VEHICLE_SPAWNER) {
            this.cancel();
            return;
        }
        Location playerLocation = this.player.getLocation();
        if (!playerLocation.equals(this.lastLocation)) {
            RayTraceResult rayTraceResult = this.player.rayTraceBlocks(MAX_DISTANCE);
            Location vehicleLocation = null;
            if (rayTraceResult != null) {
                vehicleLocation = rayTraceResult.getHitPosition().toLocation(this.player.getWorld());
            }
            Vector playerDirection = playerLocation.getDirection();
            if (vehicleLocation == null) {
                vehicleLocation = playerLocation.clone().add(playerDirection.clone().multiply(MAX_DISTANCE));
            }

            vehicleLocation.setY(vehicleLocation.getBlockY());
            vehicleLocation.setYaw(playerLocation.getYaw());
            vehicleLocation.setPitch(0);

            if (this.vehicle == null) {
                VehicleType vehicleType = this.getVehicleType(selectedItem);
                if (vehicleType == null) return;
                this.vehicle = vehicleType.construct(vehicleLocation, this.player, VehicleSpawnReason.CREATIVE);
                this.vehicle.becomeHologram();
            }

            // While loop but max 20 iterations
            for (int i = 0; i < 20; i++) {
                if (this.vehicle.collides(vehicleLocation)) {
                    // If the vehicle collides, go back a bit
                    vehicleLocation.subtract(playerDirection);
                } else {
                    // If it doesn't collide we have found a good position
                    break;
                }
            }

            vehicleLocation.setY(vehicleLocation.getBlockY());

            this.vehicle.setLocation(vehicleLocation);

            if (this.vehicle.getSlime() != null) {
                this.vehicle.updateRenderedLocation();
                if (!this.vehicle.isNonInterpolating()) {
                    this.vehicle.setNonInterpolating(true);
                }
            }

            this.lastLocation = playerLocation;
        }
    }

    /**
     * Get the currently selected vehicle type.
     *
     * @return The vehicle type to spawn
     */
    @Nullable
    public VehicleType getVehicleType(@Nullable ItemStack selectedItem) {
        if (selectedItem == null) return null;
        ItemMeta meta = selectedItem.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String vehicleTypeId = container.get(VehicleSpawnerItem.SELECTED_VEHICLE_TYPE, PersistentDataType.STRING);
        if (vehicleTypeId == null) return null;
        return SVCraftVehicles.getInstance().getRegistry().getVehicle(vehicleTypeId);
    }

    /**
     * Called when the player right clicks and will spawn an actual vehicle at
     * the location the spawner is pointing at.
     */
    public void spawn() {
        ItemStack selectedItem = this.player.getInventory().getItemInMainHand();
        if (this.vehicle != null) {
            VehicleType vehicleType = this.getVehicleType(selectedItem);
            if (vehicleType != null) vehicleType.construct(this.vehicle.getLocation(), this.player, VehicleSpawnReason.CREATIVE);
        }
    }

    public void start() {
        this.runTaskTimer(SVCraftVehicles.getInstance(), 1, 1);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();

        if (this.vehicle != null) this.vehicle.remove();
        SVCraftVehicles.getInstance().getVehicleSpawnerTaskMap().remove(this.player);
    }
}
