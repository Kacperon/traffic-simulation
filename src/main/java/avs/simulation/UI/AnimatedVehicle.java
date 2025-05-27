package avs.simulation.UI;

import avs.simulation.UI.animation.AnimationPathFactory;
import avs.simulation.UI.utils.DirectionUtils;
import avs.simulation.model.TrafficLight;
import avs.simulation.model.Vehicle;
import javafx.animation.Timeline;
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
        
        // Set rotation based on direction
        rotation.set(DirectionUtils.getRotationForDirection(fromDirection));
        
        // Set initial position based on direction
        double[] position = DirectionUtils.getStartPositionForDirection(fromDirection);
        x.set(position[0]);
        y.set(position[1]);
    }
    
    public void animate(double durationSeconds) {
        animation = new Timeline();
        
        // Create animation path based on movement type
        AnimationPathFactory.createAnimationPath(
            animation,
            vehicle.getMovementType(),
            vehicle.getStartRoad(),
            vehicle.getEndRoad(),
            x, y, rotation,
            durationSeconds
        );

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