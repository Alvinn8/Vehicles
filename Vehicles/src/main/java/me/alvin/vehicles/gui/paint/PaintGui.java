package me.alvin.vehicles.gui.paint;

import ca.bkaw.praeter.gui.components.SlotGroup;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PaintGui extends CustomGui {
    private static final SlotGroup<DyeSlot> SLOTS = new SlotGroup<>(0, 0, 8, 1, DyeSlot::new);
    private static final ColorPreview COLOR_PREVIEW = new ColorPreview(8, 0);

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .title(Component.text("Add dyes to mix"))
        .height(1)
        .add(SLOTS, COLOR_PREVIEW)
        .build();

    private final Consumer<Color> closeHandler;

    public PaintGui(Consumer<Color> closeHandler) {
        super(TYPE);
        this.closeHandler = closeHandler;
    }

    @Override
    public void update() {
        Color color = this.getColor();
        COLOR_PREVIEW.get(this).setColor(color);

        super.update();
    }

    @Override
    public void onClose(Player player, InventoryCloseEvent event) {
        this.closeHandler.accept(this.getColor());
    }

    @Nullable
    public Color getColor() {
        List<DyeColor> dyes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            ItemStack itemStack = SLOTS.getSlot(i).get(this).getItemStack();
            if (itemStack == null) {
                continue;
            }
            DyeColor dyeColor = this.getDyeColor(itemStack.getType());
            if (dyeColor != null) {
                dyes.add(dyeColor);
            }
        }
        if (dyes.isEmpty()) {
            return null;
        }
        DyeColor first = dyes.get(0);
        List<DyeColor> rest = dyes.subList(1, dyes.size());

        return first.getColor().mixDyes(rest.toArray(new DyeColor[0]));
    }

    private DyeColor getDyeColor(Material material) {
        String name = material.name();
        return DyeColor.valueOf(name.substring(0, name.length() - "_DYE".length()));
    }
}
