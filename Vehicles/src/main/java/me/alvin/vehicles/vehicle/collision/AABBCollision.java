package me.alvin.vehicles.vehicle.collision;

import org.bukkit.util.BoundingBox;

/**
 * An Axis Aligned Bounding Box vehicle collision type.
 */
public class AABBCollision implements VehicleCollisionType {
    private final BoundingBox boundingBox;

    /**
     * Create a new AABBCollision instance.
     *
     * @param width The width of the bounding box.
     * @param height The height of the bounding box.
     */
    public AABBCollision(double width, double height) {
        this.boundingBox = new BoundingBox(-width, 0, -width, width, height, width);
    }

    /**
     * Get the axis aligned bounding box of this AABBCollision.
     *
     * <p><strong>Note: </strong>As bounding boxes are mutable, so be very careful
     * not to modify the returned bounding box. It is not cloned before being
     * returned for performance reasons as this will be called a lot when
     * vehicle collision is being checked.</p>
     *
     * @return The bounding box.
     */
    public BoundingBox getBoundingBox() {
        return boundingBox.clone();
    }
}
