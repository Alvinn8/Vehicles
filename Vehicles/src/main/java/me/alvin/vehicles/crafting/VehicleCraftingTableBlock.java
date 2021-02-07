package me.alvin.vehicles.crafting;

import me.alvin.vehicles.util.DebugUtil;
import me.svcraft.minigames.block.CustomBlock;
import me.svcraft.minigames.tileentity.CustomTileEntity;
import me.svcraft.minigames.tileentity.TileEntityBlock;
import me.svcraft.minigames.util.BlockLocation;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}
