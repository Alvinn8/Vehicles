package me.alvin.vehicles;

import com.comphenix.protocol.ProtocolLibrary;
import me.alvin.vehicles.commands.VehiclesCommand;
import me.alvin.vehicles.nms.NMS;
import me.alvin.vehicles.nms.NMS_v1_16_R2;
import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.DebugUtil;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleTypes;
import me.svcraft.minigames.SVCraft;
import me.svcraft.minigames.config.Config;
import me.svcraft.minigames.plugin.SVCraftPlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class SVCraftVehicles extends SVCraftPlugin {

    private static SVCraftVehicles instance;

    private NMS nms;

    private boolean debugMode = false;
    private VehicleRegistry registry;
    private final Map<ArmorStand, Vehicle> loadedVehicles = new HashMap<>();
    private final Map<LivingEntity, Vehicle> currentVehicleMap = new HashMap<>();

    @Override
    public void onPluginEnable() {
        instance = this;

        if (!this.setupNMS()) return;

        try {
            this.reload();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        this.registerCommand("vehicles", new VehiclesCommand(this));
        this.registerPerWorldEvents(new EventListener());

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));

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
            case "v1_16_R2": this.nms = new NMS_v1_16_R2(); break;
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

    public Map<ArmorStand, Vehicle> getLoadedVehicles() {
        return this.loadedVehicles;
    }

    public Map<LivingEntity, Vehicle> getCurrentVehicleMap() {
        return this.currentVehicleMap;
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
            DebugUtil.debug("Not a passenger");
            return null;
        }
    }
}
