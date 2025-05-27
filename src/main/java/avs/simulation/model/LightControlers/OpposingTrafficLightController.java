package avs.simulation.model.LightControlers;

import avs.simulation.model.Vehicle;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Priority-based traffic light controller that gives green light to directions
 * with more waiting vehicles
 */
public class OpposingTrafficLightController extends AbstractTrafficLightController {
    private static final int GREEN_DURATION = 4;
    private static final int YELLOW_DURATION = 1;
    private static final int RED_DURATION = 1;
    private static final int RED_YELLOW_DURATION = 1;
    
    private TrafficLight.Direction currentGreenDirection;
    private Map<TrafficLight.Direction, Integer> queueLengths;
    private enum Phase { GREEN, YELLOW, RED, RED_YELLOW }
    private Phase currentPhase = Phase.GREEN;
    
    public OpposingTrafficLightController(Map<TrafficLight.Direction, TrafficLight> trafficLights) {
        super(trafficLights);
        queueLengths = new HashMap<>();
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            queueLengths.put(dir, 0);
        }
    }
    
    @Override
    protected void initializeLights() {
        this.currentGreenDirection = TrafficLight.Direction.NORTH;
        this.currentPhase = Phase.GREEN;
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
            switch (currentPhase) {
                case GREEN:
                    // Set both current and opposing direction to YELLOW
                    currentLight.setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
                    opposingLight.setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
                    currentPhase = Phase.YELLOW;
                    break;
                    
                case YELLOW:
                    // Set both to RED
                    currentLight.setState(TrafficLight.LightState.RED, RED_DURATION);
                    opposingLight.setState(TrafficLight.LightState.RED, RED_DURATION);
                    currentPhase = Phase.RED;
                    break;
                    
                case RED:
                    // Choose next direction based on queue lengths
                    TrafficLight.Direction nextDirection = findPriorityDirection();
                    TrafficLight.Direction opposingNextDirection = getOpposingDirection(nextDirection);
                    
                    // Set both next directions to RED_YELLOW before GREEN
                    trafficLights.get(nextDirection).setState(TrafficLight.LightState.RED_YELLOW, RED_YELLOW_DURATION);
                    trafficLights.get(opposingNextDirection).setState(TrafficLight.LightState.RED_YELLOW, RED_YELLOW_DURATION);
                    
                    currentGreenDirection = nextDirection;
                    currentPhase = Phase.RED_YELLOW;
                    break;
                    
                case RED_YELLOW:
                    // Now transition to GREEN
                    TrafficLight.Direction opposingDirection = getOpposingDirection(currentGreenDirection);
                    trafficLights.get(currentGreenDirection).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
                    trafficLights.get(opposingDirection).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
                    
                    // Reset queue length for both directions that just got green
                    queueLengths.put(currentGreenDirection, 0);
                    queueLengths.put(opposingDirection, 0);
                    
                    currentPhase = Phase.GREEN;
                    break;
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
        boolean isNorthSouthCurrent = (currentGreenDirection == TrafficLight.Direction.NORTH || 
                                      currentGreenDirection == TrafficLight.Direction.SOUTH);
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

    public boolean canLeftTurn(TrafficLight.Direction fromDirection) {
        TrafficLight light = trafficLights.get(fromDirection);
        if (light == null) {
            return false;
        }
        TrafficLight.LightState state = light.getCurrentState();
        return state == TrafficLight.LightState.YELLOW;
    }

    public boolean canRightStraightCross(TrafficLight.Direction fromDirection) {
        TrafficLight light = trafficLights.get(fromDirection);
        if (light == null) {
            return false;
        }
        TrafficLight.LightState state = light.getCurrentState();
        return state == TrafficLight.LightState.GREEN;
    }
}
