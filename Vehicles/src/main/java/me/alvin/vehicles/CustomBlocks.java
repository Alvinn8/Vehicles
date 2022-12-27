package me.alvin.vehicles;

import me.alvin.vehicles.crafting.VehicleCraftingTableBlock;

public final class CustomBlocks {
    public static final VehicleCraftingTableBlock VEHICLE_CRAFTING_TABLE = new VehicleCraftingTableBlock(
        () -> SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:block/vehicle_crafting_table")
    );

    private CustomBlocks() {}
}
