package avs.simulation;

import avs.simulation.UI.SimulationVisualizer;

public class Main {
    public static void main(String[] args) {
        // Parse controller type if provided
        String controllerType = null;
        for (String arg : args) {
            if (arg.startsWith("--controller=")) {
                controllerType = arg.substring("--controller=".length());
                // Remove this arg so it doesn't interfere with other processing
                args = java.util.Arrays.stream(args)
                        .filter(a -> !a.startsWith("--controller="))
                        .toArray(String[]::new);
                break;
            }
        }
        
        if (controllerType != null) {
            System.setProperty("controller.type", controllerType);
        }

        // If no arguments are provided, launch visualization
        if (args.length == 0) {
            System.out.println("Starting traffic simulation visualization...");
            SimulationVisualizer.main(args);
            return;
        }
        
        // Rest of the existing code...
    }
}