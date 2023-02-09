package me.alvin.vehicles.assets;

import ca.bkaw.praeter.core.resources.pack.JsonResource;
import ca.bkaw.praeter.core.resources.pack.ResourcePack;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.alvin.vehicles.commands.ModelSplitTestCommand;
import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssetsManager {
    public static final String HOLOGRAM_TEXTURE = "svcraftvehicles:vehicle/hologram";

    private final ResourcePack resourcePack;

    public AssetsManager(ResourcePack resourcePack) {
        this.resourcePack = resourcePack;
    }

    public void processAssets() throws IOException {
        this.processVehiclePart("golf_cart", true, "block/quartz_block_bottom");
        this.processVehiclePart("boat", true, "block/quartz_block_top");
        this.processVehiclePart("helicopter/helicopter_front", true, "block/quartz_block_top");
        this.processVehiclePart("helicopter/helicopter_tail", true, "block/quartz_block_top");
        this.processVehiclePart("helicopter/helicopter_tail_spinning", true, "block/quartz_block_top");
        this.processVehiclePart("helicopter/helicopter_rotor", false);
        this.processVehiclePart("car", true, "block/white_concrete");
        this.processVehiclePart("motorcycle", true, "block/white_concrete");
        this.processVehiclePart("truck/truck", true, "block/quartz_block_side");
        this.processVehiclePart("truck/truck_back", false);
        this.processVehiclePart("wooden_plane", false);
        this.processVehiclePart("plane/plane_front", true, "block/quartz_block_top");
        this.processVehiclePart("plane/plane_back", true, "block/quartz_block_top");
        this.processVehiclePart("plane/plane_left_wing", true, "block/quartz_block_top");
        this.processVehiclePart("plane/plane_right_wing", true, "block/quartz_block_top");
        this.processVehiclePart("plane/plane_propeller", false);

        addCustomModelData("block/testblock");
        addCustomModelData("block/vehicle_crafting_table");
        addCustomModelData("item/fuel");
        addCustomModelData("item/transparent");
        addCustomModelData("item/vehicle_spawner");
        addCustomModelData("item/leather_boots", "item/paint_bucket");
        addCustomModelData("item/leather_boots", "item/color_preview");
        addCustomModelData("gui/vehicle_crafting_table/selecting");
        addCustomModelData("gui/vehicle_crafting_table/viewing_enabled");
        addCustomModelData("gui/vehicle_crafting_table/viewing_disabled");
        addCustomModelData("gui/vehicle_crafting_table/crafting_disabled");
        addCustomModelData("gui/vehicle_crafting_table/crafting_enabled");
        addCustomModelData("gui/vehicle_crafting_table/timer");
        addCustomModelData("gui/vehicle_menu_icon/fuel");
        addCustomModelData("gui/vehicle_menu_icon/health");

        addCustomModelData("vehicle/plane_test");

        ModelSplitter modelSplitter = new ModelSplitter(this.resourcePack, new NamespacedKey("svcraftvehicles", "vehicle/plane_test"));
        modelSplitter.setElementScale(2);
        modelSplitter.setDisplayScale(44.0 / 15.0);
        modelSplitter.addTranslationModifier("back", translation -> translation.add(new Vector(0, 0, 12)));
        modelSplitter.addTranslationModifier("front", translation -> translation.add(new Vector(0, -18, 10)));
        modelSplitter.addTranslationModifier("left_wing", translation -> translation.add(new Vector(-10, 0, 0)));
        modelSplitter.addTranslationModifier("right_wing", translation -> translation.add(new Vector(10, 0, 0)));
        modelSplitter.split();
        for (ModelSplitter.Part part : modelSplitter.getParts()) {
            this.resourcePack.addCustomModelData(NamespacedKey.minecraft("item/barrier"), part.key());
        }
        ModelSplitTestCommand.parts = modelSplitter.getParts();
    }

    private void addCustomModelData(String model) throws IOException {
        addCustomModelData("item/barrier", model);
    }
    private void addCustomModelData(String vanillaModel, String model) throws IOException {
        NamespacedKey vanillaModelKey = NamespacedKey.minecraft(vanillaModel);
        NamespacedKey modelKey = new NamespacedKey("svcraftvehicles", model);
        this.resourcePack.addCustomModelData(vanillaModelKey, modelKey);
    }

    public void processVehiclePart(String path, boolean colorable, String... colorableTextures) throws IOException {
        NamespacedKey key = new NamespacedKey("svcraftvehicles", "vehicle/" + path);
        if (colorable) {
            this.processColorableModel(key, colorableTextures);
        }

        NamespacedKey barrier = NamespacedKey.minecraft("item/barrier");
        NamespacedKey vanillaKey = colorable ? NamespacedKey.minecraft("item/leather_boots") : barrier;
        this.resourcePack.addCustomModelData(vanillaKey, key);

        NamespacedKey hologramKey = this.createHologram(key);
        this.resourcePack.addCustomModelData(barrier, hologramKey);
    }

    public void processColorableModel(NamespacedKey modelKey, String... colorableTextures) throws IOException {
        Path path = this.resourcePack.getModelPath(modelKey);
        JsonResource resource = new JsonResource(this.resourcePack, path);

        Set<String> colorableTextureSet = new HashSet<>(List.of(colorableTextures));

        // Get all the texture keys that can be colored
        Set<String> colorableTextureKeys = new HashSet<>();
        JsonObject texturesJson = resource.getJson().getAsJsonObject("textures");
        for (Map.Entry<String, JsonElement> entry : texturesJson.entrySet()) {
            String texture = entry.getValue().getAsString();
            if (colorableTextureSet.contains(texture)) {
                colorableTextureKeys.add('#' + entry.getKey());
            }
        }

        // Set the tintindex of all faces on elements with colorable textures
        JsonArray elements = resource.getJson().getAsJsonArray("elements");
        for (JsonElement jsonElement : elements) {
            JsonObject element = jsonElement.getAsJsonObject();
            JsonObject faces = element.getAsJsonObject("faces");
            for (Map.Entry<String, JsonElement> entry : faces.entrySet()) {
                JsonObject face = entry.getValue().getAsJsonObject();
                if (colorableTextureKeys.contains(face.get("texture").getAsString())) {
                    // A colorable face
                    face.addProperty("tintindex", 0);
                }
            }
        }

        // Save the json
        resource.save();
    }

    public NamespacedKey createHologram(NamespacedKey modelKey) throws IOException {
        NamespacedKey hologramKey = new NamespacedKey(modelKey.getNamespace(), modelKey.getKey() + "_hologram");
        Path modelPath = this.resourcePack.getModelPath(modelKey);
        Path hologramPath = this.resourcePack.getModelPath(hologramKey);
        Files.copy(modelPath, hologramPath);
        JsonResource hologram = new JsonResource(this.resourcePack, hologramPath);
        // Replace all textures with the hologram texture
        JsonObject textures = hologram.getJson().getAsJsonObject("textures");
        for (String textureKey : new HashSet<>(textures.keySet())) {
            textures.addProperty(textureKey, HOLOGRAM_TEXTURE);
        }
        hologram.save();
        return hologramKey;
    }
}
