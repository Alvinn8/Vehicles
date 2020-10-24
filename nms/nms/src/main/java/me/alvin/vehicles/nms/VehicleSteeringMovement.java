package me.alvin.vehicles.nms;

/**
 * Variables related to vehicle steering. This has to be inside the nms
 * module as it can't depend on the main module as that would cause
 * circular dependencies.
 */
public class VehicleSteeringMovement {
    /**
     * 1 for steering forward, -1 for steering backwards, 0 for no movement.
     * Should NOT be set to any other value.
     */
    public float forward = 0;
    /**
     * 1 for steering left, -1 for steering right, 0 for no movement.
     * Should NOT be set to any other value.
     */
    public float side = 0;
    /**
     * {@code true} if the space bar/jump button is being pressed
     */
    public boolean space = false;
    /**
     * {@code true} if the shift/dismount button is being pressed
     */
    public boolean shift = false;

    public void reset() {
        this.forward = 0;
        this.side = 0;
        this.space = false;
        this.shift = false;
    }
}
