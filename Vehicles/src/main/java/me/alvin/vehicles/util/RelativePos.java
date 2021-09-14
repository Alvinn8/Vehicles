package me.alvin.vehicles.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a relative position in the world. Can be applied to a
 * location using {@link #relativeTo(Location,float)} to get the world
 * location where this RelativePos points with the rotations found in
 * the location, with an optional roll that can be supplied as a
 * parameter. Similar to the vanilla ^ ^ ^ notation in commands.
 *
 * <p>A RelativePos consists of how much be moved forward, up and left
 * with respect to the current rotation. This can be used to for
 * example get the location of a point in a vehicle even when it is
 * rotated in yaw, pitch and roll.</p>
 *
 * <p>This class is immutable, the {@link #add(double, double, double)} and other
 * methods create new instances.</p>
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

    /**
     * Get the world location relative with this RelativePos' values to the
     * specified location.
     *
     * <p>This RelativePos' forward, up and left values will be applied to the
     * location with its yaw and pitch, and with the roll taken from the
     * roll parameter.</p>
     *
     * @param location The location to base from
     * @param roll The roll rotation
     * @return The new location.
     */
    @NotNull
    public Location relativeTo(@NotNull Location location, float roll) {
        /*
         * The math here uses matrix rotations to rotate the coordinates around different
         * axes. The matrices which the x, y and z coordinates are multiplied by are
         * gotten from this wikipedia page: https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations
         *
         * Pitch (rotation around the x-axis) a = pitch
         * ⎡ 1  0      0      ⎤
         * ⎟ 0  cos a  -sin a ⎥
         * ⎣ 0  sin a  cos a  ⎦
         *
         * Yaw (rotation around the y-axis) a = yaw
         * ⎡ cos a   0  sin a ⎤
         * ⎟ 0       1  0     ⎥
         * ⎣ -sin a  0  cos a ⎦
         * The yaw is also inverted when converting to radians as these matrix
         * transformations rotate counter-clockwise, but we rotate clockwise.
         *
         * Roll (rotation around the z-axis) a = roll
         * ⎡ cos a  -sin a  0 ⎤
         * ⎟ sin a  cos a   0 ⎥
         * ⎣ 0      0       1 ⎦
         * The roll is also inverted when converting to radians as these matrix
         * transformations rotate counter-clockwise, but we rotate clockwise.
         *
         * Work-in-progress, the roll does not quite work.
         */

        double x = this.left;
        double y = this.up;
        double z = this.forward;

        double newX;
        double newY;
        double newZ;

        // Apply roll
        if (roll != 0) {
            double radRoll = Math.toRadians(-roll);
            double rollSin = Math.sin(radRoll);
            double rollCos = Math.cos(radRoll);
            newX = x*rollCos - y*rollSin + z*0;
            newY = x*rollSin + y*rollCos + z*0;
            newZ = x*0       + y*0       + z*1;
            x = newX;
            y = newY;
            z = newZ;
        }

        // Apply pitch
        float pitch = location.getPitch();
        if (pitch != 0) {
            double radPitch = Math.toRadians(pitch);
            double pitchSin = Math.sin(radPitch);
            double pitchCos = Math.cos(radPitch);
            newX = x*1 + y*0        + z*0;
            newY = x*0 + y*pitchCos - z*pitchSin;
            newZ = x*0 + y*pitchSin + z*pitchCos;
            x = newX;
            y = newY;
            z = newZ;
        }

        // Apply yaw
        float yaw = location.getYaw();
        if (yaw != 0) {
            double radYaw = Math.toRadians(-yaw);
            double yawSin = Math.sin(radYaw);
            double yawCos = Math.cos(radYaw);
            newX =   x*yawCos + y*0 + z*yawSin;
            newY =   x*0      + y*1 + z*0;
            newZ = - x*yawSin + y*0 + z*yawCos;
            x = newX;
            y = newY;
            z = newZ;
        }

        return location.clone().add(x, y, z);
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
