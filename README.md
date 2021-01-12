# Vehicles
Vehicles plugin.

## Registering a new vehicle type
1. Create the vehicle type in VehicleTypes.
    1. Create the field.
    2. Initiate the field in &lt;clinit&gt; (static block).
    3. Register vehicle in the static register method by adding another call to registerVehicle with the new field.
2. Create the vehicle class and extend the desired class (GroundVehicle, HelicopterVehicle, etc.).
3. Implement all abstract methods and create __both__ constructors matching super.
4. Make the getType method return your created VehicleType.
5. Done.

These instructions are for creating a new vehicle inside this plugin. If a new vehicle type is registered from a different plugin that plugin should store the vehicle types somewhere staticly much like how VehicleTypes is done. Create a register method and call that method in your onEnable with `SVCraftVehicles.getInstance().getRegistry()` as the registry.