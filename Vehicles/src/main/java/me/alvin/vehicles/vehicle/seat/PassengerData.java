package me.alvin.vehicles.vehicle.seat;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.util.ni.NIE;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.perspective.Perspective;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
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
    private @Nullable Mule cameraEntity;
    private @Nullable NIE<Mule> niCameraEntity;
    private @Nullable ArmorStand iCameraEntityBase; // interpolating (i) camera-entity armor stand (base)

    public static final double SEAT_ENTITY_Y_OFFSET = 1.0D;

    public PassengerData(@NotNull Vehicle vehicle, @NotNull Seat seat, @NotNull LivingEntity passenger) {
        DebugUtil.debug("Constructing seat data");
        Location location = seat.getRelativePos().relativeTo(vehicle.getLocation(), vehicle.getRoll());
        location.subtract(0, SEAT_ENTITY_Y_OFFSET, 0);

        this.passenger = passenger;
        this.enteredAt = System.currentTimeMillis(); // They entered now
        this.seatEntity = new NIE<>(this.spawnSeatEntity(location, vehicle));

        this.seatEntity.getEntity().addPassenger(this.passenger);

        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.seatEntity.getEntity(), vehicle);
        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.seatEntity.getAreaEffectCloud(), vehicle);
    }

    @NotNull
    private Mule spawnSeatEntity(Location location, Vehicle vehicle) {
        Mule spawnedMule = SVCraftVehicles.getInstance().getNMS().spawnSeatEntity(location, mule -> {
            mule.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0, false, false));
            mule.setTamed(true);
            mule.setAI(false);
            mule.setCarryingChest(true);
            mule.setInvulnerable(true);
            mule.setSilent(true);
            mule.customName(vehicle.getType().getName());
            mule.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
            mule.setHealth((vehicle.getHealth() / vehicle.getType().getMaxHealth()) * 20);
        });
        if (this.passenger instanceof Player) {
            vehicle.updateMenuInventory(spawnedMule.getInventory(), (Player) this.passenger);
        }

        return spawnedMule;
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

            this.removeCameraEntity();
        }
    }

    public Mule getCameraEntity() {
        return this.cameraEntity;
    }

    private void removeCameraEntity() {
        if (this.cameraEntity != null) {
            this.cameraEntity.remove();
            this.cameraEntity = null;
        }
        if (this.niCameraEntity != null) {
            this.niCameraEntity.remove();
            this.niCameraEntity = null;
        }
        if (this.iCameraEntityBase != null) {
            this.iCameraEntityBase.remove();
            this.iCameraEntityBase = null;
        }
    }

    public void updateCamera(Vehicle vehicle, Player player) {
        if (this.perspective == null) return;

        Location cameraLocation = this.perspective.getCameraLocation(vehicle, player);

        if (this.cameraEntity != null && (
            (this.perspective.interpolate() && this.iCameraEntityBase == null) ||
            (!this.perspective.interpolate() && this.niCameraEntity == null))) {
            // The current interpolation state and the perspective's one does not match.
            // Let's remove the camera and the entities will be respawned by the block
            // below, and in the right interpolation state.
            DebugUtil.debug("Mismatched interpolation state, respawning the camera");
            this.removeCameraEntity();
        }

        if (this.cameraEntity == null) {
            // No camera entity present, we need to create it
            this.cameraEntity = this.spawnSeatEntity(cameraLocation, vehicle);

            // Make the player ride the camera entity, but it's only visible for them
            SVCraftVehicles.getInstance().getNMS().setClientSidePassenger(player, this.cameraEntity);

            if (this.perspective.interpolate()) {
                // interpolating
                cameraLocation.subtract(0, 1, 0);
                this.iCameraEntityBase = cameraLocation.getWorld().spawn(cameraLocation, ArmorStand.class);
                Vehicle.setupArmorStand(this.iCameraEntityBase);
                this.iCameraEntityBase.addPassenger(this.cameraEntity);
            } else {
                // non-interpolating
                this.niCameraEntity = new NIE<>(this.cameraEntity);
            }
        }

        if (this.perspective.interpolate()) {
            // interpolating
            SVCraftVehicles.getInstance().getNMS().setEntityLocation(this.iCameraEntityBase, cameraLocation.getX(), cameraLocation.getY(), cameraLocation.getZ(), 0, 0);
        } else {
            // non-interpolating
            this.niCameraEntity.setLocation(cameraLocation.getX(), cameraLocation.getY(), cameraLocation.getZ(), 0, 0);
        }
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

    public void setHealth(double vehicleHealth, double maxHealth) {
        double health = (vehicleHealth / maxHealth) * 20;
        if (health <= 0) health = 1;
        this.seatEntity.getEntity().setHealth(health);
        if (this.niCameraEntity != null) {
            this.niCameraEntity.getEntity().setHealth(health);
        }
    }
}
