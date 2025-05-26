package avs.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Simulation {
    private Intersection intersection;
    private Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues;
    private List<Vehicle> completedVehicles;
    private List<StepStatus> stepStatuses;

    private boolean visualMode = false;
    private List<Consumer<SimulationState>> simulationListeners = new CopyOnWriteArrayList<>();
    private SimulationState currentState = new SimulationState();

    private AnimationTimer timer;
    private boolean running = false;
    private long lastUpdate = 0;
    private long updateInterval = 500_000_000; // 0.5 sekundy w nanosekundach

    private SimulationController controller;

    public Simulation() {
        this.intersection = new Intersection();
        this.vehicleQueues = new HashMap<>();
        this.completedVehicles = new ArrayList<>();
        this.stepStatuses = new ArrayList<>();

        // Inicjalizacja kolejek pojazdów dla każdego kierunku
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            vehicleQueues.put(direction, new LinkedList<>());
        }

        // Inicjalizacja stanu
        currentState = new SimulationState();

        // Inicjalizacja timera do aktualizacji symulacji
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0 || (now - lastUpdate) >= updateInterval) {
                    update();
                    lastUpdate = now;
                }
            }
        };
    }

    public void runFromJsonFile(String inputFile, String outputFile) throws IOException {
        // Parsowanie pliku wejściowego JSON
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> input = mapper.readValue(new File(inputFile), Map.class);

        List<Map<String, Object>> commandsData = (List<Map<String, Object>>) input.get("commands");
        List<Command> commands = parseCommands(commandsData);

        // Wykonanie komend
        for (Command command : commands) {
            executeCommand(command);
        }

        // Zapisanie wyników do pliku wyjściowego
        generateOutputFile(outputFile);
    }

    private List<Command> parseCommands(List<Map<String, Object>> commandsData) {
        List<Command> commands = new ArrayList<>();

        for (Map<String, Object> commandData : commandsData) {
            String type = (String) commandData.get("type");

            if ("addVehicle".equals(type)) {
                String vehicleId = (String) commandData.get("vehicleId");
                TrafficLight.Direction startRoad = parseDirection((String) commandData.get("startRoad"));
                TrafficLight.Direction endRoad = parseDirection((String) commandData.get("endRoad"));

                commands.add(new Command(Command.CommandType.ADD_VEHICLE, vehicleId, startRoad, endRoad));
            } else if ("step".equals(type)) {
                commands.add(new Command(Command.CommandType.STEP));
            }
        }

        return commands;
    }

    private TrafficLight.Direction parseDirection(String direction) {
        switch (direction.toLowerCase()) {
            case "north": return TrafficLight.Direction.NORTH;
            case "east": return TrafficLight.Direction.EAST;
            case "south": return TrafficLight.Direction.SOUTH;
            case "west": return TrafficLight.Direction.WEST;
            default: throw new IllegalArgumentException("Nieznany kierunek: " + direction);
        }
    }

    private void executeCommand(Command command) {
        switch (command.getType()) {
            case ADD_VEHICLE:
                addVehicle(command.getVehicleId(), command.getStartRoad(), command.getEndRoad());
                break;
            case STEP:
                performSimulationStep();
                break;
        }
    }

    public void performSimulationStep() {
        // Utworzenie nowego statusu kroku
        StepStatus stepStatus = new StepStatus();

        // Aktualizacja stanu świateł
        intersection.update(vehicleQueues);

        // Przepuść pojazdy przez skrzyżowanie
        processVehicles(stepStatus);

        // Zaktualizuj czasy oczekiwania dla wszystkich pojazdów w kolejkach
        updateWaitingVehicles();

        // Dodaj status kroku do listy
        stepStatuses.add(stepStatus);

        if (visualMode) {
            updateSimulationState(stepStatus);
        }

        // Wizualizacja w terminalu
        visualizeIntersection(stepStatuses.size(), stepStatus);
    }

    private void processVehicles(StepStatus stepStatus) {
        List<TrafficLight.Direction> greenDirections = intersection.getCurrentGreenDirections();

        // Przepuść pojazdy dla wszystkich kierunków z zielonym światłem
        for (TrafficLight.Direction greenDirection : greenDirections) {
            Queue<Vehicle> queue = vehicleQueues.get(greenDirection);

            // ZMIANA: Zmieniamy limit z 2 na 1 pojazd na krok 
            int vehiclesToProcess = Math.min(queue.size(), 1); // max 1 pojazd na krok (było 2)

            for (int i = 0; i < vehiclesToProcess; i++) {
                if (!queue.isEmpty()) {
                    Vehicle vehicle = queue.poll();
                    
                    // Zmień stan pojazdu na "przejeżdżający"
                    vehicle.startCrossing();
                    
                    // Dodaj do listy ukończonych dopiero po animacji
                    completedVehicles.add(vehicle);
                    stepStatus.addLeftVehicle(vehicle.getVehicleId());
                }
            }
        }
    }

    private void updateWaitingVehicles() {
        for (Queue<Vehicle> queue : vehicleQueues.values()) {
            for (Vehicle vehicle : queue) {
                vehicle.update();
            }
        }
    }

    private void visualizeIntersection(int stepNumber, StepStatus stepStatus) {
        System.out.println("\n========== SIMULATION STEP #" + stepNumber + " ==========");

        // Traffic light information
        System.out.println("\nTRAFFIC LIGHT STATUS:");
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            TrafficLight light = intersection.getTrafficLight(direction);
            String stateSymbol = "";
            switch (light.getCurrentState()) {
                case RED: stateSymbol = "red"; break;
                case RED_YELLOW: stateSymbol = "red-yellow"; break;
                case YELLOW: stateSymbol = "yellow"; break;
                case GREEN: stateSymbol = "green"; break;
            }
            System.out.printf("%-7s: %s (remaining time: %ds)\n",
                    direction, stateSymbol, light.getRemainingTime());
        }

        // Vehicle queue information
        System.out.println("\nVEHICLES IN QUEUES:");
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            Queue<Vehicle> queue = vehicleQueues.get(direction);
            System.out.printf("%-7s: %d vehicles\n", direction, queue.size());
            if (!queue.isEmpty()) {
                System.out.print("         [ ");
                for (Vehicle v : queue) {
                    // Display vehicle IDs
                    System.out.print(v.getVehicleId() + " ");
                }
                System.out.println("]");
            }
        }

        // Information about vehicles that left the intersection
        System.out.println("\nVEHICLES LEAVING INTERSECTION THIS STEP:");
        if (stepStatus.getLeftVehicles().isEmpty()) {
            System.out.println("None");
        } else {
            System.out.print("[ ");
            for (String vehicleId : stepStatus.getLeftVehicles()) {
                System.out.print(vehicleId + " ");
            }
            System.out.println("]");
        }

        System.out.println("\n============================================");
    }

    private void generateOutputFile(String outputFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode stepStatusesNode = rootNode.putArray("stepStatuses");

        for (StepStatus status : stepStatuses) {
            ObjectNode statusNode = mapper.createObjectNode();
            ArrayNode leftVehiclesNode = statusNode.putArray("leftVehicles");

            for (String vehicleId : status.getLeftVehicles()) {
                leftVehiclesNode.add(vehicleId);
            }

            stepStatusesNode.add(statusNode);
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile), rootNode);
    }

    public void setVisualMode(boolean visualMode) {
        this.visualMode = visualMode;
    }

    public void addSimulationListener(Consumer<SimulationState> listener) {
        simulationListeners.add(listener);
    }

    public void removeSimulationListener(Consumer<SimulationState> listener) {
        simulationListeners.remove(listener);
    }

    public SimulationState getCurrentState() {
        return currentState;
    }

    public void addVehicle(String vehicleId, TrafficLight.Direction startRoad, TrafficLight.Direction endRoad) {
        Vehicle vehicle = new Vehicle(vehicleId, startRoad, endRoad);
        vehicleQueues.get(startRoad).add(vehicle);

        if (visualMode) {
            updateSimulationState(null);
        }
    }

    private void updateSimulationState(StepStatus stepStatus) {
        // Update light states
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            TrafficLight light = intersection.getTrafficLight(dir);
            currentState.setLightState(dir, light.getCurrentState());
        }

        // Update vehicle queues with full vehicle information
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            List<SimulationState.QueuedVehicle> vehicles = new ArrayList<>();
            for (Vehicle v : vehicleQueues.get(dir)) {
                // Store the complete vehicle information including start and end roads
                vehicles.add(new SimulationState.QueuedVehicle(
                    v.getVehicleId(), v.getStartRoad(), v.getEndRoad()));
            }
            currentState.setVehicleQueue(dir, vehicles);
        }

        // Update crossed vehicles
        if (stepStatus != null && !stepStatus.getLeftVehicles().isEmpty()) {
            List<String> crossedVehicles = new ArrayList<>(stepStatus.getLeftVehicles());
            
            // Filtruj, aby dodać tylko pojazdy, które nie są już w animacji
            List<String> newCrossedVehicles = new ArrayList<>();
            for (String vehicleId : crossedVehicles) {
                // Sprawdź, czy ten pojazd nie jest już animowany
                boolean alreadyAnimated = false;
                for (SimulationState.CrossingVehicle v : currentState.getCrossingVehicles()) {
                    if (v.getId().equals(vehicleId)) {
                        alreadyAnimated = true;
                        break;
                    }
                }
                
                if (!alreadyAnimated) {
                    newCrossedVehicles.add(vehicleId);
                }
            }
            
            // Ustaw listę pojazdów, które właśnie przejechały (dla potrzeb wyświetlania)
            currentState.setLastCrossedVehicles(newCrossedVehicles);
            
            // Dodaj tylko nowe pojazdy do animacji
            for (String vehicleId : newCrossedVehicles) {
                // Znajdź pojazd, aby określić kierunki
                for (Vehicle v : completedVehicles) {
                    if (v.getVehicleId().equals(vehicleId)) {
                        // Dodaj pojazd do animowanych pojazdów
                        currentState.addCrossingVehicle(vehicleId, 
                                                    v.getStartRoad(), 
                                                    v.getEndRoad());
                        break;
                    }
                }
            }
        } else {
            currentState.setLastCrossedVehicles(new ArrayList<>());
        }

        // Notify listeners
        for (Consumer<SimulationState> listener : simulationListeners) {
            listener.accept(currentState);
        }
    }

    public void setController(SimulationController controller) {
        this.controller = controller;
    }

    /**
     * Rozpoczyna symulację
     */
    public void start() {
        if (!running) {
            running = true;
            timer.start();
        }
    }

    /**
     * Pauzuje symulację
     */
    public void pause() {
        if (running) {
            running = false;
            timer.stop();
        }
    }

    /**
     * Resetuje symulację do stanu początkowego
     */
    public void reset() {
        pause();
        currentState = new SimulationState();

        // Powiadomienie kontrolera o zresetowanym stanie
        if (controller != null) {
            Platform.runLater(() -> controller.updateUI(currentState));
        }
    }

    /**
     * Aktualizuje stan symulacji o jeden krok
     */
    private void update() {
        // Aktualizuj animację pojazdów przejeżdżających przez skrzyżowanie
        // tylko jeśli są jakieś pojazdy do animacji
        if (!currentState.getCrossingVehicles().isEmpty()) {
            currentState.updateCrossingAnimations();
        }

        // Powiadom kontroler o nowym stanie
        if (controller != null) {
            Platform.runLater(() -> controller.updateUI(currentState));
        }
    }

    /**
     * Zwraca aktualny stan symulacji
     */
    public SimulationState getState() {
        return currentState;
    }

    /**
     * Ustawia interwał aktualizacji symulacji w milisekundach
     */
    public void setUpdateInterval(int milliseconds) {
        this.updateInterval = milliseconds * 1_000_000L;
    }

    /**
     * Rysuje pojazdy w kolejce dla danego kierunku
     */

}