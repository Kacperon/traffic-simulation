package avs.simulation.UI.utils;

import javafx.animation.Interpolator;

public class CircularInterpolator extends Interpolator {
    public enum Direction {UP, DOWN}
    private final Direction direction;
    
    public CircularInterpolator(Direction direction) {
        this.direction = direction;
    }
    
    @Override
    protected double curve(double t) {
        if (direction == Direction.UP) {
            return Math.sin(t * Math.PI / 2);
        } else {
            return 1 - Math.cos(t * Math.PI / 2);
        }
    }
}