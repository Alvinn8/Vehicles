package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.actions.FuelAction;
import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.action.VehicleActionsBuilder;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicles.TestVehicle;

public class VehicleTypes {
    public static final VehicleType TEST_VEHICLE;

    static {
        // Test Vehicle
        TEST_VEHICLE = new VehicleType(
                "test",
                TestVehicle.class,
                TestVehicle::new,
                TestVehicle::new,
                new VehicleActionsBuilder()
                        .add(FuelAction.INSTANCE)
                .build(),
                new Seat(new RelativePos(0.75, -0.4, -0.4)),
                new Seat(new RelativePos(0.1, -0.4, -0.4))
        );
    }

    public static void register(VehicleRegistry registry) {
        registry.registerVehicle(TEST_VEHICLE);
    }
}
