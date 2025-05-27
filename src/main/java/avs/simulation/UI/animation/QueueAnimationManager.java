package avs.simulation.UI.animation;

import avs.simulation.UI.SimulationState;
import avs.simulation.UI.renderers.VehicleView;
import avs.simulation.model.TrafficLight;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;

/**
 * Manages vehicle queue animations and rendering
 */
public class QueueAnimationManager {
    private Map<TrafficLight.Direction, Timeline> queueAnimationTimelines = new HashMap<>();
    private Map<TrafficLight.Direction, List<Double[]>> currentQueuePositions = new HashMap<>();
    
    public QueueAnimationManager() {
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            queueAnimationTimelines.put(dir, null);
            currentQueuePositions.put(dir, new ArrayList<>());
        }
    }
    
    /**
     * Animate queue advancement for a specific direction
     */
    public void animateQueueAdvancement(TrafficLight.Direction direction, 
                                       List<SimulationState.QueuedVehicle> vehicles,
                                       double canvasWidth, double canvasHeight) {
        if (queueAnimationTimelines.get(direction) != null) {
            queueAnimationTimelines.get(direction).stop();
        }
        
        if (vehicles.isEmpty()) {
            currentQueuePositions.put(direction, new ArrayList<>());
            return;
        }
        
        List<Double[]> targetPositions = calculateTargetPositions(
            direction, vehicles, canvasWidth, canvasHeight);
        
        List<Double[]> currentPositions = currentQueuePositions.get(direction);
        if (currentPositions.isEmpty()) {
            currentQueuePositions.put(direction, new ArrayList<>(targetPositions));
            return;
        }
        
        Timeline timeline = createAnimationTimeline(direction, currentPositions, targetPositions);
        queueAnimationTimelines.put(direction, timeline);
        timeline.play();
    }
    
    /**
     * Calculate target positions for vehicles in a queue
     */
    private List<Double[]> calculateTargetPositions(
        TrafficLight.Direction direction, 
        List<SimulationState.QueuedVehicle> vehicles,
        double canvasWidth, double canvasHeight) {
        
        List<Double[]> targetPositions = new ArrayList<>();
        double startX, startY;
        double dx = 0, dy = 0;
        
        switch (direction) {
            case NORTH:
                startX = canvasWidth/2 - 10;
                startY = canvasHeight/2 - 50;
                dy = -30;
                break;
            case EAST:
                startX = canvasWidth/2 + 50;
                startY = canvasHeight/2 - 10;
                dx = 30;
                break;
            case SOUTH:
                startX = canvasWidth/2 + 10;
                startY = canvasHeight/2 + 50;
                dy = 30;
                break;
            case WEST:
            default:
                startX = canvasWidth/2 - 50;
                startY = canvasHeight/2 + 10;
                dx = -30;
                break;
        }
        
        int maxVehicles = Math.min(vehicles.size(), 5);
        for (int i = 0; i < maxVehicles; i++) {
            targetPositions.add(new Double[] {
                startX + i * dx,
                startY + i * dy
            });
        }
        
        return targetPositions;
    }
    
    /**
     * Create a timeline for animating from current to target positions
     */
    private Timeline createAnimationTimeline(
        TrafficLight.Direction direction,
        List<Double[]> currentPositions, 
        List<Double[]> targetPositions) {
        
        Timeline timeline = new Timeline();
        KeyFrame delayFrame = new KeyFrame(Duration.seconds(0.1));
        timeline.getKeyFrames().add(delayFrame);

        for (int i = 0; i < Math.min(currentPositions.size(), targetPositions.size()); i++) {
            KeyFrame moveFrame = new KeyFrame(
                Duration.seconds(0.1),
                new KeyValue(
                    new SimpleDoubleProperty(currentPositions.get(i)[0]), 
                    targetPositions.get(i)[0], 
                    Interpolator.EASE_BOTH),
                new KeyValue(
                    new SimpleDoubleProperty(currentPositions.get(i)[1]), 
                    targetPositions.get(i)[1], 
                    Interpolator.EASE_BOTH)
            );
            timeline.getKeyFrames().add(moveFrame);
        }

        timeline.setOnFinished(e -> {
            currentQueuePositions.put(direction, new ArrayList<>(targetPositions));
        });
        
        return timeline;
    }
    
    /**
     * Draw vehicle queues for all directions
     */
    public void drawVehicleQueues(GraphicsContext gc, SimulationState state, 
                                double canvasWidth, double canvasHeight) {
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            drawVehicleQueue(gc, dir, state.getVehicleQueue(dir), canvasWidth, canvasHeight);
        }
    }
    
    /**
     * Draw vehicle queue for a specific direction
     */
    private void drawVehicleQueue(GraphicsContext gc, TrafficLight.Direction direction, 
                             List<SimulationState.QueuedVehicle> vehicles,
                             double canvasWidth, double canvasHeight) {
        if (vehicles == null || vehicles.isEmpty()) return;

        double startX, startY;
        double dx = 0, dy = 0;
        double rotation = 0;
        
        switch (direction) {
            case NORTH:
                startX = canvasWidth/2 - 10;
                startY = canvasHeight/2 - 50;
                dy = -30;
                rotation = 90;
                break;
            case EAST:
                startX = canvasWidth/2 + 50;
                startY = canvasHeight/2 - 10;
                dx = 30;
                rotation = 180;
                break;
            case SOUTH:
                startX = canvasWidth/2 + 10;
                startY = canvasHeight/2 + 50;
                dy = 30;
                rotation = 270;
                break;
            case WEST:
            default:
                startX = canvasWidth/2 - 50;
                startY = canvasHeight/2 + 10;
                dx = -30;
                rotation = 0;
                break;
        }

        int maxVehiclesToDraw = Math.min(vehicles.size(), 4);

        for (int i = 0; i < maxVehiclesToDraw; i++) {
            SimulationState.QueuedVehicle vehicle = vehicles.get(i);
            double x = startX + i * dx;
            double y = startY + i * dy;

            VehicleView.draw(gc, vehicle.getId(), x, y, rotation, vehicle.getMovementType());
        }

        if (vehicles.size() > maxVehiclesToDraw) {
            double x = startX + maxVehiclesToDraw * dx;
            double y = startY + maxVehiclesToDraw * dy;
            gc.setFill(Color.BLACK);
            gc.fillText("+" + (vehicles.size() - maxVehiclesToDraw), x, y + 4);
        }
    }
}