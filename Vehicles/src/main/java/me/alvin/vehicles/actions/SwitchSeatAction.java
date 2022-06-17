package me.alvin.vehicles.actions;

import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import me.alvin.vehicles.vehicle.seat.Seat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SwitchSeatAction implements VehicleMenuAction {
    public static final SwitchSeatAction INSTANCE = new SwitchSeatAction();

    private SwitchSeatAction() {}

    @Override
    public ItemStack getEntryItem(Vehicle vehicle, Player player) {
        return new ItemStack(Material.SADDLE);
    }

    @Override
    public void onMenuClick(Vehicle vehicle, Player player) {
        List<Seat> seats = new ArrayList<>(vehicle.getType().getSeats());
        if (vehicle.getPassengerData().size() >= seats.size()) {
            player.sendMessage("The vehicle is full!");
            return;
        }
        Seat currentSeat = vehicle.getPassengerSeat(player);
        if (currentSeat == null) return; // Should never happen
        int index = seats.indexOf(currentSeat);
        for (int i = 0; i < seats.size(); i++) {
            index++;
            if (index >= seats.size()) {
                index = 0;
            }
            Seat seat = seats.get(index);
            if (vehicle.getPassenger(seat) == null) {
                vehicle.setPassenger(seat, player);
                vehicle.setPassenger(currentSeat, null);
                return;
            }
        }
    }
}
