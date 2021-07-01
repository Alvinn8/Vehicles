package me.alvin.vehicles.explosion;

import me.alvin.vehicles.SVCraftVehicles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CoolExplosion {
    public static void explode(Location location, int power, Entity source) {
        // Actual explosion
        // location.getWorld().createExplosion(location, power, false, SVCraftVehicles.EXPLOSIONS_BREAK_BLOCKS, source);
        location.getWorld().createExplosion(location, power, false, true, source);

        // Muffled sound for players far away
        for (Player player : location.getNearbyPlayers(128)) {
            player.playSound(location, "svcraftvehicles:explosion_far_away", SoundCategory.BLOCKS,  1, 1);
        }

        // Extra particles
        location.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, power * 50, power / 2.0, power / 2.0, power / 2.0, 0.01);
        location.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, location, power * 7, power / 2.0, power / 2.0, power / 2.0, 0);
        location.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, location, power * 3, power / 2.0, power / 2.0, power / 2.0, 0.02);
        Bukkit.getScheduler().runTaskLater(SVCraftVehicles.getInstance(), () -> {
            location.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, power * 10, power / 2.0, power / 2.0, power / 2.0, 0.01);
        }, 40);

        // Spawn flying debris
        List<ExplosionDebris> debrisList = new ArrayList<>();
        Random random = new Random();
        Location debrisLocation = location.clone().add(0, 2, 0);
        for (int i = 0; i < 15; i++) {
            double angle = random.nextDouble() * 2 * Math.PI; // from 0 rad to 2pi rad (0deg to 360 deg)
            double velY = random.nextDouble() * 1 + 0.5;
            Vector direction = new Vector(Math.sin(angle), velY, Math.cos(angle));
            direction.multiply(random.nextDouble() * 1);
            ExplosionDebris debris = new ExplosionDebris(debrisLocation, direction);
            debrisList.add(debris);
        }
        ExplosionDebrisRunnable runnable = new ExplosionDebrisRunnable(debrisList);
        runnable.start();
    }
}
