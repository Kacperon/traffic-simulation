package avs.simulation.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StepStatusTest {

    @Test
    void addLeftVehicle() {
        StepStatus stepStatus = new StepStatus();
        stepStatus.addLeftVehicle("V123");

        List<String> leftVehicles = stepStatus.getLeftVehicles();
        assertEquals(1, leftVehicles.size(), "List should contain one vehicle");
        assertTrue(leftVehicles.contains("V123"), "Vehicle ID 'V123' should be in the list");
    }

    @Test
    void getLeftVehicles() {
        StepStatus stepStatus = new StepStatus();
        stepStatus.addLeftVehicle("V1");
        stepStatus.addLeftVehicle("V2");

        List<String> leftVehicles = stepStatus.getLeftVehicles();
        assertNotNull(leftVehicles, "Returned list should not be null");
        assertEquals(2, leftVehicles.size(), "List should contain two vehicles");
        assertEquals(List.of("V1", "V2"), leftVehicles, "List should match the added vehicles");
    }
}
