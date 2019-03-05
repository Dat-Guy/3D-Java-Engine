import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static java.lang.Math.round;

public class ThreeDimensionalCanvas {
	private final JFrame frame;
	private final Canvas canvas;
	private final Graphics2D graphics;
	private final Color background;
	private final int centerX;
	private final int centerY;
	private int counter;
	
	public ThreeDimensionalCanvas(int width, int height, int centerX, int centerY, Color background){
		frame = new JFrame();
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		canvas = new Canvas();
		canvas.setSize(width, height);
		
		frame.add(canvas);
		frame.setVisible(true);
		
		graphics = (Graphics2D) canvas.getGraphics();
		this.background = background;
		graphics.setBackground(background);
		
		this.centerX = centerX;
		this.centerY = centerY;
	}
	public void draw3DShape(double[] x, double[] y, double[] z, int[][] lineList, int[][] faceList, double thetaX, double thetaY, double thetaZ, Color lineColor, Color faceColor){
		
		if (!(x.length == y.length && y.length == z.length)){
			throw new Error("Lengths of point arrays do not equal each other!");
		}
		for (var i = 0; i < lineList.length; i++){
			if (lineList[i].length != 2){
				throw new Error("Not all lines are two point pairs (defined in the order points are passed)");
			}
		}
		for (var i = 0; i < faceList.length; i++){
			if (faceList[i].length < 3){
				throw new Error("Not all faces consist of AT LEAST three points (defined in the order points are passed)");
			}
		}
		
		int pointCount = x.length;
		
		double[] sinTheta = new double[]{Math.sin(thetaX), Math.sin(thetaY), Math.sin(thetaZ)};
		double[] cosTheta = new double[]{Math.cos(thetaX), Math.cos(thetaY), Math.cos(thetaZ)};
		
		for (var i = 0; i < pointCount; i++){
			double Y = y[i];
			double Z = z[i];
			y[i] = cosTheta[0] * Y - sinTheta[0] * Z;
			z[i] = cosTheta[0] * Z + sinTheta[0] * Y;
		}
		
		for (var i = 0; i < pointCount; i++){
			double X = x[i];
			double Z = z[i];
			x[i] = cosTheta[1] * X - sinTheta[1] * Z;
			z[i] = cosTheta[1] * Z + sinTheta[1] * X;
		}
		
		for (var i = 0; i < pointCount; i++){
			double X = x[i];
			double Y = y[i];
			x[i] = cosTheta[2] * X - sinTheta[2] * Y;
			y[i] = cosTheta[2] * Y + sinTheta[2] * X;
		}
		
		graphics.setColor(lineColor);
		for (var i = 0; i < lineList.length; i++){
			if (lineList[i][0] >= pointCount || lineList[i][1] >= pointCount){
				throw new Error("One of your line-point pairs references a non-existient point!");
			}
			graphics.drawLine((int) round(x[lineList[i][0]]) + centerX, (int) round(y[lineList[i][0]]) + centerY, (int) round(x[lineList[i][1]]) + centerX, (int) round(y[lineList[i][1]]) + centerY);
		}
		ArrayList<Polygon> polygons = new ArrayList<>();
		ArrayList<Double> zList = new ArrayList<>();
		for (var i = 0; i < faceList.length; i++){
			polygons.add(new Polygon());
			double zSum = 0;
			for (var j = 0; j < faceList[i].length; j++){
				polygons.get(i).addPoint((int) Math.round(x[faceList[i][j]]) + centerX, (int) Math.round(y[faceList[i][j]]) + centerY);
				zSum += z[faceList[i][j]];
			}
			zList.add(zSum / faceList[i].length);
		}
		int smallZ;
		double smallValue;
		ArrayList<Polygon> drawPoly = new ArrayList<>();
		int size = polygons.size();
		for (var i = 0; i < size; i++){
			smallValue = 1.0/0.0;
			smallZ = -1;
			for (var j = 0; j < polygons.size(); j++){
				if (smallValue > zList.get(j)){
					smallValue = zList.get(j);
					smallZ = j;
				}
			}
			drawPoly.add(polygons.get(smallZ));
			polygons.remove(smallZ);
			zList.remove(smallZ);
		}
		for (var i = 0; i < drawPoly.size(); i++){
			graphics.setColor(lineColor);
			graphics.drawPolygon(drawPoly.get(i));
			graphics.setColor(faceColor);
			graphics.fillPolygon(drawPoly.get(i));
		}
		
	}
	public void clear(){
		graphics.setColor(background);
		graphics.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
	}
	public void update(){
		frame.repaint();
	}
	public int getWidth(){
		return frame.getWidth();
	}
	public int getHeight(){
		return frame.getHeight();
	}
	public void loop(){
		clear();
		draw3DShape(new double[]{-50,-50, 50, 50, -50, -50, 50, 50}, new double[]{-50, 50, -50, 50, -50, 50, -50, 50}, new double[]{50, 50, 50, 50, -50, -50, -50, -50},
				new int[][]{new int[]{0, 1}, new int[]{1, 3}, new int[]{2, 3}, new int[]{2, 0}, new int[]{0, 4}, new int[]{1, 5}, new int[]{2, 6}, new int[]{3, 7}, new int[]{4, 5}, new int[]{5, 7}, new int[]{6, 7}, new int[]{6, 4}, new int[]{4, 7}},
				new int[][]{new int[]{0,2,3,1}, new int[]{4,6,7,5}, new int[]{0,4,5,1}, new int[]{1,5,7,3}, new int[]{2,6,7,3}, new int[]{0,4,6,2}},
				Math.toRadians(counter), Math.toRadians(counter/3), Math.toRadians(-counter/2),
				new Color(255,11,0), new Color(23,255,0));
		update();
		counter++;
	}
}
