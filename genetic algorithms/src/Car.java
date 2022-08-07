import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.Math;

public class Car {
    private float x, y;
    private float dirX, dirY;
    private float speed;
    private float width, height;
    private Color color;
    private Boolean alive;
    private float leftMinDist, rightMinDist;
    private float[] steeringBehavior;
    private float distanceTraveled;
    private long timeTraveled;
    private final int THRESHOLD_CHANGEDIR = 0;
    private final int ANGLE_CHANGEDIR = 1;
    private final int THRESHOLD_ACCELERATE = 2;
    private final int INCREMENT_ACCELERATE = 3;
    private final int THRESHOLD_BRAKE = 4;
    private final int INCREMENT_BRAKE = 5;

    /**
     * Constructs a new car
     * @param startX Starting position in the (-1, 1) coordinate system
     * @param startY Starting position in the (-1, 1) coordinate system
     * @param startDirX Initial direction vector (-1, 1) coordinate system; will be multiplied with speed and added to the position
     * @param startDirY Initial direction vector (-1, 1) coordinate system; will be multiplied with speed and added to the position
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

        leftMinDist = rightMinDist = 9999.0f;
        steeringBehavior = new float[5];
        distanceTraveled = 0;
        timeTraveled = 0;

        // Dummy values for testing
        steeringBehavior[THRESHOLD_CHANGEDIR] = 0.2f;
        steeringBehavior[ANGLE_CHANGEDIR] = 0.1f;
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
     * Draws the car to a viewport of the size defined by the scaling parameters
     * @param g Graphics2D object to perform the drawing
     * @param startX The offset within the viewport from which to start drawing
     * @param startY The offset within the viewport from which to start drawing
     * @param scaleToX The viewport size in pixels to which to draw the car
     * @param scaleToY The viewport size in pixels to which to draw the car
     */
    public void draw(Graphics g, int startX, int startY, int scaleToX, int scaleToY) {
        Graphics2D g2d = (Graphics2D) g.create();

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
                perceive(l, 4);
                
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

                distanceTraveled += Math.sqrt(incX * incX + incY * incY);
                x += incX;
                y += incY;
                timeTraveled += deltaMillis;
            }
        }
    }

    /**
     * Tests whether the car has crossed the inner or outer boundaries of the level. It does so by determining the 
     * nearest line segment of the level and then using the dot product to determine on which side of the line the
     * car currently is.
     * @param l The Level object against which to test
     * @return True if a collision has occurred, false otherwise
     */
    public Boolean collision(Level l) {
        int closestInner, closestOuter;
        float dotInner, dotOuter;

        // Test collision against inner circle
        closestInner = getNearestLine(l.getInnerVertices());
        dotInner = dotProduct(l.getInnerVertices(), closestInner);

        // Test collision against outer circle
        closestOuter = getNearestLine(l.getOuterVertices());
        dotOuter = dotProduct(l.getOuterVertices(), closestOuter);

        // Upper half of the level
        /*
        if(y < 0 && (dotInner > 0 || dotOuter < 0)) {
            System.out.println("Collision detected at (" + x + ", " + y + ")");
            return true;
        }
        // Lower half of the level
        if(y >= 0 && (dotInner < 0 || dotOuter > 0)) {
            System.out.println("Collision detected at (" + x + ", " + y + ")");
            return true;
        }
        */
        if(dotInner < 0 || dotOuter > 0) {
            System.out.println("Collision detected at (" + x + ", " + y + ")");
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

    public void perceive(Level l, int numRays) {
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
                
            // Make the rays 1 unit long (for now)
            float rx2 = rx1 + dirX * 1;
            float ry2 = ry1 + dirY * 1;

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
