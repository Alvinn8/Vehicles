package me.alvin.vehicles.vehicle.seat;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.util.ni.NIE;
import me.alvin.vehicles.vehicle.Vehicle;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents data about a seat in a vehicle
 */
public class SeatData {
    private final LivingEntity passenger;
    private final NIE<Mule> riderEntity;
    private final long enteredAt;

    public static final double RIDER_ENTITY_Y_OFFSET = 1.0D;

    public SeatData(@NotNull Vehicle vehicle, @NotNull Seat seat, @NotNull LivingEntity passenger) {
        DebugUtil.debug("Constructing seat data");
        Location location = seat.getRelativePos().relativeTo(vehicle.getLocation(), vehicle.getRoll());
        location.subtract(0, RIDER_ENTITY_Y_OFFSET, 0);

        Mule spawnedMule = SVCraftVehicles.getInstance().getNMS().spawnSeatEntity(location, mule -> {
            mule.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0, false, false));
            mule.setTamed(true);
            mule.setAI(false);
            mule.setCarryingChest(true);
            mule.setInvulnerable(true);
            mule.setSilent(true);
            mule.customName(vehicle.getType().getName());
        });
        spawnedMule.addPassenger(passenger);
        if (passenger instanceof Player) {
            vehicle.updateMenuInventory(spawnedMule.getInventory(), (Player) passenger);
        }

        this.riderEntity = new NIE<>(spawnedMule);
        this.passenger = passenger;
        this.enteredAt = System.currentTimeMillis(); // They entered now

        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.riderEntity.getEntity(), vehicle);
        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.riderEntity.getAreaEffectCloud(), vehicle);
    }

    @NotNull
    public LivingEntity getPassenger() {
        return this.passenger;
    }

    @NotNull
    public NIE<Mule> getRiderEntity() {
        return this.riderEntity;
    }

    /**
     * Get the time in milliseconds when the player sat in this seatdata.
     *
     * @return The time in milliseconds
     */
    public long getTimeEntered() {
        return this.enteredAt;
    }

    /**
     * Make the entity exit the seat and remove the rider armor stand.
     */
    public void exitSeat() {
        DebugUtil.debug("Exiting and removing seat");
        if (this.passenger.getVehicle() == this.riderEntity.getEntity()) {
            SVCraftVehicles.getInstance().getCurrentVehicleMap().remove(this.passenger);
            DebugUtil.debug("Removing "+ this.passenger.getName() + " from current vehicles");
            this.passenger.leaveVehicle();
        }
        SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.riderEntity.getEntity());
        SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.riderEntity.getAreaEffectCloud());
        this.riderEntity.remove();
    }

    /**
     * @return {@code true} if the seat data is valid and the passenger is still in this seat
     *         {@code false} if the player has left the seat or the rider entity is invalid.
     * @see Entity#isValid()
     */
    public boolean isValid() {
        if (!this.riderEntity.isValid()) return false;

        return this.passenger.getVehicle() == this.riderEntity.getEntity();
    }
}
