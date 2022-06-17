package me.alvin.vehicles.crafting.recipe;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

/**
 * A step in a {@link VehicleCraftingRecipe}.
 *
 * @param items A list of items needed to complete this step. ItemStacks in this list
 *              can have an amount bigger than normally possible in an itemstack as
 *              they will never be rendered as an actual item. Note that ItemStacks
 *              are mutable, modifying the items in this array later will change the
 *              recipe.
 * @param name  The name of the step to display when it is being completed.
 * @param completeTime The amount of milliseconds it takes to complete this step after
 *                     all items have been deposited.
 */
public record RecipeStep(List<ItemStack> items, Component name, int completeTime) {
    /**
     * Check whether the player can complete this step and has all the required
     * ingredients.
     *
     * @param player The player to check if they can complete the step.
     * @return {@code true} if they can complete the step, {@code false} otherwise.
     */
    public boolean canComplete(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : this.items) {
            if (!inventory.containsAtLeast(item, item.getAmount())) {
                // If an ingredient is missing, the step cannot be completed.
                return false;
            }
        }
        // If none of the ingredients were missing the recipe can be completed.
        return true;
    }

    /**
     * Take all the items this step requires from the player.
     * <p>
     * {@link #canComplete(Player)} should be run first to make sure the player has
     * all the ingredients required.
     *
     * @param player The player to take the items from.
     */
    public void takeItems(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : this.items) {
            inventory.removeItem(item);
        }
    }

    public static Builder step() {
        return new Builder();
    }

    public static class Builder {
        private final List<ItemStack> items = new ArrayList<>();
        private Component name;
        private int completeTime;

        /**
         * Add an item to this step.
         *
         * @param item The item to add. This ItemStack can have an amount bigger than
         *             normally possible in an itemstack as they will never be rendered
         *             as an actual item. Note that ItemStacks are mutable, modifying
         *             this itemstack later will change the recipe.
         * @return this
         */
        @Contract("_ -> this")
        public Builder addItem(ItemStack item) {
            this.items.add(item);
            return this;
        }

        /**
         * Set the name of this step.
         *
         * @param component The name of the step to display when it is being completed.
         * @return this
         */
        @Contract("_ -> this")
        public Builder name(Component component) {
            this.name = component;
            return this;
        }

        /**
         * Set the complete time of this step.
         *
         * @param completeTime The amount of milliseconds it takes to complete this step after
         *                     all items have been deposited.
         * @return this
         */
        @Contract("_ -> this")
        public Builder completeTime(int completeTime) {
            this.completeTime = completeTime;
            return this;
        }

        public RecipeStep build() {
            return new RecipeStep(this.items, this.name, this.completeTime);
        }
    }
}
