package me.alvin.vehicles;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.alvin.vehicles.nms.NMS;
import me.alvin.vehicles.vehicle.Vehicle;
import org.bukkit.plugin.Plugin;

public class PacketListener extends PacketAdapter {
    private final NMS nms;

    public PacketListener(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE);
        this.nms = SVCraftVehicles.getInstance().getNMS();
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
            Vehicle vehicle = SVCraftVehicles.getInstance().getVehicle(event.getPlayer());
            if (vehicle != null) {
                if (vehicle.getDriver() == event.getPlayer()) {
                    this.nms.handlePacket(vehicle.movement, event);
                    if (vehicle.movement.space) event.setCancelled(true);
                }
            }
        }
    }
}
