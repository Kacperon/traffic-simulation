package avs.simulation;

import java.util.*;

public class SimulationState {
    private Map<TrafficLight.Direction, TrafficLight.LightState> lightStates;
    private Map<TrafficLight.Direction, List<String>> vehicleQueues;
    private List<String> lastCrossedVehicles;
    private List<CrossingVehicle> crossingVehicles;

    public SimulationState() {
        lightStates = new HashMap<>();
        vehicleQueues = new HashMap<>();
        lastCrossedVehicles = new ArrayList<>();
        crossingVehicles = new ArrayList<>();

        // Initialize with default values
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            lightStates.put(dir, TrafficLight.LightState.RED);
            vehicleQueues.put(dir, new ArrayList<>());
        }
    }

    public void setLightState(TrafficLight.Direction direction, TrafficLight.LightState state) {
        lightStates.put(direction, state);
    }

    public void setVehicleQueue(TrafficLight.Direction direction, List<String> vehicles) {
        vehicleQueues.put(direction, vehicles);
    }

    public void setLastCrossedVehicles(List<String> vehicles) {
        lastCrossedVehicles = vehicles;
    }

    public TrafficLight.LightState getLightState(TrafficLight.Direction direction) {
        return lightStates.get(direction);
    }

    public List<String> getVehicleQueue(TrafficLight.Direction direction) {
        return vehicleQueues.get(direction);
    }

    public List<String> getLastCrossedVehicles() {
        return lastCrossedVehicles;
    }

    public List<CrossingVehicle> getCrossingVehicles() {
        return crossingVehicles;
    }

    // Metoda do dodawania pojazdów do animacji
    public void addCrossingVehicle(String id, TrafficLight.Direction from, TrafficLight.Direction to) {
        crossingVehicles.add(new CrossingVehicle(id, from, to, 0));
    }

    /**
     * Aktualizuje stan animacji pojazdów przejeżdżających
     * i usuwa te, które zakończyły animację
     */
    public void updateCrossingAnimations() {
        // Stwórz nową listę tylko z pojazdami, które nie ukończyły animacji
        List<CrossingVehicle> remainingVehicles = new ArrayList<>();

        for (CrossingVehicle vehicle : crossingVehicles) {
            int newStep = vehicle.getAnimationStep() + 1;
            vehicle.setAnimationStep(newStep);

            // Zachowaj tylko pojazdy, które nie zakończyły animacji
            if (newStep <= 2) {
                remainingVehicles.add(vehicle);
            } else {
                System.out.println("Usunięto pojazd z animacji: " + vehicle.getId());
            }
        }

        // Zastąp starą listę nową (bez ukończonych animacji)
        this.crossingVehicles = remainingVehicles;
    }

    /**
     * Usuwa pojazd z listy animowanych pojazdów
     */
    public void removeVehicleFromAnimation(String id) {
        Iterator<CrossingVehicle> iterator = crossingVehicles.iterator();
        while (iterator.hasNext()) {
            CrossingVehicle vehicle = iterator.next();
            if (vehicle.getId().equals(id)) {
                iterator.remove();
                System.out.println("Usunięto pojazd z symulacji: " + id);
                break;
            }
        }
    }

    public class CrossingVehicle {
        private String id;
        private TrafficLight.Direction fromDirection;
        private TrafficLight.Direction toDirection;
        private int animationStep;

        public CrossingVehicle(String id, TrafficLight.Direction fromDirection, TrafficLight.Direction toDirection, int animationStep) {
            this.id = id;
            this.fromDirection = fromDirection;
            this.toDirection = toDirection;
            this.animationStep = animationStep;
        }

        // Dodaj gettery
        public String getId() {
            return id;
        }

        public TrafficLight.Direction getFromDirection() {
            return fromDirection;
        }

        public TrafficLight.Direction getToDirection() {
            return toDirection;
        }

        public int getAnimationStep() {
            return animationStep;
        }

        public void setAnimationStep(int animationStep) {
            this.animationStep = animationStep;
        }
    }
}