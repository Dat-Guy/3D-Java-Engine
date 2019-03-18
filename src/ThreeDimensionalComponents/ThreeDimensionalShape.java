package ThreeDimensionalComponents;

import java.awt.*;
import java.util.ArrayList;

class ThreeDimensionalShape {
	private double[] x;
	private double[] y;
	private double[] z;
	private final ArrayList<Double> zValues = new ArrayList<>();
	private int[][] lines;
	private int[][] shapes;
	
	ThreeDimensionalShape(){
		x = new double[0];
		y = new double[0];
		z = new double[0];
		lines = new int[0][];
		shapes = new int[0][];
	}
	
	ThreeDimensionalShape(double[] X, double[] Y, double[] Z, int[][] Lines, int[][] Shapes){
		x = X;
		y = Y;
		z = Z;
		lines = Lines;
		shapes = new int[Shapes.length][];
		
		ArrayList<Integer> shapeIndex = new ArrayList<>();
		ArrayList<Double> zList = new ArrayList<>();
		
		for (var i = 0; i < Shapes.length; i++){
			double zSum = 0;
			shapeIndex.add(i);
			for (var j = 0; j < Shapes[i].length; j++){
				zSum += z[Shapes[i][j]];
				if (z[Shapes[i][j]] > 0 && Globals.Camera.strictClip){
					zSum = Globals.Numbers.infinity;
				}
			}
			zList.add(zSum / Shapes[i].length);
		}
		
		int smallZ;
		double smallValue;
		int size = Shapes.length;
		
		for (var i = 0; i < size; i++){
			smallValue = Globals.Numbers.infinity;
			smallZ = -1;
			for (var j = 0; j < shapeIndex.size(); j++){
				if (smallValue > zList.get(shapeIndex.get(j))){
					smallValue = zList.get(shapeIndex.get(j));
					smallZ = j;
				}
			}
			if (smallZ == -1){
				size = i;
				break;
			}
			shapes[i] = Shapes[shapeIndex.get(smallZ)];
			zValues.add(smallValue);
			
			shapeIndex.remove(smallZ);
		}
		
		int[][] temp = new int[size][];
		for (var i = 0; i < size; i++){
			temp[i] = shapes[i];
		}
		shapes = temp;
	}
	
	double[] getX() {
		return x;
	}
	
	double[] getY() {
		return y;
	}
	
	double[] getZ() {
		return z;
	}
	
	int[][] getLines() {
		return lines;
	}
	
	int[][] getShapes() {
		return shapes;
	}
	
	ArrayList<Polygon> getPolygons(int centerX, int centerY, double FOV) {
		ArrayList<Polygon> polygons = new ArrayList<>();
		for (var i = 0; i < shapes.length; i++){
			polygons.add(new Polygon());
			for (var j = 0; j < shapes[i].length; j++){
				polygons.get(i).addPoint((int) (Math.round(x[shapes[i][j]])/-z[shapes[i][j]] * FOV) + centerX,(int) (Math.round(y[shapes[i][j]])/-z[shapes[i][j]] * FOV) + centerY);
			}
		}
		return polygons;
	}
	ArrayList<Double> getZList(){
		return zValues;
	}
}
