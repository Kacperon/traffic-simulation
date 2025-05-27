package avs.simulation.UI;

import avs.simulation.Simulation;
import avs.simulation.UI.utils.SimulationUIHelper;
import avs.simulation.model.Intersection;
import avs.simulation.model.TrafficLight;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class SimulationController implements Initializable {
    
    @FXML private BorderPane simulationContainer;
    @FXML private Button addNorthButton;
    @FXML private Button addEastButton;
    @FXML private Button addSouthButton;
    @FXML private Button addWestButton;
    @FXML private Button stepButton;
    @FXML private Button playPauseButton;
    @FXML private Button clearButton;
    @FXML private ComboBox<String> controllerTypeComboBox;
    
    private IntersectionView intersectionView;
    private Simulation simulation;
    private Timeline timeline;
    private boolean isRunning = false;
    private final static float TIMESTEP = 0.6f;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Create the IntersectionView programmatically
        intersectionView = new IntersectionView(400, 400);
        simulationContainer.setCenter(intersectionView);
        
        // Create simulation
        simulation = new Simulation();
        simulation.setVisualMode(true);
        
        // Set up the timeline for automated simulation using helper
        timeline = SimulationUIHelper.createSimulationTimeline(simulation, TIMESTEP);
        
        // Register simulation listener for visualization updates
        simulation.addSimulationListener(simulationState -> {
            Platform.runLater(() -> intersectionView.update(simulationState));
        });
        
        // Initial update
        intersectionView.update(simulation.getCurrentState());
        
        // Set up controller type combo box using helper
        SimulationUIHelper.setupControllerTypeComboBox(controllerTypeComboBox, "Standard");
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
        if (isRunning) {
            timeline.stop();
            isRunning = false;
            playPauseButton.setText("Play");
        }
        
        simulation = new Simulation();
        simulation.setVisualMode(true);
        simulation.addSimulationListener(simulationState -> {
            Platform.runLater(() -> intersectionView.update(simulationState));
        });
        intersectionView.update(simulation.getCurrentState());
    }
    
    @FXML
    public void changeControllerType(ActionEvent event) {
        String selectedType = controllerTypeComboBox.getValue();
        Intersection.ControllerType type = SimulationUIHelper.getControllerTypeFromString(selectedType);
        
        simulation.setIntersectionControllerType(type);
        System.out.println("Switched to " + selectedType + " traffic light controller");
    }
    
    public void setInitialControllerType(Intersection.ControllerType type) {
        // Update UI to match actual controller type
        controllerTypeComboBox.setValue(type == Intersection.ControllerType.PRIORITY ? "Priority" : "Standard");
        
        // Set the controller type in the simulation
        if (simulation != null) {
            simulation.setIntersectionControllerType(type);
        }
    }
    
    private void addRandomVehicle(TrafficLight.Direction startDirection) {
        // Use helper methods for generating random vehicle data
        TrafficLight.Direction endDirection = SimulationUIHelper.getRandomEndDirection(startDirection);
        String vehicleId = SimulationUIHelper.generateRandomVehicleId();
        
        simulation.addVehicle(vehicleId, startDirection, endDirection);
    }
}