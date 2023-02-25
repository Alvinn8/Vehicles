package me.alvin.vehicles.crafting;

import ca.bkaw.praeter.gui.gui.CustomGui;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.crafting.progress.RecipeProgress;
import me.alvin.vehicles.crafting.recipe.RecipeStep;
import me.alvin.vehicles.crafting.recipe.VehicleCraftingRecipe;
import me.alvin.vehicles.gui.crafting.selecting.SelectingGui;
import me.alvin.vehicles.gui.crafting.step.StepGui;
import me.alvin.vehicles.gui.crafting.timer.CreatingStepGui;
import me.alvin.vehicles.gui.crafting.viewing.ViewingGui;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import svcraft.core.tileentity.CustomTileEntity;
import svcraft.core.util.BlockLocation;

import static me.alvin.vehicles.item.VehicleSpawnerItem.VEHICLE_TYPE_TAG;

public class VehicleCraftingTable extends CustomTileEntity {
    private static final NamespacedKey CURRENT_STEP = new NamespacedKey(SVCraftVehicles.getInstance(), "step");
    private static final NamespacedKey TIMER_END = new NamespacedKey(SVCraftVehicles.getInstance(), "timerEndsAt");

    private @Nullable Player player;
    /** The vehicle type that is currently being crafted. */
    private @Nullable VehicleType vehicleType;
    private @Nullable RecipeProgress progress;
    private @Nullable Long timerEndsAt;
    private @Nullable Vehicle hologram;
    private @Nullable CustomGui gui;
    private @Nullable TimerTask timerTask;

    public VehicleCraftingTable(World world, BlockLocation location) {
        super(world, location);

        BlockState state = this.getBlock().getState();
        if (state instanceof TileState) {
            PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();

            String vehicleTypeId = container.get(VEHICLE_TYPE_TAG, PersistentDataType.STRING);
            if (vehicleTypeId != null) {
                VehicleType vehicleType = SVCraftVehicles.getInstance().getRegistry().getVehicle(vehicleTypeId);
                if (vehicleType != null) {
                    this.vehicleType = vehicleType;
                    VehicleCraftingRecipe recipe = this.vehicleType.getRecipe();
                    if (recipe != null) {
                        Integer stepIndex = container.get(CURRENT_STEP, PersistentDataType.INTEGER);
                        if (stepIndex != null) {
                            RecipeStep step = recipe.steps().get(stepIndex);
                            this.progress = new RecipeProgress(recipe, step, stepIndex);
                        }
                    }
                }
            }

            this.timerEndsAt = container.get(TIMER_END, PersistentDataType.LONG);

            if (this.vehicleType == null) {
                this.setGui(new SelectingGui(this));
            } else {
                if (this.progress == null) {
                    this.setGui(new ViewingGui(this, this.vehicleType));
                } else {
                    if (this.timerEndsAt == null) {
                        this.setGui(new StepGui(this, this.progress));
                    } else {
                        this.setGui(new CreatingStepGui(this, this.timerEndsAt, this.progress));
                    }
                }
            }

            if (this.timerEndsAt != null && this.vehicleType != null && this.timerTask == null) {
                this.timerTask = new TimerTask();
                this.timerTask.start();
            }
        }
    }

    @Override
    public void onUnload() {
        BlockState state = this.getBlock().getState();
        if (state instanceof TileState) {
            PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
            if (this.vehicleType != null) {
                container.set(VEHICLE_TYPE_TAG, PersistentDataType.STRING, this.vehicleType.getId());
            }
            if (this.progress != null) {
                container.set(CURRENT_STEP, PersistentDataType.INTEGER, this.progress.getIndex());
            }
            if (this.timerEndsAt != null) {
                container.set(TIMER_END, PersistentDataType.LONG, this.timerEndsAt);
            }
            state.update(false, false);
        }
        if (this.hologram != null) this.hologram.remove();
        if (this.timerTask != null && !this.timerTask.isCancelled()) this.timerTask.cancel();
    }

    @Override
    public void onBreak() {
        this.dropDepositedItems();
        if (this.hologram != null) this.hologram.remove();
    }

    public void viewVehicleType(@NotNull VehicleType vehicleType, Player player) {
        this.vehicleType = vehicleType;
        this.player = player;
        this.spawnHologram();
        this.setGui(new ViewingGui(this, vehicleType));
    }

    public void stopViewing() {
        this.removeHologram();
        this.setGui(new SelectingGui(this));
    }

    private void spawnHologram() {
        if (this.vehicleType == null) {
            return;
        }
        this.hologram = this.vehicleType.construct(getBlock().getLocation().add(0, 1, 0), this.player, VehicleSpawnReason.CRAFTING_HOLOGRAM);
        this.hologram.becomeHologram();
        this.hologram.getSlime().remove(); // Remove the slime to remove collision
    }

    /**
     * Start crafting the vehicle that is currently being viewed.
     */
    public void startCrafting(Player player) {
        if (this.vehicleType == null) {
            throw new IllegalStateException("Can not start crafting when no vehicle type was viewed.");
        }
        this.player = player;
        VehicleCraftingRecipe recipe = this.vehicleType.getRecipe();
        this.progress = new RecipeProgress(recipe);
        this.setGui(new StepGui(this, this.progress));
    }

    public void stopCrafting() {
        this.dropDepositedItems();
        this.removeHologram();
        this.setGui(new SelectingGui(this));
        this.stopTimer();
    }

    /**
     * Check whether the vehicle would fit above the vehicle crafting table.
     *
     * @return Whether the vehicle would fit.
     */
    public boolean doesVehicleFit() {
        return this.hologram == null || !this.hologram.collides(this.hologram.getLocation());
    }

    private void setGui(CustomGui gui) {
        CustomGui oldGui = this.gui;
        this.gui = gui;
        if (oldGui != null && this.gui != null) {
            oldGui.getViewers().forEach(this.gui::show);
        }
    }

    private void removeHologram() {
        if (this.hologram != null) {
            this.hologram.remove();
            this.hologram = null;
        }
    }

    /**
     * Drop the items that have been deposited into the craft table on the ground.
     */
    private void dropDepositedItems() {
        if (this.progress != null) {
            int end = this.progress.getIndex();
            // If we are in the timer view the items of the current step
            // should also be returned.
            if (this.timerEndsAt != null) end++;

            Location location = getBlock().getLocation().add(0, 1, 0);
            for (int i = 0; i < end; i++) {
                RecipeStep step = this.progress.getRecipe().steps().get(i);
                for (ItemStack item : step.items()) {
                    location.getWorld().dropItemNaturally(location, item);
                }
            }
        }
    }

    public void startTimer(long timerEndsAt) {
        this.timerEndsAt = timerEndsAt;
        this.setGui(new CreatingStepGui(this, this.timerEndsAt, this.progress));
        this.timerTask = new TimerTask();
        this.timerTask.start();
    }

    private void stopTimer() {
        if (this.timerTask != null) {
            if (!this.timerTask.isCancelled()) {
                this.timerTask.cancel();
            }
            this.timerTask = null;
        }
    }

    private void completeStep() {
        if (this.progress == null || this.vehicleType == null) {
            throw new IllegalStateException("Need progress and vehicleType to call completeStep");
        }

        int newIndex = this.progress.getIndex() + 1;
        // Is this the last step?
        if (newIndex >= this.progress.getRecipe().steps().size()) {
            // Is the last step, the recipe has been completed.
            if (this.hologram != null) {
                this.hologram.remove();
                this.hologram = null;
            }
            this.vehicleType.construct(getBlock().getLocation().add(0, 1, 0), this.player, VehicleSpawnReason.CRAFTING);
            if (this.gui != null) {
                this.gui.getViewers().forEach(Player::closeInventory);
            }
            this.progress = null;
            this.timerEndsAt = null;
            this.timerTask = null;
            this.setGui(new SelectingGui(this));
        } else {
            // Is not the last step, advance to the next one
            RecipeStep nextStep = this.progress.getRecipe().steps().get(newIndex);
            this.progress.setCurrentStep(nextStep, newIndex);

            // Set to crafting again to re-render the gui with the new step
            this.setGui(new StepGui(this, this.progress));
        }
    }

    public void openGui(Player player) {
        if (this.gui == null) {
            this.gui = new SelectingGui(this);
        }
        this.gui.show(player);
    }

    public class TimerTask extends BukkitRunnable {
        @Override
        public void run() {
            if (timerEndsAt == null) {
                SVCraftVehicles.getInstance().getLogger().warning("TimerTask was running but timerEndsAt was null");
                cancel();
                return;
            }
            if (System.currentTimeMillis() >= timerEndsAt) {
                this.cancel();
                completeStep();
                Block block = getBlock();
                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1, 1.75F);
                return;
            }

            if (gui != null && gui instanceof CreatingStepGui creatingStepGui && gui.getViewers().size() > 0) {
                creatingStepGui.updateTimer();
                creatingStepGui.update();
            }
        }

        public void start() {
            this.runTaskTimer(SVCraftVehicles.getInstance(), 0, 20);
        }
    }
}
