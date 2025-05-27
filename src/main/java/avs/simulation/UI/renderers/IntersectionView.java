package avs.simulation.UI.renderers;

import avs.simulation.UI.SimulationState;
import avs.simulation.model.LightControlers.TrafficLight;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Handles rendering of the intersection background and static elements
 */
public class IntersectionView {
    
    /**
     * Draw the intersection background including roads
     */
    public void drawBackground(GraphicsContext gc, double width, double height) {
        gc.clearRect(0, 0, width, height);
        
        // Draw roads
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, height/2 - 30, width, 60);
        gc.fillRect(width/2 - 30, 0, 60, height);
        
        // Draw center lines
        gc.setStroke(Color.WHITE);
        gc.setLineDashes(5); 
        gc.setLineWidth(2);
        gc.strokeLine(0, height/2, width, height/2);
        gc.strokeLine(width/2, 0, width/2, height);
        gc.setLineDashes();
        
        // Draw intersection center
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(width/2 - 30, height/2 - 30, 60, 60);
    }
    
    /**
     * Draw traffic lights for all directions
     */
    public void drawTrafficLights(GraphicsContext gc, SimulationState state, double width, double height) {
        TrafficLightView.draw(gc, TrafficLight.Direction.NORTH,
                             state.getLightState(TrafficLight.Direction.NORTH), 
                             width/2 - 35, height/2 - 65, 0);
        
        TrafficLightView.draw(gc, TrafficLight.Direction.EAST, 
                             state.getLightState(TrafficLight.Direction.EAST), 
                             width/2 + 65, height/2 - 35, 90);
        
        TrafficLightView.draw(gc, TrafficLight.Direction.SOUTH, 
                             state.getLightState(TrafficLight.Direction.SOUTH), 
                             width/2 + 35, height/2 + 65, 180);
        
        TrafficLightView.draw(gc, TrafficLight.Direction.WEST, 
                             state.getLightState(TrafficLight.Direction.WEST), 
                             width/2 - 65, height/2 + 35, 270);
    }
}