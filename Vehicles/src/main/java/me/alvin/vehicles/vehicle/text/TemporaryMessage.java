package me.alvin.vehicles.vehicle.text;

import me.alvin.vehicles.vehicle.Vehicle;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * A temporary message, that doesn't change, to display on the vehicle text
 * for a specified number of ticks.
 */
public class TemporaryMessage implements VehicleTextEntry {
    private final Component message;
    private final int maxTime;
    private int time;

    /**
     * Create a new VehicleTextMessage instance.
     *
     * @param message The message to display
     * @param maxTime The time to display it for, in ticks
     */
    public TemporaryMessage(Component message, int maxTime) {
        this.message = message;
        this.maxTime = maxTime;
    }

    @Override
    public Component getMessage(Vehicle vehicle, Player player) {
        return this.message;
    }

    public int getMaxTime() {
        return this.maxTime;
    }

    public int getTime() {
        return this.time;
    }

    public void incrementTime() {
        this.time++;
    }
}
