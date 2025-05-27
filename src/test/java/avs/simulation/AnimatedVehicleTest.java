package avs.simulation;

import avs.simulation.UI.AnimatedVehicle;
import avs.simulation.model.LightControlers.TrafficLight;
import avs.simulation.model.Vehicle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class AnimatedVehicleTest {
    
    // Note: This test requires JavaFX runtime
    @Test
    public void testMidpointCallback() throws InterruptedException {
        AtomicBoolean midpointReached = new AtomicBoolean(false);
        AtomicBoolean finished = new AtomicBoolean(false);
        
        AnimatedVehicle vehicle = new AnimatedVehicle(
            "test", 
            TrafficLight.Direction.NORTH,
            TrafficLight.Direction.SOUTH,
            dir -> midpointReached.set(true),
            id -> finished.set(true)
        );
        
        // Wait for the animation to reach midpoint
        Thread.sleep(100);
        assertTrue(midpointReached.get(), "Midpoint callback should have been triggered");
        
        // Wait for animation to complete
        Thread.sleep(200);
        assertTrue(finished.get(), "Finished callback should have been triggered");
    }
    
    @Test
    public void testInitialPositionAndRotation() {
        AnimatedVehicle north = new AnimatedVehicle(
            "north", TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH,
            null, null
        );
        assertEquals(90, north.getRotation());
        
        AnimatedVehicle east = new AnimatedVehicle(
            "east", TrafficLight.Direction.EAST, TrafficLight.Direction.WEST,
            null, null
        );
        assertEquals(180, east.getRotation());
    }
    
    @Test
    public void testMovementType() {
        AnimatedVehicle straight = new AnimatedVehicle(
            "straight", TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH,
            null, null
        );
        assertEquals(Vehicle.MovementType.STRAIGHT, straight.getMovementType());
        
        AnimatedVehicle left = new AnimatedVehicle(
            "left", TrafficLight.Direction.NORTH, TrafficLight.Direction.EAST,
            null, null
        );
        assertEquals(Vehicle.MovementType.LEFT, left.getMovementType());
        
        AnimatedVehicle right = new AnimatedVehicle(
            "right", TrafficLight.Direction.NORTH, TrafficLight.Direction.WEST,
            null, null
        );
        assertEquals(Vehicle.MovementType.RIGHT, right.getMovementType());
    }
}