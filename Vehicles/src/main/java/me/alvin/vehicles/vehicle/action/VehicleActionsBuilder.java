package me.alvin.vehicles.vehicle.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VehicleActionsBuilder {
    private final List<VehicleAction> actions = new ArrayList<>();

    public VehicleActionsBuilder add(VehicleAction action) {
        this.actions.add(action);
        return this;
    }

    public VehicleActionsBuilder addAll(Collection<? extends  VehicleAction> actions) {
        this.actions.addAll(actions);
        return this;
    }

    public List<VehicleAction> build() {
        return this.actions;
    }
}
