package avs.simulation.UI;

import avs.simulation.model.TrafficLight;
import avs.simulation.model.Vehicle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Interpolator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;
import java.util.function.Consumer;

public class AnimatedVehicle {
    private Vehicle vehicle;
    private Timeline animation;
    private boolean reachedMidpoint = false;
    private Consumer<String> onMidpointReached;
    private Consumer<String> onAnimationFinished;
    private final DoubleProperty x = new SimpleDoubleProperty();
    private final DoubleProperty y = new SimpleDoubleProperty();
    private final DoubleProperty rotation = new SimpleDoubleProperty();
    
    public AnimatedVehicle(String id, 
                          TrafficLight.Direction fromDirection, 
                          TrafficLight.Direction toDirection,
                          Consumer<String> onMidpointReached,
                          Consumer<String> onAnimationFinished) {
        this.vehicle = new Vehicle(id, fromDirection, toDirection);
        this.onMidpointReached = onMidpointReached;
        this.onAnimationFinished = onAnimationFinished;
        setInitialPositionAndRotation();
    }
    
    private void setInitialPositionAndRotation() {

        TrafficLight.Direction fromDirection = vehicle.getStartRoad();
        
        switch(fromDirection) {
            case NORTH: rotation.set(90); break;
            case EAST: rotation.set(180); break;
            case SOUTH: rotation.set(270); break;
            case WEST: rotation.set(0); break;
        }
        switch(fromDirection) {
            case NORTH: 
                x.set(0.47);
                y.set(0.4);
                break;
            case EAST: 
                x.set(0.6);
                y.set(0.47);
                break;
            case SOUTH: 
                x.set(0.53);
                y.set(0.6);
                break;
            case WEST: 
                x.set(0.4);
                y.set(0.53);
                break;
        }
    }
    
    public void animate(double durationSeconds) {
        animation = new Timeline();
        switch (vehicle.getMovementType()) {
            case STRAIGHT: animateStraight(durationSeconds); break;
            case LEFT:     animateLeft(durationSeconds); break;
            case RIGHT:    animateRight(durationSeconds); break;
        }

        animation.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!reachedMidpoint && newTime.toSeconds() >= 0) {
                reachedMidpoint = true;
                if (onMidpointReached != null) {
                    onMidpointReached.accept(vehicle.getStartRoad().name());
                }
            }
        });
        
        animation.setOnFinished(event -> {
            if (onAnimationFinished != null) {
                onAnimationFinished.accept(vehicle.getVehicleId());
            }
        });
        
        animation.play();
    }
    
    private void animateStraight(double durationSeconds) {
        double endX = x.get();
        double endY = y.get();
        TrafficLight.Direction toDirection = vehicle.getEndRoad();
        
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
        
        animation.getKeyFrames().add(keyFrame);
    }
    
    private void animateLeft(double durationSeconds) {
        double midX = x.get(), midY = y.get();
        double endX = x.get(), endY = y.get();
        double finalRotation = rotation.get() + 90;
        TrafficLight.Direction fromDirection = vehicle.getStartRoad();

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
        animation.getKeyFrames().addAll(keyFrame1, keyFrame2);
    }
    
    private void animateRight(double durationSeconds) {
        double midX = x.get(), midY = y.get();
        double endX = x.get(), endY = y.get();
        double finalRotation = rotation.get() - 90;
        TrafficLight.Direction fromDirection = vehicle.getStartRoad();

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
        
        animation.getKeyFrames().addAll(keyFrame1, keyFrame2);
    }
    
    public void stopAnimation() {
        if (animation != null) {
            animation.stop();
        }
    }
    
    public String getId() {
        return vehicle.getVehicleId();
    }
    
    public TrafficLight.Direction getFromDirection() {
        return vehicle.getStartRoad();
    }
    
    public Vehicle.MovementType getMovementType() {
        return vehicle.getMovementType();
    }
    
    public double getDrawX(double canvasWidth) {
        return x.get() * canvasWidth;
    }
    
    public double getDrawY(double canvasHeight) {
        return y.get() * canvasHeight;
    }
    
    public double getRotation() {
        return rotation.get();
    }
    
    public boolean hasReachedMidpoint() {
        return reachedMidpoint;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
}