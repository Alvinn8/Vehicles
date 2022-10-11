package me.alvin.vehicles;

import me.alvin.vehicles.crafting.VehicleCraftingTable;
import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicle.seat.PassengerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityDismountEvent;
import svcraft.core.SVCraft;
import svcraft.core.item.CustomItem;
import svcraft.core.tileentity.CustomTileEntity;
import svcraft.core.world.event.PerWorldListener;

import java.util.Map;

public class EventListener implements PerWorldListener {
    @Override
    public boolean isListenerEnabledIn(World world) {
        return SVCraftVehicles.getInstance().isEnabledIn(world);
    }

    @EventHandler
    public void onChunkLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof ArmorStand) {
                PersistentDataContainer data = entity.getPersistentDataContainer();
                if (data.has(Vehicle.VEHICLE_ID, PersistentDataType.STRING)) {
                    String id = data.get(Vehicle.VEHICLE_ID, PersistentDataType.STRING);
                    VehicleType vehicleType = SVCraftVehicles.getInstance().getRegistry().getVehicle(id);
                    if (vehicleType == null) {
                        SVCraftVehicles.getInstance().getLogger().warning("Unknown vehicle id '" + id + "' when loading entity " + entity.getUniqueId() + " in chunk " + event.getChunk().getX() + " " + event.getChunk().getZ() + " in world " + event.getWorld().getName());
                        continue;
                    }
                    try {
                        vehicleType.construct((ArmorStand) entity);
                    } catch (Throwable e) {
                        SVCraftVehicles.getInstance().getLogger().severe("Failed to load vehicle with id '" + id + "' when loading entity " + entity.getUniqueId() + " in chunk " + event.getChunk().getX() + " " + event.getChunk().getZ() + " in world " + event.getWorld().getName());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!event.getChunk().isLoaded()) {
            SVCraftVehicles.getInstance().getLogger().warning("ChunkUnloadEvent was called with an already unloaded chunk! x: " + event.getChunk().getX() + " z: " + event.getChunk().getZ());
            new Exception().printStackTrace();
            return;
        }
        for (Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof ArmorStand)) continue;
            if (!SVCraftVehicles.getInstance().getVehiclePartMap().containsKey(entity)) continue;

            Vehicle vehicle = SVCraftVehicles.getInstance().getVehiclePartMap().get(entity);
            DebugUtil.debug("Unloading a vehicle with id " + vehicle.getType().getId()
                + " because the chunk [" + event.getChunk().getX() + ", " + event.getChunk().getZ()
                + "] was unloaded. The entity that was a vehicle has uuid " + entity.getUniqueId()
                + " and is a " + entity.getType().getKey() + ", isInsideVehicle = " + entity.isInsideVehicle()
                + " passengers size = " + entity.getPassengers().size() + " name = " + entity.getName());
            try {
                vehicle.unload();
            } catch (Throwable e) {
                SVCraftVehicles.getInstance().getLogger().severe("Failed to unload vehicle with id '" + vehicle.getType().getId() + "' for entity "+ entity.getUniqueId() + " in chunk "+ event.getChunk().getX() + " "+ event.getChunk().getZ() + " in world "+ event.getWorld().getName());
                e.printStackTrace();
            }
            // TODO: Load vehicles from already loaded chunks in onEnable (reloads)
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof ArmorStand entity) {
            if (SVCraftVehicles.getInstance().getLoadedVehicles().containsKey(entity)) {
                // If the main entity of a vehicle is killed, we destroy the vehicle
                Vehicle vehicle = SVCraftVehicles.getInstance().getLoadedVehicles().get(entity);
                vehicle.remove();
            }
        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity passenger) {
            // The vehicle has to be fetched directly from the map as mc has
            // already started dismounting the entity by this point
            Vehicle vehicle = SVCraftVehicles.getInstance().getCurrentVehicleMap().get(passenger);
            if (vehicle != null) {
                // We also have to get the seat directly from the map
                // as the methods will think the seat data is invalid
                for (Map.Entry<Seat, PassengerData> entry : vehicle.getPassengerData().entrySet()) {
                    PassengerData passengerData = entry.getValue();
                    // skip isValid check
                    if (passengerData.getPassenger() == passenger) {
                        Seat seat = entry.getKey();
                        vehicle.setPassenger(seat, null);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() instanceof AbstractHorseInventory && event.getWhoClicked() instanceof Player) {
            Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(event.getWhoClicked());
            if (vehicle != null) {
                event.setCancelled(true);
                int index = event.getSlot() - 2;
                VehicleMenuAction action = vehicle.getMenuAction(index);
                if (action != null) {
                    action.onMenuClick(vehicle, (Player) event.getWhoClicked());
                }
            }
        }
    }

    public void onInteract(Cancellable event, Player player, boolean isRightClick) {
        Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(player);
        if (vehicle != null) {
            vehicle.onInteract(event, player);
        }

        if (isRightClick) {
            VehicleSpawnerTask vehicleSpawnerTask = SVCraftVehicles.getInstance().getVehicleSpawnerTaskMap().get(player);
            if (vehicleSpawnerTask != null) {
                vehicleSpawnerTask.spawn();
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        this.onInteract(event, player, true);

        Vehicle vehicle = SVCraftVehicles.getInstance().getVehiclePartMap().get(event.getRightClicked());
        if (vehicle != null && !vehicle.isPassenger(player)) {
            vehicle.addPassenger(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        this.onInteract(event, event.getPlayer(), event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null) {
                CustomTileEntity tileEntity = SVCraft.getInstance().getTileEntityManager().getTileEntity(clickedBlock);
                if (tileEntity instanceof VehicleCraftingTable) {
                    ((VehicleCraftingTable) tileEntity).openInventory(event.getPlayer());
                    event.setCancelled(true);
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            ItemStack selectedItem = event.getPlayer().getInventory().getItemInMainHand();
            CustomItem item = CustomItem.getItem(selectedItem);
            if (item == CustomItems.VEHICLE_SPAWNER) {
                if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                    CustomItems.VEHICLE_SPAWNER.openVehicleTypeSelector(event.getPlayer(), selectedItem);
                } else {
                    event.getPlayer().sendActionBar(Component.text("You need to be in creative mode to use the vehicle spawner", NamedTextColor.RED));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Vehicle vehicle = SVCraftVehicles.getInstance().getVehiclePartMap().get(event.getEntity());
        if (vehicle != null) {
            event.setCancelled(true);
            Entity damager = event instanceof EntityDamageByEntityEvent event2 ? event2.getDamager() : null;
            Vehicle damagerVehicle = damager instanceof LivingEntity ? SVCraftVehicles.getInstance().getVehicle((LivingEntity) damager) : null;
            // Prevent damaging the vehicle you are inside
            if (damagerVehicle == vehicle) {
                return;
            }
            // Prevent suffocation damage to prevent non perfect collision causing damage
            if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                return;
            }
            // Prevent iron golems from destroying vehicles
            if (damager instanceof IronGolem) {
                ((IronGolem) damager).setTarget(null);
                vehicle.raise();
                return;
            }
            if (damager instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && ((Player) damager).getGameMode() == GameMode.CREATIVE) {
                // Creative mode players destroy vehicles instantly
                vehicle.remove();
            } else {
                // Damage the vehicle normally
                vehicle.damage(event.getDamage(), damager);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItem(event.getNewSlot());
        CustomItem item = CustomItem.getItem(itemStack);

        if (item == CustomItems.VEHICLE_SPAWNER) {
            // The player switched to the vehicle spawner
            if (player.getGameMode() == GameMode.CREATIVE) {
                CustomItems.VEHICLE_SPAWNER.onSelect(player, itemStack);
            } else {
                player.sendActionBar(Component.text("You need to be in creative mode to use the vehicle spawner", NamedTextColor.RED));
            }
        }
    }
}
