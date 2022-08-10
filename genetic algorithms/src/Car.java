import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.Math;

public class Car {
    private float x, y;
    private float dirX, dirY;
    private float speed;
    private float width, height;
    private Color color;
    private boolean alive;
    private float leftMinDist, rightMinDist;
    private float[] steeringBehavior;
    private long timeTraveled;
    private int[] segmentsPassed;
    public static final int THRESHOLD_CHANGEDIR = 0;
    public static final int ANGLE_CHANGEDIR = 1;
    public static final int THRESHOLD_ACCELERATE = 2;
    public static final int INCREMENT_ACCELERATE = 3;
    public static final int THRESHOLD_BRAKE = 4;
    public static final int INCREMENT_BRAKE = 5;
    public static final int NUM_PARAMETERS = 6;

    /**
     * Constructs a new car
     * @param startX Starting position in the (-1, 1) coordinate system
     * @param startY Starting position in the (-1, 1) coordinate system
     * @param startDirX Initial direction vector (-1, 1) coordinate system; will be multiplied with speed and added to the position;
     *                  this vector will always be normalized by the class itself
     * @param startDirY Initial direction vector (-1, 1) coordinate system; will be multiplied with speed and added to the position;
     *                  this vector will always be normalized by the class itself
     * @param startSpeed Initial speed of the car in pixels per second
     * @param drawWidth Size of the car in a (-1, 1) coordinate system; will then be scaled to pixel coordinates
     * @param drawHeight Size of the car in a (-1, 1) coordinate system; will then be scaled to pixel coordinates
     */
    public Car(float startX, float startY, float startDirX, float startDirY, float startSpeed, float drawWidth, float drawHeight) {
        x = startX;
        y = startY;

        // Normalize direction vector
        float length = (float) Math.sqrt(startDirX * startDirX + startDirY * startDirY);       
        dirX = startDirX / length;
        dirY = startDirY / length;     
        
        speed = startSpeed;
        width = drawWidth;
        height = drawHeight;
        color = Color.black;
        alive = true;

        // Since all coordinates should be in a space between -1.5 and +1.5, this should be safe
        leftMinDist = rightMinDist = 9999.0f;
        timeTraveled = 0;
    }

    /**
     * Gives the car parameters for when to change direction (and how much) as well as when to accelerate/brake (and how much).
     * @param behavior An array of size Car.NUM_PARAMETERS that contains the steering parameters specified by the public variables
     *                  Car.THRESHOLD_CHANGEDIR, Car.ANGLE_CHANGEDIR (in radians), etc.
     */
    public void setSteeringBehavior(float[] behavior) {
        steeringBehavior = new float[NUM_PARAMETERS];
        for(int i = 0; i < steeringBehavior.length; i++) {
            steeringBehavior[i] = behavior[i];
        }
    }

    public float[] getSteeringBehavior() {
        return steeringBehavior;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float newWidth) {
        width = newWidth;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float newHeight) {
        height = newHeight;
    }

    /**
     * Returns the distance traveled measured in line segments the car has passed by. These are counted only once, so the distance
     * is capped at the number of line segments making up the level
     * @return Number of inner line segments of the level the car has passed
     */
    public int getDistanceTraveled() {
        int dist = 0;

        if(null == segmentsPassed) return 0;

        for(int i = 0; i < segmentsPassed.length; i++) {
            dist += segmentsPassed[i];
        }

        return dist;
    }

    /**
     * Returns the time the car has been alive
     * @return Time in milliseconds
     */
    public long getTimeTraveled() {
        return timeTraveled;
    }

    public boolean getStatus() {
        return alive;
    }

    /**
     * Draws the car to a viewport of the size defined by the scaling parameters
     * @param g Graphics2D object to perform the drawing
     * @param startX The offset within the viewport from which to start drawing
     * @param startY The offset within the viewport from which to start drawing
     * @param scaleToX The viewport size in pixels to which to draw the car
     * @param scaleToY The viewport size in pixels to which to draw the car
     */
    public void draw(Graphics g, int startX, int startY, int scaleToX, int scaleToY) {
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);

        AffineTransform at = new AffineTransform();
        // Translate car position into drawing (pixel) coordinate system (adding 1 to ensure all coordinates are positive)
        at.translate(startX + (x + 1) * scaleToX, startY + (y + 1) * scaleToY);
        
        // Have the car face in the direction it is driving in
        at.rotate(-dirY, dirX);

        g2d.transform(at);

        g2d.fillPolygon(new int[] {0, (int) (width/2 * scaleToX), (int) (-width/2 * scaleToX)}, 
                        new int[] {(int) (-height/2 * scaleToY), (int) (height/2 * scaleToY), (int) (height/2 * scaleToY)},
                        3);

        g2d.dispose();

        // For debug: draw perception rays
        /*
        g2d = (Graphics2D) g.create();
        g2d.setColor(Color.red);
        g2d.translate(startX, startY);
        g2d.drawString("Shortest Distance left: " + leftMinDist, scaleToX / 2, scaleToY / 2);
        g2d.drawString("Shortest Distance right: " + rightMinDist, scaleToX / 2, scaleToY / 2 + 12);
        g2d.drawString("Distance traveled: " + distanceTraveled, scaleToX / 2, scaleToY / 2 + 24);
        g2d.drawString("Time traveled: " + timeTraveled, scaleToX / 2, scaleToY / 2 + 36);
        g2d.drawString("Direcation: (" + dirX + ", " + dirY + ")", scaleToX / 2, scaleToY / 2 + 48);
        for(int i = 0; i < 4; i++) {
            // Start with horizontally distributed points in the local coordinate space of the car
            float rx1 = -width / 2 + (width / 3) * i;
            float ry1 = 0;

            // Rotate these points around the origin of the local coordinate system of the car
            // Start with the angle to the Y axis that the car currently has
            float theta = (float) (Math.atan2(-dirX, dirY));
            // Rorate the points and shift them to the car's coordinates in the (-1, 1) coordinate system shared with the Level
            float temp = (float) (rx1 * Math.cos(theta) - ry1 * Math.sin(theta));
            ry1  = y + (float) (rx1 * Math.sin(theta) + ry1 * Math.cos(theta));
            rx1 = x + temp;
                
            // Make rays 1 unit long (for now)
            float rx2 = rx1 + dirX * 1;
            float ry2 = ry1 + dirY * 1;

            g2d.drawLine((int) ((rx1 + 1) * scaleToX), (int) ((ry1 + 1) * scaleToY), 
                        (int) ((rx2 + 1) * scaleToX), (int) ((ry2 + 1) * scaleToY));
        }
        g2d.dispose();
        */
    }

    /**
     * Updates the cars position and tests for collision with the level; will stop the car if it has collided
     * @param deltaMillis Time since last update in milliseconds (speed is measured in pixel per second, though)
     * @param l Level against which to test collision
     */
    public void update(long deltaMillis, Level l) {
        if(alive) {
            if(collision(l)) {
                alive = false;
                color = Color.red;
            } else {
                // Measure distance to the wall of the level
                perceive(l, 4, 1);
                
                // Steering is performed by turning the car at an angle specified as one of the steering parameters
                if(leftMinDist < steeringBehavior[THRESHOLD_CHANGEDIR]) {
                    float newDirX = (float) (dirX * Math.cos(steeringBehavior[ANGLE_CHANGEDIR])
                                    - dirY * Math.sin(steeringBehavior[ANGLE_CHANGEDIR]));
                    float newDirY = (float) (dirX * Math.sin(steeringBehavior[ANGLE_CHANGEDIR])
                                    + dirY * Math.cos(steeringBehavior[ANGLE_CHANGEDIR]));
                    float length = (float) Math.sqrt(newDirX * newDirX + newDirY * newDirY);
                    dirX = newDirX / length;
                    dirY = newDirY / length; 
                } else if(rightMinDist < steeringBehavior[THRESHOLD_CHANGEDIR]) {
                    float newDirX = (float) (dirX * Math.cos(-steeringBehavior[ANGLE_CHANGEDIR])
                                    - dirY * Math.sin(-steeringBehavior[ANGLE_CHANGEDIR]));
                    float newDirY = (float) (dirX * Math.sin(-steeringBehavior[ANGLE_CHANGEDIR])
                                    + dirY * Math.cos(-steeringBehavior[ANGLE_CHANGEDIR]));
                    float length = (float) Math.sqrt(newDirX * newDirX + newDirY * newDirY);
                    dirX = newDirX / length;
                    dirY = newDirY / length;
                }

                if(Math.min(leftMinDist, rightMinDist) < steeringBehavior[THRESHOLD_ACCELERATE]) {
                    speed += steeringBehavior[INCREMENT_ACCELERATE]; 
                }

                if(Math.min(leftMinDist, rightMinDist) < steeringBehavior[THRESHOLD_BRAKE]) {
                    speed += steeringBehavior[INCREMENT_BRAKE];
                }
                
                float incX = dirX * speed * ((float) deltaMillis / 1000);
                float incY = dirY * speed * ((float) deltaMillis / 1000);

                x += incX;
                y += incY;
                timeTraveled += deltaMillis;
            }
        }
    }

    /**
     * Tests whether the car has crossed the inner or outer boundaries of the level. It does so by determining the 
     * nearest line segment of the level and then using the dot product to determine on which side of the line the
     * car currently is. Also measures the distance traveled by keeping log of the line segments the car has passed.
     * @param l The Level object against which to test
     * @return True if a collision has occurred, false otherwise
     */
    public Boolean collision(Level l) {
        int closestInner, closestOuter;
        float dotInner, dotOuter;

        // Test collision against inner circle
        closestInner = getNearestLine(l.getInnerVertices());
        dotInner = dotProduct(l.getInnerVertices(), closestInner);

        // Use collision testing also for travel distance measurement
        if(null == segmentsPassed) {
            segmentsPassed = new int[l.getInnerVertices().length];
        }
        segmentsPassed[closestInner] = 1;

        // Test collision against outer circle
        closestOuter = getNearestLine(l.getOuterVertices());
        dotOuter = dotProduct(l.getOuterVertices(), closestOuter);

        // Test whether car is on the "right side of the line"
        if(dotInner < 0 || dotOuter > 0) {
            //System.out.println("Collision detected at (" + x + ", " + y + ")");
            return true;
        }
        return false;
    }

    /**
     * Determines the nearest line segment by projecting the car position (x, y) onto the line segments of the level
     * and then calcilating the distance
     * @param vertices An array of float values making up the points of the level edge
     * @return Index in the above array denoting the starting vertex of the closest line segment
     */
    private int getNearestLine(float[][] vertices) {
        float curX, curY, nextX, nextY;
        int indexClosest = 0;
        float shortestDist = 9999.0f;

        for(int i = 0; i < vertices.length; i++) {
            // Get current vertex
            curX = vertices[i][0];
            curY = vertices[i][1];

            // Get next vertex, wrapping to the first one at the end
            if(i + 1 < vertices.length) {
                nextX = vertices[i + 1][0];
                nextY = vertices[i + 1][1];
            } else {
                nextX = vertices[0][0];
                nextY = vertices[0][1];
            }

            // Calculate distance between car and the current line segment of the level
            float len_sq = (nextX - curX) * (nextX - curX) + (nextY - curY) * (nextY - curY);
            float t = ((x - curX) * (nextX - curX) + (y - curY) * (nextY - curY)) / len_sq;
            t = Math.max(0, Math.min(1, t));
            float lx = curX + t * (nextX - curX);
            float ly = curY + t * (nextY - curY);
            float dist = (float) Math.sqrt((x - lx) * (x - lx) + (y - ly) * (y - ly));
                     
            if(dist < shortestDist) {
                shortestDist = dist;
                indexClosest = i;
            }
        }

        return indexClosest;
    }

    /**
     * Calculates the dot product between the car position and a line segment to indicate on which side of the line the car is
     * @param vertices An array of points making up the level edge
     * @param index Index into the above array specifying the starting point of the line segment for which to calculate the dot product
     * @return The dot product between car position and line segment
     */
    private float dotProduct(float[][] vertices, int index) {
        float curX, curY, nextX, nextY;

        curX = vertices[index][0];
        curY = vertices[index][1];

        // Get next vertex, wrapping to the first one at the end
        if(index + 1 < vertices.length) {
            nextX = vertices[index + 1][0];
            nextY = vertices[index + 1][1];
        } else {
            nextX = vertices[0][0];
            nextY = vertices[0][1];
        }

        // Calculate dot product between the car center and the line segment to see on which "side" the car is
        return (nextX - curX) * (curY - y) - (curX - x) * (nextY - curY);
    }

    /**
     * Measures the minimum distance to the next wall on the left and right side of the car and saves this in the respective
     * private class attributes
     * @param l The level against which to measure distances
     * @param numRays How many rays to send out from the car (half of which will be measuring the left-side distance and half
     * of which will be measuring the right-side distance)
     * @param rayLength The length of the ray in units (i.e., the distance the car can see)
     */
    public void perceive(Level l, int numRays, float rayLength) {
        float shortestDistLeft = 9999.0f;
        float shortestDistRight = 9999.0f;

        // Create equally spaced rays from the base of the car that extend 2 units into the direction of travel
        for(int i = 0; i < numRays; i++) {
            // Start with horizontally distributed points in the local coordinate space of the car
            float rx1 = -width / 2 + (width / (numRays - 1)) * i;
            float ry1 = 0;

            // Rotate these points around the origin of the local coordinate system of the car
            // Start with the angle to the Y axis that the car currently has
            float theta = (float) (Math.atan2(-dirX, dirY));
            // Rorate the points and shift them to the car's coordinates in the (-1, 1) coordinate system shared with the Level
            float temp = (float) (rx1 * Math.cos(theta) - ry1 * Math.sin(theta));
            ry1  = y + (float) (rx1 * Math.sin(theta) + ry1 * Math.cos(theta));
            rx1 = x + temp;
                
            // Elongate the rays to the desired length
            float rx2 = rx1 + dirX * rayLength;
            float ry2 = ry1 + dirY * rayLength;

            float distInner = distanceToWall(rx1, ry1, rx2, ry2, l.getInnerVertices());
            float distOuter = distanceToWall(rx1, ry1, rx2, ry2, l.getOuterVertices());

            if(i >= numRays / 2) {
                if(distInner < shortestDistLeft) {
                    shortestDistLeft = distInner;
                }

                if(distOuter < shortestDistLeft) {
                    shortestDistLeft = distOuter;
                }
            } else {
                if(distInner < shortestDistRight) {
                    shortestDistRight = distInner;
                }

                if(distOuter < shortestDistRight) {
                    shortestDistRight = distOuter;
                }
            }
        }

        leftMinDist = shortestDistLeft;
        rightMinDist = shortestDistRight;
    }

    /**
     * Uses the ray specified by the first 4 coordinates to compute whether it touches a wall of the level and how far away
     * that wall is.
     * @param rx1 Starting point of the ray
     * @param ry1 Starting point of the ray
     * @param rx2 Ending point of the ray
     * @param ry2 Ending point of the ray
     * @param vertices Vertices making up the wall of the level to test against (can be inner or outer wall)
     * @return If the ray hits a wall, distance to that wall in units; otherwise zero
     */
    private float distanceToWall(float rx1, float ry1, float rx2, float ry2, float[][] vertices) {
        float shortestDist = 9999.0f;
        float curX, curY, nextX, nextY;

        for(int i = 0; i < vertices.length; i++) {
            // Get current vertex
            curX = vertices[i][0];
            curY = vertices[i][1];

            // Get next vertex, wrapping to the first one at the end
            if(i + 1 < vertices.length) {
                nextX = vertices[i + 1][0];
                nextY = vertices[i + 1][1];
            } else {
                nextX = vertices[0][0];
                nextY = vertices[0][1];
            }

            float dist = lineDist(rx1, ry1, rx2, ry2, curX, curY, nextX, nextY);
            if(dist < shortestDist) {
                shortestDist = dist;
            }
        }
        
        return shortestDist;
    }

    /**
     * Computes whether two lines intersect and where that intersection point is
     * @param x1 Starting point of the first line
     * @param y1 Starting point of the first line
     * @param x2 Ending point of the first line
     * @param y2 Ending point of the first line
     * @param x3 Starting point of the second line
     * @param y3 Starting point of the second line
     * @param x4 Ending point of the second line
     * @param y4 Ending point of the second line
     * @return Distance from the origin of the first line to the intersection point with the second line; 9999.0f if no
     *          intersection is present
     */
    private float lineDist(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        float t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));
        float u = ((x1 - x3) * (y1 - y2) - (y1 - y3) * (x1 - x2)) / ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));

        // Test for intersection
        if(t >= 0 && t <= 1 && u >= 0 && u <= 1) {
            // Determine the distance from the origin point of the first line to the intersection point
            float ix = x1 + t * (x2 - x1);
            float iy = y1 + t * (y2 - y1);

            return (float) Math.sqrt((x1 - ix) * (x1 - ix) + (y1 - iy) * (y1 - iy));
        }

        // No intersection
        return 9999.0f;
    }
}
