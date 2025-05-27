package avs.simulation;

import avs.simulation.UI.SimulationState;
import avs.simulation.model.Intersection;
import avs.simulation.model.StepStatus;
import avs.simulation.model.LightControlers.TrafficLight;
import avs.simulation.model.Vehicle;
import avs.simulation.util.Command;
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
    private SimulationState currentState;
    private static final int MAX_VEHICLES_PER_GREEN = 1; // Limit the number of vehicles per green/yellow light

    public Simulation() {
        this.intersection = new Intersection();
        this.vehicleQueues = new HashMap<>();
        this.completedVehicles = new ArrayList<>();
        this.stepStatuses = new ArrayList<>();

        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            vehicleQueues.put(direction, new LinkedList<>());
        }

        currentState = new SimulationState();

    }

    public void runFromJsonFile(String inputFile, String outputFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map input = mapper.readValue(new File(inputFile), Map.class);
        List<Map<String, Object>> commandsData = (List<Map<String, Object>>) input.get("commands");
        List<Command> commands = parseCommands(commandsData);

        for (Command command : commands) {
            executeCommand(command);
        }
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
        return switch (direction.toLowerCase()) {
            case "north" -> TrafficLight.Direction.NORTH;
            case "east" -> TrafficLight.Direction.EAST;
            case "south" -> TrafficLight.Direction.SOUTH;
            case "west" -> TrafficLight.Direction.WEST;
            default -> throw new IllegalArgumentException("Unnown: " + direction);
        };
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

        StepStatus stepStatus = new StepStatus();
        intersection.update(vehicleQueues);
        intersection.processVehicles(vehicleQueues, stepStatus, completedVehicles);
        updateWaitingVehicles();
        stepStatuses.add(stepStatus);

        if (visualMode) {
            updateSimulationState(stepStatus);
        }
    }


    private void updateWaitingVehicles() {
        for (Queue<Vehicle> queue : vehicleQueues.values()) {
            for (Vehicle vehicle : queue) {
                vehicle.update();
            }
        }
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
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            TrafficLight light = intersection.getTrafficLight(dir);
            currentState.setLightState(dir, light.getCurrentState());
        }
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            List<SimulationState.QueuedVehicle> vehicles = new ArrayList<>();
            for (Vehicle v : vehicleQueues.get(dir)) {
                // Store the complete vehicle information including start and end roads
                vehicles.add(new SimulationState.QueuedVehicle(
                        v.getVehicleId(), v.getStartRoad(), v.getEndRoad()));
            }
            currentState.setVehicleQueue(dir, vehicles);
        }

        if (stepStatus != null && !stepStatus.getLeftVehicles().isEmpty()) {
            List<String> crossedVehicles = new ArrayList<>(stepStatus.getLeftVehicles());
            List<String> newCrossedVehicles = new ArrayList<>();
            for (String vehicleId : crossedVehicles) {
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
            currentState.setLastCrossedVehicles(newCrossedVehicles);
            for (String vehicleId : newCrossedVehicles) {
                for (Vehicle v : completedVehicles) {
                    if (v.getVehicleId().equals(vehicleId)) {

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

        for (Consumer<SimulationState> listener : simulationListeners) {
            listener.accept(currentState);
        }
    }

    // Add this method to your Simulation class
    public void setIntersectionControllerType(Intersection.ControllerType type) {
        if (intersection != null) {
            intersection.setControllerType(type);
        }
        
        // Notify listeners about the state change
        notifyListeners(getCurrentState());
    }

    private void notifyListeners(SimulationState state) {
        for (Consumer<SimulationState> listener : simulationListeners) {
            listener.accept(state);
        }
    }
}