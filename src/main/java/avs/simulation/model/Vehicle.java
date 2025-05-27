package avs.simulation.model;

import avs.simulation.UI.utils.DirectionUtils;
import avs.simulation.model.LightControlers.TrafficLight;

/**
 * Reprezentuje pojazd w symulacji
 */
public class Vehicle {
    
    private String vehicleId;
    private TrafficLight.Direction startRoad;
    private TrafficLight.Direction endRoad;
    private VehicleState state;
    private int waitingTime;
    
    public enum VehicleState {
        WAITING,
        CROSSING,
        COMPLETED
    }
    
    public enum MovementType {
        STRAIGHT, LEFT, RIGHT
    }
    
    public Vehicle(String vehicleId, TrafficLight.Direction startRoad, TrafficLight.Direction endRoad) {
        this.vehicleId = vehicleId;
        this.startRoad = startRoad;
        this.endRoad = endRoad;
        this.state = VehicleState.WAITING;
        this.waitingTime = 0;
    }

    public void update() {
        if (state == VehicleState.WAITING) {
            waitingTime++;
        }
    }
    
    public void startCrossing() {
        if (state == VehicleState.WAITING) {
            state = VehicleState.CROSSING;
        }
    }

    public String getVehicleId() {
        return vehicleId;
    }
    
    public TrafficLight.Direction getStartRoad() {
        return startRoad;
    }
    
    public TrafficLight.Direction getEndRoad() {
        return endRoad;
    }
    
    @Override
    public String toString() {
        return String.format("Vehicle[%s: %s->%s, %s, waited: %ds]", 
                vehicleId, startRoad, endRoad, state, waitingTime);
    }

    /**
     * Get the movement type (LEFT, STRAIGHT, RIGHT) based on start and end directions
     * @return The movement type
     */
    public MovementType getMovementType() {
        // Use DirectionUtils instead of duplicating the logic
        return DirectionUtils.getMovementType(startRoad, endRoad);
    }
}