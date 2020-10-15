package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.util.ExtraPersistentDataTypes;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicle.seat.SeatData;
import me.svcraft.minigames.SVCraft;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Vehicle {

    // Persistent Data Keys

    public static final NamespacedKey VEHICLE_ID = new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle_id");
    public static final NamespacedKey LOCATION = new NamespacedKey(SVCraftVehicles.getInstance(), "location");
    public static final NamespacedKey CURRENT_FUEL = new NamespacedKey(SVCraftVehicles.getInstance(), "current_fuel");

    // Fields

    /**
     * The main entity of the vehicle. Used for saving persistent data
     */
    protected @NotNull ArmorStand entity;
    // Movement
    private @NotNull Location location;
    private float speed = 0;
    private float forwardMovement = 0;
    private float sideMovement = 0;
    // Fuel
    private int currentFuel = 0;
    private int fuelUsage = 0;
    private int maxFuel = 0;
    // Seats
    private final Map<Seat, SeatData> seatData = new HashMap<>();
    // Attachment
    private Map<Vehicle, AttachmentData> attachedVehicles;
    private Vehicle attachedTo;


    // Saving and loading

    /**
     * Construct a vehicle from an existing entity, will load the vehicle
     * using all the stored data on the entity.
     *
     * @param entity The entity to load from
     * @throws NullPointerException If any of the data is missing from the entity
     */
    public Vehicle(@NotNull ArmorStand entity) {
        this.entity = entity;
        PersistentDataContainer data = this.entity.getPersistentDataContainer();
        // These might throw NPE's but if they do it will be caught
        // inside the method loading the vehicle. In case of an NPE
        // the loaded vehicle is invalid.

        this.currentFuel = Objects.requireNonNull(data.get(CURRENT_FUEL, PersistentDataType.INTEGER));
        this.location    = Objects.requireNonNull(data.get(LOCATION, ExtraPersistentDataTypes.LOCATION));

        DebugUtil.debug("Constructed/Loaded vehicle of class " + this.getClass().getName());
    }

    /**
     * Construct a new vehicle at the specified location.
     *
     * @param location The location to spawn the vehicle at
     * @param creator The player that created the vehicle
     */
    public Vehicle(@NotNull Location location, @NotNull Player creator) {
        this.location = location;
        this.location.setPitch(0);
        this.entity = spawnArmorStand(this.location);

        this.updateRenderedLocation();

        DebugUtil.debug(creator.getName() + " spawned a vehicle of class " + this.getClass().getName());

        // TODO: VehicleSpawnReason (maybe?) for like crafting or (spawn egg) or command
    }

    /**
     * Utility method used for spawning armor stands. Will make the armor stand
     * have no gravity (and be invisible)
     *
     * @param location The location to spawn the armor stand
     * @return The spawned armor stand
     */
    public static ArmorStand spawnArmorStand(Location location) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setGravity(false);
        return armorStand;
    }

    /**
     * Save {@link PersistentDataContainer} data on the main entity.
     */
    public void save() {
        PersistentDataContainer data = this.entity.getPersistentDataContainer();
        data.set(VEHICLE_ID,   PersistentDataType.STRING,         this.getType().getId());
        data.set(CURRENT_FUEL, PersistentDataType.INTEGER,        this.currentFuel);
        data.set(LOCATION,     ExtraPersistentDataTypes.LOCATION, this.location);

        DebugUtil.debug("Saving vehicle");
    }

    /**
     * Called when the vehicle is unloaded because the chunk the main entity is in was unloaded.
     * Default behaviour is to save the vehicle using {@link #save()}. Subclasses can override
     * this and do other stuff before the vehicle is unloaded and removed from the loaded
     * vehicles list.
     */
    public void unload() {
        DebugUtil.debug("Unloading vehicle");
        this.save();
    }

    /**
     * Remove the vehicle and all entities asosiated with it.
     * Will also remove the vehicle from the loaded vehicles list,
     * therefore it is not safe to iterate over all vehicles while
     * removing some as that will throw a ConcurrentModificationException
     */
    public void remove() {
        DebugUtil.debug("Removing vehicle");
        if (this.entity.isValid()) this.entity.remove();

        for (SeatData seatData : this.seatData.values()) {
            seatData.exitSeat();
        }

        SVCraftVehicles.getInstance().getLoadedVehicles().remove(this.entity, this);
    }


    public abstract VehicleType getType();

    @NotNull
    public ArmorStand getEntity() {
        return this.entity;
    }

    // Coloring

    public boolean canBeColored() {
        return false;
    }

    /**
     * Color the specified armor stand. Can be used inside {@link #setColor(Color)} if the
     * vehicle uses multiple entities to render the vehicle.
     *
     * @param entity The entity to color the helmet of
     * @param color The color to set
     * @return See {@link #setColor(Color)} return value
     */
    protected boolean colorArmorStand(ArmorStand entity, Color color) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return false;
        ItemStack helmet = equipment.getHelmet();
        if (helmet == null) return false;
        ItemMeta meta = helmet.getItemMeta();
        if (meta == null) return false;
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(color);
            helmet.setItemMeta(meta);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the color of the vehicle.
     *
     * @param color The color to set
     * @return {@code true} if the color was successfully applied, {@code false} if not, for
     * example due to the vehicle not being colorable or if the entity does not wear a leather
     * armor in it's helmet slot
     */
    public boolean setColor(Color color) {
        if (!this.canBeColored()) return false;

        return this.colorArmorStand(this.entity, color);
    }

    // Movement
    //<editor-fold desc="Movement related methods" defaultstate="collapsed">

    public abstract float getAccelerationSpeed();

    public abstract float getMaxSpeed();

    /**
     * Get the location of the vehicle.
     *
     * @return The location of this vehicle
     */
    @NotNull
    public Location getLocation() {
        return this.location;
    }

    /**
     * Set the location of the vehicle. This will not render the vehicle at that
     * location but only change the internal variable holding the current location.
     * To update the rendered position use {@link #updateRenderedLocation()}
     *
     * @param location The new location of the vehicle. The location is not cloned so if you
     *                 re-use this location instance for other calculations that can alter
     *                 the location of the vehicle.
     */
    public void setLocation(@NotNull Location location) {
        this.location = location;
    }

    public void tick() {
        if (this.isAttached()) return;

        LivingEntity driver = this.getDriver();

        // Temporary code, packet interception will be used instead of looking at slots
        if (driver instanceof Player) {
            int slot = ((Player) driver).getInventory().getHeldItemSlot();
            if (slot == 0) this.forwardMovement = 1;
            else if (slot == 2) this.forwardMovement = -1;
            else this.forwardMovement = 0;

            if (slot == 4) this.sideMovement = -1;
            else if (slot == 5) this.sideMovement = 1;
            else this.sideMovement = 0;
        }

        this.updateSpeed();

        if (this.speed != 0) {
            this.calculateLocation();
            this.updateRenderedLocation();
            this.updateRenderedPassengerPositions();

            if (this.attachedVehicles != null) {
                this.updateAttachedVehicles();
            }
        }

        if (this.usesFuel()) {
            this.currentFuel -= this.fuelUsage * (this.speed / this.getMaxSpeed());
            if (this.currentFuel < 0) {
                this.currentFuel = 0;
            }
        }
    }

    public void updateSpeed() {
        if (this.forwardMovement != 0 && this.speed < this.getMaxSpeed()) {
            this.speed += this.getAccelerationSpeed() * this.forwardMovement;
        }

        if (Math.abs(this.speed) < 0.01) {
            this.speed = 0;
        }
    }

    /**
     * Calculate where the vehicle should be at. Does not update where the entity
     * is rendered. For that use {@link #updateRenderedLocation()}
     */
    public void calculateLocation() {
        Vector direction = this.location.getDirection();
        direction.multiply(this.speed / 20);
        this.location.add(direction);
        if (this.sideMovement != 0) {
            this.location.setYaw(this.location.getYaw() + this.sideMovement * 2);
        }

        this.speed *= 0.9;
    }

    /**
     * Update the location of the vehicle to the specified location. Can be
     * overridden in subclasses if the vehicle has multiple parts. Should
     * be followed by {@link #updateRenderedPassengerPositions()}. Called every tick
     * if the vehicle's speed is not 0.
     */
    public void updateRenderedLocation() {
        // this.entity.teleport(this.location);
        SVCraftVehicles.getInstance().getNMS().setEntityLocation(this.entity, this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), this.location.getPitch());
    }

    /**
     * Reposition all passengers to the correct location. Called after
     * {@link #updateRenderedLocation()} every tick if the vehicle's
     * speed is not 0.
     */
    public void updateRenderedPassengerPositions() {
        for (Map.Entry<Seat, SeatData> entry : this.seatData.entrySet()) {
            Seat seat = entry.getKey();
            SeatData seatData = entry.getValue();
            if (!seatData.isValid()) continue;

            Location location = seat.getRelativePos().relativeTo(this.location);
            if (seat.hasOffsetYaw()) location.setYaw(location.getYaw() + seat.getOffsetYaw());
            SVCraftVehicles.getInstance().getNMS().setEntityLocation(seatData.getRiderEntity(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        }
    }
    // </editor-fold>

    // Fuel
    //<editor-fold desc="Fuel related methods" defaultstate="collapsed">

    /**
     * Whether the vehicle uses fuel or not. Can be overriden by subclasses
     * that do not use fuel.
     *
     * @return {@code true} if the vehicle uses fuel and {@code false} if not.
     */
    public boolean usesFuel() {
        return true;
    }

    /**
     * Gets the current amount of fuel the vehicle has.
     *
     * @return The amount of fuel the vehicle has
     * @throws IllegalStateException If the vehicle does not use fuel.
     */
    public int getCurrentFuel() {
        if (!this.usesFuel()) throw new IllegalStateException("Can not get current fuel on vehicle that does not use fuel");
        return this.currentFuel;
    }

    /**
     * Sets the current amount of fuel the vehicle has
     *
     * @param currentFuel The new amount of fuel
     * @throws IllegalStateException If the vehicle does not use fuel.
     */
    public void setCurrentFuel(int currentFuel) {
        if (!this.usesFuel()) throw new IllegalStateException("Can not set current fuel on vehicle that does not use fuel");
        this.currentFuel = currentFuel;
    }

    /**
     * Gets the current amount of fuel the vehicle uses
     *
     * @return The amount of fuel the vehicle uses
     * @throws IllegalStateException If the vehicle does not use fuel.
     */
    public int getFuelUsage() {
        if (!this.usesFuel()) throw new IllegalStateException("Can not get fuel usage on vehicle that does not use fuel");
        return this.fuelUsage;
    }

    /**
     * Sets the amount of fuel the vehicle uses
     *
     * @param fuelUsage The new fuel usage
     * @throws IllegalStateException If the vehicle does not use fuel.
     */
    public void setFuelUsage(int fuelUsage) {
        if (!this.usesFuel()) throw new IllegalStateException("Can not set fuel usage on vehicle that does not use fuel");
        this.fuelUsage = fuelUsage;
    }

    /**
     * Gets the maximum amount of fuel the vehicle can hold
     *
     * @return The maximum amount of fuel the vehicle can hold
     * @throws IllegalStateException If the vehicle does not use fuel.
     */
    public int getMaxFuel() {
        if (!this.usesFuel()) throw new IllegalStateException("Can not get max fuel on vehicle that does not use fuel");
        return this.maxFuel;
    }

    /**
     * Sets the maximum amount of fuel the vehicle can hold
     *
     * @param maxFuel The new maximum fuel
     * @throws IllegalStateException If the vehicle does not use fuel.
     */
    public void setMaxFuel(int maxFuel) {
        if (!this.usesFuel()) throw new IllegalStateException("Can not set max fuel on vehicle that does not use fuel");
        this.maxFuel = maxFuel;
    }
    // </editor-fold>

    // Seats
    //<editor-fold desc="Seat related methods" defaultstate="collapsed">


    /**
     * Get the map used for storing passenger seat data. This method should
     * preferably not be used and the other seat related methods should be
     * used for modifying the seat data.
     *
     * @return The seat data
     */
    @NotNull
    public Map<Seat, SeatData> getSeatData() {
        return this.seatData;
    }

    @Nullable
    public LivingEntity getPassenger(Seat seat) {
        SeatData seatData = this.seatData.get(seat);
        if (seatData == null) return null;
        if (!seatData.isValid()) {
            SVCraftVehicles.getInstance().getLogger().warning("Invalid seat data");
            // seatData.exitSeat();
            // this.seatData.remove(seat);
            return null;
        }
        return seatData.getPassenger();
    }

    @Nullable
    public LivingEntity getDriver() {
        return this.getPassenger(this.getType().getDriverSeat());
    }

    /**
     * Get the seat that the specified passenger is currently sitting in. Or null
     * if none
     *
     * @param passenger The passenger to get the seat of
     * @return The seat the passenger is in. Or null if none
     */
    @Nullable
    public Seat getPassengerSeat(LivingEntity passenger) {
        for (Map.Entry<Seat, SeatData> entry : this.seatData.entrySet()) {
            SeatData seatData = entry.getValue();
            if (seatData.getPassenger() == passenger && seatData.isValid()) return entry.getKey();
        }
        return null;
    }

    @Nullable
    public Seat getNearestAvailableSeat(@NotNull Location location) {
        Seat closestSeat = null;
        double closestDistance = 0;

        Location vehicleLocation = this.location;
        for (Seat seat : this.getType().getSeats()) {
            if (this.getPassenger(seat) != null) continue;

            double distance = seat.getRelativePos().relativeTo(vehicleLocation).distanceSquared(location);
            if (closestDistance == 0 || distance < closestDistance) {
                closestSeat = seat;
                closestDistance = distance;
            }
        }
        return closestSeat;
    }

    /**
     * Set the passenger for the specified seat in this vehicle. If there
     * was a previous passenger it will be exited from the seat before
     * the new passenger is set.
     *
     * @param seat The seat to put the new passenger
     * @param passenger The new passenger, or null to empty the specified seat.
     */
    public void setPassenger(@NotNull Seat seat, @Nullable LivingEntity passenger) {
        if (this.seatData.containsKey(seat)) {
            SeatData oldSeatData = this.seatData.get(seat);
            this.seatData.remove(seat);
            oldSeatData.exitSeat();
        }

        if (passenger != null) {
            DebugUtil.debug("Adding new seat data in seat");
            this.seatData.put(seat, new SeatData(this, seat, passenger));
            SVCraftVehicles.getInstance().getCurrentVehicleMap().put(passenger, this);
            DebugUtil.debug("Adding "+ passenger.getName() + " to current vehicles");
        }
        DebugUtil.debug("setPassenger was called. The size of seatData is now "+ this.seatData.size());
    }

    /**
     * Attempt to add the entity as a passenger in the vehicle
     *
     * @param passenger The passenger that wants to enter the vehicle
     * @return {@code true} if the passenger entered the vehicle, {@code false} if not
     */
    public boolean addPassenger(@NotNull LivingEntity passenger) {
        DebugUtil.debug("Entering passenger "+ passenger.getName());
        Seat seat = this.getNearestAvailableSeat(passenger.getLocation());
        DebugUtil.debugVariable("seat", seat);
        if (seat == null) return false;
        this.setPassenger(seat, passenger);
        return true;
    }

    /**
     * Check if the entity is inside this vehicle
     *
     * @param entity The entity to check whether they're a passenger or not
     * @return {@code true} if the entity is a passenger {@code false} if not
     */
    public boolean isPassenger(LivingEntity entity) {
        for (SeatData seatData : this.seatData.values()) {
            DebugUtil.debug("isPassenger check for seatData that is valid? "+ seatData.isValid() + " and has a passenger called "+ seatData.getPassenger().getName());
            if (seatData.getPassenger() == entity && seatData.isValid()) {
                return true;
            }
        }
        return false;
    }
    // </editor-fold>

    // Attachment
    //<editor-fold desc="Attachment related methods" defaultstate="collapsed">

    /**
     * @return Get the vehicle this vehicle is attached to, or null if this vehicle is not
     * attached to any vehicle
     */
    @Nullable
    public Vehicle getAttachedTo() {
        return this.attachedTo;
    }

    /**
     * @return {@code true} If the vehicle is attached to another vehicle. {@code false} otherwise
     */
    public boolean isAttached() {
        return this.attachedTo != null;
    }

    public void attachVehicle(Vehicle vehicle, AttachmentData attachmentData) {
        if (this.attachedVehicles == null) this.attachedVehicles = new HashMap<>();

        if (vehicle.isAttached()) vehicle.detach();

        this.attachedVehicles.put(vehicle, attachmentData);
        vehicle.attachedTo = this;
        // TODO: Play sound maybe?
    }

    /**
     * Detatch the specified vehicle from this vehicle.
     *
     * @param vehicle The vehicle to detach
     */
    public void detachVehicle(Vehicle vehicle) {
        if (this.attachedVehicles == null || !this.attachedVehicles.containsKey(vehicle))
            throw new IllegalStateException("The specified vehicle is not attached to this vehicle");

        this.attachedVehicles.remove(vehicle);
        vehicle.attachedTo = null;
        // TODO: Play sound maybe?
    }

    /**
     * Detach this vehicle from the vehicle it is attached to.
     */
    public void detach() {
        if (this.attachedTo == null) throw new IllegalStateException("Can not detach vehicle as it is not attached");

        this.attachedTo.detachVehicle(this);
    }

    /**
     * Update the positions of attached vehicles.
     */
    private void updateAttachedVehicles() {
        for (Map.Entry<Vehicle, AttachmentData> entry : this.attachedVehicles.entrySet()) {
            Vehicle attachedVehicle = entry.getKey();
            AttachmentData attachmentData = entry.getValue();

            attachedVehicle.setLocation(attachmentData.getRelativePos().relativeTo(this.location));
            attachedVehicle.updateRenderedLocation();
            attachedVehicle.updateRenderedPassengerPositions();
        }
    }
    // </editor-fold>
}
