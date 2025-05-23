package avs.simulation;

import java.util.ArrayList;
import java.util.List;

public class StepStatus {
    private List<String> leftVehicles;

    public StepStatus() {
        this.leftVehicles = new ArrayList<>();
    }

    public void addLeftVehicle(String vehicleId) {
        leftVehicles.add(vehicleId);
    }

    public List<String> getLeftVehicles() {
        return leftVehicles;
    }
}