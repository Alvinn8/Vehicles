package me.alvin.vehicles.gui.fuel;

import ca.bkaw.praeter.core.resources.font.FontSequence;
import ca.bkaw.praeter.gui.GuiUtils;
import ca.bkaw.praeter.gui.component.GuiComponent;
import ca.bkaw.praeter.gui.font.RenderDispatcher;
import ca.bkaw.praeter.gui.font.RenderSetupContext;
import ca.bkaw.praeter.gui.gui.CustomGui;
import me.alvin.vehicles.SVCraftVehicles;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FuelBar extends GuiComponent {
    public static final NamespacedKey FUEL_BAR_TEXTURE
        = new NamespacedKey(SVCraftVehicles.getInstance(), "gui/fuel/fuel_bar.png");

    public static final NamespacedKey FUEL_CONNECTOR_TEXTURE
        = new NamespacedKey(SVCraftVehicles.getInstance(), "gui/fuel/fuel_connection.png");

    public static final int FULL_BAR_WIDTH = 7 * GuiUtils.SLOT_SIZE;
    private static final int ACCURACY = 1;

    private FontSequence offsetX;
    private FontSequence offsetXReverse;
    private FontSequence shiftRightOne;
    private FontSequence shiftLeftOne;
    private FontSequence filled;

    public FuelBar(int y) {
        super(0, y, 9, 1);
    }

    @Override
    public void onSetup(RenderSetupContext context) throws IOException {
        context.getBackground().drawImage(FUEL_BAR_TEXTURE, 0, -1);

        context.getBackground().drawImage(FUEL_CONNECTOR_TEXTURE, 2 * 18 + (18 - 6) / 2, 17);

        this.offsetX = context.newFontSequence().shiftRight(24).build();
        this.offsetXReverse = context.newFontSequence().shiftLeft(24).build();

        this.shiftRightOne = context.newFontSequence().shiftRight(ACCURACY).build();
        this.shiftLeftOne = context.newFontSequence().shiftLeft(ACCURACY).build();

        BufferedImage image = new BufferedImage(ACCURACY, 17, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 1, ACCURACY, 17);
        this.filled = context.newFontSequence()
            .drawImage(image, 0, 0)
            .build();
    }

    @Override
    public GuiComponent.State createState() {
        return new State();
    }

    @Override
    public FuelBar.State get(CustomGui gui) {
        return (FuelBar.State) super.get(gui);
    }

    public class State extends GuiComponent.State {
        private float percentage;

        @Override
        public void renderItems(Inventory inventory) {
            ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/transparent");
            item.editMeta(meta ->
                meta.displayName(
                    Component.text(Math.round(this.percentage * 100) + "%", NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false)
                )
            );
            GuiUtils.forEachSlot(FuelBar.this, slot ->
                inventory.setItem(slot, item)
            );
        }

        @Override
        public void onRender(RenderDispatcher renderDispatcher) {
            renderDispatcher.render(offsetX);
            float percentage = this.percentage;
            if (percentage > 1) {
                percentage = 1;
            }
            int count = (int) (percentage * (FULL_BAR_WIDTH / ACCURACY));
            for (int i = 0; i < count; i++) {
                renderDispatcher.render(filled);
                renderDispatcher.render(shiftRightOne);
            }
            // Shift back, so we don't cause an offset to other components
            for (int i = 0; i < count; i++) {
                renderDispatcher.render(shiftLeftOne);
            }
            renderDispatcher.render(offsetXReverse);
        }

        public void setPercentage(float percentage) {
            this.percentage = percentage;
        }

        public float getPercentage() {
            return percentage;
        }
    }
}