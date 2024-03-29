package me.alvin.vehicles.crafting;

import me.alvin.vehicles.CustomItems;
import me.alvin.vehicles.util.DebugUtil;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import svcraft.core.block.CustomBlock;
import svcraft.core.tileentity.CustomTileEntity;
import svcraft.core.tileentity.TileEntityBlock;
import svcraft.core.util.BlockLocation;

/**
 * The vehicle crafting table custom block.
 */
public class VehicleCraftingTableBlock extends CustomBlock implements TileEntityBlock {
    public VehicleCraftingTableBlock(@Nullable BlockProperties properties) {
        super(properties);
    }

    @Override
    @NotNull
    public CustomTileEntity createTileEntity(World world, BlockLocation blockLocation) {
        DebugUtil.debug("Placing crafting table in " + world.getName() + " at "+ blockLocation.getX() + " " + blockLocation.getY() + " " + blockLocation.getZ());
        return new VehicleCraftingTable(world, blockLocation);
    }

    @Override
    public void onBreak(Block block) {
        block.getWorld().dropItemNaturally(block.getLocation(), CustomItems.VEHICLE_CRAFTING_TABLE.makeItemStack());
    }
}
