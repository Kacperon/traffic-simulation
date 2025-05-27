package avs.simulation.model.LightControlers;

import avs.simulation.model.Vehicle;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Priority-based traffic light controller that gives green light to directions
 * with more waiting vehicles
 */
public class PriorityTrafficLightController extends AbstractTrafficLightController {
    private static final int GREEN_DURATION = 4;
    private static final int YELLOW_DURATION = 1;
    private static final int RED_DURATION = 1;
    private static final int MIN_VEHICLES_FOR_PRIORITY = 3;
    
    private TrafficLight.Direction currentGreenDirection;
    private Map<TrafficLight.Direction, Integer> queueLengths;
    
    public PriorityTrafficLightController(Map<TrafficLight.Direction, TrafficLight> trafficLights) {
        super(trafficLights);
        queueLengths = new HashMap<>();
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            queueLengths.put(dir, 0);
        }
    }
    
    @Override
    protected void initializeLights() {
        this.currentGreenDirection = TrafficLight.Direction.NORTH;
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            if (dir == TrafficLight.Direction.NORTH) {
                trafficLights.get(dir).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
            } else {
                trafficLights.get(dir).setState(TrafficLight.LightState.RED, RED_DURATION);
            }
        }
    }
    
    @Override
    public void updateLightStates() {
        TrafficLight currentLight = trafficLights.get(currentGreenDirection);
        
        if (currentLight.isStateFinished()) {
            if (currentLight.getCurrentState() == TrafficLight.LightState.GREEN) {
                currentLight.setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
            } else if (currentLight.getCurrentState() == TrafficLight.LightState.YELLOW) {
                currentLight.setState(TrafficLight.LightState.RED, RED_DURATION);
                
                // Choose next direction based on queue lengths
                TrafficLight.Direction nextDirection = findPriorityDirection();
                trafficLights.get(nextDirection).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
                currentGreenDirection = nextDirection;
                
                // Reset queue length for the direction that just got green
                queueLengths.put(nextDirection, 0);
            }
        }
    }
    
    private TrafficLight.Direction findPriorityDirection() {
        TrafficLight.Direction priorityDir = currentGreenDirection;
        int maxVehicles = -1;
        
        for (Map.Entry<TrafficLight.Direction, Integer> entry : queueLengths.entrySet()) {
            TrafficLight.Direction dir = entry.getKey();
            int count = entry.getValue();
            
            if (dir != currentGreenDirection && count > maxVehicles) {
                maxVehicles = count;
                priorityDir = dir;
            }
        }
        
        // If priority queue has enough vehicles, choose it
        if (maxVehicles >= MIN_VEHICLES_FOR_PRIORITY) {
            return priorityDir;
        }
        
        // Otherwise use simple rotation
        return getNextDirection();
    }
    
    private TrafficLight.Direction getNextDirection() {
        TrafficLight.Direction[] directions = TrafficLight.Direction.values();
        int currentIndex = -1;
        
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == currentGreenDirection) {
                currentIndex = i;
                break;
            }
        }
        
        return directions[(currentIndex + 1) % directions.length];
    }
    
    @Override
    public TrafficLight.Direction getCurrentGreenDirection() {
        return currentGreenDirection;
    }
    
    /**
     * Update queue lengths from simulation data
     */
    public void updateQueueLengths(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues) {
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            Queue<Vehicle> queue = vehicleQueues.get(dir);
            if (queue != null) {
                queueLengths.put(dir, queue.size());
            }
        }
    }
}