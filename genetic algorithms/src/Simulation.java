import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import javax.swing.JPanel;

class Surface extends JPanel implements Runnable {    
    public static final float MARGIN = 0.2f;
    public static final float CARWIDTH = 0.035f, CARHEIGHT = 0.065f;
    public static final float TRACKWIDTH = 0.125f;
    private Level level;
    private GeneticAlgorithm ga;
    private int lastWidth, lastHeight;
    private Thread animator;
    private Boolean running;
    
    public Surface() {
        level = new Level(50, 0.05f, TRACKWIDTH);
        ga = new GeneticAlgorithm(5, 5, level, TRACKWIDTH);
        lastWidth = lastHeight = 0;

        start();
    }
    
    private void draw(Graphics g) {
        if(getWidth() != lastWidth || getHeight() != lastHeight) {
            lastWidth = getWidth();
            lastHeight = getHeight();
            
            // Rescale level if window has been rescaled
            level.rescale((int) (lastWidth * (1 - MARGIN) / 2), (int) (lastHeight * (1 - MARGIN) / 2));
        }
        
        int offsetX = (int) ((getWidth() * MARGIN) / 2);
        int offsetY = (int) ((getHeight() * MARGIN) / 2);

        level.draw(g, offsetX, offsetY);
        ga.draw(g, offsetX, offsetY, (int) (lastWidth * (1 - MARGIN) / 2), (int) (lastHeight * (1 - MARGIN) / 2));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void start() {
        animator = new Thread(this);
        animator.start();
        running = true;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        long beforeTime, timeDiff;

        beforeTime = System.currentTimeMillis();

        while(running) {
            timeDiff = System.currentTimeMillis() - beforeTime;

            // Ensure max frame rate of 60 FPS
            if(timeDiff < 1000 / 60) {
                try {
                    Thread.sleep(1000 / 60 - timeDiff);
                    timeDiff = 1000 / 60;
                } catch(InterruptedException e) {
                    // Empty for now
                }
            }

            ga.update(timeDiff, level);
            repaint();

            beforeTime = System.currentTimeMillis();
        }
    }
}

public class Simulation extends JFrame {
    private Surface surface;

    public Simulation() {
        initUI();
    }

    private void initUI() {
        surface = new Surface();
        add(surface);

        setTitle("Self-driving car simulation");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Simulation s = new Simulation();
                s.setVisible(true);
            }
        });
    }
}
