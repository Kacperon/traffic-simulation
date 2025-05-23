package avs.simulation;

public class TrafficLight {
    public enum Direction {
        NORTH, EAST, SOUTH, WEST
    }

    public enum LightState {
        RED, RED_YELLOW, GREEN, YELLOW
    }

    private LightState currentState;
    private int remainingTime;

    public TrafficLight() {
        this.currentState = LightState.RED;
        this.remainingTime = 0;
    }

    public void setState(LightState state, int duration) {
        this.currentState = state;
        this.remainingTime = duration;
    }

    public void update() {
        if (remainingTime > 0) {
            remainingTime--;
        }
    }

    public boolean isStateFinished() {
        return remainingTime <= 0;
    }

    public LightState getCurrentState() {
        return currentState;
    }

    public int getRemainingTime() {
        return remainingTime;
    }
}