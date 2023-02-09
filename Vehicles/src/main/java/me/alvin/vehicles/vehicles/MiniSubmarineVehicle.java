package me.alvin.vehicles.vehicles;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.UnderwaterVehicle;
import me.alvin.vehicles.vehicle.VehicleSpawnReason;
import me.alvin.vehicles.vehicle.VehicleType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: Smoke
public class MiniSubmarineVehicle extends UnderwaterVehicle {
    public MiniSubmarineVehicle(@NotNull ArmorStand entity) {
        super(entity);
    }

    public MiniSubmarineVehicle(@NotNull Location location, @Nullable Player creator, @NotNull VehicleSpawnReason reason) {
        super(location, creator, reason);
    }

    @Override
    protected void addParts() {
        this.mainPart(new NamespacedKey(SVCraftVehicles.getInstance(), "vehicle/mini_submarine"), RelativePos.ZERO, false);
    }

    @Override
    public @NotNull VehicleType getType() {
        throw new UnsupportedOperationException();
        // return VehicleTypes.MINI_SUBMARINE;
    }

    @Override
    public boolean usesFuel() {
        return false;
    }

    @Override
    public float getAccelerationSpeed() {
        return 0.25F;
    }

    @Override
    public float getMaxSpeed() {
        return 20; // max speed madness
    }
}
