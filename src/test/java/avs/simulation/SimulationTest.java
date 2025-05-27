package avs.simulation;

import avs.simulation.UI.SimulationState;
import avs.simulation.model.LightControlers.TrafficLight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class SimulationTest {
    private Simulation simulation;
    private List<SimulationState> capturedStates;
    
    @BeforeEach
    public void setUp() {
        simulation = new Simulation();
        capturedStates = new ArrayList<>();
        
        simulation.addSimulationListener(state -> capturedStates.add(state));
    }
    
    @Test
    public void testInitialState() {
        // Check initial state has no vehicles
        SimulationState initialState = simulation.getCurrentState();
        assertNotNull(initialState);
        
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            assertTrue(initialState.getVehicleQueue(dir).isEmpty());
        }
        
        assertTrue(initialState.getCrossingVehicles().isEmpty());
    }
    
    @Test
    public void testAddVehicle() {
        simulation.addVehicle("V",TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH);
        
        SimulationState state = simulation.getCurrentState();
        assertEquals(1, state.getVehicleQueue(TrafficLight.Direction.NORTH).size());
    }
    
    @Test
    public void testStepFunction() {
        // Add a vehicle to test with
        simulation.addVehicle("V",TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH);
        
        // Initially in NORTH queue
        assertEquals(1, simulation.getCurrentState().getVehicleQueue(TrafficLight.Direction.NORTH).size());
        
        // Step should move a vehicle from NORTH (which starts with green light)
        simulation.performSimulationStep();
        
        // Vehicle should now be crossing
        assertTrue(simulation.getCurrentState().getVehicleQueue(TrafficLight.Direction.NORTH).isEmpty());
        assertFalse(simulation.getCurrentState().getLastCrossedVehicles().isEmpty());
    }
    
    @Test
    public void testListenerNotifications() {
        capturedStates.clear();
        
        // Step should trigger listener
        simulation.performSimulationStep();
        assertEquals(1, capturedStates.size());
        
        // Add a vehicle
        simulation.addVehicle("V",TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH);
        assertEquals(2, capturedStates.size());
    }
}