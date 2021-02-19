package me.alvin.vehicles.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.actions.TestArrowAction;
import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.ColorUtil;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.svcraft.minigames.command.SubCommandedCommand;
import me.svcraft.minigames.command.brigadier.Cmd;
import me.svcraft.minigames.command.subcommand.SubCommand;
import me.svcraft.minigames.nms.CommandSource;
import me.svcraft.minigames.plugin.SVCraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class VehiclesCommand extends SubCommandedCommand {
    private final static DynamicCommandExceptionType UNKNOWN_VEHICLE_TYPE = new DynamicCommandExceptionType(type -> new LiteralMessage("Unknown vehicle type '"+ type +"'"));

    public static void register(CommandDispatcher<Object> dispatcher) {
        dispatcher.register(
            Cmd.literal("vehicles")
                .requires(obj -> Cmd.getSource(obj).hasPermission("svcraftvehicles.command.vehicles"))
                .then(
                    Cmd.literal("spawn")
                        .then(
                            Cmd.argument("type", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                for (String vehicleType : SVCraftVehicles.getInstance().getRegistry().getVehicleTypes()) {
                                    if (StringUtil.startsWithIgnoreCase(vehicleType, builder.getRemaining())) {
                                        builder.suggest(vehicleType);
                                    }
                                }
                                return builder.buildFuture();
                            })
                            .executes(context -> {
                                String id = StringArgumentType.getString(context, "type");
                                VehicleType vehicleType = SVCraftVehicles.getInstance().getRegistry().getVehicle(id);
                                if (vehicleType == null) {
                                    throw UNKNOWN_VEHICLE_TYPE.create(id);
                                }

                                CommandSource source = Cmd.getSource(context);
                                Player player = source.getPlayerRequired();

                                Vehicle vehicle = vehicleType.construct(player.getLocation(), player, VehicleSpawnReason.COMMAND);
                                Bukkit.getScheduler().runTaskLater(SVCraftVehicles.getInstance(), () -> {
                                    if (player.getGameMode() == GameMode.CREATIVE && vehicle.usesFuel()) {
                                        vehicle.setCurrentFuel(vehicle.getMaxFuel());
                                    }
                                }, 1L);
                                SVCraftVehicles.getInstance().getLoadedVehicles().put(vehicle.getEntity(), vehicle);
                                return 1;
                            })
                        )
                )
                .then(
                    Cmd.literal("report")
                        .executes(context -> {
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

                            Cmd.getSource(context).getCommandSender().sendMessage(message.toString());
                            return 1;
                        })
            )
            .then(
                Cmd.literal("reload")
                    .executes(context -> {
                        CommandSender sender = Cmd.getSource(context).getCommandSender();
                        try {
                            SVCraftVehicles.getInstance().reload();
                            sender.sendMessage(ChatColor.GREEN + "The configuration was reloaded.");
                        } catch (IOException | InvalidConfigurationException e) {
                            e.printStackTrace();
                            sender.sendMessage(ChatColor.RED + "There was an error when reloading");
                        }
                        return 1;
                    })
            )
            .then(
                Cmd.literal("relativepos")
                    .then(
                        Cmd.argument("left", DoubleArgumentType.doubleArg(-20, 20))
                            .then(
                                Cmd.argument("up", DoubleArgumentType.doubleArg(-20, 20))
                                    .then(
                                        Cmd.argument("forward", DoubleArgumentType.doubleArg(-20, 20))
                                        .executes(context -> {
                                            double left = DoubleArgumentType.getDouble(context, "left");
                                            double up = DoubleArgumentType.getDouble(context, "up");
                                            double forward = DoubleArgumentType.getDouble(context, "forward");

                                            RelativePos relativePos = new RelativePos(left, up, forward);

                                            Player player = Cmd.getSource(context).getPlayerRequired();

                                            player.sendMessage(relativePos.toString());

                                            Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(player);

                                            vehicle.debugRelativePos = relativePos;
                                            return 1;
                                        })
                                    )
                            )
                    )
            )
            .then(
                Cmd.literal("color")
                    .executes(context -> {
                        Player player = Cmd.getSource(context).getPlayerRequired();
                        Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(player);
                        if (vehicle != null) {
                            if (vehicle.canBeColored()) {
                                ItemStack handItem = player.getInventory().getItemInMainHand();
                                DyeColor color = ColorUtil.getDyeColorForMaterial(handItem.getType());
                                if (color != null) {
                                    boolean success = vehicle.setColor(color.getColor());
                                    player.sendMessage("success: "+ success);
                                } else {
                                    player.sendMessage(ChatColor.RED + "Please hold a dye in your hand");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "The vehicle you are in can not be painted");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Please sit in the vehicle you want to paint");
                        }
                        return 1;
                    })
            )
            .then(
                Cmd.literal("leave")
                    .executes(context -> {
                        Entity entity = Cmd.getSource(context).getEntityRequired();
                        entity.leaveVehicle();
                        return 1;
                    })
            )
            .then(
                Cmd.literal("refuel")
                    .executes(context -> {
                        Player player = Cmd.getSource(context).getPlayerRequired();
                        Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(player);
                        if (vehicle != null) {
                            if (vehicle.usesFuel()) {
                                vehicle.setCurrentFuel(vehicle.getMaxFuel());
                                player.sendMessage(ChatColor.GREEN + "The vehicle has been refueled.");
                            } else {
                                player.sendMessage(ChatColor.RED + "The vehicle you are in does not use fuel");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Please sit in the vehicle you want to refuel");
                        }
                        return 1;
                    })
            )
            .then(
                Cmd.literal("update")
                    .executes(context -> {
                        CommandSource source = Cmd.getSource(context);
                        source.getCommandSender().sendMessage("This command is meant to be used to update seats, please connect via intellij debug");
                        Player player = source.getPlayerRequired();
                        Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(player);

                        Set<Seat> seats = vehicle.getType().getSeats();
                        seats.clear();
                        Seat driverSeat = new Seat(new RelativePos(0.35, 0.3, 0.1));
                        seats.add(driverSeat);
                        seats.add(new Seat(new RelativePos(-0.35, 0.3, 0.1)));
                        seats.add(new Seat(new RelativePos(0.35, 0.3, -1.0)));
                        seats.add(new Seat(new RelativePos(-0.35, 0.3, -1.0)));

                        try {
                            Field field = VehicleType.class.getDeclaredField("driverSeat");
                            field.setAccessible(true);

                            Field modifiersField = Field.class.getDeclaredField("modifiers");
                            modifiersField.setAccessible(true);
                            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

                            field.set(vehicle.getType(), driverSeat);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        source.getCommandSender().sendMessage("Seats updated (for car!!!)");
                        return 1;
                    })
            )
        );
    }
    @Deprecated
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
