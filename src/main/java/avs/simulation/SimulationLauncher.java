package avs.simulation;

import avs.visualization.SimulationVisualizer;

public class SimulationLauncher {
    public static void main(String[] args) {
        // Check if we should run in visual mode
        if (args.length > 0 && args[0].equals("--visual")) {
            // Run in visual mode
            SimulationVisualizer.main(args);
        } else if (args.length >= 2) {
            // Run in console mode with input/output files
            Main.main(args);
        } else {
            System.out.println("Usage:");
            System.out.println("  java -jar simulation.jar --visual                 (for visual mode)");
            System.out.println("  java -jar simulation.jar input.json output.json   (for console mode)");
        }
    }
}