package me.alvin.vehicles.item;

import me.alvin.vehicles.SVCraftVehicles;
import org.bukkit.inventory.ItemStack;
import svcraft.core.item.CustomItem;

public class FuelItem extends CustomItem {
    /**
     * The amount of fuel one fuel item adds to the vehicle's current fuel.
     */
    public static final int FUEL_AMOUNT = 5000;

    @Override
    public ItemStack makeItemStack() {
        ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/fuel");
        this.setName(item, "Fuel");
        this.addId(item);
        return item;
    }
}
