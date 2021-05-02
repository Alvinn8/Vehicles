package me.alvin.vehicles;

import me.alvin.vehicles.crafting.VehicleCraftingTable;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.action.VehicleClickAction;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicle.seat.SeatData;
import me.svcraft.minigames.SVCraft;
import me.svcraft.minigames.item.CustomItem;
import me.svcraft.minigames.tileentity.CustomTileEntity;
import me.svcraft.minigames.world.event.PerWorldListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Map;

public class EventListener implements PerWorldListener {
    /**
     * 5 for player reach + 3 for vehicle radius.
     */
    public static final int VEHICLE_ENTER_SEARCH_DISTANCE = 5 + 3;

    @Override
    public boolean isEnabledIn(World world) {
        return SVCraftVehicles.getInstance().isEnabledIn(world);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof ArmorStand) {
                PersistentDataContainer data = entity.getPersistentDataContainer();
                if (data.has(Vehicle.VEHICLE_ID, PersistentDataType.STRING)) {
                    String id = data.get(Vehicle.VEHICLE_ID, PersistentDataType.STRING);
                    VehicleType vehicleType = SVCraftVehicles.getInstance().getRegistry().getVehicle(id);
                    if (vehicleType == null) {
                        SVCraftVehicles.getInstance().getLogger().warning("Unknown vehicle id '" + id + "' when loading entity " + entity.getUniqueId().toString() + " in chunk " + chunk.getX() + " " + chunk.getZ() + " in world " + event.getWorld().getName());
                        continue;
                    }
                    try {
                        vehicleType.construct((ArmorStand) entity);
                    } catch (Throwable e) {
                        SVCraftVehicles.getInstance().getLogger().severe("Failed to load vehicle with id '" + id + "' when loading entity " + entity.getUniqueId().toString() + " in chunk " + chunk.getX() + " " + chunk.getZ() + " in world " + event.getWorld().getName());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof ArmorStand)) continue;
            if (!SVCraftVehicles.getInstance().getVehiclePartMap().containsKey(entity)) continue;

            Vehicle vehicle = SVCraftVehicles.getInstance().getVehiclePartMap().get(entity);
            try {
                vehicle.unload();
            } catch (Throwable e) {
                SVCraftVehicles.getInstance().getLogger().severe("Failed to unload vehicle with id '" + vehicle.getType().getId() + "' for entity "+ entity.getUniqueId().toString() + " in chunk "+ event.getChunk().getX() + " "+ event.getChunk().getZ() + " in world "+ event.getWorld().getName());
                e.printStackTrace();
            }
            // TODO: Load vehicles from already loaded chunks in onEnable (reloads)
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand entity = (ArmorStand) event.getEntity();
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
        if (entity instanceof LivingEntity) {
            LivingEntity passenger = (LivingEntity) entity;
            // The vehicle has to be fetched directly from the map as mc has
            // already started dismounting the entity by this point
            Vehicle vehicle = SVCraftVehicles.getInstance().getCurrentVehicleMap().get(passenger);
            if (vehicle != null) {
                // We also have to get the seat directly from the map
                // as the methods will think the seat data is invalid
                for (Map.Entry<Seat, SeatData> entry : vehicle.getSeatData().entrySet()) {
                    SeatData seatData = entry.getValue();
                    // skip isValid check
                    if (seatData.getPassenger() == passenger) {
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
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Vehicle vehicle = SVCraftVehicles.getInstance().getVehiclePartMap().get(event.getEntity());
        if (vehicle != null) {
            event.setCancelled(true);
            if (event.getDamager() instanceof Player && ((Player) event.getDamager()).getGameMode() == GameMode.CREATIVE) {
                vehicle.remove();
            } else {
                // TODO: Damage vehicle by event.getDamage()
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
