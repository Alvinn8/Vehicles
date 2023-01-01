package me.alvin.vehicles.gui.crafting.timer;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import me.alvin.vehicles.crafting.VehicleCraftingTable;
import me.alvin.vehicles.crafting.progress.RecipeProgress;
import me.alvin.vehicles.gui.Timer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CreatingStepGui extends CustomGui {
    private static final Timer TIMER = new Timer(0, 2);
    private static final Button CANCEL_BUTTON = new Button("Cancel", 7, 2, 2, 1);

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .title(Component.text("Vehicle crafting table"))
        .height(6)
        .add(TIMER, CANCEL_BUTTON)
        .build();

    private final VehicleCraftingTable table;
    private final RecipeProgress recipeProgress;
    private final long completionTime;

    public CreatingStepGui(VehicleCraftingTable table, long completionTime, RecipeProgress recipeProgress) {
        super(TYPE);
        this.table = table;
        this.recipeProgress = recipeProgress;
        this.completionTime = completionTime;
        this.updateTimer();

        CANCEL_BUTTON.get(this).setHoverText(List.of(
            Component.text("Will send you back to the vehicle"),
            Component.text("selection screen. You will get all"),
            Component.text("of the deposited items back.")
        ));

        CANCEL_BUTTON.get(this).setOnClick(context -> {
            context.playClickSound();
            this.table.stopCrafting();
        });
    }

    public void updateTimer() {
        int ms = (int) (this.completionTime - System.currentTimeMillis());
        int totalSeconds = ms / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        int minutes1 = minutes / 10; // first digit
        int minutes2 = minutes % 10; // second digit

        int seconds1 = seconds / 10; // first digit
        int seconds2 = seconds % 10; // second digit

        TIMER.get(this).set(minutes1, minutes2, seconds1, seconds2);
        TIMER.get(this).setHoverText(List.of(
            Component.text("This step is being assembled."),
            Component.text("It will complete in " + minutes + " " + (minutes == 1 ? "minute" : "minutes") + " and " + seconds + " " + (seconds == 1 ? "second" : "seconds"))
        ));
    }

    @Override
    public void show(Player player) {
        this.updateTimer();
        this.update();
        super.show(player);
    }

    @Override
    public @NotNull Component getTitle() {
        return this.recipeProgress.getStepComponent();
    }
}
