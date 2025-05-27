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
        TrafficLight.Direction opposingDirection = getOpposingDirection(currentGreenDirection);
        
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            if (dir == currentGreenDirection || dir == opposingDirection) {
                trafficLights.get(dir).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
            } else {
                trafficLights.get(dir).setState(TrafficLight.LightState.RED, RED_DURATION);
            }
        }
    }
    
    @Override
    public void updateLightStates() {
        // Update all traffic lights
        for (TrafficLight light : trafficLights.values()) {
            light.update();
        }
        
        TrafficLight currentLight = trafficLights.get(currentGreenDirection);
        TrafficLight opposingLight = trafficLights.get(getOpposingDirection(currentGreenDirection));
        
        if (currentLight.isStateFinished()) {
            if (currentLight.getCurrentState() == TrafficLight.LightState.GREEN) {
                // Set both current and opposing direction to YELLOW
                currentLight.setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
                opposingLight.setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
            } else if (currentLight.getCurrentState() == TrafficLight.LightState.YELLOW) {
                // Set both to RED
                currentLight.setState(TrafficLight.LightState.RED, RED_DURATION);
                opposingLight.setState(TrafficLight.LightState.RED, RED_DURATION);
                
                // Choose next direction based on queue lengths
                TrafficLight.Direction nextDirection = findPriorityDirection();
                TrafficLight.Direction opposingNextDirection = getOpposingDirection(nextDirection);
                
                // Set both next directions to GREEN
                trafficLights.get(nextDirection).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
                trafficLights.get(opposingNextDirection).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
                
                currentGreenDirection = nextDirection;
                
                // Reset queue length for both directions that just got green
                queueLengths.put(nextDirection, 0);
                queueLengths.put(opposingNextDirection, 0);
            }
        }
    }
    
    /**
     * Gets the opposing direction (NORTH↔SOUTH, EAST↔WEST)
     */
    private TrafficLight.Direction getOpposingDirection(TrafficLight.Direction direction) {
        return switch (direction) {
            case NORTH -> TrafficLight.Direction.SOUTH;
            case SOUTH -> TrafficLight.Direction.NORTH;
            case EAST -> TrafficLight.Direction.WEST;
            case WEST -> TrafficLight.Direction.EAST;
        };
    }
    
    private TrafficLight.Direction findPriorityDirection() {
        // Only consider NORTH and EAST as the primary directions
        TrafficLight.Direction[] primaryDirections = {TrafficLight.Direction.NORTH, TrafficLight.Direction.EAST};
        
        // Get current primary axis (NORTH/SOUTH or EAST/WEST)
        boolean isNorthSouthCurrent = (currentGreenDirection == TrafficLight.Direction.NORTH || 
                                      currentGreenDirection == TrafficLight.Direction.SOUTH);
        
        // The candidate is the opposite axis
        TrafficLight.Direction candidateDir = isNorthSouthCurrent ? 
                                             TrafficLight.Direction.EAST : 
                                             TrafficLight.Direction.NORTH;
        
        // Sum queue lengths for both directions on the candidate axis
        int candidateSum = queueLengths.get(candidateDir) + 
                          queueLengths.get(getOpposingDirection(candidateDir));
        
        // Only switch if queue length exceeds threshold
        if (candidateSum >= MIN_VEHICLES_FOR_PRIORITY) {
            return candidateDir;
        }
        
        // Otherwise toggle between NORTH and EAST
        return isNorthSouthCurrent ? TrafficLight.Direction.EAST : TrafficLight.Direction.NORTH;
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