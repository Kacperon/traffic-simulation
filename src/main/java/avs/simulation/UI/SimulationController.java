package avs.simulation.UI;

import avs.simulation.Simulation;
import avs.simulation.model.TrafficLight;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SimulationController implements Initializable {
    
    @FXML private IntersectionView intersectionView;
    @FXML private Button addNorthButton;
    @FXML private Button addEastButton;
    @FXML private Button addSouthButton;
    @FXML private Button addWestButton;
    @FXML private Button stepButton;
    @FXML private Button playPauseButton;
    @FXML private Button clearButton;
    
    private Simulation simulation;
    private Timeline timeline;
    private boolean isRunning = false;
    private final static float TIMESTEP = 0.7f;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Create simulation
        simulation = new Simulation();
        simulation.setVisualMode(true);
        
        // Set up simulation listener for visualization updates
        simulation.addSimulationListener(simulationState -> {
            Platform.runLater(() -> intersectionView.update(simulationState));
        });
        
        // Setup the simulation timeline
        timeline = new Timeline(new KeyFrame(Duration.seconds(TIMESTEP), e -> {
            simulation.performSimulationStep();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        
        // Initial update
        intersectionView.update(simulation.getCurrentState());
    }
    
    @FXML
    private void addNorthVehicle() {
        addRandomVehicle(TrafficLight.Direction.NORTH);
    }
    
    @FXML
    private void addEastVehicle() {
        addRandomVehicle(TrafficLight.Direction.EAST);
    }
    
    @FXML
    private void addSouthVehicle() {
        addRandomVehicle(TrafficLight.Direction.SOUTH);
    }
    
    @FXML
    private void addWestVehicle() {
        addRandomVehicle(TrafficLight.Direction.WEST);
    }
    
    @FXML
    private void step() {
        simulation.performSimulationStep();
    }
    
    @FXML
    private void toggleSimulation() {
        if (isRunning) {
            timeline.stop();
            isRunning = false;
            playPauseButton.setText("Play");
        } else {
            timeline.play();
            isRunning = true;
            playPauseButton.setText("Pause");
        }
    }
    
    @FXML
    private void clearSimulation() {
        // Stop simulation if running
        if (isRunning) {
            timeline.stop();
            isRunning = false;
            playPauseButton.setText("Play");
        }
        
        // Reset simulation
        simulation = new Simulation();
        simulation.setVisualMode(true);
        
        // Re-register listener
        simulation.addSimulationListener(simulationState -> {
            Platform.runLater(() -> intersectionView.update(simulationState));
        });
        
        // Update view
        intersectionView.update(simulation.getCurrentState());
    }
    
    private void addRandomVehicle(TrafficLight.Direction startDirection) {
        // Get random end direction that makes sense
        TrafficLight.Direction[] directions = TrafficLight.Direction.values();
        TrafficLight.Direction endDirection;
        do {
            endDirection = directions[(int)(Math.random() * directions.length)];
        } while (endDirection == startDirection); // Don't go back in the same direction
        
        // Generate random vehicle ID
        String vehicleId = "V" + (int)(Math.random() * 1000);
        
        // Add to simulation
        simulation.addVehicle(vehicleId, startDirection, endDirection);
    }
}