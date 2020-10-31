package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.util.RelativePos;

/**
 * Represents a wheel in a {@link VehicleType}. Contains information
 * on where to calculate gravity and can in the future be used for
 * more realistic car driving.
 */
public class Wheel {
    private final RelativePos relativePos;
    private final float radius;
    private final float circumference;

    /**
     * @param relativePos The position of the center of the wheel relative to the vehicle's position
     * @param radius The radius of the wheel. The distance between the centre and the edges
     */
    public Wheel(RelativePos relativePos, float radius) {
        this.relativePos = relativePos;
        this.radius = radius;
        this.circumference = 2.0F * this.radius * (float) Math.PI;
    }

    public RelativePos getRelativePos() {
        return this.relativePos;
    }

    public float getRadius() {
        return this.radius;
    }

    public float getCircumference() {
        return this.circumference;
    }
}
