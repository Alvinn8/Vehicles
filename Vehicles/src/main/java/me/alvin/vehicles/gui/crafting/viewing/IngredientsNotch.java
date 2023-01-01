package me.alvin.vehicles.gui.crafting.viewing;

import ca.bkaw.praeter.gui.component.GuiComponent;
import ca.bkaw.praeter.gui.font.RenderSetupContext;
import ca.bkaw.praeter.gui.gui.CustomGui;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.crafting.recipe.RecipeStep;
import me.alvin.vehicles.crafting.recipe.VehicleCraftingRecipe;
import me.alvin.vehicles.vehicle.VehicleType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IngredientsNotch extends GuiComponent {
    public static final NamespacedKey TEXTURE = new NamespacedKey(SVCraftVehicles.getInstance(), "gui/vehicle_crafting_table/ingredients_notch.png");

    public IngredientsNotch(int x, int y) {
        super(x, y, 2, 1);
    }

    @Override
    public void onSetup(RenderSetupContext context) throws IOException {
        context.getBackground().drawImage(TEXTURE, 14, 5);
    }

    @Override
    public GuiComponent.State createState() {
        return new IngredientsNotch.State();
    }

    @Override
    public IngredientsNotch.State get(CustomGui gui) {
        return (IngredientsNotch.State) super.get(gui);
    }

    public class State extends GuiComponent.State {
        public void setVehicleType(VehicleType vehicleType) {
            VehicleCraftingRecipe recipe = vehicleType.getRecipe();
            if (recipe == null) {
                this.setHoverText(List.of());
                return;
            }

            // Total material list
            Map<ItemStack, Integer> map = new LinkedHashMap<>();
            for (RecipeStep step : recipe.steps()) {
                for (ItemStack item : step.items()) {
                    ItemStack base = item.clone();
                    base.setAmount(1);
                    int currentAmount = map.getOrDefault(base, 0);
                    map.put(base, currentAmount + item.getAmount());
                }
            }

            List<Component> text = new ArrayList<>();
            text.add(Component.text("Total items required:", NamedTextColor.WHITE, TextDecoration.UNDERLINED));
            for (Map.Entry<ItemStack, Integer> entry : map.entrySet()) {
                ItemStack item = entry.getKey();
                text.add(Component.text()
                    .append(Component.text(entry.getValue() + "x ", NamedTextColor.YELLOW))
                    .append(Component.translatable(item))
                    .build()
                );
            }
            this.setHoverText(text);
        }
    }
}
