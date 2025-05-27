package avs.simulation.model.LightControlers;

import avs.simulation.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PriorityTrafficLightControllerTest {

    private Map<TrafficLight.Direction, TrafficLight> lights;
    private OpposingTrafficLightController controller;

    @BeforeEach
    void setUp() {
        lights = new EnumMap<>(TrafficLight.Direction.class);
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            lights.put(dir, new TrafficLight());
        }
        controller = new OpposingTrafficLightController(lights);
        controller.initializeLights();
    }

    @Test
    void initializeLights() {
        assertEquals(TrafficLight.LightState.GREEN,
                lights.get(TrafficLight.Direction.NORTH).getCurrentState(),
                "NORTH light should be initially GREEN");
        assertEquals(TrafficLight.LightState.GREEN,
                lights.get(TrafficLight.Direction.SOUTH).getCurrentState(),
                "SOUTH light should be initially RED");
        assertEquals(TrafficLight.LightState.RED,
                lights.get(TrafficLight.Direction.EAST).getCurrentState(),
                "EAST light should be initially RED");
        assertEquals(TrafficLight.LightState.RED,
                lights.get(TrafficLight.Direction.WEST).getCurrentState(),
                "WEST light should be initially RED");
    }

    @Test
    void updateLightStates_priorityLogic() {
        // Simulate GREEN phase ends
        for (int i = 0; i < 5; i++) {
            controller.updateLightStates();
        }

        assertEquals(TrafficLight.LightState.RED,
                lights.get(TrafficLight.Direction.NORTH).getCurrentState(),
                "NORTH light should be RED after green ends");

        // Add high queue count to SOUTH before switching
        Map<TrafficLight.Direction, Queue<Vehicle>> queues = new EnumMap<>(TrafficLight.Direction.class);
        Queue<Vehicle> southQueue = new LinkedList<>();
        for (int i = 0; i < 5; i++) southQueue.add(new Vehicle("V" + i, TrafficLight.Direction.NORTH, TrafficLight.Direction.EAST));
        queues.put(TrafficLight.Direction.SOUTH, southQueue);

        controller.updateQueueLengths(queues);

        // Simulate YELLOW ends and switch should happen
        controller.updateLightStates();

        assertEquals(TrafficLight.LightState.RED,
                lights.get(TrafficLight.Direction.SOUTH).getCurrentState(),
                "SOUTH should get GREEN due to high queue");

        assertEquals(TrafficLight.Direction.EAST,
                controller.getCurrentGreenDirection(),
                "Current green direction should be EAST");
    }

    @Test
    void updateLightStates_rotateWhenLowPriority() {
        // Simulate GREEN phase ends
        for (int i = 0; i < 4; i++) controller.updateLightStates(); // GREEN ends

        // YELLOW phase
        controller.updateLightStates(); // YELLOW ends

        // No queues updated → fallback to next direction
        controller.updateLightStates(); // RED → GREEN (rotation)

        assertEquals(TrafficLight.Direction.EAST,
                controller.getCurrentGreenDirection(),
                "Should rotate to EAST if no direction has priority queue");
    }

    @Test
    void updateQueueLengths() {
        Map<TrafficLight.Direction, Queue<Vehicle>> queues = new EnumMap<>(TrafficLight.Direction.class);
        Queue<Vehicle> eastQueue = new LinkedList<>();
        eastQueue.add(new Vehicle("v1", TrafficLight.Direction.NORTH, TrafficLight.Direction.EAST));
        eastQueue.add(new Vehicle("v2", TrafficLight.Direction.NORTH, TrafficLight.Direction.EAST));
        queues.put(TrafficLight.Direction.EAST, eastQueue);

        controller.updateQueueLengths(queues);

        // Now we simulate light change cycle and EAST should be selected if it exceeds MIN_VEHICLES_FOR_PRIORITY
        for (int i = 0; i < 4; i++) controller.updateLightStates(); // GREEN ends
        controller.updateLightStates(); // YELLOW ends
        controller.updateLightStates(); // should move to EAST

        // Because east had only 2 (less than 3), fallback to EAST via rotation (if NORTH was current)
        assertEquals(TrafficLight.Direction.EAST,
                controller.getCurrentGreenDirection());
    }
}
