package me.alvin.vehicles.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.VehicleSpawnerTask;
import me.alvin.vehicles.explosion.CoolExplosion;
import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.ColorUtil;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import me.alvin.vehicles.vehicle.collision.AABBCollision;
import me.alvin.vehicles.vehicle.seat.Seat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.StringUtil;
import svcraft.core.SVCraft;
import svcraft.core.command.brigadier.Cmd;
import svcraft.core.nms.CommandSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

public class VehiclesCommand {
    private final static DynamicCommandExceptionType UNKNOWN_VEHICLE_TYPE = new DynamicCommandExceptionType(type -> new LiteralMessage("Unknown vehicle type '"+ type +"'"));
    private final static SimpleCommandExceptionType NO_BLOCK_IN_SIGHT = new SimpleCommandExceptionType(new LiteralMessage("Please look at the location you want to blow up."));

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
                                if (player.getGameMode() == GameMode.CREATIVE && vehicle.usesFuel()) {
                                    vehicle.setCurrentFuel(vehicle.getMaxFuel());
                                }
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
                                Vehicle vehicle = entry.getValue();
                                message.append(entry.getKey().getUniqueId());
                                message.append(": ");
                                message.append(vehicle.getType().getId());
                                message.append(' ');
                                message.append(vehicle.getNIEntity() == null ? "regular armor stand" : "ni armor stand");
                                message.append(' ');
                                if (!vehicle.getEntity().isValid()) {
                                    message.append(" §c[INVALID ENTITY]§r");
                                }
                                Slime slime = vehicle.getSlime();
                                if (slime != null) {
                                    message.append(vehicle.getNiSlime() == null ? " no ni-slime" : " ni-slime");
                                    if (!slime.isValid()) message.append(" §cinvalid slime!§r");
                                } else {
                                    message.append(" §cno slime§r");
                                }
                                message.append('\n');
                            }
                            message.append('\n');
                            message.append("vehiclePartMap size: ");
                            message.append(SVCraftVehicles.getInstance().getVehiclePartMap().size());

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
                                vehicle.setCurrentFuel(vehicle.getMaxFuel() / 100 * 2);
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
                        source.getCommandSender().sendMessage("§e⚠ This command is meant to be used with the IntelliJ debugger to update seats.");
                        Player player = source.getPlayerRequired();
                        Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(player);

                        // Seat to modify
                        Seat seat = vehicle.getType().getDriverSeat();
                        // New relativepos for the seat
                        RelativePos newDriverSeatRelPos = new RelativePos(0, 1.2, -1.25);

                        try {
                            Field field = Seat.class.getDeclaredField("relativePos");
                            field.setAccessible(true);
                            field.set(seat, newDriverSeatRelPos);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        source.getCommandSender().sendMessage("Driver seat updated");
                        return 1;
                    })
            )
            .then(
                Cmd.literal("mathtest")
                .executes(context -> {
                    CommandSource source = Cmd.getSource(context);
                    CommandSender sender = source.getCommandSender();
                    sender.sendMessage("§e⚠ This command is meant to be used with the IntelliJ debugger.");
                    Entity entity = Bukkit.getEntity(UUID.fromString("d21bf130-0fcb-4dec-a25a-444395116e08"));
                    long gameTime = SVCraft.getInstance().getNMS().getGameTime(entity.getWorld());
                    float yaw = gameTime % 360.0F;
                    float pitch = gameTime % 180.0F - 90.0F;
                    float roll = gameTime % 180.0F - 90.0F;

                    sender.sendMessage("yaw = " + yaw);
                    sender.sendMessage("pitch = " + pitch);
                    sender.sendMessage("roll = " + roll);
                    Location location = entity.getLocation();
                    location.setYaw(yaw);
                    location.setPitch(pitch);
                    entity.teleport(location);
                    ((ArmorStand) entity).setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, Math.toRadians(roll)));
                    RelativePos relativePos = new RelativePos(1, 1, 1);
                    location.add(0, 1.5, 0);
                    Location newLocation = relativePos.relativeTo(location, roll);
                    // newLocation.add(0, 0.35, 0);
                    entity.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 1, 0, 0,0, 0);
                    entity.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, newLocation, 1, 0, 0,0, 0);
                    return 1;
                })
            )
            .then(
                Cmd.literal("showboundingbox")
                .executes(context -> {
                    CommandSource source = Cmd.getSource(context);
                    Player player = source.getPlayerRequired();
                    Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(player);
                    if (vehicle == null) {
                        player.sendMessage("§cYou are not in a vehicle.");
                        return 1;
                    }

                    ByteBuffer buf = ByteBuffer.allocate(60);
                    buf.put((byte) 1);
                    buf.putInt(vehicle.getEntity().getEntityId());
                    BoundingBox boundingBox = ((AABBCollision) vehicle.getType().getCollisionType()).getBoundingBox();
                    buf.putFloat((float) boundingBox.getMaxX()); // Width
                    buf.putFloat((float) boundingBox.getMaxY()); // Height
                    player.sendPluginMessage(SVCraftVehicles.getInstance(), "vehicles:vehicle-bb", buf.array());
                    return 1;
                })
            )
            .then(
                Cmd.literal("attachtest")
                .executes(context -> {
                    CommandSource source = Cmd.getSource(context);
                    Player player = source.getPlayerRequired();
                    Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(player);
                    Vehicle truck = null;
                    for (Vehicle value : SVCraftVehicles.getInstance().getLoadedVehicles().values()) {
                        if (value.getType() == VehicleTypes.TRUCK) {
                            truck = value;
                        }
                    }
                    // vehicle.attachVehicle(truck, new AttachmentData(new RelativePos(0, -10, 0)));
                    truck.detach();
                    return 1;
                })
            )
            .then(
                Cmd.literal("creativetest")
                .executes(context -> {
                    CommandSource source = Cmd.getSource(context);
                    Player player = source.getPlayerRequired();
                    VehicleSpawnerTask vehicleSpawnerTask = new VehicleSpawnerTask(player);
                    vehicleSpawnerTask.start();
                    return 1;
                })
            )
            .then(
                Cmd.literal("hijack")
                    .executes(context -> {
                        System.out.println("Hello, world!");
                        return 1;
                    })
            )
            .then(
                Cmd.literal("tmpvars")
                    .then(
                        Cmd.literal("double")
                            .then(
                                Cmd.argument("name", StringArgumentType.string())
                                    .executes(context -> {
                                        CommandSource source = Cmd.getSource(context);
                                        String name = StringArgumentType.getString(context, "name");
                                        try {
                                            Field field = Class.forName("me.alvin.vehicles.util.TmpVars").getField(name);
                                            Object value = field.get(null);
                                            source.getCommandSender().sendMessage(String.valueOf(value));
                                        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
                                            source.getCommandSender().sendMessage(e.getMessage());
                                            e.printStackTrace();
                                            return 0;
                                        }
                                        return 1;
                                    })
                                    .then(
                                        Cmd.argument("value", DoubleArgumentType.doubleArg())
                                            .executes(context -> {
                                                CommandSource source = Cmd.getSource(context);
                                                String name = StringArgumentType.getString(context, "name");
                                                double value = DoubleArgumentType.getDouble(context, "value");
                                                try {
                                                    Field field = Class.forName("me.alvin.vehicles.util.TmpVars").getField(name);
                                                    field.set(null, value);
                                                } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
                                                    source.getCommandSender().sendMessage(e.getMessage());
                                                    e.printStackTrace();
                                                    return 0;
                                                }
                                                return 1;
                                            })
                                    )
                            )
                    )
            )
            .then(
                Cmd.literal("explode")
                    .requires(obj -> Cmd.getSource(obj).hasPermission("vehicles.command.explode"))
                    .executes(context -> {
                        CommandSource source = Cmd.getSource(context);
                        Player player = source.getPlayerRequired();
                        Block targetBlock = player.getTargetBlock(50);
                        if (targetBlock == null) throw NO_BLOCK_IN_SIGHT.create();
                        CoolExplosion.explode(targetBlock.getLocation().add(0, 2, 0), 3, player);
                        return 1;
                    })
            )
        );
    }
}
