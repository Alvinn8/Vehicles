package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.util.RelativePos;

/**
 * Contains data on where a attached vehicle should be placed
 * relative to the vehicle it is attached to.
 */
public class AttachmentData {
    private final RelativePos relativePos;
    private final int offsetYaw;

    public AttachmentData(RelativePos relativePos) {
        this(relativePos, 0);
    }
    public AttachmentData(RelativePos relativePos, int offsetYaw) {
        this.relativePos = relativePos;
        this.offsetYaw = offsetYaw;
    }

    public RelativePos getRelativePos() {
        return this.relativePos;
    }

    public int getOffsetYaw() {
        return this.offsetYaw;
    }
}
