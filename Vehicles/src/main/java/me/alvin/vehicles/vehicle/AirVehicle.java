package me.alvin.vehicles.vehicle;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Deprecated
public abstract class AirVehicle extends Vehicle {
    /**
     * The milliseconds the last shot flare will expire. When a flare is fired this will be
     * set to the current milliseconds + the amount of milliseconds the flare lasts.
     * Will be null if the vehicle has not fired a flare.
     */
    private Long flareExpireTime;

    public AirVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public AirVehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    /**
     * Whether the vehicle can flare or not.
     *
     * @return {@code true} If the vehicle can flare, {@code false} otherwise
     */
    public abstract boolean canFlare();

    /**
     * Whether the vehicle is using a flare right now. This will cause
     * incoming missiles to miss.
     *
     * @return {@code true} If the plane recently flared and the flare is still in used.
     * {@code false} otherwise
     */
    public boolean isUsingFlare() {
        if (this.flareExpireTime == null) return false;
        return this.flareExpireTime < System.currentTimeMillis();
    }
}
