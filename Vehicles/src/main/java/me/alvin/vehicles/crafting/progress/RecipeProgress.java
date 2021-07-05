package me.alvin.vehicles.crafting.progress;

import me.alvin.vehicles.crafting.recipe.RecipeStep;
import me.alvin.vehicles.crafting.recipe.VehicleCraftingRecipe;

public class RecipeProgress {
    private final VehicleCraftingRecipe recipe;
    private RecipeStep currentStep;
    private int index;

    public RecipeProgress(VehicleCraftingRecipe recipe) {
        this(recipe, recipe.steps().get(0), 0);
    }

    public RecipeProgress(VehicleCraftingRecipe recipe, RecipeStep currentStep, int index) {
        this.recipe = recipe;
        this.currentStep = currentStep;
        this.index = index;
    }

    public VehicleCraftingRecipe getRecipe() {
        return this.recipe;
    }

    public RecipeStep getCurrentStep() {
        return this.currentStep;
    }

    public void setCurrentStep(RecipeStep currentStep, int index) {
        this.currentStep = currentStep;
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }
}
