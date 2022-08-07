import java.util.Random;
import java.lang.Math;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.geom.GeneralPath;

public class Level {
    // The edge points making up the "graph" of the level (x, y)
    private float[][] innerVertices, outerVertices;
    private int[][] innerVerticesScaled, outerVerticesScaled;
    
    // THe size of the level in a (-1, 1) coordinate system, measuring the inner circle (!)
    private float minX, maxX, minY, maxY;

    /**
     * Creates a Level object using the supplied parameters
     * @param numPoints The number of points to distribute on a unit circle to form the level polygon
     * @param variability How much to randomly shift each individual point by to make the level less regular (use small values like 0.05F)
     * @param trackWidth The scaling factor by which to make the outer edge of the level larger than the inner (determining how wide the track is); typical values between 0.05f and 0.2f
     */
    public Level(int numPoints, float variability, float trackWidth) {
        minX = maxX = minY = maxY = 0.0f;
        
        if(numPoints < 3) {
            throw new IllegalArgumentException("numPoints must be at least 3");
        }
        
        // Create the list of points making up the level polygon and set the first point at (0, 0); since this is before scaling, these coordinates do not really matter
        float[][] points = new float[numPoints][2];
        points[0][0] = 0;
        points[0][1] = 0;

        // Points are placed on a unit circle (shifted by one to make all coordinates positive) and then "jiggled" based on the variability parameter
        Random r = new Random();
        for(int i = 0; i < numPoints; i++) {
            // Place points on unit circle
            points[i][0] = (float) Math.cos(2 * i * Math.PI / numPoints);
            points[i][1] = (float) Math.sin(2 * i * Math.PI / numPoints);
            
            // "Jiggle" points based on variability (essentially scaling them relative to the center of the circle)
            points[i][0] *= (1 - variability + 2 * r.nextFloat() * variability);
            points[i][1] *= (1 - variability + 2 * r.nextFloat() * variability);

            if(points[i][0] < minX) minX = points[i][0];
            if(points[i][0] > maxX) maxX = points[i][0];
            if(points[i][1] < minY) minY = points[i][1];
            if(points[i][1] > maxY) maxY = points[i][1];
        }

        // Determine rescaling factor by comparing generated level size to desired level size and scaling all points
        // At the same time, convert to integer space (pixel coordinates) and copy into private class attribute
        innerVertices = new float[numPoints][2];
        innerVerticesScaled = new int[numPoints][2];
        outerVertices = new float[numPoints][2];
        outerVerticesScaled = new int[numPoints][2];
        for(int i = 0; i < numPoints; i++) {
            innerVertices[i][0] = points[i][0];
            innerVertices[i][1] = points[i][1];
            outerVertices[i][0] = points[i][0] * (1 + trackWidth);
            outerVertices[i][1] = points[i][1] * (1+ trackWidth);
        }
    }

    /**
     * Rescales the level from a (-1, 1) coordinate system based on the shifted unit circle to a 
     * pixel coordinate system (shifting the center from (0, 0) to (1, 1) to ensure all coordinates are positive)
     * @param scaleToX Desired level width in pixels
     * @param scaleToY Desired level height in pixels
     */
    public void rescale(int scaleToX, int scaleToY) {
        for(int i = 0; i < innerVertices.length; i++) {
            innerVerticesScaled[i][0] = (int) ((innerVertices[i][0] + 1) * scaleToX);
            innerVerticesScaled[i][1] = (int) ((innerVertices[i][1] + 1) * scaleToY);
            outerVerticesScaled[i][0] = (int) ((outerVertices[i][0] + 1) * scaleToX);
            outerVerticesScaled[i][1] = (int) ((outerVertices[i][1] + 1) * scaleToY);
        }
    }

    /**
     * Draws the level to the screen (inner and outer edge) using the supplied Graphics2D object
     * @param g A Graphics2D object to use for drawing
     * @param startX The X coordinate from which to start drawing the level (i.e., offset from (0, 0))
     * @param startY The Y coordinate from which to start drawing the level (i.e., offset from (0, 0))
     */
    public void draw(Graphics g, int startX, int startY) {
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setPaint(Color.blue);
        g2d.translate(startX, startY);
        
        // Draw the inner line of the track
        GeneralPath outline = new GeneralPath();
        outline.moveTo(innerVerticesScaled[0][0], innerVerticesScaled[0][1]);
        g2d.drawString("0", innerVerticesScaled[0][0] + 5, innerVerticesScaled[0][1] + 5);
        for(int i = 1; i < innerVerticesScaled.length; i++) {
            outline.lineTo(innerVerticesScaled[i][0], innerVerticesScaled[i][1]);
            g2d.drawString("" + i, innerVerticesScaled[i][0] + 5, innerVerticesScaled[i][1] + 5);
        }
        outline.closePath();
        g2d.draw(outline);

        // Draw the outer line of the track
        g2d.setPaint(Color.blue);
        outline = new GeneralPath();
        outline.moveTo(outerVerticesScaled[0][0], outerVerticesScaled[0][1]);
        for(int i = 1; i < outerVerticesScaled.length; i++) {
            outline.lineTo(outerVerticesScaled[i][0], outerVerticesScaled[i][1]);
        }
        outline.closePath();
        g2d.draw(outline);
        
        g2d.dispose();
    }

    public float getMinX() {
        return minX;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxY() {
        return maxY;
    }

    public float[][] getInnerVertices() {
        return innerVertices;
    }

    public float[][] getOuterVertices() {
        return outerVertices;
    }
}
