package me.alvin.vehicles.vehicle.seat;

import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.VehicleType;

/**
 * Represents a seat in a {@link VehicleType}. Contains information
 * on where to put the player entering the vehicle.
 */
public class Seat {
    private final RelativePos relativePos;
    private final int offsetYaw;

    public Seat(RelativePos relativePos) {
        this(relativePos, 0);
    }

    public Seat(RelativePos relativePos, int offsetYaw) {
        this.relativePos = relativePos;
        this.offsetYaw = offsetYaw;
    }

    public RelativePos getRelativePos() {
        return this.relativePos;
    }

    public int getOffsetYaw() {
        return this.offsetYaw;
    }

    public boolean hasOffsetYaw() {
        return this.offsetYaw != 0;
    }
}
