package avs.simulation;

import avs.simulation.model.LightControlers.TrafficLight;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrafficLightTest {

    @Test
    public void testInitialState() {
        TrafficLight light = new TrafficLight();
        assertEquals(TrafficLight.LightState.RED, light.getCurrentState());
    }

    @Test
    public void testStateChange() {
        TrafficLight light = new TrafficLight();
        light.setState(TrafficLight.LightState.GREEN, 2);
        assertEquals(TrafficLight.LightState.GREEN, light.getCurrentState());
    }
    
    @Test
    public void testStateFinished() {
        TrafficLight light = new TrafficLight();
        light.setState(TrafficLight.LightState.YELLOW, 0);
        assertTrue(light.isStateFinished());
    }
    
    @Test
    public void testStateNotFinished() {
        TrafficLight light = new TrafficLight();
        light.setState(TrafficLight.LightState.GREEN, 2);
        assertFalse(light.isStateFinished());
    }
    
    @Test
    public void testUpdateDecrementsTimeLeft() {
        TrafficLight light = new TrafficLight();
        light.setState(TrafficLight.LightState.GREEN, 2);
        light.update();
        light.update();
        assertTrue(light.isStateFinished());
    }
}