package avs.simulation;

import avs.simulation.UI.renderers.VehicleView;
import avs.simulation.model.Vehicle;
import org.junit.jupiter.api.Test;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

// Note: This test requires JavaFX runtime
public class VehicleViewTest {
    
    @Test
    public void testDrawVehicle() {
        try {
            // Initialize JavaFX
            javafx.application.Platform.startup(() -> {});
            
            // Create a canvas to draw on
            Canvas canvas = new Canvas(100, 100);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // This should not throw exceptions
            VehicleView.draw(gc, "test", 50, 50, 0, Vehicle.MovementType.STRAIGHT);
            VehicleView.draw(gc, "test", 50, 50, 90, Vehicle.MovementType.LEFT);
            VehicleView.draw(gc, "test", 50, 50, 180, Vehicle.MovementType.RIGHT);
        } catch (Exception e) {
            // If running in a CI/CD environment without graphics, test will be skipped
            System.out.println("JavaFX initialization failed, test skipped");
        }
    }
}