package me.alvin.vehicles;

import me.alvin.vehicles.crafting.VehicleCraftingTableItem;

public final class CustomItems {
    public static final VehicleCraftingTableItem VEHICLE_CRAFTING_TABLE = new VehicleCraftingTableItem(CustomBlocks.VEHICLE_CRAFTING_TABLE, "Vehicle Crafting Table");
    public static final FuelItem FUEL = new FuelItem();

    private CustomItems() {}
}
