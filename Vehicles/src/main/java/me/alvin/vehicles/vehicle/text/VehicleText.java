package me.alvin.vehicles.vehicle.text;

import me.alvin.vehicles.vehicle.Vehicle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Stores what text to display on the action bar when inside a vehicle.
 */
public class VehicleText {
    private final Vehicle vehicle;
    private final List<VehicleTextEntry> entries = new ArrayList<>();

    public VehicleText(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /**
     * Get a mutable list of text entries.
     *
     * @return The list of entries
     */
    public List<VehicleTextEntry> getEntries() {
        return this.entries;
    }

    /**
     * Add a text entry to the end of the entries list.
     *
     * @param entry The entry to add
     */
    public void addEntry(VehicleTextEntry entry) {
        this.entries.add(entry);
    }

    /**
     * Get one final component to display to the player.
     *
     * <p>This method does not display the action bar, only generates
     * the component to display.</p>
     *
     * @param player The player to display the component to.
     * @return The component to display
     */
    public Component getComponent(Player player) {
        TextComponent.Builder component = Component.text();

        boolean isFirst = true;
        for (VehicleTextEntry entry : this.entries) {
            if (!entry.shouldDisplay(this.vehicle, player)) continue;
            if (isFirst) {
                isFirst = false;
            } else {
                // Separator for all entries except the first
                component.append(Component.text(" | "));
            }

            component.append(entry.getMessage(this.vehicle, player));
        }
        return component.build();
    }

    /**
     * Tick all temporary messages and remove ones that have expired.
     */
    public void tickMessages() {
        Iterator<VehicleTextEntry> iterator = this.entries.iterator();
        while (iterator.hasNext()) {
            VehicleTextEntry entry = iterator.next();
            if (entry instanceof TemporaryMessage message) {
                message.incrementTime();
                if (message.getTime() >= message.getMaxTime()) {
                    iterator.remove();
                }
            }
        }
    }
}
