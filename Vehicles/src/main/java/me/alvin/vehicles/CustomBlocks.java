package me.alvin.vehicles;

import me.alvin.vehicles.crafting.VehicleCraftingTableBlock;
import svcraft.core.block.CustomBlock;

public final class CustomBlocks {
    public static final VehicleCraftingTableBlock VEHICLE_CRAFTING_TABLE = new VehicleCraftingTableBlock(
        new CustomBlock.BlockProperties()
            .renderItemStack(
                SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:block/vehicle_crafting_table")
            )
    );

    private CustomBlocks() {}
}
