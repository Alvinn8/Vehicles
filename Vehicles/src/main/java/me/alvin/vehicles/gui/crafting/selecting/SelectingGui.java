package me.alvin.vehicles.gui.crafting.selecting;

import ca.bkaw.praeter.core.ItemUtils;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.components.SlotGroup;
import ca.bkaw.praeter.gui.components.StaticSlot;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.crafting.VehicleCraftingTable;
import me.alvin.vehicles.vehicle.VehicleType;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.List;

/**
 * The gui where the player selects which vehicle to craft.
 */
public class SelectingGui extends CustomGui {
    private static final SlotGroup<StaticSlot> SLOTS = SlotGroup.box(1, 1, 7, 5, StaticSlot::new);

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .title(Component.text("Select a vehicle to craft"))
        .height(6)
        .add(SLOTS)
        .build();

    public SelectingGui(VehicleCraftingTable table) {
        super(TYPE);

        int slotIndex = 0;
        for (VehicleType vehicleType : SVCraftVehicles.getInstance().getRegistry().getMap().values()) {
            if (vehicleType.getRecipe() == null) continue;

            ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem(vehicleType.getPreviewModel().toString());
            item.editMeta(LeatherArmorMeta.class, meta -> {
                meta.setColor(Color.WHITE);
                meta.addItemFlags(ItemFlag.HIDE_DYE);
            });
            ItemUtils.setItemText(item, List.of(vehicleType.getName()));
            Slot.State slot = SLOTS.getSlot(slotIndex++).get(this);
            slot.setItemStack(item);
            slot.setOnClick(context -> {
                context.playClickSound();
                table.viewVehicleType(vehicleType, context.getPlayer());
            });
        }
    }
}
