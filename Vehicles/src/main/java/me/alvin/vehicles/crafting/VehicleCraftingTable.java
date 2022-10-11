package me.alvin.vehicles.crafting;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.crafting.progress.RecipeProgress;
import me.alvin.vehicles.crafting.recipe.RecipeStep;
import me.alvin.vehicles.crafting.recipe.VehicleCraftingRecipe;
import me.alvin.vehicles.util.ExtraPersistentDataTypes;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import svcraft.core.tileentity.CustomTileEntity;
import svcraft.core.util.BlockLocation;
import svcraft.core.util.CustomInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.alvin.vehicles.item.VehicleSpawnerItem.VEHICLE_TYPE_TAG;

/**
 * The vehicle crafting table tile entity that holds all data for
 * placed crafting tables.
 */
public class VehicleCraftingTable extends CustomTileEntity {
    public static final NamespacedKey CURRENT_VIEW = new NamespacedKey(SVCraftVehicles.getInstance(), "view");
    public static final NamespacedKey CURRENT_STEP = new NamespacedKey(SVCraftVehicles.getInstance(), "step");
    public static final NamespacedKey TIMER_END = new NamespacedKey(SVCraftVehicles.getInstance(), "timerEndsAt");
    public static final NamespacedKey PLAYER = new NamespacedKey(SVCraftVehicles.getInstance(), "player");

    private @Nullable Inventory inventory;
    private @Nullable Component title;
    private @NotNull CraftingView view = CraftingView.SELECTING;
    /** The vehicle type that is currently being crafted. */
    private @Nullable VehicleType vehicleType;
    private @Nullable RecipeProgress progress;
    private @Nullable Long timerEndsAt;
    private @Nullable TimerTask timerTask;
    private @Nullable Player player;
    private @Nullable Vehicle hologram;

    public VehicleCraftingTable(World world, BlockLocation location) {
        super(world, location);

        BlockState state = this.getBlock().getState();
        if (state instanceof TileState) {
            PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
            Integer currentViewOrdinal = container.get(CURRENT_VIEW, PersistentDataType.INTEGER);
            if (currentViewOrdinal != null) {
                this.setView(CraftingView.values()[currentViewOrdinal]);
            }

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
            UUID playerUUID = container.get(PLAYER, ExtraPersistentDataTypes.UUID);
            Player player = playerUUID == null ? null : Bukkit.getPlayer(playerUUID);
            if (player != null) {
                this.player = player;
            }

            if (this.view == CraftingView.TIMER && this.timerEndsAt != null && this.vehicleType != null && this.timerTask == null) {
                this.timerTask = new TimerTask();
                this.timerTask.start();
            }
        }
    }

    private void createInventory() {
        this.inventory = Bukkit.createInventory(new VehicleCraftingInventory(), 54, this.title == null ? Component.text("Vehicle Crafting Table") : this.title);
        this.setView(this.view);
    }

    public void openInventory(HumanEntity player) {
        if (this.inventory == null) {
            this.createInventory();
        }
        this.updateTimer();
        this.updateCanStartCrafting();
        player.openInventory(this.inventory);
        this.updateCanComplete(); // this method needs the viewer (player), so we call it after opening the inventory
    }

    /**
     * Get the recipe currently being crafted.
     *
     * @return The recipe.
     */
    @NotNull
    public VehicleCraftingRecipe getRecipe() {
        if (this.vehicleType == null) throw new IllegalStateException("No vehicle type currently");
        VehicleCraftingRecipe recipe = this.vehicleType.getRecipe();
        if (recipe == null) throw new RuntimeException("Vehicle type has no recipe, was it removed?");
        return recipe;
    }

    @NotNull
    public CraftingView getView() {
        return this.view;
    }

    /**
     * Set the view for the vehicle crafting table and re-render it.
     *
     * @param view The view to set to
     */
    public void setView(@NotNull CraftingView view) {
        this.view = view;

        if (this.inventory == null) return;

        if ((this.view == CraftingView.SELECTING || this.view == CraftingView.VIEWING) && this.title != null) {
            // Set the title to null which will make it say vehicle crafting table instead.
            this.setTitle(null);
        }

        this.inventory.clear();

        switch (this.view) {
            case SELECTING -> {
                // Background
                this.setBackground("svcraftvehicles:gui/vehicle_crafting_table/selecting");

                // Fill with vehicle types
                int slot = 1;
                for (VehicleType vehicleType : SVCraftVehicles.getInstance().getRegistry().getMap().values()) {
                    if (vehicleType.getRecipe() == null) continue;

                    ItemStack item = new ItemStack(Material.DIAMOND);
                    item.editMeta(meta -> {
                        meta.displayName(Component.empty().color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false).append(vehicleType.getName()));
                        PersistentDataContainer container = meta.getPersistentDataContainer();
                        container.set(VEHICLE_TYPE_TAG, PersistentDataType.STRING, vehicleType.getId());
                    });
                    this.inventory.setItem(slot, item);
                    slot++;
                    if (slot % 9 == 8) slot += 2;
                }
            }
            case VIEWING -> {
                if (this.vehicleType == null) {
                    SVCraftVehicles.getInstance().getLogger().warning("Not viewing any recipe, but tried to set the stage to VIEWING.");
                    setView(CraftingView.SELECTING);
                    return;
                }

                this.hologram = this.vehicleType.construct(getBlock().getLocation().add(0, 1, 0), this.player, VehicleSpawnReason.CRAFTING);
                this.hologram.becomeHologram();
                this.hologram.getSlime().remove(); // Remove the slime to remove collision

                VehicleCraftingRecipe recipe = this.getRecipe();

                // this method sets the background
                this.updateCanStartCrafting();

                // Set the preview item
                this.inventory.setItem(40, recipe.displayItem());

                // Cancel button
                for (int slot = 43; slot <= 44; slot++) {
                    ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/transparent");
                    item.editMeta(meta -> {
                        meta.displayName(Component.text("Will send you back to the", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
                        meta.lore(Collections.singletonList(Component.text("vehicle selection screen", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
                    });
                    this.inventory.setItem(slot, item);
                }

                // Total material list
                Map<ItemStack, Integer> map = new LinkedHashMap<>();
                for (RecipeStep step : recipe.steps()) {
                    for (ItemStack item : step.items()) {
                        ItemStack base = item.clone();
                        base.setAmount(1);
                        int currentAmount = map.getOrDefault(base, 0);
                        map.put(base, currentAmount + item.getAmount());
                    }
                }

                ItemStack materialListItem = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/transparent");
                materialListItem.editMeta(meta -> {
                    meta.displayName(Component.text("Total items required:", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.UNDERLINED));
                    List<Component> lore = new ArrayList<>();
                    for (Map.Entry<ItemStack, Integer> entry : map.entrySet()) {
                        ItemStack item = entry.getKey();
                        lore.add(Component.text()
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(Component.text(entry.getValue() + "x ", NamedTextColor.YELLOW))
                            .append(Component.translatable(item))
                            .build()
                        );
                    }
                    meta.lore(lore);
                });

                this.inventory.setItem(2, materialListItem);
                this.inventory.setItem(3, materialListItem.clone());
            }
            case CRAFTING -> {
                if (this.progress == null) {
                    SVCraftVehicles.getInstance().getLogger().warning("No progress but in CRAFTING view.");
                    this.setView(CraftingView.SELECTING);
                    return;
                }

                Component title = Component.text().content("Step " + (this.progress.getIndex() + 1) + " / " + this.progress.getRecipe().steps().size() + ": ").append(this.progress.getCurrentStep().name()).build();
                if (!title.equals(this.title)) {
                    this.setTitle(title);
                }

                // The background is set inside updateCanComplete

                // Cancel button
                for (int slot = 43; slot <= 44; slot++) {
                    this.inventory.setItem(slot, this.makeCancelItem());
                }

                RecipeStep step = this.progress.getCurrentStep();

                int slot = 10;
                for (ItemStack item : step.items()) {
                    ItemStack renderItem = item.clone();
                    Component name = Component.text().decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(item.getAmount() + "x "))
                        .append(Component.translatable(item))
                        .build();
                    renderItem.editMeta(meta -> meta.displayName(name));
                    this.inventory.setItem(slot, renderItem);

                    // If we have more than 64 items we need to split it into
                    // multiple stacks
                    if (item.getAmount() > 64) {
                        int amount = item.getAmount() - 64;
                        while (amount > 0) {
                            ItemStack renderItem2 = renderItem.clone();
                            if (amount > 64) {
                                // Need yet another stack
                                renderItem2.setAmount(64);
                                amount -= 64;
                            } else {
                                // We can consume the rest here
                                renderItem2.setAmount(amount);
                                amount = 0; // amount -= amount
                            }
                            slot++;
                            if (slot % 9 == 5) slot += 5;
                            this.inventory.setItem(slot, renderItem2);
                        }
                    }
                    slot++;
                    if (slot % 9 == 5) slot += 5;
                }

                // Update whether the player can complete the step
                this.updateCanComplete();
            }
            case TIMER -> {
                // Background
                this.setBackground("svcraftvehicles:gui/vehicle_crafting_table/timer");

                // Cancel button
                for (int slot = 25; slot <= 26; slot++) {
                    this.inventory.setItem(slot, this.makeCancelItem());
                }
            }
        }
    }

    private void setBackground(String key) {
        ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem(key);
        item.editMeta(meta -> meta.displayName(Component.empty()));
        this.inventory.setItem(53, item);
    }

    private ItemStack makeCancelItem() {
       ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/transparent");
       item.editMeta(meta -> {
           meta.displayName(       Component.text("Will send you back to the vehicle", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
           meta.lore(Arrays.asList(Component.text("selection screen. You will get all", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                                   Component.text("of the deposited items back.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
           ));
       });
        return item;
    }

    private boolean doesVehicleFit() {
        return this.hologram == null || !this.hologram.collides(this.hologram.getLocation());
    }

    private void updateCanStartCrafting() {
        if (this.view != CraftingView.VIEWING) return;

        boolean vehicleFits = this.doesVehicleFit();

        if (vehicleFits) {
            this.setBackground("svcraftvehicles:gui/vehicle_crafting_table/viewing_enabled");
        } else {
            this.setBackground("svcraftvehicles:gui/vehicle_crafting_table/viewing_disabled");
        }

        // Craft button
        for (int slot = 16; slot <= 17; slot++) {
            ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/transparent");
            item.editMeta(meta -> {
                if (vehicleFits) {
                    meta.displayName(Component.text("Start crafting the vehicle", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
                } else {
                    meta.displayName(                   Component.text("Please make room above the crafting", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
                    meta.lore(Collections.singletonList(Component.text("table so the vehicle fits.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
                }
            });
            this.inventory.setItem(slot, item);
        }
    }

    /**
     * Update whether the current step can be completed or not.
     */
    public void updateCanComplete() {
        if (this.view != CraftingView.CRAFTING) return;
        if (this.progress == null) {
            SVCraftVehicles.getInstance().getLogger().warning("No progress but in CRAFTING view.");
            this.setView(CraftingView.SELECTING);
            return;
        }

        Player player;
        if (this.inventory == null || this.inventory.getViewers().isEmpty()) player = null;
        else player = (Player) this.inventory.getViewers().get(0); // Player = ServerPlayer, HumanEntity = Player, we are server side, so players are always ServerPlayers, it's safe to cast.

        if (player != null && this.progress.getCurrentStep().canComplete(player)) {
            // The step can be completed
            this.setBackground("svcraftvehicles:gui/vehicle_crafting_table/crafting_enabled");

            for (int slot = 16; slot <= 17; slot++) {
                ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/transparent");
                item.editMeta(meta -> meta.displayName(Component.text("Complete step", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
                this.inventory.setItem(slot, item);
            }
        } else {
            // The step can not be completed
            this.setBackground("svcraftvehicles:gui/vehicle_crafting_table/crafting_disabled");

            for (int slot = 16; slot <= 17; slot++) {
                ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/transparent");
                item.editMeta(meta -> meta.displayName(Component.text("You do not have all the required items", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
                this.inventory.setItem(slot, item);
            }
        }
    }

    public void updateTimer() {
        if (this.view != CraftingView.TIMER || this.timerEndsAt == null) return;

        int ms = (int) (this.timerEndsAt - System.currentTimeMillis());
        int totalSeconds = ms / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        Component line1 = Component.text("This step is being assembled.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
        Component line2 = Component.text("It will complete in " + minutes + " " + (minutes == 1 ? "minute" : "minutes") + " and " + seconds + " " + (seconds == 1 ? "second" : "seconds"), NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);

        int minutes1 = minutes / 10; // first digit
        int minutes2 = minutes % 10; // second digit

        int seconds1 = seconds / 10; // first digit
        int seconds2 = seconds % 10; // second digit

        this.renderDigit(19, minutes1, line1, line2);
        this.renderDigit(20, minutes2, line1, line2);

        this.renderDigit(21, seconds1, line1, line2);
        this.renderDigit(22, seconds2, line1, line2);
    }

    private void renderDigit(int slot, int digit, Component name, Component lore) {
        ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("numbers:" + digit);
        item.editMeta(meta -> {
            meta.displayName(name);
            meta.lore(Collections.singletonList(lore));
        });
        this.inventory.setItem(slot, item);
    }

    /**
     * Drop the items that have been deposited into the craft table on the ground.
     */
    private void dropDepositedItems() {
        if (this.progress != null) {
            int end = this.progress.getIndex();
            // If we are in the timer view the items of the current step
            // should also be returned.
            if (this.view == CraftingView.TIMER) end++;

            Location location = getBlock().getLocation().add(0, 1, 0);
            for (int i = 0; i < end; i++) {
                RecipeStep step = this.progress.getRecipe().steps().get(i);
                for (ItemStack item : step.items()) {
                    location.getWorld().dropItemNaturally(location, item);
                }
            }
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
            if (this.inventory != null && this.player != null && this.inventory.getViewers().contains(this.player)) {
                this.player.closeInventory();
            }
            this.progress = null;
            this.timerEndsAt = null;
            this.timerTask = null;
            setView(CraftingView.SELECTING);
        } else {
            // Is not the last step, advance to the next one
            RecipeStep nextStep = this.progress.getRecipe().steps().get(newIndex);
            this.progress.setCurrentStep(nextStep, newIndex);

            // Set to crafting again to re-render the gui with the new step
            setView(CraftingView.CRAFTING);
        }
    }

    /**
     * Set the title of the inventory, which isn't possible so the inventory is
     * re-created and viewers are re-added.
     * <p>
     * The actual new inventory is created inside {@link #openInventory(HumanEntity)}.
     *
     * @param title The title to use, or null for default
     */
    private void setTitle(@Nullable Component title) {
        HumanEntity[] viewers = this.inventory == null ? new HumanEntity[0] : this.inventory.getViewers().toArray(new HumanEntity[0]);
        this.inventory = null;
        this.title = title;
        this.createInventory();
        for (HumanEntity viewer : viewers) {
            this.openInventory(viewer);
        }
    }

    @Override
    public void onUnload() {
        BlockState state = this.getBlock().getState();
        if (state instanceof TileState) {
            PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
            container.set(CURRENT_VIEW, PersistentDataType.INTEGER, this.view.ordinal());
            if (this.vehicleType != null) container.set(VEHICLE_TYPE_TAG, PersistentDataType.STRING, this.vehicleType.getId());
            if (this.progress != null)    container.set(CURRENT_STEP, PersistentDataType.INTEGER, this.progress.getIndex());
            if (this.timerEndsAt != null) container.set(TIMER_END, PersistentDataType.LONG, this.timerEndsAt);
            if (this.player != null)      container.set(PLAYER, ExtraPersistentDataTypes.UUID, this.player.getUniqueId());
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

    public class VehicleCraftingInventory extends CustomInventory {
        @Override
        public void onClick(InventoryClickEvent event) {
            event.setCancelled(true);

            switch (view) {
                case SELECTING -> {
                    ItemStack item = event.getCurrentItem();
                    if (item == null) return;
                    ItemMeta meta = item.getItemMeta();
                    if (meta == null) return;
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    String vehicleTypeId = container.get(VEHICLE_TYPE_TAG, PersistentDataType.STRING);
                    if (vehicleTypeId == null) return;
                    VehicleType vehicleType = SVCraftVehicles.getInstance().getRegistry().getVehicle(vehicleTypeId);
                    if (vehicleType == null) return;
                    if (vehicleType.getRecipe() == null) return;

                    VehicleCraftingTable.this.vehicleType = vehicleType;
                    VehicleCraftingTable.this.player = (Player) event.getWhoClicked();
                    setView(CraftingView.VIEWING);
                }
                case VIEWING -> {
                    if (event.getSlot() == 16 || event.getSlot() == 17) {
                        // Craft button was pressed
                        if (doesVehicleFit()) {
                            // The vehicle fits (button was enabled)
                            VehicleCraftingRecipe recipe = getRecipe();
                            progress = new RecipeProgress(recipe);
                            setView(CraftingView.CRAFTING);
                        }
                    } else if (event.getSlot() == 43 || event.getSlot() == 44) {
                        // Cancel button was pressed
                        vehicleType = null;
                        if (hologram != null) {
                            hologram.remove();
                            hologram = null;
                        }
                        setView(CraftingView.SELECTING);
                    }
                }
                case CRAFTING -> {
                    if (progress == null || vehicleType == null) {
                        SVCraftVehicles.getInstance().getLogger().warning("No progress or no vehicleType but in CRAFTING view.");
                        return;
                    }
                    Player player = (Player) event.getWhoClicked();
                    if (event.getSlot() == 16 || event.getSlot() == 17) {
                        RecipeStep currentStep = progress.getCurrentStep();
                        if (currentStep.canComplete(player)) {
                            // Take the items
                            currentStep.takeItems(player);

                            VehicleCraftingTable.this.player = player;

                            timerEndsAt = System.currentTimeMillis() + currentStep.completeTime();
                            setView(CraftingView.TIMER);
                            timerTask = new TimerTask();
                            timerTask.start();
                        }
                    } else if (event.getSlot() == 43 || event.getSlot() == 44) {
                        dropDepositedItems();
                        if (hologram != null) {
                            hologram.remove();
                            hologram = null;
                        }
                        setView(CraftingView.SELECTING);
                    }
                }
                case TIMER -> {
                    if (event.getSlot() == 25 || event.getSlot() == 26) {
                        dropDepositedItems();
                        setView(CraftingView.SELECTING);
                        if (timerTask != null && !timerTask.isCancelled()) {
                            timerTask.cancel();
                            timerTask = null;
                        }
                        if (hologram != null) {
                            hologram.remove();
                            hologram = null;
                        }
                    }
                }
            }
        }
    }

    public class TimerTask extends BukkitRunnable {

        @Override
        public void run() {
            if (timerEndsAt == null) {
                SVCraftVehicles.getInstance().getLogger().warning("TimerTask was running but timerEndsAt was null");
                this.cancel();
                return;
            }

            if (System.currentTimeMillis() >= timerEndsAt) {
                completeStep();
                Block block = getBlock();
                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1, 1.75F);
                this.cancel();
                return;
            }

            if (inventory == null || inventory.getViewers().isEmpty()) {
                return;
            }
            // Only update if we have viewers
            updateTimer();
        }

        public void start() {
            this.runTaskTimer(SVCraftVehicles.getInstance(), 0, 20);
        }
    }
}
