import java.util.Random;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

public class GeneticAlgorithm {
    private int generations;
    private int currentGeneration;
    private ArrayList<Car> cars;
    private Level level;
    private float trackWidth;
    private long waitCounter;
    private long currentGenRunTime;
    private int[] maxDistHistory, totalDistHistory, totalTimeHistory;
    private static final float CARWIDTH = 0.035f, CARHEIGHT = 0.065f;

    /**
     * Sets up the genetic algorithm to train a population of cars to navigate the provided level
     * @param numGenerations How many generations of cars to train
     * @param numCars Number of cars to generate per generation
     * @param l Level with which to train the cars
     * @param trkWidth Track width of the level (needed to position the cars randomly in valid spots)
     */
    public GeneticAlgorithm(int numGenerations, int numCars, Level l, float trkWidth) {
        generations = numGenerations;
        currentGeneration = 0;
        cars = new ArrayList<Car>();
        float[] behavior = new float[Car.NUM_PARAMETERS];
        level = l;
        trackWidth = trkWidth;
        currentGenRunTime = 0;
        waitCounter = 0;
        maxDistHistory = new int[numGenerations];
        totalDistHistory = new int[numGenerations];
        totalTimeHistory = new int[numGenerations];

        Random r = new Random();
        for(int i = 0; i < numCars; i++) {
            // Generate random cars
            cars.add(generateNewCar());

            // Start out with completely random steering behavior
            behavior[Car.THRESHOLD_CHANGEDIR] = r.nextFloat();
            behavior[Car.ANGLE_CHANGEDIR] = r.nextFloat();
            behavior[Car.THRESHOLD_ACCELERATE] = r.nextFloat();
            behavior[Car.INCREMENT_ACCELERATE] = r.nextFloat();
            behavior[Car.THRESHOLD_BRAKE] = r.nextFloat();
            behavior[Car.INCREMENT_BRAKE] = -r.nextFloat();
            cars.get(i).setSteeringBehavior(behavior);
        }
    }

    /**
     * Updates the position and status of all cars. If no cars are alive anymore or the current generation as a whole has been alive
     * for the maximum run-time of each generation, a new generation will be created. The maximum run-time for each generation is set
     * to prevent cars getting stuck in an endless loop (moving in circles). It should not be set too low, as the algorightm will
     * otherwise disproportionately favor fast, but short-lived cars.
     * @param deltaMillis Time delta since the last update in milliseconds
     */
    public void update(long deltaMillis) {
        currentGenRunTime += deltaMillis;

        // Check whether any cars are still alive
        boolean stillAlive = false;
        for(int i = 0; i < cars.size(); i++) {
            if(cars.get(i).getStatus()) {
                cars.get(i).update(deltaMillis, level);
                stillAlive = true;
            }
        }

        // Break if this generation is just taking too long
        if(currentGenRunTime > 15000) {
            stillAlive = false;
        }

        // If all cars have crashed, generate the next generation
        if(!stillAlive && currentGeneration < generations) {
            if(waitCounter > 1000) {
                nextGeneration();
                currentGeneration++;
                waitCounter = 0;
                currentGenRunTime = 0;
            } else {
                waitCounter += deltaMillis;
            }
        }
    }

    /**
     * Draws the level and all cars to the screen
     * @param g Graphics2D object with which to do the drawing
     * @param startX Offset in pixel coordinates
     * @param startY Offset in pixel coordinates
     * @param scaleToX Size of the viewport to draw to in pixels
     * @param scaleToY Size of the viewport to draw to in pixels
     */
    public void draw(Graphics g, int startX, int startY, int scaleToX, int scaleToY) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw a line chart showing max dist and total dist history
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.black);
        g2d.drawRect(startX + (int) (scaleToX * 2 / 3), startY + (int) (scaleToY * 2 / 4), (int) (scaleToX * 2 / 3), (int) (scaleToY * 2 / 8));
        g2d.drawString("Maximum distance traveled", startX + (int) (scaleToX * 2 / 3) + 5, startY + (int) (scaleToY * 2 / 4) + 15);
        
        int xSpace = (int) ((scaleToX * 2 / 3) * 0.95f);
        int ySpace = (int) ((scaleToY * 2 / 8) * 0.95f - 20);
        int lineStartX = startX + (int) (scaleToX * 2 / 3) + (int) (xSpace / generations / 2);
        int lineStartY = startY + (int) (scaleToY * 2 / 4) + 20;
        if(currentGeneration > 0) {
            for(int i = 0; i < currentGeneration; i++) {
                if(i < (currentGeneration - 1)) {
                    float y1 = (float) maxDistHistory[i] / (float) level.getInnerVertices().length;
                    float y2 = (float) maxDistHistory[i + 1] / (float) level.getInnerVertices().length;
                    g2d.drawLine(lineStartX + i * (int) (xSpace / generations), lineStartY + (int) (( 1 - y1) * ySpace),
                                lineStartX + (i + 1) * (int) (xSpace / generations), lineStartY + (int) ((1 - y2) * ySpace));
                }
            }
        }

        g2d.drawRect(startX + (int) (scaleToX * 2 / 3), startY + (int) (scaleToY * 2 / 4) + (int) (scaleToY * 2 / 8) + 20, (int) (scaleToX * 2 / 3), (int) (scaleToY * 2 / 8));
        g2d.drawString("Total distance traveled", startX + (int) (scaleToX * 2 / 3) + 5, startY + (int) (scaleToY * 2 / 4)  + (int) (scaleToY * 2 / 8) + 20 + 15);
        
        lineStartY = startY + (int) (scaleToY * 2 / 4) + (int) (scaleToY * 2 / 8) + 20 + 20;
        float maxDist = 0;
        for(int i = 0; i < currentGeneration; i++) {
            if(totalDistHistory[i] > maxDist) maxDist = totalDistHistory[i];
        }

        if(currentGeneration > 0) {
            for(int i = 0; i < currentGeneration; i++) {
                if(i < (currentGeneration - 1)) {
                    float y1 = (float) totalDistHistory[i] / maxDist;
                    float y2 = (float) totalDistHistory[i + 1] / maxDist;
                    g2d.drawLine(lineStartX + i * (int) (xSpace / generations), lineStartY + (int) (( 1 - y1) * ySpace),
                                lineStartX + (i + 1) * (int) (xSpace / generations), lineStartY + (int) ((1 - y2) * ySpace));
                }
            }
        }

        g2d.drawRect(startX + (int) (scaleToX * 2 / 3), startY + (int) (scaleToY * 2 / 4) + 2 * (int) (scaleToY * 2 / 8) + 40, (int) (scaleToX * 2 / 3), (int) (scaleToY * 2 / 8));
        g2d.drawString("Total time traveled", startX + (int) (scaleToX * 2 / 3) + 5, startY + (int) (scaleToY * 2 / 4)  + 2 * (int) (scaleToY * 2 / 8) + 20 + 20 + 15);
        
        lineStartY = startY + (int) (scaleToY * 2 / 4) + 2 * (int) (scaleToY * 2 / 8) + 20 + 20 + 20;
        long maxTime = 0;
        for(int i = 0; i < currentGeneration; i++) {
            if(totalTimeHistory[i] > maxTime) maxTime = totalTimeHistory[i];
        }

        if(currentGeneration > 0) {
            for(int i = 0; i < currentGeneration; i++) {
                if(i < (currentGeneration - 1)) {
                    float y1 = (float) totalTimeHistory[i] / maxTime;
                    float y2 = (float) totalTimeHistory[i + 1] / maxTime;
                    g2d.drawLine(lineStartX + i * (int) (xSpace / generations), lineStartY + (int) (( 1 - y1) * ySpace),
                                lineStartX + (i + 1) * (int) (xSpace / generations), lineStartY + (int) ((1 - y2) * ySpace));
                }
            }
        }

        // Draw all cars
        for(int i = 0; i < cars.size(); i++) {
            cars.get(i).draw(g, startX, startY, scaleToX, scaleToY);
        }
    }

    /**
     * Sets up the next generation of cars by running tournament selection on the current generation. Each new car is generated
     * by crossing over two parent cars. Parent cars are selected based on fitness from a pool of a third of all cars.
     * Crossover is done in 2 blocks: one block for the directional steering and one for acceleration/braking. Mutation is applied
     * to all genes with strength decreasing over time.
     */
    private void nextGeneration() {
        ArrayList<Car> nextCars = new ArrayList<Car>();
        Random r = new Random();

        // Calculate maximum and total fitness
        maxDistHistory[currentGeneration] = totalDistHistory[currentGeneration] = totalTimeHistory[currentGeneration] = 0;
        for(int i = 0; i < cars.size(); i++) {
            if(cars.get(i).getDistanceTraveled() > maxDistHistory[currentGeneration]) {
                maxDistHistory[currentGeneration] = cars.get(i).getDistanceTraveled();
            }
            totalDistHistory[currentGeneration] += cars.get(i).getDistanceTraveled();
            totalTimeHistory[currentGeneration] += cars.get(i).getTimeTraveled();
        }

        // Generate a new generation of cars
        float[] behavior = new float[Car.NUM_PARAMETERS];
        for(int i = 0; i < cars.size(); i++) {
            // Use tournament selection to select parent cars (sample at least 3)
            Collections.shuffle(cars);
            int parent1 = 0, parent2 = 1;
            for(int j = 2; j < Math.max(3, (int) (cars.size() / 3)); j++) {
                int selection = selectParent(parent1, j);
                if(selection == parent1) {
                    parent2 = selectParent(parent2, j);
                } else {
                    parent1 = selection;
                }
            }

            // Create a new child car and use cross-over by parameter pair to generate its steering behavior
            nextCars.add(generateNewCar());
            if(r.nextFloat() < 0.5f) {
                behavior[Car.THRESHOLD_CHANGEDIR] = cars.get(parent1).getSteeringBehavior()[Car.THRESHOLD_CHANGEDIR];
                behavior[Car.ANGLE_CHANGEDIR] = cars.get(parent1).getSteeringBehavior()[Car.ANGLE_CHANGEDIR];
            } else {
                behavior[Car.THRESHOLD_CHANGEDIR] = cars.get(parent2).getSteeringBehavior()[Car.THRESHOLD_CHANGEDIR];
                behavior[Car.ANGLE_CHANGEDIR] = cars.get(parent2).getSteeringBehavior()[Car.ANGLE_CHANGEDIR];
            }

            if(r.nextFloat() < 0.5f) {
                behavior[Car.THRESHOLD_ACCELERATE] = cars.get(parent1).getSteeringBehavior()[Car.THRESHOLD_ACCELERATE];
                behavior[Car.INCREMENT_ACCELERATE] = cars.get(parent1).getSteeringBehavior()[Car.INCREMENT_ACCELERATE];
                behavior[Car.THRESHOLD_BRAKE] = cars.get(parent1).getSteeringBehavior()[Car.THRESHOLD_BRAKE];
                behavior[Car.INCREMENT_BRAKE] = cars.get(parent1).getSteeringBehavior()[Car.INCREMENT_BRAKE];
            } else {
                behavior[Car.THRESHOLD_ACCELERATE] = cars.get(parent2).getSteeringBehavior()[Car.THRESHOLD_ACCELERATE];
                behavior[Car.INCREMENT_ACCELERATE] = cars.get(parent2).getSteeringBehavior()[Car.INCREMENT_ACCELERATE];
                behavior[Car.THRESHOLD_BRAKE] = cars.get(parent2).getSteeringBehavior()[Car.THRESHOLD_BRAKE];
                behavior[Car.INCREMENT_BRAKE] = cars.get(parent2).getSteeringBehavior()[Car.INCREMENT_BRAKE];
            }

            // Add mutation with a decreasing impact and 50:50 chance of occurrence
            for(int j = 0; j < behavior.length; j++) {
                float mutationFactor = r.nextFloat() > 0.5 ? 1.0f / (currentGeneration + 1) : 0;
                behavior[j] += mutationFactor * (-0.5f + r.nextFloat());
            }

            nextCars.get(i).setSteeringBehavior(behavior);
        }

        cars = nextCars;
    }

    /**
     * Selects a parent from two cars by comparing their fitness values
     * @param candidate1 Index of the first car in the private cars array
     * @param candidate2 Index of the second car in the private cars array
     * @return Index of the selected car in the private cars array
     */
    private int selectParent(int candidate1, int candidate2) {
        if(cars.get(candidate1).getDistanceTraveled() > cars.get(candidate2).getDistanceTraveled()) {
            return candidate1;
        } else if(cars.get(candidate2).getDistanceTraveled() > cars.get(candidate1).getDistanceTraveled()) {
            return candidate2;
        } else if(cars.get(candidate1).getTimeTraveled() > cars.get(candidate2).getTimeTraveled()) {
            return candidate1;
        } else {
            return candidate2;
        }
    }

    /**
     * Generates a car with random (but valid) starting values
     * @return The generated car object
     */
    private Car generateNewCar() {
        // Generate a random valid position in the level
        Random r = new Random();

        int vertex = r.nextInt(level.getInnerVertices().length);
        float x = level.getInnerVertices()[vertex][0];
        float y = level.getInnerVertices()[vertex][1];
        
        if(x > 0) {
            x += 0.5f * CARWIDTH + r.nextFloat() * (trackWidth - 1.5f * CARWIDTH);
        } else {
            x -= 0.5 * CARWIDTH + r.nextFloat() * (trackWidth - 1.5f * CARWIDTH);
        }
        
        if(y > 0) {
            y += 0.5f * CARHEIGHT + r.nextFloat() * (trackWidth - 1.5f * CARHEIGHT);
        } else {
            y -= 0.5f * CARHEIGHT + r.nextFloat() * (trackWidth - 1.5f * CARHEIGHT);
        }
        
        return new Car(x, y, r.nextFloat(), r.nextFloat(), r.nextFloat() * 0.1f, CARWIDTH, CARHEIGHT);
    }
}
