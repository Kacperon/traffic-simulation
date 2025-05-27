package avs.simulation.UI;

import avs.simulation.Simulation;
import avs.simulation.model.Intersection;
import avs.simulation.model.TrafficLight;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SimulationVisualizer extends Application {
    private Simulation simulation;
    private IntersectionView intersectionView;
    private Timeline timeline;
    private boolean isRunning = false;
    private final static float TIMESTEP = 0.7f;
    private Intersection.ControllerType initialControllerType = Intersection.ControllerType.STANDARD;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Parse parameters if provided
            parseParameters();

            // Ensure the FXML file can be found
            URL fxmlUrl = getClass().getResource("/fxml/SimulationView.fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file at: /fxml/SimulationView.fxml");
                // Fall back to non-FXML UI if needed
                createSimpleUI(primaryStage);
                return;
            }

            // Load FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Get controller and set initial controller type if specified
            SimulationController controller = loader.getController();
            controller.setInitialControllerType(initialControllerType);

            // Set up scene
            Scene scene = new Scene(root, 800, 500);

            // Add CSS if available
            URL cssUrl = getClass().getResource("/css/simulation-styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // Configure and show stage
            primaryStage.setTitle("Traffic Simulation");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            // Fall back to non-FXML UI if FXML loading fails
            createSimpleUI(primaryStage);
        }
    }

    private void parseParameters() {
        Parameters params = getParameters();
        if (params != null) {
            Map<String, String> namedParams = params.getNamed();

            // Check for controller type parameter
            String controllerType = namedParams.get("controller");
            if ("priority".equalsIgnoreCase(controllerType)) {
                initialControllerType = Intersection.ControllerType.PRIORITY;
            }
        }
    }

    // Fallback method to create UI programmatically if FXML loading fails
    private void createSimpleUI(Stage primaryStage) {
        // Create simulation
        simulation = new Simulation();
        simulation.setVisualMode(true);

        // Create visual components
        BorderPane root = new BorderPane();
        intersectionView = new IntersectionView(400, 400);

        // Create controls
        HBox controls = new HBox(10);
        Button addNorthButton = new Button("Add North");
        Button addEastButton = new Button("Add East");
        Button addSouthButton = new Button("Add South");
        Button addWestButton = new Button("Add West");
        Button stepButton = new Button("Step");
        Button playButton = new Button("Play/Pause");

        controls.getChildren().addAll(addNorthButton, addEastButton,
                addSouthButton, addWestButton,
                stepButton, playButton);

        addNorthButton.setOnAction(e -> addRandomVehicle(TrafficLight.Direction.NORTH));
        addEastButton.setOnAction(e -> addRandomVehicle(TrafficLight.Direction.EAST));
        addSouthButton.setOnAction(e -> addRandomVehicle(TrafficLight.Direction.SOUTH));
        addWestButton.setOnAction(e -> addRandomVehicle(TrafficLight.Direction.WEST));
        stepButton.setOnAction(e -> simulation.performSimulationStep());
        playButton.setOnAction(e -> toggleSimulation());

        root.setCenter(intersectionView);
        root.setBottom(controls);

        timeline = new Timeline(new KeyFrame(Duration.seconds(TIMESTEP), e -> {
            simulation.performSimulationStep();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);

        // Register simulation listener for visualization updates
        simulation.addSimulationListener(simulationState -> {
            Platform.runLater(() -> intersectionView.update(simulationState));
        });

        // Set up scene and show
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setTitle("Traffic Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial update
        intersectionView.update(simulation.getCurrentState());
    }

    private void toggleSimulation() {
        if (isRunning) {
            timeline.stop();
            isRunning = false;
        } else {
            timeline.play();
            isRunning = true;
        }
    }

    private void addRandomVehicle(TrafficLight.Direction startDirection) {
        // Get random end direction that makes sense (usually not the same as start)
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

    public static void main(String[] args) {
        launch(args);
    }
}