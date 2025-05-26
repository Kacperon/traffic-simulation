package avs.simulation;

import java.io.IOException;
import avs.simulation.UI.SimulationVisualizer;

public class Main {
    public static void main(String[] args) {
        // If no arguments are provided, launch visualization
        if (args.length == 0) {
            System.out.println("Starting traffic simulation visualization...");
            SimulationVisualizer.main(args);
            return;
        }
        
        // Handle "--visual" command explicitly 
        if (args.length == 1 && args[0].equals("--visual")) {
            System.out.println("Starting traffic simulation visualization...");
            SimulationVisualizer.main(args);
            return;
        }

        // Console mode with input/output files
        if (args.length >= 2) {
            String inputFile = args[0];
            String outputFile = args[1];

            try {
                Simulation simulation = new Simulation();
                simulation.runFromJsonFile(inputFile, outputFile);
                System.out.println("Simulation completed successfully!");
            } catch (IOException e) {
                System.err.println("File processing error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Simulation error: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }
        
        // If we get here, show usage help
        System.out.println("Usage:");
        System.out.println("  java -jar simulation.jar                    (for visual mode)");
        System.out.println("  java -jar simulation.jar --visual           (for visual mode)");
        System.out.println("  java -jar simulation.jar <input.json> <output.json>   (for console mode)");
    }
}