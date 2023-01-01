package me.alvin.vehicles.gui.crafting.step;

import ca.bkaw.praeter.core.ItemUtils;
import ca.bkaw.praeter.gui.components.DisableableButton;
import ca.bkaw.praeter.gui.components.SlotGroup;
import ca.bkaw.praeter.gui.components.StaticSlot;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import me.alvin.vehicles.crafting.VehicleCraftingTable;
import me.alvin.vehicles.crafting.progress.RecipeProgress;
import me.alvin.vehicles.crafting.recipe.RecipeStep;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StepGui extends CustomGui {
    private static final SlotGroup<StaticSlot> SLOTS = SlotGroup.box(1, 1, 4, 4, StaticSlot::new);
    private static final DisableableButton CRAFT_BUTTON = new DisableableButton("Craft", 7, 1, 2, 1);
    private static final DisableableButton CANCEL_BUTTON = new DisableableButton("Cancel", 7, 4, 2, 1);

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(6)
        .add(SLOTS, CRAFT_BUTTON, CANCEL_BUTTON)
        .build();

    private final VehicleCraftingTable table;
    private final RecipeProgress progress;

    public StepGui(VehicleCraftingTable table, RecipeProgress progress) {
        super(TYPE);
        this.table = table;
        this.progress = progress;

        CANCEL_BUTTON.get(this).setHoverText(List.of(
            Component.text("Will send you back to the vehicle"),
            Component.text("selection screen. You will get all"),
            Component.text("of the deposited items back.")
        ));

        RecipeStep step = this.progress.getCurrentStep();
        int slot = 0;
        for (ItemStack item : step.items()) {
            ItemStack renderItem = item.clone();
            ItemUtils.setItemText(renderItem, List.of(
                Component.text()
                    .append(Component.text(item.getAmount() + "x "))
                    .append(Component.translatable(item))
                    .build()
            ));
            SLOTS.getSlot(slot++).get(this).setItemStack(renderItem);

            // If we have more than 64 items we need to split it into
            // multiple stacks
            if (item.getAmount() > 64) {
                int amount = item.getAmount() - 64;
                while (amount > 0) {
                    ItemStack renderItem2 = renderItem.clone();
                    if (amount > 64) {
                        // Need yet another stack
                        renderItem2.setAmount(64);
                        amount -= 64;
                    } else {
                        // We can consume the rest here
                        renderItem2.setAmount(amount);
                        amount = 0; // amount -= amount
                    }
                    SLOTS.getSlot(slot++).get(this).setItemStack(renderItem2);
                }
            }
        }

        CANCEL_BUTTON.get(this).setOnClick(context -> {
            context.playClickSound();
            this.table.stopCrafting();
        });

        CRAFT_BUTTON.get(this).setOnClick(context -> {
            RecipeStep currentStep = progress.getCurrentStep();
            Player player = context.getPlayer();
            if (currentStep.canComplete(player)) {
                context.playClickSound();

                // Take the items
                currentStep.takeItems(player);

                this.table.startTimer(System.currentTimeMillis() + currentStep.completeTime());
            }
        });
    }

    public void updateCraftButton(Player player) {
        RecipeStep step = this.progress.getCurrentStep();
        boolean canComplete = player != null && step.canComplete(player);

        CRAFT_BUTTON.get(this).setEnabled(canComplete);
        if (canComplete) {
            CRAFT_BUTTON.get(this).setHoverText(List.of(
                Component.text("Complete step")
            ));
        } else {
            CRAFT_BUTTON.get(this).setHoverText(List.of(
                Component.text("You do not have all the required items")
            ));
        }
    }

    @Override
    public void show(Player player) {
        this.updateCraftButton(player);
        update();
        super.show(player);
    }

    @Override
    @NotNull
    public Component getTitle() {
        return this.progress.getStepComponent();
    }
}
