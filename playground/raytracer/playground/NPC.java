package playground;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class NPC {
	private int xPos, yPos;
	private int[] pixels;
	private int width, height;
	
	public NPC(String path, int xp, int yp) {
		xPos = xp;
		yPos = yp;
		
		try {
			BufferedImage image = ImageIO.read(new File(path));
			width = image.getWidth();
			height = image.getHeight();
			pixels = new int[width * height];
			image.getRGB(0,  0, width, height, pixels, 0, width);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void render(Camera camera, int[] screenBuffer, Screen screen) {
		double distance = Math.sqrt((xPos - camera.xPos) * (xPos - camera.xPos)
				+ (yPos - camera.yPos) * (yPos - camera.yPos));
		int scaledHeight = (int)(height / distance);
		int scaledWidth = (int)(width / distance);
		
		if(scaledHeight > screen.height) 
			scaledHeight = screen.height;
		
		if(scaledWidth > screen.width)
			scaledWidth = screen.width;
		
		for(int y = 0; y < scaledHeight; y++) {
			for(int x = 0; x < scaledWidth; x++) {
				int texX = (int)(x * width / scaledWidth);
				int texY = (int)(y * height / scaledHeight);
				
				if(pixels[texY * width + texX] != 0)
					screenBuffer[y * screen.width + x] = pixels[texY * width + texX];
			}
		}
		
	}
}
