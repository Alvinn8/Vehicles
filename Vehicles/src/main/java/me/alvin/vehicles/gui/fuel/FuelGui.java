package me.alvin.vehicles.gui.fuel;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.item.FuelItem;
import me.alvin.vehicles.vehicle.Vehicle;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class FuelGui extends CustomGui {
    public static final FuelSlot FUEL_SLOT = new FuelSlot(2, 2);
    public static final FuelBar FUEL_BAR = new FuelBar(0);

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .title(Component.text("Fuel"))
        .height(3)
        .add(FUEL_SLOT, FUEL_BAR)
        .build();

    private final Vehicle vehicle;

    public FuelGui(Vehicle vehicle) {
        super(TYPE);
        this.vehicle = vehicle;
        this.setPercentage(this.calculatePercentage());
        FUEL_SLOT.get(this).onFuel(player -> {
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, SoundCategory.PLAYERS, 1, 1);
            vehicle.setCurrentFuel(vehicle.getCurrentFuel() + FuelItem.FUEL_AMOUNT);
            setPercentage(calculatePercentage());
            update();
        });
        new FuelGuiTask(this).runTaskTimer(SVCraftVehicles.getInstance(), 20L, 20L);
    }

    public float calculatePercentage() {
        return vehicle.getMaxFuel() != 0 ?
            (float) vehicle.getCurrentFuel() / vehicle.getMaxFuel()
            : 0;
    }

    public void setPercentage(float percentage) {
        FUEL_BAR.get(this).setPercentage(percentage);
    }

    public float getPercentage() {
        return FUEL_BAR.get(this).getPercentage();
    }
}
