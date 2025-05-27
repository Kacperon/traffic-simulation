package avs.simulation;

import avs.simulation.model.LightControlers.TrafficLight;
import avs.simulation.model.Vehicle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VehicleTest {

    @Test
    public void testVehicleCreation() {
        Vehicle vehicle = new Vehicle("v1", TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH);
        assertEquals("v1", vehicle.getVehicleId());
        assertEquals(TrafficLight.Direction.NORTH, vehicle.getStartRoad());
        assertEquals(TrafficLight.Direction.SOUTH, vehicle.getEndRoad());
    }
    
    @Test
    public void testMovementType() {
        // Test straight movement
        Vehicle straightVehicle = new Vehicle("v1", TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH);
        assertEquals(Vehicle.MovementType.STRAIGHT, straightVehicle.getMovementType());
        
        // Test left turn
        Vehicle leftTurnVehicle = new Vehicle("v2", TrafficLight.Direction.NORTH, TrafficLight.Direction.EAST);
        assertEquals(Vehicle.MovementType.LEFT, leftTurnVehicle.getMovementType());
        
        // Test right turn
        Vehicle rightTurnVehicle = new Vehicle("v3", TrafficLight.Direction.NORTH, TrafficLight.Direction.WEST);
        assertEquals(Vehicle.MovementType.RIGHT, rightTurnVehicle.getMovementType());
    }
    
    @Test
    public void testEqualsAndHashCode() {
        Vehicle v1 = new Vehicle("v1", TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH);
        Vehicle v2 = new Vehicle("v1", TrafficLight.Direction.NORTH, TrafficLight.Direction.EAST);
        Vehicle v3 = new Vehicle("v2", TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH);
        
        // Same ID should make vehicles equal
        assertEquals(v1, v2);
        assertNotEquals(v1, v3);
        
        // HashCode should be based on ID
        assertEquals(v1.hashCode(), v2.hashCode());
        assertNotEquals(v1.hashCode(), v3.hashCode());
    }
}