package avs.simulation;

import java.util.*;

public abstract class AbstractIntersection {
    protected Map<TrafficLight.Direction, TrafficLight> trafficLights;

    /**
     * Updates the intersection state based on vehicle queues
     *
     * @param vehicleQueues The vehicle queues for each direction
     */
    public abstract void update(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues);

    /**
     * Gets the traffic light for a specific direction
     *
     * @param direction The direction to get the traffic light for
     * @return The traffic light for that direction
     */
    public abstract TrafficLight getTrafficLight(TrafficLight.Direction direction);

    /**
     * Gets the current green directions
     *
     * @return List of directions that currently have a green light
     */
    public abstract List<TrafficLight.Direction> getCurrentGreenDirections();

    /**
     * Processes vehicles that can cross the intersection
     *
     * @param vehicleQueues The vehicle queues for each direction
     * @param stepStatus The status object to record vehicles that cross
     * @param completedVehicles The list to add vehicles that have completed crossing
     */
    public abstract void processVehicles(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues,
                                         StepStatus stepStatus,
                                         List<Vehicle> completedVehicles);
}