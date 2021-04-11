package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.collision.AABBCollision;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicles.GolfCartVehicle;
import me.alvin.vehicles.vehicles.MotorcycleVehicle;
import me.alvin.vehicles.vehicles.SimpleBoatVehicle;
import me.alvin.vehicles.vehicles.SimpleCarVehicle;
import me.alvin.vehicles.vehicles.SimpleHelicopterVehicle;
import me.alvin.vehicles.vehicles.TestVehicle;
import me.alvin.vehicles.vehicles.TruckVehicle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Arrays;
import java.util.Collections;

public class VehicleTypes {
    public static final VehicleType TEST;
    public static final VehicleType GOLF_CART;
    public static final VehicleType SIMPLE_BOAT;
    public static final VehicleType SIMPLE_HELICOPTER;
    public static final VehicleType SIMPLE_CAR;
    public static final VehicleType MOTORCYCLE;
    public static final VehicleType TRUCK;

    static {
        // Test Vehicle
        TEST = new VehicleType(
                "test",
                Component.text("Test Vehicle").decoration(TextDecoration.ITALIC, true).color(NamedTextColor.GRAY),
                TestVehicle.class,
                TestVehicle::new,
                TestVehicle::new,
                // Collision
                new AABBCollision(2, 2),
                // Seats
                new Seat(new RelativePos(0.75, 1, -0.4)),
                Collections.singletonList(
                        new Seat(new RelativePos(0.1, 1, -0.4))
                )
        );

        // Golf Cart
        GOLF_CART = new VehicleType(
            "golf_cart",
            Component.text("Golf Cart"),
            GolfCartVehicle.class,
            GolfCartVehicle::new,
            GolfCartVehicle::new,
            // Collision
            new AABBCollision(1.5, 2.5),
            // Seats
            new Seat(new RelativePos(0.75, 1, -0.4)),
            Collections.singletonList(
                new Seat(new RelativePos(0.1, 1, -0.4))
            )
        );

        // Simple Boat
        SIMPLE_BOAT = new VehicleType(
                "simple_boat",
                Component.text("Simple Boat"),
                SimpleBoatVehicle.class,
                SimpleBoatVehicle::new,
                SimpleBoatVehicle::new,
                // Collision
                new AABBCollision(2, 2),
                // Seats
                new Seat(new RelativePos(0, 0, 0.1)),
                Collections.singletonList(
                        new Seat(new RelativePos(0, 0, -1))
                )
        );

        // Simple Helicopter
        SIMPLE_HELICOPTER = new VehicleType(
                "simple_helicopter",
                Component.text("Simple Helicopter"),
                SimpleHelicopterVehicle.class,
                SimpleHelicopterVehicle::new,
                SimpleHelicopterVehicle::new,
                // Collision
                new AABBCollision(2, 3),
                // Seats
                new Seat(new RelativePos(0.2, 0.5, 0.1)),
                Arrays.asList(
                        new Seat(new RelativePos(-0.5, 0.5, 0.1)),
                        new Seat(new RelativePos(0, 0.5, -1)),
                        new Seat(new RelativePos(-0.5, 0.5, -1))
                )
        );

        // Simple Car
        SIMPLE_CAR = new VehicleType(
            "simple_car",
            Component.text("Simple Car"),
            SimpleCarVehicle.class,
            SimpleCarVehicle::new,
            SimpleCarVehicle::new,
            // Collision
            new AABBCollision(2, 2),
            // Seats
            new Seat(new RelativePos(0.35, 0.3, 0.1)),
            Arrays.asList(
                new Seat(new RelativePos(-0.35, 0.3, 0.1)),
                new Seat(new RelativePos(0.35, 0.3, -1.0)),
                new Seat(new RelativePos(-0.35, 0.3, -1.0))
            )
        );

        // Motorcycle
        MOTORCYCLE = new VehicleType(
            "motorcycle",
            Component.text("Motorcycle"),
            MotorcycleVehicle.class,
            MotorcycleVehicle::new,
            MotorcycleVehicle::new,
            // Collision
            new AABBCollision(1.5, 1.5),
            // Seat
            new Seat(new RelativePos(-0.1, 1.3, -0.8)),
            null
        );

        // Truck
        TRUCK = new VehicleType(
            "truck",
            Component.text("Truck"),
            TruckVehicle.class,
            TruckVehicle::new,
            TruckVehicle::new,
            // Collision
            new AABBCollision(2.5, 3.5),
            // Seat
            new Seat(new RelativePos(0.6, 2, 0.5)),
            Collections.singletonList(
                new Seat(new RelativePos(-0.5, 2, 0.5))
            )
        );
    }

    public static void register(VehicleRegistry registry) {
        registry.registerVehicle(TEST);
        registry.registerVehicle(GOLF_CART);
        registry.registerVehicle(SIMPLE_BOAT);
        registry.registerVehicle(SIMPLE_HELICOPTER);
        registry.registerVehicle(SIMPLE_CAR);
        registry.registerVehicle(MOTORCYCLE);
        registry.registerVehicle(TRUCK);
    }
}
