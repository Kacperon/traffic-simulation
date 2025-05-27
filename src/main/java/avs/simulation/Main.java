package avs.simulation;

import avs.simulation.model.Intersection;
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
            String controllerTypeArg = args.length >= 3 ? args[2] : null;
            
            try {
                System.out.println("Running simulation from file: " + inputFile);
                System.out.println("Saving results to: " + outputFile);
                
                // Create simulation
                Simulation simulation = new Simulation();
                
                // Set controller type if provided
                if (controllerTypeArg != null) {
                    Intersection.ControllerType controllerType = parseControllerType(controllerTypeArg);
                    System.out.println("Using controller type: " + controllerType);
                    simulation.setIntersectionControllerType(controllerType);
                }
                
                // Run the simulation
                simulation.runFromJsonFile(inputFile, outputFile);
                
                System.out.println("Simulation completed successfully.");
            } catch (IOException e) {
                System.err.println("Error running simulation from file: " + e.getMessage());
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
                System.err.println("Valid controller types: standard, priority, opposing");
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
        System.out.println("\nValid controller types: standard, priority, opposing");
    }
    
    /**
     * Parses the controller type string into the corresponding enum value
     */
    private static Intersection.ControllerType parseControllerType(String type) {
        return switch (type.toLowerCase()) {
            case "standard" -> Intersection.ControllerType.STANDARD;
            case "priority" -> Intersection.ControllerType.PRIORITY;
            case "opposing" -> Intersection.ControllerType.OPPOSING;
            default -> throw new IllegalArgumentException("Unknown controller type: " + type);
        };
    }
}