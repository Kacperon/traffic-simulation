package avs.simulation;

import avs.simulation.UI.utils.CircularInterpolator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CircularInterpolatorTest {
    
    @Test
    public void testUpInterpolator() {
        CircularInterpolator interpolator = new CircularInterpolator(CircularInterpolator.Direction.UP);
        
        // At t=0, sine starts at 0
        assertEquals(0, interpolator.interpolate(0, 1, 0), 0.001);
        
        // At t=0.5, sine is at ~0.7071 (sin of 45 degrees)
        assertEquals(0.7071, interpolator.interpolate(0, 1, 0.5), 0.001);
        
        // At t=1, sine reaches 1
        assertEquals(1.0, interpolator.interpolate(0, 1, 1.0), 0.001);
    }
    
    @Test
    public void testDownInterpolator() {
        CircularInterpolator interpolator = new CircularInterpolator(CircularInterpolator.Direction.DOWN);
        
        // At t=0, 1-cosine starts at 0
        assertEquals(0, interpolator.interpolate(0, 1, 0), 0.001);
        
        // At t=0.5, 1-cosine is at ~0.2929 (1 - cos of 45 degrees)
        assertEquals(0.2929, interpolator.interpolate(0, 1, 0.5), 0.001);
        
        // At t=1, 1-cosine reaches 1
        assertEquals(1.0, interpolator.interpolate(0, 1, 1.0), 0.001);
    }
}