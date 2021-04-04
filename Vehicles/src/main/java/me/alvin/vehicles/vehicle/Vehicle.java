package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.actions.FuelAction;
import me.alvin.vehicles.actions.SwitchSeatAction;
import me.alvin.vehicles.nms.VehicleSteeringMovement;
import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.util.ExtraPersistentDataTypes;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.util.ni.NIArmorStand;
import me.alvin.vehicles.util.ni.NIE;
import me.alvin.vehicles.vehicle.action.VehicleAction;
import me.alvin.vehicles.vehicle.action.VehicleClickAction;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import me.alvin.vehicles.vehicle.collision.AABBCollision;
import me.alvin.vehicles.vehicle.collision.VehicleCollisionType;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicle.seat.SeatData;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class Vehicle {

    // Persistent Data Keys

    public static final NamespacedKey VEHICLE_ID = new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle_id");
    public static final NamespacedKey LOCATION = new NamespacedKey(SVCraftVehicles.getInstance(), "location");
    public static final NamespacedKey CURRENT_FUEL = new NamespacedKey(SVCraftVehicles.getInstance(), "current_fuel");
    public static final NamespacedKey COLOR = new NamespacedKey(SVCraftVehicles.getInstance(), "color");

    // Constants

    /**
     * The amount of blocks vehicles fall per tick. Same value
     * as for boats, minecraft and other vanilla "vehicles"
     */
    public static final double GRAVITY = 0.04D;

    // Fields

    /**
     * The main entity of the vehicle. Used for saving persistent data
     */
    protected @NotNull ArmorStand entity;
    /**
     * The main entity as a non interpolating armor stand. This will only
     * be present if the vehicle is in motion.
     */
    protected @Nullable NIArmorStand niEntity;
    /**
     * The invisible slime around the vehicle that ensures client send
     * right click packets.
     */
    protected Slime slime;
    protected NIE<Slime> niSlime;
    public RelativePos debugRelativePos; // TEMP
    // Movement
    protected @NotNull Location location;
    protected float speed = 0;
    protected double velX = 0.0;
    protected double velY = 0.0;
    protected double velZ = 0.0;
    public final VehicleSteeringMovement movement = new VehicleSteeringMovement();
    /**
     * Whether the vehicle is on the ground or not. Is updated in
     * {@link #applyVelocity()}.
     */
    protected boolean onGround = true;
    /**
     * The highest block the vehicle is colliding with.
     */
    protected Block highestCollisionBlock;
    // Fuel
    private int currentFuel = 0;
    private int fuelUsage = 0;
    private int maxFuel = 0;
    // Seats
    private final Map<Seat, SeatData> seatData = new HashMap<>();
    // Attachment
    private Map<Vehicle, AttachmentData> attachedVehicles;
    private Vehicle attachedTo;
    // Actions
    private final List<VehicleMenuAction> menuActions = new ArrayList<>();
    private final List<VehicleClickAction> clickActions = new ArrayList<>();
    private final List<VehicleAction> allActions = new ArrayList<>();


    // Saving and loading

    /**
     * Load a vehicle from an existing entity, will load the vehicle
     * using all the stored data on the entity.
     *
     * @param entity The entity to load from
     * @throws NullPointerException If any of the data is missing from the entity
     */
    public Vehicle(@NotNull ArmorStand entity) {
        this.entity = entity;
        PersistentDataContainer data = this.entity.getPersistentDataContainer();
        // These might throw NPEs but if they do it will be caught
        // inside the method loading the vehicle. In case of an NPE
        // the loaded vehicle is invalid.

        this.currentFuel = Objects.requireNonNull(data.get(CURRENT_FUEL, PersistentDataType.INTEGER));
        this.location    = Objects.requireNonNull(data.get(LOCATION, ExtraPersistentDataTypes.LOCATION));

        Bukkit.getScheduler().runTaskLater(SVCraftVehicles.getInstance(), () -> {
            this.postInit();
            if (this.canBeColored()) {
                Integer rgbColor = data.get(COLOR, PersistentDataType.INTEGER);
                if (rgbColor != null) {
                    this.setColor(Color.fromRGB(rgbColor));
                }
            }
            for (VehicleAction action : this.allActions) {
                action.onLoad(this, data);
            }
        }, 1L);

        DebugUtil.debug("Loaded vehicle of class " + this.getClass().getName());
    }

    /**
     * Create a new vehicle at the specified location.
     *
     * @param location The location to spawn the vehicle at
     * @param creator The player that created the vehicle
     * @param reason The reason the vehicle was spawned
     */
    public Vehicle(@NotNull Location location, @NotNull Player creator, @NotNull VehicleSpawnReason reason) {
        this.location = location;
        this.location.setPitch(0);
        this.entity = spawnArmorStand(this.location);
        this.entity.setPersistent(true);

        DebugUtil.debug(creator.getName() + " spawned a vehicle of class " + this.getClass().getName());

        Bukkit.getScheduler().runTaskLater(SVCraftVehicles.getInstance(), () -> {
            this.postInit();
            this.updateRenderedLocation();
            if (this.canBeColored()) {
                this.setColor(this.getDefaultColor());
            }
        }, 1L);
    }

    /**
     * Called when the vehicle has been fully constructed.
     *
     * <p>This is the method that registers all {@link VehicleAction}s for the vehicle.</p>
     *
     * <p>Override this method if want to register other actions or call methods
     * like {@link #setMaxFuel(int)}, etc. Calling the super method is recommended
     * as it will register important actions like a fuel action if the vehicle uses
     * fuel and the switch seat action.</p>
     *
     * <p>This method is called right after a vehicle has been spawned or loaded.
     * More specifically, 1 tick after the vehicle has been loaded (as this can
     * not be called in the Vehicle constructor as when that is called, methods
     * overridden by subclasses will not yet have been overridden).</p>
     */
    protected void postInit() {
        if (this.usesFuel()) this.addAction(FuelAction.INSTANCE);
        if (this.getType().getSeats().size() > 1) this.addAction(SwitchSeatAction.INSTANCE);
        this.spawnSlime();
    }

    private void spawnSlime() {
        int size = 3;
        VehicleCollisionType collisionType = this.getType().getCollisionType();
        if (collisionType instanceof AABBCollision) {
            BoundingBox boundingBox = ((AABBCollision) collisionType).getBoundingBox();
            size = (int) Math.floor(Math.max(boundingBox.getMaxX(), boundingBox.getMaxY()) * 2); // slime size = (slime size in blocks) * 2
        }
        int finalSize = size;
        this.slime = this.location.getWorld().spawn(this.location, Slime.class, slime -> {
            slime.setSize(finalSize);
            slime.setAI(false);
            slime.setInvulnerable(true);
            slime.setSilent(true);
            slime.setPersistent(false);
            slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0, false, false));
        });
        SVCraftVehicles.getInstance().getLoadedVehicles().put(this.getEntity(), this);
        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.getEntity(), this);
        SVCraftVehicles.getInstance().getVehiclePartMap().put(this.slime, this);
    }

    /**
     * Utility method used for spawning armor stands. Will make the armor stand
     * have no gravity (and be invisible)
     *
     * @param location The location to spawn the armor stand
     * @return The spawned armor stand
     */
    @NotNull
    public static ArmorStand spawnArmorStand(Location location) {
        if (location.getWorld() == null) throw new IllegalArgumentException("location has to have a world");
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        setupArmorStand(armorStand);
        return armorStand;
    }

    /**
     * Utility method used when spawning armor stands to set them up.
     *
     * <p>Note that this method will set the entity's persistent to
     * false meaning they wont be saved when chunks unload. The main
     * vehicle entity needs to re-set this back to true.</p>
     *
     * @param armorStand The armor stand to set up
     */
    public static void setupArmorStand(ArmorStand armorStand) {
        armorStand.setGravity(false);
        armorStand.addDisabledSlots(EquipmentSlot.HEAD);
        armorStand.setPersistent(false);
    }

    /**
     * Save {@link PersistentDataContainer} data on the main entity.
     */
    public void save() {
        PersistentDataContainer data = this.entity.getPersistentDataContainer();
        data.set(VEHICLE_ID,   PersistentDataType.STRING,         this.getType().getId());
        data.set(CURRENT_FUEL, PersistentDataType.INTEGER,        this.currentFuel);
        data.set(LOCATION,     ExtraPersistentDataTypes.LOCATION, this.location);
        if (this.canBeColored()) data.set(COLOR, PersistentDataType.INTEGER, this.getColor().asRGB());

        for (VehicleAction action : this.allActions) {
            action.onSave(this, data);
        }

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
        this.slime.remove();
        SVCraftVehicles.getInstance().getLoadedVehicles().remove(this.getEntity());
        SVCraftVehicles.getInstance().getVehiclePartMap().entrySet().removeIf(entry -> entry.getValue() == this);
    }

    /**
     * Remove the vehicle and all entities associated with it.
     * Will also remove the vehicle from the loaded vehicles list,
     * therefore it is not safe to iterate over all vehicles while
     * removing some as that will throw a ConcurrentModificationException
     */
    public void remove() {
        DebugUtil.debug("Removing vehicle");

        for (VehicleAction action : this.allActions) {
            action.onRemove(this);
        }

        if (this.niEntity != null) this.niEntity.remove();
        this.entity.remove();

        for (SeatData seatData : this.seatData.values()) {
            seatData.exitSeat();
        }

        this.slime.remove();

        SVCraftVehicles.getInstance().getLoadedVehicles().remove(this.entity, this);
        SVCraftVehicles.getInstance().getVehiclePartMap().entrySet().removeIf(entry -> entry.getValue() == this);
    }


    @NotNull
    public abstract VehicleType getType();

    @NotNull
    public ArmorStand getEntity() {
        return this.entity;
    }

    @Nullable
    public NIArmorStand getNIEntity() {
        return this.niEntity;
    }

    // Coloring
    //<editor-fold desc="Coloring related methods" defaultstate="collapsed">

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
            equipment.setHelmet(helmet);
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

    /**
     * Get the current color of the vehicle. Will return {@code null}
     * if the vehicle can not be colored.
     *
     * @return The color of the vehicle
     */
    public Color getColor() {
        if (!this.canBeColored()) return null;

        EntityEquipment equipment = this.entity.getEquipment();
        if (equipment == null) return null;
        ItemStack helmet = equipment.getHelmet();
        if (helmet == null) return null;
        ItemMeta meta = helmet.getItemMeta();
        if (meta == null) return null;
        if (meta instanceof LeatherArmorMeta) {
            return ((LeatherArmorMeta) meta).getColor();
        } else {
            return null;
        }
    }

    /**
     * Get the default color of the vehicle. Will be set when a new vehicle is spawned
     * if the vehicle is colorable. The default default color is white.
     *
     * @return The default color
     */
    public Color getDefaultColor() {
        return Color.WHITE;
    }
    // </editor-fold>

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

    /**
     * Get the current roll rotation of the vehicle. Subclasses can override this
     * method and return a value other than 0 when that fits, but by default this
     * method will simply return 0.
     *
     * <p>This is used in RelativePos calculations to align points correctly when
     * vehicles are rolling.</p>
     *
     * @return The vehicle's current roll.
     */
    public float getRoll() {
        return 0.0F;
    }

    /**
     * Called each tick.
     *
     * <p>The default tick cycle is:</p>
     * <pre>
     * 1: updateSpeed
     * if (isMoving()) {
     *     2: calculateVelocity
     *         3: calculateGravity
     *     4: applyVelocity
     *     5: updateRenderedLocation
     *     6: updateRenderedPassengerPositions
     *     7: spawnParticles
     *     if attached {
     *         7: updateAttachedVehicles
     *     }
     * }
     * </pre>
     *
     * <p>Note that vehicles that are attached do not tick at all.</p>
     */
    public void tick() {
        if (this.isAttached()) return;

        if (this.debugRelativePos != null) {
            Location location = this.debugRelativePos.relativeTo(this.location, this.getRoll());
            location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0);
        }
        if (true) {
            VehicleCollisionType collisionType = this.getType().getCollisionType();
            if (collisionType instanceof AABBCollision) {
                BoundingBox boundingBox = ((AABBCollision) collisionType).getBoundingBox();
                for (double x = boundingBox.getMinX(); x <= boundingBox.getMaxX(); x += 0.5) {
                    for (double y = boundingBox.getMinY(); y <= boundingBox.getMaxY(); y += 0.5) {
                        for (double z = boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z += 0.5) {
                            if (x == boundingBox.getMinX() || (Math.abs(x - boundingBox.getMaxX()) < 0.1D)
                            ||  y == boundingBox.getMinY() || (Math.abs(y - boundingBox.getMaxY()) < 0.1D)
                            ||  z == boundingBox.getMinZ() || (Math.abs(z - boundingBox.getMaxZ()) < 0.1D)) {
                                DebugUtil.debugLocation(this.location.clone().add(x, y, z));
                            }
                        }
                    }
                }
            }
        }

        this.updateSpeed();

        if (this.isMoving()) {
            if (!this.isNonInterpolating()) {
                this.setNonInterpolating(true);
            }
            this.calculateVelocity();
            this.applyVelocity();
            this.updateRenderedLocation();
            this.updateRenderedPassengerPositions();
            this.spawnParticles();

            if (this.attachedVehicles != null) {
                this.updateAttachedVehicles();
            }

            if (this.usesFuel()) {
                this.currentFuel -= this.fuelUsage * (this.speed / this.getMaxSpeed());
                if (this.currentFuel < 0) {
                    this.currentFuel = 0;
                }
            }
        } else {
            if (this.isNonInterpolating() && this.seatData.size() <= 0) {
                this.setNonInterpolating(false);
            }
        }
    }

    /**
     * Determine whether the vehicle is moving or not. The return value
     * of this method determines whether the vehicle should tick or skip
     * most of the tick cycle.
     *
     * <p>Default implementation is to check whether the {@link #speed}
     * has a non-zero value.</p>
     *
     * <p>If this returns false the vehicle will skip most of the tick
     * cycle.</p>
     *
     * @return Whether the vehicle is moving and should tick.
     *
     * @see #tick()
     */
    public boolean isMoving() {
        return this.speed != 0;
    }

    /**
     * Determine whether the vehicle can accelerate. The return value of this
     * method determines whether the vehicle should be able to accelerate
     * forward in the {@link #updateSpeed()} method.
     *
     * <p>The default implementation is, for vehicles that use fuel, to check
     * whether it has fuel</p>
     *
     * @return Whether the vehicle can accelerate forward.
     */
    public boolean canAccelerate() {
        if (!this.usesFuel()) return true;

        return this.currentFuel > 0;
    }

    /**
     * Update the speed of the vehicle based on the driver movement. This method
     * does not update the velocity.
     * <br>
     * Default implementation increases the speed by {@link #getAccelerationSpeed()}
     * if forward movement is positive, also sets speed to 0 if it's absolute value
     * is less than <code>0.01</code>.
     *
     * @see #tick()
     */
    public void updateSpeed() {
        if (this.movement.forward != 0 && Math.abs(this.speed) < this.getMaxSpeed() && this.canAccelerate()) {
            this.speed += this.getAccelerationSpeed() * this.movement.forward;
        }

        if (Math.abs(this.speed) < 0.01) {
            this.speed = 0;
        }
    }

    /**
     * Calculate the velocity for the vehicle, this is then applied to the
     * location using {@link #applyVelocity()} (next in the tick cycle).
     *
     * @see #tick()
     */
    public abstract void calculateVelocity();

    /**
     * Do a block collision check, checking if there is a block at the
     * specified location and whether that block is passable.
     *
     * @param x X coordinate to check.
     * @param y Y coordinate to check.
     * @param z Z coordinate to check.
     * @param axis The axis the check is being made in
     * @return true if collision was found, otherwise false
     */
    protected boolean doCollisionBlockCheck(double x, double y, double z, Axis axis) {
        // DebugUtil.debugLocation(new Location(this.location.getWorld(), x, y, z));
        Block block = this.location.getWorld().getBlockAt(Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z));
        if (!block.isPassable()) {
            if (this.highestCollisionBlock == null || block.getY() > this.highestCollisionBlock.getY()) this.highestCollisionBlock = block;
            return true;
        }
        return false;
    }

    /**
     * Apply the vehicle's current velocity to the {@link #location} field
     * to what extent is possible taking collision into account.
     *
     * <p>This does not update where the entity is rendered. For that use
     * {@link #updateRenderedLocation()}.</p>
     *
     * @see #tick()
     */
    public void applyVelocity() {
        VehicleCollisionType collisionType = this.getType().getCollisionType();
        if (collisionType instanceof AABBCollision) {
            double x = this.location.getX();
            double y = this.location.getY();
            double z = this.location.getZ();
            double newX = x + this.velX;
            double newY = y + this.velY;
            double newZ = z + this.velZ;
            double oldVelX = this.velX;
            double oldVelZ = this.velZ;
            BoundingBox boundingBox = ((AABBCollision) collisionType).getBoundingBox();
            this.onGround = false;
            this.highestCollisionBlock = null;
            // x-collision
            if (this.velX != 0) {
                for (double offsetY = boundingBox.getMaxY(); offsetY >= boundingBox.getMinY(); offsetY--) {
                    for (double offsetZ = boundingBox.getMinZ(); offsetZ <= boundingBox.getMaxZ(); offsetZ++) {
                        if (this.doCollisionBlockCheck(newX + (this.velX > 0 ? boundingBox.getMaxX() : boundingBox.getMinX()), y + offsetY, z + offsetZ, Axis.X)) {
                            this.velX = 0;
                            break;
                        }
                    }
                }
            }
            // y-collision
            if (this.velY != 0) {
                for (double offsetX = boundingBox.getMinX(); offsetX <= boundingBox.getMaxX(); offsetX++) {
                    for (double offsetZ = boundingBox.getMinZ(); offsetZ <= boundingBox.getMaxZ(); offsetZ++) {
                        if (this.doCollisionBlockCheck(x + offsetX, newY + (this.velY > 0 ? boundingBox.getMaxY() : boundingBox.getMinY()), z + offsetZ, Axis.Y)) {
                            if (this.velY < 0) this.onGround = true;
                            this.velY = 0;
                            break;
                        }
                    }
                }
            }
            // z-collision
            if (this.velZ != 0) {
                for (double offsetX = boundingBox.getMinX(); offsetX <= boundingBox.getMaxX(); offsetX++) {
                    for (double offsetY = boundingBox.getMaxY(); offsetY >= boundingBox.getMinY(); offsetY--) {
                        if (this.doCollisionBlockCheck(x + offsetX, y + offsetY, newZ + (this.velZ > 0 ? boundingBox.getMaxZ() : boundingBox.getMinZ()), Axis.Z)) {
                            this.velZ = 0;
                            break;
                        }
                    }
                }
            }
            if (this.highestCollisionBlock != null && this.highestCollisionBlock.getY() == this.location.getBlockY()/* && !this.collides(this.location.clone().add(this.velX, this.velY, this.velZ).add(0, 1.01, 0))*/) {
                this.location.setY(this.location.getBlockY() + 1);
                this.velX = oldVelX;
                this.velZ = oldVelZ;
            }
        }

        // Apply velocity
        this.location.add(this.velX, this.velY, this.velZ);
    }

    /**
     * Add the gravity to the vehicle's velocity. Subclasses can override
     * this method to for example not apply gravity at certain times.
     *
     * <p>Called inside {@link #calculateVelocity()}</p>
     *
     * @see #tick()
     */
    public void calculateGravity() {
        this.velY -= GRAVITY;
    }

    /**
     * Update the location of the vehicle to the specified location. Can be
     * overridden in subclasses if the vehicle has multiple parts. Should
     * be followed by {@link #updateRenderedPassengerPositions()}. Called every tick
     * if the vehicle's speed is not 0.
     */
    public void updateRenderedLocation() {
        NIArmorStand.setLocation(this.niEntity, this.entity, this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), this.location.getPitch());
        NIE.setLocation(this.niSlime, this.slime, this.location.getX(), this.location.getY(), this.location.getZ(), 0, 0);
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

            Location location = seat.getRelativePos().relativeTo(this.location, this.getRoll());
            if (seat.hasOffsetYaw()) location.setYaw(location.getYaw() + seat.getOffsetYaw());
            seatData.getRiderEntity().setLocation(location.getX(), location.getY() - SeatData.RIDER_ENTITY_Y_OFFSET, location.getZ(), location.getYaw(), location.getPitch());
        }
    }

    /**
     * Override in subclasses to display particles when the vehicle is moving. Will only
     * be called if {@link Vehicle#speed} != 0.
     */
    public void spawnParticles() {}

    /**
     * Check if the vehicle is currently made of non interpolating entites.
     *
     * <p>Subclasses should not need to override this method as if the main
     * entity is non interpolating, all entities should be non interpolating,
     * and the other way around.</p>
     *
     * @return Whether the vehicle is non interpolating
     */
    public boolean isNonInterpolating() {
        return this.niEntity != null;
    }

    /**
     * Make the entity non interpolating or not.
     *
     * <p>This method should be overridden in subclasses that use multiple
     * entities to render the model to make sure all entities are marked
     * as non interpolating or not.</p>
     *
     * @param nonInterpolating Whether to be non interpolating or not, may
     *                         not be the current state of the vehicle.
     * @throws IllegalStateException If the vehicle already is non interpolating
     * and {@code nonInterpolating} was true, or the other way around.
     */
    public void setNonInterpolating(boolean nonInterpolating) {
        if (this.isNonInterpolating() == nonInterpolating) throw new IllegalStateException("The vehicle is already non interpolating");
        if (nonInterpolating) {
            DebugUtil.debug("Creating niEntity");
            this.niEntity = new NIArmorStand(this.entity);
            this.niSlime = new NIE<>(this.slime);
        } else {
            DebugUtil.debug("Removing niEntity");
            this.niEntity.toArmorStand();
            this.niEntity = null;
            this.niSlime.toNormalEntity();
            this.niSlime = null;
        }
    }
    // </editor-fold>

    /**
     * @return Whether the vehicle is on the ground or not.
     */
    public boolean isOnGround() {
        return this.onGround;
    }

    /**
     * Check if the vehicle collides with blocks at the specified
     * location.
     *
     * <p>Also sets the {@link #highestCollisionBlock} field to the highest
     * block that the vehicle collided with.</p>
     *
     * @param location The location of the vehicle to check collision for.
     *                 This does not have to be the actual location of the vehicle
     * @return Whether the vehicle is colliding with blocks.
     */
    public boolean collides(Location location) {
        VehicleCollisionType collisionType = this.getType().getCollisionType();
        if (collisionType instanceof AABBCollision) {
            double x = this.location.getX();
            double y = this.location.getY();
            double z = this.location.getZ();
            BoundingBox boundingBox = ((AABBCollision) collisionType).getBoundingBox();
            for (double offsetX = boundingBox.getMinX(); offsetX <= boundingBox.getMaxX(); offsetX++) {
                for (double offsetY = boundingBox.getMinY(); offsetY <= boundingBox.getMaxY(); offsetY++) {
                    for (double offsetZ = boundingBox.getMinZ(); offsetZ <= boundingBox.getMaxZ(); offsetZ++) {
                        if (this.doCollisionBlockCheck(x + offsetX, y + offsetY, z + offsetZ + 0.001D, null)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } else {
            throw new IllegalStateException("Unknown collision type: "+ collisionType.getClass().getName());
        }
    }

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

            double distance = seat.getRelativePos().relativeTo(vehicleLocation, this.getRoll()).distanceSquared(location);
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
            if (this.getType().getDriverSeat() == seat) this.movement.reset();
        }

        if (passenger != null) {
            this.seatData.put(seat, new SeatData(this, seat, passenger));
            SVCraftVehicles.getInstance().getCurrentVehicleMap().put(passenger, this);
        }
    }

    /**
     * Attempt to add the entity as a passenger in the vehicle
     *
     * @param passenger The passenger that wants to enter the vehicle
     * @return {@code true} if the passenger entered the vehicle, {@code false} if not
     */
    public boolean addPassenger(@NotNull LivingEntity passenger) {
        Seat seat = this.getNearestAvailableSeat(passenger.getLocation());
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

            attachedVehicle.setLocation(attachmentData.getRelativePos().relativeTo(this.location, this.getRoll()));
            attachedVehicle.updateRenderedLocation();
            attachedVehicle.updateRenderedPassengerPositions();
        }
    }
    // </editor-fold>

    // Actions
    //<editor-fold desc="Actions related methods" defaultstate="collapsed">


    public List<VehicleMenuAction> getMenuActions() {
        return this.menuActions;
    }

    public List<VehicleClickAction> getClickActions() {
        return this.clickActions;
    }

    public List<VehicleAction> getAllActions() {
        return this.allActions;
    }

    public void addAction(VehicleAction action) {
        if (action instanceof VehicleMenuAction) this.menuActions.add((VehicleMenuAction) action);
        if (action instanceof VehicleClickAction) this.clickActions.add((VehicleClickAction) action);
        this.allActions.add(action);
    }

    public VehicleMenuAction getMenuAction(int index) {
        if (index >= 0 && index < this.menuActions.size()) {
            return this.menuActions.get(index);
        }
        return null;
    }

    public VehicleClickAction getClickAction(int index) {
        if (index >= 0 && index < this.clickActions.size()) {
            return this.clickActions.get(index);
        }
        return null;
    }

    /**
     * Update the specified inventory and add all menu items.
     *
     * @param inventory The inventory to add the items to
     * @param player The player that opens the menu.
     */
    public void updateMenuInventory(AbstractHorseInventory inventory, Player player) {
        // The saddle slot has to be empty, otherwise the seats will stand up when
        // pressing space and players will get a jump progress bar.

        for (int i = 0; i < this.menuActions.size(); i++) {
            VehicleMenuAction menuAction = this.menuActions.get(i);
            inventory.setItem(i + 2, menuAction.getEntryItem(this, player));
        }
    }
    // </editor-fold>
}
