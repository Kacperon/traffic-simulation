package avs.simulation.model;

import avs.simulation.model.LightControlers.*;

import java.util.*;

public class Intersection extends AbstractIntersection{
    private AbstractTrafficLightController controller;

    public Intersection() {
        this(ControllerType.STANDARD);
    }

    public Intersection(ControllerType controllerType) {
        trafficLights = new HashMap<>();
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            trafficLights.put(direction, new TrafficLight());
        }
        setControllerType(controllerType);
    }
    
    public enum ControllerType {
        STANDARD,
        PRIORITY,
        OPPOSING
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

    }


    public List<TrafficLight.Direction> getCurrentGreenDirections(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues) {
        List<TrafficLight.Direction> directions = new ArrayList<>();
        List<TrafficLight.Direction> leftTurningTrafic = new ArrayList<>();
        for (Map.Entry<TrafficLight.Direction, TrafficLight> entry : trafficLights.entrySet()) {
            TrafficLight.Direction dir = entry.getKey();
            Queue<Vehicle> queue = vehicleQueues.get(dir);
            
            // Skip empty queues
            if (queue == null || queue.isEmpty()) {
                continue;
            }

            if (controller.canVehicleCross(dir)) {
                // Peek at first vehicle without removing it yet
                Vehicle vehicle = queue.peek();
                
                boolean canCross = false;

                if (controller instanceof OpposingTrafficLightController) {
                    // Special rules for OpposingTrafficLightController
                    OpposingTrafficLightController otc = (OpposingTrafficLightController) controller;
                    
                    if (vehicle.getMovementType() == Vehicle.MovementType.RIGHT) {
                        // Left turns only on yellow
                        canCross = otc.canLeftTurn(dir);
                        leftTurningTrafic.add(dir);

                    } else {
                        // Straight and right turns on green
                        canCross = otc.canRightStraightCross(dir);
                    }
                } else {
                    // Default behavior for other controllers
                    canCross = true;
                }
                
                if (canCross) {
                    directions.add(dir);
                    // Don't remove the vehicle here - that happens in processVehicles
                }
            }
        }
        return !directions.isEmpty() ? directions : leftTurningTrafic;
    }
    
    @Override
    public void processVehicles(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues,
                               StepStatus stepStatus,
                               List<Vehicle> completedVehicles) {

        List<TrafficLight.Direction> crossableDirections = getCurrentGreenDirections(vehicleQueues);
        for (TrafficLight.Direction dir : crossableDirections) {

            Queue<Vehicle> queue = vehicleQueues.get(dir);
            if (queue == null || queue.isEmpty()) {
                continue;
            }
            Vehicle v = queue.poll();
            if (v != null) {
                v.startCrossing();
                completedVehicles.add(v);
                stepStatus.addLeftVehicle(v.getVehicleId());

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
            (type == ControllerType.PRIORITY && controller instanceof PriorityTrafficLightController)||
            (type == ControllerType.OPPOSING && controller instanceof OpposingTrafficLightController)) {
            return;
        }
        controller = switch (type) {
            case PRIORITY -> new PriorityTrafficLightController(trafficLights);
            case OPPOSING -> new OpposingTrafficLightController(trafficLights);
            default -> new StandardTrafficLightController(trafficLights);
        };
    }
}