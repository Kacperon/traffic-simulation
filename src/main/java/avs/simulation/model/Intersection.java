package avs.simulation.model;

import java.util.*;

public class Intersection {
    private Map<TrafficLight.Direction, TrafficLight> trafficLights;
    private TrafficLightController controller;

    public Intersection() {
        trafficLights = new HashMap<>();
        for (TrafficLight.Direction direction : TrafficLight.Direction.values()) {
            trafficLights.put(direction, new TrafficLight());
        }
        controller = new TrafficLightController(trafficLights);
    }

    public TrafficLight getTrafficLight(TrafficLight.Direction direction) {
        return trafficLights.get(direction);
    }

    public void update(Map<TrafficLight.Direction, Queue<Vehicle>> vehicleQueues) {
        controller.updateLightStates();
        for (TrafficLight light : trafficLights.values()) {
            light.update();
        }
    }

    public TrafficLight.Direction getCurrentGreenDirection() {
        return controller.getCurrentGreenDirection();
    }

    public List<TrafficLight.Direction> getCurrentGreenDirections() {
        List<TrafficLight.Direction> directions = new ArrayList<>();

        for (Map.Entry<TrafficLight.Direction, TrafficLight> entry : trafficLights.entrySet()) {
            TrafficLight.LightState state = entry.getValue().getCurrentState();
            // Include both green and yellow as "can cross" states
            if (state == TrafficLight.LightState.GREEN || state == TrafficLight.LightState.YELLOW) {
                directions.add(entry.getKey());
            }
        }

        return directions;
    }

    public boolean canVehicleCross(TrafficLight.Direction fromDirection) {
        TrafficLight light = trafficLights.get(fromDirection);

        return light.getCurrentState() == TrafficLight.LightState.GREEN ||
                light.getCurrentState() == TrafficLight.LightState.YELLOW;
    }

    private class TrafficLightController {
        private static final int GREEN_DURATION = 2;
        private static final int YELLOW_DURATION = 1;
        private static final int RED_DURATION = 1;
        private static final int RED_YELLOW_DURATION = 2;

        private Map<TrafficLight.Direction, TrafficLight> lights;
        private TrafficLight.Direction currentGreenDirection;

        public TrafficLightController(Map<TrafficLight.Direction, TrafficLight> lights) {
            this.lights = lights;
            this.currentGreenDirection = TrafficLight.Direction.NORTH;
            lights.get(TrafficLight.Direction.NORTH).setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
            lights.get(TrafficLight.Direction.SOUTH).setState(TrafficLight.LightState.RED, RED_DURATION);
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
                    currentLight.setState(TrafficLight.LightState.YELLOW, YELLOW_DURATION);
                    TrafficLight.Direction nextDirection = getNextDirection();
                    lights.get(nextDirection).setState(TrafficLight.LightState.RED_YELLOW, RED_YELLOW_DURATION);
                    break;

                case YELLOW:
                    currentLight.setState(TrafficLight.LightState.RED, RED_DURATION);
                    switchToNextDirection();
                    break;

                case RED_YELLOW:
                    currentLight.setState(TrafficLight.LightState.GREEN, GREEN_DURATION);
                    break;

                case RED:
                    break;
            }
        }

        private TrafficLight.Direction getNextDirection() {
            TrafficLight.Direction[] directions = {
                    TrafficLight.Direction.NORTH,
                    TrafficLight.Direction.EAST,
                    TrafficLight.Direction.SOUTH,
                    TrafficLight.Direction.WEST
            };

            int currentIndex = Arrays.asList(directions).indexOf(currentGreenDirection);
            return directions[(currentIndex + 1) % directions.length];
        }

        private void switchToNextDirection() {
            currentGreenDirection = getNextDirection();
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