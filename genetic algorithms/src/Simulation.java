import java.awt.*;
import javax.swing.*;

public class Simulation {
    private static void createWindow() {
        JFrame frame = new JFrame("Self-driving car simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel placeholder = new JLabel("I am placeholder content", SwingConstants.CENTER);
        placeholder.setPreferredSize(new Dimension(640, 280));
        frame.getContentPane().add(placeholder, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        createWindow();
    }
}
