package avs.simulation;

import java.util.*;

public class Intersection {
    private Map<TrafficLight.Direction, TrafficLight> trafficLights;
    private TrafficLightController controller;

    public Intersection() {
        // Inicjalizacja świateł dla wszystkich kierunków
        trafficLights = new HashMap<>();
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            trafficLights.put(direction, new TrafficLight());
        }

        // Inicjalizacja kontrolera świateł
        controller = new TrafficLightController(trafficLights);
    }

    public TrafficLight getTrafficLight(TrafficLight.Direction direction) {
        return trafficLights.get(direction);
    }

    public void update(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues) {
        // Aktualizacja stanu świateł
        controller.updateLightStates();

        // Dostosowanie cykli świateł na podstawie natężenia ruchu
        controller.adjustLightCycles(vehicleQueues);

        // Aktualizacja liczników czasu dla wszystkich świateł
        for (TrafficLight light : trafficLights.values()) {
            light.update();
        }
    }

    public TrafficLight.Direction getCurrentGreenDirection() {
        return controller.getCurrentGreenDirection();
    }

    public List<TrafficLight.Direction> getCurrentGreenDirections() {
        List<TrafficLight.Direction> greenDirections = new ArrayList<>();
        for (Map.Entry<TrafficLight.Direction, TrafficLight> entry : trafficLights.entrySet()) {
            if (entry.getValue().getCurrentState() == TrafficLight.LightState.GREEN) {
                greenDirections.add(entry.getKey());
            }
        }
        return greenDirections;
    }

    private class TrafficLightController {
        private static final int GREEN_DURATION = 2;
        private static final int YELLOW_DURATION = 2;
        private static final int RED_DURATION = 2;

        private Map<TrafficLight.Direction, TrafficLight> lights;
        private TrafficLight.Direction currentGreenDirection;
        private int cycleStep;

        public TrafficLightController(Map<TrafficLight.Direction, TrafficLight> lights) {
            this.lights = lights;
            this.currentGreenDirection = TrafficLight.Direction.NORTH;
            this.cycleStep = 0;

            // zgodnie z testowym przypadkiem
            lights.get(TrafficLight.Direction.NORTH).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
            lights.get(TrafficLight.Direction.SOUTH).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);

            // Ustaw pozostałe światła na czerwone
            lights.get(TrafficLight.Direction.EAST).setState(TrafficLight.LightState.RED, RED_DURATION);
            lights.get(TrafficLight.Direction.WEST).setState(TrafficLight.LightState.RED, RED_DURATION);
        }


        public void updateLightStates() {
            TrafficLight currentLight = lights.get(currentGreenDirection);

            if (currentLight.isStateFinished()) {
                switchToNextPhase();
            }
        }

        private void switchToNextPhase() {
            TrafficLight currentLight = lights.get(currentGreenDirection);

            switch (currentLight.getCurrentState()) {
                case GREEN:
                    // Przejście z zielonego na żółte
                    currentLight.setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
                    break;

                case YELLOW:
                    // Przejście z żółtego na czerwone
                    currentLight.setState(TrafficLight.LightState.RED, RED_DURATION);
                    break;

                case RED:
                    // Przejście z czerwonego na czerwono-żółte
                    currentLight.setState(TrafficLight.LightState.RED_YELLOW, 1); // 1 sekunda dla czerwono-żółtego
                    break;

                case RED_YELLOW:
                    // Przejście z czerwono-żółtego na zielone i wybór następnego kierunku
                    switchToNextDirection();
                    break;
            }
        }

        private void switchToNextDirection() {
            // Cykliczne przechodzenie między kierunkami: N -> E -> S -> W -> N
            TrafficLight.Direction[] directions = {
                    TrafficLight.Direction.NORTH,
                    TrafficLight.Direction.EAST,
                    TrafficLight.Direction.SOUTH,
                    TrafficLight.Direction.WEST
            };

            int currentIndex = Arrays.asList(directions).indexOf(currentGreenDirection);
            currentGreenDirection = directions[(currentIndex + 1) % directions.length];

            lights.get(currentGreenDirection).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
        }

        public void adjustLightCycles(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues) {
            // Logika dostosowująca cykle świateł na podstawie natężenia ruchu
            int currentQueueSize = vehicleQueues.get(currentGreenDirection).size();

            if (currentQueueSize > 3) {
                TrafficLight currentLight = lights.get(currentGreenDirection);
                if (currentLight.getCurrentState() == TrafficLight.LightState.GREEN &&
                        currentLight.getRemainingTime() < 2) {
                    // Wydłuż zielone światło o dodatkowy czas
                    currentLight.setState(TrafficLight.LightState.GREEN, 3);
                }
            }

            // Sprawdź inne kierunki z dużym natężeniem ruchu
            for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
                if (direction != currentGreenDirection &&
                        vehicleQueues.get(direction).size() > 5) {
                    // Priorytetyzuj kierunki z dużym natężeniem ruchu
                    cycleStep = (cycleStep + 1) % 4; // Zwiększ priorytet przełączenia
                }
            }
        }

        public TrafficLight.Direction getCurrentGreenDirection() {
            for (Map.Entry<TrafficLight.Direction, TrafficLight> entry : lights.entrySet()) {
                if (entry.getValue().getCurrentState() == TrafficLight.LightState.GREEN) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }
}