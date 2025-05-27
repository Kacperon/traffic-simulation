package avs.simulation.UI.animation;

import avs.simulation.UI.AnimatedVehicle;
import avs.simulation.UI.SimulationState;
import avs.simulation.UI.renderers.VehicleView;
import avs.simulation.model.LightControlers.TrafficLight;
import javafx.scene.canvas.GraphicsContext;

import java.util.*;
import java.util.function.Consumer;

/**
 * Manages animated vehicles in the intersection
 */
public class VehicleAnimationManager {
    private Map<String, AnimatedVehicle> activeAnimatedVehicles = new HashMap<>();
    private Map<TrafficLight.Direction, Queue<SimulationState.CrossingVehicle>> pendingVehicles = new HashMap<>();
    private final Consumer<String> onVehicleRemoved;
    
    public VehicleAnimationManager(Consumer<String> onVehicleRemoved) {
        this.onVehicleRemoved = onVehicleRemoved;
        
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            pendingVehicles.put(dir, new LinkedList<>());
        }
    }
    
    /**
     * Update animated vehicles based on the current simulation state
     */
    public void updateAnimatedVehicles(SimulationState state) {
        clearCompletedAnimations(state);
        
        List<SimulationState.CrossingVehicle> crossingVehicles = state.getCrossingVehicles();
        if (crossingVehicles == null || crossingVehicles.isEmpty()) return;
        
        // Clear pending vehicles and track changed directions
        Set<TrafficLight.Direction> changedDirections = new HashSet<>();
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            Queue<SimulationState.CrossingVehicle> oldQueue = pendingVehicles.get(dir);
            int oldSize = oldQueue.size();
            
            oldQueue.clear();
            if (oldSize > 0) {
                changedDirections.add(dir);
            }
        }

        // Process crossing vehicles
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
    }
    
    /**
     * Start animation for a specific vehicle
     */
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
                if (onVehicleRemoved != null) {
                    onVehicleRemoved.accept(vehicleId);
                }
            }
        );
        
        animVehicle.animate(1.5);
        activeAnimatedVehicles.put(vehicle.getId(), animVehicle);
    }
    
    /**
     * Remove completed animations
     */
    private void clearCompletedAnimations(SimulationState state) {
        List<String> vehiclesToRemove = new ArrayList<>();
        for (String vehicleId : activeAnimatedVehicles.keySet()) {
            boolean found = false;
            for (SimulationState.CrossingVehicle vehicle : state.getCrossingVehicles()) {
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
    
    /**
     * Draw all active animated vehicles
     */
    public void drawAnimatedVehicles(GraphicsContext gc, double width, double height) {
        for (AnimatedVehicle vehicle : activeAnimatedVehicles.values()) {
            VehicleView.draw(
                gc, 
                vehicle.getId(),
                vehicle.getDrawX(width),
                vehicle.getDrawY(height),
                vehicle.getRotation(),
                vehicle.getMovementType()
            );
        }
    }
    
    /**
     * Get all directions that have had their pending queues changed
     */
    public Set<TrafficLight.Direction> getChangedDirections() {
        Set<TrafficLight.Direction> changedDirections = new HashSet<>();
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            if (!pendingVehicles.get(dir).isEmpty()) {
                changedDirections.add(dir);
            }
        }
        return changedDirections;
    }
}