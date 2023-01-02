package me.alvin.vehicles;

import me.alvin.vehicles.item.FuelItem;
import me.alvin.vehicles.item.PaintBucket;
import me.alvin.vehicles.item.VehicleSpawnerItem;
import svcraft.core.item.BlockItem;

public final class CustomItems {
    public static final BlockItem VEHICLE_CRAFTING_TABLE = new BlockItem(CustomBlocks.VEHICLE_CRAFTING_TABLE, "Vehicle Crafting Table");
    public static final FuelItem FUEL = new FuelItem();
    public static final VehicleSpawnerItem VEHICLE_SPAWNER = new VehicleSpawnerItem();
    public static final PaintBucket PAINT_BUCKET = new PaintBucket();

    private CustomItems() {}
}
