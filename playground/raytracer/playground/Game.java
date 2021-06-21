package playground;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import javax.swing.JFrame;

public class Game extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;
	public int mapWidth = 16;
	public int mapHeight = 16;
	private Thread thread;
	private boolean running;
	private BufferedImage image;
	public int [] pixels;
	public static int[][] map = 
		{
				{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
				{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
				{1, 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
				{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
		};
	public ArrayList<Texture> textures;
	public Camera camera;
	public Screen screen;
	public NPC[] npcs;
	
	public Game() {
		thread = new Thread(this);
		image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		
		setSize(640, 480);
		setResizable(false);
		setTitle("RayCasting experiment");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBackground(Color.black);
		setLocationRelativeTo(null);
		setVisible(true);
		
		textures = new ArrayList<Texture>();
		textures.add(Texture.wood);
		textures.add(Texture.stone);
		textures.add(Texture.bluestone);
		textures.add(Texture.brick);
		
		camera = new Camera(4.5, 4.5, 1, 0, 0, -0.66);
		addKeyListener(camera);
		
		screen = new Screen(map, mapWidth, mapHeight, textures, 640, 480);
		npcs = new NPC[1];
		npcs[0] = new NPC("C:\\Users\\Joachim\\git\\LearningJava\\playground\\raytracer\\playground\\res\\demon_cartoon_small.png", 7, 9);
		
		start();
	}
	
	private synchronized void start() {
		running = true;
		thread.start();
	}
	
	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
		bs.show();
	}
	
	public void run() {
		long lastTime = System.nanoTime();
		final double ns = 1000000000.0 / 60.0;
		double delta = 0;
		
		requestFocus();
		while(running) {
			long now = System.nanoTime();
			delta = delta + ((now - lastTime) / ns);
			lastTime = now;
			
			while(delta >= 1) {
				screen.update(camera, pixels, npcs);
				camera.update(map);
				delta--;
			}
			
			render();
		}
	}
	
	public static void main(String[] args) {
		Game game = new Game();
	}

}
