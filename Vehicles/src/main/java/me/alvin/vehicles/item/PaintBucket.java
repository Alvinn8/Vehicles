package me.alvin.vehicles.item;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.gui.paint.PaintGui;
import me.alvin.vehicles.vehicle.Vehicle;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import svcraft.core.item.CustomItem;

public class PaintBucket extends CustomItem {
    @Override
    public ItemStack makeItemStack() {
        ItemStack item = SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:item/paint_bucket");
        item.editMeta(LeatherArmorMeta.class, meta -> {
            meta.setColor(Color.WHITE);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
        });
        this.setName(item, "Paint Bucket");
        this.addId(item);
        return item;
    }

    @Override
    public void onUse(Player player, ItemStack item, Cancellable cancellable) {
        if (!(cancellable instanceof PlayerInteractAtEntityEvent event)) {
            PaintGui gui = new PaintGui(color -> {
                if (color != null) {
                    item.editMeta(LeatherArmorMeta.class, meta ->
                        meta.setColor(color)
                    );
                }

            });
            gui.show(player);
            return;
        }
        Entity rightClicked = event.getRightClicked();
        Vehicle vehicle = SVCraftVehicles.getInstance().getVehiclePartMap().get(rightClicked);
        if (vehicle == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof LeatherArmorMeta colorMeta)) {
            return;
        }
        Color color = colorMeta.getColor();
        vehicle.setColor(color);
        colorMeta.setColor(Color.WHITE);
        item.setItemMeta(meta);
        cancellable.setCancelled(true);
    }
}
