package me.alvin.vehicles.actions;

import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.action.VehicleMenuAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import svcraft.core.util.CustomInventory;

public class HealthAction implements VehicleMenuAction {
    public static final HealthAction INSTANCE = new HealthAction();

    private HealthAction() {}

    @Override
    public ItemStack getEntryItem(Vehicle vehicle, Player player) {
        ItemStack itemStack = new ItemStack(Material.POTION);
        itemStack.editMeta(meta -> meta.displayName(Component.text("Health").decoration(TextDecoration.ITALIC, false)));
        return itemStack;
    }

    @Override
    public void onMenuClick(Vehicle vehicle, Player player) {
        Inventory inventory = Bukkit.createInventory(new CustomInventory() {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
            }
        }, 9, Component.text("Health"));
        // TODO: Gui and repairing

        ItemStack item = new ItemStack(Material.POTION);
        int health = (int) Math.ceil(vehicle.getHealth());
        int maxHealth = (int) Math.ceil(vehicle.getType().getMaxHealth());

        NamedTextColor color;
        if (health <= maxHealth / 4) color = NamedTextColor.RED;
        else if (health <= maxHealth / 2) color = NamedTextColor.YELLOW;
        else color = NamedTextColor.WHITE;

        TextComponent.Builder component = Component.text()
            .decoration(TextDecoration.ITALIC, false)
            .color(NamedTextColor.WHITE)
            .append(Component.text(health, color))
            .append(Component.text(" / " + maxHealth));
        item.editMeta(meta -> meta.displayName(component.build()));

        player.openInventory(inventory);
    }
}
