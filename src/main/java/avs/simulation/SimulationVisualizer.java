package avs.simulation;

import avs.simulation.UI.SimulationController;
import avs.simulation.model.Intersection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Map;

public class SimulationVisualizer extends Application {
    private Intersection.ControllerType initialControllerType = Intersection.ControllerType.STANDARD;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Parse parameters if provided
            parseParameters();

            // Ensure the FXML file can be found
            URL fxmlUrl = getClass().getResource("/fxml/SimulationView.fxml");

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

    public static void main(String[] args) {
        launch(args);
    }
}