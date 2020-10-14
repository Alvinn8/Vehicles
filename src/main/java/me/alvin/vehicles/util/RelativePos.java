package me.alvin.vehicles.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a relative position in the world. Can be applied to a
 * location using {@link #relativeTo(Location)}. Similar to the vanilla
 * minecraft ^ ^ ^ notation.
 */
public class RelativePos {
    private double left;
    private double up;
    private double forward;

    public RelativePos(double left, double up, double forward) {
        this.left = left;
        this.up = up;
        this.forward = forward;
    }

    @NotNull
    public Location relativeTo(@NotNull Location location) {

        // https://www.spigotmc.org/threads/lots-of-math-how-do-you-teleport-an-entity-relative-to-a-players-direction.362937/#post-3335542
        // TODO: I don't really understand any of this math...
        //       Also doesn't work on the y axis
        /*
        How minecraft does ^ ^ ^:
        @net/minecraft/server/ArgumentVectorPosition.java#a(CommandListenerWrapper)

        Vec2F rotation = commandlistenerwrapper.getRotation();
        Vec3D position = commandlistenerwrapper.k().a(commandlistenerwrapper);
        float f = MathHelper.cos((rotation.pitch + 90.0F) * 0.017453292F);
        float f1 = MathHelper.sin((rotation.pitch + 90.0F) * 0.017453292F);
        float f2 = MathHelper.cos(-rotation.yaw * 0.017453292F);
        float f3 = MathHelper.sin(-rotation.yaw * 0.017453292F);
        float f4 = MathHelper.cos((-rotation.yaw + 90.0F) * 0.017453292F);
        float f5 = MathHelper.sin((-rotation.yaw + 90.0F) * 0.017453292F);
        Vec3D forwardsVector = new Vec3D(f * f2, f3, f1 * f2);
        Vec3D upVector = new Vec3D(f * f4, f5, f1 * f4);
        Vec3D leftVector = forwardsVector.cross(upVector).multiply(-1.0D);
        double offsetX = forwardsVector.x * this.forwards + upVector.x * this.up + leftVector.x * this.left;
        double offsetY = forwardsVector.y * this.forwards + upVector.y * this.up + leftVector.y * this.left;
        double offsetZ = forwardsVector.z * this.forwards + upVector.z * this.up + leftVector.z * this.left;

        return new Vec3D(position.x + offsetX, position.y + offsetY, position.z + offsetZ);
         */

        /*
        MY OLD CODE FROM
        https://www.spigotmc.org/threads/lots-of-math-how-do-you-teleport-an-entity-relative-to-a-players-direction.362937/#post-3335542

        Vector dir = location.getDirection();
        Location pointForward = location.clone().add(dir.clone().multiply(this.forward));

        Vector up = new Vector(0, 1, 0);
        Vector side = dir.clone().crossProduct(up).multiply(this.side);

        return pointForward.clone().add(side).add(0, this.up, 0);
         */




        float radians = (float) Math.toRadians(-location.getYaw());

        float f1 = (float) Math.cos(radians);
        float f2 = (float) Math.sin(radians);

        double x = this.left * (double) f1 + this.forward * (double) f2;
        double y = this.up;
        double z = this.forward * (double) f1 - this.left * (double) f2;

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

    public void setLeft(double left) {
        this.left = left;
    }

    public void setUp(double up) {
        this.up = up;
    }

    public void setForward(double forward) {
        this.forward = forward;
    }
}
