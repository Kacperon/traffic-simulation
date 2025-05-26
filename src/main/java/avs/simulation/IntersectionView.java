package avs.simulation;


import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Interpolator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;
import javafx.animation.AnimationTimer;

import java.util.*;

public class IntersectionView extends Canvas {
    private GraphicsContext gc;
    private SimulationState currentState;

    // Dodaj te pola do klasy IntersectionView
    private Map<String, AnimatedVehicle> activeAnimatedVehicles = new HashMap<>();
    private AnimationTimer animationTimer;
    private Simulation simulation; // Dodane pole klasy
    private Map<TrafficLight.Direction, Queue<SimulationState.CrossingVehicle>> pendingVehicles = new HashMap<>();

    public IntersectionView(double width, double height) {
        super(width, height);
        gc = getGraphicsContext2D();
        currentState = new SimulationState();
        
        // Initialize queues for each direction
        for (TrafficLight.Direction dir : TrafficLight.Direction.values()) {
            pendingVehicles.put(dir, new LinkedList<>());
        }
        
        // Inicjalizacja timera animacji
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Wywołuj tylko odświeżanie animacji, nie cały draw()
                redrawCanvas();
            }
        };
        animationTimer.start();
    }

    /**
     * Aktualizuje stan widoku na podstawie stanu symulacji
     */
    public void update(SimulationState state) {
        this.currentState = state;
        
        // Nie ma potrzeby oddzielnego wywołania redrawCanvas(), 
        // animationTimer zrobi to automatycznie w następnej klatce
        
        // Wyczyść mapę aktywnych animacji dla nowych pojazdów
        clearCompletedAnimations();
        
        // Dodaj nowe pojazdy do animacji
        updateAnimatedVehicles();
    }

    // Nowa metoda do rysowania tylko Canvas z uwzględnieniem animacji
    private void redrawCanvas() {
        double width = getWidth();
        double height = getHeight();
        
        // Wyczyść Canvas
        gc.clearRect(0, 0, width, height);
        
        // Narysuj ponownie tło i infrastrukturę
        drawBackground(width, height);
        
        // Narysuj światła i kolejki pojazdów
        drawTrafficLights(width, height);
        drawVehicleQueues(width, height);
        
        // Narysuj aktywnie animowane pojazdy - używamy bezpośrednio drawAnimatedVehicles(), 
        // która jest już poprawnie zaimplementowana
        drawAnimatedVehicles();
    }

    // Metoda do rysowania tła i dróg
    private void drawBackground(double width, double height) {
        // Narysuj tło (drogi)
        gc.setFill(Color.DARKGRAY);
        
        // Droga pozioma
        gc.fillRect(0, height/2 - 30, width, 60);
        
        // Droga pionowa
        gc.fillRect(width/2 - 30, 0, 60, height);
        
        // Oznaczenia dróg
        gc.setStroke(Color.WHITE);
        gc.setLineDashes(5); 
        gc.setLineWidth(2);
        
        // Linia środkowa pozioma
        gc.strokeLine(0, height/2, width, height/2);
        
        // Linia środkowa pionowa
        gc.strokeLine(width/2, 0, width/2, height);
        
        gc.setLineDashes();
        
        // Narysuj centrum skrzyżowania
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(width/2 - 30, height/2 - 30, 60, 60);
    }

    // Metoda do rysowania świateł
    private void drawTrafficLights(double width, double height) {
        // Rysuj światła drogowe - przesunięte na lewą stronę pasa (ruch prawostronny)
        drawTrafficLight(TrafficLight.Direction.NORTH, width/2 - 35, height/2 - 65, 0);
        drawTrafficLight(TrafficLight.Direction.EAST, width/2 + 65, height/2 - 35, 90);
        drawTrafficLight(TrafficLight.Direction.SOUTH, width/2 + 35, height/2 + 65, 180);
        drawTrafficLight(TrafficLight.Direction.WEST, width/2 - 65, height/2 + 35, 270);
    }

    // Metoda do rysowania kolejek pojazdów
    private void drawVehicleQueues(double width, double height) {
        // Rysuj pojazdy w kolejkach - dostosowane do ruchu prawostronnego i bliżej skrzyżowania
        drawVehicleQueue(TrafficLight.Direction.NORTH, width/2 - 10, height/2 - 50);
        drawVehicleQueue(TrafficLight.Direction.EAST, width/2 + 50, height/2 - 10);
        drawVehicleQueue(TrafficLight.Direction.SOUTH, width/2 + 10, height/2 + 50);
        drawVehicleQueue(TrafficLight.Direction.WEST, width/2 - 50, height/2 + 10);
    }

    /**
     * Rysuje światło drogowe w określonym miejscu i z określoną rotacją
     */
    private void drawTrafficLight(TrafficLight.Direction direction, double x, double y, int rotation) {
        // Pobierz aktualny stan światła dla danego kierunku
        TrafficLight.LightState state = currentState.getLightState(direction);
        
        // Zapisz stan grafiki
        gc.save();
        
        // Przesuń i obróć do odpowiedniej pozycji
        gc.translate(x, y);
        gc.rotate(rotation);
        
        // Rysuj obudowę światła (czarny prostokąt)
        gc.setFill(Color.BLACK);
        gc.fillRect(-10, -25, 20, 50);
        
        // Rysuj sygnały świetlne
        // Światło czerwone
        gc.setFill(state == TrafficLight.LightState.RED || state == TrafficLight.LightState.RED_YELLOW ? 
                  Color.RED : Color.rgb(100, 0, 0));
        gc.fillOval(-5, -20, 10, 10);
        
        // Światło żółte
        gc.setFill(state == TrafficLight.LightState.YELLOW || state == TrafficLight.LightState.RED_YELLOW ? 
                  Color.YELLOW : Color.rgb(100, 100, 0));
        gc.fillOval(-5, -5, 10, 10);
        
        // Światło zielone
        gc.setFill(state == TrafficLight.LightState.GREEN ? 
                  Color.LIME : Color.rgb(0, 100, 0));
        gc.fillOval(-5, 10, 10, 10);
        
        // Przywróć stan grafiki
        gc.restore();
    }

    // Dodaj klasę pomocniczą do animacji ruchu circular
    private static class CircularInterpolator extends Interpolator {
        public enum Direction {UP, DOWN}
        
        private final Direction direction;
        
        public CircularInterpolator(Direction direction) {
            this.direction = direction;
        }
        
        @Override
        protected double curve(double t) {
            // Krzywa kołowa dla płynnych zakrętów
            if (direction == Direction.UP) {
                return Math.sin(t * Math.PI / 2);
            } else {
                return 1 - Math.cos(t * Math.PI / 2);
            }
        }
    }

    // Klasa reprezentująca animowany pojazd
    private class AnimatedVehicle {
        String id;
        TrafficLight.Direction fromDirection;
        TrafficLight.Direction toDirection;
        Vehicle.MovementType movementType;
        
        // Właściwości animacji
        private final DoubleProperty x = new SimpleDoubleProperty();
        private final DoubleProperty y = new SimpleDoubleProperty();
        private final DoubleProperty rotation = new SimpleDoubleProperty();
        Timeline animation;
        
        public AnimatedVehicle(String id, TrafficLight.Direction fromDirection, 
                             TrafficLight.Direction toDirection) {
            this.id = id;
            this.fromDirection = fromDirection;
            this.toDirection = toDirection;
            this.movementType = calculateMovementType(fromDirection, toDirection);
            
            // Ustawienie początkowej pozycji i rotacji
            setInitialPositionAndRotation();
        }
        
        private Vehicle.MovementType calculateMovementType(TrafficLight.Direction from, TrafficLight.Direction to) {
            // Logika określania typu ruchu na podstawie kierunków
            int diff = (to.ordinal() - from.ordinal() + 4) % 4;
            switch (diff) {
                case 1: return Vehicle.MovementType.LEFT;
                case 2: return Vehicle.MovementType.STRAIGHT;
                case 3: return Vehicle.MovementType.RIGHT;
                default: return Vehicle.MovementType.STRAIGHT;
            }
        }
        
        public void setInitialPositionAndRotation() {
            switch(fromDirection) {
                case NORTH: rotation.set(90); break;
                case EAST: rotation.set(180); break;
                case SOUTH: rotation.set(270); break;
                case WEST: rotation.set(0); break;
            }
            
            // Pozycja początkowa na krawędzi skrzyżowania
            switch(fromDirection) {
                case NORTH: 
                    x.set(0.47);  // Lewy pas (ruch prawostronny)
                    y.set(0.4);
                    break;
                case EAST: 
                    x.set(0.6);
                    y.set(0.47);  // Górny pas
                    break;
                case SOUTH: 
                    x.set(0.53);  // Prawy pas
                    y.set(0.6);
                    break;
                case WEST: 
                    x.set(0.4);
                    y.set(0.53);  // Dolny pas
                    break;
            }
        }
        
        public void animate(double durationSeconds) {
            animation = new Timeline();
            
            // Przygotuj animację w zależności od typu ruchu
            switch (movementType) {
                case STRAIGHT: animateStraight(durationSeconds); break;
                case LEFT:     animateLeft(durationSeconds); break;
                case RIGHT:    animateRight(durationSeconds); break;
            }
            
            // Dodaj obsługę zakończenia animacji
            animation.setOnFinished(event -> {
                // Wystarczy usunąć z aktywnych animacji, clearCompletedAnimations zajmie się resztą
                activeAnimatedVehicles.remove(id);
                currentState.removeVehicleFromAnimation(id);
                System.out.println("Animacja zakończona - usunięto pojazd: " + id);
            });
            
            animation.play();
        }
        
        private void animateStraight(double durationSeconds) {
            // Punkty końcowe dla ruchu prosto
            double endX = x.get();
            double endY = y.get();
            
            switch(toDirection) {
                case NORTH: endY = 0.2; break;
                case EAST: endX = 0.8; break;
                case SOUTH: endY = 0.8; break;
                case WEST: endX = 0.2; break;
            }
            
            // Animacja ruchu prosto
            KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(durationSeconds),
                new KeyValue(x, endX, Interpolator.EASE_BOTH),
                new KeyValue(y, endY, Interpolator.EASE_BOTH)
            );
            
            animation.getKeyFrames().add(keyFrame);
        }

        private void animateLeft(double durationSeconds) {
            // Animacja skrętu w lewo (szeroki łuk)
            double midX = x.get(), midY = y.get();
            double endX = x.get(), endY = y.get();

            // ZMIANA: odwrócenie kierunku rotacji dla skrętu w lewo
            double finalRotation = rotation.get() + 90;  // Zmienione z -90 na +90

            // Określenie punktów pośrednich i końcowych
            switch(fromDirection) {
                case NORTH:  // Z północy w kierunku zachodnim
                    midX = 0.45; midY = 0.45;
                    endX = 0.2; endY = 0.45;
                    break;
                case EAST:  // Ze wschodu w kierunku północnym
                    midX = 0.55; midY = 0.45;
                    endX = 0.55; endY = 0.2;
                    break;
                case SOUTH:  // Z południa w kierunku wschodnim
                    midX = 0.55; midY = 0.55;
                    endX = 0.8; endY = 0.55;
                    break;
                case WEST:  // Z zachodu w kierunku południowym
                    midX = 0.45; midY = 0.55;
                    endX = 0.45; endY = 0.8;
                    break;
            }

            // Pierwszy krok - przejazd na środek
            KeyFrame keyFrame1 = new KeyFrame(
                Duration.seconds(durationSeconds * 0.3),
                new KeyValue(x, midX, new CircularInterpolator(CircularInterpolator.Direction.UP)),
                new KeyValue(y, midY, new CircularInterpolator(CircularInterpolator.Direction.DOWN)),
                new KeyValue(rotation, finalRotation, Interpolator.EASE_BOTH)
            );
            
            // Drugi krok - skręt w lewo i wyjazd
            KeyFrame keyFrame2 = new KeyFrame(
                Duration.seconds(durationSeconds),
                new KeyValue(x, endX, new CircularInterpolator(CircularInterpolator.Direction.DOWN)),
                new KeyValue(y, endY, new CircularInterpolator(CircularInterpolator.Direction.UP))
                
            );
            
            animation.getKeyFrames().addAll(keyFrame1, keyFrame2);
        }
        
        private void animateRight(double durationSeconds) {
            // Animacja skrętu w prawo (ciasny łuk)
            double midX = x.get(), midY = y.get();
            double endX = x.get(), endY = y.get();
            
            // ZMIANA: odwrócenie kierunku rotacji dla skrętu w prawo
            double finalRotation = rotation.get() - 90;  // Zmienione z +90 na -90
            
            // Określenie punktów pośrednich i końcowych
            switch(fromDirection) {
                case NORTH:  // Z północy w kierunku wschodnim
                    midX = 0.5; midY = 0.55;
                    endX = 0.8; endY = 0.55;
                    break;
                case EAST:  // Ze wschodu w kierunku południowym
                    midX = 0.45; midY = 0.5;
                    endX = 0.45; endY = 0.8;
                    break;
                case SOUTH:  // Z południa w kierunku zachodnim
                    midX = 0.5; midY = 0.45;
                    endX = 0.2; endY = 0.45;
                    break;
                case WEST:  // Z zachodu w kierunku północnym
                    midX = 0.55; midY = 0.5;
                    endX = 0.55; endY = 0.2;
                    break;
            }
            
            // Pierwszy krok - przejazd na środek
            KeyFrame keyFrame1 = new KeyFrame(
                Duration.seconds(durationSeconds * 0.3),
                new KeyValue(x, midX, new CircularInterpolator(CircularInterpolator.Direction.DOWN)),
                new KeyValue(y, midY, new CircularInterpolator(CircularInterpolator.Direction.UP)),
                new KeyValue(rotation, finalRotation, Interpolator.EASE_BOTH)
            );
            
            // Drugi krok - skręt w prawo i wyjazd
            KeyFrame keyFrame2 = new KeyFrame(
                Duration.seconds(durationSeconds),
                new KeyValue(x, endX, new CircularInterpolator(CircularInterpolator.Direction.UP)),
                new KeyValue(y, endY, new CircularInterpolator(CircularInterpolator.Direction.DOWN))
                
            );
            
            animation.getKeyFrames().addAll(keyFrame1, keyFrame2);
        }
        
        public double getDrawX() {
            return x.get() * getWidth();
        }
        
        public double getDrawY() {
            return y.get() * getHeight();
        }
        
        public double getRotation() {
            return rotation.get();
        }
    }

    /**
     * Aktualizuje animowane pojazdy na podstawie stanu symulacji
     */
    private void updateAnimatedVehicles() {
        List<SimulationState.CrossingVehicle> crossingVehicles = currentState.getCrossingVehicles();
        if (crossingVehicles == null || crossingVehicles.isEmpty()) return;
        
        // Twórz animacje tylko dla nowych pojazdów
        for (SimulationState.CrossingVehicle vehicle : crossingVehicles) {
            String vehicleId = vehicle.getId();
            
            // Jeśli pojazd nie jest jeszcze animowany, dodaj go
            if (!activeAnimatedVehicles.containsKey(vehicleId)) {
                AnimatedVehicle animVehicle = new AnimatedVehicle(
                    vehicleId, 
                    vehicle.getFromDirection(),
                    vehicle.getToDirection()
                );
                
                // Rozpocznij animację z odpowiednim czasem trwania
                animVehicle.animate(1.5); // 1.5 sekundy na całą animację
                
                // Dodaj do mapy aktywnych animacji
                activeAnimatedVehicles.put(vehicleId, animVehicle);
            }
        }
    }

    // Metoda do rysowania animowanych pojazdów
    private void drawAnimatedVehicles() {
        // Rysuj wszystkie aktywne animowane pojazdy
        for (AnimatedVehicle vehicle : activeAnimatedVehicles.values()) {
            drawAnimatedVehicle(
                vehicle.id, 
                vehicle.getDrawX(), 
                vehicle.getDrawY(),
                vehicle.getRotation(),
                vehicle.movementType
            );
        }
    }
    private void drawVehicleQueue(TrafficLight.Direction direction, double startX, double startY) {
        List<SimulationState.QueuedVehicle> vehicles = currentState.getVehicleQueue(direction);
        if (vehicles == null || vehicles.isEmpty()) return;

        // Określenie kierunku układania pojazdów
        double dx = 0, dy = 0;
        switch (direction) {
            case NORTH: dx = 0; dy = -30; break;
            case EAST: dx = 30; dy = 0; break;
            case SOUTH: dx = 0; dy = 30; break;
            case WEST: dx = -30; dy = 0; break;
        }

        // Rysuj maksymalnie 5 pojazdów w kolejce
        int maxVehiclesToDraw = Math.min(vehicles.size(), 5);

        for (int i = 0; i < maxVehiclesToDraw; i++) {
            SimulationState.QueuedVehicle vehicle = vehicles.get(i);
            double x = startX + i * dx;
            double y = startY + i * dy;

            // Określ kolor na podstawie typu ruchu (tak samo jak dla animowanych pojazdów)
            Vehicle.MovementType movementType = vehicle.getMovementType();
            Color vehicleColor;
            switch (movementType) {
                case LEFT: vehicleColor = Color.ORANGE; break;
                case RIGHT: vehicleColor = Color.CYAN; break;
                default: vehicleColor = Color.LIME; break; // STRAIGHT
            }
            
            gc.save();  // Zapisz stan obecny
            
            // Określ rotację na podstawie kierunku startowego
            double rotation = 0;
            switch(direction) {
                case NORTH: rotation = 90; break;
                case EAST: rotation = 180; break;
                case SOUTH: rotation = 270; break;
                case WEST: rotation = 0; break;
            }
            
            // Przesuń i obróć
            gc.translate(x, y);
            gc.rotate(rotation);
            
            // Narysuj pojazd w takim samym kształcie jak animowane pojazdy
            gc.setFill(vehicleColor);
            gc.fillRect(-10, -6, 20, 12);
            
            // Trójkąt wskazujący kierunek
            double[] triangleX = {10, 15, 10};
            double[] triangleY = {-6, 0, 6};
            gc.fillPolygon(triangleX, triangleY, 3);
            
            // Tekst z ID pojazdu
            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(vehicle.getId(), 0, 3);
            
            gc.restore();  // Przywróć stan
        }

        // Jeśli jest więcej pojazdów niż możemy narysować
        if (vehicles.size() > maxVehiclesToDraw) {
            double x = startX + maxVehiclesToDraw * dx;
            double y = startY + maxVehiclesToDraw * dy;

            gc.setFill(Color.BLACK);
            gc.fillText("+" + (vehicles.size() - maxVehiclesToDraw), x, y + 4);
        }
    }

    /**
     * Usuwa zakończone animacje
     */
    private void clearCompletedAnimations() {
        // Usuń pojazdy, których nie ma już w stanie symulacji
        List<String> vehiclesToRemove = new ArrayList<>();
        
        for (String vehicleId : activeAnimatedVehicles.keySet()) {
            boolean found = false;
            for (SimulationState.CrossingVehicle vehicle : currentState.getCrossingVehicles()) {
                if (vehicle.getId().equals(vehicleId)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                vehiclesToRemove.add(vehicleId);
            }
        }
        
        // Usuń nieaktualne animacje
        for (String vehicleId : vehiclesToRemove) {
            AnimatedVehicle vehicle = activeAnimatedVehicles.remove(vehicleId);
            if (vehicle != null && vehicle.animation != null) {
                vehicle.animation.stop(); // Zatrzymaj animację przed usunięciem
            }
            System.out.println("Usunięto animację pojazdu: " + vehicleId);
        }
    }

    /**
     * Ustawia referencję do obiektu symulacji
     */
    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    /**
     * Rysuje animowany pojazd z określoną pozycją, rotacją i typem ruchu
     */
    private void drawAnimatedVehicle(String id, double x, double y, double rotation, Vehicle.MovementType type) {
        gc.save();  // Zapisz stan obecny
        
        // Przesuń i obróć
        gc.translate(x, y);
        gc.rotate(rotation);
        
        // Wybierz kolor zależnie od typu ruchu
        Color vehicleColor;
        switch (type) {
            case LEFT: vehicleColor = Color.ORANGE; break;
            case RIGHT: vehicleColor = Color.CYAN; break;
            default: vehicleColor = Color.LIME; break; // STRAIGHT
        }
        
        // Narysuj pojazd jako prostokąt z "nosem"
        gc.setFill(vehicleColor);
        gc.fillRect(-10, -6, 20, 12);
        
        // Trójkąt wskazujący kierunek
        double[] triangleX = {10, 15, 10};
        double[] triangleY = {-6, 0, 6};
        gc.fillPolygon(triangleX, triangleY, 3);
        
        // Tekst z ID pojazdu
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(id, 0, 3);
        
        gc.restore();  // Przywróć stan
    }
}