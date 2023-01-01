package me.alvin.vehicles.gui.crafting.viewing;

import ca.bkaw.praeter.gui.components.DisableableButton;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import me.alvin.vehicles.crafting.VehicleCraftingTable;
import me.alvin.vehicles.vehicle.VehicleType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The gui where the player can view details about a vehicle, and choose to start
 * crafting it.
 */
public class ViewingGui extends CustomGui {
    private static final IngredientsNotch INGREDIENTS_NOTCH = new IngredientsNotch(2, 0);
    private static final VehiclePreview PREVIEW = new VehiclePreview(1, 1);
    private static final DisableableButton CRAFT_BUTTON = new DisableableButton("Craft", 7, 1, 2, 1);
    private static final DisableableButton CANCEL_BUTTON = new DisableableButton("Cancel", 7, 4, 2, 1);

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(6)
        .add(INGREDIENTS_NOTCH, PREVIEW, CRAFT_BUTTON, CANCEL_BUTTON)
        .build();

    private final VehicleCraftingTable table;
    private final VehicleType vehicleType;

    public ViewingGui(VehicleCraftingTable table, VehicleType vehicleType) {
        super(TYPE);
        this.table = table;
        this.vehicleType = vehicleType;

        INGREDIENTS_NOTCH.get(this).setVehicleType(vehicleType);
        PREVIEW.get(this).setVehicleType(vehicleType);

        boolean vehicleFits = this.table.doesVehicleFit();

        CRAFT_BUTTON.get(this).setEnabled(vehicleFits);
        if (vehicleFits) {
            CRAFT_BUTTON.get(this).setHoverText(List.of(
                Component.text("Start crafting the vehicle")
            ));
        } else {
            CRAFT_BUTTON.get(this).setHoverText(List.of(
                Component.text("Please make room above the crafting"),
                Component.text("table so that the vehicle fits.")
            ));
        }

        CANCEL_BUTTON.get(this).setOnClick(context -> {
            context.playClickSound();
            this.table.stopViewing();
        });

        CRAFT_BUTTON.get(this).setOnClick(context ->
            this.table.startCrafting(context.getPlayer())
        );
    }

    @Override
    @NotNull
    public Component getTitle() {
        return this.vehicleType.getName();
    }
}
