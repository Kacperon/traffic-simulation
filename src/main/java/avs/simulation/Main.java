package avs.simulation;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("vis")) {
            // Visualization mode
            System.out.println("Starting traffic simulation visualization...");
            SimulationVisualizer.main(new String[0]);
            return;
        }
        
        if (args.length >= 2) {
            // File processing mode
            String inputFile = args[0];
            String outputFile = args[1];
            String controllerType = args.length >= 3 ? args[2] : null;
            
            if (controllerType != null) {
                System.setProperty("controller.type", controllerType);
            }
            
            try {
                System.out.println("Running simulation from file: " + inputFile);
                System.out.println("Saving results to: " + outputFile);
                if (controllerType != null) {
                    System.out.println("Using controller type: " + controllerType);
                }
                
                Simulation simulation = new Simulation();
                simulation.runFromJsonFile(inputFile, outputFile);
                
                System.out.println("Simulation completed successfully.");
            } catch (IOException e) {
                System.err.println("Error running simulation from file: " + e.getMessage());

            }
            return;
        }
        
        // If we get here, show usage info
        System.out.println("Usage:");
        System.out.println("  java -jar simulation.jar vis");
        System.out.println("  java -jar simulation.jar <inputFile.json> <outputFile.json> [controllerType]");
        System.out.println("Examples:");
        System.out.println("  java -jar simulation.jar vis");
        System.out.println("  java -jar simulation.jar input.json output.json");
        System.out.println("  java -jar simulation.jar input.json output.json opposing");
    }
}