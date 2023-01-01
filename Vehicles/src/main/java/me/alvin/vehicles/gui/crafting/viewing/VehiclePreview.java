package me.alvin.vehicles.gui.crafting.viewing;

import ca.bkaw.praeter.core.ItemUtils;
import ca.bkaw.praeter.gui.GuiUtils;
import ca.bkaw.praeter.gui.component.GuiComponent;
import ca.bkaw.praeter.gui.components.Panel;
import ca.bkaw.praeter.gui.gui.CustomGui;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.vehicle.VehicleType;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.List;

public class VehiclePreview extends Panel {
    public VehiclePreview(int x, int y) {
        super(x, y, 4, 4);
    }

    @Override
    public GuiComponent.State createState() {
        return new VehiclePreview.State();
    }

    @Override
    public VehiclePreview.State get(CustomGui gui) {
        return (VehiclePreview.State) super.get(gui);
    }

    public class State extends GuiComponent.State {
        private VehicleType vehicleType;

        @Override
        public void renderItems(Inventory inventory) {
            super.renderItems(inventory);
            if (vehicleType == null) return;

            ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem(this.vehicleType.getLargePreviewModel().toString());
            item.editMeta(LeatherArmorMeta.class, meta -> {
                meta.setColor(Color.WHITE);
                meta.addItemFlags(ItemFlag.HIDE_DYE);
            });
            ItemUtils.setItemText(item, List.of(Component.empty()));

            inventory.setItem(GuiUtils.getSlot(x + 3, y + 3), item);
        }

        public void setVehicleType(VehicleType vehicleType) {
            this.vehicleType = vehicleType;
        }
    }
}
