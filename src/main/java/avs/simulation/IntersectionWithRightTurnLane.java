package avs.simulation;

import java.util.*;

public class IntersectionWithRightTurnLane extends AbstractIntersection {
    private TrafficLightController controller;
    private Map<TrafficLight.Direction, Queue<Vehicle>> rightTurnLanes;

    public IntersectionWithRightTurnLane() {
        // Initialize traffic lights for all directions
        trafficLights = new HashMap<>();
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            trafficLights.put(direction, new TrafficLight());
        }

        // Initialize right turn lanes
        rightTurnLanes = new HashMap<>();
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            rightTurnLanes.put(direction, new LinkedList<>());
        }

        // Initialize traffic light controller
        controller = new TrafficLightController(trafficLights);
    }

    @Override
    public TrafficLight getTrafficLight(TrafficLight.Direction direction) {
        return trafficLights.get(direction);
    }

    @Override
    public void update(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues) {
        // Extract right turning vehicles into separate queues
        separateRightTurningVehicles(vehicleQueues);

        // Update traffic light states
        controller.updateLightStates();

        // Adjust light cycles based on traffic intensity
        controller.adjustLightCycles(vehicleQueues);

        // Update timers for all traffic lights
        for (TrafficLight light : trafficLights.values()) {
            light.update();
        }
    }

    private void separateRightTurningVehicles(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues) {
        // Clear previous right turn lanes
        for (Queue<Vehicle> lane : rightTurnLanes.values()) {
            lane.clear();
        }

        // For each direction, identify vehicles that want to turn right
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            Queue<Vehicle> mainQueue = vehicleQueues.get(direction);
            Queue<Vehicle> filteredQueue = new LinkedList<>();

            for (Vehicle vehicle : mainQueue) {
                if (isRightTurn(vehicle.getStartRoad(), vehicle.getEndRoad())) {
                    rightTurnLanes.get(direction).add(vehicle);
                } else {
                    filteredQueue.add(vehicle);
                }
            }

            // Replace the main queue with filtered one (excluding right turns)
            mainQueue.clear();
            mainQueue.addAll(filteredQueue);
        }
    }

    private boolean isRightTurn(TrafficLight.Direction from, TrafficLight.Direction to) {
        // Check if the vehicle is making a right turn
        switch (from) {
            case NORTH: return to == TrafficLight.Direction.EAST;
            case EAST: return to == TrafficLight.Direction.SOUTH;
            case SOUTH: return to == TrafficLight.Direction.WEST;
            case WEST: return to == TrafficLight.Direction.NORTH;
            default: return false;
        }
    }

    @Override
    public List<TrafficLight.Direction> getCurrentGreenDirections() {
        List<TrafficLight.Direction> greenDirections = new ArrayList<>();
        for (Map.Entry<TrafficLight.Direction, TrafficLight> entry : trafficLights.entrySet()) {
            if (entry.getValue().getCurrentState() == TrafficLight.LightState.GREEN) {
                greenDirections.add(entry.getKey());
            }
        }
        return greenDirections;
    }

    @Override
    public void processVehicles(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues,
                                StepStatus stepStatus,
                                List<Vehicle> completedVehicles) {
        // First process right turning vehicles - they have priority and can always proceed
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            Queue<Vehicle> rightTurnQueue = rightTurnLanes.get(direction);

            // Process all right turning vehicles, up to 1 per step (to model slower traffic)
            if (!rightTurnQueue.isEmpty()) {
                Vehicle vehicle = rightTurnQueue.poll();
                vehicle.completeCrossing();
                completedVehicles.add(vehicle);
                stepStatus.addLeftVehicle(vehicle.getVehicleId());
            }
        }

        // Then process normal vehicles based on traffic light
        List<TrafficLight.Direction> greenDirections = getCurrentGreenDirections();

        for (TrafficLight.Direction greenDirection : greenDirections) {
            Queue<Vehicle> queue = vehicleQueues.get(greenDirection);

            // Process up to a certain number of vehicles per step
            int vehiclesToProcess = Math.min(queue.size(), 2); // max 2 vehicles per step

            for (int i = 0; i < vehiclesToProcess; i++) {
                if (!queue.isEmpty()) {
                    Vehicle vehicle = queue.poll();
                    vehicle.completeCrossing();
                    completedVehicles.add(vehicle);
                    stepStatus.addLeftVehicle(vehicle.getVehicleId());
                }
            }
        }
    }

    // TrafficLightController class similar to Intersection's
    private class TrafficLightController {
        // Similar implementation to Intersection's TrafficLightController
        private static final int GREEN_DURATION = 2;
        private static final int YELLOW_DURATION = 1;
        private static final int RED_DURATION = 1;
        private static final int RED_YELLOW_DURATION = 2;

        private Map<TrafficLight.Direction, TrafficLight> lights;
        private TrafficLight.Direction currentGreenDirection;

        public TrafficLightController(Map<TrafficLight.Direction, TrafficLight> lights) {
            this.lights = lights;
            this.currentGreenDirection = TrafficLight.Direction.NORTH;

            // Setup initial traffic light states
            lights.get(TrafficLight.Direction.NORTH).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
            lights.get(TrafficLight.Direction.SOUTH).setState(TrafficLight.LightState.RED, RED_DURATION);
            lights.get(TrafficLight.Direction.EAST).setState(TrafficLight.LightState.RED, RED_DURATION);
            lights.get(TrafficLight.Direction.WEST).setState(TrafficLight.LightState.RED, RED_DURATION);
        }

        public void updateLightStates() {
            TrafficLight currentLight = lights.get(currentGreenDirection);

            if (currentLight.isStateFinished()) {
                switchToNextPhase();
            }
        }

        private void switchToNextPhase() {
            // Same implementation as in Intersection
            // ...
        }

        public TrafficLight.Direction getCurrentGreenDirection() {
            for (Map.Entry<TrafficLight.Direction, TrafficLight> entry : lights.entrySet()) {
                if (entry.getValue().getCurrentState() == TrafficLight.LightState.GREEN) {
                    return entry.getKey();
                }
            }
            return null;
        }

        public void adjustLightCycles(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues) {
            // Same implementation as in Intersection
            // ...
        }
    }
}