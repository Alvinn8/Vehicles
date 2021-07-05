package me.alvin.vehicles.crafting.recipe;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe for crafting a vehicle.
 */
public record VehicleCraftingRecipe(List<RecipeStep> steps, ItemStack displayItem) {
    /**
     * Create a new recipe.
     *
     * @param steps       A list of steps that must be completed to complete the recipe.
     * @param displayItem The item to use for displaying a big preview of the
     *                    vehicle to craft.
     */
    public VehicleCraftingRecipe {}

    public static Builder recipe() {
        return new Builder();
    }

    public static class Builder {
        private final List<RecipeStep> steps = new ArrayList<>();
        private ItemStack displayItem;

        /**
         * Add a step to this recipe.
         *
         * @param recipeStep The step to add.
         * @return this
         * @see RecipeStep#RecipeStep(List, Component, int)
         */
        @Contract("_ -> this")
        public Builder addStep(RecipeStep.Builder recipeStep) {
            this.steps.add(recipeStep.build());
            return this;
        }

        /**
         * Set the item to use for displaying a big preview of the vehicle to craft.
         *
         * @param displayItem The display item.
         * @return this
         */
        @Contract("_ -> this")
        public Builder displayItem(ItemStack displayItem) {
            this.displayItem = displayItem;
            return this;
        }

        public VehicleCraftingRecipe build() {
            return new VehicleCraftingRecipe(this.steps, this.displayItem);
        }
    }
}
