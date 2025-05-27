package avs.simulation.model.LightControlers;

import avs.simulation.model.TrafficLight;
import java.util.Map;

/**
 * Traffic light controller that cycles through directions in a fixed pattern
 * with proper RED_YELLOW transition phase before GREEN
 */
public class StandardTrafficLightController extends AbstractTrafficLightController {
    private static final int GREEN_DURATION = 4;
    private static final int YELLOW_DURATION = 1;
    private static final int RED_YELLOW_DURATION = 1;
    
    private TrafficLight.Direction currentGreenDirection;
    private int timeRemaining;
    private enum Phase { GREEN, YELLOW, RED_YELLOW }
    private Phase currentPhase = Phase.GREEN;
    
    public StandardTrafficLightController(Map<TrafficLight.Direction, TrafficLight> trafficLights) {
        super(trafficLights);
    }
    
    @Override
    protected void initializeLights() {
        // Start with North direction green
        this.currentGreenDirection = TrafficLight.Direction.NORTH;
        this.timeRemaining = GREEN_DURATION;
        this.currentPhase = Phase.GREEN;
        
        // Set initial states
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            if (direction == TrafficLight.Direction.NORTH) {
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
                    // Change from green to yellow
                    trafficLights.get(currentGreenDirection).setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
                    timeRemaining = YELLOW_DURATION;
                    currentPhase = Phase.YELLOW;
                    break;
                    
                case YELLOW:
                    // Change from yellow to red
                    trafficLights.get(currentGreenDirection).setState(TrafficLight.LightState.RED, 
                            GREEN_DURATION + YELLOW_DURATION);
                    
                    // Get next direction and prepare for RED_YELLOW
                    currentGreenDirection = getNextDirection();
                    
                    // Set RED_YELLOW for the next direction
                    trafficLights.get(currentGreenDirection).setState(TrafficLight.LightState.RED_YELLOW, RED_YELLOW_DURATION);
                    
                    timeRemaining = RED_YELLOW_DURATION;
                    currentPhase = Phase.RED_YELLOW;
                    break;
                    
                case RED_YELLOW:
                    // Change from red-yellow to green
                    trafficLights.get(currentGreenDirection).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
                    
                    // Reset timings for new green phase
                    timeRemaining = GREEN_DURATION;
                    currentPhase = Phase.GREEN;
                    break;
            }
        }
    }
    
    private TrafficLight.Direction getNextDirection() {
        // Simple clockwise rotation: NORTH → EAST → SOUTH → WEST → NORTH
        switch (currentGreenDirection) {
            case NORTH: return TrafficLight.Direction.EAST;
            case EAST: return TrafficLight.Direction.SOUTH;
            case SOUTH: return TrafficLight.Direction.WEST;
            case WEST: return TrafficLight.Direction.NORTH;
            default: return TrafficLight.Direction.NORTH; // Fallback
        }
    }
    
    @Override
    public TrafficLight.Direction getCurrentGreenDirection() {
        return currentGreenDirection;
    }
}