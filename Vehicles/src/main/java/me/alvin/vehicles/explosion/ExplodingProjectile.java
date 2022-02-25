package me.alvin.vehicles.explosion;

import me.alvin.vehicles.SVCraftVehicles;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.function.Predicate;

public class ExplodingProjectile extends BukkitRunnable {
    private final World world;
    private final Location location;
    private final Vector direction;
    private final Player source;
    private final boolean gravity;
    private final int explosionPower;
    private final Predicate<LivingEntity> predicate;
    private int ticks = 0;
    private double velY;

    public ExplodingProjectile(Location start, Vector direction, boolean gravity, int explosionPower, Player source, Predicate<LivingEntity> predicate) {
        this.location = start.clone();
        this.world = this.location.getWorld();
        if (this.world == null) throw new IllegalArgumentException("start location has to have a world");
        this.direction = direction;
        this.gravity = gravity;
        this.explosionPower = explosionPower;
        this.source = source;
        this.predicate = predicate;
    }

    public void start() {
        this.runTaskTimer(SVCraftVehicles.getInstance(), 1, 1);
    }

    @Override
    public void run() {
        // Tick
        this.ticks++;
        if (this.ticks > 600) {
            this.explode(false);
            return;
        }
        // Particles
        this.world.spawnParticle(Particle.CLOUD, this.location, 5, 0, 0, 0, 0, null, true);
        // Move
        this.location.add(this.direction);
        // Gravity
        if (this.gravity) {
            this.velY += 0.02;
            if (this.velY > 2) {
                this.velY = 2;
            }
            this.location.add(0, -this.velY, 0);
        }
        // Check if the missile went to an unloaded chunk
        if (!this.location.isChunkLoaded()) {
            this.explode(false);
            return;
        }
        // Check for collision with blocks
        Material material = this.location.getBlock().getType();
        if (material != Material.AIR && material.isSolid()) {
            this.explode(false);
            return;
        }
        // Check for collision with entities
        Collection<Entity> entities = this.world.getNearbyEntities(this.location, 3, 3, 3, entity -> entity != this.source && entity instanceof LivingEntity && this.predicate.test((LivingEntity) entity));
        if (entities.size() > 1) {
            Location entityLocation = entities.iterator().next().getLocation();
            this.location.setX(entityLocation.getX());
            this.location.setY(entityLocation.getY());
            this.location.setZ(entityLocation.getZ());
            System.out.println("Exploding on entity");
            this.explode(true);
        }
    }

    public void explode(boolean onEntity) {
        if (!onEntity) this.location.subtract(this.direction);
        // this.world.createExplosion(this.location, this.explosionPower, false, SVCraftVehicles.EXPLOSIONS_BREAK_BLOCKS, this.source);
        CoolExplosion.explode(this.location, this.explosionPower, this.source);
        this.cancel();
    }
}
