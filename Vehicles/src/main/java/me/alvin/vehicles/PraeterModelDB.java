package me.alvin.vehicles;

import ca.bkaw.praeter.core.Praeter;
import ca.bkaw.praeter.core.resources.bake.BakedItemModel;
import ca.bkaw.praeter.core.resources.bake.BakedResourcePack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import svcraft.core.resourcepack.modeldb.ModelDB;
import svcraft.core.resourcepack.modeldb.ModelDBCustomModelDataEntry;
import svcraft.core.resourcepack.modeldb.ModelDBEntry;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// A temporary bridge between praeter baked packs and svcraft ModelDB
public class PraeterModelDB extends ModelDB {
    private Map<String, ModelDBCustomModelDataEntry> models;

    private Map<String, ModelDBCustomModelDataEntry> getModels() {
        if (this.models == null) {
            try {
                BakedResourcePack baked = Praeter.get().getResourceManager().getBakedPacks().getMain();
                Field itemModels = BakedResourcePack.class.getDeclaredField("itemModels");
                itemModels.setAccessible(true);
                Map<NamespacedKey, BakedItemModel> map = (Map<NamespacedKey, BakedItemModel>) itemModels.get(baked);
                this.models = new HashMap<>();
                for (Map.Entry<NamespacedKey, BakedItemModel> entry : map.entrySet()) {
                    String modelNamespacedkey = entry.getKey().toString();
                    BakedItemModel praeter = entry.getValue();
                    ModelDBCustomModelDataEntry svcraft = new ModelDBCustomModelDataEntry(praeter.material(), praeter.customModelData());
                    this.models.put(modelNamespacedkey, svcraft);
                }
            } catch (Throwable e) {
                this.models = null;
                throw new RuntimeException(e);
            }
        }
        return models;
    }

    @Override
    public @NotNull ItemStack generateItem(String modelNamespacekey) {
        ModelDBEntry entry = getEntry(modelNamespacekey);
        ItemStack itemStack;
        if (entry != null) {
            itemStack = entry.generateItem();
            itemStack.editMeta((meta) ->
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE)
            );
            return itemStack;
        } else {
            itemStack = new ItemStack(Material.BARRIER);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Component.text("Missing model").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED));
            itemMeta.lore(Collections.singletonList(Component.text(modelNamespacekey).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE)));
            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }
    }

    @Override
    public @Nullable ModelDBEntry getEntry(String modelNamespacekey) {
        return this.getModels().get(modelNamespacekey);
    }
}
