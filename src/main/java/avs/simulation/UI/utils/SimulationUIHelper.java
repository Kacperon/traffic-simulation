package avs.simulation.UI.utils;

import avs.simulation.Simulation;
import avs.simulation.model.Intersection;
import avs.simulation.model.TrafficLight;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.util.Duration;

/**
 * Helper class for common UI setup and configuration tasks
 */
public class SimulationUIHelper {
    
    /**
     * Configure the controller type combo box
     */
    public static void setupControllerTypeComboBox(ComboBox<String> comboBox, String defaultValue) {
        ObservableList<String> controllerTypes = FXCollections.observableArrayList(
            "Standard", "Priority", "Opposing"
        );
        comboBox.setItems(controllerTypes);
        comboBox.setValue(defaultValue != null ? defaultValue : "Standard");
    }
    
    /**
     * Create and configure a simulation timeline
     */
    public static Timeline createSimulationTimeline(Simulation simulation, float timeStep) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(timeStep), e -> {
            simulation.performSimulationStep();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }
    
    /**
     * Generate a vehicle ID
     */
    public static String generateRandomVehicleId() {
        return "V" + (int)(Math.random() * 1000);
    }
    
    /**
     * Get a random end direction that's different from the start direction
     */
    public static TrafficLight.Direction getRandomEndDirection(TrafficLight.Direction startDirection) {
        TrafficLight.Direction[] directions = TrafficLight.Direction.values();
        TrafficLight.Direction endDirection;
        
        do {
            endDirection = directions[(int)(Math.random() * directions.length)];
        } while (endDirection == startDirection);
        
        return endDirection;
    }
    
    /**
     * Convert controller type string to enum
     */
    public static Intersection.ControllerType getControllerTypeFromString(String controllerTypeString) {
        switch (controllerTypeString) {
            case "Priority":
                return Intersection.ControllerType.PRIORITY;
//            case "Opposing":
//                return Intersection.ControllerType.OPPOSING;
            default:
                return Intersection.ControllerType.STANDARD;
        }
    }
}