package me.alvin.vehicles.vehicle.seat;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.util.ni.NIArmorStand;
import me.alvin.vehicles.vehicle.Vehicle;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Represents data about a seat in a vehicle
 */
public class SeatData {
    private final LivingEntity passenger;
    private final NIArmorStand riderEntity;

    public SeatData(@NotNull Vehicle vehicle, @NotNull Seat seat, @NotNull LivingEntity passenger) {
        DebugUtil.debug("Constructing seat data");
        Location location = seat.getRelativePos().relativeTo(vehicle.getLocation());
        this.riderEntity = new NIArmorStand(location);
        Vehicle.setupArmorStand(this.riderEntity.getArmorStand());
        this.riderEntity.getArmorStand().addPassenger(passenger);
        this.passenger = passenger;
    }

    @NotNull
    public LivingEntity getPassenger() {
        return this.passenger;
    }

    @NotNull
    public NIArmorStand getRiderEntity() {
        return this.riderEntity;
    }

    /**
     * Make the entity exit the seat and remove the rider armor stand.
     */
    public void exitSeat() {
        DebugUtil.debug("Exiting and removing seat");
        if (this.passenger.getVehicle() == this.riderEntity) {
            SVCraftVehicles.getInstance().getCurrentVehicleMap().remove(this.passenger);
            DebugUtil.debug("Removing "+ this.passenger.getName() + " from current vehicles");
            this.passenger.leaveVehicle();
        }
        this.riderEntity.remove();
    }

    /**
     * @return {@code true} if the seat data is valid and the passenger is still in this seat
     *         {@code false} if the player has left the seat or the rider entity is invalid.
     * @see Entity#isValid()
     */
    public boolean isValid() {
        if (!this.riderEntity.isValid()) return false;

        return this.passenger.getVehicle() == this.riderEntity.getArmorStand();
    }
}
