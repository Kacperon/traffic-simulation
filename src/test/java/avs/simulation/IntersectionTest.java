package avs.simulation;

import avs.simulation.model.Intersection;
import avs.simulation.model.TrafficLight;
import avs.simulation.model.Vehicle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class IntersectionTest {

    @Test
    public void testInitialState() {
        Intersection intersection = new Intersection();
        assertEquals(TrafficLight.LightState.GREEN,
                     intersection.getTrafficLight(TrafficLight.Direction.NORTH).getCurrentState());
        
        assertEquals(TrafficLight.LightState.RED, 
                     intersection.getTrafficLight(TrafficLight.Direction.SOUTH).getCurrentState());
        
        assertEquals(TrafficLight.LightState.RED, 
                     intersection.getTrafficLight(TrafficLight.Direction.EAST).getCurrentState());
        
        assertEquals(TrafficLight.LightState.RED, 
                     intersection.getTrafficLight(TrafficLight.Direction.WEST).getCurrentState());
    }
    
    @Test
    public void testGetCurrentGreenDirection() {
        Intersection intersection = new Intersection();
        assertEquals(TrafficLight.Direction.NORTH, intersection.getCurrentGreenDirection());
    }
    
    @Test
    public void testGetCurrentGreenDirections() {
        Intersection intersection = new Intersection();
        List<TrafficLight.Direction> greenDirections = intersection.getCurrentGreenDirections();
        assertEquals(1, greenDirections.size());
        assertEquals(TrafficLight.Direction.NORTH, greenDirections.get(0));
    }
    
    @Test
    public void testLightCycling() {
        Intersection intersection = new Intersection();
        Map<TrafficLight.Direction, Queue<Vehicle>> emptyQueues = new HashMap<>();
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            emptyQueues.put(dir, new LinkedList<>());
        }
        
        // North is initially green, run through a full cycle
        assertEquals(TrafficLight.Direction.NORTH, intersection.getCurrentGreenDirection());
        
        // Update 2 times to end green phase
        intersection.update(emptyQueues);
        intersection.update(emptyQueues);
        assertEquals(TrafficLight.LightState.YELLOW, 
                     intersection.getTrafficLight(TrafficLight.Direction.NORTH).getCurrentState());
                     
        // Update 1 time to end yellow phase
        intersection.update(emptyQueues);
        assertEquals(TrafficLight.LightState.RED, 
                     intersection.getTrafficLight(TrafficLight.Direction.NORTH).getCurrentState());
        
        // East should now have red-yellow
        assertEquals(TrafficLight.LightState.RED_YELLOW, 
                     intersection.getTrafficLight(TrafficLight.Direction.EAST).getCurrentState());
                     
        // Update 2 more times to get East to green
        intersection.update(emptyQueues);
        intersection.update(emptyQueues);
        assertEquals(TrafficLight.Direction.EAST, intersection.getCurrentGreenDirection());
    }
}