package me.alvin.vehicles.actions;

import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import me.alvin.vehicles.vehicle.perspective.Perspective;
import me.alvin.vehicles.vehicle.seat.PassengerData;
import me.alvin.vehicles.vehicle.seat.Seat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SwitchPerspectiveAction implements VehicleMenuAction {
    public static final SwitchPerspectiveAction INSTANCE = new SwitchPerspectiveAction();

    private SwitchPerspectiveAction() {}

    @Override
    public ItemStack getEntryItem(Vehicle vehicle, Player player) {
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Change Perspective").decoration(TextDecoration.ITALIC, false));

        Seat seat = vehicle.getPassengerSeat(player);
        PassengerData passengerData = vehicle.getPassengerData().get(seat);
        Perspective currentPerspective = passengerData == null ? null : passengerData.getPerspective();

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text()
            .content("- Seat")
            .decoration(TextDecoration.ITALIC, false)
            .color(currentPerspective == null ? NamedTextColor.YELLOW : NamedTextColor.WHITE)
            .build());
        for (Perspective perspective : vehicle.getType().getPerspectives()) {
            lore.add(
                Component.text()
                    .content("- " + perspective.getName())
                    .decoration(TextDecoration.ITALIC, false)
                    .color(currentPerspective == perspective ? NamedTextColor.YELLOW : NamedTextColor.WHITE)
                    .build());
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void onMenuClick(Vehicle vehicle, Player player) {
        Seat seat = vehicle.getPassengerSeat(player);
        PassengerData passengerData = vehicle.getPassengerData().get(seat);

        List<Perspective> perspectives = vehicle.getType().getPerspectives();

        Perspective currentPerspective = passengerData.getPerspective();
        int index = perspectives.indexOf(currentPerspective);

        index++;
        if (index >= perspectives.size()) {
            index = -1;
        }
        Perspective perspective = index == -1 ? null : perspectives.get(index);
        passengerData.setPerspective(perspective);
        vehicle.updateMenuInventory(passengerData.getSeatEntity().getEntity().getInventory(), player);
    }
}
