package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.vehicle.GroundVehicle;
import me.alvin.vehicles.vehicle.Vehicle;
import me.alvin.vehicles.vehicle.VehicleType;
import me.alvin.vehicles.vehicle.VehicleTypes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class TestVehicle extends GroundVehicle {
    public TestVehicle(ArmorStand entity) {
        super(entity);
    }

    public TestVehicle(Location location, Player creator) {
        super(location, creator);

        ItemStack helmet = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = helmet.getItemMeta();
        meta.setUnbreakable(true);
        ((Damageable) meta).setDamage(6);
        helmet.setItemMeta(meta);
        this.entity.getEquipment().setHelmet(helmet);
    }

    @Override
    public VehicleType getType() {
        return VehicleTypes.TEST_VEHICLE;
    }

    @Override
    public float getAccelerationSpeed() {
        return 0.75F;
    }

    @Override
    public float getMaxSpeed() {
        return 40;
    }
}
