//package avs.simulation.model.LightControlers;
//
//import avs.simulation.model.TrafficLight;
//import avs.simulation.model.Vehicle;
//import java.util.Map;
//import java.util.Queue;
//import java.util.HashMap;
//
///**
// * Traffic light controller that gives green to opposing directions simultaneously.
// * Left-turning vehicles must wait until end of phase or can cross simultaneously
// * when both opposing vehicles want to turn left.
// */
//public class OpposingTrafficLightController extends AbstractTrafficLightController {
//    private static final int GREEN_DURATION = 6;
//    private static final int YELLOW_DURATION = 1;
//    private static final int RED_YELLOW_DURATION = 1;
//
//    // End of phase threshold (when left turns become allowed)
//    private static final int END_PHASE_THRESHOLD = 2; // Last 2 seconds of green
//
//    private enum AxisState { NORTH_SOUTH, EAST_WEST }
//    private enum Phase { GREEN, YELLOW, RED_YELLOW }
//
//    private AxisState currentAxis = AxisState.NORTH_SOUTH;
//    private Phase currentPhase = Phase.GREEN;
//    private int timeRemaining;
//
//    // Track waiting vehicles by direction
//    private Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues;
//
//    public OpposingTrafficLightController(Map<TrafficLight.Direction, TrafficLight> trafficLights) {
//        super(trafficLights);
//        vehicleQueues = new HashMap<>();
//    }
//
//    @Override
//    protected void initializeLights() {
//        this.currentAxis = AxisState.NORTH_SOUTH;
//        this.timeRemaining = GREEN_DURATION;
//        this.currentPhase = Phase.GREEN;
//
//        // Set initial states - North-South green, East-West red
//        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
//            if (direction == TrafficLight.Direction.NORTH || direction == TrafficLight.Direction.SOUTH) {
//                trafficLights.get(direction).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
//            } else {
//                trafficLights.get(direction).setState(TrafficLight.LightState.RED,
//                        GREEN_DURATION + YELLOW_DURATION + RED_YELLOW_DURATION);
//            }
//        }
//    }
//
//    @Override
//    public void updateLightStates() {
//        // Decrement time
//        timeRemaining--;
//
//        if (timeRemaining <= 0) {
//            // Time to switch to next phase
//            switch (currentPhase) {
//                case GREEN:
//                    // Change current axis directions to yellow
//                    for (TrafficLight.Direction dir : getCurrentAxisDirections()) {
//                        trafficLights.get(dir).setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
//                    }
//                    timeRemaining = YELLOW_DURATION;
//                    currentPhase = Phase.YELLOW;
//                    break;
//
//                case YELLOW:
//                    // Change current axis directions to red
//                    for (TrafficLight.Direction dir : getCurrentAxisDirections()) {
//                        trafficLights.get(dir).setState(TrafficLight.LightState.RED,
//                                GREEN_DURATION + YELLOW_DURATION);
//                    }
//
//                    // Switch to other axis
//                    currentAxis = (currentAxis == AxisState.NORTH_SOUTH) ?
//                                 AxisState.EAST_WEST : AxisState.NORTH_SOUTH;
//
//                    // Set RED_YELLOW for new axis directions
//                    for (TrafficLight.Direction dir : getCurrentAxisDirections()) {
//                        trafficLights.get(dir).setState(TrafficLight.LightState.RED_YELLOW, RED_YELLOW_DURATION);
//                    }
//
//                    timeRemaining = RED_YELLOW_DURATION;
//                    currentPhase = Phase.RED_YELLOW;
//                    break;
//
//                case RED_YELLOW:
//                    // Change from red-yellow to green for current axis
//                    for (TrafficLight.Direction dir : getCurrentAxisDirections()) {
//                        trafficLights.get(dir).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
//                    }
//
//                    // Reset timings for new green phase
//                    timeRemaining = GREEN_DURATION;
//                    currentPhase = Phase.GREEN;
//                    break;
//            }
//        }
//    }
//
//    /**
//     * Get the directions that are active in the current axis state
//     */
//    private TrafficLight.Direction[] getCurrentAxisDirections() {
//        if (currentAxis == AxisState.NORTH_SOUTH) {
//            return new TrafficLight.Direction[] {
//                TrafficLight.Direction.NORTH,
//                TrafficLight.Direction.SOUTH
//            };
//        } else {
//            return new TrafficLight.Direction[] {
//                TrafficLight.Direction.EAST,
//                TrafficLight.Direction.WEST
//            };
//        }
//    }
//
//    @Override
//    public TrafficLight.Direction getCurrentGreenDirection() {
//        // Return first green direction
//        for (TrafficLight.Direction dir : getCurrentAxisDirections()) {
//            if (trafficLights.get(dir).getCurrentState() == TrafficLight.LightState.GREEN) {
//                return dir;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Set the vehicle queues for left-turn decision making
//     */
//    public void setVehicleQueues(Map<TrafficLight.Direction, Queue<Vehicle>> queues) {
//        this.vehicleQueues = queues;
//    }
//
//    /**
//     * Enhanced vehicle crossing check that handles left turns aggressively.
//     * Left turns only allowed:
//     * 1. At the end of green phase (last few seconds)
//     * 2. When opposing traffic is empty
//     * 3. When opposing vehicle also turns left (simultaneous left turns)
//     */
//    @Override
//    public boolean canVehicleCross(TrafficLight.Direction fromDirection,
//                                  TrafficLight.Direction toDirection) {
//        TrafficLight light = trafficLights.get(fromDirection);
//        if (light == null || light.getCurrentState() != TrafficLight.LightState.GREEN) {
//            return false;
//        }
//
//        // Check if turning left
//        boolean isLeftTurn = isLeftTurn(fromDirection, toDirection);
//
//        // If not turning left, can cross normally
//        if (!isLeftTurn) {
//            return true;
//        }
//
//        // For left turns, check special conditions
//        TrafficLight.Direction opposingDir = getOpposingDirection(fromDirection);
//        Queue<Vehicle> opposingQueue = vehicleQueues.get(opposingDir);
//
//        // CONDITION 1: End of green phase - allow left turns in last few seconds
//        boolean isEndOfPhase = (currentPhase == Phase.GREEN && timeRemaining <= END_PHASE_THRESHOLD);
//        if (isEndOfPhase) {
//            return true;
//        }
//
//        // CONDITION 2: No opposing traffic - free to turn left
//        if (opposingQueue == null || opposingQueue.isEmpty()) {
//            return true;
//        }
//
//        // CONDITION 3: Check if first oncoming vehicle also turns left - simultaneous left turns
//        Vehicle firstOpposingVehicle = opposingQueue.peek();
//        if (firstOpposingVehicle != null) {
//            TrafficLight.Direction opposingVehicleDestination = firstOpposingVehicle.getEndRoad();
//            boolean opposingVehicleTurnsLeft = isLeftTurn(opposingDir, opposingVehicleDestination);
//
//            // Allow left turn if opposing vehicle also turns left
//            if (opposingVehicleTurnsLeft) {
//                return true;
//            }
//        }
//
//        // Default case - cannot cross (straight traffic has priority)
//        return false;
//    }
//
//    // Method overload to maintain compatibility with the abstract method
//    @Override
//    public boolean canVehicleCross(TrafficLight.Direction fromDirection) {
//        // This simple check doesn't consider left turns
//        TrafficLight light = trafficLights.get(fromDirection);
//        return light != null && light.getCurrentState() == TrafficLight.LightState.GREEN;
//    }
//
//    /**
//     * Check if a move from one direction to another is a left turn
//     */
//    private boolean isLeftTurn(TrafficLight.Direction from, TrafficLight.Direction to) {
//        switch (from) {
//            case NORTH: return to == TrafficLight.Direction.EAST;
//            case EAST: return to == TrafficLight.Direction.SOUTH;
//            case SOUTH: return to == TrafficLight.Direction.WEST;
//            case WEST: return to == TrafficLight.Direction.NORTH;
//            default: return false;
//        }
//    }
//
//    /**
//     * Get the opposing direction
//     */
//    private TrafficLight.Direction getOpposingDirection(TrafficLight.Direction direction) {
//        switch (direction) {
//            case NORTH: return TrafficLight.Direction.SOUTH;
//            case SOUTH: return TrafficLight.Direction.NORTH;
//            case EAST: return TrafficLight.Direction.WEST;
//            case WEST: return TrafficLight.Direction.EAST;
//            default: return null;
//        }
//    }
//}