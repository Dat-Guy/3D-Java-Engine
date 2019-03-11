import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import static java.lang.Math.round;

public class ThreeDimensionalCanvas extends Canvas implements MouseMotionListener {
	private final JFrame frame;
	private final Canvas canvas;
	private final Graphics2D graphics;
	private final Color background;
	private final int centerX;
	private final int centerY;
	private final double FOV;
	private double rotX = 0;
	private double rotY = 0;
	private double counter;
	private ArrayList<Polygon> storedFaces = new ArrayList<>();
	private ArrayList<Double> zIndex = new ArrayList<>();
	private ArrayList<Color> faceColors = new ArrayList<>();
	private ArrayList<Color> lineColors = new ArrayList<>();
	private final Object mouseLock = new Object();
	private double mouseX = -1;
	private double mouseY = -1;
	
	public ThreeDimensionalCanvas(double FOV, int width, int height, int centerX, int centerY, Color background){
		frame = new JFrame("3D Display");
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
		this.FOV = FOV;
		
		canvas.addMouseMotionListener(this);
		addMouseMotionListener(this);
	}
	private ThreeDimensionalShape processShape(double[] x, double[] y, double[] z,
	                            int[][] lineList, int[][] faceList,
	                            double thetaX, double thetaY, double thetaZ){
		
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
		
		double centerX = 0;
		double centerY = 0;
		double centerZ = 0;
		
		for (var i = 0; i < x.length; i++){
			centerX += x[i];
			centerY += y[i];
			centerZ += z[i];
		}
		
		centerX /= x.length;
		centerY /= y.length;
		centerZ /= z.length;
		
		double[] sinTheta = new double[]{Math.sin(thetaX), Math.sin(thetaY), Math.sin(thetaZ)};
		double[] cosTheta = new double[]{Math.cos(thetaX), Math.cos(thetaY), Math.cos(thetaZ)};
		
		for (var i = 0; i < pointCount; i++){
			double Y = y[i] - centerY;
			double Z = z[i] - centerZ;
			y[i] = cosTheta[0] * Y - sinTheta[0] * Z + centerY;
			z[i] = cosTheta[0] * Z + sinTheta[0] * Y + centerZ;
		}
		
		for (var i = 0; i < pointCount; i++){
			double X = x[i] - centerX;
			double Z = z[i] - centerZ;
			x[i] = cosTheta[1] * X - sinTheta[1] * Z + centerX;
			z[i] = cosTheta[1] * Z + sinTheta[1] * X + centerZ;
		}
		
		for (var i = 0; i < pointCount; i++){
			double X = x[i] - centerX;
			double Y = y[i] - centerY;
			x[i] = cosTheta[2] * X - sinTheta[2] * Y + centerX;
			y[i] = cosTheta[2] * Y + sinTheta[2] * X + centerY;
		}
		
		return new ThreeDimensionalShape(x, y, z, lineList, faceList);
	}
	public void draw3DShape(double[] x, double[] y, double[] z,
	                        int[][] lineList, int[][] faceList,
	                        double thetaX, double thetaY, double thetaZ,
	                        Color lineColor, Color faceColor){
		
		ThreeDimensionalShape shape = processShape(x, y, z, lineList, faceList, thetaX, thetaY, thetaZ);
		
		int pointCount = shape.getX().length;
		
		x = shape.getX();
		y = shape.getY();
		z = shape.getZ();
		faceList = shape.getShapes();
		
		ArrayList<Polygon> polygons = new ArrayList<>();
		for (var i = 0; i < faceList.length; i++){
			polygons.add(new Polygon());
			for (var j = 0; j < faceList[i].length; j++){
				polygons.get(i).addPoint((int) (Math.round(x[faceList[i][j]])/-z[faceList[i][j]] * FOV) + centerX, (int) (Math.round(y[faceList[i][j]])/-z[faceList[i][j]] * FOV) + centerY);
			}
			graphics.setColor(faceColor);
			graphics.fillPolygon(polygons.get(i));
			graphics.setColor(lineColor);
			graphics.drawPolygon(polygons.get(i));
		}
		
		graphics.setColor(lineColor);
		for (var i = 0; i < lineList.length; i++){
			if (lineList[i][0] >= pointCount || lineList[i][1] >= pointCount){
				throw new Error("One of your line-point pairs references a non-existient point!");
			}
			graphics.drawLine((int) round(x[lineList[i][0]]) + centerX, (int) round(y[lineList[i][0]]) + centerY, (int) round(x[lineList[i][1]]) + centerX, (int) round(y[lineList[i][1]]) + centerY);
		}
		
	}
	
	public void add3DShape(double[] x, double[] y, double[] z, int[][] lineList, int[][] faceList, double thetaX, double thetaY, double thetaZ, Color lineColor, Color faceColor){
		ThreeDimensionalShape shape = processShape(x,y,z,lineList,faceList,thetaX,thetaY,thetaZ);
		ArrayList<Polygon> polygons = shape.getPolygons(centerX, centerY, FOV);
		storedFaces.addAll(polygons);
		zIndex.addAll(shape.getZList());
		for (var i = 0; i < polygons.size(); i++) {
			faceColors.add(faceColor);
			lineColors.add(lineColor);
		}
	}
	
	public void drawShapes(boolean clear){
		ArrayList<Color> faceColorCache = (ArrayList<Color>) faceColors.clone();
		ArrayList<Color> lineColorCache = (ArrayList<Color>) lineColors.clone();
		ArrayList<Polygon> shapeCache = (ArrayList<Polygon>) storedFaces.clone();
		ArrayList<Double> zIndexCache = (ArrayList<Double>) zIndex.clone();
		
		int size = storedFaces.size();
		double lowZValue;
		int lowZIndex;
		for (var i = 0; i < size; i++){
			lowZValue = 1e300;
			lowZIndex = -1;
			for (var j = 0; j < storedFaces.size(); j++){
				if (lowZValue > zIndex.get(j)){
					lowZValue = zIndex.get(j).doubleValue();
					lowZIndex = j;
				}
			}
			graphics.setColor(faceColors.get(lowZIndex));
			graphics.fillPolygon(storedFaces.get(lowZIndex));
			graphics.setColor(lineColors.get(lowZIndex));
			graphics.drawPolygon(storedFaces.get(lowZIndex));
			
			faceColors.remove(lowZIndex);
			storedFaces.remove(lowZIndex);
			lineColors.remove(lowZIndex);
			zIndex.remove(lowZIndex);
		}
		
		if (!clear){
			faceColors = faceColorCache;
			lineColors = lineColorCache;
			storedFaces = shapeCache;
			zIndex = zIndexCache;
		}
	}
	public void clear(boolean clearShapes){
		graphics.setColor(background);
		graphics.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
		if (clearShapes) {
			storedFaces.clear();
			zIndex.clear();
		}
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
	
	public void addRotX(double rotX) {
		this.rotX += rotX;
	}
	public void addRotY(double rotY){
		this.rotY += rotY;
	}
	public void loop(){
		clear(false);
		add3DShape(new double[]{-1500,-1500, 500, 500, -1500, -1500, 500, 500}, new double[]{-1000, 1000, -1000, 1000, -1000, 1000, -1000, 1000}, new double[]{-5000, -5000, -5000, -5000, -7000, -7000, -7000, -7000},
				new int[][]{},
				new int[][]{new int[]{0,2,3,1}, new int[]{4,6,7,5}, new int[]{0,4,5,1}, new int[]{1,5,7,3}, new int[]{2,6,7,3}, new int[]{0,4,6,2}},
				Math.toRadians(rotX+20), Math.toRadians(rotY+20), Math.toRadians(0),
				new Color(255, 255, 255), new Color(0, 2, 122));
		add3DShape(new double[]{-500,-500, 1500, 1500, -500, -500, 1500, 1500}, new double[]{-1000, 1000, -1000, 1000, -1000, 1000, -1000, 1000}, new double[]{-5000, -5000, -5000, -5000, -7000, -7000, -7000, -7000},
				new int[][]{},
				new int[][]{new int[]{0,2,3,1}, new int[]{4,6,7,5}, new int[]{0,4,5,1}, new int[]{1,5,7,3}, new int[]{2,6,7,3}, new int[]{0,4,6,2}},
				Math.toRadians(rotX+20), Math.toRadians(rotY+20), Math.toRadians(0),
				new Color(255, 255, 255), new Color(122, 0, 2));
		drawShapes(true);
		update();
		counter++;
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (mouseX != -1 && mouseY != -1) {
			addRotY((mouseX - e.getX()) / 2);
			addRotX((mouseY - e.getY()) / 2);
		}
		
		mouseX = e.getX();
		mouseY = e.getY();
	}
}
