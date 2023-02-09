package me.alvin.vehicles.assets;

import ca.bkaw.praeter.core.model.DisplaySetting;
import ca.bkaw.praeter.core.model.Model;
import ca.bkaw.praeter.core.model.ModelDisplay;
import ca.bkaw.praeter.core.model.ModelElementList;
import ca.bkaw.praeter.core.model.ModelGroup;
import ca.bkaw.praeter.core.model.Perspective;
import ca.bkaw.praeter.core.resources.pack.JsonResource;
import ca.bkaw.praeter.core.resources.pack.ResourcePack;
import me.alvin.vehicles.util.RelativePos;
import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class ModelSplitter {
    private final ResourcePack resourcePack;
    private final NamespacedKey modelKey;
    private final Model model;
    private final Map<String, Consumer<Vector>> translationModifiers = new HashMap<>();
    private double displayScale = 1;
    private double elementScale = 1;
    private List<Part> parts;

    public ModelSplitter(ResourcePack resourcePack, NamespacedKey modelKey) throws IOException {
        this.resourcePack = resourcePack;
        this.modelKey = modelKey;
        JsonResource model = new JsonResource(this.resourcePack, this.resourcePack.getModelPath(modelKey));
        this.model = new Model(model);
    }

    public void addTranslationModifier(String groupName, Consumer<Vector> translationConsumer) {
        this.translationModifiers.put(groupName, translationConsumer);
    }

    public void setDisplayScale(double displayScale) {
        this.displayScale = displayScale;
    }

    public void setElementScale(double elementScale) {
        this.elementScale = elementScale;
    }

    public void split() throws IOException {
        List<ModelGroup> groups = this.model.getGroups();
        if (groups == null) {
            throw new IllegalArgumentException("The model has no groups.");
        }
        this.parts = new ArrayList<>(groups.size());
        for (ModelGroup group : groups) {
            if (group.getName().startsWith("_") || group.getName().startsWith(".")) {
                continue;
            }
            ModelElementList elements = this.model.getElements(group.getAllElements());

            // Scale
            // The scale origin doesn't matter because we center anyway
            Vector scaleVector = new Vector(this.elementScale, this.elementScale, this.elementScale);
            elements.scale(scaleVector, new Vector(8, 8, 8));

            // Center the elements
            Vector centerMovedBy = elements.center();

            ModelDisplay display = this.model.getDisplay();
            if (display == null) {
                display = new ModelDisplay();
            } else {
                display = display.deepCopy();
            }
            DisplaySetting headDisplaySetting = display.get(Perspective.HEAD);
            if (headDisplaySetting == null) {
                headDisplaySetting = new DisplaySetting();
                display.set(Perspective.HEAD, headDisplaySetting);
            }

            Vector translation = headDisplaySetting.getTranslation();
            if (translation == null) {
                translation = new Vector(0, 0, 0);
            }
            Vector originalTranslation = translation.clone();

            Consumer<Vector> translationConsumer = this.translationModifiers.get(group.getName());
            if (translationConsumer != null) {
                translationConsumer.accept(translation);
            }

            headDisplaySetting.setTranslation(translation);

            Model partModel = new Model();
            partModel.getJson().add("textures", this.model.getJson().get("textures"));
            partModel.setDisplay(display);
            partModel.setElements(elements);

            NamespacedKey partKey = new NamespacedKey(this.modelKey.getNamespace(), this.modelKey.getKey() + "/" + group.getName());
            Path partPath = this.resourcePack.getModelPath(partKey);
            JsonResource partResource = new JsonResource(this.resourcePack, partPath, partModel.getJson());
            partResource.save();

            Vector translationDiff = originalTranslation.subtract(translation);

            RelativePos offset = new RelativePos(
                getBlockOffset(centerMovedBy.getX(), 2.93333, -translationDiff.getX()),
                getBlockOffset(-centerMovedBy.getY(), 2.93333, translationDiff.getY()),
                getBlockOffset(centerMovedBy.getZ(), 2.93333, -translationDiff.getZ())
            );

            Part part = new Part(partKey, offset);
            this.parts.add(part);
        }
    }

    private static double getBlockOffset(double movedBy, double displayScale, double translatedBy) {
        return (movedBy * displayScale + translatedBy) / 25.6;
    }

    public List<Part> getParts() {
        return parts;
    }

    public record Part(NamespacedKey key, RelativePos offset) {}
}
