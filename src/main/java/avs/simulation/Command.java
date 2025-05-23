package avs.simulation;

public class Command {
    public enum CommandType {
        ADD_VEHICLE,
        STEP
    }

    private CommandType type;
    private String vehicleId;
    private TrafficLight.Direction startRoad;
    private TrafficLight.Direction endRoad;

    // Konstruktor dla komendy ADD_VEHICLE
    public Command(CommandType type, String vehicleId, TrafficLight.Direction startRoad, TrafficLight.Direction endRoad) {
        this.type = type;
        this.vehicleId = vehicleId;
        this.startRoad = startRoad;
        this.endRoad = endRoad;
    }

    // Konstruktor dla komendy STEP
    public Command(CommandType type) {
        this.type = type;
    }

    public CommandType getType() {
        return type;
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
}