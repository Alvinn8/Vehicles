package me.alvin.vehicles.gui.health;

import ca.bkaw.praeter.gui.GuiUtils;
import ca.bkaw.praeter.gui.component.GuiComponent;
import ca.bkaw.praeter.gui.font.RenderSetupContext;
import ca.bkaw.praeter.gui.gui.CustomGui;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.vehicle.Vehicle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class HealthIcon extends GuiComponent {
    public static final NamespacedKey TEXTURE = new NamespacedKey(SVCraftVehicles.getInstance(), "gui/vehicle_menu_icon/health.png");

    public HealthIcon(int x, int y) {
        super(x, y, 1, 1);
    }

    @Override
    public void onSetup(RenderSetupContext context) throws IOException {
        context.getBackground().drawImage(TEXTURE, 1, 1);
    }

    @Override
    public GuiComponent.State createState() {
        return new HealthIcon.State();
    }

    @Override
    public HealthIcon.State get(CustomGui gui) {
        return (HealthIcon.State) super.get(gui);
    }

    public class State extends GuiComponent.State {
        private Vehicle vehicle;

        @Override
        public void renderItems(Inventory inventory) {
            if (vehicle == null) {
                return;
            }
            int health = (int) Math.ceil(vehicle.getHealth());
            int maxHealth = (int) Math.ceil(vehicle.getType().getMaxHealth());

            NamedTextColor color;
            if (health <= maxHealth / 4) color = NamedTextColor.RED;
            else if (health <= maxHealth / 2) color = NamedTextColor.YELLOW;
            else color = NamedTextColor.WHITE;

            TextComponent.Builder component = Component.text()
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.WHITE)
                .append(Component.text(health, color))
                .append(Component.text(" / " + maxHealth));

            ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/transparent");
            item.editMeta(meta -> meta.displayName(component.build()));
            int slot = GuiUtils.getSlot(x, y);
            inventory.setItem(slot, item);
        }

        public void setVehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
    }
}
