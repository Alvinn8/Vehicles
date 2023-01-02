package me.alvin.vehicles.gui.paint;

import ca.bkaw.praeter.core.ItemUtils;
import ca.bkaw.praeter.gui.GuiUtils;
import ca.bkaw.praeter.gui.component.GuiComponent;
import ca.bkaw.praeter.gui.gui.CustomGui;
import me.alvin.vehicles.SVCraftVehicles;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.List;

public class ColorPreview extends GuiComponent {
    public ColorPreview(int x, int y) {
        super(x, y, 1, 1);
    }

    @Override
    public GuiComponent.State createState() {
        return new ColorPreview.State();
    }

    @Override
    public ColorPreview.State get(CustomGui gui) {
        return (ColorPreview.State) super.get(gui);
    }

    public class State extends GuiComponent.State {
        private Color color;

        @Override
        public void renderItems(Inventory inventory) {
            if (this.color == null) {
                return;
            }
            ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/color_preview");
            item.editMeta(LeatherArmorMeta.class, meta -> {
                meta.setColor(this.color);
                meta.addItemFlags(ItemFlag.HIDE_DYE);
            });
            ItemUtils.setItemText(item, List.of(
                Component.text("Color preview")
            ));
            inventory.setItem(GuiUtils.getSlot(x, y), item);
        }

        public void setColor(Color color) {
            this.color = color;
        }
    }
}
