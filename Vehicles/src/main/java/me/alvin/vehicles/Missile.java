package me.alvin.vehicles;

import org.bukkit.Bukkit;
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

public class Missile extends BukkitRunnable {
    private final World world;
    private final Location location;
    private final Vector direction;
    private final Player source;
    private final int explosionPower;
    private int ticks = 0;

    public Missile(Location start, Vector direction, int explosionPower, Player source) {
        this.location = start.clone();
        this.world = this.location.getWorld();
        if (this.world == null) throw new IllegalArgumentException("start location has to have a world");
        this.direction = direction;
        this.explosionPower = explosionPower;
        this.source = source;
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
        this.world.spawnParticle(Particle.CLOUD, this.location, 5, 0, 0, 0, 0);
        // Move
        this.location.add(this.direction);
        // Check for collision
        Material material = this.location.getBlock().getType();
        if (material != Material.AIR && material.isSolid()) {
            this.explode(false);
            return;
        }
        Collection<Entity> entities = this.world.getNearbyEntities(this.location, 1, 1, 1, entity -> entity != this.source && entity instanceof LivingEntity);
        if (entities.size() > 1) {
            this.explode(true);
        }
    }

    public void explode(boolean onPlayer) {
        if (!onPlayer) this.location.subtract(this.direction);
        this.world.createExplosion(this.location, this.explosionPower, false, false, this.source);
        this.cancel();
    }
}
