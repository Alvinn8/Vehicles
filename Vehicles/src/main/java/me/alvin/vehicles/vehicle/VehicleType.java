package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.action.VehicleAction;
import me.alvin.vehicles.vehicle.seat.Seat;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a vehicle type. Will only be constructed once per vehicle type,
 * and new instances of the corresponding {@link Vehicle} will be constructed
 * for each loaded vehicle. The corresponding {@link Vehicle} class can be
 * gotten using {@link #getVehicleClass()}
 */
public class VehicleType {
    private final String id;
    private final Class<? extends Vehicle> vehicleClass;
    private final Set<Seat> seats;
    private final Seat driverSeat;
    private final Function<ArmorStand, Vehicle> loadConstructor;
    private final BiFunction<Location, Player, Vehicle> spawnConstructor;
    private final List<Wheel> wheels;
    private final List<RelativePos> gravityPoints;

    public VehicleType(@NotNull String id,
                       @NotNull Class<? extends Vehicle> vehicleClass,
                       @NotNull Function<ArmorStand, Vehicle> loadConstructor,
                       @NotNull BiFunction<Location, Player, Vehicle> spawnConstructor,
                       @NotNull Seat driverSeat,
                       @Nullable List<Seat> seats,
                       @Nullable List<Wheel> wheels,
                       @Nullable List<RelativePos> extraGravityPoints) {
        this.id = id;
        this.vehicleClass = vehicleClass;
        this.loadConstructor = loadConstructor;
        this.spawnConstructor = spawnConstructor;
        Set<Seat> seatSet = new HashSet<>();
        seatSet.add(driverSeat);
        if (seats != null) seatSet.addAll(seats);
        this.seats = seatSet;
        this.driverSeat = driverSeat;
        this.wheels = wheels != null ? wheels : Collections.emptyList();

        List<RelativePos> gravityPoints = new ArrayList<>();
        for (Wheel wheel : this.wheels) {
            gravityPoints.add(wheel.getRelativePos().subtract(0, wheel.getRadius(), 0));
        }
        if (extraGravityPoints != null) gravityPoints.addAll(extraGravityPoints);

        // There has to be at least one gravity point
        if (gravityPoints.isEmpty()) gravityPoints.add(new RelativePos(0, 0, 0));
        this.gravityPoints = Collections.unmodifiableList(gravityPoints);
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
     * @see Vehicle#Vehicle(Location, Player)
     */
    public Vehicle construct(Location location, Player creator) {
        return this.spawnConstructor.apply(location, creator);
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

    public boolean hasWheels() {
        return this.wheels.size() > 0;
    }

    @NotNull
    public List<Wheel> getWheels() {
        return this.wheels;
    }

    /**
     * Get the (relative) positions of where to calculate
     * the gravity for the vehicle.
     *
     * For vehicles with wheels this will be the wheel
     * locations - their radius (bottom of the wheeels)
     *
     * @return A list of relative positions to calculate gravity at.
     */
    @NotNull
    public List<RelativePos> getGravityPoints() {
        return this.gravityPoints;
    }
}
