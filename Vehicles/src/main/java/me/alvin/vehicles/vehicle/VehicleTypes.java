package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.actions.FuelAction;
import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicles.TestVehicle;

import java.util.Arrays;
import java.util.Collections;

public class VehicleTypes {
    public static final VehicleType TEST_VEHICLE;

    static {
        // Test Vehicle
        TEST_VEHICLE = new VehicleType(
                "test",
                TestVehicle.class,
                TestVehicle::new,
                TestVehicle::new,
                // Actions
                Collections.singletonList(
                        FuelAction.INSTANCE
                ),
                // Seats
                new Seat(new RelativePos(0.75, -0.4, -0.4)),
                Collections.singletonList(
                        new Seat(new RelativePos(0.1, -0.4, -0.4))
                ),
                // Wheels
                Arrays.asList(
                        new Wheel(new RelativePos(1.125, 0.4375, 1.5), 0.4375F),
                        new Wheel(new RelativePos(-0.35, 0.4375, 1.5), 0.4375F),
                        new Wheel(new RelativePos(1.125, 0.4375, -1.14), 0.4375F),
                        new Wheel(new RelativePos(-0.3, 0.4375, -1.14), 0.4375F)
                ),
                // Extra gravity points
                null
        );
    }

    public static void register(VehicleRegistry registry) {
        registry.registerVehicle(TEST_VEHICLE);
    }
}
