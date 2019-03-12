import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import static java.lang.Math.round;

public class ThreeDimensionalCanvas extends Canvas implements MouseMotionListener {
	
	//AWT and SWING components
	private final JFrame frame;
	private final Canvas canvas;
	private final Graphics2D graphics;
	
	//Global Constants
	private final Color background;
	private final Color textColor;
	private final int centerX;
	private final int centerY;
	private final double FOV;
	
	//Working varibles (such as current mouse position, shapes to be rendered, and shape rotation)
	private double rotX = 0;
	private double rotY = 0;
	private double camX = 0;
	private double camY = 0;
	private double counter;
	private ArrayList<Polygon> storedFaces = new ArrayList<>();
	private ArrayList<Double> zIndex = new ArrayList<>();
	private ArrayList<Color> faceColors = new ArrayList<>();
	private ArrayList<Color> lineColors = new ArrayList<>();
	private double mouseX = -1;
	private double mouseY = -1;
	private char[] debug = "foo".toCharArray();
	
	//Constructor. By default, it accepts FOV, canvas width, canvas height, canvas center of x and y, and background color
	public ThreeDimensionalCanvas(double FOV, int width, int height, int centerX, int centerY, Color background, Color textColor){
		//Make a frame to hold display, configure
		frame = new JFrame("3D Display");
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Make a canvas to draw on, configure
		canvas = new Canvas();
		canvas.setSize(width, height);
		
		//Add canvas to frame
		frame.add(canvas);
		frame.setVisible(true);
		
		//Get canvas graphics, configure background
		graphics = (Graphics2D) canvas.getGraphics();
		this.background = background;
		this.textColor = textColor;
		graphics.setBackground(background);
		
		//Set constants
		this.centerX = centerX;
		this.centerY = centerY;
		this.FOV = FOV;
		
		//Setup event listeners
		canvas.addMouseMotionListener(this);
		addMouseMotionListener(this);
	}
	//Processes a shape for further use. Accepts lists of the x, y, and z coords of various points; Lists of all line
	//pairs and shape groups; rotation values (in degress) along the X, Y, and Z axis
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
		
		sinTheta = new double[]{Math.sin(camX), Math.sin(camY)};
		cosTheta = new double[]{Math.cos(camX), Math.cos(camY)};
		
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
		
		return new ThreeDimensionalShape(x, y, z, lineList, faceList);
	}
	//Draw a 3D Shape on top of the canvas, regardless of previously drawn objects
	//Accepts lists of point coords, lists of line pairs and shape groups, rotation for X, Y, and Z axis,
	//color of the outline, and color of the face
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
	//Adds a 3D Shape to the draw queue...same as draw3DShape, but shapes stack relative to all of their components
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
	//TODO: Figure out better clipping method
	//Draws all the shapes in the draw queue...if clear is true, the draw queue should be cleared
	public void drawShapes(boolean clear){
		
		ArrayList<Color> faceColorCache = (ArrayList<Color>) faceColors.clone();
		ArrayList<Color> lineColorCache = (ArrayList<Color>) lineColors.clone();
		ArrayList<Polygon> shapeCache = (ArrayList<Polygon>) storedFaces.clone();
		ArrayList<Double> zIndexCache = (ArrayList<Double>) zIndex.clone();
		
		int size = storedFaces.size();
		double lowZValue;
		int lowZIndex;
		for (var i = 0; i < size; i++){
			lowZValue = 1.0/0.0;
			lowZIndex = -1;
			for (var j = 0; j < storedFaces.size(); j++){
				if (lowZValue > zIndex.get(j)){
					lowZValue = zIndex.get(j).doubleValue();
					lowZIndex = j;
				}
			}
			double avgPoint = 0;
			for (var j = 0; j < storedFaces.get(lowZIndex).npoints; j++){
				avgPoint += storedFaces.get(lowZIndex).xpoints[j] + storedFaces.get(lowZIndex).ypoints[j];
			}
			avgPoint /= storedFaces.get(lowZIndex).npoints * 2;
			if (Math.abs(lowZValue / avgPoint) > 5 && lowZValue < 10) {
				graphics.setColor(faceColors.get(lowZIndex));
				graphics.fillPolygon(storedFaces.get(lowZIndex));
				graphics.setColor(lineColors.get(lowZIndex));
				graphics.drawPolygon(storedFaces.get(lowZIndex));
			}
			
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
	//Clears the canvas, and if clearShapes is true, clear the draw queue
	public void clear(boolean clearShapes){
		graphics.setColor(background);
		graphics.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
		if (clearShapes) {
			storedFaces.clear();
			zIndex.clear();
		}
	}
	//Basic update
	public void update(){
		frame.repaint();
	}
	//Get canvas width
	public int getWidth(){
		return frame.getWidth();
	}
	//Get canvas height
	public int getHeight(){
		return frame.getHeight();
	}
	//Add to shape rotation
	public void addRotX(double rotX) {
		this.rotX += rotX;
	}
	//Add to shape rotation
	public void addRotY(double rotY){
		this.rotY += rotY;
	}
	
	public void setCamX(double rotX){ this.camX = rotX; }
	public void setCamY(double rotY){ this.camY = rotY; }
	//Default loop (for testing purposes)
	public void loop(){
		clear(false);
		graphics.setColor(textColor);
		graphics.drawChars(debug.clone(), 0, debug.length, 10, 10);
		add3DShape(new double[]{-1500,-1500, 500, 500, -1500, -1500, 500, 500}, new double[]{-1000, 1000, -1000, 1000, -1000, 1000, -1000, 1000}, new double[]{-5000, -5000, -5000, -5000, -7000, -7000, -7000, -7000},
				new int[][]{},
				new int[][]{new int[]{0,2,3,1}, new int[]{4,6,7,5}, new int[]{0,4,5,1}, new int[]{1,5,7,3}, new int[]{2,6,7,3}, new int[]{0,4,6,2}},
				Math.toRadians(rotX+20), Math.toRadians(rotY), Math.toRadians(0),
				new Color(255, 255, 255), new Color(0, 11, 141, 200));
		add3DShape(new double[]{-500,-500, 1500, 1500, -500, -500, 1500, 1500}, new double[]{-1000, 1000, -1000, 1000, -1000, 1000, -1000, 1000}, new double[]{-5000, -5000, -5000, -5000, -7000, -7000, -7000, -7000},
				new int[][]{},
				new int[][]{new int[]{0,2,3,1}, new int[]{4,6,7,5}, new int[]{0,4,5,1}, new int[]{1,5,7,3}, new int[]{2,6,7,3}, new int[]{0,4,6,2}},
				Math.toRadians(rotX-20), Math.toRadians(rotY+20), Math.toRadians(0),
				new Color(255, 255, 255), new Color(122, 0, 2, 200));
		drawShapes(true);
		update();
		counter++;
	}
	//Mouse move event
	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		
		setCamY(Math.toRadians(centerX - e.getX()));
		setCamX(Math.max(Math.min(Math.toRadians(centerY - e.getY()), Math.PI / 2), -Math.PI / 2));
		
		debug = ("X rotation: " + String.valueOf(centerX - e.getX()) + "\r\nY rotation: " + String.valueOf(Math.max(Math.min(centerY - e.getY(), 90), -90))).toCharArray();
	}
	//Mouse drag event
	@Override
	public void mouseDragged(MouseEvent e) {
		if (mouseX != -1 && mouseY != -1) {
			//addRotY((mouseX - e.getX()) / 2);
			//addRotX((mouseY - e.getY()) / 2);
		}
		
		mouseX = e.getX();
		mouseY = e.getY();
	}
}
