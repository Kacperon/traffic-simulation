package avs.simulation.UI;

import avs.simulation.model.TrafficLight;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TrafficLightView {
    
    public static void draw(GraphicsContext gc, TrafficLight.Direction direction,
                           TrafficLight.LightState state, double x, double y, int rotation) {
        
        gc.save();
        gc.translate(x, y);
        gc.rotate(rotation);
        
        // Draw light housing
        gc.setFill(Color.BLACK);
        gc.fillRect(-10, -25, 20, 50);
        
        // Red light
        gc.setFill(state == TrafficLight.LightState.RED || state == TrafficLight.LightState.RED_YELLOW ? 
                  Color.RED : Color.rgb(100, 0, 0));
        gc.fillOval(-5, -20, 10, 10);
        
        // Yellow light
        gc.setFill(state == TrafficLight.LightState.YELLOW || state == TrafficLight.LightState.RED_YELLOW ? 
                  Color.YELLOW : Color.rgb(100, 100, 0));
        gc.fillOval(-5, -5, 10, 10);
        
        // Green light
        gc.setFill(state == TrafficLight.LightState.GREEN ? 
                  Color.LIME : Color.rgb(0, 100, 0));
        gc.fillOval(-5, 10, 10, 10);
        
        gc.restore();
    }
}