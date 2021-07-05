# Vehicles
A plugin that adds vehicles.

**Note:** This plugin depends on a closed source core plugin that I use on my server so it cannot be compiled or ran, I might port a standalone version sometime though.

## Development help:
## Creating a new vehicle type
I use the instructions below to not forget anything when creating a new vehicle type.
1. Create the vehicle type in VehicleTypes.
    1. Create the field.
    2. Initiate the field in the static block.
    3. Register vehicle in the static register method by adding another call to registerVehicle with the new field.
2. Create the vehicle class and extend the desired class (GroundVehicle, HelicopterVehicle, etc.).
3. Implement all abstract methods and create __both__ constructors matching super.
4. Override the `init` method (but call super) and give the main entity the right model and spawn extra entities if applicable.
5. Make the getType method return your created VehicleType.
6. Add smoke offsets and override the spawnParticles method to render smoke at the right place when the vehicle is damaged (usually less than half of the health).
7. Done.

These instructions are for creating a new vehicle inside this plugin. If a new vehicle type is registered from a different plugin that plugin should store the vehicle types somewhere staticly much like how VehicleTypes is done. Create a register method and call that method in your onEnable with `SVCraftVehicles.getInstance().getRegistry()` as the registry.