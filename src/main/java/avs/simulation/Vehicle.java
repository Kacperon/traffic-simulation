package avs.simulation;

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
        WAITING,      // Czeka na zielone światło
        CROSSING,     // Przejeżdża przez skrzyżowanie
        COMPLETED     // Ukończył przejazd
    }
    
    public Vehicle(String vehicleId, TrafficLight.Direction startRoad, TrafficLight.Direction endRoad) {
        this.vehicleId = vehicleId;
        this.startRoad = startRoad;
        this.endRoad = endRoad;
        this.state = VehicleState.WAITING;
        this.waitingTime = 0;
    }
    
    /**
     * Aktualizuje stan pojazdu o jeden krok czasowy
     */
    public void update() {
        if (state == VehicleState.WAITING) {
            waitingTime++;
        }
    }
    
    /**
     * Rozpoczyna przejazd przez skrzyżowanie
     */
    public void startCrossing() {
        if (state == VehicleState.WAITING) {
            state = VehicleState.CROSSING;
        }
    }
    
    /**
     * Kończy przejazd przez skrzyżowanie
     */
    public void completeCrossing() {
        if (state == VehicleState.CROSSING) {
            state = VehicleState.COMPLETED;
        }
    }
    
    /**
     * Sprawdza czy pojazd może przejechać przez skrzyżowanie
     * na podstawie kierunku ruchu
     */
    public boolean canCross(TrafficLight.Direction currentGreenDirection) {
        if (state != VehicleState.WAITING) {
            return false;
        }
        
        // Sprawdza czy kierunek startowy ma zielone światło
        return startRoad == currentGreenDirection;
    }
    
    /**
     * Określa typ ruchu pojazdu (prosto, w lewo, w prawo)
     */
    public MovementType getMovementType() {
        if (startRoad == endRoad) {
            return MovementType.STRAIGHT; // Teoretycznie niemożliwe, ale dla kompletności
        }
        
        // Logika określania typu ruchu na podstawie kierunków
        switch (startRoad) {
            case NORTH:
                if (endRoad == TrafficLight.Direction.SOUTH) return MovementType.STRAIGHT;
                if (endRoad == TrafficLight.Direction.EAST) return MovementType.RIGHT;
                if (endRoad == TrafficLight.Direction.WEST) return MovementType.LEFT;
                break;
            case SOUTH:
                if (endRoad == TrafficLight.Direction.NORTH) return MovementType.STRAIGHT;
                if (endRoad == TrafficLight.Direction.WEST) return MovementType.RIGHT;
                if (endRoad == TrafficLight.Direction.EAST) return MovementType.LEFT;
                break;
            case EAST:
                if (endRoad == TrafficLight.Direction.WEST) return MovementType.STRAIGHT;
                if (endRoad == TrafficLight.Direction.NORTH) return MovementType.RIGHT;
                if (endRoad == TrafficLight.Direction.SOUTH) return MovementType.LEFT;
                break;
            case WEST:
                if (endRoad == TrafficLight.Direction.EAST) return MovementType.STRAIGHT;
                if (endRoad == TrafficLight.Direction.SOUTH) return MovementType.RIGHT;
                if (endRoad == TrafficLight.Direction.NORTH) return MovementType.LEFT;
                break;
        }
        
        return MovementType.STRAIGHT; // domyślnie
    }
    
    public enum MovementType {
        STRAIGHT, LEFT, RIGHT
    }
    
    // Gettery i settery
    public String getVehicleId() {
        return vehicleId;
    }
    
    public TrafficLight.Direction getStartRoad() {
        return startRoad;
    }
    
    public TrafficLight.Direction getEndRoad() {
        return endRoad;
    }
    
    public VehicleState getState() {
        return state;
    }
    
    public int getWaitingTime() {
        return waitingTime;
    }
    
    @Override
    public String toString() {
        return String.format("Vehicle[%s: %s->%s, %s, waited: %ds]", 
                vehicleId, startRoad, endRoad, state, waitingTime);
    }
}