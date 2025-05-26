package avs.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    public Simulation() {
        this.intersection = new Intersection();
        this.vehicleQueues = new HashMap<>();
        this.completedVehicles = new ArrayList<>();
        this.stepStatuses = new ArrayList<>();

        // Inicjalizacja kolejek pojazdów dla każdego kierunku
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            vehicleQueues.put(direction, new LinkedList<>());
        }
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

            // W każdym kroku przepuszczamy określoną liczbę pojazdów
            int vehiclesToProcess = Math.min(queue.size(), 2); // max 2 pojazdy na krok

            for (int i = 0; i < vehiclesToProcess; i++) {
                if (!queue.isEmpty()) {
                    Vehicle vehicle = queue.poll();
                    vehicle.completeCrossing();
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

        // Update vehicle queues
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            List<String> vehicleIds = new ArrayList<>();
            for (Vehicle v : vehicleQueues.get(dir)) {
                vehicleIds.add(v.getVehicleId());
            }
            currentState.setVehicleQueue(dir, vehicleIds);
        }

        // Update crossed vehicles
        if (stepStatus != null) {
            currentState.setLastCrossedVehicles(stepStatus.getLeftVehicles());
        } else {
            currentState.setLastCrossedVehicles(new ArrayList<>());
        }

        // Notify listeners
        for (Consumer<SimulationState> listener : simulationListeners) {
            listener.accept(currentState);
        }
    }
}