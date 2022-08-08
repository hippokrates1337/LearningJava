import java.util.Random;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GeneticAlgorithm {
    private int generations;
    private Car[] cars;
    public static final float CARWIDTH = 0.035f, CARHEIGHT = 0.065f;

    public GeneticAlgorithm(int numGenerations, int numCars, Level l, float trackWidth) {
        generations = numGenerations;
        cars = new Car[numCars];
        float[] behavior = new float[Car.NUM_PARAMETERS];

        Random r = new Random();
        for(int i = 0; i < numCars; i++) {
            // Generate a random valid position in the level
            int vertex = r.nextInt(l.getInnerVertices().length);
            float x = l.getInnerVertices()[vertex][0] + 1.5f * CARWIDTH;
            float y = l.getInnerVertices()[vertex][1];
            x += r.nextFloat() * trackWidth - CARWIDTH;
            cars[i] = new Car(x, y, r.nextFloat(), r.nextFloat(), r.nextFloat() * 0.15f, CARWIDTH, CARHEIGHT);

            // Set initial steering behavior
            behavior[Car.THRESHOLD_CHANGEDIR] = r.nextFloat();
            behavior[Car.ANGLE_CHANGEDIR] = r.nextFloat();
            behavior[Car.THRESHOLD_ACCELERATE] = r.nextFloat();
            behavior[Car.INCREMENT_ACCELERATE] = r.nextFloat();
            behavior[Car.THRESHOLD_BRAKE] = r.nextFloat();
            behavior[Car.INCREMENT_BRAKE] = -r.nextFloat();
            cars[i].setSteeringBehavior(behavior);
        }
    }

    public void update(long deltaMillis, Level l) {
        for(int i = 0; i < cars.length; i++) {
            cars[i].update(deltaMillis, l);
        }
    }

    public void draw(Graphics g, int startX, int startY, int scaleToX, int scaleToY) {
        for(int i = 0; i < cars.length; i++) {
            cars[i].draw(g, startX, startY, scaleToX, scaleToY);
        }
    }
}
