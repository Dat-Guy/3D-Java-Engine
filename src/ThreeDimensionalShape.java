import java.awt.*;
import java.util.ArrayList;

public class ThreeDimensionalShape {
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
	
	public ThreeDimensionalShape(double[] X, double[] Y, double[] Z, int[][] Lines, int[][] Shapes){
		x = X;
		y = Y;
		z = Z;
		lines = Lines;
		shapes = new int[Shapes.length][];
		
		ArrayList<Integer> shapeIndex = new ArrayList<>();
		ArrayList<ArrayList<Integer>> shapes1 = new ArrayList<>();
		ArrayList<Double> zList = new ArrayList<>();
		
		for (var i = 0; i < Shapes.length; i++){
			double zSum = 0;
			shapes1.add(new ArrayList<>());
			shapeIndex.add(i);
			for (var j = 0; j < Shapes[i].length; j++){
				zSum += z[Shapes[i][j]];
				shapes1.get(i).add(Shapes[i][j]);
			}
			zList.add(zSum / Shapes[i].length);
		}
		
		int smallZ;
		double smallValue;
		int size = Shapes.length;
		
		for (var i = 0; i < size; i++){
			smallValue = 1.0/0.0;
			smallZ = -1;
			for (var j = 0; j < shapeIndex.size(); j++){
				if (smallValue > zList.get(shapeIndex.get(j))){
					smallValue = zList.get(shapeIndex.get(j));
					smallZ = j;
				}
			}
			shapes[i] = Shapes[shapeIndex.get(smallZ)];
			zValues.add(smallValue);
			shapeIndex.remove(smallZ);
		}
	}
	
	private boolean valueIn(int[] a, int b){
		for (var i = 0; i < a.length; i++){
			if (a[i] == b){
				return true;
			}
		}
		return false;
	}
	
	public double[] getX() {
		return x;
	}
	
	public double[] getY() {
		return y;
	}
	
	public double[] getZ() {
		return z;
	}
	
	public int[][] getLines() {
		return lines;
	}
	
	public int[][] getShapes() {
		return shapes;
	}
	
	public ArrayList<Polygon> getPolygons(int centerX, int centerY, double FOV) {
		ArrayList<Polygon> polygons = new ArrayList<>();
		for (var i = 0; i < shapes.length; i++){
			polygons.add(new Polygon());
			for (var j = 0; j < shapes[i].length; j++){
				polygons.get(i).addPoint((int) (Math.round(x[shapes[i][j]])/-z[shapes[i][j]] * FOV) + centerX,(int) (Math.round(y[shapes[i][j]])/-z[shapes[i][j]] * FOV) + centerY);
			}
		}
		return polygons;
	}
	public ArrayList<Double> getZList(){
		return zValues;
	}
}
