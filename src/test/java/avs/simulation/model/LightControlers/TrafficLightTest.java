package avs.simulation.model.LightControlers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrafficLightTest {

    @Test
    void setState() {
        TrafficLight light = new TrafficLight();
        light.setState(TrafficLight.LightState.GREEN, 5);
        assertEquals(TrafficLight.LightState.GREEN, light.getCurrentState(), "State should be GREEN");
        assertEquals(5, light.getRemainingTime(), "Remaining time should be 5");
    }

    @Test
    void update() {
        TrafficLight light = new TrafficLight();
        light.setState(TrafficLight.LightState.YELLOW, 3);
        light.update();
        assertEquals(2, light.getRemainingTime(), "Remaining time should decrease by 1");
        light.update();
        light.update();
        light.update(); // Extra update shouldn't make time negative
        assertEquals(0, light.getRemainingTime(), "Remaining time should not go below 0");
    }

    @Test
    void isStateFinished() {
        TrafficLight light = new TrafficLight();
        light.setState(TrafficLight.LightState.RED_YELLOW, 1);
        assertFalse(light.isStateFinished(), "Should not be finished yet");
        light.update();
        assertTrue(light.isStateFinished(), "Should be finished after duration expires");
    }

    @Test
    void getCurrentState() {
        TrafficLight light = new TrafficLight();
        assertEquals(TrafficLight.LightState.RED, light.getCurrentState(), "Initial state should be RED");
        light.setState(TrafficLight.LightState.GREEN, 4);
        assertEquals(TrafficLight.LightState.GREEN, light.getCurrentState(), "State should be GREEN after set");
    }

    @Test
    void getRemainingTime() {
        TrafficLight light = new TrafficLight();
        assertEquals(0, light.getRemainingTime(), "Initial remaining time should be 0");
        light.setState(TrafficLight.LightState.YELLOW, 2);
        assertEquals(2, light.getRemainingTime(), "Remaining time should be set to 2");
    }
}
