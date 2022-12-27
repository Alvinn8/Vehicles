package me.alvin.vehicles.crafting.recipe;

import me.alvin.vehicles.SVCraftVehicles;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A recipe for crafting a vehicle.
 */
public final class VehicleCraftingRecipe {
    private final List<RecipeStep> steps;
    private final String displayItem;
    private ItemStack cachedDisplayItem;

    /**
     * Create a new recipe.
     *
     * @param steps       A list of steps that must be completed to complete the recipe.
     * @param displayItem The item to use for displaying a big preview of the
     *                    vehicle to craft.
     */
    public VehicleCraftingRecipe(List<RecipeStep> steps, String displayItem) {
        this.steps = steps;
        this.displayItem = displayItem;
    }

    public static Builder recipe() {
        return new Builder();
    }

    public List<RecipeStep> steps() {
        return this.steps;
    }

    public ItemStack displayItem() {
        if (this.cachedDisplayItem == null) {
            this.cachedDisplayItem = SVCraftVehicles.getInstance().getModelDB().generateItem(this.displayItem);
        }
        return this.cachedDisplayItem;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (VehicleCraftingRecipe) obj;
        return Objects.equals(this.steps, that.steps) &&
            Objects.equals(this.displayItem, that.displayItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(steps, displayItem);
    }

    @Override
    public String toString() {
        return "VehicleCraftingRecipe[" +
            "steps=" + steps + ", " +
            "displayItem=" + displayItem + ']';
    }


    public static class Builder {
        private final List<RecipeStep> steps = new ArrayList<>();
        private String displayItem;

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
         * @param displayItem The key to the display item.
         * @return this
         */
        @Contract("_ -> this")
        public Builder displayItem(String displayItem) {
            this.displayItem = displayItem;
            return this;
        }

        public VehicleCraftingRecipe build() {
            return new VehicleCraftingRecipe(this.steps, this.displayItem);
        }
    }
}
