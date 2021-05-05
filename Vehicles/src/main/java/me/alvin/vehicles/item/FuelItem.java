package me.alvin.vehicles.item;

import me.alvin.vehicles.SVCraftVehicles;
import svcraft.core.item.CustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class FuelItem extends CustomItem {
    /**
     * The amount of fuel one fuel item adds to the vehicle's current fuel.
     */
    public static final int FUEL_AMOUNT = 5000;

    public FuelItem() {
        super(new NamespacedKey(SVCraftVehicles.getInstance(), "fuel"), Material.DIAMOND_HOE, "Fuel");
    }

    @Override
    public ItemStack makeItemStack() {
        ItemStack item = SVCraftVehicles.getInstance().getResourcepackData().generateItem("svcraftvehicles:item/fuel");
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(this.name).decoration(TextDecoration.ITALIC, false));
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(CUSTOM_ITEM_ID_KEY, PersistentDataType.STRING, this.id.toString());
        item.setItemMeta(meta);
        return item;
    }
}
