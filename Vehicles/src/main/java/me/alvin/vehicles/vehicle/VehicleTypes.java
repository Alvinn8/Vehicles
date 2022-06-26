package me.alvin.vehicles.vehicle;

import me.alvin.vehicles.SVCraftVehicles;
import me.alvin.vehicles.registry.VehicleRegistry;
import me.alvin.vehicles.util.RelativePos;
import me.alvin.vehicles.vehicle.collision.AABBCollision;
import me.alvin.vehicles.vehicle.perspective.ThirdPersonPerspective;
import me.alvin.vehicles.vehicle.perspective.VehiclePartPerspective;
import me.alvin.vehicles.vehicle.seat.Seat;
import me.alvin.vehicles.vehicles.GolfCartVehicle;
import me.alvin.vehicles.vehicles.MiniSubmarineVehicle;
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
    public static final VehicleType MINI_SUBMARINE;

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
                // Perspectives
                Arrays.asList(
                    new ThirdPersonPerspective(new RelativePos(0, 0, -15)),
                    new VehiclePartPerspective("Test", new RelativePos(3, 3, -3))
                ),
                // Max Health
                40,
                // Enableable
                SVCraftVehicles.getInstance(),
                // Recipe
                recipe()
                    .displayItem(new ItemStack(Material.STONE))
                    .addStep(step()
                        .name(Component.text("Test Vehicle").decorate(TextDecoration.ITALIC))
                        .addItem(new ItemStack(Material.BEDROCK))
                        .completeTime(20)
                    )
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
            // Perspectives
            Collections.singletonList(
                new ThirdPersonPerspective(new RelativePos(0, 0, -5))
            ),
            // Max Health
            100,
            // Enableable
            SVCraftVehicles.getInstance(),
            // Recipe
            recipe()
                .displayItem(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/golf_cart"))
                .addStep(step()
                    .name(Component.text("Body"))
                    .addItem(new ItemStack(Material.IRON_INGOT, 20))
                    .addItem(new ItemStack(Material.WHITE_CONCRETE, 10))
                    .addItem(new ItemStack(Material.BLACK_CONCRETE, 10))
                    .addItem(new ItemStack(Material.GLASS_PANE, 4))
                    .completeTime(180000)
                )
                .addStep(step()
                    .name(Component.text("Wheels"))
                    .addItem(new ItemStack(Material.BLACK_WOOL, 4))
                    .completeTime(40000)
                )
                .addStep(step()
                    .name(Component.text("Electric Engine"))
                    .addItem(new ItemStack(Material.PISTON, 1))
                    .addItem(new ItemStack(Material.IRON_INGOT, 6))
                    .addItem(new ItemStack(Material.COPPER_INGOT, 12))
                    .addItem(new ItemStack(Material.REDSTONE, 15))
                    .addItem(new ItemStack(Material.REDSTONE_TORCH, 10))
                    .addItem(new ItemStack(Material.REPEATER, 5))
                    .completeTime(120000)
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
                ),
                // Perspectives
                Collections.singletonList(
                    new ThirdPersonPerspective(new RelativePos(0, 0, -5))
                ),
                // Max Health
                100,
                // Enableable
                SVCraftVehicles.getInstance(),
            // Recipe
            recipe()
                .displayItem(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/simple_car"))
                .addStep(step()
                    .name(Component.text("Body"))
                    .addItem(new ItemStack(Material.IRON_BLOCK, 1))
                    .addItem(new ItemStack(Material.IRON_INGOT, 35))
                    .addItem(new ItemStack(Material.COPPER_INGOT, 10))
                    .addItem(new ItemStack(Material.GLASS_PANE, 1))
                    .addItem(new ItemStack(Material.GRAY_WOOL, 2))
                    .completeTime(210000)
                )
                .addStep(step()
                    .name(Component.text("Engine"))
                    .addItem(new ItemStack(Material.IRON_INGOT, 2))
                    .addItem(new ItemStack(Material.FURNACE, 1))
                    .addItem(new ItemStack(Material.REDSTONE, 10))
                    .addItem(new ItemStack(Material.REDSTONE_TORCH, 2))
                    .addItem(new ItemStack(Material.REPEATER, 5))
                    .completeTime(180000)
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
                ),
                // Perspectives
                Collections.singletonList(
                    new ThirdPersonPerspective(new RelativePos(0, 0, -10))
                ),
                // Max Health
                150,
                // Enableable
                SVCraftVehicles.getInstance(),
            // Recipe
            recipe()
                .displayItem(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:missing"))
                .addStep(step()
                    .name(Component.text("Body"))
                    .addItem(new ItemStack(Material.IRON_BLOCK, 2))
                    .addItem(new ItemStack(Material.IRON_INGOT, 50))
                    .addItem(new ItemStack(Material.COPPER_INGOT, 16))
                    .addItem(new ItemStack(Material.GLASS_PANE, 4))
                    .completeTime(300000)
                )
                .addStep(step()
                    .name(Component.text("Tail"))
                    .addItem(new ItemStack(Material.IRON_BLOCK, 1))
                    .addItem(new ItemStack(Material.IRON_INGOT, 15))
                    .addItem(new ItemStack(Material.IRON_NUGGET, 6))
                    .completeTime(180000)
                )
                .addStep(step()
                    .name(Component.text("Skids"))
                    .addItem(new ItemStack(Material.IRON_INGOT, 10))
                    .addItem(new ItemStack(Material.IRON_NUGGET, 8))
                    .completeTime(60000)
                )
                .addStep(step()
                    .name(Component.text("Engine"))
                    .addItem(new ItemStack(Material.DISPENSER, 2))
                    .addItem(new ItemStack(Material.FURNACE, 2))
                    .addItem(new ItemStack(Material.DROPPER, 1))
                    .addItem(new ItemStack(Material.REDSTONE, 20))
                    .addItem(new ItemStack(Material.REDSTONE_TORCH, 10))
                    .addItem(new ItemStack(Material.REPEATER, 5))
                    .addItem(new ItemStack(Material.COMPARATOR, 3))
                    .completeTime(180000)
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
            ),
            // Perspectives
            Collections.singletonList(
                new ThirdPersonPerspective(new RelativePos(0, 0, -5))
            ),
            // Max Health
            150,
            // Enableable
            SVCraftVehicles.getInstance(),
            // Recipe
            recipe()
                .displayItem(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/simple_car"))
                .addStep(step()
                    .name(Component.text("Body"))
                    .addItem(new ItemStack(Material.IRON_BLOCK, 2))
                    .addItem(new ItemStack(Material.IRON_INGOT, 50))
                    .addItem(new ItemStack(Material.COPPER_INGOT, 16))
                    .addItem(new ItemStack(Material.GLASS_PANE, 6))
                    .completeTime(300000)
                )
                .addStep(step()
                    .name(Component.text("Wheels"))
                    .addItem(new ItemStack(Material.BLACK_WOOL, 4))
                    .addItem(new ItemStack(Material.IRON_INGOT, 4))
                    .completeTime(60000)
                )
                .addStep(step()
                    .name(Component.text("Engine"))
                    .addItem(new ItemStack(Material.PISTON, 2))
                    .addItem(new ItemStack(Material.FURNACE, 1))
                    .addItem(new ItemStack(Material.REDSTONE, 10))
                    .addItem(new ItemStack(Material.REDSTONE_TORCH, 2))
                    .addItem(new ItemStack(Material.REPEATER, 5))
                    .completeTime(180000)
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
            new AABBCollision(1, 1.5),
            // Seat
            new Seat(new RelativePos(-0.1, 1.3, -0.8)),
            null,
            // Perspectives
            Collections.singletonList(
                new ThirdPersonPerspective(new RelativePos(0, 0, -5))
            ),
            // Max Health
            50,
            // Enableable
            SVCraftVehicles.getInstance(),
            // Recipe
            recipe()
                .displayItem(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/motorcycle"))
                .addStep(step()
                    .name(Component.text("Body"))
                    .addItem(new ItemStack(Material.IRON_INGOT, 36))
                    .addItem(new ItemStack(Material.COPPER_INGOT, 12))
                    .completeTime(120000)
                )
                .addStep(step()
                    .name(Component.text("Wheels"))
                    .addItem(new ItemStack(Material.BLACK_WOOL, 2))
                    .completeTime(20000)
                )
                .addStep(step()
                    .name(Component.text("Engine"))
                    .addItem(new ItemStack(Material.FURNACE, 1))
                    .addItem(new ItemStack(Material.REDSTONE, 5))
                    .addItem(new ItemStack(Material.REDSTONE_TORCH, 2))
                    .completeTime(60000)
                )
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
            // Perspectives
            Collections.singletonList(
                new ThirdPersonPerspective(new RelativePos(0, 0, -12))
            ),
            // Max Health
            200,
            // Enableable
            SVCraftVehicles.getInstance(),
            // Recipe
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
            // Perspectives
            Collections.singletonList(
                new ThirdPersonPerspective(new RelativePos(0, 0, -10))
            ),
            // Max Health
            80,
            // Enableable
            SVCraftVehicles.getInstance(),
            // Recipe
            recipe()
                .displayItem(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/wooden_plane"))
                .addStep(step()
                    .name(Component.text("Body"))
                    .addItem(new ItemStack(Material.OAK_BOAT, 1))
                    .addItem(new ItemStack(Material.OAK_PLANKS, 10))
                    .addItem(new ItemStack(Material.OAK_SLAB, 20))
                    .addItem(new ItemStack(Material.OAK_FENCE, 4))
                    .completeTime(120000)
                )
                .addStep(step()
                    .name(Component.text("Wheels"))
                    .addItem(new ItemStack(Material.BLACK_WOOL, 3))
                    .addItem(new ItemStack(Material.STICK, 3))
                    .completeTime(30000)
                )
                .addStep(step()
                    .name(Component.text("Propeller"))
                    .addItem(new ItemStack(Material.STICK, 5))
                    .completeTime(30000)
                )
                .addStep(step()
                    .name(Component.text("Engine"))
                    .addItem(new ItemStack(Material.PISTON, 2))
                    .addItem(new ItemStack(Material.FURNACE, 1))
                    .addItem(new ItemStack(Material.REDSTONE, 10))
                    .addItem(new ItemStack(Material.REDSTONE_TORCH, 2))
                    .addItem(new ItemStack(Material.REPEATER, 5))
                    .completeTime(60000)
                )
        );

        // Mini Submarine
        MINI_SUBMARINE = new VehicleType(
            "mini_submarine",
            Component.text("Mini Submarine"),
            MiniSubmarineVehicle.class,
            MiniSubmarineVehicle::new,
            MiniSubmarineVehicle::new,
            // Collision
            new AABBCollision(1.5, 2),
            // Seats
            new Seat(new RelativePos(0, 0, 0)),
            null,
            // Perspectives
            Collections.singletonList(
                new ThirdPersonPerspective(new RelativePos(0, 0, -5))
            ),
            // Max Health
            50,
            // Enableable
            SVCraftVehicles.getInstance(),
            // Recipe
            recipe()
                .displayItem(SVCraftVehicles.getInstance().getModelDB().generateItem("svcraftvehicles:vehicle/mini_submarine"))
                .addStep(step()
                    .name(Component.text("Body"))
                    .addItem(new ItemStack(Material.IRON_BLOCK, 4))
                    .addItem(new ItemStack(Material.IRON_INGOT, 10))
                    .addItem(new ItemStack(Material.PRISMARINE, 5))
                    .completeTime(60000)
                )
                .addStep(step()
                    .name(Component.text("Glass"))
                    .addItem(new ItemStack(Material.GLASS_PANE, 6))
                    .addItem(new ItemStack(Material.GLASS, 2))
                    .completeTime(120000)
                )
                .addStep(step()
                    .name(Component.text("Engine"))
                    .addItem(new ItemStack(Material.PISTON))
                    .addItem(new ItemStack(Material.DISPENSER))
                    .addItem(new ItemStack(Material.REDSTONE, 10))
                    .addItem(new ItemStack(Material.REPEATER, 2))
                    .completeTime(120000)
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
        registry.registerVehicle(WOODEN_PLANE);
        registry.registerVehicle(MINI_SUBMARINE);
    }
}
