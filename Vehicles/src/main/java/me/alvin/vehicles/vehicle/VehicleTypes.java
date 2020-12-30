package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicles.SimpleBoatVehicle;
import me.alvin.vehicles.vehicles.SimpleHelicopterVehicle;
import me.alvin.vehicles.vehicles.TestVehicle;

import java.util.Arrays;
import java.util.Collections;

public class VehicleTypes {
    public static final VehicleType TEST_VEHICLE;
    public static final VehicleType SIMPLE_BOAT_VEHICLE;
    public static final VehicleType SIMPLE_HELICOPTER_VEHICLE;

    static {
        // Test Vehicle
        TEST_VEHICLE = new VehicleType(
                "test",
                TestVehicle.class,
                TestVehicle::new,
                TestVehicle::new,
                // Seats
                new Seat(new RelativePos(0.75, 1, -0.4)),
                Collections.singletonList(
                        new Seat(new RelativePos(0.1, 1, -0.4))
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

        // Simple Boat
        SIMPLE_BOAT_VEHICLE = new VehicleType(
                "simple_boat",
                SimpleBoatVehicle.class,
                SimpleBoatVehicle::new,
                SimpleBoatVehicle::new,
                // Seats
                new Seat(new RelativePos(0, 0, 0.1)),
                Collections.singletonList(
                        new Seat(new RelativePos(0, 0, -1))
                ),
                // Wheels
                null,
                // Extra gravity points
                Arrays.asList(
                        new RelativePos(0, 0, 1),
                        new RelativePos(0, 0, -1)
                )
        );

        // Simple Helicopter
        SIMPLE_HELICOPTER_VEHICLE = new VehicleType(
                "simple_helicopter",
                SimpleHelicopterVehicle.class,
                SimpleHelicopterVehicle::new,
                SimpleHelicopterVehicle::new,
                // Seats
                new Seat(new RelativePos(0.2, 0.5, 0.1)),
                Arrays.asList(
                        new Seat(new RelativePos(-0.5, 0.5, 0.1)),
                        new Seat(new RelativePos(0, 0.5, -1)),
                        new Seat(new RelativePos(-0.5, 0.5, -1))
                ),
                // Wheels
                null,
                // Extra gravity points
                Arrays.asList(
                        new RelativePos(0.6, 0, 0),
                        new RelativePos(-0.8, 0, 0),
                        new RelativePos(0.6, 0, -2),
                        new RelativePos(-0.8, 0, -2)
                )
        );
    }

    public static void register(VehicleRegistry registry) {
        registry.registerVehicle(TEST_VEHICLE);
        registry.registerVehicle(SIMPLE_BOAT_VEHICLE);
        registry.registerVehicle(SIMPLE_HELICOPTER_VEHICLE);
    }
}
