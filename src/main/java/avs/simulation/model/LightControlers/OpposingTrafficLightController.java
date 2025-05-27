package avs.simulation.model.LightControlers;

import java.util.Map;

/**
 * Traffic light controller that gives green to opposing directions simultaneously.
 * Left-turning vehicles must wait until end of phase or can cross simultaneously
 * when both opposing vehicles want to turn left.
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
        
        if (timeRemaining <= 0) {
            // Time to switch to next phase
            switch (currentPhase) {
                case GREEN:
                    // Change from green to yellow for both directions
                    trafficLights.get(currentGreenDirection).setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
                    trafficLights.get(opposingGreenDirection).setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
                    timeRemaining = YELLOW_DURATION;
                    currentPhase = Phase.YELLOW;
                    break;
                    
                case YELLOW:
                    // Change from yellow to red for both directions
                    trafficLights.get(currentGreenDirection).setState(TrafficLight.LightState.RED, 
                            GREEN_DURATION + YELLOW_DURATION);
                    trafficLights.get(opposingGreenDirection).setState(TrafficLight.LightState.RED, 
                            GREEN_DURATION + YELLOW_DURATION);
                    
                    // Get next direction pair and prepare for RED_YELLOW
                    currentGreenDirection = getNextDirection();
                    opposingGreenDirection = getOpposingDirection(currentGreenDirection);
                    
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
                    
                    // Reset timings for new green phase
                    timeRemaining = GREEN_DURATION;
                    currentPhase = Phase.GREEN;
                    break;
            }
        }
    }
    
    private TrafficLight.Direction getNextDirection() {
        // In opposing traffic pattern we alternate between North/South and East/West
        if (currentGreenDirection == TrafficLight.Direction.NORTH || 
            currentGreenDirection == TrafficLight.Direction.SOUTH) {
            return TrafficLight.Direction.EAST;
        } else {
            return TrafficLight.Direction.NORTH;
        }
    }
    
    private TrafficLight.Direction getOpposingDirection(TrafficLight.Direction direction) {
        // Return the opposing direction
        return switch (direction) {
            case NORTH -> TrafficLight.Direction.SOUTH;
            case SOUTH -> TrafficLight.Direction.NORTH;
            case EAST -> TrafficLight.Direction.WEST;
            case WEST -> TrafficLight.Direction.EAST;
        };
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