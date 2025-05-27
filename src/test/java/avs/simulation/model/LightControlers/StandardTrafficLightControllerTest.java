package avs.simulation.model.LightControlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StandardTrafficLightControllerTest {

    private Map<TrafficLight.Direction, TrafficLight> lights;
    private StandardTrafficLightController controller;

    @BeforeEach
    void setUp() {
        lights = new EnumMap<>(TrafficLight.Direction.class);
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            lights.put(dir, new TrafficLight());
        }
        controller = new StandardTrafficLightController(lights);
        controller.initializeLights();
    }

    @Test
    void initializeLights() {
        assertEquals(TrafficLight.LightState.GREEN, lights.get(TrafficLight.Direction.NORTH).getCurrentState(),
                "NORTH light should be GREEN initially");

        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            if (dir != TrafficLight.Direction.NORTH) {
                assertEquals(TrafficLight.LightState.RED, lights.get(dir).getCurrentState(),
                        dir + " light should be RED initially");
            }
        }
    }

    @Test
    void updateLightStates_cycleThroughPhases() {
        // Cycle through GREEN → YELLOW → RED → RED_YELLOW → GREEN (next direction)
        // 1. Simulate GREEN phase completion
        for (int i = 0; i < 4; i++) controller.updateLightStates(); // GREEN ends

        assertEquals(TrafficLight.LightState.YELLOW,
                lights.get(TrafficLight.Direction.NORTH).getCurrentState(), "Should switch to YELLOW");

        // 2. Simulate YELLOW phase completion
        controller.updateLightStates(); // YELLOW ends

        assertEquals(TrafficLight.LightState.RED,
                lights.get(TrafficLight.Direction.NORTH).getCurrentState(), "NORTH should be RED now");
        assertEquals(TrafficLight.LightState.RED_YELLOW,
                lights.get(TrafficLight.Direction.EAST).getCurrentState(), "EAST should be RED_YELLOW");

        // 3. Simulate RED_YELLOW phase completion
        controller.updateLightStates(); // RED_YELLOW ends

        assertEquals(TrafficLight.LightState.GREEN,
                lights.get(TrafficLight.Direction.EAST).getCurrentState(), "EAST should be GREEN now");
    }

    @Test
    void getCurrentGreenDirection_initialAndAfterUpdate() {
        assertEquals(TrafficLight.Direction.NORTH, controller.getCurrentGreenDirection(),
                "Initial green direction should be NORTH");

        // Advance to EAST
        for (int i = 0; i < 6; i++) controller.updateLightStates();

        assertEquals(TrafficLight.Direction.EAST, controller.getCurrentGreenDirection(),
                "After one full cycle, green direction should be EAST");
    }
}
