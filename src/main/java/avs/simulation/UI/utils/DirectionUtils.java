package avs.simulation.UI.utils;

import avs.simulation.model.LightControlers.TrafficLight;
import avs.simulation.model.Vehicle;

/**
 * Utility class for direction-related calculations and conversions
 */
public class DirectionUtils {
    
    /**
     * Get rotation angle based on direction
     */
    public static double getRotationForDirection(TrafficLight.Direction direction) {
        return switch (direction) {
            case NORTH -> 90;
            case EAST -> 180;
            case SOUTH -> 270;
            case WEST -> 0;
        };
    }
    
    /**
     * Get starting position for a direction
     * @return double[] {x, y} normalized coordinates (0-1)
     */
    public static double[] getStartPositionForDirection(TrafficLight.Direction direction) {
        return switch (direction) {
            case NORTH -> new double[]{0.47, 0.4};
            case EAST -> new double[]{0.6, 0.47};
            case SOUTH -> new double[]{0.53, 0.6};
            case WEST -> new double[]{0.4, 0.53};
        };
    }
    
    /**
     * Get the movement type (LEFT, STRAIGHT, RIGHT) based on start and end directions
     */
    public static Vehicle.MovementType getMovementType(TrafficLight.Direction from, TrafficLight.Direction to) {
        int diff = (to.ordinal() - from.ordinal() + 4) % 4;
        return switch (diff) {
            case 1 -> Vehicle.MovementType.LEFT;
            case 2 -> Vehicle.MovementType.STRAIGHT;
            case 3 -> Vehicle.MovementType.RIGHT;
            default -> Vehicle.MovementType.STRAIGHT;
        };
    }
    
    /**
     * Check if a move from one direction to another is a left turn
     */
    public static boolean isLeftTurn(TrafficLight.Direction from, TrafficLight.Direction to) {
        return getMovementType(from, to) == Vehicle.MovementType.LEFT;
    }
    
    /**
     * Get the opposing direction
     */
    public static TrafficLight.Direction getOpposingDirection(TrafficLight.Direction direction) {
        return switch (direction) {
            case NORTH -> TrafficLight.Direction.SOUTH;
            case SOUTH -> TrafficLight.Direction.NORTH;
            case EAST -> TrafficLight.Direction.WEST;
            case WEST -> TrafficLight.Direction.EAST;
        };
    }
    
    /**
     * Get the next direction in clockwise order
     */
    public static TrafficLight.Direction getNextClockwiseDirection(TrafficLight.Direction direction) {
        return switch (direction) {
            case NORTH -> TrafficLight.Direction.EAST;
            case EAST -> TrafficLight.Direction.SOUTH;
            case SOUTH -> TrafficLight.Direction.WEST;
            case WEST -> TrafficLight.Direction.NORTH;
        };
    }
}