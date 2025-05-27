package avs.simulation.model;

import avs.simulation.model.LightControlers.TrafficLight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static avs.simulation.model.Vehicle.VehicleState;

class VehicleTest {

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle("V1", TrafficLight.Direction.NORTH, TrafficLight.Direction.EAST);
    }

    @Test
    void update_shouldIncreaseWaitingTime_whenInWaitingState() {
        vehicle.update();
        vehicle.update();
        assertEquals("Vehicle[V1: NORTH->EAST, WAITING, waited: 2s]", vehicle.toString());
    }

    @Test
    void update_shouldNotIncreaseWaitingTime_whenCrossing() {
        vehicle.startCrossing();
        vehicle.update();
        assertEquals("Vehicle[V1: NORTH->EAST, CROSSING, waited: 0s]", vehicle.toString());
    }

    @Test
    void startCrossing_shouldChangeStateFromWaitingToCrossing() {
        vehicle.startCrossing();
        assertEquals(VehicleState.CROSSING, getState(vehicle));
    }

    @Test
    void startCrossing_shouldNotChangeState_whenNotWaiting() {
        vehicle.startCrossing();
        vehicle.startCrossing(); // second call should do nothing
        assertEquals(VehicleState.CROSSING, getState(vehicle));
    }

    @Test
    void getVehicleId_shouldReturnCorrectId() {
        assertEquals("V1", vehicle.getVehicleId());
    }

    @Test
    void getStartRoad_shouldReturnCorrectDirection() {
        assertEquals(TrafficLight.Direction.NORTH, vehicle.getStartRoad());
    }

    @Test
    void getEndRoad_shouldReturnCorrectDirection() {
        assertEquals(TrafficLight.Direction.EAST, vehicle.getEndRoad());
    }

    @Test
    void testToString_shouldContainVehicleInfo() {
        String output = vehicle.toString();
        assertTrue(output.contains("V1"));
        assertTrue(output.contains("NORTH->EAST"));
        assertTrue(output.contains("WAITING"));
        assertTrue(output.contains("waited: 0s"));
    }

    @Test
    void getMovementType_shouldReturnCorrectType() {

        assertEquals(Vehicle.MovementType.LEFT, vehicle.getMovementType());
    }

    private VehicleState getState(Vehicle vehicle) {
        try {
            var field = Vehicle.class.getDeclaredField("state");
            field.setAccessible(true);
            return (VehicleState) field.get(vehicle);
        } catch (Exception e) {
            fail("Reflection access failed: " + e.getMessage());
            return null;
        }
    }
}
