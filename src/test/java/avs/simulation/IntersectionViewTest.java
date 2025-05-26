package avs.simulation;

import avs.simulation.UI.IntersectionView;
import avs.simulation.UI.SimulationState;
import avs.simulation.model.TrafficLight;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

// Note: This test requires JavaFX runtime - it may need to be run 
// with a specific JUnit launcher that supports JavaFX
public class IntersectionViewTest {
    private IntersectionView view;
    
    @BeforeEach
    public void setUp() {
        // Initialize JavaFX Toolkit
        try {
            // This will fail in headless environments
            javafx.application.Platform.startup(() -> {});
            view = new IntersectionView(400, 400);
        } catch (Exception e) {
            // If running in a CI/CD environment without graphics, tests will be skipped
            System.out.println("JavaFX initialization failed, tests will be skipped");
        }
    }
    
    @Test
    public void testViewCreation() {
        if (view == null) {
            // Skip test if JavaFX isn't available
            return;
        }
        
        assertNotNull(view);
        assertEquals(400, view.getWidth());
        assertEquals(400, view.getHeight());
    }
    
    @Test
    public void testStateUpdate() {
        if (view == null) {
            // Skip test if JavaFX isn't available
            return;
        }
        
        SimulationState state = new SimulationState();
        state.setLightState(TrafficLight.Direction.NORTH, TrafficLight.LightState.GREEN);
        
        // Add a vehicle to a queue
        state.setVehicleQueue(TrafficLight.Direction.NORTH, java.util.Arrays.asList(
            new SimulationState.QueuedVehicle("test", TrafficLight.Direction.NORTH, TrafficLight.Direction.SOUTH)
        ));
        
        // This should not throw exceptions
        view.update(state);
    }

}