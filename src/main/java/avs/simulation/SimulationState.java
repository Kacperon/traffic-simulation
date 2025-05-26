package avs.simulation;

import java.util.*;

public class SimulationState {
    private Map<TrafficLight.Direction, TrafficLight.LightState> lightStates;
    private Map<TrafficLight.Direction, List<String>> vehicleQueues;
    private List<String> lastCrossedVehicles;

    public SimulationState() {
        lightStates = new HashMap<>();
        vehicleQueues = new HashMap<>();
        lastCrossedVehicles = new ArrayList<>();

        // Initialize with default values
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            lightStates.put(dir, TrafficLight.LightState.RED);
            vehicleQueues.put(dir, new ArrayList<>());
        }
    }

    public void setLightState(TrafficLight.Direction direction, TrafficLight.LightState state) {
        lightStates.put(direction, state);
    }

    public void setVehicleQueue(TrafficLight.Direction direction, List<String> vehicles) {
        vehicleQueues.put(direction, vehicles);
    }

    public void setLastCrossedVehicles(List<String> vehicles) {
        lastCrossedVehicles = vehicles;
    }

    public TrafficLight.LightState getLightState(TrafficLight.Direction direction) {
        return lightStates.get(direction);
    }

    public List<String> getVehicleQueue(TrafficLight.Direction direction) {
        return vehicleQueues.get(direction);
    }

    public List<String> getLastCrossedVehicles() {
        return lastCrossedVehicles;
    }
}