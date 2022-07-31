import java.util.Random;
import java.lang.Math;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.geom.GeneralPath;

public class Level {
    // The edge points making up the "graph" of the level (x, y)
    private int[][] innerVertices;
    private int[][] outerVertices;

    /**
     * Creates a Level object using the supplied parameters
     * @param numPoints The number of points to distribute on a unit circle to form the level polygon
     * @param variability How much to randomly shift each individual point by to make the level less regular (use small values like 0.05F)
     * @param trackWidth The scaling factor by which to make the outer edge of the level larger than the inner (determining how wide the track is); typical values between 0.05f and 0.2f
     * @param scaleToX The width in pixels the level should have in the window it is going to be displayed in
     * @param scaleToY The height in pixels the level should have in the window it is going to be displayed in
     */
    public Level(int numPoints, float variability, float trackWidth, int scaleToX, int scaleToY) {
        float minX = 0, maxX = 0, minY = 0, maxY = 0;
        
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
        // Shift the center of the unit circle by 1 unit in order to ensure all coordinates are positive
        innerVertices = new int[numPoints][2];
        outerVertices = new int[numPoints][2];
        for(int i = 0; i < numPoints; i++) {
            innerVertices[i][0] = (int) ((points[i][0] + 1) * (scaleToX / (maxX - minX)));
            innerVertices[i][1] = (int) ((points[i][1] + 1) * (scaleToY / (maxY - minY)));
            outerVertices[i][0] = (int) ((points[i][0] * (1 + trackWidth) + 1) * (scaleToX / (maxX - minX)));
            outerVertices[i][1] = (int) ((points[i][1] * (1+ trackWidth) + 1) * (scaleToY / (maxY - minY)));
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

        g2d.setPaint(Color.red);
        g2d.translate(startX, startY);
        
        // Draw the inner line of the track
        GeneralPath outline = new GeneralPath();
        outline.moveTo(innerVertices[0][0], innerVertices[0][1]);
        for(int i = 1; i < innerVertices.length; i++) {
            outline.lineTo(innerVertices[i][0], innerVertices[i][1]);
        }
        outline.closePath();
        g2d.draw(outline);

        // Draw the outer line of the track
        g2d.setPaint(Color.blue);
        outline = new GeneralPath();
        outline.moveTo(outerVertices[0][0], outerVertices[0][1]);
        for(int i = 1; i < outerVertices.length; i++) {
            outline.lineTo(outerVertices[i][0], outerVertices[i][1]);
        }
        outline.closePath();
        g2d.draw(outline);
        
        g2d.dispose();
    }
}
