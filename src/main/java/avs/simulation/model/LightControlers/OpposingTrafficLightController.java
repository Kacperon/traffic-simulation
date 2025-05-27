package avs.simulation.model.LightControlers;

import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import avs.simulation.UI.utils.DirectionUtils;

/**
 * Traffic light controller that gives green to opposing directions simultaneously.
 * North-South directions are synchronized, and East-West directions are synchronized.
 */
public class OpposingTrafficLightController extends AbstractTrafficLightController {
    private static final int GREEN_DURATION = 4;
    private static final int YELLOW_DURATION = 1;
    private static final int RED_YELLOW_DURATION = 1;

    private TrafficLight.Direction currentGreenDirection;
    private TrafficLight.Direction opposingGreenDirection;
    private int timeRemaining;
    private enum Phase { GREEN, YELLOW, RED_YELLOW }
    private Phase currentPhase = Phase.GREEN;
    
    // Track which directions currently have green lights
    private Set<TrafficLight.Direction> activeDirections = new HashSet<>();

    public OpposingTrafficLightController(Map<TrafficLight.Direction, TrafficLight> trafficLights) {
        super(trafficLights);
    }
    
    @Override
    protected void initializeLights() {
        // Start with North-South pair
        this.currentGreenDirection = TrafficLight.Direction.NORTH;
        this.opposingGreenDirection = getOpposingDirection(this.currentGreenDirection);
        this.timeRemaining = GREEN_DURATION;
        this.currentPhase = Phase.GREEN;
        
        // Initialize active directions
        activeDirections = new HashSet<>();
        activeDirections.add(currentGreenDirection);
        activeDirections.add(opposingGreenDirection);
        
        System.out.println("Initializing lights: " + currentGreenDirection + " and " + opposingGreenDirection + " GREEN");
        
        // Set initial states
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            if (direction == currentGreenDirection || direction == opposingGreenDirection) {
                trafficLights.get(direction).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
            } else {
                trafficLights.get(direction).setState(TrafficLight.LightState.RED, 
                      GREEN_DURATION + YELLOW_DURATION + RED_YELLOW_DURATION);
            }
        }
    }
    
    @Override
    public void updateLightStates() {
        // Decrement time
        timeRemaining--;
        
        // Debug output 
        if (timeRemaining == 0) {
            System.out.println("Time expired, phase: " + currentPhase);
        }
        
        if (timeRemaining <= 0) {
            // Time to switch to next phase
            switch (currentPhase) {
                case GREEN:
                    // Change from green to yellow for both directions
                    trafficLights.get(currentGreenDirection).setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
                    trafficLights.get(opposingGreenDirection).setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
                    timeRemaining = YELLOW_DURATION;
                    currentPhase = Phase.YELLOW;
                    System.out.println("Switching to YELLOW for: " + currentGreenDirection + " and " + opposingGreenDirection);
                    break;
                    
                case YELLOW:
                    // Change from yellow to red for both directions
                    trafficLights.get(currentGreenDirection).setState(TrafficLight.LightState.RED, 
                            GREEN_DURATION + YELLOW_DURATION);
                    trafficLights.get(opposingGreenDirection).setState(TrafficLight.LightState.RED, 
                            GREEN_DURATION + YELLOW_DURATION);
                    
                    // Get next direction pair and prepare for RED_YELLOW
                    TrafficLight.Direction oldDirection = currentGreenDirection;
                    currentGreenDirection = getNextDirection();
                    opposingGreenDirection = getOpposingDirection(currentGreenDirection);
                    
                    System.out.println("Switching directions from " + oldDirection + 
                                      " to " + currentGreenDirection + 
                                      " (with opposing " + opposingGreenDirection + ")");
                    
                    // Update active directions
                    activeDirections.clear();
                    activeDirections.add(currentGreenDirection);
                    activeDirections.add(opposingGreenDirection);
                    
                    // Set RED_YELLOW for the next directions
                    trafficLights.get(currentGreenDirection).setState(TrafficLight.LightState.RED_YELLOW, RED_YELLOW_DURATION);
                    trafficLights.get(opposingGreenDirection).setState(TrafficLight.LightState.RED_YELLOW, RED_YELLOW_DURATION);
                    
                    timeRemaining = RED_YELLOW_DURATION;
                    currentPhase = Phase.RED_YELLOW;
                    break;
                    
                case RED_YELLOW:
                    // Change from red-yellow to green for both directions
                    trafficLights.get(currentGreenDirection).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
                    trafficLights.get(opposingGreenDirection).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
                    
                    System.out.println("Switching to GREEN for: " + currentGreenDirection + " and " + opposingGreenDirection);
                    
                    timeRemaining = GREEN_DURATION;
                    currentPhase = Phase.GREEN;
                    break;
            }
        }
    }
    
    private TrafficLight.Direction getNextDirection() {
        // In opposing traffic pattern we alternate between North/South and East/West pairs
        if (currentGreenDirection == TrafficLight.Direction.NORTH || 
            currentGreenDirection == TrafficLight.Direction.SOUTH) {
            return TrafficLight.Direction.EAST;
        } else {
            return TrafficLight.Direction.NORTH;
        }
    }
    
    private TrafficLight.Direction getOpposingDirection(TrafficLight.Direction direction) {
        return switch (direction) {
            case NORTH -> TrafficLight.Direction.SOUTH;
            case SOUTH -> TrafficLight.Direction.NORTH;
            case EAST -> TrafficLight.Direction.WEST;
            case WEST -> TrafficLight.Direction.EAST;
        };
    }
    
    @Override
    public boolean canVehicleCross(TrafficLight.Direction fromDirection) {
        // Check if the light is green or yellow
        TrafficLight light = trafficLights.get(fromDirection);
        if (light == null) {
            return false;
        }
        
        TrafficLight.LightState state = light.getCurrentState();
        return state == TrafficLight.LightState.GREEN || state == TrafficLight.LightState.YELLOW;
    }
    
    @Override
    public boolean canVehicleCross(TrafficLight.Direction fromDirection, 
                                  TrafficLight.Direction toDirection) {
        // Basic check if the light is green or yellow
        return canVehicleCross(fromDirection);
    }
    
    @Override
    public TrafficLight.Direction getCurrentGreenDirection() {
        return currentGreenDirection;
    }
    
    /**
     * Get the opposing direction that is also green
     */
    public TrafficLight.Direction getOpposingGreenDirection() {
        return opposingGreenDirection;
    }
}