package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicles.GolfCartVehicle;
import me.alvin.vehicles.vehicles.MotorcycleVehicle;
import me.alvin.vehicles.vehicles.SimpleBoatVehicle;
import me.alvin.vehicles.vehicles.SimpleCarVehicle;
import me.alvin.vehicles.vehicles.SimpleHelicopterVehicle;
import me.alvin.vehicles.vehicles.TestVehicle;

import java.util.Arrays;
import java.util.Collections;

public class VehicleTypes {
    public static final VehicleType TEST;
    public static final VehicleType GOLF_CART;
    public static final VehicleType SIMPLE;
    public static final VehicleType SIMPLE_HELICOPTER;
    public static final VehicleType SIMPLE_CAR;
    public static final VehicleType MOTORCYCLE;

    static {
        // Test Vehicle
        TEST = new VehicleType(
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

        // Golf Cart
        GOLF_CART = new VehicleType(
            "golf_cart",
            GolfCartVehicle.class,
            GolfCartVehicle::new,
            GolfCartVehicle::new,
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
        SIMPLE = new VehicleType(
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
        SIMPLE_HELICOPTER = new VehicleType(
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

        // Simple Car
        SIMPLE_CAR = new VehicleType(
            "simple_car",
            SimpleCarVehicle.class,
            SimpleCarVehicle::new,
            SimpleCarVehicle::new,
            // Seats
            new Seat(new RelativePos(0.35, 0.3, 0.1)),
            Arrays.asList(
                new Seat(new RelativePos(-0.35, 0.3, 0.1)),
                new Seat(new RelativePos(0.35, 0.3, -1.0)),
                new Seat(new RelativePos(-0.35, 0.3, -1.0))
            ),
            // TODO: wheels (they are taken from golfcart)
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

        // Motorcycle
        MOTORCYCLE = new VehicleType(
            "motorcycle",
            MotorcycleVehicle.class,
            MotorcycleVehicle::new,
            MotorcycleVehicle::new,
            // Seat
            new Seat(new RelativePos(0.35, 0.3, 0.1)),
            null,
            // TODO: wheels and gravity points
            // Wheels
            Arrays.asList(
                new Wheel(new RelativePos(1.125, 0.4375, 1.5), 0.4375F),
                new Wheel(new RelativePos(-0.35, 0.4375, 1.5), 0.4375F)
            ),
            // Extra gravity points
            null
        );
    }

    public static void register(VehicleRegistry registry) {
        registry.registerVehicle(TEST);
        registry.registerVehicle(GOLF_CART);
        registry.registerVehicle(SIMPLE);
        registry.registerVehicle(SIMPLE_HELICOPTER);
        registry.registerVehicle(SIMPLE_CAR);
        registry.registerVehicle(MOTORCYCLE);
    }
}
