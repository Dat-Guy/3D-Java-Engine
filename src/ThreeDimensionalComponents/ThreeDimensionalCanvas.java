package ThreeDimensionalComponents;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import static java.lang.Math.*;

public class ThreeDimensionalCanvas extends Canvas implements MouseMotionListener, KeyListener {
	
	//-----------------------------------------
	//============Global Constants=============
	//-----------------------------------------
	
	//AWT and SWING components
	private final JFrame frame;
	private final Canvas canvas;
	private final Graphics2D Graphics;
	
	//Background and text colors
	private final Color background;
	private final Color TextColor;
	
	//Center of the screen
	private final int centerX;
	private final int centerY;
	
	//Field of view
	private final double FOV;
	
	//-----------------------------------------
	//============Global Variables=============
	//-----------------------------------------
	
	//Keybind Booleans
	private boolean[] keyValues = new boolean[5];
	
	//Mouse Position
	private double mouseX = -1;
	private double mouseY = -1;
	
	//General rotation of shapes in environment
	private double rotX = 0;
	private double rotY = 0;
	
	//Camera Variables
	private double camRotX = 0;
	private double camRotY = 0;
	private double camX = 0;
	private double camY = 0;
	private double camZ = 10000;
	
	//Non-essential variables for default loop
	private double volY = 0;
	
	//Short-term storage for pre-rendered shapes
	private ArrayList<Polygon> storedFaces = new ArrayList<>();
	private ArrayList<Double> zIndex = new ArrayList<>();
	private ArrayList<Color> faceColors = new ArrayList<>();
	private ArrayList<Color> lineColors = new ArrayList<>();
	
	//Default upper-left hand text. Can be modified via "updateDebug(char[])"
	private char[] debug = "Move mouse to center of display!".toCharArray();
	
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
		Graphics = (Graphics2D) canvas.getGraphics();
		this.background = background;
		this.TextColor = textColor;
		Graphics.setBackground(background);
		
		//Set constants
		this.centerX = centerX;
		this.centerY = centerY;
		
		this.FOV = FOV;
		
		//Setup event listeners
		canvas.addMouseMotionListener(this);
		addMouseMotionListener(this);
		canvas.addKeyListener(this);
	}
	//Processes a shape for further use. Accepts lists of the x, y, and z coords of various points; Lists of all line
	//pairs and shape groups; rotation values (in degress) along the X, Y, and Z axis
	private ThreeDimensionalShape processShape(double[] x, double[] y, double[] z,
	                                           @NotNull int[][] lineList, int[][] faceList,
	                                           double thetaX, double thetaY, double thetaZ){
		
		//Checks if point arrays are properly set up
		if (!(x.length == y.length && y.length == z.length)){
			throw new Error("Lengths of point arrays do not equal each other!");
		}
		//Checks if lines each reference two points by index
		for (var i = 0; i < lineList.length; i++){
			if (lineList[i].length != 2){
				throw new Error("Not all lines are two point pairs defined by index");
			}
		}
		//Checks if faces each reference AT LEAST three points by index
		for (var i = 0; i < faceList.length; i++){
			if (faceList[i].length < 3){
				throw new Error("Not all faces consist of AT LEAST three points defined by index");
			}
		}
		
		//General working array length
		final int pointCount = x.length;
		
		//Pans camera
		for (var i = 0; i < pointCount; i++){
			x[i] -= camX;
			y[i] -= camY;
			z[i] -= camZ;
		}
		
		//Sets up variables for center of shape
		double centerX = 0;
		double centerY = 0;
		double centerZ = 0;
		
		//Computes average of points, sets it to center
		for (var i = 0; i < x.length; i++){
			centerX += x[i];
			centerY += y[i];
			centerZ += z[i];
		}
		
		centerX /= x.length;
		centerY /= y.length;
		centerZ /= z.length;
		
		//General sin/cos theta calculations for shape rotation, to save processing power
		double[] sinTheta = new double[]{sin(thetaX), sin(thetaY), sin(thetaZ)};
		double[] cosTheta = new double[]{cos(thetaX), cos(thetaY), cos(thetaZ)};
		
		//Rotation on the x-axis
		for (var i = 0; i < pointCount; i++){
			double Y = y[i] - centerY;
			double Z = z[i] - centerZ;
			y[i] = cosTheta[0] * Y - sinTheta[0] * Z + centerY;
			z[i] = cosTheta[0] * Z + sinTheta[0] * Y + centerZ;
		}
		
		//Rotation on the y-axis
		for (var i = 0; i < pointCount; i++){
			double X = x[i] - centerX;
			double Z = z[i] - centerZ;
			x[i] = cosTheta[1] * X - sinTheta[1] * Z + centerX;
			z[i] = cosTheta[1] * Z + sinTheta[1] * X + centerZ;
		}
		
		//Rotation on the z-axis
		for (var i = 0; i < pointCount; i++){
			double X = x[i] - centerX;
			double Y = y[i] - centerY;
			x[i] = cosTheta[2] * X - sinTheta[2] * Y + centerX;
			y[i] = cosTheta[2] * Y + sinTheta[2] * X + centerY;
		}
		
		//General sin/cos theta calculations for camera rotation
		sinTheta = new double[]{-sin(camRotX), -sin(camRotY)};
		cosTheta = new double[]{-cos(camRotX), -cos(camRotY)};
		
		//Camera rotation on the y-axis
		for (var i = 0; i < pointCount; i++){
			double X = x[i];
			double Z = z[i];
			x[i] = -(cosTheta[1] * X - sinTheta[1] * Z);
			z[i] = cosTheta[1] * Z + sinTheta[1] * X;
		}
		
		//Camera rotation on the x-axis
		for (var i = 0; i < pointCount; i++){
			double Y = y[i];
			double Z = z[i];
			z[i] = cosTheta[0] * Z - sinTheta[0] * Y;
			y[i] = cosTheta[0] * Y + sinTheta[0] * Z;
		}
		
		//Returns the processed shape through default sorter
		return new ThreeDimensionalShape(x, y, z, lineList, faceList);
	}
	//Draw a 3D Shape on top of the canvas, regardless of previously drawn objects or the camera
	//Accepts lists of point coords, lists of line pairs and shape groups, rotation for X, Y, and Z axis,
	//color of the outline, and color of the face
	public void draw3DShape(double[] x, double[] y, double[] z,
	                        int[][] lineList, int[][] faceList,
	                        double thetaX, double thetaY, double thetaZ,
	                        Color lineColor, Color faceColor){
		//NOTE: Lines drawn on top of shapes to improve visibility!
		
		
		//Gets the shape with inputs
		ThreeDimensionalShape shape = processShape(x, y, z, lineList, faceList, thetaX, thetaY, thetaZ);
		
		//Gets the length of point array
		int pointCount = shape.getX().length;
		
		x = shape.getX();
		y = shape.getY();
		z = shape.getZ();
		faceList = shape.getShapes();
		
		//Draws the polygons, with outlines being the line color
		ArrayList<Polygon> polygons = new ArrayList<>();
		for (var i = 0; i < faceList.length; i++){
			polygons.add(new Polygon());
			for (var j = 0; j < faceList[i].length; j++){
				polygons.get(i).addPoint((int) (Math.round(x[faceList[i][j]])/-z[faceList[i][j]] * FOV) + centerX, (int) (Math.round(y[faceList[i][j]])/-z[faceList[i][j]] * FOV) + centerY);
			}
			Graphics.setColor(faceColor);
			Graphics.fillPolygon(polygons.get(i));
			Graphics.setColor(lineColor);
			Graphics.drawPolygon(polygons.get(i));
		}
		
		//Draws the lines
		Graphics.setColor(lineColor);
		for (var i = 0; i < lineList.length; i++){
			if (lineList[i][0] >= pointCount || lineList[i][1] >= pointCount){
				throw new Error("One of your line-point pairs references a non-existient point!");
			}
			Graphics.drawLine((int) round(x[lineList[i][0]]) + centerX, (int) round(y[lineList[i][0]]) + centerY, (int) round(x[lineList[i][1]]) + centerX, (int) round(y[lineList[i][1]]) + centerY);
		}
		
	}
	//Adds a 3D Shape to the draw queue...same as draw3DShape, but shapes stack relative to all of their components. Individual lines not supports
	public void add3DShape(double[] x, double[] y, double[] z, int[][] faceList, double thetaX, double thetaY, double thetaZ, Color lineColor, Color faceColor){
		ThreeDimensionalShape shape = processShape(x,y,z,new int[0][],faceList,thetaX,thetaY,thetaZ);
		ArrayList<Polygon> polygons = shape.getPolygons(centerX, centerY, FOV);
		storedFaces.addAll(polygons);
		zIndex.addAll(shape.getZList());
		for (var i = 0; i < polygons.size(); i++) {
			faceColors.add(faceColor);
			lineColors.add(lineColor);
		}
	}
	//Draws all the shapes in the draw queue...if clear is true, the draw queue should be cleared
	public void drawShapes(boolean clear){
		
		//Caches the shapes, for restoration if clear is false
		ArrayList<Color> faceColorCache = (ArrayList<Color>) faceColors.clone();
		ArrayList<Color> lineColorCache = (ArrayList<Color>) lineColors.clone();
		ArrayList<Polygon> shapeCache = (ArrayList<Polygon>) storedFaces.clone();
		ArrayList<Double> zIndexCache = (ArrayList<Double>) zIndex.clone();
		
		//Sorts all faces by average z-index. This can result in undesirable clipping, but is fast.
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
			//Draws face if Average Z index is in front of camera
			if (lowZValue < 0) {
				Graphics.setColor(faceColors.get(lowZIndex));
				Graphics.fillPolygon(storedFaces.get(lowZIndex));
				Graphics.setColor(lineColors.get(lowZIndex));
				Graphics.drawPolygon(storedFaces.get(lowZIndex));
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
		Graphics.setColor(background);
		Graphics.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
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
	public void setRotX(double rotX) {
		this.rotX = rotX;
	}
	//Add to shape rotation
	public void setRotY(double rotY){
		this.rotY = rotY;
	}
	
	public void setCamRotX(double rotX){ this.camRotX = rotX; }
	public void setCamRotY(double rotY){ this.camRotY = rotY; }
	
	public void setCamX(double camX) {
		this.camX = camX;
	}
	
	public void setCamY(double camY) {
		this.camY = camY;
	}
	
	public void setCamZ(double camZ) {
		this.camZ = camZ;
	}
	
	public void drawText(char[] text, int x, int y){
		Graphics.setColor(TextColor);
		Graphics.drawChars(text.clone(), 0, text.length, x, y);
	}
	
	public boolean[] getKeyValues() {
		return keyValues;
	}
	
	public void updateDebug(char[] text){
		debug = text;
	}
	
	//Default loop (for testing purposes)
	public void loop(){
		//Clears the canvas; Does not clear the stored shapes
		clear(false);
		//Draw debug text...somehow takes like 10s to initialize.
		drawText(debug, 10, 10);
		//Draw three default shapes
		add3DShape(new double[]{-1500,-1500, 500, 500, -1500, -1500, 500, 500}, new double[]{-1000, 1000, -1000, 1000, -1000, 1000, -1000, 1000}, new double[]{5000, 5000, 5000, 5000, 7000, 7000, 7000, 7000},
				new int[][]{new int[]{0,2,3,1}, new int[]{4,6,7,5}, new int[]{0,4,5,1}, new int[]{1,5,7,3}, new int[]{2,6,7,3}, new int[]{0,4,6,2}},
				Math.toRadians(rotX+20), Math.toRadians(rotY), Math.toRadians(0),
				new Color(255, 255, 255), new Color(0, 11, 141, 200));
		add3DShape(new double[]{-500,-500, 1500, 1500, -500, -500, 1500, 1500}, new double[]{-1000, 1000, -1000, 1000, -1000, 1000, -1000, 1000}, new double[]{5000, 5000, 5000, 5000, 7000, 7000, 7000, 7000},
				new int[][]{new int[]{0,2,3,1}, new int[]{4,6,7,5}, new int[]{0,4,5,1}, new int[]{1,5,7,3}, new int[]{2,6,7,3}, new int[]{0,4,6,2}},
				Math.toRadians(rotX-20), Math.toRadians(rotY+20), Math.toRadians(0),
				new Color(255, 255, 255), new Color(122, 0, 2, 200));
		add3DShape(new double[]{-5000,-5000, 5000, 5000, -5000, -5000, 5000, 5000}, new double[]{-2001, -2000, -2001, -2000, -2001, -2000, -2001, -2000}, new double[]{-5000, -5000, -5000, -5000, 5000, 5000, 5000, 5000},
				new int[][]{new int[]{0,2,3,1}, new int[]{4,6,7,5}, new int[]{0,4,5,1}, new int[]{1,5,7,3}, new int[]{2,6,7,3}, new int[]{0,4,6,2}},
				Math.toRadians(rotX), Math.toRadians(rotY), Math.toRadians(0),
				new Color(255, 255, 255), new Color(0, 122, 16, 80));
		//Draw the shapes, and clear the queue
		drawShapes(true);
		//Update changes to the canvas
		update();
		
		//System.out.print("UP: " + keyValues[0] + " RIGHT: " + keyValues[1] + " DOWN: " + keyValues[2] + " LEFT: " + keyValues[3] + " SPACE: " + keyValues[4]);
		
		//Update camera varibles for stuff like movement, and jumping
		setCamZ(camZ - (keyValues[0] ? 50 : 0) * cos(camRotY) + (keyValues[2] ? 50 : 0) * cos(camRotY) - (keyValues[1] ? 50 : 0) * sin(camRotY) + (keyValues[3] ? 50 : 0) * sin(camRotY));
		setCamX(camX - (keyValues[3] ? 50 : 0) * cos(camRotY) + (keyValues[1] ? 50 : 0) * cos(camRotY) - (keyValues[0] ? 50 : 0) * sin(camRotY) + (keyValues[2] ? 50 : 0) * sin(camRotY));
		volY -= 9.8;
		if (camY < -500){
			volY = Math.max(0, volY);
			if (keyValues[4]){
				volY += 200;
				camY = -700;
			}
		}
		camY += volY;
	}
	//Mouse move event
	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		
		setCamRotY(Math.toRadians(centerX - e.getX())/2);
		setCamRotX(Math.max(Math.min(Math.toRadians(e.getY() - centerY)/2, Math.PI / 3), -Math.PI / 3));
		
		debug = ("X rotation: " + ((centerX - e.getX())/2) + "\r\nY rotation: " + (Math.max(Math.min((centerY - e.getY())/2, 60), -60))).toCharArray();
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
	//Key 'typed' event (pressed and released)
	@Override
	public void keyTyped(KeyEvent keyEvent) {
	
	}
	//Key pressed (pushed down)
	@Override
	public void keyPressed(KeyEvent keyEvent) {
		if (keyEvent.getKeyCode() == KeyEvent.VK_UP){
			keyValues[0] = true;
		}
		if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT){
			keyValues[1] = true;
		}
		if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN){
			keyValues[2] = true;
		}
		if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT){
			keyValues[3] = true;
		}
		if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE){
			keyValues[4] = true;
		}
	}
	//Key released (allowed to release)
	@Override
	public void keyReleased(KeyEvent keyEvent) {
		if (keyEvent.getKeyCode() == KeyEvent.VK_UP){
			keyValues[0] = false;
		}
		if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT){
			keyValues[1] = false;
		}
		if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN){
			keyValues[2] = false;
		}
		if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT){
			keyValues[3] = false;
		}
		if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE){
			keyValues[4] = false;
		}
	}
	
}