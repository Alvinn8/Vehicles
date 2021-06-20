package me.alvin.vehicles.crafting;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.vehicle.VehicleType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import svcraft.core.tileentity.CustomTileEntity;
import svcraft.core.util.BlockLocation;
import svcraft.core.util.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * The vehicle crafting table tile entity that holds all data for
 * placed crafting tables.
 */
public class VehicleCraftingTable extends CustomTileEntity {
    public static final NamespacedKey CURRENT_VIEW = new NamespacedKey(SVCraftVehicles.getInstance(), "view");

    private CraftingView view;
    private final Inventory inventory = Bukkit.createInventory(new VehicleCraftingInventory(), 54);

    public VehicleCraftingTable(World world, BlockLocation location) {
        super(world, location);
        this.setView(CraftingView.SELECTING);

        BlockState state = this.getBlock().getState();
        if (state instanceof TileState) {
            PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
            Integer currentViewOrdinal = container.get(CURRENT_VIEW, PersistentDataType.INTEGER);
            if (currentViewOrdinal != null) {
                this.setView(CraftingView.values()[currentViewOrdinal]);
            }
        }
    }

    public void openInventory(Player player) {
        player.openInventory(this.inventory);
    }

    public CraftingView getView() {
        return this.view;
    }

    /**
     * Set the view for the vehicle crafting table and re-render it.
     *
     * @param view The view to set to
     */
    public void setView(CraftingView view) {
        this.view = view;

        this.inventory.clear();

        switch (this.view) {
            case SELECTING: {
                // Background
                this.inventory.setItem(53, SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:gui/vehicle_crafting_table/selecting"));

                // Fill with vehicle types
                int slot = 10;
                for (VehicleType vehicleType : SVCraftVehicles.getInstance().getRegistry().getRegisteredVehicles().values()) {
                    ItemStack item = new ItemStack(Material.DIAMOND);
                    ItemMeta meta = item.getItemMeta();
                    meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false).append(vehicleType.getName()));
                    item.setItemMeta(meta);
                    this.inventory.setItem(slot, item);
                    slot++;
                    if (slot % 9 == 8) slot += 2;
                }
                break;
            }
            case VIEWING: {
                // Background
                this.inventory.setItem(53, SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:gui/vehicle_crafting_table/viewing"));
                break;
            }
            case CRAFTING: {
                // Background
                this.inventory.setItem(53, SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:gui/vehicle_crafting_table/crafting_disabled"));
                break;
            }
        }
    }

    @Override
    public void onUnload() {
        BlockState state = this.getBlock().getState();
        if (state instanceof TileState) {
            PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
            container.set(CURRENT_VIEW, PersistentDataType.INTEGER, this.view.ordinal());
        }
    }

    public class VehicleCraftingInventory extends CustomInventory {
        @Override
        public void onClick(InventoryClickEvent event) {
            event.setCancelled(true);

            switch (view) {
                case SELECTING: {
                    setView(CraftingView.VIEWING);
                    break;
                }
                case VIEWING: {
                    setView(CraftingView.CRAFTING);
                    break;
                }
                case CRAFTING: {
                    setView(CraftingView.SELECTING);
                    break;
                }
            }
        }
    }
}
