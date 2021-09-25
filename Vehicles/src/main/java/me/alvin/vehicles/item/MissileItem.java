package me.alvin.vehicles.item;

import org.bukkit.inventory.ItemStack;

public interface MissileItem {
    /**
     * Get the explosion power to blow up with.
     *
     * @param itemStack The ItemStack to get the explosion power of.
     * @return The explosion power.
     */
    int getExplosionPower(ItemStack itemStack);
}
