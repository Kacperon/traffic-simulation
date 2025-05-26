package avs.visualization;

import avs.simulation.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;

public class IntersectionView extends Canvas {
    private GraphicsContext gc;
    private SimulationState currentState;

    public IntersectionView(double width, double height) {
        super(width, height);
        gc = getGraphicsContext2D();
        currentState = new SimulationState();
    }

    public void update(SimulationState state) {
        this.currentState = state;
        draw();
    }

    private void draw() {
        double width = getWidth();
        double height = getHeight();
        
        // Clear canvas
        gc.clearRect(0, 0, width, height);
        
        // Draw background (road)
        gc.setFill(Color.DARKGRAY);
        
        // Horizontal road
        gc.fillRect(0, height/2 - 30, width, 60);
        
        // Vertical road
        gc.fillRect(width/2 - 30, 0, 60, height);
        
        // Road markings
        gc.setStroke(Color.WHITE);
        gc.setLineDashes(5); 
        gc.setLineWidth(2);
        
        // Center line horizontal
        gc.strokeLine(0, height/2, width, height/2);
        
        // Center line vertical
        gc.strokeLine(width/2, 0, width/2, height);
        
        gc.setLineDashes();
        
        // Draw intersection center
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(width/2 - 30, height/2 - 30, 60, 60);
        
        // Draw traffic lights - REPOSITIONED to the right side of each lane
        // NORTH direction (approaching from top) - light on the right side (east)
        drawTrafficLight(TrafficLight.Direction.NORTH, width/2 + 35, height/2 - 65, 0);
        
        // EAST direction (approaching from right) - light on the right side (south)
        drawTrafficLight(TrafficLight.Direction.EAST, width/2 + 65, height/2 + 35, 90);
        
        // SOUTH direction (approaching from bottom) - light on the right side (west)
        drawTrafficLight(TrafficLight.Direction.SOUTH, width/2 - 35, height/2 + 65, 180);
        
        // WEST direction (approaching from left) - light on the right side (north)
        drawTrafficLight(TrafficLight.Direction.WEST, width/2 - 65, height/2 - 35, 270);
        
        // Draw vehicles in queues
        drawVehicleQueue(TrafficLight.Direction.NORTH, width/2 + 10, height/2 - 100);
        drawVehicleQueue(TrafficLight.Direction.EAST, width/2 + 100, height/2 + 10);
        drawVehicleQueue(TrafficLight.Direction.SOUTH, width/2 - 10, height/2 + 100);
        drawVehicleQueue(TrafficLight.Direction.WEST, width/2 - 100, height/2 - 10);

        // Draw last vehicles that crossed the intersection
        drawCrossingVehicles();
    }

    private void drawTrafficLight(TrafficLight.Direction direction, double x, double y, double rotation) {
        TrafficLight.LightState state = currentState.getLightState(direction);
        
        // Save the current transformation
        gc.save();
        
        // Translate to the position where we want to draw, then rotate
        gc.translate(x, y);
        gc.rotate(rotation);
        
        // Draw traffic light box - vertical orientation
        gc.setFill(Color.BLACK);
        gc.fillRect(-10, -30, 20, 60);
        
        // Draw light indicators (relative to the rotated coordinate system)
        double redY = -20;
        double yellowY = 0;
        double greenY = 20;
        
        // Red light
        gc.setFill(state == TrafficLight.LightState.RED || state == TrafficLight.LightState.RED_YELLOW ? 
                Color.RED : Color.rgb(100, 0, 0));
        gc.fillOval(-5, redY - 5, 10, 10);
        
        // Yellow light
        gc.setFill(state == TrafficLight.LightState.YELLOW || state == TrafficLight.LightState.RED_YELLOW ? 
                Color.YELLOW : Color.rgb(100, 100, 0));
        gc.fillOval(-5, yellowY - 5, 10, 10);
        
        // Green light
        gc.setFill(state == TrafficLight.LightState.GREEN ? Color.LIMEGREEN : Color.rgb(0, 100, 0));
        gc.fillOval(-5, greenY - 5, 10, 10);
        
        // Draw direction label
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(direction.toString(), 0, -40);
        
        // Restore the original transformation
        gc.restore();
    }

    private void drawVehicleQueue(TrafficLight.Direction direction, double startX, double startY) {
        List<String> vehicles = currentState.getVehicleQueue(direction);
        if (vehicles == null || vehicles.isEmpty()) return;

        gc.setFill(Color.BLUE);
        gc.setTextAlign(TextAlignment.CENTER);

        // Direction-specific layout
        double dx = 0, dy = 0;
        switch (direction) {
            case NORTH: dx = 0; dy = -20; break;
            case EAST: dx = 20; dy = 0; break;
            case SOUTH: dx = 0; dy = 20; break;
            case WEST: dx = -20; dy = 0; break;
        }

        for (int i = 0; i < vehicles.size(); i++) {
            double x = startX + i * dx;
            double y = startY + i * dy;

            // Draw vehicle
            gc.fillRect(x - 8, y - 8, 16, 16);
            gc.setFill(Color.WHITE);
            gc.fillText(vehicles.get(i), x, y + 4);
            gc.setFill(Color.BLUE);
        }
    }

    private void drawCrossingVehicles() {
        List<String> vehicles = currentState.getLastCrossedVehicles();
        if (vehicles == null || vehicles.isEmpty()) return;

        gc.setFill(Color.GREEN);
        gc.setTextAlign(TextAlignment.CENTER);

        // Display crossing vehicles in the center
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;

        gc.setFont(new Font(14));
        for (int i = 0; i < Math.min(vehicles.size(), 3); i++) {
            gc.fillText("✓ " + vehicles.get(i), centerX, centerY - 10 + i * 20);
        }
    }
}