package me.alvin.vehicles;

import com.comphenix.protocol.ProtocolLibrary;
import me.alvin.vehicles.commands.VehiclesCommand;
import me.alvin.vehicles.nms.NMS;
import me.alvin.vehicles.nms.NMS_v1_17_R1;
import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Material;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import svcraft.core.SVCraft;
import svcraft.core.config.Config;
import svcraft.core.plugin.SVCraftPlugin;
import svcraft.core.resourcepack.ResourcePack;
import svcraft.core.resourcepack.modelmanagerdata.RPCModelManagerData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class SVCraftVehicles extends SVCraftPlugin {

    public static boolean EXPLOSIONS_BREAK_BLOCKS = true;

    private static SVCraftVehicles instance;

    private NMS nms;

    private boolean debugMode = false;
    private VehicleRegistry registry;
    private final Map<ArmorStand, Vehicle> loadedVehicles = new HashMap<>();
    private final Map<LivingEntity, Vehicle> currentVehicleMap = new HashMap<>();
    private final Map<Entity, Vehicle> vehiclePartMap = new HashMap<>();
    private final Map<Player, VehicleSpawnerTask> vehicleSpawnerTaskMap = new HashMap<>();

    private RPCModelManagerData resourcepackData;

    @Override
    public void onPluginEnable() {
        instance = this;

        if (!this.setupNMS()) return;

        // Bukkit.getPluginManager().registerEvents(new TestEventListener(), this);

        ResourcePack vehiclesResourcePack = SVCraft.getInstance().getResourcePackManager().getResourcePack("vehicles");
        if (vehiclesResourcePack != null && vehiclesResourcePack.hasRPCModelManagerData()) {
            this.resourcepackData = vehiclesResourcePack.getRpcModelManagerData();
        } else {
            this.getLogger().severe("No \"vehicles\" resource pack detected, or it doesn't have rpCompiler ModelManager data! It is required for SVCraftVehicles to know what models to use for what vehicles!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            this.reload();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        // this.registerCommand("vehicles", new VehiclesCommand(this));
        this.registerCommand(VehiclesCommand::register);
        this.registerPerWorldEvents(new EventListener());

        this.registerCustomBlock(new NamespacedKey(SVCraft.getInstance(), "vehicle_crafting_table"), CustomBlocks.VEHICLE_CRAFTING_TABLE);

        this.registerItem(CustomItems.VEHICLE_CRAFTING_TABLE);
        this.registerItem(CustomItems.FUEL);
        this.registerItem(CustomItems.VEHICLE_SPAWNER);

        ShapedRecipe vehicleCraftingTableRecipe = new ShapedRecipe(new NamespacedKey(this, "vehicle_crafting_table"), CustomItems.VEHICLE_CRAFTING_TABLE.makeItemStack());
        vehicleCraftingTableRecipe.shape(" # ", "#-#", " # ");
        vehicleCraftingTableRecipe.setIngredient('#', Material.IRON_INGOT);
        vehicleCraftingTableRecipe.setIngredient('-', Material.CRAFTING_TABLE);
        this.registerRecipe(vehicleCraftingTableRecipe);

        ShapelessRecipe fuelRecipe = new ShapelessRecipe(new NamespacedKey(this, "fuel"), CustomItems.FUEL.makeItemStack());
        fuelRecipe.addIngredient(Material.IRON_INGOT);
        fuelRecipe.addIngredient(5, Material.COAL);
        this.registerRecipe(fuelRecipe);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));

        Bukkit.getMessenger().registerOutgoingPluginChannel(SVCraftVehicles.getInstance(), "vehicles:vehicle-bb");

        this.registry = new VehicleRegistry();

        VehicleTypes.register(this.registry);

        new VehicleTicker().start();
    }

    @Override
    public void onPluginDisable() {
        for (Map.Entry<ArmorStand, Vehicle> entry : this.loadedVehicles.entrySet()) {
            try {
                entry.getValue().save();
            } catch (Throwable e) {
                this.getLogger().severe("Failed to save vehicle for "+ entry.getKey().getUniqueId().toString());
                e.printStackTrace();
            }
        }
    }

    private boolean setupNMS() {
        String nmsVersion = SVCraft.getInstance().getNMS().getVersion();

        switch (nmsVersion) {
            case "v1_17_R1": this.nms = new NMS_v1_17_R1(); break;
        }

        if (this.nms != null) {
            this.getLogger().info("NMS set up successfully for minecraft version "+ this.nms.getVersion());
            return true;
        }  else {
            this.getLogger().severe("---");
            this.getLogger().severe("Vehicles does not support this server version!");
            this.getLogger().severe("Please change your server version or look for an upgraded version of this plugin");
            this.getLogger().severe("---");
            this.getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }


    @Override
    public void reload() throws IOException, InvalidConfigurationException {
        super.reload();

        Config config = this.getConfigManager().getConfig("config");
        boolean save = false;
        if (!config.isSet("debugMode")) {
            config.set("debugMode", false);
            save = true;
        }
        this.debugMode = config.getBoolean("debugMode");
        if (save) {
            config.save();
        }

        DebugUtil.debug("Running in debug mode");
    }

    public static SVCraftVehicles getInstance() {
        return instance;
    }

    public NMS getNMS() {
        return this.nms;
    }

    public boolean isInDebugMode() {
        return this.debugMode;
    }

    public VehicleRegistry getRegistry() {
        return this.registry;
    }

    /**
     * Get a map of all main entities to the vehicle they represent.
     *
     * @return The map
     */
    public Map<ArmorStand, Vehicle> getLoadedVehicles() {
        return this.loadedVehicles;
    }

    public Map<LivingEntity, Vehicle> getCurrentVehicleMap() {
        return this.currentVehicleMap;
    }

    /**
     * Get a map of all entities that are a port of a vehicle to
     * the vehicle they are a part of.
     *
     * @return The map
     */
    public Map<Entity, Vehicle> getVehiclePartMap() {
        return this.vehiclePartMap;
    }

    /**
     * Get a map of players that are currently using the vehicle spawner
     *
     * @return The map
     */
    public Map<Player, VehicleSpawnerTask> getVehicleSpawnerTaskMap() {
        return this.vehicleSpawnerTaskMap;
    }

    /**
     * Get the rpCompiler ModelManager data used for determining what models to use.
     */
    public RPCModelManagerData getResourcepackData() {
        return this.resourcepackData;
    }

    /**
     * Get the vehicle to entity is riding. Or null if none
     *
     * @param entity The entity to get the current vehicle for
     * @return The vehicle the entity is inside. Or null if none
     */
    @Nullable
    public Vehicle getVehicle(LivingEntity entity) {
        Vehicle vehicle = this.currentVehicleMap.get(entity);
        if (vehicle == null) return null;
        if (vehicle.isPassenger(entity)) {
            return vehicle;
        } else {
            return null;
        }
    }
}
