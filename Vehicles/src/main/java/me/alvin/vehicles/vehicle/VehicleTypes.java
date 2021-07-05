package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.collision.AABBCollision;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicles.AttackHelicopterVehicle;
import me.alvin.vehicles.vehicles.GolfCartVehicle;
import me.alvin.vehicles.vehicles.MotorcycleVehicle;
import me.alvin.vehicles.vehicles.SimpleBoatVehicle;
import me.alvin.vehicles.vehicles.SimpleCarVehicle;
import me.alvin.vehicles.vehicles.SimpleHelicopterVehicle;
import me.alvin.vehicles.vehicles.TestVehicle;
import me.alvin.vehicles.vehicles.TruckVehicle;
import me.alvin.vehicles.vehicles.WoodenPlaneVehicle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

import static me.alvin.vehicles.crafting.recipe.RecipeStep.step;
import static me.alvin.vehicles.crafting.recipe.VehicleCraftingRecipe.recipe;

public class VehicleTypes {
    public static final VehicleType TEST;
    public static final VehicleType GOLF_CART;
    public static final VehicleType SIMPLE_BOAT;
    public static final VehicleType SIMPLE_HELICOPTER;
    public static final VehicleType SIMPLE_CAR;
    public static final VehicleType MOTORCYCLE;
    public static final VehicleType TRUCK;
    public static final VehicleType WOODEN_PLANE;
    public static final VehicleType ATTACK_HELICOPTER;

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
                ),
                40,
                // Recipe,
                recipe()
                    .displayItem(new ItemStack(Material.STONE))
                    .addStep(step()
                        .name(Component.text("Test Step 1"))
                        .addItem(new ItemStack(Material.DIAMOND, 10))
                        .addItem(new ItemStack(Material.COPPER_INGOT, 10))
                        .addItem(new ItemStack(Material.REDSTONE, 10))
                        .addItem(new ItemStack(Material.IRON_INGOT, 10))
                        .addItem(new ItemStack(Material.GOLD_BLOCK, 10))
                        .addItem(new ItemStack(Material.GOLD_INGOT, 10))
                        .addItem(new ItemStack(Material.WHITE_CONCRETE, 10))
                        .completeTime(10000)
                    )
                    .addStep(step()
                        .name(Component.text("Test Step 2"))
                        .addItem(new ItemStack(Material.IRON_INGOT, 100))
                        .completeTime(50000)
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
            ),
            100,
            // Recipe,
            null
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
                ),
                100,
            // Recipe,
            null
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
                ),
                150,
            // Recipe,
            null
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
            ),
            150,
            // Recipe,
            null
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
            null,
            50,
            // Recipe,
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
            // Seats
            new Seat(new RelativePos(0.6, 2, 0.5)),
            Collections.singletonList(
                new Seat(new RelativePos(-0.5, 2, 0.5))
            ),
            200,
            // Recipe,
            null
        );

        // Wooden Plane
        WOODEN_PLANE = new VehicleType(
            "wooden_plane",
            Component.text("Wooden Plane"),
            WoodenPlaneVehicle.class,
            WoodenPlaneVehicle::new,
            WoodenPlaneVehicle::new,
            // Collision
            new AABBCollision(3, 2),
            // Seats
            new Seat(new RelativePos(0, 0.8, -0.3)),
            Collections.singletonList(
                new Seat(new RelativePos(0, 0.8, -1.4))
            ),
            80,
            // Recipe,
            null
        );

        // Attack Helicopter
        ATTACK_HELICOPTER = new VehicleType(
            "attack_helicopter",
            Component.text("Attack Helicopter"),
            AttackHelicopterVehicle.class,
            AttackHelicopterVehicle::new,
            AttackHelicopterVehicle::new,
            // Collision
            new AABBCollision(2, 3),
            // Seats
            new Seat(new RelativePos(0, 1.2, -1.25)),
            Collections.singletonList(
                new Seat(new RelativePos(0, 1.0, -0.3))
            ),
            200,
            // Recipe,
            null
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
        registry.registerVehicle(WOODEN_PLANE);
        registry.registerVehicle(ATTACK_HELICOPTER);
    }
}
