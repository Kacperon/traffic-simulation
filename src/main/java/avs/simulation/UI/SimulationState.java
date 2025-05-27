package avs.simulation.UI;

import avs.simulation.model.LightControlers.TrafficLight;
import avs.simulation.model.Vehicle;
import avs.simulation.UI.utils.DirectionUtils;

import java.util.*;

public class SimulationState {
    private Map<TrafficLight.Direction, TrafficLight.LightState> lightStates;
    private Map<TrafficLight.Direction, List<QueuedVehicle>> vehicleQueues;
    private List<String> lastCrossedVehicles;
    private List<CrossingVehicle> crossingVehicles;

    public SimulationState() {
        lightStates = new HashMap<>();
        vehicleQueues = new HashMap<>();
        lastCrossedVehicles = new ArrayList<>();
        crossingVehicles = new ArrayList<>();

        // Initialize with default values
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            lightStates.put(dir, TrafficLight.LightState.RED);
            vehicleQueues.put(dir, new ArrayList<>());
        }
    }

    public void setLightState(TrafficLight.Direction direction, TrafficLight.LightState state) {
        lightStates.put(direction, state);
    }

    public void setVehicleQueue(TrafficLight.Direction direction, List<QueuedVehicle> vehicles) {
        vehicleQueues.put(direction, new ArrayList<>(vehicles));
    }

    public void setLastCrossedVehicles(List<String> vehicles) {
        lastCrossedVehicles = vehicles;
    }

    public TrafficLight.LightState getLightState(TrafficLight.Direction direction) {
        return lightStates.get(direction);
    }

    public List<QueuedVehicle> getVehicleQueue(TrafficLight.Direction direction) {
        return vehicleQueues.getOrDefault(direction, new ArrayList<>());
    }

    public List<String> getLastCrossedVehicles() {
        return lastCrossedVehicles;
    }

    public List<CrossingVehicle> getCrossingVehicles() {
        return crossingVehicles;
    }

    public void addCrossingVehicle(String id, TrafficLight.Direction from, TrafficLight.Direction to) {
        crossingVehicles.add(new CrossingVehicle(id, from, to, 0));
    }


    public void removeVehicleFromAnimation(String id) {
        Iterator<CrossingVehicle> iterator = crossingVehicles.iterator();
        while (iterator.hasNext()) {
            CrossingVehicle vehicle = iterator.next();
            if (vehicle.getId().equals(id)) {
                iterator.remove();
                System.out.println("Removed: " + id);
                break;
            }
        }
    }

    public static class CrossingVehicle {
        private final String id;
        private TrafficLight.Direction fromDirection;
        private TrafficLight.Direction toDirection;
        private int animationStep;

        public CrossingVehicle(String id, TrafficLight.Direction fromDirection, TrafficLight.Direction toDirection, int animationStep) {
            this.id = id;
            this.fromDirection = fromDirection;
            this.toDirection = toDirection;
            this.animationStep = animationStep;
        }

        public String getId() {
            return id;
        }

        public TrafficLight.Direction getFromDirection() {
            return fromDirection;
        }

        public TrafficLight.Direction getToDirection() {
            return toDirection;
        }

        public int getAnimationStep() {
            return animationStep;
        }

        public void setAnimationStep(int animationStep) {
            this.animationStep = animationStep;
        }

        // Add this method to the CrossingVehicle class
        public Vehicle.MovementType getMovementType() {
            return DirectionUtils.getMovementType(fromDirection, toDirection);
        }
    }

    // Add this new class to represent queued vehicles with their directions
    public static class QueuedVehicle {
        private String id;
        private TrafficLight.Direction startRoad;
        private TrafficLight.Direction endRoad;

        public QueuedVehicle(String id, TrafficLight.Direction startRoad, TrafficLight.Direction endRoad) {
            this.id = id;
            this.startRoad = startRoad;
            this.endRoad = endRoad;
        }

        public String getId() { return id; }
        public TrafficLight.Direction getStartRoad() { return startRoad; }
        public TrafficLight.Direction getEndRoad() { return endRoad; }

        // Helper method to determine movement type
        public Vehicle.MovementType getMovementType() {
            // Use the utility method instead of duplicating logic
            return DirectionUtils.getMovementType(startRoad, endRoad);
        }
    }
}