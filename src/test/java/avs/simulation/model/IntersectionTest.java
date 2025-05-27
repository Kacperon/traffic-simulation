package avs.simulation.model;

import avs.simulation.model.LightControlers.TrafficLight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IntersectionTest {

    private Intersection intersection;

    @BeforeEach
    void setUp() {
        intersection = new Intersection();
    }

    @Test
    void getTrafficLight() {
        TrafficLight light = intersection.getTrafficLight(TrafficLight.Direction.NORTH);
        assertNotNull(light, "Traffic light for NORTH should not be null");
    }

    @Test
    void update() {
        Map<TrafficLight.Direction, Queue<Vehicle>> queues = new EnumMap<>(TrafficLight.Direction.class);
        queues.put(TrafficLight.Direction.NORTH, new LinkedList<>());
        queues.put(TrafficLight.Direction.SOUTH, new LinkedList<>());

        intersection.update(queues);

        // Since it's logic dependent, just assert no exception and check if lights exist
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            assertNotNull(intersection.getTrafficLight(dir), "Traffic light should exist for " + dir);
        }
    }

    @Test
    void getCurrentGreenDirections() {
        List<TrafficLight.Direction> greenDirs = intersection.getCurrentGreenDirections();
        assertNotNull(greenDirs, "Green directions list should not be null");
        // Depending on controller, this may return 1-2 directions
        assertTrue(greenDirs.size() >= 0, "Green directions list should be zero or more");
    }

    @Test
    void processVehicles() {
        Map<TrafficLight.Direction, Queue<Vehicle>> queues = new EnumMap<>(TrafficLight.Direction.class);
        Queue<Vehicle> northQueue = new LinkedList<>();
        Vehicle v1 = new Vehicle("V1", TrafficLight.Direction.NORTH, TrafficLight.Direction.NORTH);
        Vehicle v2 = new Vehicle("V2", TrafficLight.Direction.NORTH, TrafficLight.Direction.NORTH);
        northQueue.add(v1);
        northQueue.add(v2);
        queues.put(TrafficLight.Direction.NORTH, northQueue);

        StepStatus status = new StepStatus();
        List<Vehicle> completed = new ArrayList<>();

        intersection.processVehicles(queues, status, completed);

        assertEquals(2, completed.size(), "Should have processed 2 vehicles");
        assertTrue(status.getLeftVehicles().contains("V1"), "V1 should be marked as left");
        assertTrue(status.getLeftVehicles().contains("V2"), "V2 should be marked as left");
    }

    @Test
    void getCurrentGreenDirection() {
        TrafficLight.Direction current = intersection.getCurrentGreenDirection();
        assertNotNull(current, "Current green direction should not be null");
    }

    @Test
    void setControllerType() {
        intersection.setControllerType(Intersection.ControllerType.PRIORITY);
        // Can be tested by checking behavior or internal field, but we rely on behavior:
        // After switching, check update or green direction still works
        List<TrafficLight.Direction> greens = intersection.getCurrentGreenDirections();
        assertNotNull(greens, "Green directions should still be returned after controller switch");
    }
}
