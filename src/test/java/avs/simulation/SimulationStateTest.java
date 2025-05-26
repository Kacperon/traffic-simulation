package avs.simulation;

import avs.simulation.UI.SimulationState;
import avs.simulation.model.TrafficLight;
import avs.simulation.model.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class SimulationStateTest {
    private SimulationState state;
    
    @BeforeEach
    public void setUp() {
        state = new SimulationState();
    }
    
    @Test
    public void testInitialState() {
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            assertNotNull(state.getVehicleQueue(dir));
            assertTrue(state.getVehicleQueue(dir).isEmpty());
            assertEquals(TrafficLight.LightState.RED, state.getLightState(dir));
        }
        
        assertNotNull(state.getCrossingVehicles());
        assertTrue(state.getCrossingVehicles().isEmpty());
    }
    
    @Test
    public void testSetAndGetLightState() {
        state.setLightState(TrafficLight.Direction.NORTH, TrafficLight.LightState.GREEN);
        assertEquals(TrafficLight.LightState.GREEN, state.getLightState(TrafficLight.Direction.NORTH));
    }
    
    @Test
    public void testSetAndGetVehicleQueue() {
        List<SimulationState.QueuedVehicle> vehicles = new ArrayList<>();
        vehicles.add(new SimulationState.QueuedVehicle("v1", TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH));
        vehicles.add(new SimulationState.QueuedVehicle("v2", TrafficLight.Direction.NORTH, TrafficLight.Direction.WEST));
        
        state.setVehicleQueue(TrafficLight.Direction.NORTH, vehicles);
        
        List<SimulationState.QueuedVehicle> retrieved = state.getVehicleQueue(TrafficLight.Direction.NORTH);
        assertEquals(2, retrieved.size());
        assertEquals("v1", retrieved.get(0).getId());
        assertEquals("v2", retrieved.get(1).getId());
    }
    
    @Test
    public void testAddAndRemoveCrossingVehicle() {
        state.addCrossingVehicle("v1", TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH);
        
        List<SimulationState.CrossingVehicle> crossing = state.getCrossingVehicles();
        assertEquals(1, crossing.size());
        assertEquals("v1", crossing.get(0).getId());
        
        state.removeVehicleFromAnimation("v1");
        assertTrue(state.getCrossingVehicles().isEmpty());
    }
    
    @Test
    public void testLastCrossedVehicles() {
        List<String> vehicles = Arrays.asList("v1", "v2");
        state.setLastCrossedVehicles(vehicles);
        
        assertEquals(2, state.getLastCrossedVehicles().size());
        assertEquals("v1", state.getLastCrossedVehicles().get(0));
        assertEquals("v2", state.getLastCrossedVehicles().get(1));
    }
    
    @Test
    public void testQueuedVehicleMovementType() {
        SimulationState.QueuedVehicle straight = new SimulationState.QueuedVehicle(
            "v1", TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH);
        assertEquals(Vehicle.MovementType.STRAIGHT, straight.getMovementType());
        
        SimulationState.QueuedVehicle left = new SimulationState.QueuedVehicle(
            "v2", TrafficLight.Direction.NORTH, TrafficLight.Direction.EAST);
        assertEquals(Vehicle.MovementType.LEFT, left.getMovementType());
        
        SimulationState.QueuedVehicle right = new SimulationState.QueuedVehicle(
            "v3", TrafficLight.Direction.NORTH, TrafficLight.Direction.WEST);
        assertEquals(Vehicle.MovementType.RIGHT, right.getMovementType());
    }
}