import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import javax.swing.JPanel;

class Surface extends JPanel {
    private Level level;
    private final int levelX = 640;
    private final int levelY = 480;
    
    public Surface() {
        level = new Level(50, 0.05f, 0.125f, levelX, levelY);
    }
    
    private void draw(Graphics g) {
        level.draw(g, (int)((getWidth() - levelX) / 2), (int)((getHeight() - levelY) / 2));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
}

public class Simulation extends JFrame {
    public Simulation() {
        initUI();
    }

    private void initUI() {
        add(new Surface());

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
