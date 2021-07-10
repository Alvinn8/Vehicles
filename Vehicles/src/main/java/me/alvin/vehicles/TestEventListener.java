package me.alvin.vehicles;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TestEventListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        System.out.println("InventoryClickEvent called");
        if (event.getView().title().equals(Component.text("Fuel"))) { // just for testing
            System.out.println("InventoryClickEvent called for \"Fuel\" inventory.");

            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                event.setCancelled(true);
                System.out.println("cancelled");
            }
        }
    }
}
