package avs.simulation.UI.utils;

import avs.simulation.model.TrafficLight;
import avs.simulation.model.Vehicle;

/**
 * Utility class for direction-related calculations and conversions
 */
public class DirectionUtils {
    
    /**
     * Get rotation angle based on direction
     */
    public static double getRotationForDirection(TrafficLight.Direction direction) {
        switch(direction) {
            case NORTH: return 90;
            case EAST: return 180;
            case SOUTH: return 270;
            case WEST: return 0;
            default: return 0;
        }
    }
    
    /**
     * Get starting position for a direction
     * @return double[] {x, y} normalized coordinates (0-1)
     */
    public static double[] getStartPositionForDirection(TrafficLight.Direction direction) {
        switch(direction) {
            case NORTH: return new double[] {0.47, 0.4};
            case EAST: return new double[] {0.6, 0.47};
            case SOUTH: return new double[] {0.53, 0.6};
            case WEST: return new double[] {0.4, 0.53};
            default: return new double[] {0.5, 0.5};
        }
    }
    
    /**
     * Get the opposing direction
     */
    public static TrafficLight.Direction getOpposingDirection(TrafficLight.Direction direction) {
        switch(direction) {
            case NORTH: return TrafficLight.Direction.SOUTH;
            case EAST: return TrafficLight.Direction.WEST;
            case SOUTH: return TrafficLight.Direction.NORTH;
            case WEST: return TrafficLight.Direction.EAST;
            default: return null;
        }
    }
    
    /**
     * Get the next direction in clockwise order
     */
    public static TrafficLight.Direction getNextClockwiseDirection(TrafficLight.Direction direction) {
        switch(direction) {
            case NORTH: return TrafficLight.Direction.EAST;
            case EAST: return TrafficLight.Direction.SOUTH;
            case SOUTH: return TrafficLight.Direction.WEST;
            case WEST: return TrafficLight.Direction.NORTH;
            default: return TrafficLight.Direction.NORTH;
        }
    }
    
    /**
     * Determine movement type from start and end directions
     */
    public static Vehicle.MovementType getMovementType(TrafficLight.Direction from, TrafficLight.Direction to) {
        int diff = (to.ordinal() - from.ordinal() + 4) % 4;
        switch(diff) {
            case 1: return Vehicle.MovementType.LEFT;
            case 2: return Vehicle.MovementType.STRAIGHT;
            case 3: return Vehicle.MovementType.RIGHT;
            default: return Vehicle.MovementType.STRAIGHT;
        }
    }
}