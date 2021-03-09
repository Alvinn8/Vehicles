package me.alvin.vehicles.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a relative position in the world. Can be applied to a
 * location using {@link #relativeTo(Location)}. Similar to the vanilla
 * minecraft ^ ^ ^ notation.
 *
 * This class is immutable, the {@link #add(double, double, double)} and other
 * methods create new instances.
 */
public class RelativePos {
    private final double left;
    private final double up;
    private final double forward;

    public RelativePos(double left, double up, double forward) {
        this.left = left;
        this.up = up;
        this.forward = forward;
    }

    @NotNull
    public Location relativeTo(@NotNull Location location) {

        double radians = Math.toRadians(-location.getYaw());

        double f1 = Math.cos(radians);
        double f2 = Math.sin(radians);

        double x = this.left * f1 + this.forward * f2;
        double y = this.up;
        double z = this.forward * f1 - this.left * f2;

        return location.clone().add(x, y, z);
    }

    @NotNull
    public Vector toVector() {
        return new Vector(this.forward, this.up, this.left);
    }

    public double getLeft() {
        return this.left;
    }

    public double getUp() {
        return this.up;
    }

    public double getForward() {
        return this.forward;
    }

    @Override
    public String toString() {
        return "RelativePos{" +
                "left=" + this.left +
                ", up=" + this.up +
                ", forward=" + this.forward +
                '}';
    }

    public RelativePos add(double left, double up, double forward) {
        return new RelativePos(this.left + left, this.up + up, this.forward + forward);
    }

    public RelativePos subtract(double left, double up, double forward) {
        return new RelativePos(this.left - left, this.up - up, this.forward - forward);
    }
}
