package avs.simulation.UI.animation;

import avs.simulation.UI.utils.CircularInterpolator;
import avs.simulation.model.TrafficLight;
import avs.simulation.model.Vehicle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Interpolator;
import javafx.beans.property.DoubleProperty;
import javafx.util.Duration;

/**
 * Factory class for creating animation paths for vehicles
 */
public class AnimationPathFactory {
    
    /**
     * Create animation keyframes based on movement type
     */
    public static void createAnimationPath(
            Timeline timeline,
            Vehicle.MovementType movementType,
            TrafficLight.Direction fromDirection,
            TrafficLight.Direction toDirection,
            DoubleProperty x,
            DoubleProperty y,
            DoubleProperty rotation,
            double durationSeconds) {
        
        switch(movementType) {
            case STRAIGHT: 
                createStraightPath(timeline, toDirection, x, y, durationSeconds);
                break;
            case LEFT:
                createLeftTurnPath(timeline, fromDirection, x, y, rotation, durationSeconds);
                break;
            case RIGHT:
                createRightTurnPath(timeline, fromDirection, x, y, rotation, durationSeconds);
                break;
        }
    }
    
    private static void createStraightPath(Timeline timeline, TrafficLight.Direction toDirection, 
                                         DoubleProperty x, DoubleProperty y, double durationSeconds) {
        double endX = x.get();
        double endY = y.get();
        
        switch(toDirection) {
            case NORTH: endY = 0.2; break;
            case EAST: endX = 0.8; break;
            case SOUTH: endY = 0.8; break;
            case WEST: endX = 0.2; break;
        }
        
        KeyFrame keyFrame = new KeyFrame(
            Duration.seconds(durationSeconds),
            new KeyValue(x, endX, Interpolator.EASE_BOTH),
            new KeyValue(y, endY, Interpolator.EASE_BOTH)
        );
        
        timeline.getKeyFrames().add(keyFrame);
    }
    
    private static void createLeftTurnPath(Timeline timeline, TrafficLight.Direction fromDirection,
                                        DoubleProperty x, DoubleProperty y, DoubleProperty rotation,
                                        double durationSeconds) {
        double midX = x.get(), midY = y.get();
        double endX = x.get(), endY = y.get();
        double finalRotation = rotation.get() + 90;

        switch(fromDirection) {
            case NORTH:
                midX = 0.45; midY = 0.45;
                endX = 0.2; endY = 0.45;
                break;
            case EAST:
                midX = 0.55; midY = 0.45;
                endX = 0.55; endY = 0.2;
                break;
            case SOUTH:
                midX = 0.55; midY = 0.55;
                endX = 0.8; endY = 0.55;
                break;
            case WEST:
                midX = 0.45; midY = 0.55;
                endX = 0.45; endY = 0.8;
                break;
        }
        
        KeyFrame keyFrame1 = new KeyFrame(
            Duration.seconds(durationSeconds * 0.3),
            new KeyValue(x, midX, new CircularInterpolator(CircularInterpolator.Direction.UP)),
            new KeyValue(y, midY, new CircularInterpolator(CircularInterpolator.Direction.DOWN)),
            new KeyValue(rotation, finalRotation, Interpolator.EASE_BOTH)
        );
        KeyFrame keyFrame2 = new KeyFrame(
            Duration.seconds(durationSeconds),
            new KeyValue(x, endX, new CircularInterpolator(CircularInterpolator.Direction.DOWN)),
            new KeyValue(y, endY, new CircularInterpolator(CircularInterpolator.Direction.UP))
        );
        timeline.getKeyFrames().addAll(keyFrame1, keyFrame2);
    }
    
    private static void createRightTurnPath(Timeline timeline, TrafficLight.Direction fromDirection,
                                         DoubleProperty x, DoubleProperty y, DoubleProperty rotation,
                                         double durationSeconds) {
        double midX = x.get(), midY = y.get();
        double endX = x.get(), endY = y.get();
        double finalRotation = rotation.get() - 90;

        switch(fromDirection) {
            case NORTH:
                midX = 0.5; midY = 0.55;
                endX = 0.8; endY = 0.55;
                break;
            case EAST:
                midX = 0.45; midY = 0.5;
                endX = 0.45; endY = 0.8;
                break;
            case SOUTH:
                midX = 0.5; midY = 0.45;
                endX = 0.2; endY = 0.45;
                break;
            case WEST:
                midX = 0.55; midY = 0.5;
                endX = 0.55; endY = 0.2;
                break;
        }

        KeyFrame keyFrame1 = new KeyFrame(
            Duration.seconds(durationSeconds * 0.3),
            new KeyValue(x, midX, new CircularInterpolator(CircularInterpolator.Direction.DOWN)),
            new KeyValue(y, midY, new CircularInterpolator(CircularInterpolator.Direction.UP)),
            new KeyValue(rotation, finalRotation, Interpolator.EASE_BOTH)
        );

        KeyFrame keyFrame2 = new KeyFrame(
            Duration.seconds(durationSeconds),
            new KeyValue(x, endX, new CircularInterpolator(CircularInterpolator.Direction.UP)),
            new KeyValue(y, endY, new CircularInterpolator(CircularInterpolator.Direction.DOWN))
        );
        
        timeline.getKeyFrames().addAll(keyFrame1, keyFrame2);
    }
}