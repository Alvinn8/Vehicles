package me.alvin.vehicles.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.assets.ModelSplitter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import svcraft.core.command.brigadier.Cmd;
import svcraft.core.nms.CommandSource;

import java.util.List;

public class ModelSplitTestCommand {
    public static List<ModelSplitter.Part> parts;

    public static void register(CommandDispatcher<Object> dispatcher) {
        dispatcher.register(
            Cmd.literal("modelsplit")
                .then(
                    Cmd.literal("spawn")
                        .then(
                            Cmd.literal("ref")
                                .executes(ModelSplitTestCommand::spawnRef)
                        )
                        .then(
                            Cmd.literal("split")
                                .executes(ModelSplitTestCommand::spawnSplit)
                                .then(
                                    Cmd.literal("respawn")
                                        .executes(ModelSplitTestCommand::respawnSplit)
                                )
                        )
                )
        );
    }

    private static int spawnRef(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSource source = Cmd.getSource(context);

        ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/plane_test");
        Location location = source.getPlayerRequired().getLocation();
        source.getWorld().spawn(location, ArmorStand.class, armorStand -> {
            armorStand.setGravity(false);
            armorStand.getEquipment().setHelmet(item);
        });
        source.getCommandSender().sendRichMessage("<green>Spawned a reference.");

        return 1;
    }

    private static int spawnSplit(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSource source = Cmd.getSource(context);

        Location location = source.getPlayerRequired().getLocation();

        for (ModelSplitter.Part part : parts) {
            ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem(part.key().toString());
            Location partLocation = part.offset().relativeTo(location, 0);
            source.getWorld().spawn(partLocation, ArmorStand.class, armorStand -> {
                armorStand.setGravity(false);
                armorStand.getEquipment().setHelmet(item);
            });
        }
        source.getCommandSender().sendRichMessage("<green>Spawned the parts.");

        return 1;
    }

    private static int respawnSplit(CommandContext<Object> context) throws CommandSyntaxException {
        CommandSource source = Cmd.getSource(context);
        int count = 0;
        for (ArmorStand armorStand : source.getPlayerRequired().getLocation().getNearbyEntitiesByType(ArmorStand.class, 5)) {
            armorStand.remove();
        }
        source.getCommandSender().sendRichMessage("<green>Removed " + count + " armor stands.");
        spawnSplit(context);
        return 1;
    }

}
