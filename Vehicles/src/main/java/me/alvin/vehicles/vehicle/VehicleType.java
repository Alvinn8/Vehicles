package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.vehicle.collision.VehicleCollisionType;
import me.alvin.vehicles.vehicle.seat.Seat;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents a vehicle type. Will only be constructed once per vehicle type,
 * and new instances of the corresponding {@link Vehicle} will be constructed
 * for each loaded vehicle. The corresponding {@link Vehicle} class can be
 * gotten using {@link #getVehicleClass()}
 */
public class VehicleType {
    private final String id;
    private final Component name;
    private final Class<? extends Vehicle> vehicleClass;
    private final Set<Seat> seats;
    private final Seat driverSeat;
    private final Function<ArmorStand, Vehicle> loadConstructor;
    private final VehicleSpawnConstructorFunction spawnConstructor;
    private final VehicleCollisionType collisionType;
    private final double maxHealth;

    public VehicleType(@NotNull String id,
                       @NotNull Component name,
                       @NotNull Class<? extends Vehicle> vehicleClass,
                       @NotNull Function<ArmorStand, Vehicle> loadConstructor,
                       @NotNull VehicleSpawnConstructorFunction spawnConstructor,
                       @NotNull VehicleCollisionType collisionType,
                       @NotNull Seat driverSeat,
                       @Nullable List<Seat> seats,
                       double maxHealth) {
        this.id = id;
        this.name = name;
        this.vehicleClass = vehicleClass;
        this.loadConstructor = loadConstructor;
        this.spawnConstructor = spawnConstructor;
        Set<Seat> seatSet = new HashSet<>();
        seatSet.add(driverSeat);
        if (seats != null) seatSet.addAll(seats);
        this.seats = seatSet;
        this.driverSeat = driverSeat;
        this.collisionType = collisionType;
        this.maxHealth = maxHealth;
    }

    /**
     * Get the id of this vehicle type. This is the id the vehicle type
     * will be registered as.
     *
     * @return The id of this vehicle type
     */
    @NotNull
    public String getId() {
        return this.id;
    }

    /**
     * Get the name of the vehicle. This will be displayed to
     * players.
     *
     * @return The name component
     */
    public Component getName() {
        return this.name;
    }

    /**
     * Get the corresponding {@link Vehicle} class which will be constructed
     * for each loaded vehicle
     *
     * @return The {@link Vehicle} class for this vehicle type
     */
    @NotNull
    public Class<? extends Vehicle> getVehicleClass() {
        return this.vehicleClass;
    }

    /**
     * Construct the a new instance of the vehicle from
     * an existing entity. Used when loading the vehicle
     *
     * @see Vehicle#Vehicle(ArmorStand)
     */
    public Vehicle construct(ArmorStand entity) {
        return this.loadConstructor.apply(entity);
    }

    /**
     * Spawn a new vehicle and get the instance of that vehicle.
     * Used when new vehicles are spawned in to the world by
     * a player.
     *
     * @see Vehicle#Vehicle(Location, Player, VehicleSpawnReason)
     */
    public Vehicle construct(Location location, Player creator, VehicleSpawnReason reason) {
        return this.spawnConstructor.apply(location, creator, reason);
    }

    /**
     * Get the seats for the vehicle, including the driver seat
     *
     * @return A set of seats
     */
    @NotNull
    public Set<Seat> getSeats() {
        return this.seats;
    }

    /**
     * Get the seat that will allow the passenger to drive the vehicle
     *
     * @return The driver seat
     */
    @NotNull
    public Seat getDriverSeat() {
        return this.driverSeat;
    }

    /**
     * Get the collision type the vehicle will use for
     * collision checks.
     *
     * @return The collision type
     */
    public VehicleCollisionType getCollisionType() {
        return this.collisionType;
    }

    /**
     * Get the max health the vehicle can have, is also the
     * health the vehicle starts at.
     *
     * @return The max health
     */
    public double getMaxHealth() {
        return this.maxHealth;
    }
}
