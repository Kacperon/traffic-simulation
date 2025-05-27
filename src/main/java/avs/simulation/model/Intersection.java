package avs.simulation.model;

import java.util.*;

public class Intersection extends AbstractIntersection {
    private AbstractTrafficLightController controller;

    public Intersection() {
        this(ControllerType.STANDARD);
    }
    
    public Intersection(ControllerType controllerType) {
        trafficLights = new HashMap<>();
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            trafficLights.put(direction, new TrafficLight());
        }
        
        // Create the appropriate controller based on type
        switch (controllerType) {
            case PRIORITY:
                controller = new PriorityTrafficLightController(trafficLights);
                break;
            case STANDARD:
            default:
                controller = new StandardTrafficLightController(trafficLights);
                break;
        }
    }
    
    public enum ControllerType {
        STANDARD,
        PRIORITY
    }

    @Override
    public TrafficLight getTrafficLight(TrafficLight.Direction direction) {
        return trafficLights.get(direction);
    }

    @Override
    public void update(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues) {
        // If using priority controller, update queue lengths
        if (controller instanceof PriorityTrafficLightController) {
            ((PriorityTrafficLightController) controller).updateQueueLengths(vehicleQueues);
        }
        
        controller.updateLightStates();
        
        for (TrafficLight light : trafficLights.values()) {
            light.update();
        }
    }

    @Override
    public List<TrafficLight.Direction> getCurrentGreenDirections() {
        List<TrafficLight.Direction> directions = new ArrayList<>();

        for (Map.Entry<TrafficLight.Direction, TrafficLight> entry : trafficLights.entrySet()) {
            TrafficLight.Direction dir = entry.getKey();
            if (controller.canVehicleCross(dir)) {
                directions.add(dir);
            }
        }

        return directions;
    }
    
    @Override
    public void processVehicles(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues,
                               StepStatus stepStatus,
                               List<Vehicle> completedVehicles) {
        // Get all directions that allow crossing (green OR yellow)
        List<TrafficLight.Direction> crossableDirections = getCurrentGreenDirections();
        
        for (TrafficLight.Direction dir : crossableDirections) {
            Queue<Vehicle> queue = vehicleQueues.get(dir);
            if (queue == null || queue.isEmpty()) {
                continue;
            }
            
            // Process up to 2 vehicles per green/yellow light
            int vehiclesToProcess = Math.min(queue.size(), 2);
            
            for (int i = 0; i < vehiclesToProcess; i++) {
                Vehicle v = queue.poll();
                if (v != null) {
                    v.startCrossing();
                    completedVehicles.add(v);
                    stepStatus.addLeftVehicle(v.getVehicleId());
                }
            }
        }
    }

    public TrafficLight.Direction getCurrentGreenDirection() {
        return controller.getCurrentGreenDirection();
    }
    
    /**
     * Changes the traffic light controller type at runtime
     */
    public void setControllerType(ControllerType type) {
        if ((type == ControllerType.STANDARD && controller instanceof StandardTrafficLightController) ||
            (type == ControllerType.PRIORITY && controller instanceof PriorityTrafficLightController)) {
            // Already using this controller type
            return;
        }
        
        switch (type) {
            case PRIORITY:
                controller = new PriorityTrafficLightController(trafficLights);
                break;
            case STANDARD:
            default:
                controller = new StandardTrafficLightController(trafficLights);
                break;
        }
    }
}