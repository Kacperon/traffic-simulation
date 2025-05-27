package avs.simulation.model.LightControlers;

import java.util.Map;

/**
 * Abstract base class for all traffic light controllers.
 * This allows implementing different traffic light patterns and algorithms.
 */
public abstract class AbstractTrafficLightController {
    
    protected Map<TrafficLight.Direction, TrafficLight> trafficLights;
    
    /**
     * Constructor that takes traffic lights to control
     */
    public AbstractTrafficLightController(Map<TrafficLight.Direction, TrafficLight> trafficLights) {
        this.trafficLights = trafficLights;
        initializeLights();
    }
    
    /**
     * Initialize the starting pattern of traffic lights
     */
    protected abstract void initializeLights();
    
    /**
     * Update the state of all traffic lights
     */
    public abstract void updateLightStates();
    
    /**
     * Get the current direction with a green light
     * @return The direction with a green light, or null if no direction has a green light
     */
    public abstract TrafficLight.Direction getCurrentGreenDirection();
    
    /**
     * Enhanced method to check if a vehicle can cross, considering its destination.
     * This allows proper handling of left turns.
     * 
     * @param fromDirection The direction the vehicle is coming from
     * @param toDirection The direction the vehicle is going to
     * @return True if the vehicle can cross, false otherwise
     */
    public boolean canVehicleCross(TrafficLight.Direction fromDirection,
                                  TrafficLight.Direction toDirection) {
        // Default implementation just uses the basic check
        return canVehicleCross(fromDirection);
    }
    
    /**
     * Check if a vehicle from a specific direction can cross
     * @param fromDirection The direction the vehicle is coming from
     * @return True if the vehicle can cross, false otherwise
     */
    public boolean canVehicleCross(TrafficLight.Direction fromDirection) {
        TrafficLight light = trafficLights.get(fromDirection);
        if (light == null) {
            return false;
        }
        
        // Default implementation considers both GREEN and YELLOW as crossable
        TrafficLight.LightState state = light.getCurrentState();
        return state == TrafficLight.LightState.GREEN || state == TrafficLight.LightState.YELLOW;
    }
}