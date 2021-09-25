package me.alvin.vehicles.actions;

import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleAction;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import svcraft.core.SVCraft;

/**
 * A base class for actions that store items in an inventory.
 */
public abstract class AbstractStorageAction implements VehicleAction {
    private final NamespacedKey storageKey;
    private final Inventory inventory;

    /**
     * @param storageKey The key to store the inventory contents on when loading and saving the vehicle.
     * @param inventory The inventory to store items in. Should have an appropriate title and size.
     */
    public AbstractStorageAction(NamespacedKey storageKey, Inventory inventory) {
        this.storageKey = storageKey;
        this.inventory = inventory;
    }

    @Override
    public void onLoad(Vehicle vehicle, PersistentDataContainer data) {
        PersistentDataContainer[] containers = data.get(this.storageKey, PersistentDataType.TAG_CONTAINER_ARRAY);
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
        data.set(this.storageKey, PersistentDataType.TAG_CONTAINER_ARRAY, containers);
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

    public Inventory getInventory() {
        return inventory;
    }

}
