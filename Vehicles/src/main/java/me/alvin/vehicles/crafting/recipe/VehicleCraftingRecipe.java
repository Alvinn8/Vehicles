package me.alvin.vehicles.crafting.recipe;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A recipe for crafting a vehicle.
 */
public final class VehicleCraftingRecipe {
    private final List<RecipeStep> steps;

    /**
     * Create a new recipe.
     *
     * @param steps       A list of steps that must be completed to complete the recipe.
     */
    public VehicleCraftingRecipe(List<RecipeStep> steps) {
        this.steps = steps;
    }

    public static Builder recipe() {
        return new Builder();
    }

    public List<RecipeStep> steps() {
        return this.steps;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (VehicleCraftingRecipe) obj;
        return Objects.equals(this.steps, that.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(steps);
    }

    @Override
    public String toString() {
        return "VehicleCraftingRecipe[" +
            "steps=" + steps + ']';
    }


    public static class Builder {
        private final List<RecipeStep> steps = new ArrayList<>();

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

        public VehicleCraftingRecipe build() {
            return new VehicleCraftingRecipe(this.steps);
        }
    }
}
