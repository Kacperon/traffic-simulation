package avs.simulation.UI;

import avs.simulation.*;
import avs.simulation.model.TrafficLight;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Interpolator;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;
import javafx.animation.AnimationTimer;

import java.util.*;

public class IntersectionView extends Canvas {
    private GraphicsContext gc;
    private SimulationState currentState;
    private Map<String, AnimatedVehicle> activeAnimatedVehicles = new HashMap<>();
    private AnimationTimer animationTimer;
    private Map<TrafficLight.Direction, Queue<SimulationState.CrossingVehicle>> pendingVehicles = new HashMap<>();
    private Map<TrafficLight.Direction, Timeline> queueAnimationTimelines = new HashMap<>();
    private Map<TrafficLight.Direction, List<Double[]>> currentQueuePositions = new HashMap<>();

    public IntersectionView(double width, double height) {
        super(width, height);
        gc = getGraphicsContext2D();
        currentState = new SimulationState();

        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            pendingVehicles.put(dir, new LinkedList<>());
            queueAnimationTimelines.put(dir, null);
            currentQueuePositions.put(dir, new ArrayList<>());
        }

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                redrawCanvas();
            }
        };
        animationTimer.start();
    }

    public void update(SimulationState state) {
        this.currentState = state;
        clearCompletedAnimations();
        updateAnimatedVehicles();
    }

    private void redrawCanvas() {
        double width = getWidth();
        double height = getHeight();

        gc.clearRect(0, 0, width, height);
        drawBackground(width, height);
        drawTrafficLights(width, height);
        drawVehicleQueues(width, height);
        drawAnimatedVehicles();
    }

    private void drawBackground(double width, double height) {
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, height/2 - 30, width, 60);
        gc.fillRect(width/2 - 30, 0, 60, height);
        gc.setStroke(Color.WHITE);
        gc.setLineDashes(5); 
        gc.setLineWidth(2);
        gc.strokeLine(0, height/2, width, height/2);
        gc.strokeLine(width/2, 0, width/2, height);
        gc.setLineDashes();
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(width/2 - 30, height/2 - 30, 60, 60);
    }

    private void drawTrafficLights(double width, double height) {
        TrafficLightView.draw(gc, TrafficLight.Direction.NORTH,
                             currentState.getLightState(TrafficLight.Direction.NORTH), 
                             width/2 - 35, height/2 - 65, 0);
        
        TrafficLightView.draw(gc, TrafficLight.Direction.EAST, 
                             currentState.getLightState(TrafficLight.Direction.EAST), 
                             width/2 + 65, height/2 - 35, 90);
        
        TrafficLightView.draw(gc, TrafficLight.Direction.SOUTH, 
                             currentState.getLightState(TrafficLight.Direction.SOUTH), 
                             width/2 + 35, height/2 + 65, 180);
        
        TrafficLightView.draw(gc, TrafficLight.Direction.WEST, 
                             currentState.getLightState(TrafficLight.Direction.WEST), 
                             width/2 - 65, height/2 + 35, 270);
    }

    private void drawVehicleQueues(double width, double height) {
        drawVehicleQueue(TrafficLight.Direction.NORTH, width/2 - 10, height/2 - 50);
        drawVehicleQueue(TrafficLight.Direction.EAST, width/2 + 50, height/2 - 10);
        drawVehicleQueue(TrafficLight.Direction.SOUTH, width/2 + 10, height/2 + 50);
        drawVehicleQueue(TrafficLight.Direction.WEST, width/2 - 50, height/2 + 10);
    }

    private void drawVehicleQueue(TrafficLight.Direction direction, double startX, double startY) {
        List<SimulationState.QueuedVehicle> vehicles = currentState.getVehicleQueue(direction);
        if (vehicles == null || vehicles.isEmpty()) return;

        double dx = 0, dy = 0;
        switch (direction) {
            case NORTH: dx = 0; dy = -30; break;
            case EAST: dx = 30; dy = 0; break;
            case SOUTH: dx = 0; dy = 30; break;
            case WEST: dx = -30; dy = 0; break;
        }

        int maxVehiclesToDraw = Math.min(vehicles.size(), 4);

        for (int i = 0; i < maxVehiclesToDraw; i++) {
            SimulationState.QueuedVehicle vehicle = vehicles.get(i);
            double x = startX + i * dx;
            double y = startY + i * dy;

            double rotation = 0;
            switch(direction) {
                case NORTH: rotation = 90; break;
                case EAST: rotation = 180; break;
                case SOUTH: rotation = 270; break;
                case WEST: rotation = 0; break;
            }

            VehicleView.draw(gc, vehicle.getId(), x, y, rotation, vehicle.getMovementType());
        }

        if (vehicles.size() > maxVehiclesToDraw) {
            double x = startX + maxVehiclesToDraw * dx;
            double y = startY + maxVehiclesToDraw * dy;
            gc.setFill(Color.BLACK);
            gc.fillText("+" + (vehicles.size() - maxVehiclesToDraw), x, y + 4);
        }
    }

    private void updateAnimatedVehicles() {
        List<SimulationState.CrossingVehicle> crossingVehicles = currentState.getCrossingVehicles();
        if (crossingVehicles == null || crossingVehicles.isEmpty()) return;
        
        Set<TrafficLight.Direction> changedDirections = new HashSet<>();
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            Queue<SimulationState.CrossingVehicle> oldQueue = pendingVehicles.get(dir);
            int oldSize = oldQueue.size();
            
            oldQueue.clear();
            if (oldSize > 0) {
                changedDirections.add(dir);
            }
        }

        for (SimulationState.CrossingVehicle vehicle : crossingVehicles) {
            String vehicleId = vehicle.getId();
            TrafficLight.Direction fromDir = vehicle.getFromDirection();

            if (activeAnimatedVehicles.containsKey(vehicleId)) {
                continue;
            }
            
            // Check if there's already a vehicle animating from this direction
            boolean directionHasActiveVehicle = false;
            for (AnimatedVehicle activeVehicle : activeAnimatedVehicles.values()) {
                if (activeVehicle.getFromDirection() == fromDir && !activeVehicle.hasReachedMidpoint()) {
                    directionHasActiveVehicle = true;
                    pendingVehicles.get(fromDir).add(vehicle);
                    break;
                }
            }
            
            // If no active vehicle from this direction, start this one immediately
            if (!directionHasActiveVehicle) {
                startVehicleAnimation(vehicle);
            }
        }
        
        // Animate any changed queues
        for (TrafficLight.Direction dir : changedDirections) {
            animateQueueAdvancement(dir, currentState.getVehicleQueue(dir));
        }
    }

    private void startVehicleAnimation(SimulationState.CrossingVehicle vehicle) {
        AnimatedVehicle animVehicle = new AnimatedVehicle(
            vehicle.getId(),
            vehicle.getFromDirection(),
            vehicle.getToDirection(),
            // Midpoint callback
            (fromDirName) -> {
                TrafficLight.Direction dir = TrafficLight.Direction.valueOf(fromDirName);
                Queue<SimulationState.CrossingVehicle> dirQueue = pendingVehicles.get(dir);
                if (!dirQueue.isEmpty()) {
                    SimulationState.CrossingVehicle nextVehicle = dirQueue.poll();
                    startVehicleAnimation(nextVehicle);
                }
            },
            // Animation finished callback
            (vehicleId) -> {
                activeAnimatedVehicles.remove(vehicleId);
                currentState.removeVehicleFromAnimation(vehicleId);
            }
        );
        
        animVehicle.animate(1.5);
        activeAnimatedVehicles.put(vehicle.getId(), animVehicle);
    }

    private void drawAnimatedVehicles() {
        for (AnimatedVehicle vehicle : activeAnimatedVehicles.values()) {
            VehicleView.draw(
                gc, 
                vehicle.getId(),
                vehicle.getDrawX(getWidth()),
                vehicle.getDrawY(getHeight()),
                vehicle.getRotation(),
                vehicle.getMovementType()
            );
        }
    }

    private void clearCompletedAnimations() {
        List<String> vehiclesToRemove = new ArrayList<>();
        for (String vehicleId : activeAnimatedVehicles.keySet()) {
            boolean found = false;
            for (SimulationState.CrossingVehicle vehicle : currentState.getCrossingVehicles()) {
                if (vehicle.getId().equals(vehicleId)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                vehiclesToRemove.add(vehicleId);
            }
        }
        
        for (String vehicleId : vehiclesToRemove) {
            AnimatedVehicle vehicle = activeAnimatedVehicles.remove(vehicleId);
            if (vehicle != null) {
                vehicle.stopAnimation();
            }
        }
    }

    private void animateQueueAdvancement(TrafficLight.Direction direction, 
                                    List<SimulationState.QueuedVehicle> vehicles) {
        if (queueAnimationTimelines.get(direction) != null) {
            queueAnimationTimelines.get(direction).stop();
        }
        
        if (vehicles.isEmpty()) {
            currentQueuePositions.put(direction, new ArrayList<>());
            return;
        }
        
        List<Double[]> targetPositions = new ArrayList<>();
        double startX, startY;
        double dx = 0, dy = 0;
        
        switch (direction) {
            case NORTH:
                startX = getWidth()/2 - 10;
                startY = getHeight()/2 - 50;
                dy = -30;
                break;
            case EAST:
                startX = getWidth()/2 + 50;
                startY = getHeight()/2 - 10;
                dx = 30;
                break;
            case SOUTH:
                startX = getWidth()/2 + 10;
                startY = getHeight()/2 + 50;
                dy = 30;
                break;
            case WEST:
            default:
                startX = getWidth()/2 - 50;
                startY = getHeight()/2 + 10;
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
        
        List<Double[]> currentPositions = currentQueuePositions.get(direction);
        if (currentPositions.isEmpty()) {
            currentQueuePositions.put(direction, new ArrayList<>(targetPositions));
            return;
        }
        
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
        
        queueAnimationTimelines.put(direction, timeline);
        timeline.play();
    }
}