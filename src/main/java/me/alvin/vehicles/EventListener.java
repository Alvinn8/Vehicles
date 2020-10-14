package me.alvin.vehicles;

import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.seat.SeatData;
import me.svcraft.minigames.world.event.PerWorldListener;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Map;

public class EventListener implements PerWorldListener {
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
                    Vehicle vehicle;
                    try {
                        vehicle = vehicleType.construct((ArmorStand) entity);
                    } catch (Throwable e) {
                        SVCraftVehicles.getInstance().getLogger().severe("Failed to load vehicle with id '" + id + "' when loading entity " + entity.getUniqueId().toString() + " in chunk " + chunk.getX() + " " + chunk.getZ() + " in world " + event.getWorld().getName());
                        e.printStackTrace();
                        continue;
                    }

                    SVCraftVehicles.getInstance().getLoadedVehicles().put((ArmorStand) entity, vehicle);
                }
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof ArmorStand)) continue;
            if (!SVCraftVehicles.getInstance().getLoadedVehicles().containsKey(entity)) continue;

            Vehicle vehicle = SVCraftVehicles.getInstance().getLoadedVehicles().get(entity);
            try {
                vehicle.unload();
            } catch (Throwable e) {
                SVCraftVehicles.getInstance().getLogger().severe("Failed to unload vehicle with id '" + vehicle.getType().getId() + "' for entity "+ entity.getUniqueId().toString() + " in chunk "+ event.getChunk().getX() + " "+ event.getChunk().getZ() + " in world "+ event.getWorld().getName());
                e.printStackTrace();
            }

            SVCraftVehicles.getInstance().getLoadedVehicles().remove(entity);
            // TODO: Load vehicles from already loaded chunks in onEnable (reloads)
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        DebugUtil.debug("PlayerInteractAtEntityEvent called");
        Entity entity = event.getRightClicked();
        if (!(entity instanceof ArmorStand)) return;
        if (!SVCraftVehicles.getInstance().getLoadedVehicles().containsKey(entity)) return;

        DebugUtil.debug("PlayerInteractAtEntityEvent if checks passed");

        Vehicle vehicle = SVCraftVehicles.getInstance().getLoadedVehicles().get(entity);
        DebugUtil.debug("success?: " + vehicle.addPassenger(event.getPlayer()));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand entity = (ArmorStand) event.getEntity();
            if (SVCraftVehicles.getInstance().getLoadedVehicles().containsKey(entity)) {
                Vehicle vehicle = SVCraftVehicles.getInstance().getLoadedVehicles().get(entity);
                vehicle.remove();
            }
        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        Entity entity = event.getEntity();
        DebugUtil.debug(entity.getName() + " dismounted something");
        if (entity instanceof LivingEntity) {
            LivingEntity passenger = (LivingEntity) entity;
            // The vehicle has to be fetched directly from the map as mc has
            // already started dismounting the entity by this point
            Vehicle vehicle = SVCraftVehicles.getInstance().getCurrentVehicleMap().get(passenger);
            if (vehicle != null) {
                DebugUtil.debug("Entity left vehicle");
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
            } else {
                DebugUtil.debug("Entity left something that isn't a vehicle");
            }
        }
    }
}
