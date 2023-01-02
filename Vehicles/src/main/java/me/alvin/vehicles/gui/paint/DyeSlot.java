package me.alvin.vehicles.gui.paint;

import ca.bkaw.praeter.gui.components.Slot;
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DyeSlot extends Slot {
    public DyeSlot(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean canHold(@NotNull ItemStack itemStack) {
        return MaterialTags.DYES.isTagged(itemStack);
    }
}
