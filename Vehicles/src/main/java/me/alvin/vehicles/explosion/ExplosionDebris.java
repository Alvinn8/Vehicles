package me.alvin.vehicles.explosion;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ExplosionDebris {
    public static final double GRAVITY = 0.04;

    private final Location location;
    private final Vector direction;

    public ExplosionDebris(Location location, Vector direction) {
        this.location = location.clone();
        this.direction = direction;
    }

    public void tick() {
        // Gravity
        this.direction.setY(this.direction.getY() - GRAVITY);

        // Add direction
        this.location.add(this.direction);

        // Particle
        this.location.getWorld().spawnParticle(Particle.CLOUD, this.location, 1, 0, 0, 0, 0, null, true);
    }

    public Location getLocation() {
        return this.location;
    }
}
