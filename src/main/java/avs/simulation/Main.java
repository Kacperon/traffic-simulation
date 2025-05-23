package avs.simulation;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Użycie: java -jar simulation.jar input.json output.json");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try {
            Simulation simulation = new Simulation();
            simulation.runFromJsonFile(inputFile, outputFile);
            System.out.println("Symulacja zakończona pomyślnie!");
        } catch (IOException e) {
            System.err.println("Błąd przetwarzania plików: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Błąd podczas symulacji: " + e.getMessage());
            e.printStackTrace();
        }
    }
}