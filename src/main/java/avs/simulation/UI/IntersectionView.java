package avs.simulation.UI;

import avs.simulation.UI.animation.QueueAnimationManager;
import avs.simulation.UI.animation.VehicleAnimationManager;
import avs.simulation.model.TrafficLight;
import javafx.scene.canvas.Canvas;
import javafx.animation.AnimationTimer;

import java.util.Set;

/**
 * Main view class for the intersection that coordinates the different components
 */
public class IntersectionView extends Canvas {
    private SimulationState currentState;
    private AnimationTimer animationTimer;
    
    // Component managers
    private final avs.simulation.UI.renderers.IntersectionView renderer;
    private final VehicleAnimationManager vehicleAnimationManager;
    private final QueueAnimationManager queueAnimationManager;
    
    public IntersectionView(double width, double height) {
        super(width, height);
        
        // Initialize state
        currentState = new SimulationState();
        
        // Initialize components
        renderer = new avs.simulation.UI.renderers.IntersectionView();
        vehicleAnimationManager = new VehicleAnimationManager(
            vehicleId -> currentState.removeVehicleFromAnimation(vehicleId)
        );
        queueAnimationManager = new QueueAnimationManager();
        
        // Set up animation timer for continuous rendering
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                redrawCanvas();
            }
        };
        animationTimer.start();
    }

    /**
     * Update the view with new simulation state
     */
    public void update(SimulationState state) {
        this.currentState = state;
        
        // Update animated vehicles
        vehicleAnimationManager.updateAnimatedVehicles(state);
        
        // Now get the changed directions separately
        Set<TrafficLight.Direction> changedDirections = vehicleAnimationManager.getChangedDirections();
        
        // Update queue animations for any changed directions
        for (TrafficLight.Direction dir : changedDirections) {
            queueAnimationManager.animateQueueAdvancement(
                dir, state.getVehicleQueue(dir), getWidth(), getHeight());
        }
    }

    /**
     * Redraw the entire canvas
     */
    private void redrawCanvas() {
        var gc = getGraphicsContext2D();
        double width = getWidth();
        double height = getHeight();

        // Draw static elements
        renderer.drawBackground(gc, width, height);
        renderer.drawTrafficLights(gc, currentState, width, height);
        
        // Draw dynamic elements
        queueAnimationManager.drawVehicleQueues(gc, currentState, width, height);
        vehicleAnimationManager.drawAnimatedVehicles(gc, width, height);
    }
}