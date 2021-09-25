package me.alvin.vehicles.item;

import me.alvin.vehicles.SVCraftVehicles;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import svcraft.core.item.CustomItem;

import java.util.Collections;

public class SimpleMissileItem extends CustomItem implements MissileItem {
    public SimpleMissileItem() {
        super(new NamespacedKey(SVCraftVehicles.getInstance(), "missile"), Material.DIAMOND_HOE, "Missile");
    }

    @Override
    public ItemStack makeItemStack() {
        ItemStack item = SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:item/missile");
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(this.name, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        meta.lore(Collections.singletonList(Component.text("Explosion Power: " + this.getExplosionPower(item), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(CUSTOM_ITEM_ID_KEY, PersistentDataType.STRING, this.id.toString());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public int getExplosionPower(ItemStack itemStack) {
        return 3;
    }
}
