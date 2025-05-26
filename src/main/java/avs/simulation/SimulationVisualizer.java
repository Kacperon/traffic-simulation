package avs.visualization;

import avs.simulation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class SimulationVisualizer extends Application {
    private Simulation simulation;
    private avs.simulation.IntersectionView intersectionView;
    private Timeline timeline;
    private boolean isRunning = false;

    @Override
    public void start(Stage primaryStage) {
        // Create simulation
        simulation = new Simulation();
        simulation.setVisualMode(true);

        // Create visual components
        BorderPane root = new BorderPane();
        intersectionView = new avs.simulation.IntersectionView(400, 400);

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

        // Set actions
        addNorthButton.setOnAction(e -> addRandomVehicle(TrafficLight.Direction.NORTH));
        addEastButton.setOnAction(e -> addRandomVehicle(TrafficLight.Direction.EAST));
        addSouthButton.setOnAction(e -> addRandomVehicle(TrafficLight.Direction.SOUTH));
        addWestButton.setOnAction(e -> addRandomVehicle(TrafficLight.Direction.WEST));
        stepButton.setOnAction(e -> simulation.performSimulationStep());
        playButton.setOnAction(e -> toggleSimulation());

        // Layout
        root.setCenter(intersectionView);
        root.setBottom(controls);

        // Create animation timeline
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
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