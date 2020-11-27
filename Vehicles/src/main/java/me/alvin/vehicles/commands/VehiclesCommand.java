package me.alvin.vehicles.commands;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.ColorUtil;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.svcraft.minigames.command.SubCommandedCommand;
import me.svcraft.minigames.command.subcommand.SubCommand;
import me.svcraft.minigames.plugin.SVCraftPlugin;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class VehiclesCommand extends SubCommandedCommand {
    public VehiclesCommand(SVCraftPlugin plugin) {
        super(plugin);

        this.addSubCommand(new SubCommand("spawn", (player, args, isPlayer) -> {
            if (args.length < 1) {
                player.sendMessage("Please specify the vehicle type to spawn");
                return;
            }
            String id = args[0];
            VehicleType vehicleType = SVCraftVehicles.getInstance().getRegistry().getVehicle(id);
            if (vehicleType == null) {
                player.sendMessage(id + "is not a vehicle type");
                return;
            }

            Vehicle vehicle = vehicleType.construct(player.getLocation(), player, VehicleSpawnReason.COMMAND);
            if (player.getGameMode() == GameMode.CREATIVE && vehicle.usesFuel())
                vehicle.setCurrentFuel(vehicle.getMaxFuel());

            SVCraftVehicles.getInstance().getLoadedVehicles().put(vehicle.getEntity(), vehicle);
        },
        (sender, arg) -> StringUtil.copyPartialMatches(arg, SVCraftVehicles.getInstance().getRegistry().getVehicleTypes(), new ArrayList<>())
        ));

        this.addSubCommand(new SubCommand("report", (commandSender, strings) -> {
            StringBuilder message = new StringBuilder();

            message.append("Registered vehicle types:\n");
            VehicleRegistry registry = SVCraftVehicles.getInstance().getRegistry();
            Map<String, VehicleType> registeredVehicles = registry.getRegisteredVehicles();
            message.append(registeredVehicles.size());
            message.append('\n');
            for (String id : registeredVehicles.keySet()) {
                message.append(id).append('\n');
            }
            message.append('\n');

            Map<ArmorStand, Vehicle> loadedVehicles = SVCraftVehicles.getInstance().getLoadedVehicles();
            message.append(loadedVehicles.size()).append(" loaded vehicles");
            message.append('\n');
            for (Map.Entry<ArmorStand, Vehicle> entry : loadedVehicles.entrySet()) {
                message.append(entry.getKey().getUniqueId().toString());
                message.append(": ");
                message.append(entry.getValue().getType().getId());
                message.append(' ');
                message.append(entry.getValue().getNIEntity() == null ? "regular armor stand" : "ni armor stand");
                message.append(' ');
                if (!entry.getValue().getEntity().isValid()) {
                    message.append(" §c[INVALID ENTITY]§r");
                }
                message.append('\n');
            }

            commandSender.sendMessage(message.toString());
        }));

        this.addSubCommand(new SubCommand("reload", (commandSender, strings) -> {
            try {
                SVCraftVehicles.getInstance().reload();
                commandSender.sendMessage("reloaded");
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
                commandSender.sendMessage("There was an error when reloading");
            }
        }));

        // TEMP
        this.addSubCommand(new SubCommand("relativepos", (sender, args, isPlayer) -> {
            double left = Double.parseDouble(args[0]);
            double up = Double.parseDouble(args[1]);
            double forward = Double.parseDouble(args[2]);

            RelativePos relativePos = new RelativePos(left, up, forward);

            sender.sendMessage(relativePos.toString());

            Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(sender);

            vehicle.debugRelativePos = relativePos;
        }));

        this.addSubCommand(new SubCommand("setColor", (player, args, isPlayer) -> {
            Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(player);
            if (vehicle != null) {
                if (vehicle.canBeColored()) {
                    ItemStack handItem = player.getInventory().getItemInMainHand();
                    DyeColor color = ColorUtil.getDyeColorForMaterial(handItem.getType());
                    if (color != null) {
                        boolean success = vehicle.setColor(color.getColor());
                        player.sendMessage("success: "+ success);
                    } else {
                        player.sendMessage("§cPlease hold a dye in your hand");
                    }
                } else {
                    player.sendMessage("§cThe vehicle you are in can not be painted");
                }
            } else {
                player.sendMessage("§cPlease sit in the vehicle you want to paint");
            }
        }));

        this.addSubCommand(new SubCommand("leave", (player, arsg, isPlayer) -> player.leaveVehicle()));
    }
}
