package me.alvin.vehicles.vehicle;

import org.bukkit.inventory.ItemStack;

/**
 * Data for repairing a vehicle.
 *
 * @param repairTime Milliseconds it takes to repair by one step.
 * @param repairAmount The amount of health points one repair heals by.
 * @param repairIngredients The ingredients to repair. Always has length 3.
 */
public record RepairData(int repairTime, int repairAmount, ItemStack... repairIngredients) {
    public RepairData(int repairTime, int repairAmount, ItemStack... repairIngredients) {
        this.repairTime = repairTime;
        this.repairAmount = repairAmount;
        this.repairIngredients = new ItemStack[3];
        System.arraycopy(repairIngredients, 0, this.repairIngredients, 0, repairIngredients.length);
    }
}
