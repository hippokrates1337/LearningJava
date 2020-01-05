package playground;

import org.w3c.dom.Text;

public class HelloWorld {

	public static void createDataSet() {
		train = new ColorPoint[100];
		test = new ColorPoint [100];
		
		for(int i = 0; i < 100; i++) {
			train[i] = new ColorPoint();
			test[i] = new ColorPoint();
			if(java.lang.Math.random() > 0.5) {
				train[i].x = -10 + (int)(10 * java.lang.Math.random());
				train[i].y = (int)(10 * java.lang.Math.random());
				train[i].color = 1;
				
				test[i].x = -10 + (int)(10 * java.lang.Math.random());
				test[i].y = (int)(10 * java.lang.Math.random());
				test[i].color = 1;
			} else {
				train[i].x = (int)(10 * java.lang.Math.random());
				train[i].y = -(int)(10 * java.lang.Math.random());
				train[i].color = 0;
				
				test[i].x = (int)(10 * java.lang.Math.random());
				test[i].y = -(int)(10 * java.lang.Math.random());
				test[i].color = 0;
			}
		}
	}
	
	public static void predict() {
		double distance, bestDistance;
		int nearestNeighbor = 0;
		int correctPredictions = 0;
		
		for(int i = 0; i < 100; i++) {
			distance = 0;
			bestDistance = 1000;
			
			for(int j = 0; j < 100; j++) {
				distance = java.lang.Math.sqrt(java.lang.Math.pow(test[i].x - train[j].x, 2) + java.lang.Math.pow(test[i].y - train[j].y, 2));
				if(distance < bestDistance) {
					bestDistance = distance;
					nearestNeighbor = j;
				}
			}
			test[i].predictedColor = train[nearestNeighbor].color;
			if (test[i].predictedColor == test[i].color) {
				correctPredictions++;
			} 
		}
		
		accuracy = (double)(correctPredictions)/100;
	}
	
	public static void main(String[] args) {
		createDataSet();
		predict();

		for(int i = 0; i < 100; i++) {
			System.out.println("{" + test[i].x + ", " + test[i].y + ", " + test[i].color + ", " + test[i].predictedColor + "}");
		}
		System.out.println("Accuracy: " + accuracy);
		
	}
	
	private static ColorPoint [] train, test;
	private static double accuracy;
}
