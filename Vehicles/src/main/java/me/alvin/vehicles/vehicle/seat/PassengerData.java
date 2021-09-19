package me.alvin.vehicles.vehicle.seat;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.util.ni.NIE;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.perspective.Perspective;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data about a passenger in a seat in a vehicle.
 * <p>
 * Also creates and manages the entities the player sits in.
 */
public class PassengerData {
    private final LivingEntity passenger;
    private final long enteredAt;
    private final NIE<Mule> seatEntity;
    private @Nullable Perspective perspective;
    private @Nullable NIE<Mule> cameraEntity;

    public static final double SEAT_ENTITY_Y_OFFSET = 1.0D;

    public PassengerData(@NotNull Vehicle vehicle, @NotNull Seat seat, @NotNull LivingEntity passenger) {
        DebugUtil.debug("Constructing seat data");
        Location location = seat.getRelativePos().relativeTo(vehicle.getLocation(), vehicle.getRoll());
        location.subtract(0, SEAT_ENTITY_Y_OFFSET, 0);

        this.passenger = passenger;
        this.enteredAt = System.currentTimeMillis(); // They entered now
        this.seatEntity = this.spawnSeatEntity(location, vehicle);

        this.seatEntity.getEntity().addPassenger(this.passenger);

        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.seatEntity.getEntity(), vehicle);
        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.seatEntity.getAreaEffectCloud(), vehicle);
    }

    private NIE<Mule> spawnSeatEntity(Location location, Vehicle vehicle) {
        Mule spawnedMule = SVCraftVehicles.getInstance().getNMS().spawnSeatEntity(location, mule -> {
            mule.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0, false, false));
            mule.setTamed(true);
            mule.setAI(false);
            mule.setCarryingChest(true);
            mule.setInvulnerable(true);
            mule.setSilent(true);
            mule.customName(vehicle.getType().getName());
        });
        if (this.passenger instanceof Player) {
            vehicle.updateMenuInventory(spawnedMule.getInventory(), (Player) this.passenger);
        }

        return new NIE<>(spawnedMule);
    }

    @NotNull
    public LivingEntity getPassenger() {
        return this.passenger;
    }

    @NotNull
    public NIE<Mule> getSeatEntity() {
        return this.seatEntity;
    }

    @Nullable
    public Perspective getPerspective() {
        return this.perspective;
    }

    public void setPerspective(@Nullable Perspective perspective) {
        this.perspective = perspective;

        if (perspective == null && this.cameraEntity != null) {
            // Make the player ride the seat entity again. This will only be called if
            // the passenger is a player, so we can cast.
            SVCraftVehicles.getInstance().getNMS().setClientSidePassenger((Player) this.passenger, this.seatEntity.getEntity());

            this.cameraEntity.remove();
            this.cameraEntity = null;
        }
    }

    @Nullable
    public NIE<Mule> getCameraEntity() {
        return this.cameraEntity;
    }

    public NIE<Mule> createCameraEntity(Location location, Vehicle vehicle) {
        this.cameraEntity = this.spawnSeatEntity(location, vehicle);

        // Make the player ride the camera entity, but it's only visible for them
        // This method will only be called in Vehicle if the passenger is a player,
        // so we can cast
        SVCraftVehicles.getInstance().getNMS().setClientSidePassenger((Player) this.passenger, this.cameraEntity.getEntity());

        return this.cameraEntity;
    }

    /**
     * Get the time in milliseconds when the player entered this seat.
     *
     * @return The time in milliseconds
     */
    public long getTimeEntered() {
        return this.enteredAt;
    }

    /**
     * Make the entity exit the seat and remove the seat and camera entities.
     */
    public void exitSeat() {
        DebugUtil.debug("Exiting and removing seat");
        if (this.passenger.getVehicle() == this.seatEntity.getEntity()) {
            SVCraftVehicles.getInstance().getCurrentVehicleMap().remove(this.passenger);
            DebugUtil.debug("Removing "+ this.passenger.getName() + " from current vehicles");
            this.passenger.leaveVehicle();
        }
        SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.seatEntity.getEntity());
        SVCraftVehicles.getInstance().getVehiclePartMap().remove(this.seatEntity.getAreaEffectCloud());
        this.seatEntity.remove();
        if (this.cameraEntity != null) {
            this.cameraEntity.remove();
        }
    }

    /**
     * @return {@code true} if the seat data is valid and the passenger is still in this seat
     *         {@code false} if the player has left the seat or the rider entity is invalid.
     * @see Entity#isValid()
     */
    public boolean isValid() {
        if (!this.seatEntity.isValid()) return false;

        // To the server, and therefore to all other players, the passenger is always
        // inside the seat entity, only for the client side are they placed in the
        // camera entity. This means this check will always work even if it looks like
        // the player is in the camera entity.
        return this.passenger.getVehicle() == this.seatEntity.getEntity();
    }
}
