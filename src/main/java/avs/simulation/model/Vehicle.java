package avs.simulation.model;

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
    public enum MovementType {
        STRAIGHT, LEFT, RIGHT
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

    public MovementType getMovementType() {
        int diff = (endRoad.ordinal() - startRoad.ordinal() + 4) % 4;
        switch (diff) {
            case 1: return MovementType.LEFT;
            case 2: return MovementType.STRAIGHT;
            case 3: return MovementType.RIGHT;
            default: return MovementType.STRAIGHT;
        }
    }
}