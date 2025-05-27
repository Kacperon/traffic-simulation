package avs.simulation.UI.renderers;

import avs.simulation.model.Vehicle;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class VehicleView {
    
    public static void draw(GraphicsContext gc, String id, double x, double y, 
                           double rotation, Vehicle.MovementType type) {
        gc.save();
        
        // Translate and rotate
        gc.translate(x, y);
        gc.rotate(rotation);
        
        // Choose color based on movement type
        Color vehicleColor;
        switch (type) {
            case LEFT: vehicleColor = Color.ORANGE; break;
            case RIGHT: vehicleColor = Color.CYAN; break;
            default: vehicleColor = Color.LIME; break; // STRAIGHT
        }
        
        // Draw vehicle body
        gc.setFill(vehicleColor);
        gc.fillRect(-10, -6, 20, 12);
        
        // Draw direction triangle
        double[] triangleX = {10, 15, 10};
        double[] triangleY = {-6, 0, 6};
        gc.fillPolygon(triangleX, triangleY, 3);
        
        // Draw vehicle ID
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(id, 0, 3);
        
        gc.restore();
    }
}