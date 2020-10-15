package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.util.RelativePos;

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
