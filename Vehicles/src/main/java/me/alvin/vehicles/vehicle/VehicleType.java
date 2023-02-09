package me.alvin.vehicles.vehicle;

import ca.bkaw.praeter.core.Praeter;
import ca.bkaw.praeter.core.resources.ResourcePackList;
import ca.bkaw.praeter.core.resources.pack.JsonResource;
import ca.bkaw.praeter.core.resources.pack.ResourcePack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.crafting.recipe.VehicleCraftingRecipe;
import me.alvin.vehicles.vehicle.collision.VehicleCollisionType;
import me.alvin.vehicles.vehicle.perspective.Perspective;
import me.alvin.vehicles.vehicle.seat.Seat;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import svcraft.core.world.Enableable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents a vehicle type. Will only be constructed once per vehicle type,
 * and new instances of the corresponding {@link Vehicle} will be constructed
 * for each loaded vehicle. The corresponding {@link Vehicle} class can be
 * gotten using {@link #getVehicleClass()}
 */
public class VehicleType {
    private final String id;
    private final Component name;
    private final Class<? extends Vehicle> vehicleClass;
    private final Set<Seat> seats;
    private final Seat driverSeat;
    private final Function<ArmorStand, Vehicle> loadConstructor;
    private final VehicleSpawnConstructorFunction spawnConstructor;
    private final VehicleCollisionType collisionType;
    private final List<Perspective> perspectives;
    private final Enableable enableable;
    private final NamespacedKey previewModel;
    private final NamespacedKey largePreviewModel;
    private final double maxHealth;
    private final RepairData repairData;
    private final VehicleCraftingRecipe recipe;

    public VehicleType(@NotNull String id,
                       @NotNull Component name,
                       @NotNull Class<? extends Vehicle> vehicleClass,
                       @NotNull Function<ArmorStand, Vehicle> loadConstructor,
                       @NotNull VehicleSpawnConstructorFunction spawnConstructor,
                       @NotNull VehicleCollisionType collisionType,
                       @NotNull Seat driverSeat,
                       @Nullable List<Seat> seats,
                       @Nullable List<Perspective> perspectives,
                       @NotNull Enableable enableable,
                       @NotNull NamespacedKey previewModel,
                       double maxHealth,
                       @NotNull RepairData repairData,
                       @Nullable VehicleCraftingRecipe.Builder recipe) {
        this.id = id;
        this.name = name;
        this.vehicleClass = vehicleClass;
        this.loadConstructor = loadConstructor;
        this.spawnConstructor = spawnConstructor;
        this.repairData = repairData;
        Set<Seat> seatSet = new HashSet<>();
        seatSet.add(driverSeat);
        if (seats != null) seatSet.addAll(seats);
        this.seats = seatSet;
        this.driverSeat = driverSeat;
        this.collisionType = collisionType;
        this.perspectives = perspectives == null ? Collections.emptyList() : perspectives;
        this.enableable = enableable;
        this.previewModel = previewModel;
        this.maxHealth = maxHealth;
        this.recipe = recipe == null ? null : recipe.build();

        try {
            this.largePreviewModel = this.createPreview();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create large preview for " + this.id, e);
        }
    }

    /**
     * Create a large preview from the small preview.
     *
     * @return The large preview.
     * @throws IOException If an I/O error occurs.
     */
    private NamespacedKey createPreview() throws IOException {
        NamespacedKey largePreviewModel = new NamespacedKey(this.previewModel.getNamespace(), this.previewModel.getKey() + "_large");
        ResourcePackList resourcePacks = Praeter.get().getResourceManager().getResourcePacks(SVCraftVehicles.getInstance());
        for (ResourcePack resourcePack : resourcePacks) {
            Path previewModelPath = resourcePacks.getModelPath(this.previewModel);
            JsonResource previewModel = new JsonResource(resourcePack, previewModelPath);

            JsonObject largePreview = new JsonObject();
            largePreview.addProperty("parent", this.previewModel.toString());
            JsonObject display = new JsonObject();
            largePreview.add("display", display);
            JsonObject guiDisplay = previewModel.getJson().getAsJsonObject("display").getAsJsonObject("gui");
            JsonArray scale = guiDisplay.getAsJsonArray("scale");
            JsonArray translation = guiDisplay.getAsJsonArray("translation");
            for (int i = 0; i < scale.size(); i++) {
                scale.set(i, new JsonPrimitive(scale.get(i).getAsDouble() * 4));
            }
            if (translation == null) {
                translation = new JsonArray(3);
                for (int i = 0; i < 3; i++) {
                    translation.add(0);
                }
                guiDisplay.add("translation", translation);
            }
            translation.set(0, new JsonPrimitive(translation.get(0).getAsDouble() - 28));
            translation.set(1, new JsonPrimitive(translation.get(1).getAsDouble() + 32));
            display.add("gui", guiDisplay);

            JsonResource largeModel = new JsonResource(resourcePack, resourcePack.getModelPath(largePreviewModel), largePreview);
            largeModel.save();

            NamespacedKey vanillaModel = NamespacedKey.minecraft("item/leather_boots");
            resourcePack.addCustomModelData(vanillaModel, this.previewModel);
            resourcePack.addCustomModelData(vanillaModel, largePreviewModel);
        }
        return largePreviewModel;
    }

    /**
     * Get the id of this vehicle type. This is the id the vehicle type
     * will be registered as.
     *
     * @return The id of this vehicle type
     */
    @NotNull
    public String getId() {
        return this.id;
    }

    /**
     * Get the name of the vehicle. This will be displayed to
     * players.
     *
     * @return The name component
     */
    public Component getName() {
        return this.name;
    }

    /**
     * Get the corresponding {@link Vehicle} class which will be constructed
     * for each loaded vehicle
     *
     * @return The {@link Vehicle} class for this vehicle type
     */
    @NotNull
    public Class<? extends Vehicle> getVehicleClass() {
        return this.vehicleClass;
    }

    /**
     * Construct a new instance of the vehicle from
     * an existing entity. Used when loading the vehicle
     *
     * @see Vehicle#Vehicle(ArmorStand)
     */
    public Vehicle construct(ArmorStand entity) {
        return this.loadConstructor.apply(entity);
    }

    /**
     * Spawn a new vehicle and get the instance of that vehicle.
     * Used when new vehicles are spawned in to the world by
     * a player.
     *
     * @see Vehicle#Vehicle(Location, Player, VehicleSpawnReason)
     */
    public Vehicle construct(Location location, Player creator, VehicleSpawnReason reason) {
        return this.spawnConstructor.apply(location, creator, reason);
    }

    /**
     * Get the seats for the vehicle, including the driver seat
     *
     * @return A set of seats
     */
    @NotNull
    public Set<Seat> getSeats() {
        return this.seats;
    }

    /**
     * Get the seat that will allow the passenger to drive the vehicle
     *
     * @return The driver seat
     */
    @NotNull
    public Seat getDriverSeat() {
        return this.driverSeat;
    }

    /**
     * Get the collision type the vehicle will use for
     * collision checks.
     *
     * @return The collision type
     */
    @NotNull
    public VehicleCollisionType getCollisionType() {
        return this.collisionType;
    }

    /**
     * Get the {@link Perspective}s players can view when they are in the vehicle.
     *
     * @return The list of perspectives.
     */
    @NotNull
    public List<Perspective> getPerspectives() {
        return this.perspectives;
    }

    /**
     * Get the max health the vehicle can have, is also the
     * health the vehicle starts at.
     *
     * @return The max health
     */
    public double getMaxHealth() {
        return this.maxHealth;
    }

    /**
     * Get the {@link Enableable} that controls where this vehicle type is enabled.
     *
     * @return The Enableable.
     */
    public Enableable getEnableable() {
        return this.enableable;
    }

    /**
     * Get the crafting recipe that is used to craft this vehicle type
     * in a Vehicle Crafting Table. May be null in case the vehicle can
     * not be crafted.
     *
     * @return The recipe, or null if the vehicle type is not craftable.
     */
    @Nullable
    public VehicleCraftingRecipe getRecipe() {
        return this.recipe;
    }

    /**
     * Get the {@link RepairData}.
     *
     * @return The data.
     */
    public RepairData getRepairData() {
        return this.repairData;
    }

    /**
     * Get the small model (one slot) to use when previewing the vehicle.
     *
     * @return The key to the model.
     */
    public NamespacedKey getPreviewModel() {
        return this.previewModel;
    }

    /**
     * Get the large model (4 slots) to use when previewing the vehicle.
     *
     * @return The key to the model.
     */
    public NamespacedKey getLargePreviewModel() {
        return this.largePreviewModel;
    }
}
