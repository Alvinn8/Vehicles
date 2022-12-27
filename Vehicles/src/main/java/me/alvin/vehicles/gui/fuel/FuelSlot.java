package me.alvin.vehicles.gui.fuel;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.font.RenderSetupContext;
import ca.bkaw.praeter.gui.gui.CustomGui;
import me.alvin.vehicles.CustomItems;
import me.alvin.vehicles.SVCraftVehicles;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import svcraft.core.item.CustomItem;

import java.io.IOException;

public class FuelSlot extends Slot {
    public static final NamespacedKey FUEL_OUTLINE = new NamespacedKey(SVCraftVehicles.getInstance(), "gui/fuel/fuel_outline.png");

    public FuelSlot(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean canHold(@NotNull ItemStack itemStack) {
        return CustomItem.getItem(itemStack) == CustomItems.FUEL;
    }

    @Override
    public void onSetup(RenderSetupContext context) throws IOException {
        super.onSetup(context);
        context.getBackground().drawImage(FUEL_OUTLINE, 1, 1);
    }

    @Override
    public Slot.State createState() {
        return new FuelSlot.State();
    }

    @Override
    public FuelSlot.State get(CustomGui gui) {
        return (FuelSlot.State) super.get(gui);
    }

    public class State extends Slot.State {
        private Consumer<HumanEntity> onFuel;

        @Override
        public void onChange(HumanEntity player) {
            ItemStack itemStack = getItemStack();
            if (itemStack != null && this.onFuel != null) {
                while (itemStack.getAmount() > 0) {
                    this.onFuel.accept(player);
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }
            }
        }

        public void onFuel(Consumer<HumanEntity> onFuel) {
            this.onFuel = onFuel;
        }
    }
}
